package me.dbw9580.apps.BridgeTooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class KeyStrokeReceiver extends BroadcastReceiver {
    static String TAG = "KeyStrokeReceiver";
    public static final String ACTION_SEND_KEY = "BridgeTooth.SEND_KEY";
    public static final String EXTRA_DATA = "BridgeTooth.DATA";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received broadcast " + intent);
        if (ACTION_SEND_KEY.equals(intent.getAction()) &&
                intent.getStringExtra(EXTRA_DATA) != null) {
            Log.d(TAG, "starting service");
            Intent send = new Intent(context, BluetoothKeyboardService.class);
            send.setAction(BluetoothKeyboardService.ACTION_SEND_STRING);
            String toSend = intent.getStringExtra(EXTRA_DATA);
            send.putExtra(BluetoothKeyboardService.EXTRA_SEND_STRING, toSend);
            context.startForegroundService(send);
        }
    }

}
