package se.abjorklund.game;

import se.abjorklund.game.controller.GameControllerInput;
import se.abjorklund.math.Vector2;
import se.abjorklund.renderer.Color;
import se.abjorklund.renderer.Colors;
import se.abjorklund.renderer.Rectangle;
import se.abjorklund.win32.Win32Platform;

import java.util.ArrayList;
import java.util.List;

import static se.abjorklund.renderer.Renderer.drawRectangle;

public final class Game {

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


    public void DEBUG_render(GameControllerInput controller, float dT) {
        int width = Win32Platform.SCREEN_WIDTH;
        int height = Win32Platform.SCREEN_HEIGHT;
        int bytesPerPixel = Win32Platform.BYTES_PER_PIXEL;
        int pitch = width * bytesPerPixel;


        Vector2 playerPosition = GAMESTATE.getPlayer().position();
        if (controller.isAnalog()) {
            // Analog controls
        } else {

            float dPlayerX = 0.0f;
            float dPlayerY = 0.0f;

            if (controller.getMoveUp().endedDown()) {
                dPlayerY = -1.0f;
            }

            if (controller.getMoveDown().endedDown()) {
                dPlayerY = 1.0f;
            }

            if (controller.getMoveLeft().endedDown()) {
                dPlayerX = -1.0f;
            }

            if (controller.getMoveRight().endedDown()) {
                dPlayerX = 1.0f;
            }
            dPlayerX *= 64.0f;
            dPlayerY *= 64.0f;

            float newPlayerX = GAMESTATE.getPlayer().position().X += dT*dPlayerX;
            float newPlayerY = GAMESTATE.getPlayer().position().Y += dT*dPlayerY;

            
        }


        //List<Rectangle> rectangles = DEBUG_CreateRectangles();
        //DEBUG_DrawPlayer(rectangles);

        int[][] tileMap = {
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
        };

        List<Rectangle> rectangles = new ArrayList<>();

        float tileWidth = 50;
        float tileHeight = 50;

        for (int row = 0; row < 9; row++) {
            for (int column = 0; column < 17; column++) {
                int tileValue = tileMap[row][column];

                float upperLeftX = ((float) column) * tileWidth;
                float upperLeftY = ((float) row) * tileHeight;
                float lowerRightX = upperLeftX + tileWidth;
                float lowerRightY = upperLeftY + tileHeight;

                Color color = tileValue == 1 ? Colors.GRAY : Colors.WHITE;
                rectangles.add(new Rectangle(new Vector2(upperLeftX, upperLeftY), new Vector2(lowerRightX, lowerRightY), color));
            }
        }

        float playerWidth = 0.75f * tileWidth;
        float playerHeight = tileHeight;
        float playerLeft = playerPosition.X - 0.5f * playerWidth;
        float playerTop = playerPosition.Y - playerHeight;

        Vector2 playerUpperLeft = new Vector2(playerLeft, playerTop);
        Vector2 playerLowerRight = new Vector2(playerLeft + playerWidth, playerTop + playerHeight);
        Rectangle player = new Rectangle(playerUpperLeft, playerLowerRight, Colors.RED);

        rectangles.add(player);

        drawRectangles(rectangles);
    }

    private void drawRectangles(List<Rectangle> rectangles) {
        for (Rectangle rectangle : rectangles) {
            drawRectangle(rectangle);
        }
    }


    private void DEBUG_DrawPlayer(List<Rectangle> rectangles) {
        Vector2 playerPosition = GAMESTATE.getPlayer().position();
        float upperLeftX = playerPosition.X - 7;
        float upperLeftY = playerPosition.Y - 7;
        float lowerRightX = playerPosition.X + 7;
        float lowerRightY = playerPosition.Y + 7;

        Color playerColor = new Color(0.0f, 0.0f, 0.0f, 0);
        rectangles.add(new Rectangle(new Vector2(upperLeftX, upperLeftY), new Vector2(lowerRightX, lowerRightY), playerColor));
        rectangles.add(new Rectangle(new Vector2(upperLeftX + 1, upperLeftY + 1), new Vector2(lowerRightX - 1, lowerRightY - 1), playerColor));
        rectangles.add(new Rectangle(new Vector2(upperLeftX + 2, upperLeftY + 2), new Vector2(lowerRightX - 2, lowerRightY - 2), playerColor));
    }

    private List<Rectangle> DEBUG_CreateRectangles() {
        List<Rectangle> rectangles = new ArrayList<>();
        int rectanglesPerRow = 50;
        int squareWidth = Win32Platform.SCREEN_WIDTH / rectanglesPerRow;
        int squareHeight = Win32Platform.SCREEN_HEIGHT / rectanglesPerRow;

        for (int x = 0; x < rectanglesPerRow; x++) {
            int upperLeftX = squareWidth * x;
            int lowerRightX = upperLeftX + squareWidth;

            for (int y = 0; y < 30; y++) {
                int upperLeftY = squareHeight * y;
                int lowerRightY = upperLeftY + squareHeight;

                rectangles.add(new Rectangle(new Vector2(upperLeftX, upperLeftY), new Vector2(lowerRightX, lowerRightY), Colors.WHITE));
                rectangles.add(new Rectangle(new Vector2(upperLeftX + 1, upperLeftY + 1), new Vector2(lowerRightX - 1, lowerRightY - 1), Colors.BLACK));
                rectangles.add(new Rectangle(new Vector2(upperLeftX + 2, upperLeftY + 2), new Vector2(lowerRightX - 2, lowerRightY - 2), Colors.YELLOW));
            }
        }
        return rectangles;
    }
}
