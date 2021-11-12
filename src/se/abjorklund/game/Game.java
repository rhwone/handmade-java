package se.abjorklund.game;

import se.abjorklund.game.controller.GameControllerInput;
import se.abjorklund.math.Vector2;
import se.abjorklund.renderer.Rectangle;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Game {

    public static final GameState GAMESTATE = new GameState();

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


    public void DEBUG_renderRectangles(GameControllerInput controller) {
        int width = GamePlatform.SCREEN_WIDTH;
        int height = GamePlatform.SCREEN_HEIGHT;
        int bytesPerPixel = GamePlatform.BYTES_PER_PIXEL;
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

        //byte[] buffer = new byte[(width * height) * bytesPerPixel];

        if (controller.isAnalog()) {
            // Analog controls
        } else {
            Vector2 playerPosition = GAMESTATE.getPlayer().position();

            int currentY = playerPosition.getY();
            int currentX = playerPosition.getX();

            int velocity = 20;

            if (controller.getMoveLeft().endedDown()) {
                playerPosition.setX(currentX - velocity);
            }

            if (controller.getMoveRight().endedDown()) {
                playerPosition.setX(currentX + velocity);
            }

            if (controller.getMoveUp().endedDown()) {
                playerPosition.setY(currentY - velocity);
            }

            if (controller.getMoveDown().endedDown()) {
                playerPosition.setY(currentY + velocity);
            }
        }

        List<Rectangle> rectangles = DEBUG_CreateRectangles();
        DEBUG_DrawPlayer(rectangles);


        renderRectangles(bytesPerPixel, pitch, rectangles);
    }

    private void renderRectangles(int bytesPerPixel, int pitch, List<Rectangle> rectangles) {
        ByteBuffer colorBuffer = ByteBuffer.allocate(4);

        for (Rectangle rectangle : rectangles) {
            byte[] color = colorBuffer.putInt(rectangle.getColor()).array();

            Vector2 upperLeft = rectangle.getUpperLeft();
            Vector2 lowerRight = rectangle.getLowerRight();

            int row = upperLeft.getY();
            for (int y = upperLeft.getY(); y <= lowerRight.getY(); ++y) {

                // Maybe only loop the range between top left and bottom right index?

                int pixelOnRow = upperLeft.getX();
                for (int x = upperLeft.getX(); x < lowerRight.getX(); ++x) {

                    int offsetInBytes = (row * pitch) + (pixelOnRow * bytesPerPixel);
                    GamePlatform.VIDEO_BUFFER.put(offsetInBytes, color[0]);
                    GamePlatform.VIDEO_BUFFER.put(offsetInBytes + 1, color[1]);
                    GamePlatform.VIDEO_BUFFER.put(offsetInBytes + 2, color[2]);
                    GamePlatform.VIDEO_BUFFER.put(offsetInBytes + 3, color[3]);
                    /*buffer[offsetInBytes] = color[0];
                    buffer[offsetInBytes + 1] = color[1];
                    buffer[offsetInBytes + 2] = color[2];
                    buffer[offsetInBytes + 3] = color[3];*/

                    ++pixelOnRow;
                }
                ++row;
            }
            colorBuffer.clear();
        }
    }

    private void DEBUG_DrawPlayer(List<Rectangle> rectangles) {
        Vector2 playerPosition = GAMESTATE.getPlayer().position();
        int upperLeftX = playerPosition.getX() - 7;
        int upperLeftY = playerPosition.getY() - 7;
        int lowerRightX = playerPosition.getX() + 7;
        int lowerRightY = playerPosition.getY() + 7;

        rectangles.add(new Rectangle(new Vector2(upperLeftX, upperLeftY), new Vector2(lowerRightX, lowerRightY), 0xFF999900));
        rectangles.add(new Rectangle(new Vector2(upperLeftX + 1, upperLeftY + 1), new Vector2(lowerRightX - 1, lowerRightY - 1), 0x00000000));
        rectangles.add(new Rectangle(new Vector2(upperLeftX + 2, upperLeftY + 2), new Vector2(lowerRightX - 2, lowerRightY - 2), 0xFF999900));
    }

    private List<Rectangle> DEBUG_CreateRectangles() {
        List<Rectangle> rectangles = new ArrayList<>();
        int squareWidth = 20;

        for (int x = 0; x < 50; x++) {
            int upperLeftX = squareWidth * x;
            int lowerRightX = upperLeftX + squareWidth;

            for (int y = 0; y < 30; y++) {
                int upperLeftY = squareWidth * y;
                int lowerRightY = upperLeftY + squareWidth;

                rectangles.add(new Rectangle(new Vector2(upperLeftX, upperLeftY), new Vector2(lowerRightX, lowerRightY), 0x66AB00FF));
                rectangles.add(new Rectangle(new Vector2(upperLeftX + 1, upperLeftY + 1), new Vector2(lowerRightX - 1, lowerRightY - 1), 0x00000000));
                rectangles.add(new Rectangle(new Vector2(upperLeftX + 2, upperLeftY + 2), new Vector2(lowerRightX - 2, lowerRightY - 2), 0x66AB00FF));
            }
        }
        return rectangles;
    }
}
