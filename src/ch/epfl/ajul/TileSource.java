package ch.epfl.ajul;

import java.util.List;

/// Identifies a tile source, i.e. one of the factories or the center area
/// from which a player may take tiles during their turn.

public sealed interface TileSource {

    /// The center area tile source.
    TileSource CENTER_AREA = CenterArea.CENTER_AREA;

    /// Factory number 1.
    TileSource FACTORY_1 = Factory.FACTORY_1;

    /// Factory number 2.
    TileSource FACTORY_2 = Factory.FACTORY_2;

    /// Factory number 3.
    TileSource FACTORY_3 = Factory.FACTORY_3;

    /// Factory number 4.
    TileSource FACTORY_4 = Factory.FACTORY_4;

    /// Factory number 5.
    TileSource FACTORY_5 = Factory.FACTORY_5;

    /// Factory number 6.
    TileSource FACTORY_6 = Factory.FACTORY_6;

    /// Factory number 7.
    TileSource FACTORY_7 = Factory.FACTORY_7;

    /// Factory number 8.
    TileSource FACTORY_8 = Factory.FACTORY_8;

    /// Factory number 9.
    TileSource FACTORY_9 = Factory.FACTORY_9;

    /// An immutable list of all tile sources, where each source is stored
    /// at the position equal to its index (0 for the center area, 1–9 for factories).
    List<TileSource> ALL = List.of(
            CENTER_AREA,
            FACTORY_1, FACTORY_2, FACTORY_3,
            FACTORY_4, FACTORY_5, FACTORY_6,
            FACTORY_7, FACTORY_8, FACTORY_9
    );

    /// The total number of tile sources (10).
    int COUNT = ALL.size();

    /// Returns the index of this tile source.
    /// The center area has index 0; factories 1–9 have indices 1–9.
    ///
    /// @return the index of this tile source
    int index();

    /// Represents the center area, the shared zone where leftover tiles accumulate.
    enum CenterArea implements TileSource {
        CENTER_AREA;

        /// Returns the index of the center area (0).
        ///
        /// @return 0
        @Override
        public int index() {
            return 0;
        }
    }

    /// Represents the nine factories, each holding a fixed number of tiles at the start of a round.
    enum Factory implements TileSource {
        FACTORY_1, FACTORY_2, FACTORY_3,
        FACTORY_4, FACTORY_5, FACTORY_6,
        FACTORY_7, FACTORY_8, FACTORY_9;

        /// An immutable list of all factories, in definition order.
        public static final List<Factory> ALL = List.of(values());

        /// The number of factories (9).
        public static final int COUNT = ALL.size();

        /// The number of tiles placed in each factory at the start of a round (4).
        public static final int TILES_PER_FACTORY = 4;

        /// Returns the index of this factory (1 for FACTORY_1, ..., 9 for FACTORY_9).
        ///
        /// @return the index of this factory
        @Override
        public int index() {
            return this.ordinal() + 1;
        }
    }
}