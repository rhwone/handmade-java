package se.abjorklund.win32.jna.xinput;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

/**
 * <p>
 * Represents the state of a controller.
 * </p>
 * <p><code>
 * typedef struct _XINPUT_STATE { <br/>
 * &nbsp;DWORD dwPacketNumber; <br/>
 * &nbsp;XINPUT_GAMEPAD Gamepad;<br/>
 * } XINPUT_STATE, *PXINPUT_STATE;</code>
 *
 * @author andrea.fantappie@gmail.com
 *
 */
public final class XInputState extends Structure {

    /**
     * State packet number. The packet number indicates whether there have been
     * any changes in the state of the controller. If the dwPacketNumber member
     * is the same in sequentially returned XINPUT_STATE structures, the
     * controller state has not changed.
     */
    public int dwPacketNumber;
    /**
     * XINPUT_GAMEPAD structure containing the current state of an Xbox 360 Controller.
     */
    public XInputGamepad Gamepad;

    @SuppressWarnings("rawtypes")
    @Override
    protected List getFieldOrder() {
        return Arrays.asList(new String[] { "dwPacketNumber", "Gamepad" });
    }

    public boolean isStateChanged(int prevoiusPacketNumber) {
        return dwPacketNumber != prevoiusPacketNumber;
    }

}
