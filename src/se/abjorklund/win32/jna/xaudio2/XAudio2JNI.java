package se.abjorklund.win32.jna.xaudio2;

public class XAudio2JNI {
    static {
        System.load("G:\\handmade-java\\src\\se\\abjorklund\\win32\\jna\\xaudio2\\XAudio2JNI.dll");
    }

    public native void initXAudio2();

}
