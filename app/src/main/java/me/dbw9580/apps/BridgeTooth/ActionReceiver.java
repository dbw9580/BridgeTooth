package me.dbw9580.apps.BridgeTooth;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.util.Log;

import org.json.JSONObject;

import keepass2android.pluginsdk.PluginAccessException;
import keepass2android.pluginsdk.PluginActionBroadcastReceiver;
import keepass2android.pluginsdk.Strings;

public class ActionReceiver extends PluginActionBroadcastReceiver {
    private static String TAG = "ActionReceiver";

    @Override
    protected void openEntry(PluginActionBroadcastReceiver.OpenEntryAction oe) {
        try {
            for (String field: oe.getEntryFields().keySet())
            {
                oe.addEntryFieldAction("bridgetooth.send", Strings.PREFIX_STRING+field, oe.getContext().getString(R.string.action_send_passwd),
                        R.drawable.ic_send_bt, null);
            }
        } catch (PluginAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void actionSelected(ActionSelectedAction actionSelected) {
        Log.d(TAG, "received action on field " + actionSelected.getFieldId());
        if (!actionSelected.isEntryAction()) {
            String fieldId = actionSelected.getFieldId();
            String toSend = actionSelected.getEntryFields().get(fieldId.substring(Strings.PREFIX_STRING.length()));
            Intent send = new Intent(actionSelected.getContext(), BluetoothKeyboardService.class);
            send.setAction(BluetoothKeyboardService.ACTION_SEND_STRING);
            send.putExtra(BluetoothKeyboardService.EXTRA_SEND_STRING, toSend);
            actionSelected.getContext().startForegroundService(send);
        } else {
            /* do nothing on an entry */
        }
    }

    @Override
    protected void entryOutputModified(EntryOutputModifiedAction eom) {
        try {
            eom.addEntryFieldAction("bridgetooth.send", eom.getModifiedFieldId(), eom.getContext().getString(R.string.action_send_passwd),
                    R.drawable.ic_send_bt, null);
        } catch (PluginAccessException e) {
            e.printStackTrace();
        }
    }
}
