package se.abjorklund.win32;

import com.sun.jna.platform.win32.*;

import static com.sun.jna.Native.getLastError;
import static com.sun.jna.platform.win32.DBT.*;
import static com.sun.jna.platform.win32.WinDef.*;
import static com.sun.jna.platform.win32.WinUser.*;

public class Win32 implements WindowProc {

    public Win32() {
        // define new window class
        String windowClass = new String("MyWindowClass");
        HMODULE hInst = Kernel32.INSTANCE.GetModuleHandle("");

        WNDCLASSEX wClass = new WNDCLASSEX();
        wClass.hInstance = hInst;
        wClass.lpfnWndProc = Win32.this;
        wClass.lpszClassName = windowClass;

        // register window class
        User32.INSTANCE.RegisterClassEx(wClass);
        getLastError();

        // create new window
        HWND hWnd = User32.INSTANCE
                .CreateWindowEx(
                        User32.WS_EX_TOPMOST,
                        windowClass,
                        "My hidden helper window, used only to catch the windows events",
                        0, 0, 0, 0, 0,
                        null, // WM_DEVICECHANGE contradicts parent=WinUser.HWND_MESSAGE
                        null, hInst, null);

        getLastError();
        System.out.println("window sucessfully created! window hwnd: "
                + hWnd.getPointer().toString());

        Wtsapi32.INSTANCE.WTSRegisterSessionNotification(hWnd,
                Wtsapi32.NOTIFY_FOR_THIS_SESSION);

        /* this filters for all device classes */
        // DEV_BROADCAST_HDR notificationFilter = new DEV_BROADCAST_HDR();
        // notificationFilter.dbch_devicetype = DBT.DBT_DEVTYP_DEVICEINTERFACE;

        /* this filters for all usb device classes */
        DEV_BROADCAST_DEVICEINTERFACE notificationFilter = new DEV_BROADCAST_DEVICEINTERFACE();
        notificationFilter.dbcc_size = notificationFilter.size();
        notificationFilter.dbcc_devicetype = DBT.DBT_DEVTYP_DEVICEINTERFACE;
        notificationFilter.dbcc_classguid = DBT.GUID_DEVINTERFACE_USB_DEVICE;

        /*
         * use User32.DEVICE_NOTIFY_ALL_INTERFACE_CLASSES instead of
         * DEVICE_NOTIFY_WINDOW_HANDLE to ignore the dbcc_classguid value
         */
        HDEVNOTIFY hDevNotify = User32.INSTANCE.RegisterDeviceNotification(
                hWnd, notificationFilter, User32.DEVICE_NOTIFY_WINDOW_HANDLE);

        getLastError();
        if (hDevNotify != null)
            System.out.println("RegisterDeviceNotification was sucessfully!");

        
        User32.INSTANCE.ShowWindow(hWnd, SW_SHOW);

        MSG msg = new MSG();
        while (User32.INSTANCE.GetMessage(msg, hWnd, 0, 0) != 0) {
            User32.INSTANCE.TranslateMessage(msg);
            User32.INSTANCE.DispatchMessage(msg);
        }

        User32.INSTANCE.UnregisterDeviceNotification(hDevNotify);
        Wtsapi32.INSTANCE.WTSUnRegisterSessionNotification(hWnd);
        User32.INSTANCE.UnregisterClass(windowClass, hInst);
        User32.INSTANCE.DestroyWindow(hWnd);

        System.out.println("program exit!");
    }

    public void getEXEFileName(Win32State state) {
        /*
        // NOTE(casey): Never use MAX_PATH in code that is user-facing, because it
	    // can be dangerous and lead to bad results.
	    DWORD sizeOfFilename = GetModuleFileNameA(0, state->EXEFileName, sizeof(state->EXEFileName));
	    state->onePastLastEXEFileNameSlash = state->EXEFileName;
	    for (char *scan = state->EXEFileName;
             *scan;
            ++scan)
        {
            if (*scan == '\\')
            {
                state->onePastLastEXEFileNameSlash = scan + 1;
            }
	    }
        */
    }

    public void runGame() {
        new Win32();
    }

