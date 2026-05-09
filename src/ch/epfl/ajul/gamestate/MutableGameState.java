package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.packed.*;
import ch.epfl.ajul.intarray.MutableIntArray;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;

import java.util.Arrays;
import java.util.random.RandomGenerator;

import static ch.epfl.ajul.TileKind.Colored.shuffle;
import static ch.epfl.ajul.TileKind.FIRST_PLAYER_MARKER;
import static ch.epfl.ajul.TileSource.CENTER_AREA;
import static ch.epfl.ajul.TileSource.FACTORY_1;

/// Represents a mutable game state that can evolve as players make moves.
/// This class implements {@link ReadOnlyGameState} and provides methods to
/// fill factories, register moves, end rounds, and end the game.
/// A {@link PointsObserver} is notified whenever a player's score changes.
///
/// @author Hedi Fourati
/// @author Fahd Ben Ismail
public final class MutableGameState implements ReadOnlyGameState {
    // The game configuration (players, factories, etc.)
    private final Game game;
    // Packed tile set representing the tiles remaining in the bag
    private int pkTileBag;
    // Read-only view of the packed tile sources array
    private final ReadOnlyIntArray pkTileSources;
    // Underlying mutable array backing pkTileSources
    private int[] pkTileSourcesList;
    // Packed int set of unique tile source indices
    private int pkUniqueTileSources;
    // Read-only view of the packed player states array
    private final ReadOnlyIntArray pkPlayerStates;
    // Underlying mutable array backing pkPlayerStates
    private int[] pkPlayerStatesList;
    // Identity of the player whose turn it is
    private PlayerId currentPlayerId;
    // Observer notified whenever a player's score changes
    private final PointsObserver pointsObserver;

    /// Creates a new mutable game state with the same content as the given
    /// initial state, and attaches the given points observer to it.
    ///
    /// @param initialState   the initial state to copy
    /// @param pointsObserver the observer to notify when points change
    /// @throws NullPointerException if {@code initialState} or {@code pointsObserver} is {@code null}
    public MutableGameState(ReadOnlyGameState initialState, PointsObserver pointsObserver) {

        this.game = initialState.game();
        this.pkTileBag = initialState.pkTileBag();
        this.pkTileSourcesList = initialState.pkTileSources().toArray();
        this.pkTileSources = MutableIntArray.wrapping(pkTileSourcesList);
        this.pkUniqueTileSources = initialState.pkUniqueTileSources();
        this.pkPlayerStatesList = initialState.pkPlayerStates().toArray();
        this.pkPlayerStates = MutableIntArray.wrapping(pkPlayerStatesList);
        this.currentPlayerId = initialState.currentPlayerId();
        this.pointsObserver = pointsObserver;
    }

    /// Creates a new mutable game state with the same content as the given
    /// initial state and an empty points observer.
    ///
    /// @param initialState the initial state to copy
    public MutableGameState(ReadOnlyGameState initialState) {
        this(initialState, PointsObserver.EMPTY);
    }

    /// Fills the factories with tiles drawn randomly from the bag.
    /// If the bag does not contain enough tiles, all remaining tiles are
    /// extracted, the bag is refilled with the discarded tiles, and additional
    /// tiles are drawn. The extracted tiles are then shuffled and distributed
    /// across the factories.
    ///
    /// @param randomGenerator the random generator used for sampling and shuffling
    public void fillFactories(RandomGenerator randomGenerator) {
        // Total number of tiles needed to fill every factory.
        int tilesNeeded = TileSource.Factory.TILES_PER_FACTORY * game.factoriesCount();
        TileKind.Colored[] tilesToDistribute = new TileKind.Colored[tilesNeeded];

        // If the bag does not contain enough tiles, empty it into the array first,
        // then refill the bag with the discarded tiles before sampling the rest.
        int offset = 0;
        if (PkTileSet.size(pkTileBag) <= tilesNeeded) {
            offset = PkTileSet.copyColoredInto(pkTileBag, tilesToDistribute);
            pkTileBag = pkDiscardedTiles();
        }

        // Randomly sample the remaining tiles from the bag (reservoir sampling).
        int lastIndex = PkTileSet.sampleColoredInto(
                pkTileBag, tilesToDistribute, offset, randomGenerator);

        // In the rare case where there are not enough tiles available, truncate.
        if (lastIndex < tilesNeeded)
            tilesToDistribute = Arrays.copyOf(tilesToDistribute, lastIndex);

        // Remove the sampled tiles from the bag.
        for (int i = offset; i < tilesToDistribute.length; i++)
            pkTileBag = PkTileSet.remove(pkTileBag, tilesToDistribute[i]);

        // Shuffle the tiles and distribute them 4 by 4 into the factories.
        shuffle(tilesToDistribute, randomGenerator);
        for (int i = 0; i < tilesToDistribute.length; i++) {
            int factoryIndex = FACTORY_1.index() + i / TileSource.Factory.TILES_PER_FACTORY;
            pkTileSourcesList[factoryIndex] =
                    PkTileSet.add(pkTileSourcesList[factoryIndex], tilesToDistribute[i]);
        }

        // Recompute the set of "unique" factories: a factory is unique iff no earlier
        // factory has the exact same content.
        for (TileSource.Factory source : game.factories()) {
            if (pkTileSourcesList[source.index()] == PkTileSet.EMPTY) continue;
            boolean isDuplicate = false;
            for (int i = FACTORY_1.index(); i < source.index(); i++) {
                if (pkTileSourcesList[source.index()] == pkTileSourcesList[i]) {
                    isDuplicate = true;
                    break;
                }
            }
            if (!isDuplicate)
                pkUniqueTileSources = PkIntSet32.add(pkUniqueTileSources, source.index());
        }
    }

