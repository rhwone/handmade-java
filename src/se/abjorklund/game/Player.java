package se.abjorklund.game;

import se.abjorklund.math.Vector2;

public class Player {
    private final Vector2 position;

    public Player(Vector2 position) {
        this.position = position;
    }

    public Vector2 position() {
        return position;
    }
}
