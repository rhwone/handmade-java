package se.abjorklund.deprecated.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.win32.W32APIOptions;

public interface IGdi32 extends com.sun.jna.platform.win32.GDI32 {
    IGdi32 INSTANCE = Native.load("gdi32", IGdi32.class, W32APIOptions.DEFAULT_OPTIONS);
    // "gdi32full" ?

    WinDef.BOOL PatBlt(WinDef.HDC hdc, int x, int y, int w, int h, WinDef.DWORD rop);

    int StretchDIBits(WinDef.HDC hdc,
                      int xDest,
                      int yDest,
                      int DestWidth,
                      int DestHeight,
                      int xSrc,
                      int ySrc,
                      int SrcWidth,
                      int SrcHeight,
                      Pointer lpValue,
                      final WinGDI.BITMAPINFO lpBmi,
                      WinDef.UINT iUsage,
                      WinDef.DWORD rop
    );
}
