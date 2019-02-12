package me.dbw9580.apps.BridgeTooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothHidDeviceAppQosSettings;
import android.bluetooth.BluetoothHidDeviceAppSdpSettings;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;


/** Part of jp.kshoji.blehid.KeyboardPeripheral
 * https://github.com/kshoji/BLE-HID-Peripheral-for-Android
 * Apache 2.0 License
 */
public class LegacyBluetoothKeyboard implements Keyboard {
    private static final String TAG = "LegacyBluetoothKeyboard";

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

    public static final int MODIFIER_KEY_NONE = 0;
    public static final int MODIFIER_KEY_CTRL = 1;
    public static final int MODIFIER_KEY_SHIFT = 2;
    public static final int MODIFIER_KEY_ALT = 4;

    public static final int KEY_F1 = 0x3a;
    public static final int KEY_F2 = 0x3b;
    public static final int KEY_F3 = 0x3c;
    public static final int KEY_F4 = 0x3d;
    public static final int KEY_F5 = 0x3e;
    public static final int KEY_F6 = 0x3f;
    public static final int KEY_F7 = 0x40;
    public static final int KEY_F8 = 0x41;
    public static final int KEY_F9 = 0x42;
    public static final int KEY_F10 = 0x43;
    public static final int KEY_F11 = 0x44;
    public static final int KEY_F12 = 0x45;

    public static final int KEY_PRINT_SCREEN = 0x46;
    public static final int KEY_SCROLL_LOCK = 0x47;
    public static final int KEY_CAPS_LOCK = 0x39;
    public static final int KEY_NUM_LOCK = 0x53;
    public static final int KEY_INSERT = 0x49;
    public static final int KEY_HOME = 0x4a;
    public static final int KEY_PAGE_UP = 0x4b;
    public static final int KEY_PAGE_DOWN = 0x4e;

    public static final int KEY_RIGHT_ARROW = 0x4f;
    public static final int KEY_LEFT_ARROW = 0x50;
    public static final int KEY_DOWN_ARROW = 0x51;
    public static final int KEY_UP_ARROW = 0x52;

    private static final Set<String> SHIFTED_KEYS = ImmutableSet.of(
            "A", "B", "C", "D", "E", "F", "G",
            "H", "I", "J", "K", "L", "M", "N",
            "O", "P", "Q", "R", "S", "T", "U",
            "V", "W", "X", "Y", "Z",
            "!", "@", "#", "$", "%", "^", "&",
            "*", "(", ")", "_", "+", "{", "}",
            "|", ":", "\"", "~", "<", ">", "?"
    );

    private static final ImmutableMap<String, Integer> KEY_CODES = new ImmutableMap.Builder<String, Integer>()
            .put("A", 0x04) .put("a", 0x04)
            .put("B", 0x05) .put("b", 0x05)
            .put("C", 0x06) .put("c", 0x06)
            .put("D", 0x07) .put("d", 0x07)
            .put("E", 0x08) .put("e", 0x08)
            .put("F", 0x09) .put("f", 0x09)
            .put("G", 0x0a) .put("g", 0x0a)
            .put("H", 0x0b) .put("h", 0x0b)
            .put("I", 0x0c) .put("i", 0x0c)
            .put("J", 0x0d) .put("j", 0x0d)
            .put("K", 0x0e) .put("k", 0x0e)
            .put("L", 0x0f) .put("l", 0x0f)
            .put("M", 0x10) .put("m", 0x10)
            .put("N", 0x11) .put("n", 0x11)
            .put("O", 0x12) .put("o", 0x12)
            .put("P", 0x13) .put("p", 0x13)
            .put("Q", 0x14) .put("q", 0x14)
            .put("R", 0x15) .put("r", 0x15)
            .put("S", 0x16) .put("s", 0x16)
            .put("T", 0x17) .put("t", 0x17)
            .put("U", 0x18) .put("u", 0x18)
            .put("V", 0x19) .put("v", 0x19)
            .put("W", 0x1a) .put("w", 0x1a)
            .put("X", 0x1b) .put("x", 0x1b)
            .put("Y", 0x1c) .put("y", 0x1c)
            .put("Z", 0x1d) .put("z", 0x1d)
            .put("!", 0x1e) .put("1", 0x1e)
            .put("@", 0x1f) .put("2", 0x1f)
            .put("#", 0x20) .put("3", 0x20)
            .put("$", 0x21) .put("4", 0x21)
            .put("%", 0x22) .put("5", 0x22)
            .put("^", 0x23) .put("6", 0x23)
            .put("&", 0x24) .put("7", 0x24)
            .put("*", 0x25) .put("8", 0x25)
            .put("(", 0x26) .put("9", 0x26)
            .put(")", 0x27) .put("0", 0x27)
            .put("\n", 0x28)
            .put("\b", 0x2a)
            .put("\t", 0x2b)
            .put(" ", 0x2c)
            .put("_", 0x2d) .put("-", 0x2d)
            .put("+", 0x2e) .put("=", 0x2e)
            .put("{", 0x2f) .put("[", 0x2f)
            .put("}", 0x30) .put("]", 0x30)
            .put("|", 0x31) .put("\\", 0x31)
            .put(":", 0x33) .put(";", 0x33)
            .put("\"", 0x34).put("'", 0x34)
            .put("~", 0x35) .put("`", 0x35)
            .put("<", 0x36) .put(",", 0x36)
            .put(">", 0x37) .put(".", 0x37)
            .put("?", 0x38) .put("/", 0x38)
            .build();

