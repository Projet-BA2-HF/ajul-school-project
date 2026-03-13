package ch.epfl.ajul.gamestate.packed;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class OurPkIntSet32Test {

    private static void assertAssertionsEnabled() {
        boolean enabled = false;
        assert enabled = true;
        assertTrue(enabled, "Assertions must be enabled (-ea) to run these tests correctly.");
    }

    @Test
    void containsOnDirectNamedValues() {
        int empty = 0b0000;
        int onlyBit0 = 0b0001;
        int onlyBit1 = 0b0010;
        int bits01 = 0b0011;
        int bits13 = 0b1010;
        int allLow4 = 0b1111;

        assertFalse(PkIntSet32.contains(empty, 0));
        assertFalse(PkIntSet32.contains(empty, 1));
        assertFalse(PkIntSet32.contains(empty, 2));
        assertFalse(PkIntSet32.contains(empty, 3));

        assertTrue(PkIntSet32.contains(onlyBit0, 0));
        assertFalse(PkIntSet32.contains(onlyBit0, 1));
        assertFalse(PkIntSet32.contains(onlyBit0, 2));
        assertFalse(PkIntSet32.contains(onlyBit0, 3));

        assertFalse(PkIntSet32.contains(onlyBit1, 0));
        assertTrue(PkIntSet32.contains(onlyBit1, 1));
        assertFalse(PkIntSet32.contains(onlyBit1, 2));
        assertFalse(PkIntSet32.contains(onlyBit1, 3));

        assertTrue(PkIntSet32.contains(bits01, 0));
        assertTrue(PkIntSet32.contains(bits01, 1));
        assertFalse(PkIntSet32.contains(bits01, 2));
        assertFalse(PkIntSet32.contains(bits01, 3));

        assertFalse(PkIntSet32.contains(bits13, 0));
        assertTrue(PkIntSet32.contains(bits13, 1));
        assertFalse(PkIntSet32.contains(bits13, 2));
        assertTrue(PkIntSet32.contains(bits13, 3));

        assertTrue(PkIntSet32.contains(allLow4, 0));
        assertTrue(PkIntSet32.contains(allLow4, 1));
        assertTrue(PkIntSet32.contains(allLow4, 2));
        assertTrue(PkIntSet32.contains(allLow4, 3));
    }

    @Test
    void containsAllOnDirectNamedValues() {
        int empty = 0b0000;
        int onlyBit0 = 0b0001;
        int onlyBit1 = 0b0010;
        int bits01 = 0b0011;
        int bits13 = 0b1010;
        int allLow4 = 0b1111;

        assertTrue(PkIntSet32.containsAll(empty, empty));

        assertTrue(PkIntSet32.containsAll(onlyBit0, empty));
        assertTrue(PkIntSet32.containsAll(bits01, empty));
        assertTrue(PkIntSet32.containsAll(allLow4, empty));

        assertTrue(PkIntSet32.containsAll(bits01, onlyBit0));
        assertTrue(PkIntSet32.containsAll(bits01, onlyBit1));
        assertTrue(PkIntSet32.containsAll(bits01, bits01));

        assertTrue(PkIntSet32.containsAll(allLow4, onlyBit0));
        assertTrue(PkIntSet32.containsAll(allLow4, onlyBit1));
        assertTrue(PkIntSet32.containsAll(allLow4, bits01));
        assertTrue(PkIntSet32.containsAll(allLow4, bits13));
        assertTrue(PkIntSet32.containsAll(allLow4, allLow4));

        assertFalse(PkIntSet32.containsAll(empty, onlyBit0));
        assertFalse(PkIntSet32.containsAll(onlyBit0, onlyBit1));
        assertFalse(PkIntSet32.containsAll(onlyBit1, onlyBit0));
        assertFalse(PkIntSet32.containsAll(bits01, bits13));
        assertFalse(PkIntSet32.containsAll(bits13, bits01));
    }

    @Test
    void addOnDirectNamedValues() {
        int empty = 0b0000;
        int onlyBit0 = 0b0001;
        int onlyBit1 = 0b0010;
        int bits01 = 0b0011;
        int bits13 = 0b1010;

        assertEquals(0b0001, PkIntSet32.add(empty, 0));
        assertEquals(0b0010, PkIntSet32.add(empty, 1));
        assertEquals(0b0100, PkIntSet32.add(empty, 2));
        assertEquals(0b1000, PkIntSet32.add(empty, 3));

        assertEquals(0b0011, PkIntSet32.add(onlyBit0, 1));
        assertEquals(0b0011, PkIntSet32.add(onlyBit1, 0));

        assertEquals(0b0011, PkIntSet32.add(bits01, 0));
        assertEquals(0b0011, PkIntSet32.add(bits01, 1));
        assertEquals(0b0111, PkIntSet32.add(bits01, 2));
        assertEquals(0b1011, PkIntSet32.add(bits01, 3));

        assertEquals(0b1011, PkIntSet32.add(bits13, 0));
        assertEquals(0b1010, PkIntSet32.add(bits13, 1));
        assertEquals(0b1110, PkIntSet32.add(bits13, 2));
        assertEquals(0b1010, PkIntSet32.add(bits13, 3));
    }

    @Test
    void removeOnDirectNamedValues() {
        int empty = 0b0000;
        int onlyBit0 = 0b0001;
        int onlyBit1 = 0b0010;
        int bits01 = 0b0011;
        int bits13 = 0b1010;
        int allLow4 = 0b1111;

        assertEquals(0b0000, PkIntSet32.remove(empty, 0));
        assertEquals(0b0000, PkIntSet32.remove(empty, 1));
        assertEquals(0b0000, PkIntSet32.remove(empty, 2));
        assertEquals(0b0000, PkIntSet32.remove(empty, 3));

        assertEquals(0b0000, PkIntSet32.remove(onlyBit0, 0));
        assertEquals(0b0001, PkIntSet32.remove(onlyBit0, 1));

        assertEquals(0b0000, PkIntSet32.remove(onlyBit1, 1));
        assertEquals(0b0010, PkIntSet32.remove(onlyBit1, 0));

        assertEquals(0b0010, PkIntSet32.remove(bits01, 0));
        assertEquals(0b0001, PkIntSet32.remove(bits01, 1));
        assertEquals(0b0011, PkIntSet32.remove(bits01, 2));

        assertEquals(0b1010, PkIntSet32.remove(bits13, 0));
        assertEquals(0b1000, PkIntSet32.remove(bits13, 1));
        assertEquals(0b1010, PkIntSet32.remove(bits13, 2));
        assertEquals(0b0010, PkIntSet32.remove(bits13, 3));

        assertEquals(0b1110, PkIntSet32.remove(allLow4, 0));
        assertEquals(0b1101, PkIntSet32.remove(allLow4, 1));
        assertEquals(0b1011, PkIntSet32.remove(allLow4, 2));
        assertEquals(0b0111, PkIntSet32.remove(allLow4, 3));
    }

    @Test
    void directEdgeBit31Values() {
        int empty = 0;
        int onlyBit31 = 1 << 31;
        int bits0And31 = (1 << 0) | (1 << 31);

        assertTrue(PkIntSet32.contains(onlyBit31, 31));
        assertFalse(PkIntSet32.contains(onlyBit31, 0));

        assertEquals(onlyBit31, PkIntSet32.add(empty, 31));
        assertEquals(bits0And31, PkIntSet32.add(1, 31));

        assertEquals(0, PkIntSet32.remove(onlyBit31, 31));
        assertEquals(1 << 31, PkIntSet32.remove(bits0And31, 0));
        assertEquals(1, PkIntSet32.remove(bits0And31, 31));

        assertTrue(PkIntSet32.containsAll(bits0And31, onlyBit31));
        assertFalse(PkIntSet32.containsAll(1, onlyBit31));
    }

    @Test
    void emptyContainsNoBit() {
        for (int i = 0; i < Integer.SIZE; i++) {
            assertFalse(PkIntSet32.contains(PkIntSet32.EMPTY, i));
        }
    }

    @Test
    void containsWorksForEverySingleBitIncludingEdges() {
        for (int i = 0; i < Integer.SIZE; i++) {
            int set = 1 << i;

            for (int j = 0; j < Integer.SIZE; j++) {
                assertEquals(i == j, PkIntSet32.contains(set, j));
            }
        }
    }

    @Test
    void containsWorksForRepresentativePatterns() {
        int[] sets = {
                0,
                1,
                1 << 31,
                (1 << 0) | (1 << 31),
                0b101010,
                0b111111,
                0x0F0F0F0F,
                0xF0F0F0F0,
                0xAAAAAAAA,
                0x55555555,
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                -1
        };

        for (int set : sets) {
            for (int i = 0; i < Integer.SIZE; i++) {
                boolean expected = ((set >>> i) & 1) == 1;
                assertEquals(expected, PkIntSet32.contains(set, i));
            }
        }
    }

//    @Test
//    void containsRejectsInvalidIndices() {
//        assertAssertionsEnabled();
//
//        assertThrows(AssertionError.class, () -> PkIntSet32.contains(0, -1));
//        assertThrows(AssertionError.class, () -> PkIntSet32.contains(0, 32));
//        assertThrows(AssertionError.class, () -> PkIntSet32.contains(0, Integer.MIN_VALUE));
//        assertThrows(AssertionError.class, () -> PkIntSet32.contains(0, Integer.MAX_VALUE));
//    }

    @Test
    void containsAllBasicCases() {
        assertTrue(PkIntSet32.containsAll(0, 0));
        assertTrue(PkIntSet32.containsAll(0b111, 0));
        assertTrue(PkIntSet32.containsAll(0b111, 0b001));
        assertTrue(PkIntSet32.containsAll(0b111, 0b010));
        assertTrue(PkIntSet32.containsAll(0b111, 0b100));
        assertTrue(PkIntSet32.containsAll(0b111, 0b111));

        assertFalse(PkIntSet32.containsAll(0b001, 0b010));
        assertFalse(PkIntSet32.containsAll(0b010, 0b001));
        assertFalse(PkIntSet32.containsAll(0b011, 0b100));
        assertFalse(PkIntSet32.containsAll(0b101, 0b111));
        assertFalse(PkIntSet32.containsAll(0, 1));
    }

    @Test
    void containsAllWorksOnEdgeBits() {
        int low = 1 << 0;
        int high = 1 << 31;
        int both = low | high;

        assertTrue(PkIntSet32.containsAll(both, low));
        assertTrue(PkIntSet32.containsAll(both, high));
        assertTrue(PkIntSet32.containsAll(both, both));
        assertTrue(PkIntSet32.containsAll(both, 0));

        assertFalse(PkIntSet32.containsAll(low, high));
        assertFalse(PkIntSet32.containsAll(high, low));
        assertFalse(PkIntSet32.containsAll(0, both));
    }

    @Test
    void containsAllMatchesBitwiseDefinitionOnManyPatterns() {
        int[] sets = {
                0,
                1,
                2,
                3,
                4,
                7,
                15,
                16,
                31,
                63,
                127,
                255,
                256,
                1023,
                0xAAAAAAAA,
                0x55555555,
                0x0F0F0F0F,
                0xF0F0F0F0,
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                -1
        };

        for (int a : sets) {
            for (int b : sets) {
                boolean expected = (a & b) == b;
                assertEquals(expected, PkIntSet32.containsAll(a, b));
            }
        }
    }

    @Test
    void addWorksFromEmptyForEveryBit() {
        for (int i = 0; i < Integer.SIZE; i++) {
            int set = PkIntSet32.add(PkIntSet32.EMPTY, i);

            for (int j = 0; j < Integer.SIZE; j++) {
                assertEquals(i == j, PkIntSet32.contains(set, j));
            }
        }
    }

    @Test
    void addSetsRequestedBit() {
        int[] bases = {
                0,
                1,
                1 << 31,
                (1 << 0) | (1 << 31),
                0xAAAAAAAA,
                0x55555555,
                0x0F0F0F0F,
                0xF0F0F0F0,
                -1
        };

        for (int base : bases) {
            for (int i = 0; i < Integer.SIZE; i++) {
                int result = PkIntSet32.add(base, i);
                assertTrue(PkIntSet32.contains(result, i));
            }
        }
    }

    @Test
    void addIsIdempotent() {
        for (int i = 0; i < Integer.SIZE; i++) {
            int once = PkIntSet32.add(0, i);
            int twice = PkIntSet32.add(once, i);
            assertEquals(once, twice);
        }
    }

    @Test
    void addPreservesOtherBits() {
        int[] bases = {
                0,
                1,
                1 << 31,
                (1 << 0) | (1 << 31),
                0xAAAAAAAA,
                0x55555555,
                0x0F0F0F0F
        };

        for (int base : bases) {
            for (int i = 0; i < Integer.SIZE; i++) {
                int result = PkIntSet32.add(base, i);

                for (int j = 0; j < Integer.SIZE; j++) {
                    boolean expected = (j == i) || (((base >>> j) & 1) == 1);
                    assertEquals(expected, PkIntSet32.contains(result, j));
                }
            }
        }
    }

//    @Test
//    void addRejectsInvalidIndices() {
//        assertAssertionsEnabled();
//
//        assertThrows(AssertionError.class, () -> PkIntSet32.add(0, -1));
//        assertThrows(AssertionError.class, () -> PkIntSet32.add(0, 32));
//        assertThrows(AssertionError.class, () -> PkIntSet32.add(0, Integer.MIN_VALUE));
//        assertThrows(AssertionError.class, () -> PkIntSet32.add(0, Integer.MAX_VALUE));
//    }

    @Test
    void removeWorksForEverySingleBitSet() {
        for (int i = 0; i < Integer.SIZE; i++) {
            int set = 1 << i;
            int result = PkIntSet32.remove(set, i);
            assertEquals(0, result);

            for (int j = 0; j < Integer.SIZE; j++) {
                assertFalse(PkIntSet32.contains(result, j));
            }
        }
    }

    @Test
    void removeClearsRequestedBit() {
        int[] bases = {
                -1,
                0xAAAAAAAA,
                0x55555555,
                0x0F0F0F0F,
                0xF0F0F0F0,
                (1 << 0) | (1 << 31)
        };

        for (int base : bases) {
            for (int i = 0; i < Integer.SIZE; i++) {
                int result = PkIntSet32.remove(base, i);
                assertFalse(PkIntSet32.contains(result, i));
            }
        }
    }

    @Test
    void removeAbsentBitDoesNothing() {
        int[] bases = {
                0,
                1,
                1 << 31,
                (1 << 0) | (1 << 31),
                0xAAAAAAAA,
                0x55555555,
                0x0F0F0F0F
        };

        for (int base : bases) {
            for (int i = 0; i < Integer.SIZE; i++) {
                if (!PkIntSet32.contains(base, i)) {
                    assertEquals(base, PkIntSet32.remove(base, i),
                            "Removing absent bit " + i + " should not change the set.");
                }
            }
        }
    }

    @Test
    void removePreservesOtherBits() {
        int[] bases = {
                -1,
                0xAAAAAAAA,
                0x55555555,
                0x0F0F0F0F,
                0xF0F0F0F0,
                (1 << 0) | (1 << 31)
        };

        for (int base : bases) {
            for (int i = 0; i < Integer.SIZE; i++) {
                int result = PkIntSet32.remove(base, i);

                for (int j = 0; j < Integer.SIZE; j++) {
                    boolean expected = (j != i) && (((base >>> j) & 1) == 1);
                    assertEquals(expected, PkIntSet32.contains(result, j));
                }
            }
        }
    }

    /*void removeRejectsInvalidIndices() {
        assertAssertionsEnabled();

        assertThrows(AssertionError.class, () -> PkIntSet32.remove(0, -1));
        assertThrows(AssertionError.class, () -> PkIntSet32.remove(0, 32));
        assertThrows(AssertionError.class, () -> PkIntSet32.remove(0, Integer.MIN_VALUE));
        assertThrows(AssertionError.class, () -> PkIntSet32.remove(0, Integer.MAX_VALUE));
    }*/

    @Test
    void addThenContainsThenRemoveRoundTripMatchesBitwiseDefinition() {
        int[] bases = {
                0,
                1,
                1 << 31,
                (1 << 0) | (1 << 31),
                0xAAAAAAAA,
                0x55555555,
                0x0F0F0F0F,
                0xF0F0F0F0
        };

        for (int base : bases) {
            for (int i = 0; i < Integer.SIZE; i++) {
                int added = PkIntSet32.add(base, i);
                assertTrue(PkIntSet32.contains(added, i));

                int removed = PkIntSet32.remove(added, i);
                int expected = base & ~(1 << i);
                assertEquals(expected, removed);
            }
        }
    }

    @Test
    void addAndRemoveMatchDirectBitwiseFormulasOnManyPatterns() {
        int[] sets = {
                0,
                1,
                2,
                3,
                7,
                15,
                31,
                63,
                127,
                255,
                0xAAAAAAAA,
                0x55555555,
                0x0F0F0F0F,
                0xF0F0F0F0,
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                -1
        };

        for (int set : sets) {
            for (int i = 0; i < Integer.SIZE; i++) {
                assertEquals(set | (1 << i), PkIntSet32.add(set, i));
                assertEquals(set & ~(1 << i), PkIntSet32.remove(set, i));
            }
        }
    }

    @Test
    void bruteForceAllEightBitPatternsShiftedAcrossInt() {
        int[] shifts = {0, 8, 16, 24};

        for (int pattern = 0; pattern < 256; pattern++) {
            for (int shift : shifts) {
                int set = pattern << shift;

                for (int i = 0; i < Integer.SIZE; i++) {
                    boolean expectedContains = ((set >>> i) & 1) == 1;
                    assertEquals(expectedContains, PkIntSet32.contains(set, i));

                    int added = PkIntSet32.add(set, i);
                    assertTrue(PkIntSet32.contains(added, i));

                    int removed = PkIntSet32.remove(set, i);
                    assertFalse(PkIntSet32.contains(removed, i));
                }
            }
        }
    }

    @Test
    void containsAllOnAllEightBitPatternsShiftedAcrossInt() {
        int[] shifts = {0, 8, 16, 24};

        for (int patternA = 0; patternA < 256; patternA++) {
            for (int patternB = 0; patternB < 256; patternB++) {
                for (int shift : shifts) {
                    int a = patternA << shift;
                    int b = patternB << shift;
                    boolean expected = (a & b) == b;

                    assertEquals(expected, PkIntSet32.containsAll(a, b));
                }
            }
        }
    }

    @Test
    void largeDeterministicRandomCampaign() {
        Random rng = new Random(0x5EEDBEEF);

        for (int t = 0; t < 200_000; t++) {
            int set = rng.nextInt();
            int other = rng.nextInt();
            int i = rng.nextInt(Integer.SIZE);

            assertEquals(((set >>> i) & 1) == 1, PkIntSet32.contains(set, i));
            assertEquals((set & other) == other, PkIntSet32.containsAll(set, other));
            assertEquals(set | (1 << i), PkIntSet32.add(set, i));
            assertEquals(set & ~(1 << i), PkIntSet32.remove(set, i));
        }
    }
}