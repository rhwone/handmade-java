package se.abjorklund.win32.jna.xinput;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public final class XInputBatteryInformation extends Structure {

    /**
     * This device is not connected
     */
    public static final int BATTERY_TYPE_DISCONNECTED = 0x00;
    /**
     * Wired device, no battery
     */
    public static final int BATTERY_TYPE_WIRED = 0x01;
    /**
     * Alkaline battery source
     */
    public static final int BATTERY_TYPE_ALKALINE = 0x02;
    /**
     * Nickel Metal Hydride battery source
     */
    public static final int BATTERY_TYPE_NIMH = 0x03;
    /**
     * Cannot determine the battery type
     */
    public static final int BATTERY_TYPE_UNKNOWN = 0xFF;

    // These are only valid for wireless, connected devices, with known battery types
    // The amount of use time remaining depends on the type of device.
    public static final int BATTERY_LEVEL_EMPTY = 0x00;
    public static final int BATTERY_LEVEL_LOW = 0x01;
    public static final int BATTERY_LEVEL_MEDIUM = 0x02;
    public static final int BATTERY_LEVEL_FULL = 0x03;

    /**
     * The type of battery. BatteryType will be one of the XInput battery type
     * constants.
     */
    public byte BatteryType;
    /**
     * he charge state of the battery. This value is only valid for wireless
     * devices with a known battery type. BatteryLevel will be one XInput
     * battery level constants.
     */
    public byte BatteryLevel;

    @SuppressWarnings("rawtypes")
    @Override
    protected List getFieldOrder() {
        return Arrays.asList(new String[] { "BatteryType", "BatteryLevel" });
    }

}
