package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;

import java.util.StringJoiner;

/// Contains static methods for manipulating the content of a player's floor line
/// packed into an {@code int} value.
///
/// The packing format is as follows:
/// <ul>
///   <li>bits 0–2: number of tiles on the floor line (0–7)</li>
///   <li>bits 3–5: kind index of the tile at position 0, or 0 if the line is empty</li>
///   <li>bits 6–8: kind index of the tile at position 1, or 0 if unused</li>
///   <li>… and so on for up to 7 tile positions</li>
///   <li>bits 24–31: always 0</li>
/// </ul>
public final class PkFloor {

    /// The packed representation of an empty floor line (no tiles).
    public static final int EMPTY = 0;

    private static final int TILE_SHIFT = 3;
    private static final int TILE_MASK = (1 << TILE_SHIFT) - 1;
    private static final int MAX_FLOOR = TileDestination.FLOOR.capacity();

    /// Returns the number of tiles on the given packed floor line.
    ///
    /// @param pkFloor the packed floor line
    /// @return the number of tiles on the floor line
    public static int size(int pkFloor) {
        return pkFloor & TILE_MASK;
    }

    /// Returns the kind of tile at the given index on the packed floor line.
    /// The index must be non-negative and strictly less than the floor size;
    /// behavior is unspecified (assertion error) otherwise.
    ///
    /// @param pkFloor the packed floor line
    /// @param i       the 0-based index of the tile to retrieve
    /// @return the tile kind at index {@code i}
    public static TileKind tileAt(int pkFloor, int i) {
        assert i >= 0 && size(pkFloor) > i;
        return TileKind.ALL.get(pkFloor >> (TILE_SHIFT + i * TILE_SHIFT)
                & TILE_MASK);
    }

    /// Returns a packed floor line identical to {@code pkFloor} but with the tiles
    /// from {@code pkTileSet} appended in kind order (A, B, C, D, E, first player marker).
    /// If the floor does not have enough capacity, excess tiles are discarded, except
    /// for the first player marker which is always placed: if the floor is full and does not
    /// yet contain the marker, it replaces the last tile.
    ///
    /// @param pkFloor   the packed floor line
    /// @param pkTileSet the packed tile set whose tiles are added
    /// @return the updated packed floor line
    public static int withAddedTiles(int pkFloor, int pkTileSet) {
        int size = PkFloor.size(pkFloor);

        for (TileKind tileKind : TileKind.ALL) {
            int maxTileCount = PkTileSet.countOf(pkTileSet, tileKind);
            for (int i = 0; i < maxTileCount; i++) {
                if (size < MAX_FLOOR) {
                    pkFloor = (pkFloor |
                            (tileKind.index() << (size + 1) * TILE_SHIFT));
                    pkFloor++;
                    size = PkFloor.size(pkFloor);
                }
            }
        }

        if (PkTileSet.countOf(pkTileSet, TileKind.FIRST_PLAYER_MARKER) == 1 &&
                size == MAX_FLOOR &&
                !containsFirstPlayerMarker(pkFloor)) {

            pkFloor = (pkFloor & ~(TILE_MASK << size * TILE_SHIFT))
                    | (TileKind.FIRST_PLAYER_MARKER.index() << (size) * TILE_SHIFT);
        }

        return pkFloor;
    }

    /// Returns {@code true} iff the given packed floor line contains the first player marker.
    ///
    /// @param pkFloor the packed floor line
    /// @return {@code true} iff the floor line contains the first player marker
    public static boolean containsFirstPlayerMarker(int pkFloor) {
        int size = PkFloor.size(pkFloor);
        for (int i = 0; i < size; i++)
            if (((pkFloor >> ((i + 1) * TILE_SHIFT))
                    & TILE_MASK) == TileKind.FIRST_PLAYER_MARKER.index())
                return true;
        return false;
    }

    /// Returns the packed tile set composed of all tiles present on the given packed floor line.
    ///
    /// @param pkFloor the packed floor line
    /// @return the packed tile set of all tiles on the floor line
    public static int asPkTileSet(int pkFloor) {
        int pkTileSet = PkTileSet.EMPTY;
        int size = size(pkFloor);

        for (int i = 0; i < size; i++) {
            pkTileSet += PkTileSet.of(1, tileAt(pkFloor, i));
        }

        return pkTileSet;
    }

    /// Returns a textual representation of the given packed floor line.
    /// The representation consists of the names of the tile kinds on the floor,
    /// separated by {@code ", "} and enclosed in brackets.
    /// For example: {@code [FIRST_PLAYER_MARKER, B, B]}.
    ///
    /// @param pkFloor the packed floor line
    /// @return the string representation of {@code pkFloor}
    public static String toString(int pkFloor) {
        StringJoiner outputString = new StringJoiner(", ", "[", "]");
        int size = size(pkFloor);
        for (int i = 0; i < size; i++) {
            outputString.add("" + TileKind.ALL.get(tileAt(pkFloor, i).index()));
        }
        return outputString.toString();
    }
}