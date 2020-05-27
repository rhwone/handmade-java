package se.abjorklund.win32.jna.dsound;

import com.sun.jna.platform.win32.WinDef;

public class DSoundJNI {
    static {
        System.load("G:\\handmade-java\\src\\se\\abjorklund\\win32\\jna\\dsound\\DSoundJNI.dll");
    }

    public native void initDSound(WinDef.HWND window, int samplesPerSecond, int bufferSize);

    public native void startPlayingBuffer();

    public native void playSquareWave();
}
