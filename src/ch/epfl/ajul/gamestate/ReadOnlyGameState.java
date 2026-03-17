package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.packed.*;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;

import java.util.List;

/// A read-only view of a game state.
public interface ReadOnlyGameState {

    Game game();

    int pkTileBag();

    ImmutableIntArray pkTileSources();

    int pkUniqueTileSources();

    ImmutableIntArray pkPlayerStates();

    PlayerId currentPlayerId();

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

    default List<PlayerId> playerIds() {
        return game().playerIds();
    }

    default boolean isRoundOver() {
        ReadOnlyIntArray pkTileSources = pkTileSources();
        for (int sourceIndex = 0; sourceIndex < pkTileSources.size(); sourceIndex++) {
            int withoutMarker = PkTileSet.difference(pkTileSources.get(sourceIndex),
                    PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER));
            if (!PkTileSet.isEmpty(withoutMarker))
                return false;
        }
        return true;
    }


    default boolean isGameOver(){
        if (!isRoundOver()) return false;
        for (PlayerId player : playerIds()){
            int wall = PkPlayerStates.pkWall(pkPlayerStates(), player);
            if (PkWall.hasFullRow(wall)) return true;
        }
        return false;
    }

    default int pkDiscardedTiles() {
        int pkRemainingTiles = PkTileSet.FULL;
        pkRemainingTiles = PkTileSet.difference(pkRemainingTiles, pkTileBag());

        ReadOnlyIntArray pkTileSources = pkTileSources();
        for (int sourceIndex = 0; sourceIndex < pkTileSources.size(); sourceIndex++)
            pkRemainingTiles = PkTileSet.difference(pkRemainingTiles, pkTileSources.get(sourceIndex));

        ReadOnlyIntArray pkPlayerStates = pkPlayerStates();
        for (PlayerId playerId : playerIds()) {
            pkRemainingTiles = PkTileSet.difference(pkRemainingTiles,
                    PkPatterns.asPkTileSet(PkPlayerStates.pkPatterns(pkPlayerStates, playerId)));
            pkRemainingTiles = PkTileSet.difference(pkRemainingTiles,
                    PkFloor.asPkTileSet(PkPlayerStates.pkFloor(pkPlayerStates, playerId)));
            pkRemainingTiles = PkTileSet.difference(pkRemainingTiles,
                    PkWall.asPkTileSet(PkPlayerStates.pkWall(pkPlayerStates, playerId)));
        }

        return pkRemainingTiles;
    }

    private int computeValidMoves(short[] destination, int pkSourceSet) {
        int moveCount = 0;
        ReadOnlyIntArray pkTileSources = pkTileSources();
        ReadOnlyIntArray pkPlayerStates = pkPlayerStates();
        PlayerId currentPlayer = currentPlayerId();
        int pkPatterns = PkPlayerStates.pkPatterns(pkPlayerStates, currentPlayer);
        int pkWall = PkPlayerStates.pkWall(pkPlayerStates, currentPlayer);

        for (int sourceIndex = 0; sourceIndex < pkTileSources.size(); sourceIndex++) {
            if (PkIntSet32.contains(pkSourceSet, sourceIndex)) {
                int pkSource = pkTileSources.get(sourceIndex);
                for (TileKind.Colored color : TileKind.Colored.ALL) {
                    if (PkTileSet.countOf(pkSource, color) > 0) {
                        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
                            if (!PkPatterns.isFull(pkPatterns, line)
                                    && PkPatterns.canContain(pkPatterns, line, color)
                                    && !PkWall.hasTileAt(pkWall, line, color)) {
                                destination[moveCount++] = PkMove.pack(TileSource.ALL.get(sourceIndex), color, line);
                            }
                        }
                        destination[moveCount++] = PkMove.pack(TileSource.ALL.get(sourceIndex),
                                color, TileDestination.FLOOR);
                    }
                }
            }
        }
        return moveCount;
    }

    default int validMoves(short[] destination) {
        int allSources = (1 << pkTileSources().size()) - 1;
        return computeValidMoves(destination, allSources);
    }

    default int uniqueValidMoves(short[] destination) {
        return computeValidMoves(destination, pkUniqueTileSources());
    }

}

