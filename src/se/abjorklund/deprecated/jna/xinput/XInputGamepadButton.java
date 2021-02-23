package se.abjorklund.deprecated.jna.xinput;

public enum XInputGamepadButton {

    PAD_UP(XInputGamepad.XINPUT_GAMEPAD_DPAD_UP),
    PAD_DOWN(XInputGamepad.XINPUT_GAMEPAD_DPAD_DOWN),
    PAD_LEFT(XInputGamepad.XINPUT_GAMEPAD_DPAD_LEFT),
    PAD_RIGHT(XInputGamepad.XINPUT_GAMEPAD_DPAD_RIGHT),
    START(XInputGamepad.XINPUT_GAMEPAD_START),
    BACK(XInputGamepad.XINPUT_GAMEPAD_BACK),
    LEFT_THUMB(XInputGamepad.XINPUT_GAMEPAD_LEFT_THUMB),
    RIGHT_THUMB(XInputGamepad.XINPUT_GAMEPAD_RIGHT_THUMB),
    LEFT_SHOULDER(XInputGamepad.XINPUT_GAMEPAD_LEFT_SHOULDER),
    RIGHT_SHOULDER(XInputGamepad.XINPUT_GAMEPAD_RIGHT_SHOULDER),
    A(XInputGamepad.XINPUT_GAMEPAD_A),
    B(XInputGamepad.XINPUT_GAMEPAD_B),
    X(XInputGamepad.XINPUT_GAMEPAD_X),
    Y(XInputGamepad.XINPUT_GAMEPAD_Y);

    private final int bitmask;

    private XInputGamepadButton(int bitmask) {
        this.bitmask = bitmask;
    }

    public int getBitmask() {
        return bitmask;
    }

}
