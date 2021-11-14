package se.abjorklund.win32;

import java.nio.ByteBuffer;

public class JNIPlatform {
    static {
        System.load("G:\\handmade-java\\src\\se\\abjorklund\\win32\\middleware.dll");
    }

    public native void start(ByteBuffer byteBuffer, int screenWidth, int screenHeight);
}
