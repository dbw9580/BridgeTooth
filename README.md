# BridgeTooth

BridgeTooth is a [Keepass2Android][1] plugin that sends your credentials to another device via
Bluetooth. It works in the way that emulates a Bluetooth keyboard, so in most cases no additional
software is needed on the receiver device, as long as it recognizes a regular Bluetooth keyboard.

## Usage

1. Install BridgeTooth.
2. Enable it inside Keepass2Android: Settings -> Plugins -> BridgeTooth -> check "enabled".
3. Go to one of your password entries, tap on the three-dot icon on the side of every credential field,
and you will find an option "Send via Bluetooth" in the context menu.
4. The plugin will be activated as a background service, prompting to enable Bluetooth, if it is not
already on.
5. Go to your receiver device where you would like to type passwords. Scan for new Bluetooth
devices from the receiver device, and connect to your phone.
6. Now you can send your passwords to the receiver device.

## Restrictions

As it works by emulating a keyboard, only ASCII characters can be sent. Any other characters will be
simply ignored. It is also a good idea to use only ASCII characters in your passwords.

Sometimes it does not work on the first connection attempt. Please disconnect and reconnect, then
retry sending password.

## Credits and License

This app is built upon the following open source projects:
* Keepass2Android plugin SDK, GPLv3
* the [BLE-HID-Peripheral-for-Android][2] library, Apache License 2.0
* [Markwon][3], Apache License 2.0

This project is licensed under the GNU Public License, version 3.

[1]: https://github.com/PhilippC/keepass2android
[2]: https://github.com/kshoji/BLE-HID-Peripheral-for-Android
[3]: https://github.com/noties/Markwon