package se.abjorklund.game;

import se.abjorklund.game.controller.GameControllerInput;
import se.abjorklund.math.Vector2;
import se.abjorklund.renderer.Rectangle;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Game {

    public static final GameState GAMESTATE = new GameState();

    static int blueOffset = 0;
    static int greenOffset = 0;
    static float tSine = 0;
    private static final double PI = 3.14159265359;

    public short[] outputSound(int sampleCount, int samplesPerSecond, int toneHz) {
        short toneVolume = 3000;
        int wavePeriod = samplesPerSecond / toneHz;
        int sampleOutIndex = 0;
        short[] sampleOut = new short[samplesPerSecond * 4];

        //fillSoundBuffer(sampleCount, toneVolume, wavePeriod, sampleOutIndex, sampleOut);
        return sampleOut;
    }

    private void fillSoundBuffer(int sampleCount, short toneVolume, int wavePeriod, int sampleOutIndex, short[] sampleOut) {
        for (int sampleIndex = 0;
             sampleIndex < sampleCount;
             ++sampleIndex) {
            // TODO(casey): Draw this out for people
            double sineValue = Math.sin(tSine);
            short sampleValue = (short) (sineValue * toneVolume);
            sampleOut[sampleOutIndex++] = sampleValue;
            sampleOut[sampleOutIndex++] = sampleValue;
            tSine += 2.0f * PI * 1.0f / wavePeriod;
        }
    }


    public byte[] renderWeirdGradient(GameControllerInput controller) {
        int width = 1280;
        int height = 740;
        int bytesPerPixel = 4;
        int pitch = width * bytesPerPixel;

        /*
         * maybe?
         *
         * ByteBuffer:
         * A direct byte buffer may also be created by mapping a region of a file directly into memory.
         * An implementation of the Java platform may optionally support the creation of direct byte buffers from native code via JNI.
         * If an instance of one of these kinds of buffers refers to an inaccessible region of memory then an attempt
         * to access that region will not change the buffer's content and will cause an unspecified exception
         * to be thrown either at the time of the access or at some later time.
         * */

        byte[] buffer = new byte[(width * height) * bytesPerPixel];

        if (controller.isAnalog()) {
            // NOTE(casey): Use analog movement tuning
            blueOffset += (int) (4.0f * controller.getStickAverageX());
        } else {
            int currentY = GAMESTATE.getPlayer().position().getY();
            int currentX = GAMESTATE.getPlayer().position().getX();
            // NOTE(casey): Use digital movement tuning
            int VELOCITY = 5;
            if (controller.getMoveLeft().endedDown()) {
                GAMESTATE.getPlayer().position().setX(currentX - VELOCITY);
            }

            if (controller.getMoveRight().endedDown()) {
                GAMESTATE.getPlayer().position().setX(currentX + VELOCITY);
            }

            if (controller.getMoveUp().endedDown()) {
                GAMESTATE.getPlayer().position().setY(currentY - VELOCITY);
            }

            if (controller.getMoveDown().endedDown()) {
                GAMESTATE.getPlayer().position().setY(currentY + VELOCITY);
            }
        }

        List<Rectangle> rectangles = DEBUG_CreateRectangles();

        rectangles.add(DEBUG_DrawPlayer());

        /*Rectangle rect1 = new Rectangle(new Vector2(10, 10), new Vector2(20, 20), 0x66AB00FF);
        Rectangle rect2 = new Rectangle(new Vector2(30, 10), new Vector2(40, 20), 0x66AB00FF);
        Rectangle rect3 = new Rectangle(new Vector2(50, 10), new Vector2(60, 20), 0x66AB00FF);
        Rectangle rect4 = new Rectangle(new Vector2(70, 10), new Vector2(80, 20), 0x66AB00FF);*/

        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        for (Rectangle rectangle : rectangles) {
            byte[] color = byteBuffer.putInt(rectangle.getColor()).array();

            int row = 0;
            for (int y = 0; y < height; ++y) {

                int pixelOnRow = 0;
                for (int x = 0; x < width; ++x) {

                    if (Rectangle.isInside(rectangle, x, y)) {
                        int offsetInBytes = (row * pitch) + (pixelOnRow * bytesPerPixel);
                        buffer[offsetInBytes] = color[0];
                        buffer[offsetInBytes + 1] = color[1];
                        buffer[offsetInBytes + 2] = color[2];
                        buffer[offsetInBytes + 3] = color[3];
                    }

                    ++pixelOnRow;
                }
                ++row;
            }
            byteBuffer.clear();
        }

        //int row = 0;
  /*      for (int y = 0; y < height; ++y) {

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
        }*/
        return buffer;
    }

    private Rectangle DEBUG_DrawPlayer() {
        Vector2 playerPosition = GAMESTATE.getPlayer().position();
        Vector2 upperLeft = new Vector2(playerPosition.getX() - 5, playerPosition.getY() - 5);
        Vector2 lowerRight = new Vector2(playerPosition.getX() + 5, playerPosition.getY() + 5);
        return new Rectangle(upperLeft, lowerRight, 0x99999900);
    }

    private List<Rectangle> DEBUG_CreateRectangles() {
        List<Rectangle> rectangles = new ArrayList<>();
        int squareWidth = 20;

        for (int x = 0; x < 30; x += 2) {
            int upperLeftX = squareWidth * x;
            int lowerRightX = upperLeftX + squareWidth;

            for (int y = 0; y < 30; y += 2) {
                int upperLeftY = squareWidth * y;
                int lowerRightY = upperLeftY + squareWidth;

                rectangles.add(new Rectangle(new Vector2(upperLeftX, upperLeftY), new Vector2(lowerRightX, lowerRightY), 0x66AB00FF));
            }
        }
        return rectangles;
    }
}