    public static byte modifier(final String aChar) {
        if (SHIFTED_KEYS.contains(aChar)) {
            return MODIFIER_KEY_SHIFT;
        } else {
            return 0;
        }
    }

    public static byte keyCode(final String aChar) {
        Integer code = KEY_CODES.get(aChar);
        return code != null ? code.byteValue() : 0;
    }

    private static final int KEY_PACKET_MODIFIER_KEY_INDEX = 0;
    private static final int KEY_PACKET_KEY_INDEX = 2;



    public static final String SDP_NAME = "BridgeTooth Keyboard";
    public static final String SDP_DESCRIPTION = "BridgeTooth Virtual Keyboard";
    public static final String SDP_RPOVIDER = "me.dbw9580";
    public static final byte SDP_SUBCLASS = 0x40; // the SUBCLASS1_KEYBOARD in API 28


    private Context mContext;
    private BluetoothHidDevice mHidDevice;
    private BluetoothDevice mHost;
    private BluetoothAdapter mAdapter;
    private Executor mExecutor = Executors.newSingleThreadExecutor();

    private Consumer<Keyboard.ConnectionState> onConnectionStateChangeCallback = new Consumer<ConnectionState>() {
        @Override
        public void accept(ConnectionState state) {

        }
    };

    private BluetoothProfile.ServiceListener mCallback = new  BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                Log.d(TAG, "Got input device proxy");
                assert proxy != null;
                mHidDevice = (BluetoothHidDevice) proxy;

