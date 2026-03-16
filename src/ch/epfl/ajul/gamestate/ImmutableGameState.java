package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;

public record ImmutableGameState() implements ReadOnlyIntArray {
//    Game game;

    @Override
    public int size() {
        return 0;
    }

    @Override
    public int get(int i) {
        return 0;
    }

    @Override
    public ImmutableIntArray immutable() {
        return null;
    }

    @Override
    public int[] toArray() {
        return new int[0];
    }
}
