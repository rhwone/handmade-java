package se.abjorklund.win32.platform;

public class JNIPlatform {
    static {
        System.load("G:\\handmade-java\\src\\se\\abjorklund\\win32\\platform\\middleware.dll");
    }

    public native void start();
}