                BluetoothHidDeviceAppSdpSettings sdpSettings = new BluetoothHidDeviceAppSdpSettings(
                        LegacyBluetoothKeyboard.SDP_NAME,
                        LegacyBluetoothKeyboard.SDP_DESCRIPTION,
                        LegacyBluetoothKeyboard.SDP_RPOVIDER,
                        BluetoothHidDevice.SUBCLASS1_KEYBOARD,
                        LegacyBluetoothKeyboard.REPORT_DESCRIPTOR
                );
                BluetoothHidDeviceAppQosSettings qosInput = new BluetoothHidDeviceAppQosSettings(
                        BluetoothHidDeviceAppQosSettings.SERVICE_GUARANTEED,
                        900,
                        9,
                        900,
                        10,
                        10
                );
                BluetoothHidDeviceAppQosSettings qosOutput = new BluetoothHidDeviceAppQosSettings(
                        BluetoothHidDeviceAppQosSettings.SERVICE_BEST_EFFORT,
                        800,
                        8,
                        500,
                        BluetoothHidDeviceAppQosSettings.MAX,
                        BluetoothHidDeviceAppQosSettings.MAX
                );
                mHidDevice.registerApp(sdpSettings, qosInput, qosOutput, mExecutor, new CallbackHandler());
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                Log.d(TAG, "Input device profile proxy disconnected");
                if (mHidDevice != null) {
                    mAdapter.closeProfileProxy(profile, mHidDevice);
                    mHidDevice = null;
                }
            }
        }
    };

    private class CallbackHandler extends BluetoothHidDevice.Callback {
        private static final String TAG = "CallbackHandler";

        @Override
        public void onAppStatusChanged(BluetoothDevice pluggedDevice, boolean registered) {
            super.onAppStatusChanged(pluggedDevice, registered);
            Log.d(TAG, "onAppStatusChanged: "
                    + (registered ? "registered" : "unregistered"));
            if (!registered) {
                mHidDevice.unregisterApp();
            }
        }

        @Override
        public void onConnectionStateChanged(BluetoothDevice device, int state) {
            super.onConnectionStateChanged(device, state);
            Log.d(TAG, "onConnectionStateChanged: "
                    + (state == BluetoothProfile.STATE_CONNECTED ? "connected" : "disconnected"));
            if (state == BluetoothProfile.STATE_CONNECTED) {
                mHost = device;
            } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                mHost = null;
            }

            onConnectionStateChangeCallback.accept(new Keyboard.ConnectionState(device, state));
        }

        @Override
        public void onGetReport(BluetoothDevice device, byte type, byte id, int bufferSize) {
            super.onGetReport(device, type, id, bufferSize);
            Log.d(TAG, "onGetReport: report type " + String.valueOf((int) type));
            if (BluetoothHidDevice.REPORT_TYPE_FEATURE == type) {

            } else if (BluetoothHidDevice.REPORT_TYPE_INPUT == type) {

            } else if (BluetoothHidDevice.REPORT_TYPE_OUTPUT == type) {

            }
        }

        @Override
        public void onSetReport(BluetoothDevice device, byte type, byte id, byte[] data) {
            super.onSetReport(device, type, id, data);
            Log.d(TAG, "onSetReport");
            // we don't care state changes made from host
            mHidDevice.reportError(device, BluetoothHidDevice.ERROR_RSP_SUCCESS);
        }

        @Override
        public void onSetProtocol(BluetoothDevice device, byte protocol) {
            super.onSetProtocol(device, protocol);
            Log.d(TAG, "onSetProtocol");
            if (protocol == BluetoothHidDevice.PROTOCOL_BOOT_MODE) {
                mHidDevice.reportError(device, BluetoothHidDevice.ERROR_RSP_UNSUPPORTED_REQ);
            } else if (protocol == BluetoothHidDevice.PROTOCOL_REPORT_MODE) {

            }
        }

        @Override
        public void onInterruptData(BluetoothDevice device, byte reportId, byte[] data) {
            super.onInterruptData(device, reportId, data);
            Log.d(TAG, "onInterruptData");
        }

        @Override
        public void onVirtualCableUnplug(BluetoothDevice device) {
            super.onVirtualCableUnplug(device);
            Log.d(TAG, "onVirtualCableUnplug");
        }
    }

    public LegacyBluetoothKeyboard(Context context, BluetoothAdapter adapter) {
        this.mContext = context;
        this.mAdapter = adapter;
    }

    @Override
    public void init() {
        if (!mAdapter.getProfileProxy(mContext, mCallback, BluetoothProfile.HID_DEVICE)) {
            throw new UnsupportedOperationException("Device does not support HID device service.");
        }
    }

    @Override
    public void close() {
        if (mHidDevice != null) {
            mAdapter.closeProfileProxy(BluetoothProfile.HID_DEVICE, mHidDevice);
            mHidDevice = null;
        }

    }

    @Override
    public void sendKeys(final String text) {
        if (mHidDevice != null && mHost != null) {
            for (int i = 0; i < text.length(); i++) {
                final String key = text.substring(i, i + 1);
                final byte[] report = new byte[8];
                report[KEY_PACKET_MODIFIER_KEY_INDEX] = modifier(key);
                report[KEY_PACKET_KEY_INDEX] = keyCode(key);

                addInputReport(report, mHost);
                sendKeyUp();
            }
            sendKeyUp();
        }
    }

    @Override
    public void sendKeyDown(final byte modifier, final byte key) {
        if (mHidDevice != null && mHost != null) {
            final byte[] report = new byte[8];
            report[KEY_PACKET_MODIFIER_KEY_INDEX] = modifier;
            report[KEY_PACKET_KEY_INDEX] = key;

            addInputReport(report, mHost);
        }

    }

    private static final byte[] EMPTY_REPORT = new byte[8];

    @Override
    public void sendKeyUp() {
        if (mHidDevice != null && mHost != null) {
            addInputReport(EMPTY_REPORT, mHost);
        }
    }

    @Override
    public void setOnConnectionStateChange(Consumer<ConnectionState> callback) {
        onConnectionStateChangeCallback = callback;
    }

    private void addInputReport(final byte[] report, final BluetoothDevice host) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mHidDevice.sendReport(host, 0, report);
            }
        });
    }


}
