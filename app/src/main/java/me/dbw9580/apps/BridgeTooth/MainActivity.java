package me.dbw9580.apps.BridgeTooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import keepass2android.pluginsdk.Strings;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static String TAG = "MainActivity";

    private TextView mHeader;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public static final String ACTION_ENABLE_BT = "me.dbw9580.apps.BridgeTooth.action.ENABLE_BT";
    public static final String ACTION_DISPLAY = "me.dbw9580.apps.BridgeTooth.action.DISPLAY";
    public static final String ACTION_SERVICE_RESULT = "me.dbw9580.apps.BridgeTooth.action.SERVICE_RESULT";
    public static final String EXTRA_SERVICE_RESULT = "me.dbw9580.apps.BridgeTooth.extra.SERVICE_RESULT";

    private static final int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHeader = (TextView) findViewById(R.id.header);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            mHeader.setText(R.string.text_bluetooth_not_supported);
            return;
        }

        Intent intent = getIntent();
        setupListView(intent);
        handleIntent(intent);

        Intent startServiceIntent = new Intent(this, BluetoothKeyboardService.class);
        startService(startServiceIntent);

    }

    private void handleIntent(Intent incomingIntent) {
        switch (incomingIntent.getAction()) {
            case ACTION_DISPLAY:
                setupListView(incomingIntent);
                break;
            case ACTION_ENABLE_BT:
                setupBluetooth();
                break;

            case ACTION_SERVICE_RESULT:
                String resultString = incomingIntent.getStringExtra(EXTRA_SERVICE_RESULT);
                setHeaderText(resultString);
                break;
            default:
                break;
        }

        return;

    }

    private void setupBluetooth() {
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager.getAdapter();
        if (!adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_OK:
                break;
            case RESULT_CANCELED:
                break;
            default:
                break;
        }

        return;
    }

    private void setHeaderText(String text) {
        mHeader.setText(text);
    }


    private void setupListView(Intent intent) {
        Map<String, String> dataset;
        if (intent.hasExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA)) {
            dataset = (Map<String, String>) intent.getSerializableExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA);
            Log.d(TAG, dataset.toString());
            Log.d(TAG, intent.getExtras().toString());
            if (intent.hasExtra(Strings.EXTRA_FIELD_ID) &&
                    intent.getStringExtra(Strings.EXTRA_FIELD_ID) != null) {
                Log.d(TAG, "field action, showing only selected field");
                String fieldId = intent.getStringExtra(Strings.EXTRA_FIELD_ID);
                dataset = ImmutableMap.of(
                        "name", "value",
                        fieldId, dataset.get(fieldId)
                );
            }
        } else {
            dataset = ImmutableMap.of("name", "value");
        }
        Log.d(TAG, dataset.toString());

        mRecyclerView = (RecyclerView) findViewById(R.id.entry_list_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(new EntryViewAdapter(dataset));

    }

    private void startBackgroundService() {
        Intent intent = new Intent(this, BluetoothKeyboardService.class);
        startService(intent);
    }

    private void stopBackgroundService() {
        Intent intent = new Intent(this, BluetoothKeyboardService.class);
        intent.setAction(BluetoothKeyboardService.ACTION_STOP_SERVICE);
        startService(intent);
    }

    @Override
    public void onClick(View v) {
        if (R.id.button1 == v.getId()) {
            startBackgroundService();
        } else if (R.id.button2 == v.getId()) {
            stopBackgroundService();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static class EntryHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView value;
        public EntryHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.entry_name);
            value = (TextView) v.findViewById(R.id.entry_value);
        }
    }

    private class EntryViewAdapter extends RecyclerView.Adapter<EntryHolder> {

        private List<Map.Entry<String, String>> mPairs;
        public EntryViewAdapter(Map<String, String> pairs) {
            mPairs = ImmutableList.copyOf(pairs.entrySet());
        }

        @Override
        public EntryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new EntryHolder(
                    LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.kvpair_layout, parent, false)
            );
        }

        @Override
        public void onBindViewHolder(EntryHolder holder, int position) {
            holder.name.setText(mPairs.get(position).getKey());
            holder.value.setText(mPairs.get(position).getValue());
        }

        @Override
        public int getItemCount() {
            return mPairs.size();
        }

    }
}
