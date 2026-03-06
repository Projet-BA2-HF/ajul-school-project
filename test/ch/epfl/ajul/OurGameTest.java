package ch.epfl.ajul;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OurGameTest {

    private Game makeGame(PlayerId... ids) {
        List<Game.PlayerDescription> descs = new ArrayList<>();
        for (PlayerId id : ids) {
            descs.add(new Game.PlayerDescription(
                    id,
                    "Player",
                    Game.PlayerDescription.PlayerKind.HUMAN
            ));
        }
        return new Game(descs);
    }

    // --- Constructeur ---

    @Test
    void constructorAccepts2Players() {
        assertDoesNotThrow(() -> makeGame(PlayerId.P1, PlayerId.P2));
    }

    @Test
    void constructorAccepts3Players() {
        assertDoesNotThrow(() -> makeGame(PlayerId.P1, PlayerId.P2, PlayerId.P3));
    }

    @Test
    void constructorAccepts4Players() {
        assertDoesNotThrow(() -> makeGame(PlayerId.P1, PlayerId.P2, PlayerId.P3, PlayerId.P4));
    }

    @Test
    void constructorRejectsEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> new Game(List.of()));
    }

    @Test
    void constructorRejectsLessThan2Players() {
        assertThrows(IllegalArgumentException.class, () -> makeGame(PlayerId.P1));
    }

    @Test
    void constructorRejectsMoreThan4Players() {
        List<Game.PlayerDescription> descs = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            descs.add(new Game.PlayerDescription(
                    PlayerId.ALL.get(i % 4),
                    "Player",
                    Game.PlayerDescription.PlayerKind.HUMAN
            ));
        }
        assertThrows(IllegalArgumentException.class, () -> new Game(descs));
    }

    @Test
    void constructorRejectsOutOfOrderIds() {
        assertThrows(IllegalArgumentException.class, () -> makeGame(PlayerId.P2, PlayerId.P1));
    }

    @Test
    void constructorRejectsSkippedId() {
        assertThrows(IllegalArgumentException.class, () -> makeGame(PlayerId.P1, PlayerId.P3));
    }

    @Test
    void constructorRejectsWrongThirdId() {
        assertThrows(IllegalArgumentException.class,
                () -> makeGame(PlayerId.P1, PlayerId.P2, PlayerId.P4));
    }

    @Test
    void constructorMakesDefensiveCopyOfInputList() {
        var desc1 = new Game.PlayerDescription(PlayerId.P1, "Alice", Game.PlayerDescription.PlayerKind.HUMAN);
        var desc2 = new Game.PlayerDescription(PlayerId.P2, "Bob", Game.PlayerDescription.PlayerKind.AI);

        List<Game.PlayerDescription> original = new ArrayList<>();
        original.add(desc1);
        original.add(desc2);

        Game g = new Game(original);

        original.clear();

        assertEquals(List.of(desc1, desc2), g.playerDescriptions());
    }

    // --- PlayerDescription ---

    @Test
    void playerDescriptionRejectsNullId() {
        assertThrows(NullPointerException.class,
                () -> new Game.PlayerDescription(null, "Alice", Game.PlayerDescription.PlayerKind.HUMAN));
    }

    @Test
    void playerDescriptionRejectsNullName() {
        assertThrows(NullPointerException.class,
                () -> new Game.PlayerDescription(PlayerId.P1, null, Game.PlayerDescription.PlayerKind.HUMAN));
    }

    @Test
    void playerDescriptionRejectsNullKind() {
        assertThrows(NullPointerException.class,
                () -> new Game.PlayerDescription(PlayerId.P1, "Alice", null));
    }

    // --- playerDescriptions ---

    @Test
    void playerDescriptions() {
        var desc1 = new Game.PlayerDescription(PlayerId.P1, "Alice", Game.PlayerDescription.PlayerKind.HUMAN);
        var desc2 = new Game.PlayerDescription(PlayerId.P2, "Bob", Game.PlayerDescription.PlayerKind.AI);
        Game g = new Game(List.of(desc1, desc2));
        assertEquals(List.of(desc1, desc2), g.playerDescriptions());
    }

    @Test
    void playerDescriptionsIsUnmodifiable() {
        Game g = makeGame(PlayerId.P1, PlayerId.P2);
        assertThrows(UnsupportedOperationException.class,
                () -> g.playerDescriptions().add(
                        new Game.PlayerDescription(PlayerId.P3, "X", Game.PlayerDescription.PlayerKind.HUMAN)
                ));
    }

    // --- playerIds ---

    @Test
    void playerIdsWith2Players() {
        assertEquals(List.of(PlayerId.P1, PlayerId.P2),
                makeGame(PlayerId.P1, PlayerId.P2).playerIds());
    }

    @Test
    void playerIdsWith3Players() {
        assertEquals(List.of(PlayerId.P1, PlayerId.P2, PlayerId.P3),
                makeGame(PlayerId.P1, PlayerId.P2, PlayerId.P3).playerIds());
    }

    @Test
    void playerIdsWith4Players() {
        assertEquals(PlayerId.ALL,
                makeGame(PlayerId.P1, PlayerId.P2, PlayerId.P3, PlayerId.P4).playerIds());
    }

    @Test
    void playerIdsIsUnmodifiable() {
        Game g = makeGame(PlayerId.P1, PlayerId.P2);
        assertThrows(UnsupportedOperationException.class,
                () -> g.playerIds().add(PlayerId.P3));
    }

    // --- playersCount ---

    @Test
    void playersCountWith2Players() {
        assertEquals(2, makeGame(PlayerId.P1, PlayerId.P2).playersCount());
    }

    @Test
    void playersCountWith3Players() {
        assertEquals(3, makeGame(PlayerId.P1, PlayerId.P2, PlayerId.P3).playersCount());
    }

    @Test
    void playersCountWith4Players() {
        assertEquals(4, makeGame(PlayerId.P1, PlayerId.P2, PlayerId.P3, PlayerId.P4).playersCount());
    }

    // --- factoriesCount ---

    @Test
    void factoriesCountWith2Players() {
        assertEquals(5, makeGame(PlayerId.P1, PlayerId.P2).factoriesCount());
    }

    @Test
    void factoriesCountWith3Players() {
        assertEquals(7, makeGame(PlayerId.P1, PlayerId.P2, PlayerId.P3).factoriesCount());
    }

    @Test
    void factoriesCountWith4Players() {
        assertEquals(9, makeGame(PlayerId.P1, PlayerId.P2, PlayerId.P3, PlayerId.P4).factoriesCount());
    }

    // --- factories ---

    @Test
    void factoriesSizeMatchesFactoriesCount() {
        Game g = makeGame(PlayerId.P1, PlayerId.P2);
        assertEquals(g.factoriesCount(), g.factories().size());
    }

    @Test
    void factoriesAreFirstFactories() {
        Game g = makeGame(PlayerId.P1, PlayerId.P2);
        assertEquals(TileSource.Factory.ALL.subList(0, g.factoriesCount()), g.factories());
    }

    @Test
    void factoriesWith4PlayersContainAllFactories() {
        Game g = makeGame(PlayerId.P1, PlayerId.P2, PlayerId.P3, PlayerId.P4);
        assertEquals(TileSource.Factory.ALL, g.factories());
    }

    @Test
    void factoriesIsUnmodifiable() {
        Game g = makeGame(PlayerId.P1, PlayerId.P2);
        assertThrows(UnsupportedOperationException.class,
                () -> g.factories().add(TileSource.Factory.FACTORY_6));
    }

    // --- tileSourcesCount ---

    @Test
    void tileSourcesCountWith2Players() {
        assertEquals(6, makeGame(PlayerId.P1, PlayerId.P2).tileSourcesCount());
    }

    @Test
    void tileSourcesCountWith3Players() {
        assertEquals(8, makeGame(PlayerId.P1, PlayerId.P2, PlayerId.P3).tileSourcesCount());
    }

    @Test
    void tileSourcesCountWith4Players() {
        assertEquals(10, makeGame(PlayerId.P1, PlayerId.P2, PlayerId.P3, PlayerId.P4).tileSourcesCount());
    }

    @Test
    void tileSourcesCountIsFactoriesPlusOne() {
        Game g = makeGame(PlayerId.P1, PlayerId.P2, PlayerId.P3);
        assertEquals(g.factoriesCount() + 1, g.tileSourcesCount());
    }

    // --- tileSources ---

    @Test
    void tileSourcesSizeMatchesTileSourcesCount() {
        Game g = makeGame(PlayerId.P1, PlayerId.P2);
        assertEquals(g.tileSourcesCount(), g.tileSources().size());
    }

    @Test
    void tileSourcesAreCenterAreaThenFactories() {
        Game g = makeGame(PlayerId.P1, PlayerId.P2);
        assertEquals(TileSource.ALL.subList(0, g.tileSourcesCount()), g.tileSources());
    }

    @Test
    void tileSourcesWith4PlayersContainAllTileSources() {
        Game g = makeGame(PlayerId.P1, PlayerId.P2, PlayerId.P3, PlayerId.P4);
        assertEquals(TileSource.ALL, g.tileSources());
    }

    @Test
    void tileSourcesIsUnmodifiable() {
        Game g = makeGame(PlayerId.P1, PlayerId.P2);
        assertThrows(UnsupportedOperationException.class,
                () -> g.tileSources().add(TileSource.CENTER_AREA));
    }

    // --- centralAreaMaxSize ---

    @Test
    void centralAreaMaxSizeWith2Players() {
        assertEquals(16, makeGame(PlayerId.P1, PlayerId.P2).centralAreaMaxSize());
    }

    @Test
    void centralAreaMaxSizeWith3Players() {
        assertEquals(22, makeGame(PlayerId.P1, PlayerId.P2, PlayerId.P3).centralAreaMaxSize());
    }

    @Test
    void centralAreaMaxSizeWith4Players() {
        assertEquals(28, makeGame(PlayerId.P1, PlayerId.P2, PlayerId.P3, PlayerId.P4).centralAreaMaxSize());
    }

    @Test
    void centralAreaMaxSizeMatchesFormula() {
        Game g = makeGame(PlayerId.P1, PlayerId.P2, PlayerId.P3);
        assertEquals(3 * g.factoriesCount() + 1, g.centralAreaMaxSize());
    }
}