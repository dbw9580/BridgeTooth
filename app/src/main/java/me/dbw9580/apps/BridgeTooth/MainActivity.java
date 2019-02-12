package me.dbw9580.apps.BridgeTooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ru.noties.markwon.Markwon;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static String TAG = "MainActivity";

    private TextView mContent;

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

        mContent = (TextView) findViewById(R.id.welcome);
        try {
            InputStream in = getAssets().open("README.md");
            String readmeMarkdown = CharStreams.toString(new InputStreamReader(in, Charsets.UTF_8));
            Markwon.setMarkdown(mContent, readmeMarkdown);
            in.close();
        } catch (IOException e) {
            Markwon.setMarkdown(mContent, "Failed to read document.");
        }

        Intent intent = getIntent();
        handleIntent(intent);


    }

    private void handleIntent(Intent incomingIntent) {
        if (incomingIntent.getAction() == null) return;
        switch (incomingIntent.getAction()) {
            case ACTION_DISPLAY:
                break;
            case ACTION_ENABLE_BT:
                setupBluetooth();
                break;

            case ACTION_SERVICE_RESULT:
                String resultString = incomingIntent.getStringExtra(EXTRA_SERVICE_RESULT);
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
        } else if (R.id.button3 == v.getId()) {
            Intent intent = new Intent(this, BluetoothKeyboardService.class);
            intent.setAction(BluetoothKeyboardService.ACTION_SEND_STRING);
            intent.putExtra(BluetoothKeyboardService.EXTRA_SEND_STRING, ((EditText) findViewById(R.id.send_string)).getText().toString());
            startService(intent);
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
}
