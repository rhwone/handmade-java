package se.abjorklund.game;

import com.sun.jna.Pointer;

public class Game {

    public void gameUpdateAndRender(GameOffscreenBuffer buffer, int xOffset, int yOffset) {

        //TODO(anders): Allow sample offsets here for more robust platform options
        //gameOutputSound(soundBuffer);
        renderWeirdGradient(buffer, xOffset, yOffset);
    }

    private void gameOutputSound(GameSoundOutputBuffer soundBuffer) {

    }

    private void renderWeirdGradient(GameOffscreenBuffer buffer, int xOffset, int yOffset) {
        int width = buffer.getWidth();
        int height = buffer.getHeight();
        int bytesPerPixel = buffer.getBytesPerPixel();
        int pitch = buffer.getPitch();
        Pointer memory = buffer.getMemory();

        int row = 0;
        for (int y = 0; y < height; ++y) {

            int pixelOnRow = 0;
            for (int x = 0; x < width; ++x) {
                long offsetInBytes = (row * pitch) + (pixelOnRow * bytesPerPixel);

                memory.setByte(offsetInBytes + 0, (byte) (x + xOffset));
                memory.setByte(offsetInBytes + 1, (byte) (y + yOffset));
                memory.setByte(offsetInBytes + 2, (byte) 0x00);
                memory.setByte(offsetInBytes + 3, (byte) 0x00);

                ++pixelOnRow;
            }
            ++row;
        }
    }

}
