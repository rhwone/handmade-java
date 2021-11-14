package se.abjorklund.renderer;

import se.abjorklund.game.GamePlatform;

public final class Renderer {

    public static void drawRectangle(int bytesPerPixel, int pitch, Rectangle rectangle) {
        int color = rectangle.getColor();

        int minX = Math.round(rectangle.getUpperLeft().getX());
        int minY = Math.round(rectangle.getUpperLeft().getY());
        int maxX = Math.round(rectangle.getLowerRight().getX());
        int maxY = Math.round(rectangle.getLowerRight().getY());

        for (int y = minY; y <= maxY; ++y) {
            for (int x = minX; x < maxX; ++x) {

                int offsetInBytes = (y * pitch) + (x * bytesPerPixel);

                GamePlatform.VIDEO_BUFFER.put(offsetInBytes, (byte) (color >> 24));
                GamePlatform.VIDEO_BUFFER.put(offsetInBytes + 1, (byte) (color >> 16));
                GamePlatform.VIDEO_BUFFER.put(offsetInBytes + 2, (byte) (color >> 8));
                GamePlatform.VIDEO_BUFFER.put(offsetInBytes + 3, (byte) (color));
            }
        }
    }
}