package se.abjorklund.deprecated.jna.dsound;

import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinNT.HRESULT;

public class DSoundJNI {
    static {
        System.load("G:\\handmade-java\\src\\se\\abjorklund\\win32\\jna\\dsound\\DSoundJNI.dll");
    }

    public native void initDSound();

    public native void startPlayingBuffer();

    public native void playSound(short[] buffer);

    public native DSoundCursorInfo getCurrentPosition();

    public native DSoundGlobalSoundOutput getGlobalSoundOutput();

    public class DSoundCursorInfo {

        private HRESULT hResult;
        private DWORD playCursor;
        private DWORD writeCursor;

        public DSoundCursorInfo(int hResult, int playCursor, int writeCursor) {
            this.hResult = new HRESULT(hResult);
            this.playCursor = new DWORD(playCursor);
            this.writeCursor = new DWORD(writeCursor);
        }

        public HRESULT getHResult() {
            return hResult;
        }

        public DWORD getPlayCursor() {
            return playCursor;
        }

        public DWORD getWriteCursor() {
            return writeCursor;
        }
    }

    public class DSoundGlobalSoundOutput {
        private int toneHz;
        private int samplesPerSecond;
        private int toneVolume;
        private int runningSampleIndex;
        private int wavePeriod;
        private int bytesPerSample;
        private int secondaryBufferSize;

        public DSoundGlobalSoundOutput(int toneHz, int samplesPerSecond, int toneVolume, int runningSampleIndex, int wavePeriod, int bytesPerSample, int secondaryBufferSize) {
            this.toneHz = toneHz;
            this.samplesPerSecond = samplesPerSecond;
            this.toneVolume = toneVolume;
            this.runningSampleIndex = runningSampleIndex;
            this.wavePeriod = wavePeriod;
            this.bytesPerSample = bytesPerSample;
            this.secondaryBufferSize = secondaryBufferSize;
        }

        public int getToneHz() {
            return toneHz;
        }

        public int getSamplesPerSecond() {
            return samplesPerSecond;
        }

        public int getToneVolume() {
            return toneVolume;
        }

        public int getRunningSampleIndex() {
            return runningSampleIndex;
        }

        public int getWavePeriod() {
            return wavePeriod;
        }

        public int getBytesPerSample() {
            return bytesPerSample;
        }

        public int getSecondaryBufferSize() {
            return secondaryBufferSize;
        }
    }
}
