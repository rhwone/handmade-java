package se.abjorklund;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.DBT.*;
import com.sun.jna.platform.win32.WinDef.*;
import com.sun.jna.platform.win32.WinUser.MSG;
import com.sun.jna.platform.win32.WinUser.WNDCLASSEX;
import com.sun.jna.platform.win32.WinUser.WindowProc;
import com.sun.jna.ptr.PointerByReference;
import se.abjorklund.jna.IGDI32;
import se.abjorklund.jna.IKernel32;
import se.abjorklund.jna.IUser32;

import static com.sun.jna.platform.win32.WinGDI.BI_RGB;
import static se.abjorklund.jna.IUser32.*;


public class Win32Platform implements WindowProc {

    public static final DWORD SRCCOPY = new DWORD(13369376);
    public static boolean running;

    public static WinGDI.BITMAPINFO BITMAP_INFO = new WinGDI.BITMAPINFO();
    static private Pointer BITMAP_MEMORY;
    private int BITMAP_WIDTH;
    private int BITMAP_HEIGHT;
    public static final int BYTES_PER_PIXEL = 4;

    /**
     * Instantiates a new win32 window test.
     */
    public Win32Platform() {

        // define new window class
        String windowClassName = "MyWindowClass";
        HMODULE instance = Kernel32.INSTANCE.GetModuleHandle("");

        WNDCLASSEX windowClass = new WNDCLASSEX();
        windowClass.style = CS_OWNDC | CS_HREDRAW | CS_VREDRAW;
        windowClass.hInstance = instance;
        windowClass.lpfnWndProc = Win32Platform.this;
        windowClass.lpszClassName = windowClassName;

        // register window class
        ATOM registerClassResult = IUser32.INSTANCE.RegisterClassEx(windowClass);
        if (registerClassResult != null) {
            if (registerClassResult.intValue() != 0) {
                HWND window = IUser32.INSTANCE.CreateWindowEx(0, windowClass.lpszClassName, "Java JNA Graphics Experiment",
                        WS_OVERLAPPEDWINDOW | WS_VISIBLE,
                        CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT,
                        null, null, instance, null);
                if (window != null) {
                    running = true;
                    int xOffset = 0;
                    int yOffset = 0;
                    while (running) {

                        MSG message = new MSG();
                        while (IUser32.INSTANCE.PeekMessage(message, null, 0, 0, PM_REMOVE)) {

                            if (message.message == WM_QUIT) {
                                running = false;
                            }

                            IUser32.INSTANCE.TranslateMessage(message);
                            IUser32.INSTANCE.DispatchMessage(message);
                        }
                        if (true) {
                            double sinValueOffset = (Math.sin(xOffset))*4;
                            System.out.println(sinValueOffset);
                            renderWeirdGradient((int) sinValueOffset, (int)sinValueOffset);
                        } else {
                            renderWeirdGradient(xOffset, yOffset);
                        }

                        HDC deviceContext = INSTANCE.GetDC(window);
                        RECT clientRect = new RECT();
                        IUser32.INSTANCE.GetClientRect(window, clientRect);
                        int windowWidth = clientRect.right - clientRect.left;
                        int windowHeight = clientRect.bottom - clientRect.top;
                        win32UpdateWindow(deviceContext, clientRect, 0, 0, windowWidth, windowHeight);
                        INSTANCE.ReleaseDC(window, deviceContext);


                        xOffset++;
                    }
                }
            } else {
                //TODO(anders): logging
            }
        } else {
            //TODO(anders): logging
        }
    }

    private void win32resizeDIBSection(int width, int height) {

        if (BITMAP_MEMORY != null) {
            IKernel32.INSTANCE.VirtualFree(BITMAP_MEMORY, null, MEM_RELEASE);
        }

        BITMAP_WIDTH = width;
        BITMAP_HEIGHT = height;

        WinGDI.BITMAPINFOHEADER infoHeader = new WinGDI.BITMAPINFOHEADER();
        infoHeader.biSize = 40;
        infoHeader.biWidth = BITMAP_WIDTH;
        infoHeader.biHeight = -BITMAP_HEIGHT; //Negative as specified by the API in order to get top-down DIB. Origin is upper left corner.
        infoHeader.biPlanes = 1;
        infoHeader.biBitCount = 32;
        infoHeader.biCompression = BI_RGB;

        BITMAP_INFO.bmiHeader = infoHeader;

        int size = (BITMAP_WIDTH * BITMAP_HEIGHT) * BYTES_PER_PIXEL;
        SIZE_T bitmapMemorySize = new SIZE_T(size);

        BITMAP_MEMORY = IKernel32.INSTANCE.VirtualAlloc(Pointer.NULL, bitmapMemorySize, MEM_COMMIT, PAGE_READWRITE);
    }

