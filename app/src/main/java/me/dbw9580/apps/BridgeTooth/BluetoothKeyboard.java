package me.dbw9580.apps.BridgeTooth;

/* Part of jp.kshoji.blehid.KeyboardPeripheral
 * https://github.com/kshoji/BLE-HID-Peripheral-for-Android
 * Apache 2.0 License
 */
public class BluetoothKeyboard {
    private static final String TAG = "BluetoothKeyboard";

    /**
     * Main items
     */
    protected static byte INPUT(final int size) {
        return (byte) (0x80 | size);
    }
    protected static byte OUTPUT(final int size) {
        return (byte) (0x90 | size);
    }
    protected static byte COLLECTION(final int size) {
        return (byte) (0xA0 | size);
    }
    protected static byte FEATURE(final int size) {
        return (byte) (0xB0 | size);
    }
    protected static byte END_COLLECTION(final int size) {
        return (byte) (0xC0 | size);
    }

    /**
     * Global items
     */
    protected static byte USAGE_PAGE(final int size) {
        return (byte) (0x04 | size);
    }
    protected static byte LOGICAL_MINIMUM(final int size) {
        return (byte) (0x14 | size);
    }
    protected static byte LOGICAL_MAXIMUM(final int size) {
        return (byte) (0x24 | size);
    }
    protected static byte PHYSICAL_MINIMUM(final int size) {
        return (byte) (0x34 | size);
    }
    protected static byte PHYSICAL_MAXIMUM(final int size) {
        return (byte) (0x44 | size);
    }
    protected static byte UNIT_EXPONENT(final int size) {
        return (byte) (0x54 | size);
    }
    protected static byte UNIT(final int size) {
        return (byte) (0x64 | size);
    }
    protected static byte REPORT_SIZE(final int size) {
        return (byte) (0x74 | size);
    }
    protected static byte REPORT_ID(final int size) {
        return (byte) (0x84 | size);
    }
    protected static byte REPORT_COUNT(final int size) {
        return (byte) (0x94 | size);
    }

    /**
     * Local items
     */
    protected static byte USAGE(final int size) {
        return (byte) (0x08 | size);
    }
    protected static byte USAGE_MINIMUM(final int size) {
        return (byte) (0x18 | size);
    }
    protected static byte USAGE_MAXIMUM(final int size) {
        return (byte) (0x28 | size);
    }

    protected static byte LSB(final int value) {
        return (byte) (value & 0xff);
    }
    protected static byte MSB(final int value) {
        return (byte) (value >> 8 & 0xff);
    }

    public static final byte[] REPORT_DESCRIPTOR = {
            USAGE_PAGE(1),      0x01,       // Generic Desktop Ctrls
            USAGE(1),           0x06,       // Keyboard
            COLLECTION(1),      0x01,       // Application
            USAGE_PAGE(1),      0x07,       //   Kbrd/Keypad
            USAGE_MINIMUM(1), (byte) 0xE0,
            USAGE_MAXIMUM(1), (byte) 0xE7,
            LOGICAL_MINIMUM(1), 0x00,
            LOGICAL_MAXIMUM(1), 0x01,
            REPORT_SIZE(1),          0x01,       //   1 byte (Modifier)
            REPORT_COUNT(1),    0x08,
            INPUT(1),           0x02,       //   Data,Var,Abs,No Wrap,Linear,Preferred State,No Null Position
            REPORT_COUNT(1),    0x01,       //   1 byte (Reserved)
            REPORT_SIZE(1),          0x08,
            INPUT(1),           0x01,       //   Const,Array,Abs,No Wrap,Linear,Preferred State,No Null Position
            REPORT_COUNT(1),    0x05,       //   5 bits (Num lock, Caps lock, Scroll lock, Compose, Kana)
            REPORT_SIZE(1),          0x01,
            USAGE_PAGE(1),      0x08,       //   LEDs
            USAGE_MINIMUM(1),   0x01,       //   Num Lock
            USAGE_MAXIMUM(1),   0x05,       //   Kana
            OUTPUT(1),          0x02,       //   Data,Var,Abs,No Wrap,Linear,Preferred State,No Null Position,Non-volatile
            REPORT_COUNT(1),    0x01,       //   3 bits (Padding)
            REPORT_SIZE(1),          0x03,
            OUTPUT(1),          0x01,       //   Const,Array,Abs,No Wrap,Linear,Preferred State,No Null Position,Non-volatile
            REPORT_COUNT(1),    0x06,       //   6 bytes (Keys)
            REPORT_SIZE(1),          0x08,
            LOGICAL_MINIMUM(1), 0x00,
            LOGICAL_MAXIMUM(1), 0x65,       //   101 keys
            USAGE_PAGE(1),      0x07,       //   Kbrd/Keypad
            USAGE_MINIMUM(1),   0x00,
            USAGE_MAXIMUM(1),   0x65,
            INPUT(1),           0x00,       //   Data,Array,Abs,No Wrap,Linear,Preferred State,No Null Position
            END_COLLECTION(0),
    };



    public static final String SDP_NAME = "BridgeTooth Keyboard";
    public static final String SDP_DESCRIPTION = "BridgeTooth Virtual Keyboard";
    public static final String SDP_RPOVIDER = "me.dbw9580";
    public static final byte SDP_SUBCLASS = 0x40; // the SUBCLASS1_KEYBOARD in API 28

}
