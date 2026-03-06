package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import org.junit.jupiter.api.Test;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class OurPkPatternsTest {

    private static final int SIZE_BITS = 3;
    private static final int BITS_PER_LINE = 6;

    // Exemple exact de l’énoncé :
    // [C, AA, AAA, EEE., .....]
    private static final int PK_PATTERNS_SPEC_EXAMPLE =
            0b00_000_000_100_011_000_011_000_010_010_001;

    // ───────────────────────── Helpers ─────────────────────────

    private record PatternsData(int pk, int[] sizes, TileKind.Colored[] colors) {}

    private static int packLine(TileDestination.Pattern line, int size, int colorIndex) {
        return (size | (colorIndex << SIZE_BITS)) << (line.index() * BITS_PER_LINE);
    }

    private static PatternsData randomPatternsData(RandomGenerator rng) {
        int pk = 0;
        int[] sizes = new int[TileDestination.Pattern.COUNT];
        TileKind.Colored[] colors = new TileKind.Colored[TileDestination.Pattern.COUNT];

        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
            int index = line.index();
            int size = rng.nextInt(line.capacity() + 1);
            sizes[index] = size;

            int colorIndex = 0;
            if (size > 0) {
                colorIndex = rng.nextInt(TileKind.Colored.COUNT);
                colors[index] = TileKind.Colored.ALL.get(colorIndex);
            }

            pk |= packLine(line, size, colorIndex);
        }

        return new PatternsData(pk, sizes, colors);
    }

    private static int oracleAsPkTileSet(PatternsData data) {
        int pkTileSet = PkTileSet.EMPTY;

        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
            int i = line.index();
            if (data.sizes()[i] > 0) {
                pkTileSet = PkTileSet.union(
                        pkTileSet,
                        PkTileSet.of(data.sizes()[i], data.colors()[i])
                );
            }
        }

        return pkTileSet;
    }

    private static String oracleToString(PatternsData data) {
        StringBuilder sb = new StringBuilder("[");
        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
            int i = line.index();
            int count = data.sizes()[i];
            int capacity = line.capacity();

            if (count > 0) {
                sb.append(data.colors()[i].toString().repeat(count));
            }
            sb.append(".".repeat(capacity - count));

            if (i < TileDestination.Pattern.COUNT - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    // ───────────────────────── constantes / exemple de spec ─────────────────────────

    @Test
    void emptyConstantIsZero() {
        assertEquals(0, PkPatterns.EMPTY);
    }

    @Test
    void specExampleIsDecodedCorrectly() {
        assertEquals(1, PkPatterns.size(PK_PATTERNS_SPEC_EXAMPLE, TileDestination.Pattern.PATTERN_1));
        assertEquals(2, PkPatterns.size(PK_PATTERNS_SPEC_EXAMPLE, TileDestination.Pattern.PATTERN_2));
        assertEquals(3, PkPatterns.size(PK_PATTERNS_SPEC_EXAMPLE, TileDestination.Pattern.PATTERN_3));
        assertEquals(3, PkPatterns.size(PK_PATTERNS_SPEC_EXAMPLE, TileDestination.Pattern.PATTERN_4));
        assertEquals(0, PkPatterns.size(PK_PATTERNS_SPEC_EXAMPLE, TileDestination.Pattern.PATTERN_5));

        assertEquals(TileKind.Colored.C, PkPatterns.color(PK_PATTERNS_SPEC_EXAMPLE, TileDestination.Pattern.PATTERN_1));
        assertEquals(TileKind.Colored.A, PkPatterns.color(PK_PATTERNS_SPEC_EXAMPLE, TileDestination.Pattern.PATTERN_2));
        assertEquals(TileKind.Colored.A, PkPatterns.color(PK_PATTERNS_SPEC_EXAMPLE, TileDestination.Pattern.PATTERN_3));
        assertEquals(TileKind.Colored.E, PkPatterns.color(PK_PATTERNS_SPEC_EXAMPLE, TileDestination.Pattern.PATTERN_4));

        assertEquals("[C, AA, AAA, EEE., .....]", PkPatterns.toString(PK_PATTERNS_SPEC_EXAMPLE));
    }

    // ───────────────────────── size ─────────────────────────

    @Test
    void sizeEmptyLine() {
        assertEquals(0, PkPatterns.size(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_1));
    }

    @Test
    void sizeOneTile() {
        int pk = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_1, 1, TileKind.Colored.ALL.get(0));
        assertEquals(1, PkPatterns.size(pk, TileDestination.Pattern.PATTERN_1));
    }

    @Test
    void sizeFullLine2() {
        int pk = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_2, 2, TileKind.Colored.ALL.get(1));
        assertEquals(2, PkPatterns.size(pk, TileDestination.Pattern.PATTERN_2));
    }

    @Test
    void sizeFullLine5() {
        int pk = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_5, 5, TileKind.Colored.ALL.get(1));
        assertEquals(5, PkPatterns.size(pk, TileDestination.Pattern.PATTERN_5));
    }

    @Test
    void sizeDoesNotAffectOtherLines() {
        int pk = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_1, 1, TileKind.Colored.ALL.get(0));
        assertEquals(0, PkPatterns.size(pk, TileDestination.Pattern.PATTERN_2));
        assertEquals(0, PkPatterns.size(pk, TileDestination.Pattern.PATTERN_3));
        assertEquals(0, PkPatterns.size(pk, TileDestination.Pattern.PATTERN_4));
        assertEquals(0, PkPatterns.size(pk, TileDestination.Pattern.PATTERN_5));
    }

    @Test
    void sizeWorksOnRandomPatterns() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        for (int k = 0; k < 1000; k += 1) {
            PatternsData data = randomPatternsData(rng);
            for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
                assertEquals(data.sizes()[line.index()], PkPatterns.size(data.pk(), line));
            }
        }
    }

    // ───────────────────────── color ─────────────────────────

    @Test
    void colorIndexA() {
        int pk = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_1, 1, TileKind.Colored.ALL.get(0));
        assertEquals(TileKind.Colored.ALL.get(0), PkPatterns.color(pk, TileDestination.Pattern.PATTERN_1));
    }

    @Test
    void colorIndexB() {
        int pk = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_1, 1, TileKind.Colored.ALL.get(1));
        assertEquals(TileKind.Colored.ALL.get(1), PkPatterns.color(pk, TileDestination.Pattern.PATTERN_1));
    }

    @Test
    void colorIndexE() {
        int pk = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_1, 1, TileKind.Colored.ALL.get(4));
        assertEquals(TileKind.Colored.ALL.get(4), PkPatterns.color(pk, TileDestination.Pattern.PATTERN_1));
    }

    @Test
    void colorOnLine3() {
        int pk = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_3, 2, TileKind.Colored.ALL.get(2));
        assertEquals(TileKind.Colored.ALL.get(2), PkPatterns.color(pk, TileDestination.Pattern.PATTERN_3));
    }

    @Test
    void colorAllLines() {
        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
            int pk = PkPatterns.withAddedTiles(PkPatterns.EMPTY, line, 1, TileKind.Colored.ALL.get(3));
            assertEquals(TileKind.Colored.ALL.get(3), PkPatterns.color(pk, line));
        }
    }

    @Test
    void colorWorksOnRandomNonEmptyLines() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        for (int k = 0; k < 1000; k += 1) {
            PatternsData data = randomPatternsData(rng);
            for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
                int i = line.index();
                if (data.sizes()[i] > 0) {
                    assertEquals(data.colors()[i], PkPatterns.color(data.pk(), line));
                }
            }
        }
    }

    @Test
    void colorRejectsEmptyLineWhenAssertionsAreEnabled() {
        assumeTrue(PkPatterns.class.desiredAssertionStatus());
        assertThrows(AssertionError.class,
                () -> PkPatterns.color(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_1));
    }

    // ───────────────────────── isFull ─────────────────────────

    @Test
    void isFullLine1True() {
        int pk = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_1, 1, TileKind.Colored.ALL.get(0));
        assertTrue(PkPatterns.isFull(pk, TileDestination.Pattern.PATTERN_1));
    }

    @Test
    void isFullLine1False() {
        assertFalse(PkPatterns.isFull(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_1));
    }

    @Test
    void isFullLine2PartiallyFull() {
        int pk = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_2, 1, TileKind.Colored.ALL.get(0));
        assertFalse(PkPatterns.isFull(pk, TileDestination.Pattern.PATTERN_2));
    }

    @Test
    void isFullLine2Full() {
        int pk = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_2, 2, TileKind.Colored.ALL.get(0));
        assertTrue(PkPatterns.isFull(pk, TileDestination.Pattern.PATTERN_2));
    }

    @Test
    void isFullLine5Full() {
        int pk = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_5, 5, TileKind.Colored.ALL.get(3));
        assertTrue(PkPatterns.isFull(pk, TileDestination.Pattern.PATTERN_5));
    }

    @Test
    void isFullWorksOnRandomPatterns() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        for (int k = 0; k < 1000; k += 1) {
            PatternsData data = randomPatternsData(rng);
            for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
                boolean expected = data.sizes()[line.index()] == line.capacity();
                assertEquals(expected, PkPatterns.isFull(data.pk(), line));
            }
        }
    }

    // ───────────────────────── canContain ─────────────────────────

    @Test
    void canContainEmptyLineAnyColor() {
        for (TileKind.Colored c : TileKind.Colored.ALL) {
            assertTrue(PkPatterns.canContain(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_1, c));
        }
    }

    @Test
    void canContainSameColor() {
        int pk = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_2, 1, TileKind.Colored.ALL.get(2));
        assertTrue(PkPatterns.canContain(pk, TileDestination.Pattern.PATTERN_2, TileKind.Colored.ALL.get(2)));
    }

    @Test
    void canContainDifferentColor() {
        int pk = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_2, 1, TileKind.Colored.ALL.get(2));
        assertFalse(PkPatterns.canContain(pk, TileDestination.Pattern.PATTERN_2, TileKind.Colored.ALL.get(3)));
    }

    @Test
    void canContainFullLineButSameColor() {
        int pk = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_1, 1, TileKind.Colored.ALL.get(0));
        assertTrue(PkPatterns.canContain(pk, TileDestination.Pattern.PATTERN_1, TileKind.Colored.ALL.get(0)));
    }

    @Test
    void canContainWorksOnRandomPatterns() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        for (int k = 0; k < 1000; k += 1) {
            PatternsData data = randomPatternsData(rng);
            for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
                int i = line.index();
                for (TileKind.Colored c : TileKind.Colored.ALL) {
                    boolean expected = data.sizes()[i] == 0 || data.colors()[i] == c;
                    assertEquals(expected, PkPatterns.canContain(data.pk(), line, c));
                }
            }
        }
    }

    // ───────────────────────── withAddedTiles ─────────────────────────

    @Test
    void withAddedTilesOnEmptyLine() {
        int result = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_1, 1, TileKind.Colored.ALL.get(0));
        assertEquals(1, PkPatterns.size(result, TileDestination.Pattern.PATTERN_1));
        assertEquals(TileKind.Colored.ALL.get(0), PkPatterns.color(result, TileDestination.Pattern.PATTERN_1));
    }

    @Test
    void withAddedTilesAccumulates() {
        int pk = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_3, 2, TileKind.Colored.ALL.get(1));
        int result = PkPatterns.withAddedTiles(pk, TileDestination.Pattern.PATTERN_3, 1, TileKind.Colored.ALL.get(1));
        assertEquals(3, PkPatterns.size(result, TileDestination.Pattern.PATTERN_3));
    }

    @Test
    void withAddedTilesDoesNotAffectOtherLines() {
        int result = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_3, 2, TileKind.Colored.ALL.get(1));
        assertEquals(0, PkPatterns.size(result, TileDestination.Pattern.PATTERN_1));
        assertEquals(0, PkPatterns.size(result, TileDestination.Pattern.PATTERN_2));
        assertEquals(0, PkPatterns.size(result, TileDestination.Pattern.PATTERN_4));
        assertEquals(0, PkPatterns.size(result, TileDestination.Pattern.PATTERN_5));
    }

    @Test
    void withAddedTilesAllColors() {
        for (TileKind.Colored c : TileKind.Colored.ALL) {
            int result = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_2, 1, c);
            assertEquals(c, PkPatterns.color(result, TileDestination.Pattern.PATTERN_2));
        }
    }

    @Test
    void withAddedTilesWorksOnRandomValidInputs() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);

        for (int k = 0; k < 1000; k += 1) {
            PatternsData data = randomPatternsData(rng);
            int pk = data.pk();

            TileDestination.Pattern chosen = null;
            for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
                if (data.sizes()[line.index()] < line.capacity()) {
                    chosen = line;
                    break;
                }
            }

            if (chosen == null) continue; // cas rare : toutes les lignes pleines

            int i = chosen.index();
            int oldSize = data.sizes()[i];
            int remaining = chosen.capacity() - oldSize;
            int tileCount = rng.nextInt(1, remaining + 1);
            TileKind.Colored color = (oldSize == 0)
                    ? TileKind.Colored.ALL.get(rng.nextInt(TileKind.Colored.COUNT))
                    : data.colors()[i];

            int actual = PkPatterns.withAddedTiles(pk, chosen, tileCount, color);

            for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
                int j = line.index();
                if (line == chosen) {
                    assertEquals(oldSize + tileCount, PkPatterns.size(actual, line));
                    assertEquals(color, PkPatterns.color(actual, line));
                } else {
                    assertEquals(data.sizes()[j], PkPatterns.size(actual, line));
                    if (data.sizes()[j] > 0) {
                        assertEquals(data.colors()[j], PkPatterns.color(actual, line));
                    }
                }
            }
        }
    }

    @Test
    void withAddedTilesRejectsWrongColorWhenAssertionsAreEnabled() {
        assumeTrue(PkPatterns.class.desiredAssertionStatus());

        int pk = PkPatterns.withAddedTiles(
                PkPatterns.EMPTY,
                TileDestination.Pattern.PATTERN_3,
                1,
                TileKind.Colored.B
        );

        assertThrows(AssertionError.class,
                () -> PkPatterns.withAddedTiles(pk, TileDestination.Pattern.PATTERN_3, 1, TileKind.Colored.C));
    }

    @Test
    void withAddedTilesRejectsOverflowWhenAssertionsAreEnabled() {
        assumeTrue(PkPatterns.class.desiredAssertionStatus());

        int pk = PkPatterns.withAddedTiles(
                PkPatterns.EMPTY,
                TileDestination.Pattern.PATTERN_2,
                2,
                TileKind.Colored.A
        );

        assertThrows(AssertionError.class,
                () -> PkPatterns.withAddedTiles(pk, TileDestination.Pattern.PATTERN_2, 1, TileKind.Colored.A));
    }

    // ───────────────────────── withEmptyLine ─────────────────────────

    @Test
    void withEmptyLineOnFilledLine() {
        int pk = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_1, 1, TileKind.Colored.ALL.get(0));
        int result = PkPatterns.withEmptyLine(pk, TileDestination.Pattern.PATTERN_1);
        assertEquals(0, PkPatterns.size(result, TileDestination.Pattern.PATTERN_1));
    }

    @Test
    void withEmptyLineDoesNotAffectOtherLines() {
        int pk = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_1, 1, TileKind.Colored.ALL.get(0));
        pk = PkPatterns.withAddedTiles(pk, TileDestination.Pattern.PATTERN_2, 2, TileKind.Colored.ALL.get(1));
        int result = PkPatterns.withEmptyLine(pk, TileDestination.Pattern.PATTERN_1);
        assertEquals(2, PkPatterns.size(result, TileDestination.Pattern.PATTERN_2));
        assertEquals(TileKind.Colored.ALL.get(1), PkPatterns.color(result, TileDestination.Pattern.PATTERN_2));
    }

    @Test
    void withEmptyLineOnAlreadyEmpty() {
        int result = PkPatterns.withEmptyLine(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_3);
        assertEquals(PkPatterns.EMPTY, result);
    }

    @Test
    void withEmptyLineWorksOnRandomPatterns() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);

        for (int k = 0; k < 1000; k += 1) {
            PatternsData data = randomPatternsData(rng);
            TileDestination.Pattern chosen = TileDestination.Pattern.ALL.get(rng.nextInt(TileDestination.Pattern.COUNT));

            int actual = PkPatterns.withEmptyLine(data.pk(), chosen);

            for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
                int i = line.index();
                if (line == chosen) {
                    assertEquals(0, PkPatterns.size(actual, line));
                } else {
                    assertEquals(data.sizes()[i], PkPatterns.size(actual, line));
                    if (data.sizes()[i] > 0) {
                        assertEquals(data.colors()[i], PkPatterns.color(actual, line));
                    }
                }
            }
        }
    }

    // ───────────────────────── asPkTileSet ─────────────────────────

    @Test
    void asPkTileSetEmpty() {
        assertEquals(PkTileSet.EMPTY, PkPatterns.asPkTileSet(PkPatterns.EMPTY));
    }

    @Test
    void asPkTileSetOneTile() {
        int pk = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_1, 1, TileKind.Colored.ALL.get(0));
        int ts = PkPatterns.asPkTileSet(pk);
        assertEquals(1, PkTileSet.countOf(ts, TileKind.Colored.ALL.get(0)));
    }

    @Test
    void asPkTileSetMultipleLines() {
        int pk = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_1, 1, TileKind.Colored.ALL.get(0));
        pk = PkPatterns.withAddedTiles(pk, TileDestination.Pattern.PATTERN_2, 2, TileKind.Colored.ALL.get(0));
        int ts = PkPatterns.asPkTileSet(pk);
        assertEquals(3, PkTileSet.countOf(ts, TileKind.Colored.ALL.get(0)));
    }

    @Test
    void asPkTileSetDifferentColors() {
        int pk = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_1, 1, TileKind.Colored.ALL.get(0));
        pk = PkPatterns.withAddedTiles(pk, TileDestination.Pattern.PATTERN_2, 2, TileKind.Colored.ALL.get(1));
        int ts = PkPatterns.asPkTileSet(pk);
        assertEquals(1, PkTileSet.countOf(ts, TileKind.Colored.ALL.get(0)));
        assertEquals(2, PkTileSet.countOf(ts, TileKind.Colored.ALL.get(1)));
    }

    @Test
    void asPkTileSetWorksOnRandomPatterns() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);

        for (int k = 0; k < 1000; k += 1) {
            PatternsData data = randomPatternsData(rng);
            int expected = oracleAsPkTileSet(data);
            int actual = PkPatterns.asPkTileSet(data.pk());
            assertEquals(expected, actual);
        }
    }

    // ───────────────────────── toString ─────────────────────────

    @Test
    void toStringEmpty() {
        assertEquals("[., .., ..., ...., .....]", PkPatterns.toString(PkPatterns.EMPTY));
    }

    @Test
    void toStringOneTileOnLine1() {
        int pk = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_1, 1, TileKind.Colored.ALL.get(0));
        assertEquals("[A, .., ..., ...., .....]", PkPatterns.toString(pk));
    }

    @Test
    void toStringFullLine1() {
        int pk = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_1, 1, TileKind.Colored.ALL.get(2));
        assertEquals("[C, .., ..., ...., .....]", PkPatterns.toString(pk));
    }

    @Test
    void toStringPartialLine2() {
        int pk = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_2, 1, TileKind.Colored.ALL.get(0));
        assertEquals("[., A., ..., ...., .....]", PkPatterns.toString(pk));
    }

    @Test
    void toStringMultipleLines() {
        int pk = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_1, 1, TileKind.Colored.ALL.get(2));
        pk = PkPatterns.withAddedTiles(pk, TileDestination.Pattern.PATTERN_2, 2, TileKind.Colored.ALL.get(0));
        pk = PkPatterns.withAddedTiles(pk, TileDestination.Pattern.PATTERN_3, 3, TileKind.Colored.ALL.get(0));
        pk = PkPatterns.withAddedTiles(pk, TileDestination.Pattern.PATTERN_4, 3, TileKind.Colored.ALL.get(4));
        assertEquals("[C, AA, AAA, EEE., .....]", PkPatterns.toString(pk));
    }

    @Test
    void toStringAllFull() {
        int pk = PkPatterns.withAddedTiles(PkPatterns.EMPTY, TileDestination.Pattern.PATTERN_1, 1, TileKind.Colored.ALL.get(0));
        pk = PkPatterns.withAddedTiles(pk, TileDestination.Pattern.PATTERN_2, 2, TileKind.Colored.ALL.get(1));
        pk = PkPatterns.withAddedTiles(pk, TileDestination.Pattern.PATTERN_3, 3, TileKind.Colored.ALL.get(2));
        pk = PkPatterns.withAddedTiles(pk, TileDestination.Pattern.PATTERN_4, 4, TileKind.Colored.ALL.get(3));
        pk = PkPatterns.withAddedTiles(pk, TileDestination.Pattern.PATTERN_5, 5, TileKind.Colored.ALL.get(4));
        assertEquals("[A, BB, CCC, DDDD, EEEEE]", PkPatterns.toString(pk));
    }

    @Test
    void toStringWorksOnRandomPatterns() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);

        for (int k = 0; k < 1000; k += 1) {
            PatternsData data = randomPatternsData(rng);
            assertEquals(oracleToString(data), PkPatterns.toString(data.pk()));
        }
    }
}