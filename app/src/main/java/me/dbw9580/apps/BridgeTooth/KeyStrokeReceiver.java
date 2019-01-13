package me.dbw9580.apps.BridgeTooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class KeyStrokeReceiver extends BroadcastReceiver {
    static String TAG = "KeyStrokeReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received broadcast " + intent);
        Intent start = new Intent();
        start.setPackage(context.getPackageName())
            .setClass(context, MainActivity.class)
            .setFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(start);
    }

}
