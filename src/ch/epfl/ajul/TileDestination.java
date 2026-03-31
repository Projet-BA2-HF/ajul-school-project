package ch.epfl.ajul;

import ch.epfl.ajul.gamestate.packed.PkFloor;

import java.util.List;

/// Identifies a tile destination, i.e. one of the five pattern lines
/// or the floor line on a player's personal board.
///
/// @author Hedi Fourati
/// @author Fahd Ben Ismail

public sealed interface TileDestination {

    /// The first pattern line (capacity 1).
    TileDestination PATTERN_1 = Pattern.PATTERN_1;

    /// The second pattern line (capacity 2).
    TileDestination PATTERN_2 = Pattern.PATTERN_2;

    /// The third pattern line (capacity 3).
    TileDestination PATTERN_3 = Pattern.PATTERN_3;

    /// The fourth pattern line (capacity 4).
    TileDestination PATTERN_4 = Pattern.PATTERN_4;

    /// The fifth pattern line (capacity 5).
    TileDestination PATTERN_5 = Pattern.PATTERN_5;

    /// The floor line (capacity 7).
    TileDestination FLOOR = Floor.FLOOR;

    /// An immutable list of all tile destinations, in index order
    /// (pattern lines 1–5, then the floor line).
    List<TileDestination> ALL = List.of(PATTERN_1,
            PATTERN_2,
            PATTERN_3,
            PATTERN_4,
            PATTERN_5, FLOOR);

    /// The total number of tile destinations (6).
    int COUNT = ALL.size();

    /// Returns the index of this destination.
    /// Pattern lines 1–5 have indices 0–4; the floor line has index 5.
    ///
    /// @return the index of this destination
    int index();

    /// Returns the maximum number of tiles this destination can hold.
    ///
    /// @return the capacity of this destination
    int capacity();

    /// Represents the five pattern lines on a player's board.
    enum Pattern implements TileDestination {
        PATTERN_1, PATTERN_2, PATTERN_3, PATTERN_4, PATTERN_5;

        /// An immutable list of all pattern lines, in definition order.
        public static final List<Pattern> ALL = List.of(values());

        /// The number of pattern lines (5).
        public static final int COUNT = ALL.size();

        /// Returns the index of this pattern line (0 for PATTERN_1, ..., 4 for PATTERN_5).
        ///
        /// @return the index of this pattern line
        @Override
        public int index() {
            return this.ordinal();
        }

        /// Returns the capacity of this pattern line (1 for the first, 2 for the second, etc.).
        ///
        /// @return the capacity of this pattern line
        @Override
        public int capacity() {
            return index() + 1;
        }
    }

    /// Represents the floor line, where excess and penalty tiles are placed.
    enum Floor implements TileDestination {
        FLOOR;

        /// Returns the index of the floor line (5).
        ///
        /// @return 5
        @Override
        public int index() {
            return 5;
        }

        /// Returns the capacity of the floor line (7).
        ///
        /// @return 7
        @Override
        public int capacity() {
            return 7;
        }
    }
}