package me.dbw9580.apps.BridgeTooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothHidDeviceAppQosSettings;
import android.bluetooth.BluetoothHidDeviceAppSdpSettings;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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


    private BluetoothHidDevice mHidDevice;
    private BluetoothAdapter mAdapter;
    private Executor mExecutor = Executors.newSingleThreadExecutor();

    private class BluetoothProfileServiceListener implements BluetoothProfile.ServiceListener {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                Log.d(TAG, "Got input device proxy");
                assert proxy != null;
                mHidDevice = (BluetoothHidDevice) proxy;

                BluetoothHidDeviceAppSdpSettings sdpSettings = new BluetoothHidDeviceAppSdpSettings(
                        BluetoothKeyboard.SDP_NAME,
                        BluetoothKeyboard.SDP_DESCRIPTION,
                        BluetoothKeyboard.SDP_RPOVIDER,
                        BluetoothHidDevice.SUBCLASS1_KEYBOARD,
                        BluetoothKeyboard.REPORT_DESCRIPTOR
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
                mHidDevice.registerApp(sdpSettings, qosInput, qosOutput, mExecutor, new CallbackHandler(mHidDevice));
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
    }

    private static class CallbackHandler extends BluetoothHidDevice.Callback {
        private static final String TAG = "CallbackHandler";
        private BluetoothHidDevice host;
        public CallbackHandler(BluetoothHidDevice host) {
            this.host = host;
        }

        @Override
        public void onAppStatusChanged(BluetoothDevice pluggedDevice, boolean registered) {
            super.onAppStatusChanged(pluggedDevice, registered);
            Log.d(TAG, "onAppStatusChanged: "
                    + (registered ? "registered" : "unregistered"));
            if (!registered) {
                host.unregisterApp();
            }
        }

        @Override
        public void onConnectionStateChanged(BluetoothDevice device, int state) {
            super.onConnectionStateChanged(device, state);
            Log.d(TAG, "onConnectionStateChanged: "
                    + (state == BluetoothProfile.STATE_CONNECTED ? "connected" : "disconnected"));
            if (state == BluetoothProfile.STATE_CONNECTED) {

            } else if (state == BluetoothProfile.STATE_DISCONNECTED) {

            }
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
            host.reportError(device, BluetoothHidDevice.ERROR_RSP_SUCCESS);
        }

        @Override
        public void onSetProtocol(BluetoothDevice device, byte protocol) {
            super.onSetProtocol(device, protocol);
            Log.d(TAG, "onSetProtocol");
            if (protocol == BluetoothHidDevice.PROTOCOL_BOOT_MODE) {
                host.reportError(device, BluetoothHidDevice.ERROR_RSP_UNSUPPORTED_REQ);
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

    public BluetoothKeyboard(BluetoothHidDevice hidDevice, BluetoothAdapter adapter) {
        this.mAdapter = adapter;
        this.mHidDevice = hidDevice;
    }
}