    private void renderWeirdGradient(int xOffset, int yOffset) {
        long pitchInBytes = BITMAP_WIDTH * BYTES_PER_PIXEL;
        long row = 0;

        for (int y = 0; y < BITMAP_HEIGHT; ++y) {

            long pixelOnRow = 0;
            for (int x = 0; x < BITMAP_WIDTH; ++x) {
                long offsetInBytes = (row * pitchInBytes) + (pixelOnRow * BYTES_PER_PIXEL);

                BITMAP_MEMORY.setByte(offsetInBytes + 0, (byte) (x + xOffset));
                BITMAP_MEMORY.setByte(offsetInBytes + 1, (byte) (y + yOffset));
                BITMAP_MEMORY.setByte(offsetInBytes + 2, (byte) 0x00);
                BITMAP_MEMORY.setByte(offsetInBytes + 3, (byte) 0x00);

                ++pixelOnRow;
            }
            ++row;
        }
    }

    private void win32UpdateWindow(HDC deviceContext, RECT windowRect, int x, int y, int width, int height) {

        int windowWidth = windowRect.right - windowRect.left;
        int windowHeight = windowRect.bottom - windowRect.top;

        IGDI32.INSTANCE.StretchDIBits(deviceContext,
                /*x, y, width, height,
                x, y, width, height,*/
                0, 0, BITMAP_WIDTH, BITMAP_HEIGHT,
                0, 0, windowWidth, windowHeight,
                BITMAP_MEMORY, BITMAP_INFO, new UINT(WinGDI.DIB_RGB_COLORS), SRCCOPY);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.sun.jna.platform.win32.User32.WindowProc#callback(com.sun.jna.platform
     * .win32.WinDef.HWND, int, com.sun.jna.platform.win32.WinDef.WPARAM,
     * com.sun.jna.platform.win32.WinDef.LPARAM)
     */
    public LRESULT callback(HWND window, int message, WPARAM wParam, LPARAM lParam) {
        LRESULT result = new LRESULT(0);
        switch (message) {
            case WM_SIZE: {
                RECT clientRect = new RECT();
                IUser32.INSTANCE.GetClientRect(window, clientRect);
                int width = clientRect.right - clientRect.left;
                int height = clientRect.bottom - clientRect.top;
                win32resizeDIBSection(width, height);
                System.out.println("WM_SIZE");
            }
            break;
            case WM_DESTROY: {
                running = false;
                System.out.println("WM_DESTROY");
            }
            break;
            case WM_CLOSE: {
                running = false;
                System.out.println("WM_CLOSE");
            }
            break;
            case WM_ACTIVATE: {
                System.out.println("WM_ACTIVATE");
            }
            break;
            case WM_PAINT: {
                PAINTSTRUCT paintstruct = new PAINTSTRUCT();
                HDC deviceContext = IUser32.INSTANCE.BeginPaint(window, paintstruct);
                int x = paintstruct.rcPaint.left;
                int y = paintstruct.rcPaint.top;
                int width = paintstruct.rcPaint.left - paintstruct.rcPaint.right;
                int height = paintstruct.rcPaint.bottom - paintstruct.rcPaint.top;

                RECT clientRect = new RECT();
                IUser32.INSTANCE.GetClientRect(window, clientRect);

                win32UpdateWindow(deviceContext, clientRect, x, y, width, height);

                IUser32.INSTANCE.EndPaint(window, paintstruct);
            }
            break;
            default: {
                //System.out.println("DEFAULT");
                result = User32.INSTANCE.DefWindowProc(window, message, wParam, lParam);
            }
            break;
        }
        return result;
    }

    /**
     * Gets the last error.
     *
     * @return the last error
     */
    public int getLastError() {
        int rc = Kernel32.INSTANCE.GetLastError();

        if (rc != 0)
            System.out.println("error: " + rc);

        return rc;
    }

    /**
     * On session change.
     *
     * @param wParam the w param
     * @param lParam the l param
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
     * @param sessionId the session id
     */
    protected void onConsoleConnect(int sessionId) {
        System.out.println("onConsoleConnect: " + sessionId);
    }

    /**
     * On console disconnect.
     *
     * @param sessionId the session id
     */
    protected void onConsoleDisconnect(int sessionId) {
        System.out.println("onConsoleDisconnect: " + sessionId);
    }

