package se.abjorklund.deprecated.jna.xinput;

import com.sun.jna.Library;

public interface XInput extends Library {

    String XINPUT_DLL_9_1_0 = "xinput9_1_0.dll";
    String XINPUT_DLL_1_3 = "xinput1_3.dll";
    String XINPUT_DLL_1_4 = "xinput1_4.dll";

    int ERROR_DEVICE_NOT_CONNECTED = 0x48F;
    int BATTERY_DEVTYPE_GAMEPAD = 0x00;
    int BATTERY_DEVTYPE_HEADSET = 0x01;

    /**
     * User index definitions
     */
    int XUSER_MAX_COUNT = 4;

    /**
     * <p>
     * Retrieves the current state of the specified controller.
     * </p>
     *
     * <code>
     * DWORD XInputGetState(<br/>
     * &nbsp;_In_ DWORD dwUserIndex,<br/>
     * &nbsp;_Out_ XINPUT_STATE *pState<br/>
     * )
     * </code>
     *
     * <p>
     * Windows 8 (XInput 1.4), DirectX SDK (XInput 1.3), Windows Vista (XInput
     * 9.1.0)
     * </p>
     *
     * @param dwUserIndex
     *        Index of the user's controller. Can be a value from 0 to 3.
     * @param pState
     *        Pointer to an XINPUT_STATE structure that receives the current
     *        state of the controller.
     *
     * @return ERROR_SUCCESS or ERROR_DEVICE_NOT_CONNECTED
     */
    int XInputGetState(int dwUserIndex, XInputState pState);

    /**
     * <p>
     * Sends data to a connected controller. This function is used to activate
     * the vibration function of a controller.
     * </p>
     *
     * <p>
     * <code>DWORD XInputSetState(<br/>
     * &nbsp; _In_ DWORD dwUserIndex,<br/>
     * &nbsp; _Inout_ XINPUT_VIBRATION *pVibration <br/>
     * )</code>
     * </p>
     *
     * <p>
     * Windows 8 (XInput 1.4), DirectX SDK (XInput 1.3), Windows Vista (XInput
     * 9.1.0)
     * </p>
     *
     * @param dwUserIndex
     *        Index of the user's controller. Can be a value from 0 to 3.
     * @param pVibration
     *        Pointer to an XINPUT_VIBRATION structure containing the vibration
     *        information to send to the controller.
     * @return
     */
    int XInputSetState(int dwUserIndex, XInputVibration pVibration);

    /**
     * <p>
     * Retrieves the battery type and charge status of a wireless controller.
     * </p>
     *
     * <p>
     * <code>DWORD XInputGetBatteryInformation(<br/>&nbsp;
     * _In_   DWORD dwUserIndex,<br/>&nbsp;
     * _In_   BYTE devType,<br/>&nbsp;
     * _Out_  XINPUT_BATTERY_INFORMATION *pBatteryInformation<br/>
     * );</code>
     * <p>
     *
     * <p>
     * Windows 8 (XInput 1.4), DirectX SDK (XInput 1.3)
     * </p>
     *
     * @param dwUserIndex
     *        Index of the signed-in gamer associated with the device. Can be a
     *        value in the range 0-XUSER_MAX_COUNT-1.
     * @param devType
     *        Specifies which device associated with this user index should be
     *        queried. Must be BATTERY_DEVTYPE_GAMEPAD or
     *        BATTERY_DEVTYPE_HEADSET.
     * @param pBatteryInformation
     *        Pointer to an XINPUT_BATTERY_INFORMATION structure that receives
     *        the battery information.
     * @return If the function succeeds, the return value is ERROR_SUCCESS.
     */
    int XInputGetBatteryInformation(int dwUserIndex, byte devType, XInputBatteryInformation pBatteryInformation);

    /**
     * <p>
     * Sets the reporting state of XInput.
     * </p>
     *
     * <p>
     * <code>void XInputEnable(<br/>&nbsp;_In_  BOOL enable<br/>);</code>
     * </p>
     *
     * <p>
     * Windows 8 (XInput 1.4), DirectX SDK (XInput 1.3)
     * </p>
     *
     * @param enable
     *        If enable is FALSE, XInput will only send neutral data in response
     *        to XInputGetState (all buttons up, axes centered, and triggers at
     *        0). XInputSetState calls will be registered but not sent to the
     *        device. Sending any value other than FALSE will restore reading
     *        and writing functionality to normal.
     */
    void XInputEnable(boolean enable);

    /**
     * <p>
     * Retrieves a gamepad input event.
     * </p>
     *
     * <p>
     * <code>DWORD WINAPI XInputGetKeystroke(<br/>&nbsp;
     * DWORD dwUserIndex,<br/>&nbsp;
     * DWORD dwReserved,<br/>&nbsp;
     * PXINPUT_KEYSTROKE pKeystroke<br/>);</code>
     * </p>
     *
     * <p>
     * Windows 8 (XInput 1.4), DirectX SDK (XInput 1.3)
     * </p>
     *
     * @param dwUserIndex
     *        Index of the signed-in gamer associated with the device. Can be a
     *        value in the range 0-XUSER_MAX_COUNT-1, or XUSER_INDEX_ANY to
     *        fetch the next available input event from any user.
     * @param dwReserved
     *        Reserved
     * @param pKeystroke
     *        Pointer to an XINPUT_KEYSTROKE structure that receives an input
     *        event.
     * @return If the function succeeds, the return value is ERROR_SUCCESS. If
     *         no new keys have been pressed, the return value is ERROR_EMPTY.
     */
    int XInputGetKeystroke(int dwUserIndex, int dwReserved, XInputKeystroke pKeystroke);

}