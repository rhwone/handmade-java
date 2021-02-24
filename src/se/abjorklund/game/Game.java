package se.abjorklund.game;

import se.abjorklund.win32.JNIPlatform;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class Game {
    private static final JNIPlatform JNI_PLATFORM = new JNIPlatform();


    public static void main(String[] args) {
        JNI_PLATFORM.start();
    }

    public static byte[] win32platform_getVideoBuffer() {
        return renderWeirdGradient();
    }

    public static byte[] win32platform_getSoundBuffer() {
        return getSimpleAudioBuffer();
    }

    private static byte[] getSimpleAudioBuffer() {
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
