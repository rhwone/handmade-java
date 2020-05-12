package se.abjorklund.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.win32.W32APIOptions;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public interface IGDI32 extends com.sun.jna.platform.win32.GDI32 {
    IGDI32 INSTANCE = Native.load("gdi32", IGDI32.class, W32APIOptions.DEFAULT_OPTIONS);
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
