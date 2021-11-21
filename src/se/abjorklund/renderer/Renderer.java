package se.abjorklund.renderer;

import se.abjorklund.win32.Win32Platform;

public final class Renderer {

    public static void drawRectangle(Rectangle rectangle) {
        int screenWidth = Win32Platform.SCREEN_WIDTH;
        int screenHeight = Win32Platform.SCREEN_HEIGHT;
        int bytesPerPixel = Win32Platform.BYTES_PER_PIXEL;

        int pitch = screenWidth * bytesPerPixel;

        Color color = rectangle.getColor();

        byte red = (byte) Math.round(color.R() * 255);
        byte green = (byte) Math.round(color.G() * 255);
        byte blue = (byte) Math.round(color.B() * 255);
        byte alpha = (byte) Math.round(color.A() * 255);

        int minX = Math.round(rectangle.getUpperLeft().X);
        int minY = Math.round(rectangle.getUpperLeft().Y);
        int maxX = Math.round(rectangle.getLowerRight().X);
        int maxY = Math.round(rectangle.getLowerRight().Y);

        if (minX < 0) {
            minX = 0;
        }

        if (maxX > screenWidth) {
            maxX = screenWidth;
        }

        if (minY < 0) {
            minY = 0;
        }

        if (maxY > screenHeight) {
            maxY = screenHeight;
        }

        for (int y = minY; y < maxY; ++y) {
            for (int x = minX; x < maxX; ++x) {

                int offsetInBytes = (y * pitch) + (x * bytesPerPixel);

                Win32Platform.VIDEO_BUFFER.put(offsetInBytes, blue);
                Win32Platform.VIDEO_BUFFER.put(offsetInBytes + 1, green);
                Win32Platform.VIDEO_BUFFER.put(offsetInBytes + 2, red);
                Win32Platform.VIDEO_BUFFER.put(offsetInBytes + 3, alpha);
            }
        }
    }
}
