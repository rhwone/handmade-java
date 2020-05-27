package se.abjorklund.win32.jna.xinput;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * Specifies motor speed levels for the vibration function of a controller.
 * </p>
 *
 * <p>
 * <code>typedef struct _XINPUT_VIBRATION {<br/>&nbsp;WORD wLeftMotorSpeed;<br/>&nbsp;WORD
 * wRightMotorSpeed;<br/>} XINPUT_VIBRATION, *PXINPUT_VIBRATION;</code>
 * </p>
 *
 * <p>
 * The left motor is the low-frequency rumble motor. The right motor is the
 * high-frequency rumble motor. The two motors are not the same, and they create
 * different vibration effects.
 * </p>
 *
 *
 */
public final class XInputVibration extends Structure {

    /**
     * Speed of the left motor. Valid values are in the range 0 to 65,535. Zero
     * signifies no motor use; 65,535 signifies 100 percent motor use.
     */
    public int wLeftMotorSpeed, wRightMotorSpeed;

    @SuppressWarnings("rawtypes")
    @Override
    protected List getFieldOrder() {
        return Arrays.asList(new String[] { "wLeftMotorSpeed", "wRightMotorSpeed" });
    }

}
