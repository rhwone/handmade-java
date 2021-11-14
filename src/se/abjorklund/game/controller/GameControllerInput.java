package se.abjorklund.game.controller;

public final class GameControllerInput {
    private final boolean isConnected;
    private final boolean isAnalog;
    private final float stickAverageX;
    private final float stickAverageY;

    private final GameButtonState moveUp;
    private final GameButtonState moveDown;
    private final GameButtonState moveLeft;
    private final GameButtonState moveRight;

    private final GameButtonState actionUp;
    private final GameButtonState actionDown;
    private final GameButtonState actionLeft;
    private final GameButtonState actionRight;

    private final GameButtonState leftShoulder;
    private final GameButtonState rightShoulder;

    private final GameButtonState back;
    private final GameButtonState start;

    private final float timeStep;

    public GameControllerInput(boolean isConnected,
                               boolean isAnalog,
                               float stickAverageX,
                               float stickAverageY,
                               GameButtonState moveUp,
                               GameButtonState moveDown,
                               GameButtonState moveLeft,
                               GameButtonState moveRight,
                               GameButtonState actionUp,
                               GameButtonState actionDown,
                               GameButtonState actionLeft,
                               GameButtonState actionRight,
                               GameButtonState leftShoulder,
                               GameButtonState rightShoulder,
                               GameButtonState back,
                               GameButtonState start,
                               float timeStep) {
        this.isConnected = isConnected;
        this.isAnalog = isAnalog;
        this.stickAverageX = stickAverageX;
        this.stickAverageY = stickAverageY;
        this.moveUp = moveUp;
        this.moveDown = moveDown;
        this.moveLeft = moveLeft;
        this.moveRight = moveRight;
        this.actionUp = actionUp;
        this.actionDown = actionDown;
        this.actionLeft = actionLeft;
        this.actionRight = actionRight;
        this.leftShoulder = leftShoulder;
        this.rightShoulder = rightShoulder;
        this.back = back;
        this.start = start;
        this.timeStep = timeStep;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public boolean isAnalog() {
        return isAnalog;
    }

    public float getStickAverageX() {
        return stickAverageX;
    }

    public float getStickAverageY() {
        return stickAverageY;
    }

    public GameButtonState getMoveUp() {
        return moveUp;
    }

    public GameButtonState getMoveDown() {
        return moveDown;
    }

    public GameButtonState getMoveLeft() {
        return moveLeft;
    }

    public GameButtonState getMoveRight() {
        return moveRight;
    }

    public GameButtonState getActionUp() {
        return actionUp;
    }

    public GameButtonState getActionDown() {
        return actionDown;
    }

    public GameButtonState getActionLeft() {
        return actionLeft;
    }

    public GameButtonState getActionRight() {
        return actionRight;
    }

    public GameButtonState getLeftShoulder() {
        return leftShoulder;
    }

    public GameButtonState getRightShoulder() {
        return rightShoulder;
    }

    public GameButtonState getBack() {
        return back;
    }

    public GameButtonState getStart() {
        return start;
    }
}
