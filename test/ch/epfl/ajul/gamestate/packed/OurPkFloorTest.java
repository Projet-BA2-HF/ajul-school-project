package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileKind;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class OurPkFloorTest {

    // Exemples réutilisables de pkFloor (ligne plancher empaquetée)

    static final int PK_FLOOR_EMPTY =
            0b00000000_000_000_000_000_000_000_000_000;

    static final int PK_FLOOR_SIZE1_ONLY_B =
            0b00000000_000_000_000_000_000_000_001_001;

    static final int PK_FLOOR_SIZE2_C_THEN_E =
            0b00000000_000_000_000_000_000_100_010_010;

    static final int PK_FLOOR_EXAMPLE_FPM_THEN_B_B =
            0b00000000_000_000_000_000_001_001_101_011;

    static final int PK_FLOOR_FULL_SIZE7_MIXED =
            0b00000000_001_101_000_100_011_010_001_111;

    static final int PK_FLOOR_SIZE1_ONLY_FIRST_PLAYER_MARKER =
            0b00000000_000_000_000_000_000_000_101_001;

    // ───────────────────────── Helpers ─────────────────────────

    private static int packFloor(TileKind... tiles) {
        int pkFloor = tiles.length;
        for (int i = 0; i < tiles.length; i += 1) {
            pkFloor |= tiles[i].index() << (3 * (i + 1));
        }
        return pkFloor;
    }

    private static TileKind[] unpackFloor(int pkFloor) {
        TileKind[] tiles = new TileKind[PkFloor.size(pkFloor)];
        for (int i = 0; i < tiles.length; i += 1) {
            tiles[i] = PkFloor.tileAt(pkFloor, i);
        }
        return tiles;
    }

    private static int randomPkFloor(RandomGenerator rng) {
        int size = rng.nextInt(8); // 0..7
        int pkFloor = size;

        boolean markerAlreadyUsed = false;

        for (int i = 0; i < size; i += 1) {
            int kindIndex;
            if (markerAlreadyUsed) {
                kindIndex = rng.nextInt(TileKind.Colored.COUNT); // seulement A..E
            } else {
                kindIndex = rng.nextInt(TileKind.COUNT); // A..E ou marker
                if (kindIndex == TileKind.FIRST_PLAYER_MARKER.index()) {
                    markerAlreadyUsed = true;
                }
            }
            pkFloor |= kindIndex << (3 * (i + 1));
        }
        return pkFloor;
    }

    private static int randomPkTileSet(RandomGenerator rng) {
        int pkTileSet = PkTileSet.EMPTY;
        for (TileKind.Colored color : TileKind.Colored.ALL) {
            pkTileSet = PkTileSet.union(pkTileSet, PkTileSet.of(rng.nextInt(21), color));
        }
        pkTileSet = PkTileSet.union(pkTileSet, PkTileSet.of(rng.nextInt(2), TileKind.FIRST_PLAYER_MARKER));
        return pkTileSet;
    }

    private static int oracleWithAddedTiles(int pkFloor, int pkTileSet) {
        List<TileKind> tiles = new ArrayList<>();
        for (TileKind tile : unpackFloor(pkFloor)) {
            tiles.add(tile);
        }

        for (TileKind tileKind : TileKind.ALL) {
            int count = PkTileSet.countOf(pkTileSet, tileKind);
            for (int i = 0; i < count; i += 1) {
                if (tiles.size() < 7) {
                    tiles.add(tileKind);
                }
            }
        }

        if (PkTileSet.countOf(pkTileSet, TileKind.FIRST_PLAYER_MARKER) == 1
                && tiles.size() == 7
                && !tiles.contains(TileKind.FIRST_PLAYER_MARKER)) {
            tiles.set(6, TileKind.FIRST_PLAYER_MARKER);
        }

        return packFloor(tiles.toArray(new TileKind[0]));
    }

    // ───────────────────────── EMPTY / base ─────────────────────────

    @Test
    void emptyConstantIsZero() {
        assertEquals(0, PkFloor.EMPTY);
    }

    // ───────────────────────── size ─────────────────────────

    @Test
    void size() {
        assertEquals(0, PkFloor.size(PK_FLOOR_EMPTY));
        assertEquals(1, PkFloor.size(PK_FLOOR_SIZE1_ONLY_B));
        assertEquals(2, PkFloor.size(PK_FLOOR_SIZE2_C_THEN_E));
        assertEquals(3, PkFloor.size(PK_FLOOR_EXAMPLE_FPM_THEN_B_B));
        assertEquals(7, PkFloor.size(PK_FLOOR_FULL_SIZE7_MIXED));
        assertEquals(1, PkFloor.size(PK_FLOOR_SIZE1_ONLY_FIRST_PLAYER_MARKER));
    }

    @Test
    void sizeWorksOnRandomFloors() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        for (int i = 0; i < 1000; i += 1) {
            int expectedSize = rng.nextInt(8);
            int pkFloor = expectedSize;
            for (int j = 0; j < expectedSize; j += 1) {
                pkFloor |= rng.nextInt(TileKind.COUNT) << (3 * (j + 1));
            }
            assertEquals(expectedSize, PkFloor.size(pkFloor));
        }
    }

    // ───────────────────────── tileAt ─────────────────────────

    @Test
    void tileAt() {
        assertEquals(TileKind.B, PkFloor.tileAt(PK_FLOOR_SIZE1_ONLY_B, 0));
        assertEquals(TileKind.E, PkFloor.tileAt(PK_FLOOR_SIZE2_C_THEN_E, 1));
        assertEquals(TileKind.C, PkFloor.tileAt(PK_FLOOR_SIZE2_C_THEN_E, 0));
        assertEquals(TileKind.B, PkFloor.tileAt(PK_FLOOR_FULL_SIZE7_MIXED, 6));
        assertEquals(TileKind.A, PkFloor.tileAt(PK_FLOOR_FULL_SIZE7_MIXED, 4));
        assertEquals(TileKind.FIRST_PLAYER_MARKER, PkFloor.tileAt(PK_FLOOR_SIZE1_ONLY_FIRST_PLAYER_MARKER, 0));
    }

    @Test
    void tileAtWorksOnRandomFloors() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        for (int k = 0; k < 1000; k += 1) {
            int size = rng.nextInt(1, 8);
            TileKind[] expected = new TileKind[size];
            int pkFloor = size;
            for (int i = 0; i < size; i += 1) {
                expected[i] = TileKind.ALL.get(rng.nextInt(TileKind.COUNT));
                pkFloor |= expected[i].index() << (3 * (i + 1));
            }

            for (int i = 0; i < size; i += 1) {
                assertEquals(expected[i], PkFloor.tileAt(pkFloor, i));
            }
        }
    }

    @Test
    void tileAtRejectsNegativeIndexWhenAssertionsAreEnabled() {
        assumeTrue(PkFloor.class.desiredAssertionStatus());
        assertThrows(AssertionError.class, () -> PkFloor.tileAt(PK_FLOOR_SIZE1_ONLY_B, -1));
    }

    @Test
    void tileAtRejectsIndexEqualToSizeWhenAssertionsAreEnabled() {
        assumeTrue(PkFloor.class.desiredAssertionStatus());
        assertThrows(AssertionError.class, () -> PkFloor.tileAt(PK_FLOOR_SIZE1_ONLY_B, 1));
    }

    // ───────────────────────── withAddedTiles ─────────────────────────

    @Test
    void withAddedTiles() {
        int pkFloor = 0;
        int pkTileSet = PkTileSet.of(1, TileKind.A);

        int result = PkFloor.withAddedTiles(pkFloor, pkTileSet);

        int expected = 0b00000000_000_000_000_000_000_000_000_001;
        assertEquals(expected, result);
    }

    @Test
    void test1_emptyFloor_addOneA() {
        int pkFloor = 0;
        int pkTileSet = PkTileSet.of(1, TileKind.A);

        int result = PkFloor.withAddedTiles(pkFloor, pkTileSet);

        int expected = 0b00000000_000_000_000_000_000_000_000_001;
        assertEquals(expected, result);
    }

    @Test
    void test2_emptyFloor_addTwoB() {
        int pkFloor = 0;
        int pkTileSet = PkTileSet.of(2, TileKind.B);

        int result = PkFloor.withAddedTiles(pkFloor, pkTileSet);

        int expected = 0b00000000_000_000_000_000_000_001_001_010;
        assertEquals(expected, result);
    }

    @Test
    void test3_emptyFloor_addMarkerOnly() {
        int pkFloor = 0;
        int pkTileSet = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);

        int result = PkFloor.withAddedTiles(pkFloor, pkTileSet);

        int expected = 0b00000000_000_000_000_000_000_000_101_001;
        assertEquals(expected, result);
    }

    @Test
    void test4_oneA_addBAndMarker() {
        int pkFloor = 0b00000000_000_000_000_000_000_000_000_001;
        int pkTileSet = PkTileSet.union(
                PkTileSet.of(1, TileKind.B),
                PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER)
        );

        int result = PkFloor.withAddedTiles(pkFloor, pkTileSet);

        int expected = 0b00000000_000_000_000_000_101_001_000_011;
        assertEquals(expected, result);
    }

    @Test
    void test5_fullFloorAllA_addMarker() {
        int pkFloor = 0b00000000_000_000_000_000_000_000_000_111;
        int pkTileSet = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);

        int result = PkFloor.withAddedTiles(pkFloor, pkTileSet);

        int expected = 0b00000000_101_000_000_000_000_000_000_111;
        assertEquals(expected, result);
    }

    @Test
    void test6_sixB_addEAndMarker() {
        int pkFloor = 0b00000000_000_001_001_001_001_001_001_110;
        int pkTileSet = PkTileSet.union(
                PkTileSet.of(1, TileKind.E),
                PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER)
        );

        int result = PkFloor.withAddedTiles(pkFloor, pkTileSet);

        int expected = 0b00000000_101_001_001_001_001_001_001_111;
        assertEquals(expected, result);
    }

    @Test
    void test7_floorAlreadyHasMarker_addB() {
        int pkFloor = 0b00000000_000_000_000_000_000_000_101_010;
        int pkTileSet = PkTileSet.of(1, TileKind.B);

        int result = PkFloor.withAddedTiles(pkFloor, pkTileSet);

        int expected = 0b00000000_000_000_000_000_001_000_101_011;
        assertEquals(expected, result);
    }

    @Test
    void test8_emptyFloor_addMixedTiles() {
        int pkFloor = 0;
        int pkTileSet = PkTileSet.union(
                PkTileSet.union(
                        PkTileSet.of(2, TileKind.A),
                        PkTileSet.of(1, TileKind.C)
                ),
                PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER)
        );

        int result = PkFloor.withAddedTiles(pkFloor, pkTileSet);

        int expected = 0b00000000_000_000_000_101_010_000_000_100;
        assertEquals(expected, result);
    }

    @Test
    void withAddedTiles_overflowIgnoresExcessNonMarker() {
        int pkFloor = 0b00000000_000_001_001_001_001_001_001_110;

        int pkTileSet = PkTileSet.union(
                PkTileSet.of(1, TileKind.D),
                PkTileSet.of(1, TileKind.E)
        );

        int result = PkFloor.withAddedTiles(pkFloor, pkTileSet);

        int expected = 0b00000000_011_001_001_001_001_001_001_111;
        assertEquals(expected, result);
    }

    @Test
    void withAddedTiles_fullFloorAddingNonMarkerDoesNothing() {
        int pkFloor = 0b00000000_001_101_000_100_011_010_001_111;
        int pkTileSet = PkTileSet.of(3, TileKind.C);

        int result = PkFloor.withAddedTiles(pkFloor, pkTileSet);

        assertEquals(pkFloor, result);
    }

    @Test
    void withAddedTiles_emptyTileSet_leavesFloorUnchanged() {
        assertEquals(PK_FLOOR_SIZE2_C_THEN_E, PkFloor.withAddedTiles(PK_FLOOR_SIZE2_C_THEN_E, PkTileSet.EMPTY));
        assertEquals(PK_FLOOR_EMPTY, PkFloor.withAddedTiles(PK_FLOOR_EMPTY, PkTileSet.EMPTY));
    }

    @Test
    void withAddedTiles_exactFill_noMarker() {
        int pkFloor = 0b00000000_000_000_100_100_100_100_100_101;
        int pkTileSet = PkTileSet.of(2, TileKind.D);

        int result = PkFloor.withAddedTiles(pkFloor, pkTileSet);

        int expected = 0b00000000_011_011_100_100_100_100_100_111;
        assertEquals(expected, result);
    }

    @Test
    void withAddedTiles_fullFloorAlreadyHasMarker_addMarkerAgain_unchanged() {
        int pkFloor = 0b00000000_000_000_000_000_000_000_101_111;
        int pkTileSet = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);

        int result = PkFloor.withAddedTiles(pkFloor, pkTileSet);

        assertEquals(pkFloor, result);
    }

    @Test
    void withAddedTiles_moreThanCapacity_onlySevenAdded() {
        int pkTileSet = PkTileSet.of(10, TileKind.B);

        int result = PkFloor.withAddedTiles(PkFloor.EMPTY, pkTileSet);

        int expected = 0b00000000_001_001_001_001_001_001_001_111;
        assertEquals(expected, result);
    }

    @Test
    void withAddedTiles_addsTilesInKindOrder() {
        int pkFloor = PkFloor.EMPTY;
        int pkTileSet = PkTileSet.EMPTY;
        pkTileSet = PkTileSet.union(pkTileSet, PkTileSet.of(1, TileKind.E));
        pkTileSet = PkTileSet.union(pkTileSet, PkTileSet.of(1, TileKind.A));
        pkTileSet = PkTileSet.union(pkTileSet, PkTileSet.of(1, TileKind.C));
        pkTileSet = PkTileSet.union(pkTileSet, PkTileSet.of(1, TileKind.B));

        int actual = PkFloor.withAddedTiles(pkFloor, pkTileSet);
        int expected = packFloor(TileKind.A, TileKind.B, TileKind.C, TileKind.E);

        assertEquals(expected, actual);
    }

    @Test
    void withAddedTiles_preservesExistingTilesOrder() {
        int pkFloor = packFloor(TileKind.C, TileKind.FIRST_PLAYER_MARKER, TileKind.B);
        int pkTileSet = PkTileSet.union(
                PkTileSet.of(1, TileKind.A),
                PkTileSet.of(1, TileKind.D)
        );

        int actual = PkFloor.withAddedTiles(pkFloor, pkTileSet);
        int expected = packFloor(TileKind.C, TileKind.FIRST_PLAYER_MARKER, TileKind.B, TileKind.A, TileKind.D);

        assertEquals(expected, actual);
    }

    @Test
    void withAddedTiles_markerReplacesLastTileWhenNeeded() {
        int pkFloor = packFloor(
                TileKind.A, TileKind.B, TileKind.C,
                TileKind.D, TileKind.E, TileKind.A
        );
        int pkTileSet = PkTileSet.union(
                PkTileSet.of(1, TileKind.B),
                PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER)
        );

        int actual = PkFloor.withAddedTiles(pkFloor, pkTileSet);
        int expected = packFloor(
                TileKind.A, TileKind.B, TileKind.C,
                TileKind.D, TileKind.E, TileKind.A,
                TileKind.FIRST_PLAYER_MARKER
        );

        assertEquals(expected, actual);
    }

    @Test
    void withAddedTilesWorksOnRandomInputs() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        for (int i = 0; i < 1000; i += 1) {
            int pkFloor = randomPkFloor(rng);
            int pkTileSet = randomPkTileSet(rng);

            int expected = oracleWithAddedTiles(pkFloor, pkTileSet);
            int actual = PkFloor.withAddedTiles(pkFloor, pkTileSet);

            assertEquals(expected, actual);
        }
    }

    // ───────────────────────── containsFirstPlayerMarker ─────────────────────────

    @Test
    void containsFirstPlayerMarker_trueWhenPresent() {
        assertTrue(PkFloor.containsFirstPlayerMarker(PK_FLOOR_EXAMPLE_FPM_THEN_B_B));
        assertTrue(PkFloor.containsFirstPlayerMarker(PK_FLOOR_SIZE1_ONLY_FIRST_PLAYER_MARKER));
    }

    @Test
    void containsFirstPlayerMarker_falseWhenAbsent() {
        assertFalse(PkFloor.containsFirstPlayerMarker(PK_FLOOR_EMPTY));
        assertFalse(PkFloor.containsFirstPlayerMarker(PK_FLOOR_SIZE1_ONLY_B));
        assertFalse(PkFloor.containsFirstPlayerMarker(PK_FLOOR_SIZE2_C_THEN_E));
    }

    @Test
    void containsFirstPlayerMarkerWorksOnRandomFloors() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        for (int i = 0; i < 1000; i += 1) {
            int pkFloor = randomPkFloor(rng);

            boolean expected = false;
            for (TileKind tile : unpackFloor(pkFloor)) {
                if (tile == TileKind.FIRST_PLAYER_MARKER) {
                    expected = true;
                    break;
                }
            }

            assertEquals(expected, PkFloor.containsFirstPlayerMarker(pkFloor));
        }
    }

    // ───────────────────────── asPkTileSet ─────────────────────────

    @Test
    void asPkTileSet_emptyGivesEmptyTileSet() {
        assertEquals(0, PkFloor.asPkTileSet(PK_FLOOR_EMPTY));
    }

    @Test
    void asPkTileSet_collectsAllTiles() {
        int expected = PkTileSet.union(
                PkTileSet.of(2, TileKind.B),
                PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER)
        );
        assertEquals(expected, PkFloor.asPkTileSet(PK_FLOOR_EXAMPLE_FPM_THEN_B_B));
    }

    @Test
    void asPkTileSetWorksOnRandomFloors() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        for (int i = 0; i < 1000; i += 1) {
            int pkFloor = randomPkFloor(rng);

            int expected = PkTileSet.EMPTY;
            for (TileKind tile : unpackFloor(pkFloor)) {
                expected = PkTileSet.union(expected, PkTileSet.of(1, tile));
            }

            assertEquals(expected, PkFloor.asPkTileSet(pkFloor));
        }
    }

    // ───────────────────────── toString ─────────────────────────

    @Test
    void toString_emptyIsBracketsOnly() {
        assertEquals("[]", PkFloor.toString(PK_FLOOR_EMPTY));
    }

    @Test
    void toString_keepsOrder() {
        assertEquals("[FIRST_PLAYER_MARKER, B, B]", PkFloor.toString(PK_FLOOR_EXAMPLE_FPM_THEN_B_B));
        assertEquals("[C, E]", PkFloor.toString(PK_FLOOR_SIZE2_C_THEN_E));
    }

    @Test
    void toString_singleTileWorksForEveryKind() {
        for (TileKind kind : TileKind.ALL) {
            int pkFloor = packFloor(kind);
            assertEquals("[" + kind + "]", PkFloor.toString(pkFloor));
        }
    }

    @Test
    void toString_fullMixedFloorWorks() {
        int pkFloor = packFloor(
                TileKind.A,
                TileKind.FIRST_PLAYER_MARKER,
                TileKind.C,
                TileKind.E,
                TileKind.B,
                TileKind.D,
                TileKind.A
        );

        assertEquals("[A, FIRST_PLAYER_MARKER, C, E, B, D, A]", PkFloor.toString(pkFloor));
    }
}