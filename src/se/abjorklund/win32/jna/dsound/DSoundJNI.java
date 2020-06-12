package se.abjorklund.win32.jna.dsound;

public class DSoundJNI {
    static {
        System.load("G:\\handmade-java\\src\\se\\abjorklund\\win32\\jna\\dsound\\DSoundJNI.dll");
    }

    public native void initDSound();

    public native void startPlayingBuffer();

    public native void playSound(short[] buffer);
}
