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


    public void DEBUG_render(GameControllerInput controller) {
        int width = GamePlatform.SCREEN_WIDTH;
        int height = GamePlatform.SCREEN_HEIGHT;
        int bytesPerPixel = GamePlatform.BYTES_PER_PIXEL;
        int pitch = width * bytesPerPixel;


        if (controller.isAnalog()) {
            // Analog controls
        } else {
            Vector2 playerPosition = GAMESTATE.getPlayer().position();

            float currentY = playerPosition.getY();
            float currentX = playerPosition.getX();

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

        drawRectangles(bytesPerPixel, pitch, rectangles);
    }

    private void drawRectangles(int bytesPerPixel, int pitch, List<Rectangle> rectangles) {
        ByteBuffer colorBuffer = ByteBuffer.allocate(4);

        for (Rectangle rectangle : rectangles) {
            drawRectangle(bytesPerPixel, pitch, colorBuffer, rectangle);
            colorBuffer.clear();
        }
    }

    private void drawRectangle(int bytesPerPixel, int pitch, ByteBuffer colorBuffer, Rectangle rectangle) {
        byte[] color = colorBuffer.putInt(rectangle.getColor()).array();

        Vector2 upperLeft = rectangle.getUpperLeft();
        Vector2 lowerRight = rectangle.getLowerRight();

        int row = (int)upperLeft.getY();
        for (int y = (int)upperLeft.getY(); y <= lowerRight.getY(); ++y) {
            int pixelOnRow = (int)upperLeft.getX();
            for (int x = (int)upperLeft.getX(); x < lowerRight.getX(); ++x) {

                int offsetInBytes = (row * pitch) + (pixelOnRow * bytesPerPixel);
                GamePlatform.VIDEO_BUFFER.put(offsetInBytes, color[0]);
                GamePlatform.VIDEO_BUFFER.put(offsetInBytes + 1, color[1]);
                GamePlatform.VIDEO_BUFFER.put(offsetInBytes + 2, color[2]);
                GamePlatform.VIDEO_BUFFER.put(offsetInBytes + 3, color[3]);
                ++pixelOnRow;
            }
            ++row;
        }
    }

    private void DEBUG_DrawPlayer(List<Rectangle> rectangles) {
        Vector2 playerPosition = GAMESTATE.getPlayer().position();
        float upperLeftX = playerPosition.getX() - 7;
        float upperLeftY = playerPosition.getY() - 7;
        float lowerRightX = playerPosition.getX() + 7;
        float lowerRightY = playerPosition.getY() + 7;

        rectangles.add(new Rectangle(new Vector2(upperLeftX, upperLeftY), new Vector2(lowerRightX, lowerRightY), 0xFFFFFFFF));
        rectangles.add(new Rectangle(new Vector2(upperLeftX + 1, upperLeftY + 1), new Vector2(lowerRightX - 1, lowerRightY - 1), 0x00000000));
        rectangles.add(new Rectangle(new Vector2(upperLeftX + 2, upperLeftY + 2), new Vector2(lowerRightX - 2, lowerRightY - 2), 0x99999999));
    }

    private List<Rectangle> DEBUG_CreateRectangles() {
        List<Rectangle> rectangles = new ArrayList<>();
        int rectanglesPerRow = 50;
        int squareWidth = GamePlatform.SCREEN_WIDTH / rectanglesPerRow;
        int squareHeight = GamePlatform.SCREEN_HEIGHT / rectanglesPerRow;

        for (int x = 0; x < rectanglesPerRow; x++) {
            int upperLeftX = squareWidth * x;
            int lowerRightX = upperLeftX + squareWidth;

            for (int y = 0; y < 30; y++) {
                int upperLeftY = squareHeight * y;
                int lowerRightY = upperLeftY + squareHeight;

                rectangles.add(new Rectangle(new Vector2(upperLeftX, upperLeftY), new Vector2(lowerRightX, lowerRightY), 0x66AB00FF));
                rectangles.add(new Rectangle(new Vector2(upperLeftX + 1, upperLeftY + 1), new Vector2(lowerRightX - 1, lowerRightY - 1), 0x00000000));
                rectangles.add(new Rectangle(new Vector2(upperLeftX + 2, upperLeftY + 2), new Vector2(lowerRightX - 2, lowerRightY - 2), 0x66AB00FF));
            }
        }
        return rectangles;
    }
}
