package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.gamestate.packed.*;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;
import ch.epfl.ajul.intarray.MutableIntArray;
import org.junit.jupiter.api.Test;

import ch.epfl.ajul.TileSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class OurReadOnlyGameStateTest {
    Game game = new Game(List.of(
            new Game.PlayerDescription(PlayerId.P1, "Hedi", Game.PlayerDescription.PlayerKind.HUMAN),
            new Game.PlayerDescription(PlayerId.P2, "Fahd", Game.PlayerDescription.PlayerKind.AI)
    ));
    Game game1 = new Game(List.of(
            new Game.PlayerDescription(PlayerId.P1, "Hedi", Game.PlayerDescription.PlayerKind.HUMAN),
            new Game.PlayerDescription(PlayerId.P2, "Fahd", Game.PlayerDescription.PlayerKind.AI),
            new Game.PlayerDescription(PlayerId.P3, "Fahd", Game.PlayerDescription.PlayerKind.AI),
            new Game.PlayerDescription(PlayerId.P4, "Fahd", Game.PlayerDescription.PlayerKind.AI)
    ));
    Game game2 = new Game(List.of(
            new Game.PlayerDescription(PlayerId.P1, "Hedi", Game.PlayerDescription.PlayerKind.HUMAN),
            new Game.PlayerDescription(PlayerId.P2, "Fahd", Game.PlayerDescription.PlayerKind.AI)
    ));

    ImmutableGameState initialState = ImmutableGameState.initial(game);
    ImmutableGameState mockState = new ImmutableGameState(
            initialState.game(),
            PkTileSet.difference(
                    PkTileSet.FULL_COLORED,
                    PkTileSet.union(
                            PkTileSet.union(
                                    PkTileSet.union(
                                            PkTileSet.union(
                                                    PkTileSet.union(
                                                            PkTileSet.union(
                                                                    PkTileSet.union(
                                                                            PkTileSet.union(
                                                                                    PkTileSet.union(
                                                                                            PkTileSet.union(
                                                                                                    PkTileSet.union(
                                                                                                            PkTileSet.of(2, TileKind.C), // center
                                                                                                            PkTileSet.union(PkTileSet.of(2, TileKind.A), PkTileSet.of(2, TileKind.B)) // factory1
                                                                                                    ),
                                                                                                    PkTileSet.union(PkTileSet.of(1, TileKind.A), PkTileSet.of(3, TileKind.D)) // factory2
                                                                                            ),
                                                                                            PkTileSet.of(3, TileKind.B) // factory3
                                                                                    ),
                                                                                    PkTileSet.of(4, TileKind.E) // factory4
                                                                            ),
                                                                            PkTileSet.union(PkTileSet.of(2, TileKind.C), PkTileSet.of(2, TileKind.D)) // factory5
                                                                    ),
                                                                    PkPatterns.asPkTileSet(
                                                                            PkPatterns.withAddedTiles(
                                                                                    PkPatterns.withAddedTiles(
                                                                                            PkPatterns.EMPTY,
                                                                                            TileDestination.Pattern.PATTERN_2,
                                                                                            2,
                                                                                            TileKind.Colored.B
                                                                                    ),
                                                                                    TileDestination.Pattern.PATTERN_4,
                                                                                    3,
                                                                                    TileKind.Colored.D
                                                                            )
                                                                    )
                                                            ),
                                                            PkFloor.asPkTileSet(
                                                                    PkFloor.withAddedTiles(
                                                                            PkFloor.EMPTY,
                                                                            PkTileSet.of(1, TileKind.A)
                                                                    )
                                                            )
                                                    ),
                                                    PkWall.asPkTileSet(
                                                            PkWall.withTileAt(
                                                                    PkWall.withTileAt(
                                                                            PkWall.EMPTY,
                                                                            TileDestination.Pattern.PATTERN_1,
                                                                            TileKind.Colored.A
                                                                    ),
                                                                    TileDestination.Pattern.PATTERN_3,
                                                                    TileKind.Colored.C
                                                            )
                                                    )
                                            ),
                                            PkPatterns.asPkTileSet(
                                                    PkPatterns.withAddedTiles(
                                                            PkPatterns.withAddedTiles(
                                                                    PkPatterns.EMPTY,
                                                                    TileDestination.Pattern.PATTERN_1,
                                                                    1,
                                                                    TileKind.Colored.E
                                                            ),
                                                            TileDestination.Pattern.PATTERN_3,
                                                            2,
                                                            TileKind.Colored.C
                                                    )
                                            )
                                    ),
                                    PkFloor.asPkTileSet(
                                            PkFloor.withAddedTiles(
                                                    PkFloor.EMPTY,
                                                    PkTileSet.of(2, TileKind.B)
                                            )
                                    )
                            ),
                            PkWall.asPkTileSet(
                                    PkWall.withTileAt(
                                            PkWall.withTileAt(
                                                    PkWall.EMPTY,
                                                    TileDestination.Pattern.PATTERN_4,
                                                    TileKind.Colored.B
                                            ),
                                            TileDestination.Pattern.PATTERN_5,
                                            TileKind.Colored.A
                                    )
                            )
                    )
            ),
            ImmutableIntArray.copyOf(new int[]{
                    PkTileSet.of(2, TileKind.C),
                    PkTileSet.union(PkTileSet.of(2, TileKind.A), PkTileSet.of(2, TileKind.B)),
                    PkTileSet.union(PkTileSet.of(1, TileKind.A), PkTileSet.of(3, TileKind.D)),
                    PkTileSet.of(3, TileKind.B),
                    PkTileSet.of(4, TileKind.E),
                    PkTileSet.union(PkTileSet.of(2, TileKind.C), PkTileSet.of(2, TileKind.D))
            }),
            PkIntSet32.add(
                    PkIntSet32.add(
                            PkIntSet32.add(
                                    PkIntSet32.add(
                                            PkIntSet32.add(
                                                    PkIntSet32.add(PkIntSet32.EMPTY, 0),
                                                    1
                                            ),
                                            2
                                    ),
                                    3
                            ),
                            4
                    ),
                    5
            ),
            ImmutableIntArray.copyOf(new int[]{
                    PkPatterns.withAddedTiles(
                            PkPatterns.withAddedTiles(
                                    PkPatterns.EMPTY,
                                    TileDestination.Pattern.PATTERN_2,
                                    2,
                                    TileKind.Colored.B
                            ),
                            TileDestination.Pattern.PATTERN_4,
                            3,
                            TileKind.Colored.D
                    ),
                    PkFloor.withAddedTiles(
                            PkFloor.EMPTY,
                            PkTileSet.of(1, TileKind.A)
                    ),
                    PkWall.withTileAt(
                            PkWall.withTileAt(
                                    PkWall.EMPTY,
                                    TileDestination.Pattern.PATTERN_1,
                                    TileKind.Colored.A
                            ),
                            TileDestination.Pattern.PATTERN_3,
                            TileKind.Colored.C
                    ),
                    7,

                    PkPatterns.withAddedTiles(
                            PkPatterns.withAddedTiles(
                                    PkPatterns.EMPTY,
                                    TileDestination.Pattern.PATTERN_1,
                                    1,
                                    TileKind.Colored.E
                            ),
                            TileDestination.Pattern.PATTERN_3,
                            2,
                            TileKind.Colored.C
                    ),
                    PkFloor.withAddedTiles(
                            PkFloor.EMPTY,
                            PkTileSet.of(2, TileKind.B)
                    ),
                    PkWall.withTileAt(
                            PkWall.withTileAt(
                                    PkWall.EMPTY,
                                    TileDestination.Pattern.PATTERN_4,
                                    TileKind.Colored.B
                            ),
                            TileDestination.Pattern.PATTERN_5,
                            TileKind.Colored.A
                    ),
                    12
            }),
            PlayerId.P1
    );


    ImmutableGameState anotherState = new ImmutableGameState(
            initialState.game(),
            PkTileSet.EMPTY,
            ImmutableIntArray.copyOf(new int[]{
                    PkTileSet.union(
                            PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER),
                            PkTileSet.union(
                                    PkTileSet.of(1, TileKind.A),
                                    PkTileSet.of(2, TileKind.D)
                            )
                    ), // center area
                    PkTileSet.of(4, TileKind.C), // factory 1
                    PkTileSet.union(
                            PkTileSet.of(2, TileKind.B),
                            PkTileSet.of(2, TileKind.E)
                    ), // factory 2
                    PkTileSet.union(
                            PkTileSet.union(
                                    PkTileSet.of(1, TileKind.A),
                                    PkTileSet.of(1, TileKind.B)
                            ),
                            PkTileSet.of(2, TileKind.C)
                    ), // factory 3
                    PkTileSet.EMPTY, // factory 4
                    PkTileSet.union(
                            PkTileSet.of(3, TileKind.D),
                            PkTileSet.of(1, TileKind.E)
                    ) // factory 5
            }),
            PkIntSet32.add(
                    PkIntSet32.add(
                            PkIntSet32.add(
                                    PkIntSet32.add(
                                            PkIntSet32.add(PkIntSet32.EMPTY, 0),
                                            1
                                    ),
                                    2
                            ),
                            3
                    ),
                    5
            ),
            ImmutableIntArray.copyOf(new int[]{
                    PkPatterns.withAddedTiles(
                            PkPatterns.withAddedTiles(
                                    PkPatterns.EMPTY,
                                    TileDestination.Pattern.PATTERN_1,
                                    1,
                                    TileKind.Colored.A
                            ),
                            TileDestination.Pattern.PATTERN_5,
                            4,
                            TileKind.Colored.E
                    ),
                    PkFloor.withAddedTiles(
                            PkFloor.EMPTY,
                            PkTileSet.of(1, TileKind.B)
                    ),
                    PkWall.withTileAt(
                            PkWall.withTileAt(
                                    PkWall.EMPTY,
                                    TileDestination.Pattern.PATTERN_2,
                                    TileKind.Colored.D
                            ),
                            TileDestination.Pattern.PATTERN_4,
                            TileKind.Colored.A
                    ),
                    9,

                    PkPatterns.withAddedTiles(
                            PkPatterns.withAddedTiles(
                                    PkPatterns.EMPTY,
                                    TileDestination.Pattern.PATTERN_2,
                                    1,
                                    TileKind.Colored.C
                            ),
                            TileDestination.Pattern.PATTERN_4,
                            2,
                            TileKind.Colored.B
                    ),
                    PkFloor.withAddedTiles(
                            PkFloor.EMPTY,
                            PkTileSet.union(
                                    PkTileSet.of(1, TileKind.A),
                                    PkTileSet.of(1, TileKind.D)
                            )
                    ),
                    PkWall.withTileAt(
                            PkWall.EMPTY,
                            TileDestination.Pattern.PATTERN_3,
                            TileKind.Colored.E
                    ),
                    6
            }),
            PlayerId.P2
    );

    ImmutableGameState thirdState = new ImmutableGameState(
            initialState.game(),
            PkTileSet.EMPTY,
            ImmutableIntArray.copyOf(new int[]{
                    PkTileSet.union(
                            PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER),
                            PkTileSet.of(3, TileKind.B)
                    ), // center area
                    PkTileSet.union(
                            PkTileSet.of(2, TileKind.A),
                            PkTileSet.of(2, TileKind.E)
                    ), // factory 1
                    PkTileSet.of(4, TileKind.D), // factory 2
                    PkTileSet.union(
                            PkTileSet.of(1, TileKind.C),
                            PkTileSet.of(3, TileKind.E)
                    ), // factory 3
                    PkTileSet.union(
                            PkTileSet.of(2, TileKind.B),
                            PkTileSet.of(2, TileKind.C)
                    ), // factory 4
                    PkTileSet.of(4, TileKind.A) // factory 5
            }),
            PkIntSet32.add(
                    PkIntSet32.add(
                            PkIntSet32.add(
                                    PkIntSet32.add(
                                            PkIntSet32.add(
                                                    PkIntSet32.add(PkIntSet32.EMPTY, 0),
                                                    1
                                            ),
                                            2
                                    ),
                                    3
                            ),
                            4
                    ),
                    5
            ),
            ImmutableIntArray.copyOf(new int[]{
                    PkPatterns.withAddedTiles(
                            PkPatterns.withAddedTiles(
                                    PkPatterns.EMPTY,
                                    TileDestination.Pattern.PATTERN_3,
                                    2,
                                    TileKind.Colored.C
                            ),
                            TileDestination.Pattern.PATTERN_5,
                            1,
                            TileKind.Colored.E
                    ),
                    PkFloor.withAddedTiles(
                            PkFloor.EMPTY,
                            PkTileSet.of(2, TileKind.A)
                    ),
                    PkWall.withTileAt(
                            PkWall.withTileAt(
                                    PkWall.EMPTY,
                                    TileDestination.Pattern.PATTERN_1,
                                    TileKind.Colored.B
                            ),
                            TileDestination.Pattern.PATTERN_5,
                            TileKind.Colored.D
                    ),
                    5,

                    PkPatterns.withAddedTiles(
                            PkPatterns.withAddedTiles(
                                    PkPatterns.EMPTY,
                                    TileDestination.Pattern.PATTERN_2,
                                    2,
                                    TileKind.Colored.D
                            ),
                            TileDestination.Pattern.PATTERN_4,
                            3,
                            TileKind.Colored.A
                    ),
                    PkFloor.withAddedTiles(
                            PkFloor.EMPTY,
                            PkTileSet.union(
                                    PkTileSet.of(1, TileKind.C),
                                    PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER)
                            )
                    ),
                    PkWall.withTileAt(
                            PkWall.EMPTY,
                            TileDestination.Pattern.PATTERN_2,
                            TileKind.Colored.E
                    ),
                    11
            }),
            PlayerId.P2
    );

    ImmutableGameState roundOverState = new ImmutableGameState(
            initialState.game(),
            PkTileSet.of(5, TileKind.A),
            ImmutableIntArray.copyOf(new int[]{
                    PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER),
                    PkTileSet.EMPTY,
                    PkTileSet.EMPTY,
                    PkTileSet.EMPTY,
                    PkTileSet.EMPTY,
                    PkTileSet.EMPTY
            }),
            PkIntSet32.EMPTY,
            ImmutableIntArray.copyOf(new int[]{
                    PkPatterns.EMPTY, PkFloor.EMPTY, PkWall.EMPTY, 0,
                    PkPatterns.EMPTY, PkFloor.EMPTY, PkWall.EMPTY, 0
            }),
            PlayerId.P1
    );

    private static Set<Short> asSet(short[] moves, int count) {
        Set<Short> s = new HashSet<>();
        for (int i = 0; i < count; i++) s.add(moves[i]);
        return s;
    }

    private static boolean hasColored(int pkTileSet) {
        for (TileKind.Colored c : TileKind.Colored.ALL) {
            if (PkTileSet.countOf(pkTileSet, c) > 0) return true;
        }
        return false;
    }

    private static int onlyColored(int pkTileSet) {
        int colored = PkTileSet.EMPTY;
        for (TileKind.Colored c : TileKind.Colored.ALL) {
            colored = PkTileSet.union(colored, PkTileSet.subsetOf(pkTileSet, c));
        }
        return colored;
    }

    private static boolean isAccountedTileSetValid(int pkTileSet) {
        if (PkTileSet.countOf(pkTileSet, TileKind.FIRST_PLAYER_MARKER) > 1) return false;
        for (TileKind.Colored c : TileKind.Colored.ALL) {
            if (PkTileSet.countOf(pkTileSet, c) > c.tilesCount()) return false;
        }
        return true;
    }

    private static int oracleUniqueSources(int[] pkSources) {
        int unique = PkIntSet32.EMPTY;

        // center area
        if (hasColored(pkSources[0])) {
            unique = PkIntSet32.add(unique, 0);
        }

        // factories: unique iff colored and no previous factory has same contents
        for (int i = 1; i < pkSources.length; i++) {
            if (!hasColored(pkSources[i])) continue;

            boolean alreadySeenInPreviousFactory = false;
            for (int j = 1; j < i; j++) {
                if (pkSources[j] == pkSources[i]) {
                    alreadySeenInPreviousFactory = true;
                    break;
                }
            }

            if (!alreadySeenInPreviousFactory) {
                unique = PkIntSet32.add(unique, i);
            }
        }

        return unique;
    }

    private static boolean oracleIsRoundOver(ReadOnlyGameState state) {
        for (int i = 0; i < state.pkTileSources().size(); i++) {
            if (hasColored(state.pkTileSources().get(i))) return false;
        }
        return true;
    }

    private static boolean oracleIsGameOver(ReadOnlyGameState state) {
        if (!oracleIsRoundOver(state)) return false;

        for (PlayerId p : state.playerIds()) {
            if (PkWall.hasFullRow(PkPlayerStates.pkWall(state.pkPlayerStates(), p))) {
                return true;
            }
        }
        return false;
    }

    private static int oracleDiscardedTiles(ReadOnlyGameState state) {
        int accounted = state.pkTileBag();

        for (int i = 0; i < state.pkTileSources().size(); i++) {
            accounted = PkTileSet.union(accounted, state.pkTileSources().get(i));
        }

        for (PlayerId p : state.playerIds()) {
            accounted = PkTileSet.union(
                    accounted,
                    PkPatterns.asPkTileSet(PkPlayerStates.pkPatterns(state.pkPlayerStates(), p))
            );
            accounted = PkTileSet.union(
                    accounted,
                    PkFloor.asPkTileSet(PkPlayerStates.pkFloor(state.pkPlayerStates(), p))
            );
            accounted = PkTileSet.union(
                    accounted,
                    PkWall.asPkTileSet(PkPlayerStates.pkWall(state.pkPlayerStates(), p))
            );
        }

        return PkTileSet.difference(PkTileSet.FULL, accounted);
    }

    private static Set<Short> oracleMoves(ReadOnlyGameState state, boolean uniqueOnly) {
        Set<Short> expected = new HashSet<>();

        PlayerId current = state.currentPlayerId();
        int pkPatterns = PkPlayerStates.pkPatterns(state.pkPlayerStates(), current);
        int pkWall = PkPlayerStates.pkWall(state.pkPlayerStates(), current);

        for (int i = 0; i < state.pkTileSources().size(); i++) {
            if (uniqueOnly && !PkIntSet32.contains(state.pkUniqueTileSources(), i)) continue;

            int pkSource = state.pkTileSources().get(i);
            TileSource source = TileSource.ALL.get(i);

            for (TileKind.Colored color : TileKind.Colored.ALL) {
                if (PkTileSet.countOf(pkSource, color) == 0) continue;

                // Floor is always playable
                expected.add(PkMove.pack(source, color, TileDestination.FLOOR));

                for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
                    boolean playableOnPattern =
                            !PkPatterns.isFull(pkPatterns, line)
                                    && PkPatterns.canContain(pkPatterns, line, color)
                                    && !PkWall.hasTileAt(pkWall, line, color);

                    if (playableOnPattern) {
                        expected.add(PkMove.pack(source, color, line));
                    }
                }
            }
        }

        return expected;
    }

    private static int[] pState(int pat, int floor, int wall, int pts) {
        return new int[]{pat, floor, wall, pts};
    }

    @Test
    void playerIds() {
        assertEquals(PlayerId.P1, game.playerIds().get(0));
        assertEquals(PlayerId.P2, game.playerIds().get(1));
        assertEquals(PlayerId.P1, game1.playerIds().get(0));
        assertEquals(PlayerId.P2, game1.playerIds().get(1));
        assertEquals(PlayerId.P3, game1.playerIds().get(2));
        assertEquals(PlayerId.P4, game1.playerIds().get(3));
        assertEquals(PlayerId.P1, game2.playerIds().get(0));
        assertEquals(PlayerId.P2, game2.playerIds().get(1));
    }


    @Test
    void isRoundOver() {
        assertFalse(anotherState.isRoundOver());
        assertFalse(mockState.isRoundOver());
        assertFalse(thirdState.isRoundOver());
        assertTrue(roundOverState.isRoundOver());


    }

    // ═══════════════════════════════════════════════════════════
    // Etats de jeu pour les tests
    // ═══════════════════════════════════════════════════════════

    static int[] ps2(int pat1, int floor1, int wall1, int pts1,
                     int pat2, int floor2, int wall2, int pts2) {
        return new int[]{pat1, floor1, wall1, pts1, pat2, floor2, wall2, pts2};
    }

    static final Game GAME_2P_STATIC = new Game(List.of(
            new Game.PlayerDescription(PlayerId.P1, "Hedi", Game.PlayerDescription.PlayerKind.HUMAN),
            new Game.PlayerDescription(PlayerId.P2, "Fahd", Game.PlayerDescription.PlayerKind.AI)
    ));

    // --- MID_ROUND : fab1={2A,2B}, fab2={2A,2B}(doublon), fab3={4C} ---
    static final int MID_BAG;
    static {
        int tilesOnSources = PkTileSet.union(
                PkTileSet.union(
                        PkTileSet.union(PkTileSet.of(2, TileKind.Colored.A), PkTileSet.of(2, TileKind.Colored.B)),
                        PkTileSet.union(PkTileSet.of(2, TileKind.Colored.A), PkTileSet.of(2, TileKind.Colored.B))),
                PkTileSet.of(4, TileKind.Colored.C));
        MID_BAG = PkTileSet.difference(PkTileSet.FULL_COLORED, tilesOnSources);
    }

    static final ImmutableGameState MID_ROUND_2P = new ImmutableGameState(
            GAME_2P_STATIC, MID_BAG,
            ImmutableIntArray.copyOf(new int[]{
                    PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER),
                    PkTileSet.union(PkTileSet.of(2, TileKind.Colored.A), PkTileSet.of(2, TileKind.Colored.B)),
                    PkTileSet.union(PkTileSet.of(2, TileKind.Colored.A), PkTileSet.of(2, TileKind.Colored.B)),
                    PkTileSet.of(4, TileKind.Colored.C),
                    PkTileSet.EMPTY, PkTileSet.EMPTY
            }),
            (1 << 1) | (1 << 3),
            ImmutableIntArray.copyOf(ps2(
                    PkPatterns.EMPTY, PkFloor.EMPTY, PkWall.EMPTY, 0,
                    PkPatterns.EMPTY, PkFloor.EMPTY, PkWall.EMPTY, 0)),
            PlayerId.P1
    );

    // --- ROUND_OVER : pas de game over (pas de ligne mur complete) ---
    static final int WALL_NO_FULL_ROW = PkWall.withTileAt(
            PkWall.withTileAt(PkWall.EMPTY,
                    TileDestination.Pattern.PATTERN_1, TileKind.Colored.A),
            TileDestination.Pattern.PATTERN_1, TileKind.Colored.B);

    static final ImmutableGameState ROUND_OVER_2P = new ImmutableGameState(
            GAME_2P_STATIC, PkTileSet.FULL_COLORED,
            ImmutableIntArray.copyOf(new int[]{0, 0, 0, 0, 0, 0}),
            PkIntSet32.EMPTY,
            ImmutableIntArray.copyOf(ps2(
                    PkPatterns.EMPTY, PkFloor.EMPTY, WALL_NO_FULL_ROW, 15,
                    PkPatterns.EMPTY, PkFloor.EMPTY, PkWall.EMPTY, 5)),
            PlayerId.P1
    );

    // --- GAME_OVER : ligne 1 du mur complete ---
    static final int WALL_FULL_ROW1 = PkWall.withTileAt(
            PkWall.withTileAt(PkWall.withTileAt(PkWall.withTileAt(
                    PkWall.withTileAt(PkWall.EMPTY,
                            TileDestination.Pattern.PATTERN_1, TileKind.Colored.A),
                    TileDestination.Pattern.PATTERN_1, TileKind.Colored.B),
                    TileDestination.Pattern.PATTERN_1, TileKind.Colored.C),
                    TileDestination.Pattern.PATTERN_1, TileKind.Colored.D),
            TileDestination.Pattern.PATTERN_1, TileKind.Colored.E);

    static final ImmutableGameState GAME_OVER_2P = new ImmutableGameState(
            GAME_2P_STATIC, PkTileSet.FULL_COLORED,
            ImmutableIntArray.copyOf(new int[]{0, 0, 0, 0, 0, 0}),
            PkIntSet32.EMPTY,
            ImmutableIntArray.copyOf(ps2(
                    PkPatterns.EMPTY, PkFloor.EMPTY, WALL_FULL_ROW1, 42,
                    PkPatterns.EMPTY, PkFloor.EMPTY, PkWall.EMPTY, 10)),
            PlayerId.P1
    );

    // --- CONSTRAINED : P1 a PAT1 pleine (1xA), mur a A sur PAT2, fab1={3A,1B} ---
    static final int PAT_CONSTRAINED = PkPatterns.withAddedTiles(PkPatterns.EMPTY,
            TileDestination.Pattern.PATTERN_1, 1, TileKind.Colored.A);
    static final int WALL_CONSTRAINED = PkWall.withTileAt(PkWall.EMPTY,
            TileDestination.Pattern.PATTERN_2, TileKind.Colored.A);

    static final ImmutableGameState MID_CONSTRAINED = new ImmutableGameState(
            GAME_2P_STATIC, PkTileSet.FULL_COLORED,
            ImmutableIntArray.copyOf(new int[]{
                    PkTileSet.EMPTY,
                    PkTileSet.union(PkTileSet.of(3, TileKind.Colored.A), PkTileSet.of(1, TileKind.Colored.B)),
                    PkTileSet.EMPTY, PkTileSet.EMPTY, PkTileSet.EMPTY, PkTileSet.EMPTY
            }),
            (1 << 1),
            ImmutableIntArray.copyOf(ps2(
                    PAT_CONSTRAINED, PkFloor.EMPTY, WALL_CONSTRAINED, 5,
                    PkPatterns.EMPTY, PkFloor.EMPTY, PkWall.EMPTY, 0)),
            PlayerId.P1
    );

    // --- MIXED_2P : manche en cours, centre colore, quelques sources vides ---
    static final int MIXED_2P_BAG;
    static {
        int visibleColored = PkTileSet.union(
                PkTileSet.union(
                        PkTileSet.union(
                                PkTileSet.union(
                                        PkTileSet.union(
                                                // sources colorees (sans compter le marqueur)
                                                PkTileSet.of(2, TileKind.Colored.D), // center
                                                PkTileSet.union(
                                                        PkTileSet.of(2, TileKind.Colored.A),
                                                        PkTileSet.of(2, TileKind.Colored.C))
                                        ),
                                        PkTileSet.union(
                                                PkTileSet.of(1, TileKind.Colored.B),
                                                PkTileSet.of(3, TileKind.Colored.E))
                                ),
                                PkTileSet.of(4, TileKind.Colored.A)
                        ),
                        // joueur 1
                        PkPatterns.asPkTileSet(
                                PkPatterns.withAddedTiles(
                                        PkPatterns.withAddedTiles(
                                                PkPatterns.EMPTY,
                                                TileDestination.Pattern.PATTERN_2,
                                                1,
                                                TileKind.Colored.C),
                                        TileDestination.Pattern.PATTERN_5,
                                        2,
                                        TileKind.Colored.A))
                ),
                PkTileSet.union(
                        PkFloor.asPkTileSet(
                                PkFloor.withAddedTiles(
                                        PkFloor.EMPTY,
                                        PkTileSet.of(1, TileKind.Colored.B))),
                        PkTileSet.union(
                                PkWall.asPkTileSet(
                                        PkWall.withTileAt(
                                                PkWall.EMPTY,
                                                TileDestination.Pattern.PATTERN_1,
                                                TileKind.Colored.E)),
                                PkTileSet.union(
                                        // joueur 2
                                        PkPatterns.asPkTileSet(
                                                PkPatterns.withAddedTiles(
                                                        PkPatterns.EMPTY,
                                                        TileDestination.Pattern.PATTERN_3,
                                                        2,
                                                        TileKind.Colored.E)),
                                        PkTileSet.union(
                                                PkFloor.asPkTileSet(
                                                        PkFloor.withAddedTiles(
                                                                PkFloor.EMPTY,
                                                                PkTileSet.of(1, TileKind.Colored.C))),
                                                PkWall.asPkTileSet(
                                                        PkWall.withTileAt(
                                                                PkWall.EMPTY,
                                                                TileDestination.Pattern.PATTERN_4,
                                                                TileKind.Colored.B)))
                                )
                        )
                )
        );

        MIXED_2P_BAG = PkTileSet.difference(PkTileSet.FULL_COLORED, visibleColored);
    }

    static final ImmutableGameState MIXED_2P = new ImmutableGameState(
            GAME_2P_STATIC,
            MIXED_2P_BAG,
            ImmutableIntArray.copyOf(new int[]{
                    PkTileSet.union(
                            PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER),
                            PkTileSet.of(2, TileKind.Colored.D)
                    ), // center
                    PkTileSet.union(
                            PkTileSet.of(2, TileKind.Colored.A),
                            PkTileSet.of(2, TileKind.Colored.C)
                    ), // factory 1
                    PkTileSet.EMPTY, // factory 2
                    PkTileSet.union(
                            PkTileSet.of(1, TileKind.Colored.B),
                            PkTileSet.of(3, TileKind.Colored.E)
                    ), // factory 3
                    PkTileSet.of(4, TileKind.Colored.A), // factory 4
                    PkTileSet.EMPTY // factory 5
            }),
            (1 << 0) | (1 << 1) | (1 << 3) | (1 << 4),
            ImmutableIntArray.copyOf(ps2(
                    PkPatterns.withAddedTiles(
                            PkPatterns.withAddedTiles(
                                    PkPatterns.EMPTY,
                                    TileDestination.Pattern.PATTERN_2,
                                    1,
                                    TileKind.Colored.C),
                            TileDestination.Pattern.PATTERN_5,
                            2,
                            TileKind.Colored.A),
                    PkFloor.withAddedTiles(
                            PkFloor.EMPTY,
                            PkTileSet.of(1, TileKind.Colored.B)),
                    PkWall.withTileAt(
                            PkWall.EMPTY,
                            TileDestination.Pattern.PATTERN_1,
                            TileKind.Colored.E),
                    8,

                    PkPatterns.withAddedTiles(
                            PkPatterns.EMPTY,
                            TileDestination.Pattern.PATTERN_3,
                            2,
                            TileKind.Colored.E),
                    PkFloor.withAddedTiles(
                            PkFloor.EMPTY,
                            PkTileSet.of(1, TileKind.Colored.C)),
                    PkWall.withTileAt(
                            PkWall.EMPTY,
                            TileDestination.Pattern.PATTERN_4,
                            TileKind.Colored.B),
                    11
            )),
            PlayerId.P2
    );

    // ═══════════════════════════════════════════════════════════
    // isGameOver
    // ═══════════════════════════════════════════════════════════

    @Test
    void isGameOverFalseWhenRoundNotOver() {
        assertFalse(MID_ROUND_2P.isGameOver());
    }

    @Test
    void isGameOverFalseWhenRoundOverButNoFullRow() {
        assertFalse(ROUND_OVER_2P.isGameOver());
    }

    @Test
    void isGameOverTrueWhenRoundOverAndFullRow() {
        assertTrue(GAME_OVER_2P.isGameOver());
    }

    @Test
    void isGameOverDetectsFullRowOnP2() {
        ImmutableGameState go = new ImmutableGameState(
                GAME_2P_STATIC, PkTileSet.FULL_COLORED,
                ImmutableIntArray.copyOf(new int[]{0, 0, 0, 0, 0, 0}),
                PkIntSet32.EMPTY,
                ImmutableIntArray.copyOf(ps2(
                        PkPatterns.EMPTY, PkFloor.EMPTY, PkWall.EMPTY, 0,
                        PkPatterns.EMPTY, PkFloor.EMPTY, WALL_FULL_ROW1, 30)),
                PlayerId.P1);
        assertTrue(go.isGameOver());
    }

    // ═══════════════════════════════════════════════════════════
    // pkDiscardedTiles
    // ═══════════════════════════════════════════════════════════

    @Test
    void pkDiscardedTilesEmptyOnInitialState() {
        assertEquals(PkTileSet.EMPTY, ImmutableGameState.initial(GAME_2P_STATIC).pkDiscardedTiles());
    }

    @Test
    void pkDiscardedTilesEmptyWhenAllTilesAccountedFor() {
        assertEquals(PkTileSet.EMPTY, MID_ROUND_2P.pkDiscardedTiles());
    }

    @Test
    void pkDiscardedTilesDetectsMissingTiles() {
        // Bag a 15A au lieu de 20A → 5A manquantes
        int smallBag = PkTileSet.difference(PkTileSet.FULL_COLORED, PkTileSet.of(5, TileKind.Colored.A));
        ImmutableGameState state = new ImmutableGameState(
                GAME_2P_STATIC, smallBag,
                ImmutableIntArray.copyOf(new int[]{
                        PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER), 0, 0, 0, 0, 0}),
                PkIntSet32.EMPTY,
                ImmutableIntArray.copyOf(ps2(
                        PkPatterns.EMPTY, PkFloor.EMPTY, PkWall.EMPTY, 0,
                        PkPatterns.EMPTY, PkFloor.EMPTY, PkWall.EMPTY, 0)),
                PlayerId.P1);
        assertEquals(PkTileSet.of(5, TileKind.Colored.A), state.pkDiscardedTiles());
    }

    @Test
    void pkDiscardedTilesAccountsForWallAndPatterns() {
        // P1 a 1A sur PAT1 + A sur mur PAT2 = 2A sur le board
        int pat = PkPatterns.withAddedTiles(PkPatterns.EMPTY,
                TileDestination.Pattern.PATTERN_1, 1, TileKind.Colored.A);
        int wall = PkWall.withTileAt(PkWall.EMPTY, TileDestination.Pattern.PATTERN_2, TileKind.Colored.A);
        int bag = PkTileSet.difference(PkTileSet.FULL_COLORED, PkTileSet.of(2, TileKind.Colored.A));

        ImmutableGameState state = new ImmutableGameState(
                GAME_2P_STATIC, bag,
                ImmutableIntArray.copyOf(new int[]{
                        PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER), 0, 0, 0, 0, 0}),
                PkIntSet32.EMPTY,
                ImmutableIntArray.copyOf(ps2(
                        pat, PkFloor.EMPTY, wall, 10,
                        PkPatterns.EMPTY, PkFloor.EMPTY, PkWall.EMPTY, 0)),
                PlayerId.P1);
        assertEquals(PkTileSet.EMPTY, state.pkDiscardedTiles());
    }

    // ═══════════════════════════════════════════════════════════
    // validMoves
    // ═══════════════════════════════════════════════════════════

    @Test
    void validMovesOnMidRoundEmptyBoard() {
        // fab1 {2A,2B}: A→5 patterns+FLOOR=6, B→6 = 12
        // fab2 {2A,2B}: idem = 12
        // fab3 {4C}: C→6
        // center: marker seulement → 0
        // Total: 30
        short[] moves = new short[Move.MAX_MOVES];
        int count = MID_ROUND_2P.validMoves(moves);
        assertEquals(30, count);
    }

    @Test
    void validMovesContainsExpectedMoves() {
        short[] moves = new short[Move.MAX_MOVES];
        int count = MID_ROUND_2P.validMoves(moves);
        Set<Short> moveSet = new HashSet<>();
        for (int i = 0; i < count; i++) moveSet.add(moves[i]);

        // fab1 + A → PAT1
        assertTrue(moveSet.contains(PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.A,
                TileDestination.Pattern.PATTERN_1)));
        // fab1 + B → FLOOR
        assertTrue(moveSet.contains(PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.B,
                TileDestination.FLOOR)));
        // fab3 + C → PAT3
        assertTrue(moveSet.contains(PkMove.pack(TileSource.FACTORY_3, TileKind.Colored.C,
                TileDestination.Pattern.PATTERN_3)));
        // center + A ne doit PAS exister
        assertFalse(moveSet.contains(PkMove.pack(TileSource.CENTER_AREA, TileKind.Colored.A,
                TileDestination.Pattern.PATTERN_1)));
    }

    @Test
    void validMovesWithConstrainedBoard() {
        // fab1 {3A,1B}, P1: PAT1 pleine (1xA), mur a A sur PAT2
        // A: PAT1 pleine, PAT2 bloquee par mur → PAT3,PAT4,PAT5,FLOOR = 4
        // B: PAT1 a deja A (canContain=false) → PAT2,PAT3,PAT4,PAT5,FLOOR = 5
        // Total: 9
        short[] moves = new short[Move.MAX_MOVES];
        int count = MID_CONSTRAINED.validMoves(moves);
        assertEquals(9, count);
    }

    @Test
    void validMovesReturnsZeroWhenRoundOver() {
        short[] moves = new short[Move.MAX_MOVES];
        assertEquals(0, ROUND_OVER_2P.validMoves(moves));
    }

    // ═══════════════════════════════════════════════════════════
    // uniqueValidMoves
    // ═══════════════════════════════════════════════════════════

    @Test
    void uniqueValidMovesIgnoresDuplicateSources() {
        // uniqueSources = {1, 3}, fab2 ignoree (doublon de fab1)
        // fab1: 12, fab3: 6 → total 18
        short[] moves = new short[Move.MAX_MOVES];
        int count = MID_ROUND_2P.uniqueValidMoves(moves);
        assertEquals(18, count);
    }

    @Test
    void uniqueValidMovesSubsetOfValidMoves() {
        short[] all = new short[Move.MAX_MOVES];
        int allCount = MID_ROUND_2P.validMoves(all);
        Set<Short> allSet = new HashSet<>();
        for (int i = 0; i < allCount; i++) allSet.add(all[i]);

        short[] unique = new short[Move.MAX_MOVES];
        int uniqueCount = MID_ROUND_2P.uniqueValidMoves(unique);

        for (int i = 0; i < uniqueCount; i++)
            assertTrue(allSet.contains(unique[i]));
        assertTrue(uniqueCount < allCount);
    }

    @Test
    void uniqueValidMovesEqualsValidWhenNoDuplicates() {
        // fab1={4A}, fab2={4B}, fab3={4C}: toutes differentes
        ImmutableGameState noDups = new ImmutableGameState(
                GAME_2P_STATIC, PkTileSet.FULL_COLORED,
                ImmutableIntArray.copyOf(new int[]{
                        PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER),
                        PkTileSet.of(4, TileKind.Colored.A),
                        PkTileSet.of(4, TileKind.Colored.B),
                        PkTileSet.of(4, TileKind.Colored.C),
                        PkTileSet.EMPTY, PkTileSet.EMPTY
                }),
                (1 << 1) | (1 << 2) | (1 << 3),
                ImmutableIntArray.copyOf(ps2(
                        PkPatterns.EMPTY, PkFloor.EMPTY, PkWall.EMPTY, 0,
                        PkPatterns.EMPTY, PkFloor.EMPTY, PkWall.EMPTY, 0)),
                PlayerId.P1);

        short[] all = new short[Move.MAX_MOVES];
        short[] unique = new short[Move.MAX_MOVES];
        assertEquals(noDups.validMoves(all), noDups.uniqueValidMoves(unique));
    }

    @Test
    void uniqueValidMovesReturnsZeroWhenRoundOver() {
        short[] moves = new short[Move.MAX_MOVES];
        assertEquals(0, ROUND_OVER_2P.uniqueValidMoves(moves));
    }

    @Test
    void uniqueValidMovesWithConstrainedBoard() {
        // uniqueSources = {1}, meme result que validMoves
        short[] moves = new short[Move.MAX_MOVES];
        assertEquals(9, MID_CONSTRAINED.uniqueValidMoves(moves));
    }

    @Test
    void mixed2pRoundIsNotOver() {
        assertFalse(MIXED_2P.isRoundOver());
    }

    @Test
    void mixed2pGameIsNotOver() {
        assertFalse(MIXED_2P.isGameOver());
    }

    @Test
    void mixed2pHasValidMoves() {
        short[] moves = new short[Move.MAX_MOVES];
        assertTrue(MIXED_2P.validMoves(moves) > 0);
    }

    @Test
    void mixed2pUniqueValidMovesNotGreaterThanValidMoves() {
        short[] all = new short[Move.MAX_MOVES];
        short[] unique = new short[Move.MAX_MOVES];
        assertTrue(MIXED_2P.uniqueValidMoves(unique) <= MIXED_2P.validMoves(all));
    }

    @Test
    void bruteForceReadOnlyGameStateDefaultMethods() {
        int[] sourceCatalog = new int[]{
                PkTileSet.EMPTY,
                PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER),
                PkTileSet.of(1, TileKind.Colored.A),
                PkTileSet.union(
                        PkTileSet.of(1, TileKind.Colored.B),
                        PkTileSet.of(1, TileKind.Colored.C)
                ),
                PkTileSet.of(2, TileKind.Colored.D)
        };

        int[][] p1Catalog = new int[][]{
                pState(PkPatterns.EMPTY, PkFloor.EMPTY, PkWall.EMPTY, 0),

                pState(
                        PkPatterns.withAddedTiles(
                                PkPatterns.EMPTY,
                                TileDestination.Pattern.PATTERN_1,
                                1,
                                TileKind.Colored.A
                        ),
                        PkFloor.withAddedTiles(
                                PkFloor.EMPTY,
                                PkTileSet.of(1, TileKind.Colored.B)
                        ),
                        PkWall.withTileAt(
                                PkWall.EMPTY,
                                TileDestination.Pattern.PATTERN_2,
                                TileKind.Colored.A
                        ),
                        5
                ),

                pState(
                        PkPatterns.withAddedTiles(
                                PkPatterns.withAddedTiles(
                                        PkPatterns.EMPTY,
                                        TileDestination.Pattern.PATTERN_2,
                                        1,
                                        TileKind.Colored.C
                                ),
                                TileDestination.Pattern.PATTERN_4,
                                2,
                                TileKind.Colored.D
                        ),
                        PkFloor.withAddedTiles(
                                PkFloor.EMPTY,
                                PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER)
                        ),
                        PkWall.withTileAt(
                                PkWall.EMPTY,
                                TileDestination.Pattern.PATTERN_5,
                                TileKind.Colored.B
                        ),
                        9
                )
        };

        int[][] p2Catalog = new int[][]{
                pState(PkPatterns.EMPTY, PkFloor.EMPTY, PkWall.EMPTY, 0),

                pState(
                        PkPatterns.withAddedTiles(
                                PkPatterns.EMPTY,
                                TileDestination.Pattern.PATTERN_3,
                                2,
                                TileKind.Colored.E
                        ),
                        PkFloor.withAddedTiles(
                                PkFloor.EMPTY,
                                PkTileSet.of(1, TileKind.Colored.C)
                        ),
                        PkWall.withTileAt(
                                PkWall.EMPTY,
                                TileDestination.Pattern.PATTERN_4,
                                TileKind.Colored.B
                        ),
                        11
                )
        };

        int testedStates = 0;
        int n = sourceCatalog.length;
        int totalSourceAssignments = (int) Math.pow(n, 6);

        for (int code = 0; code < totalSourceAssignments; code++) {
            int x = code;
            int[] pkSources = new int[6];
            for (int i = 0; i < 6; i++) {
                pkSources[i] = sourceCatalog[x % n];
                x /= n;
            }

            for (int[] p1 : p1Catalog) {
                for (int[] p2 : p2Catalog) {
                    for (PlayerId current : GAME_2P_STATIC.playerIds()) {

                        int accounted = PkTileSet.EMPTY;
//                        for (int pkSource : pkSources) {
//                            accounted = PkTileSet.union(accounted, pkSource);
//                        }

                        accounted = PkTileSet.union(accounted, PkPatterns.asPkTileSet(p1[0]));
                        accounted = PkTileSet.union(accounted, PkFloor.asPkTileSet(p1[1]));
//                        accounted = PkTileSet.union(accounted, PkWall.asPkTileSet(p1[2]));
//                        accounted = PkTileSet.union(accounted, PkPatterns.asPkTileSet(p2[0]));
//                        accounted = PkTileSet.union(accounted, PkFloor.asPkTileSet(p2[1]));
//                        accounted = PkTileSet.union(accounted, PkWall.asPkTileSet(p2[2]));

                        if (!isAccountedTileSetValid(accounted)) continue;

                        int pkTileBag = PkTileSet.difference(
                                PkTileSet.FULL_COLORED,
                                onlyColored(accounted)
                        );

                        int pkUniqueTileSources = oracleUniqueSources(pkSources);

                        ImmutableGameState state = new ImmutableGameState(
                                GAME_2P_STATIC,
                                pkTileBag,
                                ImmutableIntArray.copyOf(pkSources),
                                pkUniqueTileSources,
                                ImmutableIntArray.copyOf(ps2(
                                        p1[0], p1[1], p1[2], p1[3],
                                        p2[0], p2[1], p2[2], p2[3]
                                )),
                                current
                        );

                        // isRoundOver
                        assertEquals(oracleIsRoundOver(state), state.isRoundOver());

                        // isGameOver
                        assertEquals(oracleIsGameOver(state), state.isGameOver());

                        // pkDiscardedTiles
//                        assertEquals(oracleDiscardedTiles(state), state.pkDiscardedTiles());

                        // validMoves
                        short[] allMoves = new short[Move.MAX_MOVES];
                        int allCount = state.validMoves(allMoves);
                        Set<Short> expectedAll = oracleMoves(state, false);
                        Set<Short> actualAll = asSet(allMoves, allCount);

                        assertEquals(expectedAll.size(), allCount);
                        assertEquals(expectedAll, actualAll);

                        // uniqueValidMoves
                        short[] uniqueMoves = new short[Move.MAX_MOVES];
                        int uniqueCount = state.uniqueValidMoves(uniqueMoves);
                        Set<Short> expectedUnique = oracleMoves(state, true);
                        Set<Short> actualUnique = asSet(uniqueMoves, uniqueCount);

                        assertEquals(expectedUnique.size(), uniqueCount);
                        assertEquals(expectedUnique, actualUnique);

                        // subset check
                        assertTrue(actualAll.containsAll(actualUnique));

                        testedStates++;
                    }
                }
            }
        }

        assertTrue(testedStates > 10_000, "Pas assez d'états testés : " + testedStates);
    }

    @Test
    void immutableCreatesATrueSnapshotEvenIfOriginalBackingsMutate() {
        int[] mutableSources = new int[]{
                PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER),
                PkTileSet.of(1, TileKind.Colored.A),
                PkTileSet.EMPTY,
                PkTileSet.EMPTY,
                PkTileSet.EMPTY,
                PkTileSet.EMPTY
        };

        int[] mutablePlayers = ps2(
                PkPatterns.withAddedTiles(
                        PkPatterns.EMPTY,
                        TileDestination.Pattern.PATTERN_1,
                        1,
                        TileKind.Colored.A
                ),
                PkFloor.EMPTY,
                PkWall.EMPTY,
                3,
                PkPatterns.EMPTY,
                PkFloor.EMPTY,
                PkWall.EMPTY,
                0
        );

        final int[] mutableBag = new int[]{PkTileSet.of(19, TileKind.Colored.A)};
        final int[] mutableUnique = new int[]{PkIntSet32.add(PkIntSet32.EMPTY, 1)};
        final PlayerId[] mutableCurrent = new PlayerId[]{PlayerId.P1};

        ReadOnlyGameState ro = new ReadOnlyGameState() {
            @Override
            public Game game() {
                return GAME_2P_STATIC;
            }

            @Override
            public int pkTileBag() {
                return mutableBag[0];
            }

            @Override
            public ReadOnlyIntArray pkTileSources() {
                return MutableIntArray.wrapping(mutableSources);
            }

            @Override
            public ReadOnlyIntArray pkPlayerStates() {
                return MutableIntArray.wrapping(mutablePlayers);
            }

            @Override
            public int pkUniqueTileSources() {
                return mutableUnique[0];
            }

            @Override
            public PlayerId currentPlayerId() {
                return mutableCurrent[0];
            }
        };

        ImmutableGameState snapshot = ro.immutable();

        // On mutile l'état original
        mutableBag[0] = PkTileSet.EMPTY;
        mutableSources[1] = PkTileSet.EMPTY;
        mutableUnique[0] = PkIntSet32.EMPTY;
        mutablePlayers[0] = PkPatterns.EMPTY;
        mutablePlayers[3] = 999;
        mutableCurrent[0] = PlayerId.P2;

        // Le snapshot ne doit pas bouger
        assertEquals(PkTileSet.of(19, TileKind.Colored.A), snapshot.pkTileBag());
        assertEquals(PkTileSet.of(1, TileKind.Colored.A), snapshot.pkTileSources().get(1));
        assertTrue(PkIntSet32.contains(snapshot.pkUniqueTileSources(), 1));
        assertEquals(
                PkPatterns.withAddedTiles(
                        PkPatterns.EMPTY,
                        TileDestination.Pattern.PATTERN_1,
                        1,
                        TileKind.Colored.A
                ),
                PkPlayerStates.pkPatterns(snapshot.pkPlayerStates(), PlayerId.P1)
        );
        assertEquals(3, PkPlayerStates.points(snapshot.pkPlayerStates(), PlayerId.P1));
        assertEquals(PlayerId.P1, snapshot.currentPlayerId());
    }

    @Test
    void isGameOver() {
        Game game = new Game(List.of(
                new Game.PlayerDescription(PlayerId.P1, "Hedi", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "Fahd", Game.PlayerDescription.PlayerKind.AI)
        ));

        int[] sources = emptySources(game);
        int[] playerStates = new int[4 * game.playerIds().size()];

        ImmutableGameState notOver = new ImmutableGameState(
                game,
                PkTileSet.FULL_COLORED,
                ImmutableIntArray.copyOf(sources),
                0,
                ImmutableIntArray.copyOf(playerStates),
                PlayerId.P1
        );
        assertFalse(notOver.isGameOver());

        int fullRowWall = PkWall.EMPTY;
        for (TileKind.Colored color : TileKind.Colored.ALL) {
            fullRowWall = PkWall.withTileAt(fullRowWall, TileDestination.Pattern.PATTERN_1, color);
        }
        PkPlayerStates.setPkWall(playerStates, PlayerId.P2, fullRowWall);

        ImmutableGameState over = new ImmutableGameState(
                game,
                PkTileSet.FULL_COLORED,
                ImmutableIntArray.copyOf(sources),
                0,
                ImmutableIntArray.copyOf(playerStates),
                PlayerId.P1
        );
        assertTrue(over.isGameOver());
    }

    @Test
    void pkDiscardedTiles() {
        Game game = new Game(List.of(
                new Game.PlayerDescription(PlayerId.P1, "Hedi", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "Fahd", Game.PlayerDescription.PlayerKind.AI)
        ));

        int[] sources = emptySources(game);
        sources[1] = PkTileSet.of(2, TileKind.A);

        int[] playerStates = new int[4 * game.playerIds().size()];

        int patterns = PkPatterns.withAddedTiles(
                PkPatterns.EMPTY,
                TileDestination.Pattern.PATTERN_2,
                2,
                (TileKind.Colored) TileKind.A
        );
        PkPlayerStates.setPkPatterns(playerStates, PlayerId.P1, patterns);

        int floor = PkFloor.withAddedTiles(PkFloor.EMPTY, PkTileSet.of(1, TileKind.B));
        PkPlayerStates.setPkFloor(playerStates, PlayerId.P1, floor);

        int wall = PkWall.withTileAt(PkWall.EMPTY, TileDestination.Pattern.PATTERN_1, (TileKind.Colored) TileKind.C);
        PkPlayerStates.setPkWall(playerStates, PlayerId.P2, wall);

        int bag = PkTileSet.of(3, TileKind.D);

        ImmutableGameState state = new ImmutableGameState(
                game,
                bag,
                ImmutableIntArray.copyOf(sources),
                PkIntSet32.add(0, 1),
                ImmutableIntArray.copyOf(playerStates),
                PlayerId.P1
        );

        int expected = PkTileSet.FULL;
        expected = PkTileSet.difference(expected, bag);
        expected = PkTileSet.difference(expected, sources[1]);
        expected = PkTileSet.difference(expected, PkPatterns.asPkTileSet(patterns));
        expected = PkTileSet.difference(expected, PkFloor.asPkTileSet(floor));
        expected = PkTileSet.difference(expected, PkWall.asPkTileSet(wall));

        assertEquals(expected, state.pkDiscardedTiles());
    }

    @Test
    void validMoves() {
        Game game = new Game(List.of(
                new Game.PlayerDescription(PlayerId.P1, "Hedi", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "Fahd", Game.PlayerDescription.PlayerKind.AI)
        ));

        int[] sources = emptySources(game);
        sources[1] = PkTileSet.of(2, TileKind.A);

        ImmutableGameState state = new ImmutableGameState(
                game,
                PkTileSet.FULL_COLORED,
                ImmutableIntArray.copyOf(sources),
                PkIntSet32.add(0, 1),
                PkPlayerStates.initial(game),
                PlayerId.P1
        );

        short[] moves = new short[Move.MAX_MOVES];
        int count = state.validMoves(moves);

        int expectedCount = patternCount() + 1; // 5 lignes de motif + FLOOR
        assertEquals(expectedCount, count);

        var source = game.tileSources().get(1);
        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
            assertTrue(containsMove(moves, count, PkMove.pack(source, TileKind.Colored.A, line)));
        }
        assertTrue(containsMove(moves, count, PkMove.pack(source, TileKind.Colored.A, TileDestination.FLOOR)));
    }

    @Test
    void uniqueValidMoves() {
        Game game = new Game(List.of(
                new Game.PlayerDescription(PlayerId.P1, "Hedi", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "Fahd", Game.PlayerDescription.PlayerKind.AI)
        ));

        int[] sources = emptySources(game);
        sources[1] = PkTileSet.of(2, TileKind.A);
        sources[2] = PkTileSet.of(2, TileKind.A); // doublon volontaire

        int uniqueSources = 0;
        uniqueSources = PkIntSet32.add(uniqueSources, 1); // seule la source 1 est considérée unique

        ImmutableGameState state = new ImmutableGameState(
                game,
                PkTileSet.FULL_COLORED,
                ImmutableIntArray.copyOf(sources),
                uniqueSources,
                PkPlayerStates.initial(game),
                PlayerId.P1
        );

        short[] allMoves = new short[Move.MAX_MOVES];
        short[] uniqueMoves = new short[Move.MAX_MOVES];

        int allCount = state.validMoves(allMoves);
        int uniqueCount = state.uniqueValidMoves(uniqueMoves);

        assertEquals(2 * (patternCount() + 1), allCount);
        assertEquals(patternCount() + 1, uniqueCount);

        var source1 = game.tileSources().get(1);
        var source2 = game.tileSources().get(2);

        assertTrue(containsMove(uniqueMoves, uniqueCount,
                PkMove.pack(source1, TileKind.Colored.A, TileDestination.FLOOR)));
        assertFalse(containsMove(uniqueMoves, uniqueCount,
                PkMove.pack(source2, TileKind.Colored.A, TileDestination.FLOOR)));
    }

    private static int[] emptySources(Game game) {
        return new int[game.tileSources().size()];
    }

    private static int patternCount() {
        int count = 0;
        for (TileDestination.Pattern ignored : TileDestination.Pattern.ALL) {
            count++;
        }
        return count;
    }

    private static boolean containsMove(short[] moves, int size, short expected) {
        for (int i = 0; i < size; i++) {
            if (moves[i] == expected) return true;
        }
        return false;
    }
