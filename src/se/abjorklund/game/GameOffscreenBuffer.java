package se.abjorklund.game;

import com.sun.jna.Pointer;

public class GameOffscreenBuffer {
    private Pointer memory;
    private int width;
    private int height;
    private int pitch;
    private int bytesPerPixel;

    public Pointer getMemory() {
        return memory;
    }

    public void setMemory(Pointer memory) {
        this.memory = memory;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getPitch() {
        return pitch;
    }

    public void setPitch(int pitch) {
        this.pitch = pitch;
    }

    public int getBytesPerPixel() {
        return bytesPerPixel;
    }

    public void setBytesPerPixel(int bytesPerPixel) {
        this.bytesPerPixel = bytesPerPixel;
    }
}
