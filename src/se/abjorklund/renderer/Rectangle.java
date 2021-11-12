package se.abjorklund.renderer;

import se.abjorklund.math.Vector2;

public class Rectangle {

    private Vector2 upperLeft;
    private Vector2 lowerRight;
    private int color;

    public Rectangle(Vector2 upperLeft, Vector2 lowerRight, int color) {
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

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }


    // Static helpers

    public static boolean isInside(Rectangle rectangle, Vector2 pos) {
        int x = pos.getX();
        int y = pos.getY();
        return (x >= rectangle.getUpperLeft().getX() && y >= rectangle.getUpperLeft().getY()) &&
                (x <= rectangle.getLowerRight().getX() && y <= rectangle.getLowerRight().getY());
    }
}