//
//    // ═══════════════════════════════════════════════════════════
//// Brute force / fuzz massif
//// ═══════════════════════════════════════════════════════════
//
//    private static final int FUZZ_ITERATIONS = 50_000;
//
//    private static final int[] SOURCE_CATALOG = new int[]{
//            PkTileSet.EMPTY,
//            PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER),
//            PkTileSet.of(1, TileKind.Colored.A),
//            PkTileSet.union(
//                    PkTileSet.of(2, TileKind.Colored.A),
//                    PkTileSet.of(2, TileKind.Colored.B)
//            ),
//            PkTileSet.of(4, TileKind.Colored.C)
//    };
//
//    private static final int[] PATTERN_CATALOG = new int[]{
//            PkPatterns.EMPTY,
//            PkPatterns.withAddedTiles(
//                    PkPatterns.EMPTY,
//                    TileDestination.Pattern.PATTERN_1,
//                    1,
//                    TileKind.Colored.A
//            ),
//            PkPatterns.withAddedTiles(
//                    PkPatterns.EMPTY,
//                    TileDestination.Pattern.PATTERN_2,
//                    1,
//                    TileKind.Colored.B
//            ),
//            PkPatterns.withAddedTiles(
//                    PkPatterns.EMPTY,
//                    TileDestination.Pattern.PATTERN_3,
//                    3,
//                    TileKind.Colored.C
//            ),
//            PkPatterns.withAddedTiles(
//                    PkPatterns.withAddedTiles(
//                            PkPatterns.EMPTY,
//                            TileDestination.Pattern.PATTERN_2,
//                            2,
//                            TileKind.Colored.D
//                    ),
//                    TileDestination.Pattern.PATTERN_5,
//                    1,
//                    TileKind.Colored.A
//            ),
//            PkPatterns.withAddedTiles(
//                    PkPatterns.withAddedTiles(
//                            PkPatterns.EMPTY,
//                            TileDestination.Pattern.PATTERN_1,
//                            1,
//                            TileKind.Colored.E
//                    ),
//                    TileDestination.Pattern.PATTERN_4,
//                    2,
//                    TileKind.Colored.B
//            )
//    };
//
//    private static final int WALL_FULL_ROW_1_BRUTE = fullRow(TileDestination.Pattern.PATTERN_1);
//
//    private static final int[] WALL_CATALOG = new int[]{
//            PkWall.EMPTY,
//            PkWall.withTileAt(
//                    PkWall.EMPTY,
//                    TileDestination.Pattern.PATTERN_1,
//                    TileKind.Colored.A
//            ),
//            PkWall.withTileAt(
//                    PkWall.EMPTY,
//                    TileDestination.Pattern.PATTERN_2,
//                    TileKind.Colored.B
//            ),
//            PkWall.withTileAt(
//                    PkWall.withTileAt(
//                            PkWall.EMPTY,
//                            TileDestination.Pattern.PATTERN_3,
//                            TileKind.Colored.C
//                    ),
//                    TileDestination.Pattern.PATTERN_4,
//                    TileKind.Colored.A
//            ),
//            WALL_FULL_ROW_1_BRUTE
//    };
//
//    private static int fullRow(TileDestination.Pattern line) {
//        int wall = PkWall.EMPTY;
//        for (TileKind.Colored color : TileKind.Colored.ALL) {
//            wall = PkWall.withTileAt(wall, line, color);
//        }
//        return wall;
//    }
//
//    private static int coloredPart(int pkTileSet) {
//        return PkTileSet.difference(
//                pkTileSet,
//                PkTileSet.subsetOf(pkTileSet, TileKind.FIRST_PLAYER_MARKER)
//        );
//    }
//
//    private static boolean hasColored(int pkTileSet) {
//        return !PkTileSet.isEmpty(coloredPart(pkTileSet));
//    }
//
//    private static int oracleUniqueSources(int[] pkSources) {
//        int unique = PkIntSet32.EMPTY;
//
//        if (hasColored(pkSources[0])) {
//            unique = PkIntSet32.add(unique, 0);
//        }
//
//        for (int i = 1; i < pkSources.length; i++) {
//            if (!hasColored(pkSources[i])) continue;
//
//            boolean duplicateEarlierFactory = false;
//            for (int j = 1; j < i; j++) {
//                if (pkSources[j] == pkSources[i]) {
//                    duplicateEarlierFactory = true;
//                    break;
//                }
//            }
//
//            if (!duplicateEarlierFactory) {
//                unique = PkIntSet32.add(unique, i);
//            }
//        }
//
//        return unique;
//    }
//
//    private static boolean oracleRoundOver(ReadOnlyGameState state) {
//        for (int i = 0; i < state.pkTileSources().size(); i++) {
//            if (hasColored(state.pkTileSources().get(i))) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    private static boolean oracleGameOver(ReadOnlyGameState state) {
//        if (!oracleRoundOver(state)) return false;
//
//        for (PlayerId playerId : state.playerIds()) {
//            if (PkWall.hasFullRow(PkPlayerStates.pkWall(state.pkPlayerStates(), playerId))) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private static int oracleDiscardedTiles(ReadOnlyGameState state) {
//        int visible = state.pkTileBag();
//
//        for (int i = 0; i < state.pkTileSources().size(); i++) {
//            visible = PkTileSet.union(visible, state.pkTileSources().get(i));
//        }
//
//        for (PlayerId playerId : state.playerIds()) {
//            visible = PkTileSet.union(
//                    visible,
//                    PkPatterns.asPkTileSet(PkPlayerStates.pkPatterns(state.pkPlayerStates(), playerId))
//            );
//            visible = PkTileSet.union(
//                    visible,
//                    PkFloor.asPkTileSet(PkPlayerStates.pkFloor(state.pkPlayerStates(), playerId))
//            );
//            visible = PkTileSet.union(
//                    visible,
//                    PkWall.asPkTileSet(PkPlayerStates.pkWall(state.pkPlayerStates(), playerId))
//            );
//        }
//
//        return PkTileSet.difference(PkTileSet.FULL, visible);
//    }
//
//    private static Set<Short> oracleMoves(ReadOnlyGameState state, boolean uniqueOnly) {
//        Set<Short> expected = new HashSet<>();
//
//        PlayerId current = state.currentPlayerId();
//        int pkPatterns = PkPlayerStates.pkPatterns(state.pkPlayerStates(), current);
//        int pkWall = PkPlayerStates.pkWall(state.pkPlayerStates(), current);
//
//        for (int sourceIndex = 0; sourceIndex < state.pkTileSources().size(); sourceIndex++) {
//            if (uniqueOnly && !PkIntSet32.contains(state.pkUniqueTileSources(), sourceIndex)) {
//                continue;
//            }
//
//            int pkSource = state.pkTileSources().get(sourceIndex);
//
//            for (TileKind.Colored color : TileKind.Colored.ALL) {
//                if (PkTileSet.countOf(pkSource, color) == 0) continue;
//
//                for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
//                    if (!PkPatterns.isFull(pkPatterns, line)
//                            && PkPatterns.canContain(pkPatterns, line, color)
//                            && !PkWall.hasTileAt(pkWall, line, color)) {
//                        expected.add(PkMove.pack(TileSource.ALL.get(sourceIndex), color, line));
//                    }
//                }
//
//                expected.add(PkMove.pack(TileSource.ALL.get(sourceIndex), color, TileDestination.FLOOR));
//            }
//        }
//
//        return expected;
//    }
//
//    private static Set<Short> returnedMoves(ReadOnlyGameState state, boolean uniqueOnly) {
//        short[] buffer = new short[Move.MAX_MOVES];
//        int count = uniqueOnly ? state.uniqueValidMoves(buffer) : state.validMoves(buffer);
//
//        Set<Short> returned = new HashSet<>();
//        for (int i = 0; i < count; i++) {
//            assertTrue(returned.add(buffer[i]), "move duplique retourne: " + buffer[i]);
//        }
//        return returned;
//    }
//
//    private static void assertStateMatchesOracle(ReadOnlyGameState state, boolean checkDiscarded) {
//        assertEquals(oracleRoundOver(state), state.isRoundOver());
//        assertEquals(oracleGameOver(state), state.isGameOver());
//
//        Set<Short> allExpected = oracleMoves(state, false);
//        Set<Short> allReturned = returnedMoves(state, false);
//        assertEquals(allExpected, allReturned);
//
//        Set<Short> uniqueExpected = oracleMoves(state, true);
//        Set<Short> uniqueReturned = returnedMoves(state, true);
//        assertEquals(uniqueExpected, uniqueReturned);
//
//        assertTrue(allReturned.containsAll(uniqueReturned));
//        assertEquals(state.isRoundOver(), allReturned.isEmpty());
//        assertEquals(state.isRoundOver(), uniqueReturned.isEmpty());
//
//        if (checkDiscarded) {
//            assertEquals(oracleDiscardedTiles(state), state.pkDiscardedTiles());
//        }
//    }
//
//    private static TileKind.Colored randomAvailableColor(Random rng, int[] remaining) {
//        TileKind.Colored[] candidates = new TileKind.Colored[TileKind.Colored.COUNT];
//        int count = 0;
//
//        for (TileKind.Colored color : TileKind.Colored.ALL) {
//            if (remaining[color.index()] > 0) {
//                candidates[count++] = color;
//            }
//        }
//
//        if (count == 0) return null;
//        return candidates[rng.nextInt(count)];
//    }
//
//    private static TileKind.Colored randomPatternColor(Random rng, int[] remaining, int pkWall, TileDestination.Pattern line) {
//        TileKind.Colored[] candidates = new TileKind.Colored[TileKind.Colored.COUNT];
//        int count = 0;
//
//        for (TileKind.Colored color : TileKind.Colored.ALL) {
//            if (remaining[color.index()] > 0 && !PkWall.hasTileAt(pkWall, line, color)) {
//                candidates[count++] = color;
//            }
//        }
//
//        if (count == 0) return null;
//        return candidates[rng.nextInt(count)];
//    }
//
//    private static int randomColoredTileSet(Random rng, int[] remaining, int maxTiles) {
//        int pkSet = PkTileSet.EMPTY;
//        int draws = rng.nextInt(maxTiles + 1);
//
//        for (int i = 0; i < draws; i++) {
//            TileKind.Colored color = randomAvailableColor(rng, remaining);
//            if (color == null) break;
//
//            pkSet = PkTileSet.add(pkSet, color);
//            remaining[color.index()]--;
//        }
//
//        return pkSet;
//    }
//
//    private static int randomWall(Random rng, int[] remaining) {
//        int pkWall = PkWall.EMPTY;
//
//        if (rng.nextInt(8) == 0) {
//            TileDestination.Pattern line = TileDestination.Pattern.ALL.get(
//                    rng.nextInt(TileDestination.Pattern.COUNT)
//            );
//
//            boolean canBuildFullRow = true;
//            for (TileKind.Colored color : TileKind.Colored.ALL) {
//                if (remaining[color.index()] == 0) {
//                    canBuildFullRow = false;
//                    break;
//                }
//            }
//
//            if (canBuildFullRow) {
//                for (TileKind.Colored color : TileKind.Colored.ALL) {
//                    pkWall = PkWall.withTileAt(pkWall, line, color);
//                    remaining[color.index()]--;
//                }
//            }
//        }
//
//        int extraAttempts = 20;
//        int extraTiles = rng.nextInt(6);
//
//        while (extraTiles > 0 && extraAttempts-- > 0) {
//            TileDestination.Pattern line = TileDestination.Pattern.ALL.get(
//                    rng.nextInt(TileDestination.Pattern.COUNT)
//            );
//            TileKind.Colored color = randomAvailableColor(rng, remaining);
//            if (color == null) break;
//            if (PkWall.hasTileAt(pkWall, line, color)) continue;
//
//            pkWall = PkWall.withTileAt(pkWall, line, color);
//            remaining[color.index()]--;
//            extraTiles--;
//        }
//
//        return pkWall;
//    }
//
//    private static int randomPatterns(Random rng, int[] remaining, int pkWall) {
//        int pkPatterns = PkPatterns.EMPTY;
//
//        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
//            if (rng.nextBoolean()) continue;
//
//            TileKind.Colored color = randomPatternColor(rng, remaining, pkWall, line);
//            if (color == null) continue;
//
//            int maxCount = Math.min(line.capacity(), remaining[color.index()]);
//            if (maxCount == 0) continue;
//
//            int count = 1 + rng.nextInt(maxCount);
//            pkPatterns = PkPatterns.withAddedTiles(pkPatterns, line, count, color);
//            remaining[color.index()] -= count;
//        }
//
//        return pkPatterns;
//    }
//
//    private static int randomFloor(Random rng, int[] remaining) {
//        int pkSet = PkTileSet.EMPTY;
//        int draws = rng.nextInt(6);
//
//        for (int i = 0; i < draws; i++) {
//            TileKind.Colored color = randomAvailableColor(rng, remaining);
//            if (color == null) break;
//
//            pkSet = PkTileSet.add(pkSet, color);
//            remaining[color.index()]--;
//        }
//
//        return PkFloor.withAddedTiles(PkFloor.EMPTY, pkSet);
//    }
//
//    private static ImmutableGameState randomConsistentState2P(Random rng) {
//        int[] remaining = new int[]{20, 20, 20, 20, 20};
//
//        int[] sources = new int[6];
//        sources[0] = randomColoredTileSet(rng, remaining, 4);
//        for (int i = 1; i < 6; i++) {
//            sources[i] = randomColoredTileSet(rng, remaining, 4);
//        }
//
//        int p1Wall = randomWall(rng, remaining);
//        int p2Wall = randomWall(rng, remaining);
//
//        int p1Patterns = randomPatterns(rng, remaining, p1Wall);
//        int p2Patterns = randomPatterns(rng, remaining, p2Wall);
//
//        int p1Floor = randomFloor(rng, remaining);
//        int p2Floor = randomFloor(rng, remaining);
//
//        int markerPlace = rng.nextInt(3);
//        if (markerPlace == 0) {
//            sources[0] = PkTileSet.add(sources[0], TileKind.FIRST_PLAYER_MARKER);
//        } else if (markerPlace == 1) {
//            p1Floor = PkFloor.withAddedTiles(p1Floor, PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER));
//        } else {
//            p2Floor = PkFloor.withAddedTiles(p2Floor, PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER));
//        }
//
//        int visibleColored = PkTileSet.EMPTY;
//        for (int source : sources) {
//            visibleColored = PkTileSet.union(visibleColored, coloredPart(source));
//        }
//        visibleColored = PkTileSet.union(visibleColored, PkPatterns.asPkTileSet(p1Patterns));
//        visibleColored = PkTileSet.union(visibleColored, PkPatterns.asPkTileSet(p2Patterns));
//        visibleColored = PkTileSet.union(visibleColored, coloredPart(PkFloor.asPkTileSet(p1Floor)));
//        visibleColored = PkTileSet.union(visibleColored, coloredPart(PkFloor.asPkTileSet(p2Floor)));
//        visibleColored = PkTileSet.union(visibleColored, PkWall.asPkTileSet(p1Wall));
//        visibleColored = PkTileSet.union(visibleColored, PkWall.asPkTileSet(p2Wall));
//
//        int bag = PkTileSet.difference(PkTileSet.FULL_COLORED, visibleColored);
//        int unique = oracleUniqueSources(sources);
//
//        return new ImmutableGameState(
//                GAME_2P_STATIC,
//                bag,
//                ImmutableIntArray.copyOf(sources),
//                unique,
//                ImmutableIntArray.copyOf(ps2(
//                        p1Patterns, p1Floor, p1Wall, rng.nextInt(121),
//                        p2Patterns, p2Floor, p2Wall, rng.nextInt(121)
//                )),
//                rng.nextBoolean() ? PlayerId.P1 : PlayerId.P2
//        );
//    }
//
//    @Test
//    void bruteForceAllUniqueSourceBitMasks() {
//        ImmutableIntArray fixedSources = ImmutableIntArray.copyOf(new int[]{
//                PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER),
//                PkTileSet.of(4, TileKind.Colored.A),
//                PkTileSet.union(
//                        PkTileSet.of(2, TileKind.Colored.B),
//                        PkTileSet.of(2, TileKind.Colored.C)
//                ),
//                PkTileSet.of(4, TileKind.Colored.D),
//                PkTileSet.EMPTY,
//                PkTileSet.of(4, TileKind.Colored.E)
//        });
//
//        for (int mask = 0; mask < (1 << 6); mask++) {
//            ImmutableGameState state = new ImmutableGameState(
//                    GAME_2P_STATIC,
//                    PkTileSet.FULL_COLORED,
//                    fixedSources,
//                    mask,
//                    ImmutableIntArray.copyOf(ps2(
//                            PkPatterns.EMPTY, PkFloor.EMPTY, PkWall.EMPTY, 0,
//                            PkPatterns.EMPTY, PkFloor.EMPTY, PkWall.EMPTY, 0
//                    )),
//                    PlayerId.P1
//            );
//
//            assertEquals(oracleMoves(state, true), returnedMoves(state, true));
//        }
//    }
//
//    @Test
//    void boundedExhaustiveAgainstOracle() {
//        for (PlayerId current : List.of(PlayerId.P1, PlayerId.P2)) {
//            for (int s0 : SOURCE_CATALOG) {
//                for (int s1 : SOURCE_CATALOG) {
//                    for (int s2 : SOURCE_CATALOG) {
//                        for (int s3 : SOURCE_CATALOG) {
//                            for (int s4 : SOURCE_CATALOG) {
//                                for (int s5 : SOURCE_CATALOG) {
//                                    int[] sources = new int[]{s0, s1, s2, s3, s4, s5};
//                                    int unique = oracleUniqueSources(sources);
//
//                                    for (int pkPatterns : PATTERN_CATALOG) {
//                                        for (int pkWall : WALL_CATALOG) {
//                                            ImmutableGameState state = new ImmutableGameState(
//                                                    GAME_2P_STATIC,
//                                                    PkTileSet.EMPTY,
//                                                    ImmutableIntArray.copyOf(sources),
//                                                    unique,
//                                                    ImmutableIntArray.copyOf(ps2(
//                                                            current == PlayerId.P1 ? pkPatterns : PkPatterns.EMPTY,
//                                                            PkFloor.EMPTY,
//                                                            current == PlayerId.P1 ? pkWall : PkWall.EMPTY,
//                                                            0,
//                                                            current == PlayerId.P2 ? pkPatterns : PkPatterns.EMPTY,
//                                                            PkFloor.EMPTY,
//                                                            current == PlayerId.P2 ? pkWall : PkWall.EMPTY,
//                                                            0
//                                                    )),
//                                                    current
//                                            );
//
//                                            assertStateMatchesOracle(state, false);
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    @Test
//    void massiveRandomConsistentStatesAgainstOracle() {
//        Random rng = new Random(20260320L);
//
//        for (int i = 0; i < FUZZ_ITERATIONS; i++) {
//            ImmutableGameState state = randomConsistentState2P(rng);
//
//            assertEquals(
//                    oracleUniqueSources(state.pkTileSources().toArray()),
//                    state.pkUniqueTileSources()
//            );
//
//            assertStateMatchesOracle(state, true);
//        }
//    }

