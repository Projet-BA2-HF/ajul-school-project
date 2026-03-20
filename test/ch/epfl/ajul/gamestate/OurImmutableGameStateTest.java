package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.gamestate.packed.*;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OurImmutableGameStateTest {

    static final Game GAME_2P = new Game(List.of(
            new Game.PlayerDescription(PlayerId.P1, "Alice", Game.PlayerDescription.PlayerKind.HUMAN),
            new Game.PlayerDescription(PlayerId.P2, "Bob", Game.PlayerDescription.PlayerKind.AI)
    ));
    static final Game GAME_3P = new Game(List.of(
            new Game.PlayerDescription(PlayerId.P1, "Alice", Game.PlayerDescription.PlayerKind.HUMAN),
            new Game.PlayerDescription(PlayerId.P2, "Bob", Game.PlayerDescription.PlayerKind.AI),
            new Game.PlayerDescription(PlayerId.P3, "Charlie", Game.PlayerDescription.PlayerKind.HUMAN)
    ));
    static final Game GAME_4P = new Game(List.of(
            new Game.PlayerDescription(PlayerId.P1, "Alice", Game.PlayerDescription.PlayerKind.HUMAN),
            new Game.PlayerDescription(PlayerId.P2, "Bob", Game.PlayerDescription.PlayerKind.AI),
            new Game.PlayerDescription(PlayerId.P3, "Charlie", Game.PlayerDescription.PlayerKind.HUMAN),
            new Game.PlayerDescription(PlayerId.P4, "Diana", Game.PlayerDescription.PlayerKind.AI)
    ));

    private static final List<Game> ALL_GAMES = List.of(GAME_2P, GAME_3P, GAME_4P);

    // ───────────────────────────────────────────────────────────
    // Helpers
    // ───────────────────────────────────────────────────────────

    private static ImmutableIntArray emptyPlayerStatesWithScores(Game game, int baseScore) {
        int[] a = new int[game.playersCount() * 4];
        for (PlayerId p : game.playerIds()) {
            int o = p.ordinal() * 4;
            a[o]     = PkPatterns.EMPTY;
            a[o + 1] = PkFloor.EMPTY;
            a[o + 2] = PkWall.EMPTY;
            a[o + 3] = baseScore + p.ordinal();
        }
        return ImmutableIntArray.copyOf(a);
    }

    private static ImmutableIntArray generatedSources(Game game, int seed) {
        int[] a = new int[game.tileSourcesCount()];
        int[] palette = new int[] {
                PkTileSet.EMPTY,
                PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER),
                PkTileSet.of(1, TileKind.A),
                PkTileSet.of(2, TileKind.B),
                PkTileSet.of(3, TileKind.C),
                PkTileSet.of(4, TileKind.D),
                PkTileSet.of(2, TileKind.E)
        };
        for (int i = 0; i < a.length; i++) {
            a[i] = palette[(seed + i) % palette.length];
        }
        return ImmutableIntArray.copyOf(a);
    }

    // ───────────────────────────────────────────────────────────
    // Bruteforce : accès record + invariants simples
    // ───────────────────────────────────────────────────────────

    @Test
    void constructorPreservesExactlyAllComponentsBruteforce() {
        int[] bagVariants = {
                PkTileSet.EMPTY,
                PkTileSet.FULL_COLORED,
                PkTileSet.of(2, TileKind.A),
                PkTileSet.of(3, TileKind.C),
                PkTileSet.of(4, TileKind.E)
        };

        for (Game game : ALL_GAMES) {
            for (PlayerId current : game.playerIds()) {
                for (int bag : bagVariants) {
                    for (int seed = 0; seed < 12; seed++) {
                        ImmutableIntArray sources = generatedSources(game, seed);
                        ImmutableIntArray players = emptyPlayerStatesWithScores(game, seed);

                        int maxMask = 1 << game.tileSourcesCount();
                        for (int uniqueMask = 0; uniqueMask < maxMask; uniqueMask++) {
                            var state = new ImmutableGameState(
                                    game,
                                    bag,
                                    sources,
                                    uniqueMask,
                                    players,
                                    current
                            );

                            assertSame(game, state.game());
                            assertEquals(bag, state.pkTileBag());
                            assertSame(sources, state.pkTileSources());
                            assertEquals(uniqueMask, state.pkUniqueTileSources());
                            assertSame(players, state.pkPlayerStates());
                            assertEquals(current, state.currentPlayerId());
                            assertSame(state, state.immutable());
                        }
                    }
                }
            }
        }
    }

    @Test
    void playerIdsAlwaysMatchesUnderlyingGameBruteforce() {
        for (Game game : ALL_GAMES) {
            int maxMask = 1 << game.tileSourcesCount();
            for (int uniqueMask = 0; uniqueMask < maxMask; uniqueMask++) {
                for (PlayerId current : game.playerIds()) {
                    var state = new ImmutableGameState(
                            game,
                            PkTileSet.EMPTY,
                            generatedSources(game, uniqueMask % 7),
                            uniqueMask,
                            emptyPlayerStatesWithScores(game, uniqueMask),
                            current
                    );

                    assertEquals(game.playerIds(), state.playerIds());
                }
            }
        }
    }

    @Test
    void isRoundOverDependsOnlyOnUniqueTileSourcesBruteforce() {
        for (Game game : ALL_GAMES) {
            int maxMask = 1 << game.tileSourcesCount();
            for (int uniqueMask = 0; uniqueMask < maxMask; uniqueMask++) {
                var state = new ImmutableGameState(
                        game,
                        PkTileSet.of(2, TileKind.A),
                        generatedSources(game, uniqueMask % 9),
                        uniqueMask,
                        emptyPlayerStatesWithScores(game, 0),
                        game.playerIds().getFirst()
                );

                assertEquals(uniqueMask == PkIntSet32.EMPTY, state.isRoundOver(),
                        "mask = " + uniqueMask + ", game size = " + game.playersCount());
            }
        }
    }


    @Test
    void isGameOverIsAlwaysFalseWhenRoundNotOverAndWallsEmptyBruteforce() {
        for (Game game : ALL_GAMES) {
            int maxMask = 1 << game.tileSourcesCount();
            for (int uniqueMask = 1; uniqueMask < maxMask; uniqueMask++) { // pas round over
                var state = new ImmutableGameState(
                        game,
                        PkTileSet.EMPTY,
                        generatedSources(game, uniqueMask % 11),
                        uniqueMask,
                        emptyPlayerStatesWithScores(game, 0),
                        game.playerIds().getFirst()
                );

                assertFalse(state.isGameOver());
            }
        }
    }

    @Test
    void isGameOverIsFalseAtRoundEndWhenAllWallsAreEmptyBruteforce() {
        for (Game game : ALL_GAMES) {
            for (PlayerId current : game.playerIds()) {
                for (int seed = 0; seed < 20; seed++) {
                    var state = new ImmutableGameState(
                            game,
                            PkTileSet.EMPTY,
                            generatedSources(game, seed),
                            PkIntSet32.EMPTY,
                            emptyPlayerStatesWithScores(game, seed),
                            current
                    );

                    assertTrue(state.isRoundOver());
                    assertFalse(state.isGameOver());
                }
            }
        }
    }

    @Test
    void initialStateHasExpectedCoreFieldsForAllGames() {
        for (Game game : ALL_GAMES) {
            ImmutableGameState state = ImmutableGameState.initial(game);

            assertSame(game, state.game());
            assertEquals(PkTileSet.FULL_COLORED, state.pkTileBag());

            assertEquals(game.tileSourcesCount(), state.pkTileSources().size());
            assertEquals(PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER), state.pkTileSources().get(0));
            for (int i = 1; i < game.tileSourcesCount(); i++) {
                assertEquals(PkTileSet.EMPTY, state.pkTileSources().get(i));
            }

            assertEquals(PkIntSet32.EMPTY, state.pkUniqueTileSources());
            assertArrayEquals(PkPlayerStates.initial(game).toArray(), state.pkPlayerStates().toArray());
            assertEquals(game.playerIds().getFirst(), state.currentPlayerId());
        }
    }

    @Test
    void isGameOverIsTrueAtRoundEndWhenAnyPlayerHasFullRowBruteforce() {
        int fullRow = PkWall.EMPTY;
        for (TileKind.Colored color : TileKind.Colored.ALL) {
            fullRow = PkWall.withTileAt(fullRow, TileDestination.Pattern.PATTERN_1, color);
        }

        for (Game game : ALL_GAMES) {
            for (PlayerId player : game.playerIds()) {
                int[] ps = emptyPlayerStatesWithScores(game, 0).toArray();
                PkPlayerStates.setPkWall(ps, player, fullRow);

                var state = new ImmutableGameState(
                        game,
                        PkTileSet.EMPTY,
                        ImmutableIntArray.copyOf(new int[game.tileSourcesCount()]),
                        PkIntSet32.EMPTY,
                        ImmutableIntArray.copyOf(ps),
                        game.playerIds().getFirst()
                );

                assertTrue(state.isRoundOver());
                assertTrue(state.isGameOver());
            }
        }
    }


}