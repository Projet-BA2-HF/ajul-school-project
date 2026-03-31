package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;

import java.util.StringJoiner;

/// Contains static methods for manipulating the content of a player's wall packed into an
/// {@code int} value. The wall has {@value #WALL_HEIGHT} rows and {@value #WALL_WIDTH} columns
/// (25 cells in total). Cell occupancy is encoded bit by bit: bit {@code i} is 1 if and only if
/// cell {@code i} contains a tile, where cells are numbered 0–24 from left to right, top to bottom.
///
/// @author Hedi Fourati
/// @author Fahd Ben Ismail
public final class PkWall {

    /// The packed representation of an empty wall.
    public static final int EMPTY = 0;
    /// The width of the wall in cells (number of columns).
    public static final int WALL_WIDTH = 5;
    /// The height of the wall in cells (number of rows).
    public static final int WALL_HEIGHT = 5;


    // Bitmask selecting all cells of the first row (row 0).
    private static final int ROW0_MASK = (1 << WALL_WIDTH) - 1;

    // Bitmask selecting all cells of the first column (column 0).
    private static final int COLUMN0_MASK = 0b00001_00001_00001_00001_00001;

    // Returns the bitmask selecting all 5 cells of the wall that accept tiles of the given color.
    private static int colorMask(TileKind.Colored color) {
        return switch (color) {
            case A -> 0b10000_01000_00100_00010_00001;
            case B -> 0b00001_10000_01000_00100_00010;
            case C -> 0b00010_00001_10000_01000_00100;
            case D -> 0b00100_00010_00001_10000_01000;
            case E -> 0b01000_00100_00010_00001_10000;
        };
    }

    /// Returns the index, between 0 and 24 (inclusive), of the wall cell corresponding to
    /// pattern line {@code line} that accepts tiles of color {@code color}.
    ///
    /// @param line  the pattern line
    /// @param color the color of the tile accepted by the cell
    /// @return the cell index between 0 and 24
    public static int indexOf(TileDestination.Pattern line, TileKind.Colored colored){
        return (line.index() * WALL_WIDTH + column(line, colored));
    }

    /// Returns the column number, between 0 and 4 (inclusive), of the wall cell corresponding to
    /// pattern line {@code line} that accepts tiles of color {@code color}.
    ///
    /// @param line  the pattern line
    /// @param color the color of the tile accepted by the cell
    /// @return the column number between 0 and 4
    public static int column(TileDestination.Pattern line, TileKind.Colored color){
        return (line.index() + color.index()) % WALL_WIDTH;
    }

    /// Returns the color of the tile accepted by the wall cell at the given pattern line and column.
    ///
    /// @param line   the pattern line
    /// @param column the column number, between 0 and 4 (inclusive)
    /// @return the color accepted by that cell
    public static TileKind.Colored colorAt(TileDestination.Pattern line, int column){
        return TileKind.Colored.ALL.get((column - line.index() + WALL_WIDTH)%WALL_WIDTH);
    }

    /// Returns a packed wall identical to {@code pkWall} but with the cell corresponding to
    /// pattern line {@code line} and color {@code color} marked as occupied.
    ///
    /// @param pkWall the packed wall
    /// @param line   the pattern line of the cell to fill
    /// @param color  the color of the tile placed in that cell
    /// @return {@code pkWall} with the tile added at the corresponding cell
    public static int withTileAt(int pkWall,
                                 TileDestination.Pattern line,
                                 TileKind.Colored color){
        return PkIntSet32.add(pkWall, indexOf(line, color));
    }

    /// Returns {@code true} iff the cell corresponding to pattern line {@code line} and
    /// color {@code color} in the packed wall {@code pkWall} contains a tile.
    ///
    /// @param pkWall the packed wall
    /// @param line   the pattern line of the cell to check
    /// @param color  the color accepted by the cell to check
    /// @return {@code true} iff that cell contains a tile
    public static boolean hasTileAt(int pkWall,
                                    TileDestination.Pattern line,
                                    TileKind.Colored color){
        return PkIntSet32.contains(pkWall, indexOf(line, color));
    }

    /// Counts consecutive occupied cells starting from the neighbour of
    /// ({@code startLineIndex}, {@code startColumn}) in the direction
    /// ({@code deltaLine}, {@code deltaColumn}), stopping at the first empty cell
    /// or at the wall boundary.
    ///
    /// @param pkWall         the packed wall
    /// @param startLineIndex the row index of the reference cell, between 0 and 4 (inclusive)
    /// @param startColumn    the column of the reference cell, between 0 and 4 (inclusive)
    /// @param deltaLine      the row step (+1, 0, or -1)
    /// @param deltaColumn    the column step (+1, 0, or -1)
    /// @return the number of consecutive occupied cells in that direction
    private static int countInDirection(int pkWall,
                                        int startLineIndex,
                                        int startColumn,
                                        int deltaLine,
                                        int deltaColumn) {
        int count = 0;

        int lineIndex = startLineIndex + deltaLine;
        int column = startColumn + deltaColumn;

        while (0 <= lineIndex && lineIndex < WALL_HEIGHT
                && 0 <= column &&
                column < WALL_WIDTH &&
                hasTileAt(pkWall, TileDestination.Pattern.ALL.get(lineIndex),
                        colorAt(TileDestination.Pattern.ALL.get(lineIndex), column))) {

            lineIndex += deltaLine;
            column += deltaColumn;
            count++;

        }

        return count;
    }

