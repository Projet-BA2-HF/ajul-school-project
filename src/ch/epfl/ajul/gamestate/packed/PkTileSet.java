package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileKind;

import java.util.Arrays;
import java.util.StringJoiner;
import java.util.random.RandomGenerator;

/// Contains static methods for manipulating tile sets packed into {@code int} values.
/// Each tile kind occupies a 6-bit counter: bits 0–5 for color A, bits 6–11 for color B,
/// bits 12–17 for color C, bits 18–23 for color D, bits 24–29 for color E,
/// and bit 30 for the first player marker.
/// Bit 31 is always 0.
/// The zero separator bits between counters enable SWAR-style parallel arithmetic.
public final class PkTileSet {

    /// The packed representation of the empty tile set (no tiles of any kind).
    public static final int EMPTY = 0;

    /// The packed representation of the full tile set:
    /// 20 tiles of each colored kind and 1 first player marker.
    public static final int FULL = computeFull();

    /// The packed representation of the full colored tile set:
    /// 20 tiles of each colored kind and no first player marker.
    public static final int FULL_COLORED = remove(FULL, TileKind.FIRST_PLAYER_MARKER);

    /// The number of bits used per tile-kind counter (6).
    private static final int COLOR_SHIFT = 6;

    /// The bit mask for extracting a single tile-kind counter (63).
    private static final int MASK = (1 << COLOR_SHIFT) - 1;

    private static int computeFull(){
        int fullValue = EMPTY;
        for (TileKind tk : TileKind.ALL){
            fullValue = union(fullValue, of(tk.tilesCount(), tk));
        }
        return fullValue;
    }

    /// Returns a packed tile set containing exactly {@code count} tiles of the given kind.
    ///
    /// @param count    the number of tiles of the given kind
    /// @param tileKind the kind of tile
    /// @return the packed tile set with {@code count} tiles of {@code tileKind}
    public static int of(int count, TileKind tileKind){
        return count << (tileKind.index() * 6);
    }

    /// Returns {@code true} if the given packed tile set contains no tiles.
    ///
    /// @param pkTileSet the packed tile set
    /// @return {@code true} iff {@code pkTileSet} is empty
    public static boolean isEmpty(int pkTileSet){
        assert isValid(pkTileSet);
        return pkTileSet == EMPTY;
    }

    /// Returns the total number of tiles in the given packed tile set.
    /// Uses a SWAR technique: a single addition produces five partial sums
    /// A+B, B+C, C+D, D+E, E+M which are then reduced to the final total.
    ///
    /// @param pkTileSet the packed tile set
    /// @return the number of tiles in {@code pkTileSet}
    public static int size(int pkTileSet) {
        assert isValid(pkTileSet);
        int sum = pkTileSet + (pkTileSet >>> COLOR_SHIFT);
        int AB = sum & MASK;
        int CD = (sum >>> (2 *COLOR_SHIFT)) & MASK;
        int EM = (sum >>> (4 *COLOR_SHIFT)) & MASK;
        return AB + CD + EM;
    }

    /// Returns the number of tiles of the given kind in the given packed tile set.
    ///
    /// @param pkTileSet the packed tile set
    /// @param tileKind  the kind of tile to count
    /// @return the count of {@code tileKind} tiles in {@code pkTileSet}
    public static int countOf(int pkTileSet, TileKind tileKind){
        int tileKindIndex = tileKind.index() * COLOR_SHIFT;
        int tileKindShift = pkTileSet >> tileKindIndex;
        return tileKindShift & MASK;
    }

    /// Returns the sub-set of the given packed tile set consisting solely of tiles
    /// of the given kind.
    ///
    /// @param pkTileSet the packed tile set
    /// @param tileKind  the kind of tile to extract
    /// @return the packed tile set containing only the {@code tileKind} tiles of {@code pkTileSet}
    public static int subsetOf(int pkTileSet, TileKind tileKind){
        assert isValid(pkTileSet);
        return of(countOf(pkTileSet,tileKind), tileKind);
    }

    /// Returns a packed tile set equal to {@code pkTileSet} with exactly one additional
    /// tile of the given kind.
    ///
    /// @param pkTileSet the packed tile set
    /// @param tileKind  the kind of tile to add
    /// @return the packed tile set with one more {@code tileKind} tile
    public static int add(int pkTileSet, TileKind tileKind){
        assert isValid(pkTileSet);
        int tileCounter = 1 << (tileKind.index() * COLOR_SHIFT);
        return union(pkTileSet, tileCounter);
    }

    /// Returns a packed tile set equal to {@code pkTileSet} with exactly one fewer
    /// tile of the given kind.
    ///
    /// @param pkTileSet the packed tile set
    /// @param tileKind  the kind of tile to remove
    /// @return the packed tile set with one fewer {@code tileKind} tile
    public static int remove(int pkTileSet, TileKind tileKind){
        assert isValid(pkTileSet);
        int tileCounter = 1 << (tileKind.index() * COLOR_SHIFT);
        return difference(pkTileSet, tileCounter);
    }

