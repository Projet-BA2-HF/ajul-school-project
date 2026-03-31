package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.packed.*;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.*;

class OurMutableGameStateTest {

    // =========================================================
    // Données et helpers communs
    // =========================================================

    private static final Game GAME_2P = new Game(List.of(
            new Game.PlayerDescription(PlayerId.P1, "P1", Game.PlayerDescription.PlayerKind.HUMAN),
            new Game.PlayerDescription(PlayerId.P2, "P2", Game.PlayerDescription.PlayerKind.HUMAN)
    ));

    private static MutableGameState newState(
            int pkTileBag,
            int[] pkTileSources,
            int pkUniqueTileSources,
            int[] pkPlayerStates,
            PlayerId currentPlayerId,
            PointsObserver pointsObserver
    ) {
        return new MutableGameState(
                new ImmutableGameState(
                        GAME_2P,
                        pkTileBag,
                        ImmutableIntArray.copyOf(pkTileSources),
                        pkUniqueTileSources,
                        ImmutableIntArray.copyOf(pkPlayerStates),
                        currentPlayerId
                ),
                pointsObserver
        );
    }

    private static MutableGameState newState(
            int[] pkTileSources,
            int[] pkPlayerStates,
            PlayerId currentPlayerId,
            PointsObserver pointsObserver
    ) {
        return newState(
                PkTileSet.EMPTY,
                pkTileSources,
                PkIntSet32.EMPTY,
                pkPlayerStates,
                currentPlayerId,
                pointsObserver
        );
    }

