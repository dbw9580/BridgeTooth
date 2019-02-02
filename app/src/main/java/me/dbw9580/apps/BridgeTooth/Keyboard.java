package me.dbw9580.apps.BridgeTooth;

import android.bluetooth.BluetoothDevice;

import java.util.function.Consumer;

public interface Keyboard {
    void init();
    void close();
    void sendKeys(final String text);
    void sendKeyDown(final byte modifier, final byte key);
    void sendKeyUp();

    void setOnConnectionStateChange(Consumer<ConnectionState> callback);

    class ConnectionState {
        public BluetoothDevice device;

        /**
         * Constants from BlutoothProfile.STATE_*
         */
        public int newState;

        public ConnectionState(BluetoothDevice device, int newState) {
            this.device = device;
            this.newState = newState;
        }
    }
}