    /// Returns the size of the horizontal group to which the tile of color {@code color}
    /// on pattern line {@code line} of the packed wall {@code pkWall} belongs.
    /// Returns 0 if that cell is empty.
    ///
    /// @param pkWall the packed wall
    /// @param line   the pattern line of the tile
    /// @param color  the color of the tile
    /// @return the horizontal group size, or 0 if the cell is empty
    public static int hGroupSize(int pkWall,
                                 TileDestination.Pattern line,
                                 TileKind.Colored color) {
        int lineIndex = line.index();
        int column = column(line, color);

        // Count occupied neighbouring tiles to the left and to the right.
        return 1 + countInDirection(pkWall, lineIndex, column, 0, -1)
                + countInDirection(pkWall, lineIndex, column, 0, +1);
    }

    /// Returns the size of the vertical group to which the tile of color {@code color}
    /// on pattern line {@code line} of the packed wall {@code pkWall} belongs.
    /// Returns 0 if that cell is empty.
    ///
    /// @param pkWall the packed wall
    /// @param line   the pattern line of the tile
    /// @param color  the color of the tile
    /// @return the vertical group size, or 0 if the cell is empty
    public static int vGroupSize(int pkWall,
                                 TileDestination.Pattern line,
                                 TileKind.Colored color) {
        int lineIndex = line.index();
        int column = column(line, color);

        // Count occupied neighbouring tiles above and below.
        return 1
                + countInDirection(pkWall, lineIndex, column, -1, 0)
                + countInDirection(pkWall, lineIndex, column, +1, 0);
    }

    /// Returns {@code true} iff the row corresponding to pattern line {@code line}
    /// in the packed wall {@code pkWall} is full (all 5 cells contain a tile).
    ///
    /// @param pkWall the packed wall
    /// @param line   the pattern line identifying the row
    /// @return {@code true} iff that row is full
    public static boolean isRowFull(int pkWall, TileDestination.Pattern line) {
        int rowMask = ROW0_MASK << (line.index() * WALL_WIDTH);
        return (pkWall & rowMask) == rowMask;
    }

    /// Returns {@code true} iff column {@code column} of the packed wall {@code pkWall}
    /// is full (all 5 cells contain a tile).
    ///
    /// @param pkWall the packed wall
    /// @param column the column number, between 0 and 4 (inclusive)
    /// @return {@code true} iff that column is full
    public static boolean isColumnFull(int pkWall, int column) {
        //assert 0 <= column && column < WALL_WIDTH;
        int columnMask = COLUMN0_MASK << column;
        return (pkWall & columnMask) == columnMask;
    }

    /// Returns {@code true} iff color {@code color} of the packed wall {@code pkWall}
    /// is full (all 5 cells of that color contain a tile).
    ///
    /// @param pkWall the packed wall
    /// @param color  the color to check
    /// @return {@code true} iff all cells of that color contain a tile
    public static boolean isColorFull(int pkWall, TileKind.Colored color) {
        int mask = colorMask(color);
        return (pkWall & mask) == mask;
    }

    /// Returns the packed tile set consisting of all tiles present on the packed wall {@code pkWall}.
    ///
    /// @param pkWall the packed wall
    /// @return a packed tile set containing all tiles on the wall
    public static int asPkTileSet(int pkWall) {
        int pkTileSet = PkTileSet.EMPTY;

        for (TileKind.Colored color : TileKind.Colored.ALL) {
            int count = Integer.bitCount(pkWall & colorMask(color));
            pkTileSet = PkTileSet.union(pkTileSet, PkTileSet.of(count, color));
        }
        return pkTileSet;
    }

    /// Returns {@code true} iff at least one row of the packed wall {@code pkWall} is full.
    ///
    /// @param pkWall the packed wall
    /// @return {@code true} iff at least one row contains all 5 tiles
    public static boolean hasFullRow(int pkWall) {
        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
            if (isRowFull(pkWall, line)) {
                return true;
            }
        }
        return false;
    }

    /// Returns the textual representation of the packed wall {@code pkWall}.
    /// The representation consists of five comma-separated elements enclosed in brackets,
    /// one per row from top to bottom. Each element contains five characters: the letter
    /// of the color accepted by each cell, in lowercase if the cell is empty, uppercase
    /// if it contains a tile.
    ///
    /// @param pkWall the packed wall
    /// @return the textual representation of the wall
    public static String toString(int pkWall){
        StringJoiner outputString = new StringJoiner(", ", "[", "]");

        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
            StringBuilder row = new StringBuilder();

            for(int i = 0; i < WALL_WIDTH; i++){
                TileKind.Colored color = colorAt(line, i);
                String letter = color.name();
                if(!hasTileAt(pkWall, line, color)){
                    letter = letter.toLowerCase();
                }
                row.append(letter);
            }
            outputString.add(row.toString());
        }
        return outputString.toString();
    }
}