    /**
     * On machine locked.
     *
     * @param sessionId the session id
     */
    protected void onMachineLocked(int sessionId) {
        System.out.println("onMachineLocked: " + sessionId);
    }

    /**
     * On machine unlocked.
     *
     * @param sessionId the session id
     */
    protected void onMachineUnlocked(int sessionId) {
        System.out.println("onMachineUnlocked: " + sessionId);
    }

    /**
     * On machine logon.
     *
     * @param sessionId the session id
     */
    protected void onMachineLogon(int sessionId) {
        System.out.println("onMachineLogon: " + sessionId);
    }

    /**
     * On machine logoff.
     *
     * @param sessionId the session id
     */
    protected void onMachineLogoff(int sessionId) {
        System.out.println("onMachineLogoff: " + sessionId);
    }

    /**
     * On device change.
     *
     * @param wParam the w param
     * @param lParam the l param
     * @return the result. Null if the message is not processed.
     */
    protected LRESULT onDeviceChange(WPARAM wParam, LPARAM lParam) {
        switch (wParam.intValue()) {
            case DBT.DBT_DEVICEARRIVAL: {
                return onDeviceChangeArrival(lParam);
            }
            case DBT.DBT_DEVICEREMOVECOMPLETE: {
                return onDeviceChangeRemoveComplete(lParam);
            }
            case DBT.DBT_DEVNODES_CHANGED: {
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
            case DBT.DBT_DEVTYP_DEVICEINTERFACE: {
                // see http://msdn.microsoft.com/en-us/library/windows/desktop/aa363244.aspx
                DEV_BROADCAST_DEVICEINTERFACE bdif = new DEV_BROADCAST_DEVICEINTERFACE(bhdr.getPointer());
                System.out.println("BROADCAST_DEVICEINTERFACE: " + action);
                System.out.println("dbcc_devicetype: " + bdif.dbcc_devicetype);
                System.out.println("dbcc_name: " + bdif.getDbcc_name());
                System.out.println("dbcc_classguid: "
                        + bdif.dbcc_classguid.toGuidString());
                break;
            }
            case DBT.DBT_DEVTYP_HANDLE: {
                // see http://msdn.microsoft.com/en-us/library/windows/desktop/aa363245.aspx
                DEV_BROADCAST_HANDLE bhd = new DEV_BROADCAST_HANDLE(bhdr.getPointer());
                System.out.println("BROADCAST_HANDLE: " + action);
                break;
            }
            case DBT.DBT_DEVTYP_OEM: {
                // see http://msdn.microsoft.com/en-us/library/windows/desktop/aa363247.aspx
                DEV_BROADCAST_OEM boem = new DEV_BROADCAST_OEM(bhdr.getPointer());
                System.out.println("BROADCAST_OEM: " + action);
                break;
            }
            case DBT.DBT_DEVTYP_PORT: {
                // see http://msdn.microsoft.com/en-us/library/windows/desktop/aa363248.aspx
                DEV_BROADCAST_PORT bpt = new DEV_BROADCAST_PORT(bhdr.getPointer());
                System.out.println("BROADCAST_PORT: " + action);
                break;
            }
            case DBT.DBT_DEVTYP_VOLUME: {
                // see http://msdn.microsoft.com/en-us/library/windows/desktop/aa363249.aspx
                DEV_BROADCAST_VOLUME bvl = new DEV_BROADCAST_VOLUME(bhdr.getPointer());
                int logicalDriveAffected = bvl.dbcv_unitmask;
                short flag = bvl.dbcv_flags;
                boolean isMediaNotPhysical = 0 != (flag & DBT.DBTF_MEDIA/*value is 1*/);
                boolean isNet = 0 != (flag & DBT.DBTF_NET/*value is 2*/);
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
                System.out.println("isMediaNotPhysical:" + isMediaNotPhysical);
                System.out.println("isNet:" + isNet);
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

    /**
     * On create.
     *
     * @param wParam the w param
     * @param lParam the l param
     */
    protected void onCreate(WPARAM wParam, LPARAM lParam) {
        System.out.println("onCreate: WM_CREATE");
    }

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        new Win32Platform();
    }

    enum Win32RenderingType {
        Win32RenderType_RenderOpenGL_DisplayOpenGL,
        Win32RenderType_RenderSoftware_DisplayOpenGL,
        Win32RenderType_RenderSoftware_DisplayGDI,
    }

}