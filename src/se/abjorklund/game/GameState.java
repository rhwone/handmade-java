package se.abjorklund.game;

import se.abjorklund.math.Vector2;

public class GameState {
    private final Player player = new Player(new Vector2(20, 20));

    public Player getPlayer() {
        return player;
    }
}
