package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.packed.PkIntSet32;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkTileSet;
import ch.epfl.ajul.intarray.ImmutableIntArray;

import java.util.Arrays;
import java.util.Objects;

public record ImmutableGameState(
        Game game,
        int pkTileBag,
        ImmutableIntArray pkTileSources,
        int pkUniqueTileSources,
        ImmutableIntArray pkPlayerStates,
        PlayerId currentPlayerId
) implements ReadOnlyGameState {

    public ImmutableGameState {
        Objects.requireNonNull(game);
        Objects.requireNonNull(pkTileSources);
        Objects.requireNonNull(pkPlayerStates);
        Objects.requireNonNull(currentPlayerId);
    }

    public static ImmutableGameState initial(Game game) {
        int[] pKTileSource =  new int[game.tileSourcesCount()];
        Arrays.fill(pKTileSource, PkTileSet.EMPTY);
        pKTileSource[TileSource.CENTER_AREA.index()] = PkTileSet.of(1,
                TileKind.FIRST_PLAYER_MARKER);

        return new ImmutableGameState(
                game,
                PkTileSet.FULL_COLORED,
                ImmutableIntArray.copyOf(pKTileSource),
                PkIntSet32.EMPTY,
                PkPlayerStates.initial(game),
                game.playerIds().getFirst()
        );
    }

    @Override
    public ImmutableGameState immutable() {
        return this;
    }
}
