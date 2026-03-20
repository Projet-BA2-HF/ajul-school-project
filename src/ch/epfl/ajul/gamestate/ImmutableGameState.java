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

/// An immutable representation of the complete state of an Ajul game.
/// <p>
/// This record implements {@link ReadOnlyGameState} and stores the game configuration, the tile
/// bag, the content of every tile source, the set of unique tile sources, the packed state of
/// each player, and the identity of the current player. Because it is a record, all fields are
/// immutable once constructed.
///
/// @param game                the game configuration
/// @param pkTileBag           the packed tile bag (see {@link PkTileSet})
/// @param pkTileSources       the packed content of each tile source
/// @param pkUniqueTileSources the packed set of unique source indices (see {@link PkIntSet32})
/// @param pkPlayerStates      the packed state of all players (see {@link PkPlayerStates})
/// @param currentPlayerId     the identity of the current player
public record ImmutableGameState(
        Game game,
        int pkTileBag,
        ImmutableIntArray pkTileSources,
        int pkUniqueTileSources,
        ImmutableIntArray pkPlayerStates,
        PlayerId currentPlayerId
) implements ReadOnlyGameState {

    /// Compact constructor that validates that no nullable argument is {@code null}.
    public ImmutableGameState {
        Objects.requireNonNull(game);
        Objects.requireNonNull(pkTileSources);
        Objects.requireNonNull(pkPlayerStates);
        Objects.requireNonNull(currentPlayerId);
    }

    /// Returns the initial state of a game. In this state, the bag contains all 100 colored tiles
    /// (20 of each color), the center area contains only the first player marker, all factories
    /// are empty, all player boards are blank (empty patterns, floor and wall, 0 points), and the
    /// current player is the first player in the game.
    ///
    /// @param game the game configuration
    /// @return the initial immutable game state
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

    /// {@inheritDoc}
    /// Since this state is already immutable, this method simply returns {@code this}.
    @Override
    public ImmutableGameState immutable() {
        return this;
    }
}