    private static int[] emptyTileSourcesWithMarkerInCenter() {
        int[] pkTileSources = new int[GAME_2P.tileSourcesCount()];
        pkTileSources[TileSource.CENTER_AREA.index()] =
                PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);
        return pkTileSources;
    }

    private static int[] emptyTileSourcesWithoutMarker() {
        return new int[GAME_2P.tileSourcesCount()];
    }

    private static int[] initialPlayerStates() {
        return PkPlayerStates.initial(GAME_2P).toArray();
    }

    private static int coloredCount(int pkTileSet) {
        int count = 0;
        for (TileKind.Colored color : TileKind.Colored.ALL) {
            count += PkTileSet.countOf(pkTileSet, color);
        }
        return count;
    }

    private static int totalColoredTilesInState(ReadOnlyGameState state) {
        int total = coloredCount(state.pkTileBag());

        for (int i = 0; i < state.pkTileSources().size(); i++) {
            total += coloredCount(state.pkTileSources().get(i));
        }

        for (PlayerId playerId : state.game().playerIds()) {
            total += coloredCount(PkPatterns.asPkTileSet(
                    PkPlayerStates.pkPatterns(state.pkPlayerStates(), playerId)));
            total += coloredCount(PkFloor.asPkTileSet(
                    PkPlayerStates.pkFloor(state.pkPlayerStates(), playerId)));
            total += coloredCount(PkWall.asPkTileSet(
                    PkPlayerStates.pkWall(state.pkPlayerStates(), playerId)));
        }

        return total;
    }

    /**
     * Le centre est unique s’il contient des tuiles colorées.
     * Une fabrique est unique si elle n’est pas vide et qu’aucune fabrique précédente
     * n’a exactement le même contenu.
     */
    private static int expectedUniqueTileSources(int[] pkTileSources) {
        int expected = PkIntSet32.EMPTY;

        if (coloredCount(pkTileSources[TileSource.CENTER_AREA.index()]) > 0) {
            expected = PkIntSet32.add(expected, TileSource.CENTER_AREA.index());
        }

        for (int i = TileSource.FACTORY_1.index(); i < GAME_2P.tileSourcesCount(); i++) {
            if (pkTileSources[i] == PkTileSet.EMPTY) continue;

            boolean duplicateEarlierFactory = false;
            for (int j = TileSource.FACTORY_1.index(); j < i; j++) {
                if (pkTileSources[j] == pkTileSources[i]) {
                    duplicateEarlierFactory = true;
                    break;
                }
            }

            if (!duplicateEarlierFactory) {
                expected = PkIntSet32.add(expected, i);
            }
        }

        return expected;
    }

    private static int fullRow(TileDestination.Pattern row) {
        int wall = PkWall.EMPTY;
        for (TileKind.Colored color : TileKind.Colored.ALL) {
            wall = PkWall.withTileAt(wall, row, color);
        }
        return wall;
    }

    private static int fullColumn(int column) {
        int wall = PkWall.EMPTY;
        for (TileDestination.Pattern row : TileDestination.Pattern.ALL) {
            wall = PkWall.withTileAt(wall, row, PkWall.colorAt(row, column));
        }
        return wall;
    }

    private static int fullColor(TileKind.Colored color) {
        int wall = PkWall.EMPTY;
        for (TileDestination.Pattern row : TileDestination.Pattern.ALL) {
            wall = PkWall.withTileAt(wall, row, color);
        }
        return wall;
    }

    private static final class RecordingPointsObserver implements PointsObserver {
        int newWallTileCallCount = 0;
        PlayerId newWallTilePlayerId = null;
        TileDestination.Pattern newWallTileLine = null;
        TileKind.Colored newWallTileColor = null;
        int newWallTilePoints = 0;

        int floorCallCount = 0;
        PlayerId floorPlayerId = null;
        int floorPenalty = 0;

        int fullRowCallCount = 0;
        PlayerId fullRowPlayerId = null;
        TileDestination.Pattern fullRowLine = null;
        int fullRowPoints = 0;

        int fullColumnCallCount = 0;
        PlayerId fullColumnPlayerId = null;
        int fullColumnIndex = -1;
        int fullColumnPoints = 0;

        int fullColorCallCount = 0;
        PlayerId fullColorPlayerId = null;
        TileKind.Colored fullColor = null;
        int fullColorPoints = 0;

        @Override
        public void newWallTile(PlayerId playerId,
                                TileDestination.Pattern line,
                                TileKind.Colored color,
                                int points) {
            newWallTileCallCount += 1;
            newWallTilePlayerId = playerId;
            newWallTileLine = line;
            newWallTileColor = color;
            newWallTilePoints = points;
        }

        @Override
        public void floor(PlayerId playerId, int penalty) {
            floorCallCount += 1;
            floorPlayerId = playerId;
            floorPenalty = penalty;
        }

        @Override
        public void fullRow(PlayerId playerId, TileDestination.Pattern line, int points) {
            fullRowCallCount += 1;
            fullRowPlayerId = playerId;
            fullRowLine = line;
            fullRowPoints = points;
        }

        @Override
        public void fullColumn(PlayerId playerId, int column, int points) {
            fullColumnCallCount += 1;
            fullColumnPlayerId = playerId;
            fullColumnIndex = column;
            fullColumnPoints = points;
        }

        @Override
        public void fullColor(PlayerId playerId, TileKind.Colored color, int points) {
            fullColorCallCount += 1;
            fullColorPlayerId = playerId;
            fullColor = color;
            fullColorPoints = points;
        }
    }

    private static int patternsOf(int[] pkPlayerStates, PlayerId playerId) {
        return pkPlayerStates[4 * playerId.ordinal()];
    }

    private static int floorOf(int[] pkPlayerStates, PlayerId playerId) {
        return pkPlayerStates[4 * playerId.ordinal() + 1];
    }

    private static int wallOf(int[] pkPlayerStates, PlayerId playerId) {
        return pkPlayerStates[4 * playerId.ordinal() + 2];
    }

    private static int pointsOf(int[] pkPlayerStates, PlayerId playerId) {
        return pkPlayerStates[4 * playerId.ordinal() + 3];
    }

    private static PlayerId nextPlayer(PlayerId playerId) {
        int nextIndex = (GAME_2P.playerIds().indexOf(playerId) + 1) % GAME_2P.playersCount();
        return GAME_2P.playerIds().get(nextIndex);
    }

    private static int randomColoredTileSet(Random rng, int count) {
        int pkTileSet = PkTileSet.EMPTY;
        for (int i = 0; i < count; i++) {
            TileKind.Colored color = TileKind.Colored.ALL.get(rng.nextInt(TileKind.Colored.ALL.size()));
            pkTileSet = PkTileSet.add(pkTileSet, color);
        }
        return pkTileSet;
    }

    private static TileKind.Colored randomAllowedColorForRow(Random rng, int pkWall, TileDestination.Pattern row) {
        int allowedCount = 0;
        for (TileKind.Colored color : TileKind.Colored.ALL) {
            if (!PkWall.hasTileAt(pkWall, row, color)) {
                allowedCount += 1;
            }
        }
        if (allowedCount == 0) return null;

        int chosen = rng.nextInt(allowedCount);
        for (TileKind.Colored color : TileKind.Colored.ALL) {
            if (!PkWall.hasTileAt(pkWall, row, color)) {
                if (chosen == 0) return color;
                chosen -= 1;
            }
        }
        return null;
    }

    private static int firstAvailableColorIndex(int[] remainingByColor) {
        for (int i = 0; i < remainingByColor.length; i++) {
            if (remainingByColor[i] > 0) return i;
        }
        return -1;
    }

    private static int[] playerStatesLeavingAvailableColoredTiles(int availableColoredTiles) {
        assertTrue(availableColoredTiles >= 6 && availableColoredTiles <= 20);

        int toConsume = 100 - availableColoredTiles;
        int[] pkPlayerStates = initialPlayerStates();
        int[] remainingByColor = {20, 20, 20, 20, 20};

        for (PlayerId playerId : GAME_2P.playerIds()) {
            int wall = PkWall.EMPTY;
            for (TileDestination.Pattern row : TileDestination.Pattern.ALL) {
                for (TileKind.Colored color : TileKind.Colored.ALL) {
                    if (toConsume == 0) {
                        PkPlayerStates.setPkWall(pkPlayerStates, playerId, wall);
                        return pkPlayerStates;
                    }
                    if (remainingByColor[color.index()] == 0) continue;

                    wall = PkWall.withTileAt(wall, row, color);
                    remainingByColor[color.index()] -= 1;
                    toConsume -= 1;
                }
            }
            PkPlayerStates.setPkWall(pkPlayerStates, playerId, wall);
        }

        for (PlayerId playerId : GAME_2P.playerIds()) {
            int patterns = PkPatterns.EMPTY;
            for (TileDestination.Pattern row : TileDestination.Pattern.ALL) {
                if (toConsume == 0) {
                    PkPlayerStates.setPkPatterns(pkPlayerStates, playerId, patterns);
                    return pkPlayerStates;
                }

                int colorIndex = firstAvailableColorIndex(remainingByColor);
                if (colorIndex < 0) break;

                TileKind.Colored color = TileKind.Colored.ALL.get(colorIndex);
                int count = Math.min(
                        row.capacity(),
                        Math.min(toConsume, remainingByColor[colorIndex])
                );

                if (count > 0) {
                    patterns = PkPatterns.withAddedTiles(patterns, row, count, color);
                    remainingByColor[colorIndex] -= count;
                    toConsume -= count;
                }
            }
            PkPlayerStates.setPkPatterns(pkPlayerStates, playerId, patterns);
        }

        for (PlayerId playerId : GAME_2P.playerIds()) {
            int floor = PkFloor.EMPTY;
            while (toConsume > 0 && PkFloor.size(floor) < TileDestination.FLOOR.capacity()) {
                int colorIndex = firstAvailableColorIndex(remainingByColor);
                if (colorIndex < 0) break;

                TileKind.Colored color = TileKind.Colored.ALL.get(colorIndex);
                floor = PkFloor.withAddedTiles(floor, PkTileSet.of(1, color));
                remainingByColor[colorIndex] -= 1;
                toConsume -= 1;
            }
            PkPlayerStates.setPkFloor(pkPlayerStates, playerId, floor);
        }

        assertEquals(0, toConsume);
        return pkPlayerStates;
    }

    private static int countFullRows(int pkWall) {
        int count = 0;
        for (TileDestination.Pattern row : TileDestination.Pattern.ALL) {
            if (PkWall.isRowFull(pkWall, row)) count += 1;
        }
        return count;
    }

    private static int countFullColumns(int pkWall) {
        int count = 0;
        for (int column = 0; column < PkWall.WALL_WIDTH; column++) {
            if (PkWall.isColumnFull(pkWall, column)) count += 1;
        }
        return count;
    }

    private static int countFullColors(int pkWall) {
        int count = 0;
        for (TileKind.Colored color : TileKind.Colored.ALL) {
            if (PkWall.isColorFull(pkWall, color)) count += 1;
        }
        return count;
    }

    private static int expectedEndGameBonus(int pkWall) {
        return countFullRows(pkWall) * Points.FULL_ROW_BONUS_POINTS
                + countFullColumns(pkWall) * Points.FULL_COLUMN_BONUS_POINTS
                + countFullColors(pkWall) * Points.FULL_COLOR_BONUS_POINTS;
    }

    private static final class ExpectedMoveResult {
        final int[] pkTileSources;
        final int pkUniqueTileSources;
        final int[] pkPlayerStates;
        final PlayerId currentPlayerId;

        ExpectedMoveResult(int[] pkTileSources,
                           int pkUniqueTileSources,
                           int[] pkPlayerStates,
                           PlayerId currentPlayerId) {
            this.pkTileSources = pkTileSources;
            this.pkUniqueTileSources = pkUniqueTileSources;
            this.pkPlayerStates = pkPlayerStates;
            this.currentPlayerId = currentPlayerId;
        }
    }

    private static final class ExpectedRoundResult {
        final int[] pkTileSources;
        final int[] pkPlayerStates;
        final PlayerId currentPlayerId;
        final int newWallTileCalls;
        final int floorCalls;

        ExpectedRoundResult(int[] pkTileSources,
                            int[] pkPlayerStates,
                            PlayerId currentPlayerId,
                            int newWallTileCalls,
                            int floorCalls) {
            this.pkTileSources = pkTileSources;
            this.pkPlayerStates = pkPlayerStates;
            this.currentPlayerId = currentPlayerId;
            this.newWallTileCalls = newWallTileCalls;
            this.floorCalls = floorCalls;
        }
    }

    private static final class RandomRoundState {
        final int[] pkTileSources;
        final int[] pkPlayerStates;
        final PlayerId currentPlayerId;

        RandomRoundState(int[] pkTileSources, int[] pkPlayerStates, PlayerId currentPlayerId) {
            this.pkTileSources = pkTileSources;
            this.pkPlayerStates = pkPlayerStates;
            this.currentPlayerId = currentPlayerId;
        }
    }

    private static ExpectedMoveResult simulateRegisterMove2Spec(
            int[] initialPkTileSources,
            int[] initialPkPlayerStates,
            PlayerId initialCurrentPlayerId,
            short pkMove
    ) {
        int[] pkTileSources = initialPkTileSources.clone();
        int[] pkPlayerStates = initialPkPlayerStates.clone();
        PlayerId currentPlayerId = initialCurrentPlayerId;

        TileSource source = PkMove.source(pkMove);
        TileKind.Colored color = PkMove.color(pkMove);
        TileDestination destination = PkMove.destination(pkMove);

        int pkSource = pkTileSources[source.index()];

        if (source == TileSource.CENTER_AREA
                && PkTileSet.countOf(pkSource, TileKind.FIRST_PLAYER_MARKER) == 1) {
            int floor = floorOf(pkPlayerStates, currentPlayerId);
            floor = PkFloor.withAddedTiles(floor, PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER));
            PkPlayerStates.setPkFloor(pkPlayerStates, currentPlayerId, floor);
            pkSource = PkTileSet.remove(pkSource, TileKind.FIRST_PLAYER_MARKER);
        }

        int pickedTiles = PkTileSet.subsetOf(pkSource, color);
        int pickedCount = PkTileSet.countOf(pkSource, color);

        if (source == TileSource.CENTER_AREA) {
            pkTileSources[source.index()] = PkTileSet.difference(pkSource, pickedTiles);
        } else {
            int remainingTiles = PkTileSet.difference(pkSource, pickedTiles);
            pkTileSources[source.index()] = PkTileSet.EMPTY;
            pkTileSources[TileSource.CENTER_AREA.index()] =
                    PkTileSet.union(
                            pkTileSources[TileSource.CENTER_AREA.index()],
                            remainingTiles
                    );
        }

        if (destination instanceof TileDestination.Pattern line) {
            int patterns = patternsOf(pkPlayerStates, currentPlayerId);
            int currentSize = PkPatterns.size(patterns, line);
            int accepted = Math.min(pickedCount, line.capacity() - currentSize);
            int overflow = pickedCount - accepted;

            if (accepted > 0) {
                patterns = PkPatterns.withAddedTiles(patterns, line, accepted, color);
                PkPlayerStates.setPkPatterns(pkPlayerStates, currentPlayerId, patterns);
            }

            if (overflow > 0) {
                int floor = floorOf(pkPlayerStates, currentPlayerId);
                floor = PkFloor.withAddedTiles(floor, PkTileSet.of(overflow, color));
                PkPlayerStates.setPkFloor(pkPlayerStates, currentPlayerId, floor);
            }
        } else {
            int floor = floorOf(pkPlayerStates, currentPlayerId);
            floor = PkFloor.withAddedTiles(floor, pickedTiles);
            PkPlayerStates.setPkFloor(pkPlayerStates, currentPlayerId, floor);
        }

        int pkUniqueTileSources = expectedUniqueTileSources(pkTileSources);
        currentPlayerId = nextPlayer(currentPlayerId);

        return new ExpectedMoveResult(
                pkTileSources,
                pkUniqueTileSources,
                pkPlayerStates,
                currentPlayerId
        );
    }

    private static ExpectedRoundResult simulateEndRoundSpec(
            int[] initialPkTileSources,
            int[] initialPkPlayerStates,
            PlayerId initialCurrentPlayerId
    ) {
        int[] pkTileSources = initialPkTileSources.clone();
        int[] pkPlayerStates = initialPkPlayerStates.clone();
        PlayerId currentPlayerId = initialCurrentPlayerId;

        int newWallTileCalls = 0;
        int floorCalls = 0;

        for (PlayerId playerId : GAME_2P.playerIds()) {
            int playerPatterns = patternsOf(pkPlayerStates, playerId);
            int playerWall = wallOf(pkPlayerStates, playerId);
            int playerPoints = pointsOf(pkPlayerStates, playerId);

            for (TileDestination.Pattern pattern : TileDestination.Pattern.ALL) {
                if (PkPatterns.isFull(playerPatterns, pattern)) {
                    TileKind.Colored patternColor = PkPatterns.color(playerPatterns, pattern);
                    playerWall = PkWall.withTileAt(playerWall, pattern, patternColor);

                    int hGroupSize = PkWall.hGroupSize(playerWall, pattern, patternColor);
                    int vGroupSize = PkWall.vGroupSize(playerWall, pattern, patternColor);
                    int points = Points.newWallTilePoints(hGroupSize, vGroupSize);

                    playerPoints += points;
                    playerPatterns = PkPatterns.withEmptyLine(playerPatterns, pattern);
                    newWallTileCalls += 1;
                }
            }

            int playerFloor = floorOf(pkPlayerStates, playerId);
            if (playerFloor != PkFloor.EMPTY) {
                int penalty = Math.min(playerPoints, Points.totalFloorPenalty(playerFloor));
                if (penalty > 0) {
                    playerPoints -= penalty;
                    floorCalls += 1;
                }

                if (PkFloor.containsFirstPlayerMarker(playerFloor)) {
                    currentPlayerId = playerId;
                    pkTileSources[TileSource.CENTER_AREA.index()] =
                            PkTileSet.union(
                                    pkTileSources[TileSource.CENTER_AREA.index()],
                                    PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER)
                            );
                }

                PkPlayerStates.setPkFloor(pkPlayerStates, playerId, PkFloor.EMPTY);
            }

            PkPlayerStates.setPkPatterns(pkPlayerStates, playerId, playerPatterns);
            PkPlayerStates.setPkWall(pkPlayerStates, playerId, playerWall);
            PkPlayerStates.addPoints(pkPlayerStates, playerId, playerPoints - pointsOf(pkPlayerStates, playerId));
        }

        return new ExpectedRoundResult(
                pkTileSources,
                pkPlayerStates,
                currentPlayerId,
                newWallTileCalls,
                floorCalls
        );
    }

    private static RandomRoundState randomRoundState(Random rng) {
        int[] pkTileSources = emptyTileSourcesWithoutMarker();
        int[] pkPlayerStates = initialPlayerStates();
        PlayerId currentPlayerId = GAME_2P.playerIds().get(rng.nextInt(GAME_2P.playersCount()));

        for (PlayerId playerId : GAME_2P.playerIds()) {
            int wall = PkWall.EMPTY;
            for (TileDestination.Pattern row : TileDestination.Pattern.ALL) {
                for (TileKind.Colored color : TileKind.Colored.ALL) {
                    if (rng.nextBoolean()) {
                        wall = PkWall.withTileAt(wall, row, color);
                    }
                }
            }
            PkPlayerStates.setPkWall(pkPlayerStates, playerId, wall);
        }

        for (PlayerId playerId : GAME_2P.playerIds()) {
            int wall = wallOf(pkPlayerStates, playerId);
            int patterns = PkPatterns.EMPTY;

            for (TileDestination.Pattern row : TileDestination.Pattern.ALL) {
                TileKind.Colored allowedColor = randomAllowedColorForRow(rng, wall, row);
                if (allowedColor == null) continue;

                int size = rng.nextInt(row.capacity() + 1);
                if (size > 0) {
                    patterns = PkPatterns.withAddedTiles(patterns, row, size, allowedColor);
                }
            }
            PkPlayerStates.setPkPatterns(pkPlayerStates, playerId, patterns);
        }

        for (PlayerId playerId : GAME_2P.playerIds()) {
            PkPlayerStates.addPoints(pkPlayerStates, playerId, rng.nextInt(21));
        }

        int markerLocation = rng.nextInt(3);
        if (markerLocation == 0) {
            pkTileSources[TileSource.CENTER_AREA.index()] =
                    PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);
        }

        for (PlayerId playerId : GAME_2P.playerIds()) {
            boolean playerGetsMarker =
                    (markerLocation == 1 && playerId == PlayerId.P1)
                            || (markerLocation == 2 && playerId == PlayerId.P2);

            int coloredFloorTiles = rng.nextInt(
                    TileDestination.FLOOR.capacity() + (playerGetsMarker ? 0 : 1)
            );
            int pkFloorTiles = randomColoredTileSet(rng, coloredFloorTiles);

            if (playerGetsMarker) {
                pkFloorTiles = PkTileSet.union(
                        pkFloorTiles,
                        PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER)
                );
            }

            int pkFloor = PkFloor.withAddedTiles(PkFloor.EMPTY, pkFloorTiles);
            PkPlayerStates.setPkFloor(pkPlayerStates, playerId, pkFloor);
        }

        return new RandomRoundState(pkTileSources, pkPlayerStates, currentPlayerId);
    }

    // =========================================================
    // Tests de fillFactories
    // =========================================================

    @Test
    void fillFactoriesOnInitialStateFillsAllFactoriesAndKeepsMarkerInCenter() {
        MutableGameState state = new MutableGameState(ImmutableGameState.initial(GAME_2P));
        RandomGenerator random = new Random(2026);

        state.fillFactories(random);

        assertEquals(PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER),
                state.pkTileSources().get(TileSource.CENTER_AREA.index()));

        for (TileSource.Factory factory : GAME_2P.factories()) {
            assertEquals(TileSource.Factory.TILES_PER_FACTORY,
                    PkTileSet.size(state.pkTileSources().get(factory.index())));
        }

        assertEquals(80, coloredCount(state.pkTileBag()));
        assertEquals(100, totalColoredTilesInState(state));
        assertFalse(state.isRoundOver());

        assertEquals(expectedUniqueTileSources(state.pkTileSources().toArray()),
                state.pkUniqueTileSources());
    }

    @Test
    void fillFactoriesUsesDiscardWhenBagIsTooSmall() {
        int[] pkTileSources = emptyTileSourcesWithMarkerInCenter();
        int[] pkPlayerStates = initialPlayerStates();

        MutableGameState state = newState(
                PkTileSet.of(1, TileKind.Colored.A),
                pkTileSources,
                PkIntSet32.EMPTY,
                pkPlayerStates,
                PlayerId.P1,
                PointsObserver.EMPTY
        );

        state.fillFactories(new Random(1));

        for (TileSource.Factory factory : GAME_2P.factories()) {
            assertEquals(TileSource.Factory.TILES_PER_FACTORY,
                    PkTileSet.size(state.pkTileSources().get(factory.index())));
        }

        assertEquals(PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER),
                state.pkTileSources().get(TileSource.CENTER_AREA.index()));

        assertEquals(80, coloredCount(state.pkTileBag()));
        assertEquals(100, totalColoredTilesInState(state));
        assertEquals(expectedUniqueTileSources(state.pkTileSources().toArray()),
                state.pkUniqueTileSources());
    }

    @Test
    void fillFactoriesCanLeaveLastFactoriesEmptyWhenNotEnoughTilesExistOverall() {
        int[] pkTileSources = emptyTileSourcesWithMarkerInCenter();
        int[] pkPlayerStates = initialPlayerStates();

        int p1Wall = PkWall.EMPTY;
        int p2Wall = PkWall.EMPTY;

        // 25 tuiles sur le mur de P1
        for (TileDestination.Pattern row : TileDestination.Pattern.ALL) {
            for (TileKind.Colored color : TileKind.Colored.ALL) {
                p1Wall = PkWall.withTileAt(p1Wall, row, color);
            }
        }

        // 25 tuiles sur le mur de P2
        for (TileDestination.Pattern row : TileDestination.Pattern.ALL) {
            for (TileKind.Colored color : TileKind.Colored.ALL) {
                p2Wall = PkWall.withTileAt(p2Wall, row, color);
            }
        }

        int p1Patterns = PkPatterns.EMPTY;
        p1Patterns = PkPatterns.withAddedTiles(p1Patterns, TileDestination.Pattern.PATTERN_1, 1, TileKind.Colored.A);
        p1Patterns = PkPatterns.withAddedTiles(p1Patterns, TileDestination.Pattern.PATTERN_2, 2, TileKind.Colored.B);
        p1Patterns = PkPatterns.withAddedTiles(p1Patterns, TileDestination.Pattern.PATTERN_3, 3, TileKind.Colored.C);
        p1Patterns = PkPatterns.withAddedTiles(p1Patterns, TileDestination.Pattern.PATTERN_4, 4, TileKind.Colored.D);
        p1Patterns = PkPatterns.withAddedTiles(p1Patterns, TileDestination.Pattern.PATTERN_5, 5, TileKind.Colored.E);

        int p2Patterns = PkPatterns.EMPTY;
        p2Patterns = PkPatterns.withAddedTiles(p2Patterns, TileDestination.Pattern.PATTERN_1, 1, TileKind.Colored.B);
        p2Patterns = PkPatterns.withAddedTiles(p2Patterns, TileDestination.Pattern.PATTERN_2, 2, TileKind.Colored.C);
        p2Patterns = PkPatterns.withAddedTiles(p2Patterns, TileDestination.Pattern.PATTERN_3, 3, TileKind.Colored.D);
        p2Patterns = PkPatterns.withAddedTiles(p2Patterns, TileDestination.Pattern.PATTERN_4, 4, TileKind.Colored.E);
        p2Patterns = PkPatterns.withAddedTiles(p2Patterns, TileDestination.Pattern.PATTERN_5, 5, TileKind.Colored.A);

        PkPlayerStates.setPkWall(pkPlayerStates, PlayerId.P1, p1Wall);
        PkPlayerStates.setPkWall(pkPlayerStates, PlayerId.P2, p2Wall);
        PkPlayerStates.setPkPatterns(pkPlayerStates, PlayerId.P1, p1Patterns);
        PkPlayerStates.setPkPatterns(pkPlayerStates, PlayerId.P2, p2Patterns);

        MutableGameState state = newState(
                PkTileSet.EMPTY,
                pkTileSources,
                PkIntSet32.EMPTY,
                pkPlayerStates,
                PlayerId.P1,
                PointsObserver.EMPTY
        );

        state.fillFactories(new Random(3));

        int totalFactoryTiles = 0;
        for (TileSource.Factory factory : GAME_2P.factories()) {
            totalFactoryTiles += PkTileSet.size(state.pkTileSources().get(factory.index()));
        }

        assertEquals(20, totalFactoryTiles,
                "Il ne reste au total que 20 tuiles colorées hors murs/patterns, donc 20 doivent être distribuées");
        assertEquals(0, coloredCount(state.pkTileBag()));
        assertEquals(100, totalColoredTilesInState(state));
    }

    // =========================================================
    // Tests de registerMove2
    // =========================================================

    @Test
    void registerMove2FromFactoryMovesChosenTilesToPatternAndRemainingTilesToCenterWorks() {
        int[] pkTileSources = emptyTileSourcesWithMarkerInCenter();
        int[] pkPlayerStates = initialPlayerStates();

        pkTileSources[TileSource.FACTORY_1.index()] =
                PkTileSet.union(
                        PkTileSet.of(2, TileKind.Colored.A),
                        PkTileSet.union(
                                PkTileSet.of(1, TileKind.Colored.B),
                                PkTileSet.of(1, TileKind.Colored.D)
                        )
                );

        MutableGameState state = newState(
                PkTileSet.EMPTY,
                pkTileSources,
                expectedUniqueTileSources(pkTileSources),
                pkPlayerStates,
                PlayerId.P1,
                PointsObserver.EMPTY
        );

        Move move = new Move(
                TileSource.FACTORY_1,
                TileKind.Colored.A,
                TileDestination.Pattern.PATTERN_2
        );

        state.registerMove(move.packed());

        int p1Patterns = PkPlayerStates.pkPatterns(state.pkPlayerStates(), PlayerId.P1);
        assertEquals(2, PkPatterns.size(p1Patterns, TileDestination.Pattern.PATTERN_2));
        assertEquals(TileKind.Colored.A, PkPatterns.color(p1Patterns, TileDestination.Pattern.PATTERN_2));

        assertEquals(PkTileSet.EMPTY,
                state.pkTileSources().get(TileSource.FACTORY_1.index()));

        int pkCenter = state.pkTileSources().get(TileSource.CENTER_AREA.index());
        assertEquals(1, PkTileSet.countOf(pkCenter, TileKind.Colored.B));
        assertEquals(1, PkTileSet.countOf(pkCenter, TileKind.Colored.D));
        assertEquals(1, PkTileSet.countOf(pkCenter, TileKind.FIRST_PLAYER_MARKER));
        assertEquals(0, PkTileSet.countOf(pkCenter, TileKind.Colored.A));

        assertEquals(PlayerId.P2, state.currentPlayerId());
        assertEquals(expectedUniqueTileSources(state.pkTileSources().toArray()),
                state.pkUniqueTileSources());
    }

    @Test
    void registerMove2FromCenterMovesMarkerToFloorAndOverflowToFloorWorks() {
        int[] pkTileSources = emptyTileSourcesWithoutMarker();
        int[] pkPlayerStates = initialPlayerStates();

        pkTileSources[TileSource.CENTER_AREA.index()] =
                PkTileSet.union(
                        PkTileSet.of(2, TileKind.Colored.C),
                        PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER)
                );

        MutableGameState state = newState(
                PkTileSet.EMPTY,
                pkTileSources,
                expectedUniqueTileSources(pkTileSources),
                pkPlayerStates,
                PlayerId.P1,
                PointsObserver.EMPTY
        );

        Move move = new Move(
                TileSource.CENTER_AREA,
                TileKind.Colored.C,
                TileDestination.Pattern.PATTERN_1
        );

        state.registerMove(move.packed());

        int p1Patterns = PkPlayerStates.pkPatterns(state.pkPlayerStates(), PlayerId.P1);
        assertEquals(1, PkPatterns.size(p1Patterns, TileDestination.Pattern.PATTERN_1));
        assertEquals(TileKind.Colored.C, PkPatterns.color(p1Patterns, TileDestination.Pattern.PATTERN_1));

        int p1Floor = PkPlayerStates.pkFloor(state.pkPlayerStates(), PlayerId.P1);
        int p1FloorTiles = PkFloor.asPkTileSet(p1Floor);

        assertEquals(1, PkTileSet.countOf(p1FloorTiles, TileKind.Colored.C));
        assertEquals(1, PkTileSet.countOf(p1FloorTiles, TileKind.FIRST_PLAYER_MARKER));

        assertEquals(PkTileSet.EMPTY,
                state.pkTileSources().get(TileSource.CENTER_AREA.index()));

        assertEquals(PlayerId.P2, state.currentPlayerId());
        assertEquals(expectedUniqueTileSources(state.pkTileSources().toArray()),
                state.pkUniqueTileSources());
    }

    @Test
    void registerMove2ToFloorPutsAllChosenTilesOnFloorWorks() {
        int[] pkTileSources = emptyTileSourcesWithMarkerInCenter();
        int[] pkPlayerStates = initialPlayerStates();

        pkTileSources[TileSource.FACTORY_2.index()] =
                PkTileSet.union(
                        PkTileSet.of(3, TileKind.Colored.E),
                        PkTileSet.of(1, TileKind.Colored.A)
                );

        MutableGameState state = newState(
                PkTileSet.EMPTY,
                pkTileSources,
                expectedUniqueTileSources(pkTileSources),
                pkPlayerStates,
                PlayerId.P1,
                PointsObserver.EMPTY
        );

        Move move = new Move(
                TileSource.FACTORY_2,
                TileKind.Colored.E,
                TileDestination.FLOOR
        );

        state.registerMove(move.packed());

        int p1Floor = PkPlayerStates.pkFloor(state.pkPlayerStates(), PlayerId.P1);
        int p1FloorTiles = PkFloor.asPkTileSet(p1Floor);

        assertEquals(3, PkTileSet.countOf(p1FloorTiles, TileKind.Colored.E));
        assertEquals(1, PkTileSet.countOf(
                state.pkTileSources().get(TileSource.CENTER_AREA.index()),
                TileKind.Colored.A
        ));

        assertEquals(PlayerId.P2, state.currentPlayerId());
    }

    @Test
    void registerMove2BruteForceOnManyRandomLegalStatesMatchesSpecificationWorks() {
        Random rng = new Random(20260327L);

        for (int fillSeed = 0; fillSeed < 250; fillSeed++) {
            MutableGameState start = new MutableGameState(ImmutableGameState.initial(GAME_2P));
            start.fillFactories(new Random(fillSeed));

            int pkTileBag = start.pkTileBag();
            int[] pkTileSources = start.pkTileSources().toArray();
            int pkUniqueTileSources = start.pkUniqueTileSources();
            int[] pkPlayerStates = start.pkPlayerStates().toArray();
            PlayerId currentPlayerId = start.currentPlayerId();

            for (int step = 0; step < 20; step++) {
                MutableGameState queryState = newState(
                        pkTileBag,
                        pkTileSources,
                        pkUniqueTileSources,
                        pkPlayerStates,
                        currentPlayerId,
                        PointsObserver.EMPTY
                );

                short[] validMoves = new short[Move.MAX_MOVES];
                int moveCount = queryState.validMoves(validMoves);

                if (moveCount == 0 || queryState.isRoundOver()) {
                    break;
                }

                short pkMove = validMoves[rng.nextInt(moveCount)];

                ExpectedMoveResult expected = simulateRegisterMove2Spec(
                        pkTileSources,
                        pkPlayerStates,
                        currentPlayerId,
                        pkMove
                );

                MutableGameState actual = newState(
                        pkTileBag,
                        pkTileSources,
                        pkUniqueTileSources,
                        pkPlayerStates,
                        currentPlayerId,
                        PointsObserver.EMPTY
                );

                actual.registerMove(pkMove);

                assertArrayEquals(expected.pkTileSources,
                        actual.pkTileSources().toArray(),
                        "Sources différentes pour fillSeed=" + fillSeed + ", step=" + step);

                assertEquals(expected.pkUniqueTileSources,
                        actual.pkUniqueTileSources(),
                        "pkUniqueTileSources différent pour fillSeed=" + fillSeed + ", step=" + step);

//                assertArrayEquals(expected.pkPlayerStates,
//                        actual.pkPlayerStates().toArray(),
//                        "États joueurs différents pour fillSeed=" + fillSeed + ", step=" + step);

                assertEquals(expected.currentPlayerId,
                        actual.currentPlayerId(),
                        "Joueur courant différent pour fillSeed=" + fillSeed + ", step=" + step);

                assertEquals(pkTileBag,
                        actual.pkTileBag(),
                        "registerMove2 ne doit jamais modifier le sac");

                pkTileSources = expected.pkTileSources;
                pkUniqueTileSources = expected.pkUniqueTileSources;
                pkPlayerStates = expected.pkPlayerStates;
                currentPlayerId = expected.currentPlayerId;
            }
        }
    }
    @Test
    void endGameBruteForceOnManyRandomWallsMatchesBonusSpecificationWorks() {
        Random rng = new Random(20260327L + 2);

        for (int i = 0; i < 20000; i++) {
            int[] pkTileSources = emptyTileSourcesWithoutMarker();
            int[] pkPlayerStates = initialPlayerStates();

            int p1Wall = rng.nextInt(1 << 25);
            int p2Wall = rng.nextInt(1 << 25);

            int p1InitialPoints = rng.nextInt(51);
            int p2InitialPoints = rng.nextInt(51);

            PkPlayerStates.setPkWall(pkPlayerStates, PlayerId.P1, p1Wall);
            PkPlayerStates.setPkWall(pkPlayerStates, PlayerId.P2, p2Wall);
            PkPlayerStates.addPoints(pkPlayerStates, PlayerId.P1, p1InitialPoints);
            PkPlayerStates.addPoints(pkPlayerStates, PlayerId.P2, p2InitialPoints);

            RecordingPointsObserver observer = new RecordingPointsObserver();
            MutableGameState state = newState(
                    pkTileSources,
                    pkPlayerStates,
                    PlayerId.P1,
                    observer
            );

            int expectedP1Bonus = expectedEndGameBonus(p1Wall);
            int expectedP2Bonus = expectedEndGameBonus(p2Wall);

            int expectedRowCalls = countFullRows(p1Wall) + countFullRows(p2Wall);
            int expectedColumnCalls = countFullColumns(p1Wall) + countFullColumns(p2Wall);
            int expectedColorCalls = countFullColors(p1Wall) + countFullColors(p2Wall);

            state.endGame();

            assertEquals(
                    p1InitialPoints + expectedP1Bonus,
                    PkPlayerStates.points(state.pkPlayerStates(), PlayerId.P1),
                    "Mauvais score final P1 pour l’itération " + i
            );

            assertEquals(
                    p2InitialPoints + expectedP2Bonus,
                    PkPlayerStates.points(state.pkPlayerStates(), PlayerId.P2),
                    "Mauvais score final P2 pour l’itération " + i
            );

            assertEquals(expectedRowCalls, observer.fullRowCallCount,
                    "Mauvais nombre d’appels fullRow pour l’itération " + i);
            assertEquals(expectedColumnCalls, observer.fullColumnCallCount,
                    "Mauvais nombre d’appels fullColumn pour l’itération " + i);
            assertEquals(expectedColorCalls, observer.fullColorCallCount,
                    "Mauvais nombre d’appels fullColor pour l’itération " + i);
        }
    }

    // =========================================================
    // Tests de endRound
    // =========================================================

    @Test
    void endRoundOnInitialTwoPlayerStateWorks() {
        int[] pkTileSources = emptyTileSourcesWithMarkerInCenter();
        int[] pkPlayerStates = initialPlayerStates();

        MutableGameState state = newState(
                pkTileSources,
                pkPlayerStates,
                PlayerId.P2,
                PointsObserver.EMPTY
        );

        assertDoesNotThrow(state::endRound);

        assertEquals(PlayerId.P2, state.currentPlayerId());
        assertEquals(PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER),
                state.pkTileSources().get(TileSource.CENTER_AREA.index()));

        for (PlayerId playerId : GAME_2P.playerIds()) {
            assertEquals(PkPatterns.EMPTY,
                    PkPlayerStates.pkPatterns(state.pkPlayerStates(), playerId));
            assertEquals(PkFloor.EMPTY,
                    PkPlayerStates.pkFloor(state.pkPlayerStates(), playerId));
            assertEquals(PkWall.EMPTY,
                    PkPlayerStates.pkWall(state.pkPlayerStates(), playerId));
            assertEquals(0,
                    PkPlayerStates.points(state.pkPlayerStates(), playerId));
        }
    }

    @Test
    void endRoundMovesFullPatternLineToWallKeepsIncompleteOnesAndAwardsPointsWorks() {
        int[] pkTileSources = emptyTileSourcesWithMarkerInCenter();
        int[] pkPlayerStates = initialPlayerStates();

        int pkPatterns = PkPatterns.EMPTY;
        pkPatterns = PkPatterns.withAddedTiles(
                pkPatterns,
                TileDestination.Pattern.PATTERN_1,
                1,
                TileKind.Colored.A
        );
        pkPatterns = PkPatterns.withAddedTiles(
                pkPatterns,
                TileDestination.Pattern.PATTERN_2,
                1,
                TileKind.Colored.B
        );
        PkPlayerStates.setPkPatterns(pkPlayerStates, PlayerId.P1, pkPatterns);

        RecordingPointsObserver observer = new RecordingPointsObserver();
        MutableGameState state = newState(
                pkTileSources,
                pkPlayerStates,
                PlayerId.P1,
                observer
        );

        state.endRound();

        int actualWall = PkPlayerStates.pkWall(state.pkPlayerStates(), PlayerId.P1);
        assertTrue(PkWall.hasTileAt(actualWall,
                TileDestination.Pattern.PATTERN_1,
                TileKind.Colored.A));

        int actualPatterns = PkPlayerStates.pkPatterns(state.pkPlayerStates(), PlayerId.P1);
        assertEquals(0, PkPatterns.size(actualPatterns, TileDestination.Pattern.PATTERN_1));
        assertEquals(1, PkPatterns.size(actualPatterns, TileDestination.Pattern.PATTERN_2));
        assertEquals(TileKind.Colored.B,
                PkPatterns.color(actualPatterns, TileDestination.Pattern.PATTERN_2));

        assertEquals(1, PkPlayerStates.points(state.pkPlayerStates(), PlayerId.P1));

        assertEquals(1, observer.newWallTileCallCount);
        assertEquals(PlayerId.P1, observer.newWallTilePlayerId);
        assertEquals(TileDestination.Pattern.PATTERN_1, observer.newWallTileLine);
        assertEquals(TileKind.Colored.A, observer.newWallTileColor);
        assertEquals(1, observer.newWallTilePoints);
    }

    @Test
    void endRoundAppliesFloorPenaltyWithoutGoingNegativeAndCallsObserverWorks() {
        int[] pkTileSources = emptyTileSourcesWithMarkerInCenter();
        int[] pkPlayerStates = initialPlayerStates();

        int pkFloor = PkFloor.withAddedTiles(
                PkFloor.EMPTY,
                PkTileSet.of(2, TileKind.B)
        );
        PkPlayerStates.setPkFloor(pkPlayerStates, PlayerId.P1, pkFloor);
        PkPlayerStates.addPoints(pkPlayerStates, PlayerId.P1, 1);

        RecordingPointsObserver observer = new RecordingPointsObserver();
        MutableGameState state = newState(
                pkTileSources,
                pkPlayerStates,
                PlayerId.P1,
                observer
        );

        state.endRound();

        assertEquals(0, PkPlayerStates.points(state.pkPlayerStates(), PlayerId.P1));
        assertEquals(PkFloor.EMPTY,
                PkPlayerStates.pkFloor(state.pkPlayerStates(), PlayerId.P1));

        assertEquals(1, observer.floorCallCount);
        assertEquals(PlayerId.P1, observer.floorPlayerId);
        assertEquals(1, observer.floorPenalty);
    }

    @Test
    void endRoundReturnsFirstPlayerMarkerToCenterAndChangesCurrentPlayerWorks() {
        int[] pkTileSources = emptyTileSourcesWithoutMarker();
        int[] pkPlayerStates = initialPlayerStates();

        int pkFloor = PkFloor.withAddedTiles(
                PkFloor.EMPTY,
                PkTileSet.union(
                        PkTileSet.of(1, TileKind.Colored.C),
                        PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER)
                )
        );
        PkPlayerStates.setPkFloor(pkPlayerStates, PlayerId.P2, pkFloor);

        MutableGameState state = newState(
                pkTileSources,
                pkPlayerStates,
                PlayerId.P1,
                PointsObserver.EMPTY
        );

        state.endRound();

        assertEquals(PlayerId.P2, state.currentPlayerId());
        assertEquals(PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER),
                state.pkTileSources().get(TileSource.CENTER_AREA.index()));
        assertEquals(PkFloor.EMPTY,
                PkPlayerStates.pkFloor(state.pkPlayerStates(), PlayerId.P2));
    }

    @Test
    void endRoundLeavesCurrentPlayerUnchangedWhenMarkerIsAlreadyInCenterWorks() {
        int[] pkTileSources = emptyTileSourcesWithMarkerInCenter();
        int[] pkPlayerStates = initialPlayerStates();

        MutableGameState state = newState(
                pkTileSources,
                pkPlayerStates,
                PlayerId.P2,
                PointsObserver.EMPTY
        );

        state.endRound();

        assertEquals(PlayerId.P2, state.currentPlayerId());
        assertEquals(PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER),
                state.pkTileSources().get(TileSource.CENTER_AREA.index()));
    }

    // =========================================================
    // Tests de endGame
    // =========================================================

    @Test
    void endGameAddsFullRowBonusAndCallsObserverWorks() {
        int[] pkTileSources = emptyTileSourcesWithoutMarker();
        int[] pkPlayerStates = initialPlayerStates();

        PkPlayerStates.setPkWall(pkPlayerStates, PlayerId.P1,
                fullRow(TileDestination.Pattern.PATTERN_1));
        PkPlayerStates.addPoints(pkPlayerStates, PlayerId.P1, 5);

        RecordingPointsObserver observer = new RecordingPointsObserver();
        MutableGameState state = newState(
                pkTileSources,
                pkPlayerStates,
                PlayerId.P1,
                observer
        );

        state.endGame();

        assertEquals(5 + Points.FULL_ROW_BONUS_POINTS,
                PkPlayerStates.points(state.pkPlayerStates(), PlayerId.P1));
        assertEquals(1, observer.fullRowCallCount);
        assertEquals(PlayerId.P1, observer.fullRowPlayerId);
        assertEquals(TileDestination.Pattern.PATTERN_1, observer.fullRowLine);
        assertEquals(Points.FULL_ROW_BONUS_POINTS, observer.fullRowPoints);
    }

    @Test
    void endGameAddsFullColumnBonusAndCallsObserverWorks() {
        int[] pkTileSources = emptyTileSourcesWithoutMarker();
        int[] pkPlayerStates = initialPlayerStates();

        PkPlayerStates.setPkWall(pkPlayerStates, PlayerId.P1, fullColumn(0));

        RecordingPointsObserver observer = new RecordingPointsObserver();
        MutableGameState state = newState(
                pkTileSources,
                pkPlayerStates,
                PlayerId.P1,
                observer
        );

        state.endGame();

        assertEquals(Points.FULL_COLUMN_BONUS_POINTS,
                PkPlayerStates.points(state.pkPlayerStates(), PlayerId.P1));
        assertEquals(1, observer.fullColumnCallCount);
        assertEquals(PlayerId.P1, observer.fullColumnPlayerId);
        assertEquals(0, observer.fullColumnIndex);
        assertEquals(Points.FULL_COLUMN_BONUS_POINTS, observer.fullColumnPoints);
    }

    @Test
    void endGameAddsFullColorBonusAndCallsObserverWorks() {
        int[] pkTileSources = emptyTileSourcesWithoutMarker();
        int[] pkPlayerStates = initialPlayerStates();

        PkPlayerStates.setPkWall(pkPlayerStates, PlayerId.P1, fullColor(TileKind.Colored.A));

        RecordingPointsObserver observer = new RecordingPointsObserver();
        MutableGameState state = newState(
                pkTileSources,
                pkPlayerStates,
                PlayerId.P1,
                observer
        );

        state.endGame();

        assertEquals(Points.FULL_COLOR_BONUS_POINTS,
                PkPlayerStates.points(state.pkPlayerStates(), PlayerId.P1));
        assertEquals(1, observer.fullColorCallCount);
        assertEquals(PlayerId.P1, observer.fullColorPlayerId);
        assertEquals(TileKind.Colored.A, observer.fullColor);
        assertEquals(Points.FULL_COLOR_BONUS_POINTS, observer.fullColorPoints);
    }

    // =========================================================
    // Tests supplémentaires de registerMove
    // =========================================================

    @Test
    void registerMoveFromNonUniqueFactoryDoesNotCorruptUniqueSources() {
        // Fabriques 1, 2, 3 ont toutes le même contenu X = {2*A, 2*B}
        // Seule fabrique 1 est unique. On prend de fabrique 2 (non-unique).
        // Fabrique 3 ne doit PAS être promue en unique (fabrique 1 a encore le même contenu).
        int[] pkTileSources = emptyTileSourcesWithMarkerInCenter();
        int contentX = PkTileSet.union(
                PkTileSet.of(2, TileKind.Colored.A),
                PkTileSet.of(2, TileKind.Colored.B));
        pkTileSources[TileSource.FACTORY_1.index()] = contentX;
        pkTileSources[TileSource.FACTORY_2.index()] = contentX;
        pkTileSources[TileSource.FACTORY_3.index()] = contentX;

        int[] pkPlayerStates = initialPlayerStates();

        MutableGameState state = newState(
                PkTileSet.EMPTY,
                pkTileSources,
                expectedUniqueTileSources(pkTileSources),
                pkPlayerStates,
                PlayerId.P1,
                PointsObserver.EMPTY
        );

        // Vérifie que seule fabrique 1 est unique au départ
        assertTrue(PkIntSet32.contains(state.pkUniqueTileSources(), TileSource.FACTORY_1.index()));
        assertFalse(PkIntSet32.contains(state.pkUniqueTileSources(), TileSource.FACTORY_2.index()));
        assertFalse(PkIntSet32.contains(state.pkUniqueTileSources(), TileSource.FACTORY_3.index()));

        // Jouer depuis fabrique 2 (non-unique), prendre A vers pattern 2
        Move move = new Move(TileSource.FACTORY_2, TileKind.Colored.A, TileDestination.Pattern.PATTERN_2);
        state.registerMove(move.packed());

        // Fabrique 1 a toujours le contenu X, fabrique 3 aussi → 3 ne doit PAS être unique
        assertEquals(expectedUniqueTileSources(state.pkTileSources().toArray()),
                state.pkUniqueTileSources(),
                "Fabrique 3 ne devrait pas être unique car fabrique 1 a le même contenu");
    }

    @Test
    void registerMoveFromNonUniqueFactoryWhenOriginalIsGoneMakesNextUnique() {
        // Fabriques 1, 2 ont le même contenu. Fabrique 1 est unique.
        // On vide d'abord fabrique 1, puis on prend de fabrique 2 qui est maintenant unique.
        int[] pkTileSources = emptyTileSourcesWithMarkerInCenter();
        int contentX = PkTileSet.union(
                PkTileSet.of(2, TileKind.Colored.C),
                PkTileSet.of(2, TileKind.Colored.D));
        pkTileSources[TileSource.FACTORY_1.index()] = contentX;
        pkTileSources[TileSource.FACTORY_2.index()] = contentX;

        int[] pkPlayerStates = initialPlayerStates();
        MutableGameState state = newState(
                PkTileSet.EMPTY,
                pkTileSources,
                expectedUniqueTileSources(pkTileSources),
                pkPlayerStates,
                PlayerId.P1,
                PointsObserver.EMPTY
        );

        // P1 prend de fabrique 1 (unique) → fabrique 2 doit devenir unique
        Move move1 = new Move(TileSource.FACTORY_1, TileKind.Colored.C, TileDestination.Pattern.PATTERN_2);
        state.registerMove(move1.packed());

        assertTrue(PkIntSet32.contains(state.pkUniqueTileSources(), TileSource.FACTORY_2.index()),
                "Fabrique 2 devrait être unique après que fabrique 1 a été vidée");
        assertEquals(expectedUniqueTileSources(state.pkTileSources().toArray()),
                state.pkUniqueTileSources());
    }

    @Test
    void registerMoveFromCenterWithoutMarkerDoesNotAddMarkerToFloor() {
        // Le marqueur a déjà été pris lors d'un tour précédent.
        // Centre contient seulement des tuiles colorées.
        int[] pkTileSources = emptyTileSourcesWithoutMarker();
        pkTileSources[TileSource.CENTER_AREA.index()] =
                PkTileSet.of(3, TileKind.Colored.B);

        int[] pkPlayerStates = initialPlayerStates();
        // Le marqueur est déjà sur le plancher de P2
        int floorP2 = PkFloor.withAddedTiles(PkFloor.EMPTY,
                PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER));
        PkPlayerStates.setPkFloor(pkPlayerStates, PlayerId.P2, floorP2);

        MutableGameState state = newState(
                PkTileSet.EMPTY,
                pkTileSources,
                expectedUniqueTileSources(pkTileSources),
                pkPlayerStates,
                PlayerId.P1,
                PointsObserver.EMPTY
        );

        Move move = new Move(TileSource.CENTER_AREA, TileKind.Colored.B,
                TileDestination.Pattern.PATTERN_3);
        state.registerMove(move.packed());

        // P1 doit avoir 3 B sur pattern 3, pas de marqueur sur son plancher
        int p1Patterns = PkPlayerStates.pkPatterns(state.pkPlayerStates(), PlayerId.P1);
        assertEquals(3, PkPatterns.size(p1Patterns, TileDestination.Pattern.PATTERN_3));
        assertEquals(TileKind.Colored.B, PkPatterns.color(p1Patterns, TileDestination.Pattern.PATTERN_3));

        int p1Floor = PkPlayerStates.pkFloor(state.pkPlayerStates(), PlayerId.P1);
        assertFalse(PkFloor.containsFirstPlayerMarker(p1Floor),
                "Le marqueur ne devrait pas être sur le plancher de P1");

        // Centre doit être vide
        assertEquals(PkTileSet.EMPTY,
                state.pkTileSources().get(TileSource.CENTER_AREA.index()));
    }

    @Test
    void registerMoveOverflowTilesToFloorWhenPatternAlmostFull() {
        // Pattern 3 a déjà 2 tuiles A (capacité 3). On prend 3 A → 1 va au pattern, 2 au plancher.
        int[] pkTileSources = emptyTileSourcesWithMarkerInCenter();
        pkTileSources[TileSource.FACTORY_1.index()] =
                PkTileSet.union(
                        PkTileSet.of(3, TileKind.Colored.A),
                        PkTileSet.of(1, TileKind.Colored.E));

        int[] pkPlayerStates = initialPlayerStates();
        int patterns = PkPatterns.withAddedTiles(PkPatterns.EMPTY,
                TileDestination.Pattern.PATTERN_3, 2, TileKind.Colored.A);
        PkPlayerStates.setPkPatterns(pkPlayerStates, PlayerId.P1, patterns);

        MutableGameState state = newState(
                PkTileSet.EMPTY,
                pkTileSources,
                expectedUniqueTileSources(pkTileSources),
                pkPlayerStates,
                PlayerId.P1,
                PointsObserver.EMPTY
        );

        Move move = new Move(TileSource.FACTORY_1, TileKind.Colored.A,
                TileDestination.Pattern.PATTERN_3);
        state.registerMove(move.packed());

        int p1Patterns = PkPlayerStates.pkPatterns(state.pkPlayerStates(), PlayerId.P1);
        assertTrue(PkPatterns.isFull(p1Patterns, TileDestination.Pattern.PATTERN_3),
                "Pattern 3 devrait être plein (3/3)");

        int p1Floor = PkPlayerStates.pkFloor(state.pkPlayerStates(), PlayerId.P1);
        int floorTiles = PkFloor.asPkTileSet(p1Floor);
        assertEquals(2, PkTileSet.countOf(floorTiles, TileKind.Colored.A),
                "2 tuiles A excédentaires devraient être sur le plancher");
    }

    @Test
    void registerMoveFromCenterRemovesCenterFromUniqueWhenEmpty() {
        int[] pkTileSources = emptyTileSourcesWithoutMarker();
        pkTileSources[TileSource.CENTER_AREA.index()] =
                PkTileSet.of(2, TileKind.Colored.D);
        pkTileSources[TileSource.FACTORY_1.index()] =
                PkTileSet.of(4, TileKind.Colored.E);

        int[] pkPlayerStates = initialPlayerStates();
        MutableGameState state = newState(
                PkTileSet.EMPTY,
                pkTileSources,
                expectedUniqueTileSources(pkTileSources),
                pkPlayerStates,
                PlayerId.P1,
                PointsObserver.EMPTY
        );

        assertTrue(PkIntSet32.contains(state.pkUniqueTileSources(), TileSource.CENTER_AREA.index()));

        Move move = new Move(TileSource.CENTER_AREA, TileKind.Colored.D,
                TileDestination.Pattern.PATTERN_2);
        state.registerMove(move.packed());

        assertFalse(PkIntSet32.contains(state.pkUniqueTileSources(), TileSource.CENTER_AREA.index()),
                "Le centre devrait être retiré des sources uniques car il est maintenant vide");
        assertTrue(PkIntSet32.contains(state.pkUniqueTileSources(), TileSource.FACTORY_1.index()));
    }

    @Test
    void registerMoveFromFactoryAddsCenterToUniqueWhenTilesAdded() {
        int[] pkTileSources = emptyTileSourcesWithMarkerInCenter();
        pkTileSources[TileSource.FACTORY_1.index()] =
                PkTileSet.union(
                        PkTileSet.of(2, TileKind.Colored.A),
                        PkTileSet.of(2, TileKind.Colored.B));

        int[] pkPlayerStates = initialPlayerStates();
        MutableGameState state = newState(
                PkTileSet.EMPTY,
                pkTileSources,
                expectedUniqueTileSources(pkTileSources),
                pkPlayerStates,
                PlayerId.P1,
                PointsObserver.EMPTY
        );

        // Centre n'a que le marqueur → pas unique (pas de tuiles colorées)
        assertFalse(PkIntSet32.contains(state.pkUniqueTileSources(), TileSource.CENTER_AREA.index()));

        Move move = new Move(TileSource.FACTORY_1, TileKind.Colored.A,
                TileDestination.Pattern.PATTERN_2);
        state.registerMove(move.packed());

        // Les 2 B de la fabrique ont été déplacés au centre → centre devient unique
        assertTrue(PkIntSet32.contains(state.pkUniqueTileSources(), TileSource.CENTER_AREA.index()),
                "Le centre devrait être unique car il contient maintenant des tuiles colorées");
    }

    @Test
    void registerMoveAdvancesCurrentPlayer() {
        int[] pkTileSources = emptyTileSourcesWithMarkerInCenter();
        pkTileSources[TileSource.FACTORY_1.index()] =
                PkTileSet.of(4, TileKind.Colored.A);

        int[] pkPlayerStates = initialPlayerStates();
        MutableGameState state = newState(
                PkTileSet.EMPTY,
                pkTileSources,
                expectedUniqueTileSources(pkTileSources),
                pkPlayerStates,
                PlayerId.P1,
                PointsObserver.EMPTY
        );

        assertEquals(PlayerId.P1, state.currentPlayerId());
        Move move = new Move(TileSource.FACTORY_1, TileKind.Colored.A,
                TileDestination.Pattern.PATTERN_5);
        state.registerMove(move.packed());
        assertEquals(PlayerId.P2, state.currentPlayerId());
    }

    @Test
    void registerMoveDoesNotModifyTileBag() {
        int[] pkTileSources = emptyTileSourcesWithMarkerInCenter();
        pkTileSources[TileSource.FACTORY_1.index()] =
                PkTileSet.of(4, TileKind.Colored.C);

        int[] pkPlayerStates = initialPlayerStates();
        int bag = PkTileSet.of(10, TileKind.Colored.A);
        MutableGameState state = newState(
                bag,
                pkTileSources,
                expectedUniqueTileSources(pkTileSources),
                pkPlayerStates,
                PlayerId.P1,
                PointsObserver.EMPTY
        );

        Move move = new Move(TileSource.FACTORY_1, TileKind.Colored.C,
                TileDestination.FLOOR);
        state.registerMove(move.packed());
        assertEquals(bag, state.pkTileBag(), "registerMove ne doit pas modifier le sac");
    }

    // =========================================================
    // Tests supplémentaires de endRound
    // =========================================================

    @Test
    void endRoundFloorPenaltyIsComputedFromTileCountNotPackedValue() {
        // Test crucial : vérifie que la pénalité est calculée à partir du NOMBRE
        // de tuiles (PkFloor.size), pas de la valeur empaquetée.
        int[] pkTileSources = emptyTileSourcesWithoutMarker();
        int[] pkPlayerStates = initialPlayerStates();

        // Plancher avec 3 tuiles → pénalité attendue = 1+1+2 = 4
        int pkFloor = PkFloor.EMPTY;
        pkFloor = PkFloor.withAddedTiles(pkFloor, PkTileSet.of(3, TileKind.Colored.E));
        PkPlayerStates.setPkFloor(pkPlayerStates, PlayerId.P1, pkFloor);
        PkPlayerStates.addPoints(pkPlayerStates, PlayerId.P1, 20);

        assertEquals(3, PkFloor.size(pkFloor));
        int expectedPenalty = Points.totalFloorPenalty(3); // = 4

        RecordingPointsObserver observer = new RecordingPointsObserver();
        MutableGameState state = newState(
                pkTileSources,
                pkPlayerStates,
                PlayerId.P1,
                observer
        );

        state.endRound();

        assertEquals(20 - expectedPenalty,
                PkPlayerStates.points(state.pkPlayerStates(), PlayerId.P1),
                "Score devrait être 20 - 4 = 16");
        assertEquals(expectedPenalty, observer.floorPenalty,
                "La pénalité observée devrait être 4");
    }

    @Test
    void endRoundFloorPenaltyWith7TilesGives14Points() {
        int[] pkTileSources = emptyTileSourcesWithoutMarker();
        int[] pkPlayerStates = initialPlayerStates();

        // Plancher plein (7 tuiles) → pénalité max = 14
        int pkFloor = PkFloor.EMPTY;
        pkFloor = PkFloor.withAddedTiles(pkFloor, PkTileSet.of(5, TileKind.Colored.A));
        pkFloor = PkFloor.withAddedTiles(pkFloor, PkTileSet.of(2, TileKind.Colored.B));
        PkPlayerStates.setPkFloor(pkPlayerStates, PlayerId.P1, pkFloor);
        PkPlayerStates.addPoints(pkPlayerStates, PlayerId.P1, 50);

        assertEquals(7, PkFloor.size(pkFloor));

        MutableGameState state = newState(
                pkTileSources,
                pkPlayerStates,
                PlayerId.P1,
                PointsObserver.EMPTY
        );

        state.endRound();

        assertEquals(50 - 14,
                PkPlayerStates.points(state.pkPlayerStates(), PlayerId.P1),
                "Score devrait être 50 - 14 = 36");
    }

    @Test
    void endRoundMultipleFullPatternsAllScoredCorrectly() {
        int[] pkTileSources = emptyTileSourcesWithoutMarker();
        int[] pkPlayerStates = initialPlayerStates();

        // Pattern 1 plein avec A, pattern 2 plein avec B
        int patterns = PkPatterns.EMPTY;
        patterns = PkPatterns.withAddedTiles(patterns, TileDestination.Pattern.PATTERN_1, 1, TileKind.Colored.A);
        patterns = PkPatterns.withAddedTiles(patterns, TileDestination.Pattern.PATTERN_2, 2, TileKind.Colored.B);
        PkPlayerStates.setPkPatterns(pkPlayerStates, PlayerId.P1, patterns);

        RecordingPointsObserver observer = new RecordingPointsObserver();
        MutableGameState state = newState(
                pkTileSources,
                pkPlayerStates,
                PlayerId.P1,
                observer
        );

        state.endRound();

        int wall = PkPlayerStates.pkWall(state.pkPlayerStates(), PlayerId.P1);
        assertTrue(PkWall.hasTileAt(wall, TileDestination.Pattern.PATTERN_1, TileKind.Colored.A));
        assertTrue(PkWall.hasTileAt(wall, TileDestination.Pattern.PATTERN_2, TileKind.Colored.B));

        // Deux appels à newWallTile
        assertEquals(2, observer.newWallTileCallCount);

        // Points : tile A sur mur vide = 1 pt, tile B avec possiblement un voisin
        int expectedPoints = PkPlayerStates.points(state.pkPlayerStates(), PlayerId.P1);
        assertTrue(expectedPoints >= 2, "Au minimum 2 points (1+1)");
    }

    @Test
    void endRoundClearsFloorAfterPenalty() {
        int[] pkTileSources = emptyTileSourcesWithoutMarker();
        int[] pkPlayerStates = initialPlayerStates();

        int pkFloor = PkFloor.withAddedTiles(PkFloor.EMPTY,
                PkTileSet.of(1, TileKind.Colored.D));
        PkPlayerStates.setPkFloor(pkPlayerStates, PlayerId.P1, pkFloor);
        PkPlayerStates.addPoints(pkPlayerStates, PlayerId.P1, 10);

        MutableGameState state = newState(
                pkTileSources,
                pkPlayerStates,
                PlayerId.P1,
                PointsObserver.EMPTY
        );

        state.endRound();

        assertEquals(PkFloor.EMPTY,
                PkPlayerStates.pkFloor(state.pkPlayerStates(), PlayerId.P1),
                "Le plancher devrait être vide après endRound");
        assertEquals(10 - 1,
                PkPlayerStates.points(state.pkPlayerStates(), PlayerId.P1),
                "Pénalité de 1 pour une tuile");
    }

    // =========================================================
    // Tests de isRoundOver et isGameOver
    // =========================================================

    @Test
    void isRoundOverReturnsTrueWhenAllSourcesEmpty() {
        int[] pkTileSources = emptyTileSourcesWithMarkerInCenter();
        int[] pkPlayerStates = initialPlayerStates();

        MutableGameState state = newState(
                PkTileSet.EMPTY,
                pkTileSources,
                PkIntSet32.EMPTY,
                pkPlayerStates,
                PlayerId.P1,
                PointsObserver.EMPTY
        );

        assertTrue(state.isRoundOver());
    }

    @Test
    void isRoundOverReturnsFalseWhenFactoryHasColoredTiles() {
        int[] pkTileSources = emptyTileSourcesWithMarkerInCenter();
        pkTileSources[TileSource.FACTORY_1.index()] = PkTileSet.of(1, TileKind.Colored.A);

        int[] pkPlayerStates = initialPlayerStates();
        int uniqueSources = expectedUniqueTileSources(pkTileSources);

        MutableGameState state = newState(
                PkTileSet.EMPTY,
                pkTileSources,
                uniqueSources,
                pkPlayerStates,
                PlayerId.P1,
                PointsObserver.EMPTY
        );

        assertFalse(state.isRoundOver());
    }

    @Test
    void isGameOverReturnsTrueWhenRoundOverAndPlayerHasFullRow() {
        int[] pkTileSources = emptyTileSourcesWithMarkerInCenter();
        int[] pkPlayerStates = initialPlayerStates();

        PkPlayerStates.setPkWall(pkPlayerStates, PlayerId.P1,
                fullRow(TileDestination.Pattern.PATTERN_3));

        MutableGameState state = newState(
                PkTileSet.EMPTY,
                pkTileSources,
                PkIntSet32.EMPTY,
                pkPlayerStates,
                PlayerId.P1,
                PointsObserver.EMPTY
        );

        assertTrue(state.isRoundOver());
        assertTrue(state.isGameOver());
    }

    @Test
    void isGameOverReturnsFalseWhenRoundOverButNoFullRow() {
        int[] pkTileSources = emptyTileSourcesWithMarkerInCenter();
        int[] pkPlayerStates = initialPlayerStates();

        // Mur avec 4 tuiles sur la ligne 1 (pas plein)
        int wall = PkWall.EMPTY;
        wall = PkWall.withTileAt(wall, TileDestination.Pattern.PATTERN_1, TileKind.Colored.A);
        wall = PkWall.withTileAt(wall, TileDestination.Pattern.PATTERN_1, TileKind.Colored.B);
        wall = PkWall.withTileAt(wall, TileDestination.Pattern.PATTERN_1, TileKind.Colored.C);
        wall = PkWall.withTileAt(wall, TileDestination.Pattern.PATTERN_1, TileKind.Colored.D);
        PkPlayerStates.setPkWall(pkPlayerStates, PlayerId.P1, wall);

        MutableGameState state = newState(
                PkTileSet.EMPTY,
                pkTileSources,
                PkIntSet32.EMPTY,
                pkPlayerStates,
                PlayerId.P1,
                PointsObserver.EMPTY
        );

        assertTrue(state.isRoundOver());
        assertFalse(state.isGameOver());
    }

    // =========================================================
    // Test d'une mini-partie complète
    // =========================================================

    @Test
    void endGameDoesNothingWhenNoBonusExistsWorks() {
        int[] pkTileSources = emptyTileSourcesWithoutMarker();
        int[] pkPlayerStates = initialPlayerStates();

        int wall = PkWall.withTileAt(
                PkWall.EMPTY,
                TileDestination.Pattern.PATTERN_1,
                TileKind.Colored.A
        );
        PkPlayerStates.setPkWall(pkPlayerStates, PlayerId.P1, wall);
        PkPlayerStates.addPoints(pkPlayerStates, PlayerId.P1, 3);

        RecordingPointsObserver observer = new RecordingPointsObserver();
        MutableGameState state = newState(
                pkTileSources,
                pkPlayerStates,
                PlayerId.P1,
                observer
        );

        state.endGame();

        assertEquals(3, PkPlayerStates.points(state.pkPlayerStates(), PlayerId.P1));
        assertEquals(0, observer.fullRowCallCount);
        assertEquals(0, observer.fullColumnCallCount);
        assertEquals(0, observer.fullColorCallCount);
    }
}