package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;

/// Contains static methods for manipulating moves packed into {@code short} values.
/// A packed move encodes three components in bits 0–9:
/// the source index in bits 0–3, the tile color index in bits 4–6,
/// and the destination index in bits 7–9.
/// The six most-significant bits are always 0.
///
/// @author Hedi Fourati
/// @author Fahd Ben Ismail
public final class PkMove {
    // Bit offset of the source index in the packed move
    private static final int SOURCE_OFFSET = 0;
    // Number of bits used to encode the source index
    private static final int SOURCE_BITS = 4;
    // Bit mask for extracting the source index
    private static final int SOURCE_MASK = (1 << SOURCE_BITS) - 1;
    // Bit offset of the color index in the packed move
    private static final int COLOR_OFFSET = SOURCE_OFFSET + SOURCE_BITS;
    // Number of bits used to encode the tile color index
    private static final int COLOR_BITS = 3;
    // Bit mask for extracting the tile color index
    private static final int COLOR_MASK = (1 << COLOR_BITS) - 1;
    // Bit offset of the destination index in the packed move
    private static final int DESTINATION_OFFSET = COLOR_OFFSET + COLOR_BITS;
    // Number of bits used to encode the destination index
    private static final int DESTINATION_BITS = 3;
    // Bit mask for extracting the destination index
    private static final int DESTINATION_MASK = (1 << DESTINATION_BITS) - 1;

    /// Returns the packed {@code short} representing the move with the given
    /// source, tile color, and destination.
    ///
    /// @param source      the tile source from which the tiles are taken
    /// @param color       the color of the tiles taken
    /// @param destination the destination where the tiles are placed
    /// @return the packed {@code short} encoding of the move
    public static short pack(TileSource source, TileKind.Colored color,
                             TileDestination destination) {

        short packedMove = SOURCE_OFFSET;
        short sourceIndex = (short) source.index();
        short colorIndex = (short) (color.index() << COLOR_OFFSET);
        short destinationIndex = (short) (destination.index() << DESTINATION_OFFSET);
        packedMove = (short) (packedMove | sourceIndex | colorIndex | destinationIndex);

        return packedMove;
    }

    /// Returns the tile source of the given packed move.
    ///
    /// @param pkMove the packed move
    /// @return the tile source of {@code pkMove}
    public static TileSource source(short pkMove) {
        int sourceIndex = (pkMove >> SOURCE_OFFSET) & SOURCE_MASK;
        return TileSource.ALL.get(sourceIndex);
    }

    /// Returns the tile color of the given packed move.
    ///
    /// @param pkMove the packed move
    /// @return the tile color of {@code pkMove}
    public static TileKind.Colored color(short pkMove) {
        int coloredIndex = (pkMove >> COLOR_OFFSET) & COLOR_MASK;
        return TileKind.Colored.ALL.get(coloredIndex);
    }

    /// Returns the tile destination of the given packed move.
    ///
    /// @param pkMove the packed move
    /// @return the tile destination of {@code pkMove}
    public static TileDestination destination(short pkMove) {
        int destinationIndex = (pkMove >> DESTINATION_OFFSET) & DESTINATION_MASK;
        return TileDestination.ALL.get(destinationIndex);
    }
}
