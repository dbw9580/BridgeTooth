package me.dbw9580.apps.BridgeTooth;

import android.content.Context;

import java.util.function.Consumer;

import jp.kshoji.blehid.HidPeripheral;
import jp.kshoji.blehid.KeyboardPeripheral;

public class BleKeyboard implements Keyboard {
    private KeyboardPeripheral mKeyboard;
    private Consumer<Keyboard.ConnectionState> onConnectionStateChangeCallback = new Consumer<ConnectionState>() {
        @Override
        public void accept(ConnectionState state) {

        }
    };

    public BleKeyboard(Context context) {
        mKeyboard = new KeyboardPeripheral(context);
        mKeyboard.setDeviceName("BridgeTooth keyboard");
        mKeyboard.setConnectionStateCallback(new Consumer<HidPeripheral.ConnectionState>() {
            @Override
            public void accept(HidPeripheral.ConnectionState connectionState) {
                Keyboard.ConnectionState state = new Keyboard.ConnectionState(
                        connectionState.device,
                        connectionState.newState
                );
                onConnectionStateChangeCallback.accept(state);
            }
        });
    }

    @Override
    public void init() {
        mKeyboard.startAdvertising();
    }

    @Override
    public void close() {
        mKeyboard.stopAdvertising();
    }

    @Override
    public void sendKeys(final String text) {
        mKeyboard.sendKeys(text);
    }

    @Override
    public void sendKeyDown(final byte modifier, final byte key) {
        mKeyboard.sendKeyDown(modifier, key);
    }

    @Override
    public void sendKeyUp() {
        mKeyboard.sendKeyUp();
    }

    @Override
    public void setOnConnectionStateChange(Consumer<Keyboard.ConnectionState> callback) {
        onConnectionStateChangeCallback = callback;
    }
}
