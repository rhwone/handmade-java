package se.abjorklund.renderer;

import se.abjorklund.math.Vector2;

public class Rectangle {

    private Vector2 upperLeft;
    private Vector2 lowerRight;
    private Color color;

    public Rectangle(Vector2 upperLeft, Vector2 lowerRight, Color color) {
        this.upperLeft = upperLeft;
        this.lowerRight = lowerRight;
        this.color = color;
    }

    public Vector2 getUpperLeft() {
        return upperLeft;
    }

    public void setUpperLeft(Vector2 upperLeft) {
        this.upperLeft = upperLeft;
    }

    public Vector2 getLowerRight() {
        return lowerRight;
    }

    public void setLowerRight(Vector2 lowerRight) {
        this.lowerRight = lowerRight;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }


    // Static helpers

    public static boolean isInside(Rectangle rectangle, Vector2 pos) {
        float x = pos.X;
        float y = pos.Y;
        return (x >= rectangle.getUpperLeft().X && y >= rectangle.getUpperLeft().Y) &&
                (x <= rectangle.getLowerRight().X && y <= rectangle.getLowerRight().Y);
    }
}
