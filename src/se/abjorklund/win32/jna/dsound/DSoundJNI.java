package se.abjorklund.win32.jna.dsound;

import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;

public class DSoundJNI {
    static {
        System.load("G:\\handmade-java\\src\\se\\abjorklund\\win32\\jna\\dsound\\DSoundJNI.dll");
    }

    public native void initDSound();

    public native void startPlayingBuffer();

    public native void playSound(short[] buffer);

    public native DSoundByteInfo getSoundBufferByteInfo();

    public native DSoundCursorInfo getCurrentPosition();

    public class DSoundByteInfo {
        public WinDef.DWORD byteToLock;
        public WinDef.DWORD bytesToWrite;
    }

    public class DSoundCursorInfo {

        private int hResult;
        private int playCursor;
        private int writeCursor;

        public DSoundCursorInfo(int hResult, int playCursor, int writeCursor) {
            this.hResult = hResult;
            this.playCursor = playCursor;
            this.writeCursor = writeCursor;
        }

        public int gethResult() {
            return hResult;
        }

        public int getPlayCursor() {
            return playCursor;
        }

        public int getWriteCursor() {
            return writeCursor;
        }
    }
}
