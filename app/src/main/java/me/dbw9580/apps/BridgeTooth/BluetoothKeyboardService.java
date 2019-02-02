package me.dbw9580.apps.BridgeTooth;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.function.Consumer;

import jp.kshoji.blehid.HidPeripheral;
import jp.kshoji.blehid.util.BleUtils;
import jp.kshoji.blehid.KeyboardPeripheral;

/*
 * Adaptation of android.app.IntentService of Android framework
 */
public class BluetoothKeyboardService extends Service {
    private static String TAG = "BluetoothKeyboardService";

    public static final String ACTION_SEND_STRING = "me.dbw9580.apps.BridgeTooth.action.SEND_STRING";
    public static final String ACTION_STOP_SERVICE = "me.dbw9580.apps.BridgeTooth.action.STOP_SERVICE";
    private static final String ACTION_ENABLE_BT = "me.dbw9580.apps.BridgeTooth.action.ENABLE_BT";

    public static final String EXTRA_SEND_STRING = "me.dbw9580.apps.BridgeTooth.extra.SEND_STRING";

    private static final String FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_ID = "foreground_notif_channel";
    private static final int FOREGROUND_SERVICE_NOTIFICATION_ID = 1;

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
    private Keyboard mKeyboard;

    private BroadcastReceiver mBtStateReceiver = null;


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



        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        stopForeground(true);
        if (mBtStateReceiver != null) {
            this.unregisterReceiver(mBtStateReceiver);
        }
        if (mKeyboard != null) {
            mKeyboard.close();
        }

        mServiceLooper.quit();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setup() {
        if (!BleUtils.isBleSupported(this)) {
            Toast.makeText(getApplicationContext(), R.string.toast_ble_not_supported, Toast.LENGTH_SHORT).show();
            stopSelf();
            return;
        }

        mBtStateReceiver = new BluetoothStateReceiver();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(mBtStateReceiver, filter);

        mAdapter = ((BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        assert mAdapter != null;
        if (!mAdapter.isEnabled()) {
            mIsBtOn = false;
            askToEnableBt();
        } else {
            onBtEnabled();
        }

        createNotificationChannel();
        startForeground(FOREGROUND_SERVICE_NOTIFICATION_ID, getForegroundServiceNotification(mIsBtOn, false));
    }

    private void askToEnableBt() {
        Toast.makeText(getApplicationContext(), R.string.toast_ask_to_enable_bt, Toast.LENGTH_SHORT).show();
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(enableBtIntent);
    }

    private void onBtEnabled() {
        if (!BleUtils.isBlePeripheralSupported(this)) {
            Toast.makeText(getApplicationContext(), R.string.toast_ble_not_supported, Toast.LENGTH_SHORT).show();
            startService(new Intent(ACTION_STOP_SERVICE, null, this, LegacyBluetoothKeyboard.class));
            return;
        }
        mIsBtOn = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mKeyboard = (Keyboard) new LegacyBluetoothKeyboard(this, mAdapter);
        } else {
            mKeyboard = (Keyboard) new BleKeyboard(this);
        }
        mKeyboard.setOnConnectionStateChange(new Consumer<Keyboard.ConnectionState>() {
            @Override
            public void accept(Keyboard.ConnectionState connectionState) {
                Log.d(TAG, "connection state changed: " + connectionState.newState);
                Notification newNotification = getForegroundServiceNotification(
                        mIsBtOn,
                        connectionState.newState == BluetoothProfile.STATE_CONNECTED
                );
                updateNotification(newNotification);

            }
        });
        updateNotification(
                getForegroundServiceNotification(mIsBtOn, false)
        );
        mKeyboard.init();

    }

    private void onBtDisabled() {
        mIsBtOn = false;
        mKeyboard = null;
        updateNotification(
                getForegroundServiceNotification(mIsBtOn, false)
        );
    }

    public static void startSendString(Context context, String stringToSend) {
        Intent intent = new Intent(context, BluetoothKeyboardService.class);
        intent.setAction(ACTION_SEND_STRING);
        intent.putExtra(EXTRA_SEND_STRING, stringToSend);
        context.startService(intent);
    }

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
            } else if (ACTION_ENABLE_BT.equals(action)) {
                Log.d(TAG, "enable bt");
                askToEnableBt();

            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSendString(String stringToSend) {
        if (mKeyboard != null) {
            Log.d(TAG, "sending keys");
            if (stringToSend != null) {
                mKeyboard.sendKeys(stringToSend);
            }
        } else {
            askToEnableBt();
        }
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

    private void updateNotification(Notification newNotification) {
        getSystemService(NotificationManager.class).notify(
                FOREGROUND_SERVICE_NOTIFICATION_ID,
                newNotification
        );
    }

    private Notification getForegroundServiceNotification(boolean bluetoothEnabled, boolean deviceConnected) {
        Log.d(TAG, "updated notif: bt: " + bluetoothEnabled + " connected: " + deviceConnected);
        Notification.Builder builder = new Notification.Builder(getApplicationContext(), FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(getString(R.string.notif_foreground_notif_title))
                .setSmallIcon(R.drawable.ic_notifi)
                .setAutoCancel(true);
        {
            Intent contentIntent = new Intent(getApplicationContext(), MainActivity.class);
            contentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, contentIntent, PendingIntent.FLAG_ONE_SHOT);
            builder.setContentIntent(pendingIntent);
        }

        if (!bluetoothEnabled) {
            builder.setContentText(getString(R.string.text_bt_not_enabled));

            Intent intent = new Intent(this, BluetoothKeyboardService.class);
            intent.setAction(ACTION_ENABLE_BT);
            PendingIntent pendingIntent = PendingIntent.getService(
                    getApplicationContext(),
                    0,
                    intent,
                    PendingIntent.FLAG_ONE_SHOT
            );
            Notification.Action.Builder actionBuilder = new Notification.Action.Builder(
                    R.drawable.ic_notifi,
                    getString(R.string.text_notif_enable_bt),
                    pendingIntent
                    );
            builder.addAction(actionBuilder.build());
        } else if (!deviceConnected) {
            builder.setContentText(getString(R.string.text_host_not_connected));
        } else {
            builder.setContentText(getString(R.string.text_host_connected));
        }

        {
            Intent intent = new Intent(this, BluetoothKeyboardService.class);
            intent.setAction(ACTION_STOP_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getService(
                    getApplicationContext(),
                    0,
                    intent,
                    PendingIntent.FLAG_ONE_SHOT
            );
            Notification.Action.Builder actionBuilder = new Notification.Action.Builder(
                    R.drawable.ic_notifi,
                    getString(R.string.text_notif_stop_service),
                    pendingIntent
            );
            builder.addAction(actionBuilder.build());
        }

        return builder.build();
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
            channel.enableVibration(false);
            channel.enableLights(false);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

    }



}
