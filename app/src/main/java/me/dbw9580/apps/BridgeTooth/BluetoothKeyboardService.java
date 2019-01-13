package me.dbw9580.apps.BridgeTooth;

import android.annotation.WorkerThread;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDeviceAppConfiguration;
import android.bluetooth.BluetoothHidDeviceAppQosSettings;
import android.bluetooth.BluetoothHidDeviceAppSdpSettings;
import android.bluetooth.BluetoothHidDeviceCallback;
import android.bluetooth.BluetoothInputHost;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.IBluetoothInputDevice;
import android.bluetooth.IBluetoothInputHost;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import static android.app.Notification.DEFAULT_ALL;

/*
 * Adaptation of android.app.IntentService of Android framework
 */
public class BluetoothKeyboardService extends Service {
    private static String TAG = "BluetoothKeyboardService";

    public static final String ACTION_SEND_STRING = "me.dbw9580.apps.BridgeTooth.action.SEND_STRING";
    public static final String ACTION_STOP_SERVICE = "me.dbw9580.apps.BridgeTooth.action.STOP_SERVICE";
    private static final String ACTION_SETUP_APP = "me.dbw9580.apps.BridgeTooth.action.SETUP_APP";

    public static final String EXTRA_SEND_STRING = "me.dbw9580.apps.BridgeTooth.extra.SEND_STRING";

    private static final String FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_ID = "foreground_notif_channel";

