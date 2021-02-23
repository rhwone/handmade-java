package se.abjorklund.deprecated.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.win32.W32APIOptions;

public interface IKernel32 extends Kernel32 {

    IKernel32 INSTANCE = Native.load("kernel32", IKernel32.class, W32APIOptions.DEFAULT_OPTIONS);


    Pointer VirtualAlloc(Pointer lpAddress, SIZE_T dwSize, int flAllocationType, int flProtect);

    boolean VirtualFree(Pointer lpAddress, SIZE_T dwSize, int dwFreeType);

    boolean QueryPerformanceCounter(LARGE_INTEGER lpPerformanceCount);

    boolean QueryPerformanceFrequency(LARGE_INTEGER lpFrequency);
}