    /// Returns the union of two packed tile sets, i.e. the set whose counter for each
    /// tile kind is the sum of the two individual counters.
    ///
    /// @param pkTileSet1 the first packed tile set
    /// @param pkTileSet2 the second packed tile set
    /// @return the packed union of {@code pkTileSet1} and {@code pkTileSet2}
    public static int union(int pkTileSet1, int pkTileSet2){
        assert isValid(pkTileSet1) && isValid(pkTileSet2);
        return pkTileSet1 + pkTileSet2;
    }

    /// Returns the difference of two packed tile sets, i.e. the set whose counter for each
    /// tile kind is the counter of the first set minus that of the second.
    /// {@code pkTileSet2} must be a sub-set of {@code pkTileSet1}.
    ///
    /// @param pkTileSet1 the packed tile set to subtract from
    /// @param pkTileSet2 the packed tile set to subtract (must be a sub-set of {@code pkTileSet1})
    /// @return the packed difference of {@code pkTileSet1} and {@code pkTileSet2}
    public static int difference(int pkTileSet1, int pkTileSet2){
        assert isValid(pkTileSet1) && isValid(pkTileSet2);
        return pkTileSet1 - pkTileSet2;
    }

    /// Copies the colored tiles of the given packed tile set into {@code destination},
    /// ordered by color (A first, then B, …, E last).
    /// Returns the index of the first slot after the last written element.
    ///
    /// @param pkTileSet   the packed tile set whose colored tiles are copied
    /// @param destination the array into which the tiles are written, starting at index 0
    /// @return the index of the first unused slot in {@code destination}
    public static int copyColoredInto(int pkTileSet, TileKind.Colored[] destination) {
        assert isValid(pkTileSet);
        int tileCount = 0;
        for (TileKind.Colored tileColor : TileKind.Colored.ALL) {
            int colorCount = countOf(pkTileSet, tileColor);
            Arrays.fill(destination, tileCount, tileCount + colorCount, tileColor);
            tileCount += colorCount;
        }
        return tileCount;
    }

    /// Fills {@code destination} starting at {@code offset} with a random sample of the
    /// colored tiles in {@code pkTileSet}, using the reservoir sampling algorithm.
    /// The reservoir size equals {@code destination.length - offset}.
    /// Returns {@code offset} plus the total number of colored tiles in {@code pkTileSet}.
    ///
    /// @param pkTileSet       the packed tile set to sample from
    /// @param destination     the array into which the sample is written
    /// @param offset          the index in {@code destination} at which writing starts
    /// @param randomGenerator the random generator used by the reservoir sampling algorithm
    /// @return the sum of {@code offset} and the size of the colored part of {@code pkTileSet}
    public static int sampleColoredInto(int pkTileSet,
                                        TileKind.Colored[] destination,
                                        int offset,
                                        RandomGenerator randomGenerator) {
        assert isValid(pkTileSet);
        int currentIndex = offset;
        for (TileKind.Colored color : TileKind.Colored.ALL) {
            int colorCount = countOf(pkTileSet, color);
            for (int j = 0; j < colorCount; j++) {
                if (currentIndex < destination.length) {
                    destination [currentIndex] = color;
                } else {
                    int randomIndex = randomGenerator.nextInt(offset,
                            currentIndex + 1);
                    if (randomIndex < destination.length) {
                        destination[randomIndex] = color;
                    }
                }
                currentIndex++;
            }
        }
        return currentIndex;
    }

    /// Returns a textual representation of the given packed tile set.
    /// Each tile kind with a non-zero count appears as {@code count*KIND},
    /// elements are separated by commas and enclosed in braces,
    /// e.g. {@code {20*A,20*B,20*C,20*D,20*E,1*FIRST_PLAYER_MARKER}}.
    ///
    /// @param pkTileSet the packed tile set
    /// @return the string representation of {@code pkTileSet}
    public static String toString(int pkTileSet){
        assert isValid(pkTileSet);
        StringJoiner outputString = new StringJoiner(",", "{", "}");
        for(TileKind.Colored tileColor : TileKind.Colored.ALL){
            int colorCount = countOf(pkTileSet, tileColor);
            if (colorCount == 0) continue;
            outputString.add(colorCount + "*" + tileColor);
        }
        int markerCount = countOf(pkTileSet, TileKind.FIRST_PLAYER_MARKER);
        if(markerCount > 0){
            outputString.add(countOf(pkTileSet,
                    TileKind.FIRST_PLAYER_MARKER) + "*" + TileKind.FIRST_PLAYER_MARKER);
        }
        return outputString.toString();
    }

    private static boolean isValid(int pkTileSet) {
        for (TileKind tileKind : TileKind.ALL) {
            if (countOf(pkTileSet, tileKind) > tileKind.tilesCount()) return false;
        }
        return true;
    }
}