    @Override
    public LRESULT callback(HWND hwnd, int uMsg, WPARAM wParam, LPARAM lParam) {
        switch (uMsg) {
            case WM_CREATE: {
                onCreate(wParam, lParam);
                return new LRESULT(0);
            }
            case WM_DESTROY: {
                User32.INSTANCE.PostQuitMessage(0);
                return new LRESULT(0);
            }
            case WM_SESSION_CHANGE: {
                this.onSessionChange(wParam, lParam);
                return new LRESULT(0);
            }
            case WM_DEVICECHANGE: {
                LRESULT lResult = this.onDeviceChange(wParam, lParam);
                return lResult != null ? lResult :
                        User32.INSTANCE.DefWindowProc(hwnd, uMsg, wParam, lParam);
            }
            default:
                return User32.INSTANCE.DefWindowProc(hwnd, uMsg, wParam, lParam);
        }
    }

    protected void onCreate(WPARAM wParam, LPARAM lParam) {
        System.out.println("onCreate: WM_CREATE");
    }

    /**
     * On session change.
     *
     * @param wParam
     *            the w param
     * @param lParam
     *            the l param
     */
    protected void onSessionChange(WPARAM wParam, LPARAM lParam) {
        switch (wParam.intValue()) {
            case Wtsapi32.WTS_CONSOLE_CONNECT: {
                this.onConsoleConnect(lParam.intValue());
                break;
            }
            case Wtsapi32.WTS_CONSOLE_DISCONNECT: {
                this.onConsoleDisconnect(lParam.intValue());
                break;
            }
            case Wtsapi32.WTS_SESSION_LOGON: {
                this.onMachineLogon(lParam.intValue());
                break;
            }
            case Wtsapi32.WTS_SESSION_LOGOFF: {
                this.onMachineLogoff(lParam.intValue());
                break;
            }
            case Wtsapi32.WTS_SESSION_LOCK: {
                this.onMachineLocked(lParam.intValue());
                break;
            }
            case Wtsapi32.WTS_SESSION_UNLOCK: {
                this.onMachineUnlocked(lParam.intValue());
                break;
            }
        }
    }

    /**
     * On console connect.
     *
     * @param sessionId
     *            the session id
     */
    protected void onConsoleConnect(int sessionId) {
        System.out.println("onConsoleConnect: " + sessionId);
    }

    /**
     * On console disconnect.
     *
     * @param sessionId
     *            the session id
     */
    protected void onConsoleDisconnect(int sessionId) {
        System.out.println("onConsoleDisconnect: " + sessionId);
    }

    /**
     * On machine locked.
     *
     * @param sessionId
     *            the session id
     */
    protected void onMachineLocked(int sessionId) {
        System.out.println("onMachineLocked: " + sessionId);
    }

    /**
     * On machine unlocked.
     *
     * @param sessionId
     *            the session id
     */
    protected void onMachineUnlocked(int sessionId) {
        System.out.println("onMachineUnlocked: " + sessionId);
    }

    /**
     * On machine logon.
     *
     * @param sessionId
     *            the session id
     */
    protected void onMachineLogon(int sessionId) {
        System.out.println("onMachineLogon: " + sessionId);
    }

    /**
     * On machine logoff.
     *
     * @param sessionId
     *            the session id
     */
    protected void onMachineLogoff(int sessionId) {
        System.out.println("onMachineLogoff: " + sessionId);
    }

    /**
     * On device change.
     *
     * @param wParam
     *            the w param
     * @param lParam
     *            the l param
     * @return the result. Null if the message is not processed.
     */

    protected LRESULT onDeviceChange(WPARAM wParam, LPARAM lParam) {
        switch (wParam.intValue()) {
            case DBT_DEVICEARRIVAL: {
                return onDeviceChangeArrival(lParam);
            }
            case DBT_DEVICEREMOVECOMPLETE: {
                return onDeviceChangeRemoveComplete(lParam);
            }
            case DBT_DEVNODES_CHANGED: {
                //lParam is 0 for this wParam
                return onDeviceChangeNodesChanged();
            }
            default:
                System.out.println(
                        "Message WM_DEVICECHANGE message received, value unhandled.");
        }
        return null;
    }

