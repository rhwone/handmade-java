package se.abjorklund.game;

import se.abjorklund.win32.JNIPlatform;

public class Game {
    private static final JNIPlatform JNI_PLATFORM = new JNIPlatform();


    public static void main(String[] args) {
        JNI_PLATFORM.start();
    }

    public static byte[] getWeirdGradient() {
        return renderWeirdGradient();
    }

    public void gameUpdateAndRender(GameOffscreenBuffer buffer, int xOffset, int yOffset) {

        //TODO(anders): Allow sample offsets here for more robust platform options
        //gameOutputSound(soundBuffer);
        byte[] gradientBuffer = renderWeirdGradient();
    }

    private void gameOutputSound(GameSoundOutputBuffer soundBuffer) {

    }

    public static byte[] renderWeirdGradient() {
        int width = 1280;
        int height = 740;
        int bytesPerPixel = 4;
        int pitch = width * bytesPerPixel;

        byte[] buffer = new byte[(width * height) * bytesPerPixel];

        int row = 0;
        for (int y = 0; y < height; ++y) {

            int pixelOnRow = 0;
            for (int x = 0; x < width; ++x) {
                int offsetInBytes = (row * pitch) + (pixelOnRow * bytesPerPixel);

                buffer[offsetInBytes] = (byte) x;
                buffer[offsetInBytes + 1] = (byte) y;
                buffer[offsetInBytes + 2] = (byte) 0x00;
                buffer[offsetInBytes + 3] = (byte) 0x00;

                ++pixelOnRow;
            }
            ++row;
        }
        return buffer;
    }
}