    /// Updates the game state to reflect the given packed move played by the
    /// current player. Tiles are taken from the source and placed on the
    /// destination, with excess tiles going to the floor line or out of the
    /// game. The next player then becomes the current player.
    ///
    /// @param pkMove the packed representation of the move to register
    public void registerMove(short pkMove) {
        // Decode the packed move.
        TileSource source = PkMove.source(pkMove);
        TileDestination destination = PkMove.destination(pkMove);
        TileKind.Colored color = PkMove.color(pkMove);

        int sourceContent = pkTileSourcesList[source.index()];
        int centerIndex = CENTER_AREA.index();

        // Pick every tile of the chosen color, plus the first player marker if it
        // is present in the source (only possible in the center area).
        int pickedTiles = PkTileSet.union(
                PkTileSet.subsetOf(sourceContent, color),
                PkTileSet.subsetOf(sourceContent, FIRST_PLAYER_MARKER));

        if (source == TileSource.CENTER_AREA) {
            // Remove the picked tiles from the center area. If it becomes empty,
            // it is no longer a unique source.
            pkTileSourcesList[centerIndex] =
                    PkTileSet.difference(pkTileSourcesList[centerIndex], pickedTiles);
            if (PkTileSet.isEmpty(pkTileSourcesList[centerIndex]))
                pkUniqueTileSources = PkIntSet32.remove(pkUniqueTileSources, centerIndex);
        } else {
            // The remaining tiles of this factory overflow to the center area.
            int overflow = PkTileSet.difference(sourceContent, pickedTiles);
            pkTileSourcesList[centerIndex] =
                    PkTileSet.union(pkTileSourcesList[centerIndex], overflow);

            // If the overflow adds new tiles to the center, mark it as a unique source.
            if (!PkTileSet.isEmpty(overflow))
                pkUniqueTileSources = PkIntSet32.add(pkUniqueTileSources, centerIndex);

            // If this factory was unique, promote the first later factory that has
            // the same content (if any) to unique status, then demote this factory.
            if (PkIntSet32.contains(pkUniqueTileSources, source.index())) {
                for (int i = source.index() + 1; i < pkTileSourcesList.length; i++) {
                    if (pkTileSourcesList[source.index()] == pkTileSourcesList[i]) {
                        pkUniqueTileSources = PkIntSet32.add(pkUniqueTileSources, i);
                        break;
                    }
                }
                pkUniqueTileSources = PkIntSet32.remove(pkUniqueTileSources, source.index());
            }

            // The factory is now empty.
            pkTileSourcesList[source.index()] = PkTileSet.EMPTY;
        }

        // Place the picked tiles on the destination. Tiles that cannot be placed on
        // the pattern line (or all of them, if the destination is the floor line)
        // will end up on the floor line.
        int unusedTiles;
        if (destination == TileDestination.FLOOR) {
            unusedTiles = pickedTiles;
        } else {
            TileDestination.Pattern patternLine = (TileDestination.Pattern) destination;
            int currentPatterns = PkPlayerStates.pkPatterns(pkPlayerStates, currentPlayerId);
            int existingTileCount = PkPatterns.size(currentPatterns, patternLine);
            int coloredTileCount = PkTileSet.countOf(pickedTiles, color);
            int markerCount = PkTileSet.countOf(pickedTiles, FIRST_PLAYER_MARKER);

            // Add as many colored tiles to the pattern line as it can hold.
            int tilesToAdd = Math.min(patternLine.capacity() - existingTileCount, coloredTileCount);
            if (tilesToAdd > 0) {
                int newPatterns = PkPatterns.withAddedTiles(
                        currentPatterns, patternLine, tilesToAdd, color);
                PkPlayerStates.setPkPatterns(pkPlayerStatesList, currentPlayerId, newPatterns);
            }

            // Colored tiles that did not fit, plus the first player marker, go to the floor.
            unusedTiles = PkTileSet.union(
                    PkTileSet.of(coloredTileCount - tilesToAdd, color),
                    PkTileSet.of(markerCount, FIRST_PLAYER_MARKER));
        }

        // Add the unused tiles to the current player's floor line.
        PkPlayerStates.setPkFloor(
                pkPlayerStatesList,
                currentPlayerId,
                PkFloor.withAddedTiles(
                        PkPlayerStates.pkFloor(pkPlayerStates, currentPlayerId), unusedTiles));

        // Turn passes to the next player.
        int nextIndex = (currentPlayerId.ordinal() + 1) % game.playerIds().size();
        currentPlayerId = game.playerIds().get(nextIndex);
    }