    protected LRESULT onDeviceChangeArrivalOrRemoveComplete(LPARAM lParam, String action) {
        DEV_BROADCAST_HDR bhdr = new DEV_BROADCAST_HDR(lParam.longValue());
        switch (bhdr.dbch_devicetype) {
            case DBT_DEVTYP_DEVICEINTERFACE: {
                // see http://msdn.microsoft.com/en-us/library/windows/desktop/aa363244.aspx
                DEV_BROADCAST_DEVICEINTERFACE bdif = new DEV_BROADCAST_DEVICEINTERFACE(bhdr.getPointer());
                System.out.println("BROADCAST_DEVICEINTERFACE: " + action);
                System.out.println("dbcc_devicetype: " + bdif.dbcc_devicetype);
                System.out.println("dbcc_name: " + bdif.getDbcc_name());
                System.out.println("dbcc_classguid: "
                        + bdif.dbcc_classguid.toGuidString());
                break;
            }
            case DBT_DEVTYP_HANDLE: {
                // see http://msdn.microsoft.com/en-us/library/windows/desktop/aa363245.aspx
                DEV_BROADCAST_HANDLE bhd = new DEV_BROADCAST_HANDLE(bhdr.getPointer());
                System.out.println("BROADCAST_HANDLE: " + action);
                break;
            }
            case DBT_DEVTYP_OEM: {
                // see http://msdn.microsoft.com/en-us/library/windows/desktop/aa363247.aspx
                DEV_BROADCAST_OEM boem = new DEV_BROADCAST_OEM(bhdr.getPointer());
                System.out.println("BROADCAST_OEM: " + action);
                break;
            }
            case DBT_DEVTYP_PORT: {
                // see http://msdn.microsoft.com/en-us/library/windows/desktop/aa363248.aspx
                DEV_BROADCAST_PORT bpt = new DEV_BROADCAST_PORT(bhdr.getPointer());
                System.out.println("BROADCAST_PORT: " + action);
                break;
            }
            case DBT_DEVTYP_VOLUME: {
                // see http://msdn.microsoft.com/en-us/library/windows/desktop/aa363249.aspx
                DEV_BROADCAST_VOLUME bvl = new DEV_BROADCAST_VOLUME(bhdr.getPointer());
                int logicalDriveAffected = bvl.dbcv_unitmask;
                short flag = bvl.dbcv_flags;
                boolean isMediaNotPhysical = 0 != (flag & DBTF_MEDIA/*value is 1*/);
                boolean isNet = 0 != (flag & DBTF_NET/*value is 2*/);
                System.out.println(action);
                int driveLetterIndex = 0;
                while (logicalDriveAffected != 0) {
                    if (0 != (logicalDriveAffected & 1)) {
                        System.out.println("Logical Drive Letter: " +
                                ((char) ('A' + driveLetterIndex)));
                    }
                    logicalDriveAffected >>>= 1;
                    driveLetterIndex++;
                }
                System.out.println("isMediaNotPhysical:"+isMediaNotPhysical);
                System.out.println("isNet:"+isNet);
                break;
            }
            default:
                return null;
        }
        // return TRUE means processed message for this wParam.
        // see http://msdn.microsoft.com/en-us/library/windows/desktop/aa363205.aspx
        // see http://msdn.microsoft.com/en-us/library/windows/desktop/aa363208.aspx
        return new LRESULT(1);
    }

    protected LRESULT onDeviceChangeArrival(LPARAM lParam) {
        return onDeviceChangeArrivalOrRemoveComplete(lParam, "Arrival");
    }

    protected LRESULT onDeviceChangeRemoveComplete(LPARAM lParam) {
        return onDeviceChangeArrivalOrRemoveComplete(lParam, "Remove Complete");
    }

    protected LRESULT onDeviceChangeNodesChanged() {
        System.out.println("Message DBT_DEVNODES_CHANGED");
        // return TRUE means processed message for this wParam.
        // see http://msdn.microsoft.com/en-us/library/windows/desktop/aa363211.aspx
        return new LRESULT(1);
    }
}
