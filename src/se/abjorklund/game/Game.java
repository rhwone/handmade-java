package se.abjorklund.game;

import se.abjorklund.game.controller.GameControllerInput;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class Game {

    static int blueOffset = 0;
    static int greenOffset = 0;

    public void test() {

    }

    public byte[] getSimpleAudioBuffer() {
        String currentDir = System.getProperty("user.dir");
        System.out.println("Current dir using System:" + currentDir);
        File audioFile = new File("src/se/abjorklund/win32/Ring09.wav");

        AudioInputStream audioInputStream = null;
        byte[] buffer = new byte[0];
        try {
            audioInputStream = AudioSystem
                    .getAudioInputStream(audioFile.getAbsoluteFile());
            int available = audioInputStream.available();
            buffer = new byte[available];

            audioInputStream.read(buffer);
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }


    public byte[] renderWeirdGradient(GameControllerInput controller) {
        int width = 1280;
        int height = 740;
        int bytesPerPixel = 4;
        int pitch = width * bytesPerPixel;

        byte[] buffer = new byte[(width * height) * bytesPerPixel];

        if (controller.isAnalog()) {
            // NOTE(casey): Use analog movement tuning
            blueOffset += (int) (4.0f * controller.getStickAverageX());
        } else {
            // NOTE(casey): Use digital movement tuning
            if (controller.getMoveLeft().endedDown()) {
                blueOffset -= 1;
            }

            if (controller.getMoveRight().endedDown()) {
                blueOffset += 1;
            }
        }

        int row = 0;
        for (int y = 0; y < height; ++y) {

            int pixelOnRow = 0;
            for (int x = 0; x < width; ++x) {
                int offsetInBytes = (row * pitch) + (pixelOnRow * bytesPerPixel);

                byte blue = (byte) (x + (blueOffset * 2));
                byte green = (byte) (y + (greenOffset * 2));

                buffer[offsetInBytes] = blue;
                buffer[offsetInBytes + 1] = green;
                buffer[offsetInBytes + 2] = (byte) 0x22;
                buffer[offsetInBytes + 3] = (byte) 0x00;

                ++pixelOnRow;
            }
            ++row;
        }
        return buffer;
    }
}
