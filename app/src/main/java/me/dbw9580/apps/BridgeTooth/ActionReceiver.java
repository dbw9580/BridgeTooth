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
            oe.addEntryAction(oe.getContext().getString(R.string.action_send_passwd),
                    R.drawable.ic_send_bt, null);

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
        Intent i = new Intent(actionSelected.getContext(), MainActivity.class);
        i.setAction(MainActivity.ACTION_DISPLAY);
        i.putExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA, actionSelected.getEntryFields());
        if (!actionSelected.isEntryAction()) {
            String rawFieldId = actionSelected.getFieldId();
            if (rawFieldId.startsWith(Strings.PREFIX_STRING)) {
                i.putExtra(Strings.EXTRA_FIELD_ID, rawFieldId.split("_")[1]);
            }
        }

        i.putExtra(Strings.EXTRA_SENDER, actionSelected.getHostPackage());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        actionSelected.getContext().startActivity(i);
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
