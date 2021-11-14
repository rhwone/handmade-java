package se.abjorklund.game.controller;

public final class GameButtonState {
    private final int halfTransitionCount;
    private final boolean endedDown;

    public GameButtonState(int halfTransitionCount, boolean endedDown) {
        this.halfTransitionCount = halfTransitionCount;
        this.endedDown = endedDown;
    }

    public int getHalfTransitionCount() {
        return halfTransitionCount;
    }

    public boolean endedDown() {
        return endedDown;
    }
}
