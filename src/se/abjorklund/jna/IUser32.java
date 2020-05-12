package se.abjorklund.jna;

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.win32.W32APIOptions;

import java.util.Arrays;
import java.util.List;

public interface IUser32 extends com.sun.jna.platform.win32.User32 {

    IUser32 INSTANCE = Native.load("user32", IUser32.class, W32APIOptions.DEFAULT_OPTIONS);

    class PAINTSTRUCT extends Structure {
        public HDC hdc;
        public boolean fErase;
        public RECT rcPaint;
        public boolean fRestore;
        public boolean fIncUpdate;
        public byte rgbReserved[] = new byte[32];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("hdc", "fErase", "rcPaint", "fRestore", "fIncUpdate", "rgbReserved");
        }
    }

    HDC BeginPaint(HWND hWnd, PAINTSTRUCT lpPaint);

    boolean EndPaint(HWND hWnd, PAINTSTRUCT lpPaint);

    int WM_LBUTTONDOWN = 0x0201;
    int WS_EX_LEFT = 0x00000000;
    int WS_EX_TOPMOST = 8;
    int WS_SYSMENU = 0x00080000;
    int CW_USEDEFAULT = 0x80000000;
    int WM_ACTIVATE = 0x0006;
    int WM_MOVE = 0x0003;
    int CS_VREDRAW = 0x0001;
    int CS_HREDRAW = 0x0002;
    int CS_OWNDC = 0x0020;
    int COLOR_WINDOW = 5;

    int PM_REMOVE = 0x0001;

}
