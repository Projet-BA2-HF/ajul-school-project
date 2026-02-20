package ch.epfl.ajul;

import java.util.List;
import java.util.random.RandomGenerator;

/// Represents the six kinds of tiles that exist in the game:
/// five colored tile kinds and the first player marker.

public sealed interface TileKind {

    /// The colored tile of kind A.
    TileKind A = Colored.A;

    /// The colored tile of kind B.
    TileKind B = Colored.B;

    /// The colored tile of kind C.
    TileKind C = Colored.C;

    /// The colored tile of kind D.
    TileKind D = Colored.D;

    /// The colored tile of kind E.
    TileKind E = Colored.E;

    /// The first player marker tile.
    TileKind FIRST_PLAYER_MARKER = FirstPlayerMarker.FIRST_PLAYER_MARKER;

    /// An immutable list of all six tile kinds, in index order.
    List<TileKind> ALL = List.of(Colored.A, Colored.B, Colored.C, Colored.D, Colored.E, FirstPlayerMarker.FIRST_PLAYER_MARKER);

    /// The total number of tile kinds (6).
    int COUNT = ALL.size();

    /// Returns the index of this tile kind.
    /// Colored tiles A–E have indices 0–4; the first player marker has index 5.
    ///
    /// @return the index of this tile kind
    int index();

    /// Returns the number of tiles of this kind that exist in the game.
    /// Colored tiles have 20 each; the first player marker has 1.
    ///
    /// @return the total count of tiles of this kind
    int tilesCount();

    /// Represents the five colored tile kinds (A, B, C, D, E).
    enum Colored implements TileKind {
        A, B, C, D, E;

        /// An immutable list of all colored tile kinds, in definition order.
        public static final List<Colored> ALL = List.of(values());

        /// The number of colored tile kinds (5).
        public static final int COUNT = ALL.size();

        /// Returns the index of this colored tile kind (0 for A, 1 for B, ..., 4 for E).
        ///
        /// @return the index of this colored tile kind
        @Override
        public int index() {
            return this.ordinal();
        }

        /// Returns the number of tiles of this colored kind in the game (always 20).
        ///
        /// @return 20
        @Override
        public int tilesCount() {
            return 20;
        }

        /// Shuffles the elements of the given array using the Fisher-Yates algorithm.
        ///
        /// @param tiles           the array of colored tiles to shuffle
        /// @param randomGenerator the random generator used to pick random indices
        public static void shuffle(Colored[] tiles, RandomGenerator randomGenerator) {
            for (int i = 0; i < tiles.length - 1; i++) {
                int j = randomGenerator.nextInt(i, tiles.length);
                Colored temp = tiles[i];
                tiles[i] = tiles[j];
                tiles[j] = temp;
            }
        }
    }

    /// Represents the first player marker, a unique tile with a special role.
    enum FirstPlayerMarker implements TileKind {
        FIRST_PLAYER_MARKER;

        /// Returns the index of the first player marker (5).
        ///
        /// @return 5
        @Override
        public int index() {
            return 5;
        }

        /// Returns the number of first player markers in the game (always 1).
        ///
        /// @return 1
        @Override
        public int tilesCount() {
            return 1;
        }
    }
}