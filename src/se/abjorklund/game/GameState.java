package se.abjorklund.game;

import se.abjorklund.math.Vector2;

public final class GameState {
    private final Player player = new Player(new Vector2(10, 10));

    public Player getPlayer() {
        return player;
    }
}
