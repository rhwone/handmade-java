package se.abjorklund.win32.jna.xinput;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class XInputGamepad extends Structure {

    public static final int XINPUT_GAMEPAD_DPAD_UP = 0x0001;
    public static final int XINPUT_GAMEPAD_DPAD_DOWN = 0x0002;
    public static final int XINPUT_GAMEPAD_DPAD_LEFT = 0x0004;
    public static final int XINPUT_GAMEPAD_DPAD_RIGHT = 0x0008;
    public static final int XINPUT_GAMEPAD_START = 0x0010;
    public static final int XINPUT_GAMEPAD_BACK = 0x0020;
    public static final int XINPUT_GAMEPAD_LEFT_THUMB = 0x0040;
    public static final int XINPUT_GAMEPAD_RIGHT_THUMB = 0x0080;
    public static final int XINPUT_GAMEPAD_LEFT_SHOULDER = 0x0100;
    public static final int XINPUT_GAMEPAD_RIGHT_SHOULDER = 0x0200;
    public static final int XINPUT_GAMEPAD_A = 0x1000;
    public static final int XINPUT_GAMEPAD_B = 0x2000;
    public static final int XINPUT_GAMEPAD_X = 0x4000;
    public static final int XINPUT_GAMEPAD_Y = 0x8000;

    /**
     * Bitmask of the device digital buttons, as follows. A set bit indicates
     * that the corresponding button is pressed.
     */
    public short wButtons;
    /**
     * The current value of the trigger analog control. The value is between 0
     * and 255.
     */
    public byte bLeftTrigger, bRightTrigger;

    /**
     * Thumbstick x-axis value. Each of the thumbstick axis members is a signed
     * value between -32768 and 32767 describing the position of the thumbstick.
     * A value of 0 is centered. Negative values signify down or to the left.
     * Positive values signify up or to the right.
     */
    public short sThumbLX, sThumbLY, sThumbRX, sThumbRY;

    @SuppressWarnings("rawtypes")
    @Override
    protected List getFieldOrder() {
        return Arrays.asList(new String[] { "wButtons", "bLeftTrigger", "bRightTrigger", "sThumbLX", "sThumbLY",
                "sThumbRX", "sThumbRY" });
    }

    /**
     * Indicate if a button is pressed.
     *
     * @param button
     *        The button bitmask, use one of the provided constants
     *
     * @return <code>true</code> if the button is pressed, <code>false</code>
     *         otherwise
     */
    public boolean isPressed(XInputGamepadButton button) {
        return (wButtons & button.getBitmask()) != 0;
    }

    public List<XInputGamepadButton> getPressedButtons() {
        return Arrays.stream(XInputGamepadButton.values()).filter(b -> isPressed(b)).collect(Collectors.toList());
    }
}