    /// Ends the current round by transferring completed pattern line tiles to
    /// the wall, computing points for each new wall tile, applying floor
    /// penalties, and clearing floor lines. The first player marker is returned
    /// to the center area, and the player who held it becomes the current player.
    public void endRound() {
        for (PlayerId playerId : game.playerIds()) {
            int playerPatterns = PkPlayerStates.pkPatterns(pkPlayerStates, playerId);
            int playerWall = PkPlayerStates.pkWall(pkPlayerStates, playerId);

            for (TileDestination.Pattern pattern : TileDestination.Pattern.ALL) {
                if (PkPatterns.isFull(playerPatterns, pattern)) {
                    TileKind.Colored patternColor = PkPatterns.color(playerPatterns, pattern);

                    playerWall = PkWall.withTileAt(playerWall, pattern, patternColor);

                    int hGroupSize = PkWall.hGroupSize(playerWall, pattern, patternColor);
                    int vGroupSize = PkWall.vGroupSize(playerWall, pattern, patternColor);
                    int points = Points.newWallTilePoints(hGroupSize, vGroupSize);

                    PkPlayerStates.addPoints(pkPlayerStatesList, playerId, points);
                    pointsObserver.newWallTile(playerId, pattern, patternColor, points);

                    playerPatterns = PkPatterns.withEmptyLine(playerPatterns, pattern);
                }
            }

            PkPlayerStates.setPkWall(pkPlayerStatesList, playerId, playerWall);
            PkPlayerStates.setPkPatterns(pkPlayerStatesList, playerId, playerPatterns);

            int playerFloor = PkPlayerStates.pkFloor(pkPlayerStates, playerId);

            if (playerFloor != PkFloor.EMPTY) {
                int currentPoints = PkPlayerStates.points(pkPlayerStates, playerId);
                int penalty = Math.min(currentPoints,
                        Points.totalFloorPenalty(PkFloor.size(playerFloor)));

                PkPlayerStates.addPoints(pkPlayerStatesList, playerId, -penalty);
                if (penalty > 0) {
                    pointsObserver.floor(playerId, penalty);
                }

                if (PkFloor.containsFirstPlayerMarker(playerFloor)) {
                    currentPlayerId = playerId;
                    pkTileSourcesList[TileSource.CENTER_AREA.index()] =
                            PkTileSet.union(
                                    pkTileSourcesList[TileSource.CENTER_AREA.index()],
                                    PkTileSet.of(1, FIRST_PLAYER_MARKER)
                            );
                }

                PkPlayerStates.setPkFloor(pkPlayerStatesList, playerId, PkFloor.EMPTY);
            }
        }
    }

    /// Ends the game by awarding bonus points to all players for complete rows,
    /// complete columns, and complete colors on their walls.
    public void endGame() {
        for (PlayerId playerId : game.playerIds()) {
            int playerWall = PkPlayerStates.pkWall(pkPlayerStates, playerId);

            for (TileDestination.Pattern pattern : TileDestination.Pattern.ALL) {
                if (PkWall.isRowFull(playerWall, pattern)) {
                    PkPlayerStates.addPoints(
                            pkPlayerStatesList,
                            playerId,
                            Points.FULL_ROW_BONUS_POINTS
                    );
                    pointsObserver.fullRow(
                            playerId,
                            pattern,
                            Points.FULL_ROW_BONUS_POINTS
                    );
                }
            }

            for (int column = 0; column < PkWall.WALL_WIDTH; column++) {
                if (PkWall.isColumnFull(playerWall, column)) {
                    PkPlayerStates.addPoints(
                            pkPlayerStatesList,
                            playerId,
                            Points.FULL_COLUMN_BONUS_POINTS
                    );
                    pointsObserver.fullColumn(
                            playerId,
                            column,
                            Points.FULL_COLUMN_BONUS_POINTS
                    );
                }
            }

            for (TileKind.Colored color : TileKind.Colored.ALL) {
                if (PkWall.isColorFull(playerWall, color)) {
                    PkPlayerStates.addPoints(
                            pkPlayerStatesList,
                            playerId,
                            Points.FULL_COLOR_BONUS_POINTS
                    );
                    pointsObserver.fullColor(
                            playerId,
                            color,
                            Points.FULL_COLOR_BONUS_POINTS
                    );
                }
            }
        }
    }

    /// {@inheritDoc}
    @Override
    public Game game() {
        return game;
    }

    /// {@inheritDoc}
    @Override
    public int pkTileBag() {
        return pkTileBag;
    }

    /// {@inheritDoc}
    @Override
    public ReadOnlyIntArray pkTileSources() {
        return pkTileSources;
    }

    /// {@inheritDoc}
    @Override
    public int pkUniqueTileSources() {
        return pkUniqueTileSources;
    }

    /// {@inheritDoc}
    @Override
    public ReadOnlyIntArray pkPlayerStates() {
        return pkPlayerStates;
    }

    /// {@inheritDoc}
    @Override
    public PlayerId currentPlayerId() {
        return currentPlayerId;
    }
}
