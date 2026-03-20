package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.packed.*;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;

import java.util.List;

import static ch.epfl.ajul.gamestate.packed.PkIntSet32.EMPTY;

/// A read-only view of the complete state of an Ajul game.
/// <p>
/// The state includes the game configuration, the tile bag, the content of every tile source
/// (center area and factories), the set of unique tile sources, the packed state of each player
/// (pattern lines, floor, wall, score), and the identity of the current player.
/// <p>
/// Implementations must provide the six abstract accessor methods; all other methods are
/// provided as default methods computed from those accessors.
public interface ReadOnlyGameState {

    /// Returns the configuration of the game (players, names, types).
    ///
    /// @return the game configuration
    Game game();

    /// Returns the packed tile bag from which tiles are drawn to fill factories.
    ///
    /// @return the packed tile bag (see {@link PkTileSet})
    int pkTileBag();

    /// Returns an array describing the content of each tile source. The element at index
    /// {@code i} is the packed tile set (see {@link PkTileSet}) of the source with index {@code i}.
    ///
    /// @return the packed tile sources array
    ReadOnlyIntArray pkTileSources();

    /// Returns the packed set (see {@link PkIntSet32}) of indices of the unique tile sources.
    /// A source is unique if it contains at least one colored tile and no earlier factory has
    /// the same content.
    ///
    /// @return the packed set of unique source indices
    int pkUniqueTileSources();

    /// Returns an array containing the packed state of each player (see {@link PkPlayerStates}).
    ///
    /// @return the packed player states array
    ReadOnlyIntArray pkPlayerStates();

    /// Returns the identity of the player whose turn it is.
    ///
    /// @return the current player's identity
    PlayerId currentPlayerId();

    /// Returns an immutable copy of this game state.
    /// If this state is already an {@link ImmutableGameState}, the implementation may return
    /// {@code this} directly to avoid unnecessary copying.
    ///
    /// @return an immutable snapshot of this game state
    default ImmutableGameState immutable() {
        return new ImmutableGameState(
                game(),
                pkTileBag(),
                pkTileSources().immutable(),
                pkUniqueTileSources(),
                pkPlayerStates().immutable(),
                currentPlayerId()
        );
    }

    /// Returns the list of player identities participating in this game.
    ///
    /// @return the list of player identities
    default List<PlayerId> playerIds() {
        return game().playerIds();
    }

    /// Returns {@code true} if the current round is over, i.e. no tile source contains any
    /// colored tile (the set of unique tile sources is empty).
    ///
    /// @return {@code true} iff the round is over
    default boolean isRoundOver() {
        return pkUniqueTileSources() == EMPTY;
    }

    /// Returns {@code true} if the game is over, i.e. the round is over and at least one player
    /// has a complete horizontal row in their wall.
    ///
    /// @return {@code true} iff the game is over
    default boolean isGameOver(){
        if (!isRoundOver()) return false;
        for (PlayerId player : playerIds()){
            int wall = PkPlayerStates.pkWall(pkPlayerStates(), player);
            if (PkWall.hasFullRow(wall)) return true;
        }
        return false;
    }

    /// Returns the packed tile set of discarded tiles, i.e. tiles that are neither in the bag,
    /// nor on any tile source, nor on any player's pattern lines, floor or wall.
    ///
    /// @return the packed set of discarded tiles (see {@link PkTileSet})
    default int pkDiscardedTiles() {
        int pkRemainingTiles = PkTileSet.FULL;
        pkRemainingTiles = PkTileSet.difference(pkRemainingTiles, pkTileBag());

        for (int sourceIndex = 0; sourceIndex < pkTileSources().size(); sourceIndex++)
            pkRemainingTiles = PkTileSet.difference(pkRemainingTiles,
                    pkTileSources().get(sourceIndex));

        for (PlayerId playerId : playerIds()) {
            pkRemainingTiles = PkTileSet.difference(pkRemainingTiles,
                    PkPatterns.asPkTileSet(PkPlayerStates.pkPatterns(pkPlayerStates(),
                            playerId)));
            pkRemainingTiles = PkTileSet.difference(pkRemainingTiles,
                    PkFloor.asPkTileSet(PkPlayerStates.pkFloor(pkPlayerStates(),
                            playerId)));
            pkRemainingTiles = PkTileSet.difference(pkRemainingTiles,
                    PkWall.asPkTileSet(PkPlayerStates.pkWall(pkPlayerStates(),
                            playerId)));
        }

        return pkRemainingTiles;
    }

    /// Computes all valid moves for the current player, considering only the sources whose
    /// indices are in {@code pkSourceSet}. Each valid move is a packed short (see {@link PkMove})
    /// placed in {@code destination}. Returns the number of moves written.
    ///
    /// @param destination the array into which moves are written (size >= {@link Move#MAX_MOVES})
    /// @param pkSourceSet the packed set of source indices to consider
    /// @return the number of valid moves placed in {@code destination}
    private int computeValidMoves(short[] destination, int pkSourceSet) {
        int moveCount = 0;
        PlayerId currentPlayer = currentPlayerId();
        int pkPatterns = PkPlayerStates.pkPatterns(pkPlayerStates(), currentPlayer);
        int pkWall = PkPlayerStates.pkWall(pkPlayerStates(), currentPlayer);

        for (TileSource source : game().tileSources()) {
            if (PkIntSet32.contains(pkSourceSet, source.index())) {
                int pkSource = pkTileSources().get(source.index());
                for (TileKind.Colored color : TileKind.Colored.ALL) {
                    if (PkTileSet.countOf(pkSource, color) > 0) {
                        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
                            if (!PkPatterns.isFull(pkPatterns, line)
                                    && PkPatterns.canContain(pkPatterns, line, color)
                                    && !PkWall.hasTileAt(pkWall, line, color)) {
                                destination[moveCount++] = PkMove.pack(source, color, line);
                            }
                        }
                        destination[moveCount++] = PkMove.pack(source,
                                color, TileDestination.FLOOR);
                    }
                }
            }
        }
        return moveCount;
    }

    /// Places all valid moves for the current player into {@code destination} and returns their
    /// count. All tile sources are considered.
    ///
    /// @param destination the array into which moves are written (size >= {@link Move#MAX_MOVES})
    /// @return the number of valid moves
    default int validMoves(short[] destination) {
        int allSources = (1 << pkTileSources().size()) - 1;
        return computeValidMoves(destination, allSources);
    }

    /// Places all valid moves for the current player into {@code destination}, considering only
    /// unique tile sources, and returns their count. This avoids evaluating equivalent moves
    /// from factories with identical contents.
    ///
    /// @param destination the array into which moves are written (size >= {@link Move#MAX_MOVES})
    /// @return the number of unique valid moves
    default int uniqueValidMoves(short[] destination) {
        return computeValidMoves(destination, pkUniqueTileSources());
    }

}

