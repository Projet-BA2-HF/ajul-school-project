package ch.epfl.ajul;

/// Contains constants and static methods for computing the points earned by players
/// during a game of Ajul, both at the end of each round and at the end of the game.
///
/// @author Hedi Fourati
/// @author Fahd Ben Ismail
public final class Points {

    /// Bonus points awarded for each complete row of the wall at the end of the game.
    public static final int FULL_ROW_BONUS_POINTS = 2;
    /// Bonus points awarded for each complete column of the wall at the end of the game.
    public static final int FULL_COLUMN_BONUS_POINTS = 7;
    /// Bonus points awarded for each complete color of the wall at the end of the game.
    public static final int FULL_COLOR_BONUS_POINTS = 10;

    private static final int BITS_PER_PENALTY = 4;
    private static final int PENALTY_MASK = (1 << BITS_PER_PENALTY) - 1;
    //private static final int MAX_WIDTH = 5;
    //private static final int MAX_HEIGHT = 5;

    // Packed floor penalties: each 4-bit nibble encodes the penalty for the
    // corresponding tile position (nibble 0 = position 0, etc.).
    // Values: positions 0–1 → 1, positions 2–4 → 2, positions 5–6 → 3.
    private static final int FLOOR_PENALTY = 0x3322211;
    // Cumulative floor penalties: nibble i holds the total penalty for a floor
    // line of i tiles, computed by summing the individual penalties above.
    private static final int TOTAL_FLOOR_PENALTY =
            (FLOOR_PENALTY * 0x1111111) << BITS_PER_PENALTY;

    /// Returns the number of points earned by placing a tile on the wall when that tile
    /// belongs to a horizontal group of size {@code hGroupSize} and a vertical group of
    /// size {@code vGroupSize}. If one group has size 1, only the other group's size is
    /// counted; otherwise both sizes are summed.
    ///
    /// @param hGroupSize the size of the horizontal group, between 1 and 5 (inclusive)
    /// @param vGroupSize the size of the vertical group, between 1 and 5 (inclusive)
    /// @return the number of points earned for the newly placed tile
    public static int newWallTilePoints(int hGroupSize, int vGroupSize){
        //assert hGroupSize > 0 && hGroupSize <= MAX_WIDTH
        //&& vGroupSize > 0 && vGroupSize <= MAX_HEIGHT;
        if (vGroupSize == 1) return hGroupSize;
        else if (hGroupSize == 1) return vGroupSize;
        else return hGroupSize + vGroupSize;
    }

    /// Returns the penalty (a positive value) associated with the floor-line tile at position
    /// {@code tileIndex}. The penalty is 1 for tiles 0–1, 2 for tiles 2–4, and 3 for tiles 5–6.
    ///
    /// @param tileIndex the position of the tile in the floor line, between 0 and 6 (inclusive)
    /// @return the penalty for that tile (1, 2, or 3)
    public static int floorPenalty(int tileIndex){
        //assert 0 <= tileIndex && tileIndex <= TileDestination.FLOOR.capacity() - 1;
        return (FLOOR_PENALTY >>> (tileIndex * BITS_PER_PENALTY)) & PENALTY_MASK;
    }

    /// Returns the total penalty (a positive or zero value) associated with a floor line
    /// containing {@code tilesCount} tiles. The total is the sum of the individual penalties
    /// for tiles 0 up to {@code tilesCount − 1}, and lies between 0 and 14 (inclusive).
    ///
    /// @param tilesCount the number of tiles in the floor line, between 0 and 7 (inclusive)
    /// @return the total penalty for that floor line
    public static int totalFloorPenalty(int tilesCount){
        //assert 0 <= tilesCount && tilesCount <= TileDestination.FLOOR.capacity();
        return (TOTAL_FLOOR_PENALTY >>> (tilesCount * BITS_PER_PENALTY)) & PENALTY_MASK;
    }

}