    private volatile Looper mServiceLooper;
    private volatile Handler mServiceHandler;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            onHandleIntent((Intent)msg.obj);
        }
    }


    private boolean mIsBtOn = false;
    private BluetoothAdapter mAdapter = null;
    private BluetoothInputHost mHidDevice = null;

    private BroadcastReceiver mBtStateReceiver = null;

    private final BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.INPUT_HOST) {
                Log.d(TAG, "Got input device proxy");
                assert proxy != null;
                mHidDevice = (BluetoothInputHost) proxy;

                BluetoothHidDeviceAppSdpSettings sdpSettings = new BluetoothHidDeviceAppSdpSettings(
                        BluetoothKeyboard.SDP_NAME,
                        BluetoothKeyboard.SDP_DESCRIPTION,
                        BluetoothKeyboard.SDP_RPOVIDER,
                        BluetoothKeyboard.SDP_SUBCLASS,
                        BluetoothKeyboard.REPORT_DESCRIPTOR
                );
                BluetoothHidDeviceAppQosSettings qosInput = new BluetoothHidDeviceAppQosSettings(
                        BluetoothHidDeviceAppQosSettings.SERVICE_GUARANTEED,
                        900,
                        9,
                        900,
                        10,
                        10
                );
                BluetoothHidDeviceAppQosSettings qosOutput = new BluetoothHidDeviceAppQosSettings(
                        BluetoothHidDeviceAppQosSettings.SERVICE_BEST_EFFORT,
                        800,
                        8,
                        500,
                        BluetoothHidDeviceAppQosSettings.MAX,
                        BluetoothHidDeviceAppQosSettings.MAX
                );
                mHidDevice.registerApp(sdpSettings, qosInput, qosOutput, new CallbackHandler(mHidDevice));


            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.INPUT_HOST) {
                Log.d(TAG, "Input device profile proxy disconnected");
                if (mHidDevice != null) {
                    mAdapter.closeProfileProxy(profile, mHidDevice);
                    mHidDevice = null;
                }
            }
        }
    };

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();

        HandlerThread thread = new HandlerThread(TAG);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        setup();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: " + intent);
        onStart(intent, startId);

        createNotificationChannel();
        startForeground(1, postForegroundServiceNotification());

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        stopForeground(true);
        if (mBtStateReceiver != null) {
            this.unregisterReceiver(mBtStateReceiver);
        }
        if (mHidDevice != null) {
            mAdapter.closeProfileProxy(BluetoothProfile.INPUT_HOST, mHidDevice);
            mHidDevice = null;
        }
        mServiceLooper.quit();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setup() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Intent showBtNotSupported = new Intent(this, MainActivity.class);
            showBtNotSupported.setAction(MainActivity.ACTION_SERVICE_RESULT);
            showBtNotSupported.putExtra(MainActivity.EXTRA_SERVICE_RESULT, getString(R.string.text_bluetooth_not_supported));
            showBtNotSupported.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(showBtNotSupported);
            stopSelf();
            return;
        }

        mBtStateReceiver = new BluetoothStateReceiver();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(mBtStateReceiver, filter);

        mAdapter = ((BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        if (!mAdapter.isEnabled()) {
            mIsBtOn = false;
            askToEnableBt();
        } else {
            onBtEnabled();
        }
    }

    private void askToEnableBt() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(enableBtIntent);
    }

    private void onBtEnabled() {
        mIsBtOn = true;
        /*Intent setupBtApp = new Intent(this, BluetoothKeyboardService.class);
        setupBtApp.setAction(ACTION_SETUP_APP);
        startService(setupBtApp);*/
        mAdapter.getProfileProxy(this, mProfileListener, BluetoothProfile.INPUT_HOST);
    }

    private void onBtDisabled() {
        mIsBtOn = false;
        if (mHidDevice != null) {
            mAdapter.closeProfileProxy(BluetoothProfile.INPUT_HOST, mHidDevice);
            mHidDevice = null;
        }
    }

    public static void startSendString(Context context, String stringToSend) {
        Intent intent = new Intent(context, BluetoothKeyboardService.class);
        intent.setAction(ACTION_SEND_STRING);
        intent.putExtra(EXTRA_SEND_STRING, stringToSend);
        context.startService(intent);
    }

    @WorkerThread
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            Log.d(TAG, "received intent: " + action);
            if (ACTION_SEND_STRING.equals(action)) {
                final String toSend = intent.getStringExtra(EXTRA_SEND_STRING);
                handleActionSendString(toSend);
            } else if (ACTION_STOP_SERVICE.equals(action)) {
                Log.d(TAG, "received intent to stop");
                stopForeground(true);
                stopSelf();
            } else if (ACTION_SETUP_APP.equals(action)) {
                Log.d(TAG, "setting up HID service");

            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSendString(String stringToSend) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private class BluetoothStateReceiver extends BroadcastReceiver {

        public BluetoothStateReceiver() {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
                case BluetoothAdapter.STATE_ON:
                    onBtEnabled();
                    break;
                case BluetoothAdapter.STATE_OFF:
                    onBtDisabled();
                    break;
                default:
                    break;
            }

        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            NotificationChannel channel = new NotificationChannel(
                    FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_ID,
                    name,
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

    }

    private Notification postForegroundServiceNotification() {
        Intent stopServiceIntent = new Intent(this, BluetoothKeyboardService.class);
        stopServiceIntent.setAction(ACTION_STOP_SERVICE);
        PendingIntent stopServicePendingIntent = PendingIntent.getService(
                getApplicationContext(),
                0,
                stopServiceIntent,
                PendingIntent.FLAG_ONE_SHOT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(getString(R.string.notif_foreground_notif_title))
                .setSmallIcon(R.drawable.ic_notifi)
                .setContentText(getString(R.string.notif_foreground_notif_text))
                .setContentIntent(stopServicePendingIntent)
                .setDefaults(DEFAULT_ALL)
                .setAutoCancel(true);
        return builder.build();
    }


    private static class CallbackHandler extends BluetoothHidDeviceCallback {
        private static final String TAG = "CallbackHandler";
        private BluetoothInputHost host;
        public CallbackHandler(BluetoothInputHost host) {
            this.host = host;
        }

        @Override
        public void onAppStatusChanged(BluetoothDevice pluggedDevice, BluetoothHidDeviceAppConfiguration config, boolean registered) {
            super.onAppStatusChanged(pluggedDevice, config, registered);
            Log.d(TAG, "onAppStatusChanged: "
                    + (registered ? "registered" : "unregistered"));
            if (!registered) {
                host.unregisterApp(config);
            }
        }

        @Override
        public void onConnectionStateChanged(BluetoothDevice device, int state) {
            super.onConnectionStateChanged(device, state);
            Log.d(TAG, "onConnectionStateChanged: "
                    + (state == BluetoothProfile.STATE_CONNECTED ? "connected" : "disconnected"));
            if (state == BluetoothProfile.STATE_CONNECTED) {

            } else if (state == BluetoothProfile.STATE_DISCONNECTED) {

            }
        }

        @Override
        public void onGetReport(BluetoothDevice device, byte type, byte id, int bufferSize) {
            super.onGetReport(device, type, id, bufferSize);
            Log.d(TAG, "onGetReport: report type " + String.valueOf((int) type));
            if (BluetoothInputHost.REPORT_TYPE_FEATURE == type) {

            } else if (BluetoothInputHost.REPORT_TYPE_INPUT == type) {

            } else if (BluetoothInputHost.REPORT_TYPE_OUTPUT == type) {

            }
        }

        @Override
        public void onSetReport(BluetoothDevice device, byte type, byte id, byte[] data) {
            super.onSetReport(device, type, id, data);
            Log.d(TAG, "onSetReport");
            // we don't care state changes made from host
            host.reportError(device, BluetoothInputHost.ERROR_RSP_SUCCESS);
        }

        @Override
        public void onSetProtocol(BluetoothDevice device, byte protocol) {
            super.onSetProtocol(device, protocol);
            Log.d(TAG, "onSetProtocol");
            if (protocol == BluetoothInputHost.PROTOCOL_BOOT_MODE) {
                host.reportError(device, BluetoothInputHost.ERROR_RSP_UNSUPPORTED_REQ);
            } else if (protocol == BluetoothInputHost.PROTOCOL_REPORT_MODE) {

            }
        }

        @Override
        public void onIntrData(BluetoothDevice device, byte reportId, byte[] data) {
            super.onIntrData(device, reportId, data);
            Log.d(TAG, "onIntrData");
        }

        @Override
        public void onVirtualCableUnplug(BluetoothDevice device) {
            super.onVirtualCableUnplug(device);
            Log.d(TAG, "onVirtualCableUnplug");
        }
    };

}
