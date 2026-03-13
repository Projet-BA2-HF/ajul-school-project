package ch.epfl.ajul;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OurPointsTest {

    @Test
    void bonusConstantsAreCorrect() {
        assertEquals(2, Points.FULL_ROW_BONUS_POINTS);
        assertEquals(7, Points.FULL_COLUMN_BONUS_POINTS);
        assertEquals(10, Points.FULL_COLOR_BONUS_POINTS);
    }

    @Test
    void newWallTilePointsForPureHorizontalGroups() {
        assertEquals(1, Points.newWallTilePoints(1, 1));
        assertEquals(2, Points.newWallTilePoints(2, 1));
        assertEquals(3, Points.newWallTilePoints(3, 1));
        assertEquals(4, Points.newWallTilePoints(4, 1));
        assertEquals(5, Points.newWallTilePoints(5, 1));
    }

    @Test
    void newWallTilePointsForPureVerticalGroups() {
        assertEquals(1, Points.newWallTilePoints(1, 1));
        assertEquals(2, Points.newWallTilePoints(1, 2));
        assertEquals(3, Points.newWallTilePoints(1, 3));
        assertEquals(4, Points.newWallTilePoints(1, 4));
        assertEquals(5, Points.newWallTilePoints(1, 5));
    }

    @Test
    void newWallTilePointsForMixedGroups() {
        assertEquals(4, Points.newWallTilePoints(2, 2));
        assertEquals(5, Points.newWallTilePoints(3, 2));
        assertEquals(5, Points.newWallTilePoints(2, 3));
        assertEquals(6, Points.newWallTilePoints(3, 3));
        assertEquals(7, Points.newWallTilePoints(4, 3));
        assertEquals(8, Points.newWallTilePoints(5, 3));
        assertEquals(6, Points.newWallTilePoints(2, 4));
        assertEquals(8, Points.newWallTilePoints(3, 5));
        assertEquals(10, Points.newWallTilePoints(5, 5));
    }

    @Test
    void newWallTilePointsMatchesSpecificationForAllValidPairs() {
        for (int h = 1; h <= 5; h++) {
            for (int v = 1; v <= 5; v++) {
                int expected;
                if (v == 1) {
                    expected = h;
                } else if (h == 1) {
                    expected = v;
                } else {
                    expected = h + v;
                }

                assertEquals(expected, Points.newWallTilePoints(h, v),
                        "Mismatch for h=" + h + ", v=" + v);
            }
        }
    }

    @Test
    void floorPenaltyMatchesAllSevenPositions() {
        assertEquals(1, Points.floorPenalty(0));
        assertEquals(1, Points.floorPenalty(1));
        assertEquals(2, Points.floorPenalty(2));
        assertEquals(2, Points.floorPenalty(3));
        assertEquals(2, Points.floorPenalty(4));
        assertEquals(3, Points.floorPenalty(5));
        assertEquals(3, Points.floorPenalty(6));
    }

    @Test
    void floorPenaltyMatchesExpectedSequence() {
        int[] expected = {1, 1, 2, 2, 2, 3, 3};

        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], Points.floorPenalty(i),
                    "Mismatch at floor tile index " + i);
        }
    }

    @Test
    void totalFloorPenaltyMatchesAllEightCounts() {
        assertEquals(0, Points.totalFloorPenalty(0));
        assertEquals(1, Points.totalFloorPenalty(1));
        assertEquals(2, Points.totalFloorPenalty(2));
        assertEquals(4, Points.totalFloorPenalty(3));
        assertEquals(6, Points.totalFloorPenalty(4));
        assertEquals(8, Points.totalFloorPenalty(5));
        assertEquals(11, Points.totalFloorPenalty(6));
        assertEquals(14, Points.totalFloorPenalty(7));
    }

    @Test
    void totalFloorPenaltyMatchesExpectedSequence() {
        int[] expected = {0, 1, 2, 4, 6, 8, 11, 14};

        for (int tilesCount = 0; tilesCount < expected.length; tilesCount++) {
            assertEquals(expected[tilesCount], Points.totalFloorPenalty(tilesCount),
                    "Mismatch for tilesCount=" + tilesCount);
        }
    }

    @Test
    void totalFloorPenaltyMatchesCumulativeFloorPenalty() {
        for (int tilesCount = 0; tilesCount <= 7; tilesCount++) {
            int expected = 0;
            for (int i = 0; i < tilesCount; i++) {
                expected += Points.floorPenalty(i);
            }
            assertEquals(expected, Points.totalFloorPenalty(tilesCount),
                    "Mismatch for cumulative floor penalty with " + tilesCount + " tiles");
        }
    }

    @Test
    void totalFloorPenaltyIsNonDecreasing() {
        for (int tilesCount = 0; tilesCount < 7; tilesCount++) {
            assertTrue(Points.totalFloorPenalty(tilesCount)
                            <= Points.totalFloorPenalty(tilesCount + 1),
                    "Penalty should be non-decreasing between "
                            + tilesCount + " and " + (tilesCount + 1));
        }
    }

    @Test
    void totalFloorPenaltyAtFullFloorIsFourteen() {
        assertEquals(14, Points.totalFloorPenalty(TileDestination.FLOOR.capacity()));
    }
}