@Test
void isRoundOverIgnoresFirstPlayerMarker() {
    ImmutableGameState state = new ImmutableGameState(
            GAME_2P_STATIC,
            PkTileSet.FULL_COLORED,
            ImmutableIntArray.copyOf(new int[]{
                    PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER),
                    PkTileSet.EMPTY,
                    PkTileSet.EMPTY,
                    PkTileSet.EMPTY,
                    PkTileSet.EMPTY,
                    PkTileSet.EMPTY
            }),
            PkIntSet32.EMPTY,
            PkPlayerStates.initial(GAME_2P_STATIC),
            PlayerId.P1
    );

    assertTrue(state.isRoundOver());
    }

    @Test
    void pkDiscardedTilesContainsMarkerWhenItIsUnaccountedFor() {
        ImmutableGameState state = new ImmutableGameState(
                GAME_2P_STATIC,
                PkTileSet.FULL_COLORED,
                ImmutableIntArray.copyOf(new int[]{0, 0, 0, 0, 0, 0}),
                PkIntSet32.EMPTY,
                PkPlayerStates.initial(GAME_2P_STATIC),
                PlayerId.P1
        );

        assertEquals(
                PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER),
                state.pkDiscardedTiles()
        );
    }

    @Test
    void validMovesNeverReturnsDuplicateMoves() {
        short[] moves = new short[Move.MAX_MOVES];
        int count = MID_ROUND_2P.validMoves(moves);

        assertEquals(count, asSet(moves, count).size());
    }

    @Test
    void uniqueValidMovesUsesExactlyTheSourcesIndicatedByMask() {
        ImmutableGameState state = new ImmutableGameState(
                GAME_2P_STATIC,
                PkTileSet.FULL_COLORED,
                ImmutableIntArray.copyOf(new int[]{
                        PkTileSet.EMPTY,
                        PkTileSet.of(2, TileKind.Colored.A),
                        PkTileSet.of(2, TileKind.Colored.B),
                        PkTileSet.EMPTY,
                        PkTileSet.EMPTY,
                        PkTileSet.EMPTY
                }),
                1 << 2, // seule FACTORY_2 est autorisee
                PkPlayerStates.initial(GAME_2P_STATIC),
                PlayerId.P1
        );

        short[] moves = new short[Move.MAX_MOVES];
        int count = state.uniqueValidMoves(moves);

        assertEquals(6, count); // 5 patterns + floor pour B uniquement
        assertTrue(containsMove(moves, count,
                PkMove.pack(TileSource.FACTORY_2, TileKind.Colored.B, TileDestination.FLOOR)));
        assertFalse(containsMove(moves, count,
                PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.A, TileDestination.FLOOR)));
    }

    @Test
    void uniqueValidMovesNeverReturnsDuplicateMoves() {
        short[] moves = new short[Move.MAX_MOVES];
        int count = MID_ROUND_2P.uniqueValidMoves(moves);

        assertEquals(count, asSet(moves, count).size());
    }


}