package me.dbw9580.apps.BridgeTooth;

public interface Keyboard {
    void init();
    void close();
    void sendKeys(final String text);
    void sendKeyDown(final byte modifier, final byte key);
    void sendKeyUp();
}
