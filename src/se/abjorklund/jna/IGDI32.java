package se.abjorklund.jna;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.W32APIOptions;

public interface IGDI32 extends com.sun.jna.platform.win32.GDI32 {
    IGDI32 INSTANCE = Native.load("gdi32full", IGDI32.class, W32APIOptions.DEFAULT_OPTIONS);
    // "gdi32full" ?

    WinDef.BOOL PatBlt(WinDef.HDC hdc, int x, int y, int w, int h, WinDef.DWORD rop);
}
