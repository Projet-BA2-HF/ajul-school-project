package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;

import java.util.StringJoiner;

/// Contains static methods for manipulating the content of a player's five pattern lines
/// packed into an {@code int} value.
///
/// The packing format is as follows:
/// <ul>
///   <li>bits 0–2: number of tiles on pattern line 1 (0–1)</li>
///   <li>bits 3–5: color index of those tiles, or 0 if the line is empty</li>
///   <li>bits 6–8: number of tiles on pattern line 2 (0–2)</li>
///   <li>bits 9–11: color index of those tiles, or 0 if the line is empty</li>
///   <li>… and so on for lines 3–5</li>
///   <li>bits 30–31: always 0</li>
/// </ul>
public final class PkPatterns {

    /// The packed representation of empty pattern lines (no tiles on any line).
    public static final int EMPTY = 0;

    private static final int SIZE_BITS = 3;
    private static final int COLOR_BITS = 3;

    private static final int BITS_PER_LINE = SIZE_BITS + COLOR_BITS;

    private static final int SIZE_MASK = (1 << SIZE_BITS) - 1;
    private static final int LINE_MASK = (1 << BITS_PER_LINE) - 1;

    private static final int COLOR_SHIFT = SIZE_BITS;

    /// Returns the number of tiles present on the given pattern line.
    ///
    /// @param pkPatterns the packed pattern lines
    /// @param line       the pattern line whose tile count is queried
    /// @return the number of tiles on {@code line}
    public static int size(int pkPatterns, TileDestination.Pattern line) {
        int offset = line.index() * BITS_PER_LINE;
        return (pkPatterns >> offset) & SIZE_MASK;
    }

    /// Returns the color of the tiles present on the given pattern line.
    /// The line must not be empty; behavior is unspecified (assertion error) if it is.
    ///
    /// @param pkPatterns the packed pattern lines
    /// @param line       the pattern line whose color is queried
    /// @return the color of the tiles on {@code line}
    public static TileKind.Colored color(int pkPatterns, TileDestination.Pattern line) {
        assert size(pkPatterns, line) != 0;
        int offset = line.index() * BITS_PER_LINE;
        int colorIndex = (pkPatterns >> (offset + COLOR_SHIFT)) & SIZE_MASK;
        return TileKind.Colored.ALL.get(colorIndex);
    }

    /// Returns {@code true} iff the given pattern line is full,
    /// i.e. contains the maximum number of tiles it can hold.
    ///
    /// @param pkPatterns the packed pattern lines
    /// @param line       the pattern line to test
    /// @return {@code true} iff {@code line} is full
    public static boolean isFull(int pkPatterns, TileDestination.Pattern line) {
        return size(pkPatterns, line) == line.capacity();
    }

    /// Returns {@code true} iff the given pattern line can accept tiles of the given color,
    /// i.e. the line is either empty or already contains tiles of that color.
    /// This is independent of whether the line is full.
    ///
    /// @param pkPatterns the packed pattern lines
    /// @param line       the pattern line to test
    /// @param color      the color to check
    /// @return {@code true} iff {@code line} can contain tiles of {@code color}
    public static boolean canContain(int pkPatterns,
                                     TileDestination.Pattern line,
                                     TileKind.Colored color) {

        return size(pkPatterns, line) == 0 || color(pkPatterns, line) == color;
    }

    /// Returns packed pattern lines identical to {@code pkPatterns} but with
    /// {@code tileCount} tiles of {@code color} added to {@code line}.
    /// The line must be able to accept the tiles and the resulting size must not
    /// exceed the line's capacity; behavior is unspecified (assertion error) otherwise.
    ///
    /// @param pkPatterns the packed pattern lines
    /// @param line       the pattern line to which tiles are added
    /// @param tileCount  the number of tiles to add
    /// @param color      the color of the tiles to add
    /// @return the updated packed pattern lines
    public static int withAddedTiles(int pkPatterns,
                                     TileDestination.Pattern line,
                                     int tileCount,
                                     TileKind.Colored color) {

        assert canContain(pkPatterns, line, color);
        assert 0 < size(pkPatterns, line) + tileCount
                && size(pkPatterns, line) + tileCount <= line.capacity();
        int offset = line.index() * BITS_PER_LINE;
        int newSize = ((pkPatterns >> offset) & SIZE_MASK) + tileCount;
        int newSpace = newSize | (color.index() << COLOR_SHIFT);

        return withEmptyLine(pkPatterns, line) | (newSpace << offset);
    }

    /// Returns packed pattern lines identical to {@code pkPatterns} but with
    /// the given pattern line cleared (set to empty).
    ///
    /// @param pkPatterns the packed pattern lines
    /// @param line       the pattern line to empty
    /// @return the updated packed pattern lines
    public static int withEmptyLine(int pkPatterns, TileDestination.Pattern line) {
        int offset = line.index() * BITS_PER_LINE;
        return pkPatterns & ~(LINE_MASK << offset);
    }

    /// Returns the packed tile set composed of all tiles present on all five pattern lines.
    ///
    /// @param pkPatterns the packed pattern lines
    /// @return the packed tile set of all tiles on the pattern lines
    public static int asPkTileSet(int pkPatterns) {
        int pkTileSet = PkTileSet.EMPTY;
        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
            int size = size(pkPatterns, line);
            if (size != 0) {
                pkTileSet = PkTileSet.union(
                        pkTileSet,
                        PkTileSet.of(size, color(pkPatterns, line))
                );
            }
        }
        return pkTileSet;
    }

    /// Returns a textual representation of the given packed pattern lines.
    /// The representation consists of five elements separated by {@code ", "} and enclosed
    /// in brackets. Each element shows the color letter repeated as many times as there are
    /// tiles on that line, followed by dots to pad to the line's capacity.
    /// For example: {@code [C, AA, AAA, EEE., .....]} .
    ///
    /// @param pkPatterns the packed pattern lines
    /// @return the string representation of {@code pkPatterns}
    public static String toString(int pkPatterns) {
        StringJoiner outputString = new StringJoiner(", ", "[", "]");

        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
            int count = size(pkPatterns, line);
            int capacity = line.capacity();
            String tiles = count == 0 ? "" :
                    color(pkPatterns, line).toString().repeat(count);
            String dots = ".".repeat(capacity - count);
            outputString.add(tiles + dots);
        }
        return outputString.toString();
    }
}