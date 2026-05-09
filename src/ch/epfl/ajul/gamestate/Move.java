package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.packed.PkMove;

import java.util.Objects;

/// Represents a move in Ajul, consisting of the source from which tiles are taken,
/// the color of tiles chosen, and the destination where they are placed.
/// All three components are guaranteed non-null.
///
/// @param source      the tile source from which the tiles are taken
/// @param tileColor   the color of the tiles taken
/// @param destination the destination where the tiles are placed
///
/// @author Hedi Fourati
/// @author Fahd Ben Ismail
public record Move(TileSource source,
                   TileKind.Colored tileColor,
                   TileDestination destination) {

    /// The maximum number of valid moves a player may choose from in any game state (216).
    /// Computed as 9 factories × 4 tiles per factory × 6 destinations.
    public static final int MAX_MOVES = TileSource.Factory.COUNT *
            TileSource.Factory.TILES_PER_FACTORY *
            TileDestination.COUNT;

    /// Compact constructor that validates that none of the three components is {@code null}.
    ///
    /// @throws NullPointerException if any of {@code source}, {@code tileColor},
    ///                              or {@code destination} is {@code null}
    public Move{
        Objects.requireNonNull(source);
        Objects.requireNonNull(tileColor);
        Objects.requireNonNull(destination);
    }

    /// Returns the move corresponding to the given packed move.
    ///
    /// @param pkMove the packed move
    /// @return the {@code Move} decoded from {@code pkMove}
    public static Move ofPacked(short pkMove){
        return new Move(PkMove.source(pkMove),
                PkMove.color(pkMove),
                PkMove.destination(pkMove));
    }

    /// Returns the packed {@code short} representation of this move.
    ///
    /// @return the packed encoding of this move
    public short packed(){
        return PkMove.pack(source, tileColor, destination);
    }
}
