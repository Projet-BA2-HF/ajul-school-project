package ch.epfl.ajul.gui;

import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.Preconditions;
import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;

import java.util.Objects;

/// Represents a location that a tile may occupy, either on the board
/// (sources, pattern lines, walls, floor lines) or off the board
/// (in the bag or the box lid).
///
/// @author Hedi Fourati
/// @author Fahd Ben Ismail
public sealed interface TileLocation {

    /// Represents a location off the board, i.e. in the bag or the box lid.
    ///
    /// @param kind  the kind of the tile occupying this location
    /// @param index the index of this location among those of the same kind
    record OffBoard(TileKind kind, int index) implements TileLocation {
        public OffBoard {
            Objects.requireNonNull(kind);
            Preconditions.checkArgument(0 <= index && index < kind.tilesCount());
        }
    }

    /// Represents a location on a tile source, i.e. on a factory or in the center area.
    ///
    /// @param source the tile source
    /// @param index  the index of this location within the source
    record OnSource(TileSource source, int index) implements TileLocation {
        public OnSource {
            Objects.requireNonNull(source);
            int capacity = (source instanceof TileSource.Factory)
                    ? TileSource.Factory.TILES_PER_FACTORY
                    : Integer.MAX_VALUE;
            Preconditions.checkArgument(0 <= index && index < capacity);
        }
    }

    /// Represents a location on a pattern line of a player's board.
    ///
    /// @param playerId the identity of the player
    /// @param line     the pattern line
    /// @param index    the index of this location on the line
    record OnPattern(PlayerId playerId,
                     TileDestination.Pattern line,
                     int index) implements TileLocation {
        public OnPattern {
            Objects.requireNonNull(playerId);
            Objects.requireNonNull(line);
            Preconditions.checkArgument(0 <= index && index < line.capacity());
        }
    }

    /// Represents a location on the wall of a player's board.
    ///
    /// @param playerId the identity of the player
    /// @param line     the wall line
    /// @param color    the color of the tile that may occupy this location
    record OnWall(PlayerId playerId,
                  TileDestination.Pattern line,
                  TileKind.Colored color) implements TileLocation {
        public OnWall {
            Objects.requireNonNull(playerId);
            Objects.requireNonNull(line);
            Objects.requireNonNull(color);
        }
    }

    /// Represents a location on the floor line of a player's board.
    ///
    /// @param playerId the identity of the player
    /// @param index    the index of this location on the floor line, between 0 and 6
    record OnFloor(PlayerId playerId, int index) implements TileLocation {
        public OnFloor {
            Objects.requireNonNull(playerId);
            Preconditions.checkArgument(
                    0 <= index
                            && index < TileDestination.FLOOR.capacity());
        }
    }
}