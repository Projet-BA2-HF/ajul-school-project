package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;

public final class PkMove {
    private static final int SOURCE_OFFSET = 0;
    private static final int SOURCE_BITS = 4;
    private static final int SOURCE_MASK = (1 << SOURCE_BITS) - 1;
    private static final int COLOR_OFFSET = SOURCE_OFFSET + SOURCE_BITS;
    private static final int COLOR_BITS = 3;
    private static final int COLOR_MASK = (1 << COLOR_BITS) - 1;
    private static final int DESTINATION_OFFSET = COLOR_OFFSET + COLOR_BITS;
    private static final int DESTINATION_BITS = 3;
    private static final int DESTINATION_MASK = (1 << DESTINATION_BITS) - 1;

    public static short pack(TileSource source, TileKind.Colored color, TileDestination destination){
        /// Exemple 000000 / 100 (Destination 8) / 010 (Couleur 8) / 0111 (Source 16)

        short packedMove = SOURCE_OFFSET;
        short sourceIndex = (short) source.index();
        short colorIndex = (short) (color.index() << COLOR_OFFSET);
        short destinationIndex = (short) (destination.index() << DESTINATION_OFFSET);
        packedMove = (short) (packedMove | sourceIndex | colorIndex | destinationIndex);

        return packedMove;
    }

    public static TileSource source(short pkMove){
        int sourceIndex = (pkMove >> SOURCE_OFFSET) & SOURCE_MASK;
        return TileSource.ALL.get(sourceIndex);
    }

    public static TileKind.Colored color(short pkMove){
        int coloredIndex = (pkMove >> COLOR_OFFSET) & COLOR_MASK;
        return TileKind.Colored.ALL.get(coloredIndex);
    }

    public static TileDestination destination(short pkMove){
        int destinationIndex = (pkMove >> DESTINATION_OFFSET) & DESTINATION_MASK;
        return TileDestination.ALL.get(destinationIndex);
    }
}
