package ch.epfl.ajul;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OurPointsObserverTest {

    private static final class RecordingPointsObserver implements PointsObserver {
        int newWallTileCallCount = 0;
        PlayerId newWallTilePlayerId = null;
        TileDestination.Pattern newWallTileLine = null;
        TileKind.Colored newWallTileColor = null;
        int newWallTilePoints = -1;

        int floorCallCount = 0;
        PlayerId floorPlayerId = null;
        int floorPenalty = -1;

        int fullRowCallCount = 0;
        PlayerId fullRowPlayerId = null;
        TileDestination.Pattern fullRowLine = null;
        int fullRowPoints = -1;

        int fullColumnCallCount = 0;
        PlayerId fullColumnPlayerId = null;
        int fullColumnIndex = -1;
        int fullColumnPoints = -1;

        int fullColorCallCount = 0;
        PlayerId fullColorPlayerId = null;
        TileKind.Colored fullColor = null;
        int fullColorPoints = -1;

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

    @Test
    void pointsObserverEmptyExistsAndIsNotNull() {
        assertNotNull(PointsObserver.EMPTY);
    }

    @Test
    void pointsObserverEmptyMethodsDoNotThrow() {
        assertDoesNotThrow(() -> PointsObserver.EMPTY.newWallTile(
                PlayerId.P1,
                TileDestination.Pattern.PATTERN_1,
                TileKind.Colored.A,
                3
        ));

        assertDoesNotThrow(() -> PointsObserver.EMPTY.floor(PlayerId.P2, 2));

        assertDoesNotThrow(() -> PointsObserver.EMPTY.fullRow(
                PlayerId.P1,
                TileDestination.Pattern.PATTERN_3,
                2
        ));

        assertDoesNotThrow(() -> PointsObserver.EMPTY.fullColumn(
                PlayerId.P2,
                4,
                7
        ));

        assertDoesNotThrow(() -> PointsObserver.EMPTY.fullColor(
                PlayerId.P1,
                TileKind.Colored.E,
                10
        ));
    }

    @Test
    void pointsObserverDefaultMethodsDoNotThrowOnAnonymousImplementation() {
        PointsObserver observer = new PointsObserver() {};

        assertDoesNotThrow(() -> observer.newWallTile(
                PlayerId.P1,
                TileDestination.Pattern.PATTERN_2,
                TileKind.Colored.B,
                1
        ));

        assertDoesNotThrow(() -> observer.floor(PlayerId.P1, 1));

        assertDoesNotThrow(() -> observer.fullRow(
                PlayerId.P2,
                TileDestination.Pattern.PATTERN_5,
                2
        ));

        assertDoesNotThrow(() -> observer.fullColumn(PlayerId.P2, 0, 7));

        assertDoesNotThrow(() -> observer.fullColor(
                PlayerId.P1,
                TileKind.Colored.C,
                10
        ));
    }

    @Test
    void pointsObserverNewWallTilePassesCorrectArgumentsToImplementation() {
        RecordingPointsObserver observer = new RecordingPointsObserver();

        observer.newWallTile(
                PlayerId.P2,
                TileDestination.Pattern.PATTERN_4,
                TileKind.Colored.D,
                5
        );

        assertEquals(1, observer.newWallTileCallCount);
        assertEquals(PlayerId.P2, observer.newWallTilePlayerId);
        assertEquals(TileDestination.Pattern.PATTERN_4, observer.newWallTileLine);
        assertEquals(TileKind.Colored.D, observer.newWallTileColor);
        assertEquals(5, observer.newWallTilePoints);
    }

    @Test
    void pointsObserverFloorPassesCorrectArgumentsToImplementation() {
        RecordingPointsObserver observer = new RecordingPointsObserver();

        observer.floor(PlayerId.P1, 2);

        assertEquals(1, observer.floorCallCount);
        assertEquals(PlayerId.P1, observer.floorPlayerId);
        assertEquals(2, observer.floorPenalty);
    }

    @Test
    void pointsObserverFullRowPassesCorrectArgumentsToImplementation() {
        RecordingPointsObserver observer = new RecordingPointsObserver();

        observer.fullRow(PlayerId.P2, TileDestination.Pattern.PATTERN_2, 2);

        assertEquals(1, observer.fullRowCallCount);
        assertEquals(PlayerId.P2, observer.fullRowPlayerId);
        assertEquals(TileDestination.Pattern.PATTERN_2, observer.fullRowLine);
        assertEquals(2, observer.fullRowPoints);
    }

    @Test
    void pointsObserverFullColumnPassesCorrectArgumentsToImplementation() {
        RecordingPointsObserver observer = new RecordingPointsObserver();

        observer.fullColumn(PlayerId.P1, 3, 7);

        assertEquals(1, observer.fullColumnCallCount);
        assertEquals(PlayerId.P1, observer.fullColumnPlayerId);
        assertEquals(3, observer.fullColumnIndex);
        assertEquals(7, observer.fullColumnPoints);
    }

    @Test
    void pointsObserverFullColorPassesCorrectArgumentsToImplementation() {
        RecordingPointsObserver observer = new RecordingPointsObserver();

        observer.fullColor(PlayerId.P2, TileKind.Colored.A, 10);

        assertEquals(1, observer.fullColorCallCount);
        assertEquals(PlayerId.P2, observer.fullColorPlayerId);
        assertEquals(TileKind.Colored.A, observer.fullColor);
        assertEquals(10, observer.fullColorPoints);
    }
}