package se.abjorklund.win32;

public class JNIPlatform {
    static {
        System.load("G:\\handmade-java\\src\\se\\abjorklund\\win32\\middleware.dll");
    }

    public native void start();
}
