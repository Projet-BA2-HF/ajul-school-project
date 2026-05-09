package ch.epfl.ajul;

import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.MutableGameState;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkWall;
import ch.epfl.ajul.intarray.ImmutableIntArray;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.random.RandomGeneratorFactory;

import static org.junit.jupiter.api.Assertions.*;

class RankComputerTest {

    private static Game twoPlayerGame() {
        return new Game(List.of(
                new Game.PlayerDescription(PlayerId.P1, "P1",
                        Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "P2",
                        Game.PlayerDescription.PlayerKind.HUMAN)));
    }

    private static Game threePlayerGame() {
        return new Game(List.of(
                new Game.PlayerDescription(PlayerId.P1, "P1",
                        Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "P2",
                        Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P3, "P3",
                        Game.PlayerDescription.PlayerKind.HUMAN)));
    }

    private static Game fourPlayerGame() {
        return new Game(List.of(
                new Game.PlayerDescription(PlayerId.P1, "P1",
                        Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "P2",
                        Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P3, "P3",
                        Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P4, "P4",
                        Game.PlayerDescription.PlayerKind.HUMAN)));
    }

    // Helper to create a game state with specific points and walls
    private static MutableGameState stateWithPointsAndWalls(
            Game game, int[] points, int[] walls) {
        var initialState = ImmutableGameState.initial(game);
        var state = new MutableGameState(initialState);

        // Set points and walls via registerMove/endRound simulation is complex,
        // so we directly modify the underlying state through repeated evaluations
        // Instead, we'll build the packed player states array
        int playerCount = game.playersCount();
        int[] pkStates = new int[playerCount * 4];
        for (int i = 0; i < playerCount; i += 1) {
            pkStates[i * 4] = 0;              // empty patterns
            pkStates[i * 4 + 1] = 0;          // empty floor
            pkStates[i * 4 + 2] = walls[i];   // wall
            pkStates[i * 4 + 3] = points[i];  // points
        }

        // Create immutable state with custom player states
        var customState = new ImmutableGameState(
                game,
                0, // empty bag
                initialState.pkTileSources(),
                initialState.pkUniqueTileSources(),
                ImmutableIntArray.copyOf(pkStates),
                PlayerId.P1
        );

        return new MutableGameState(customState);
    }

    @Test
    void playersRankWithDifferentPoints() {
        var game = twoPlayerGame();
        // P1: 50 pts, P2: 30 pts
        var state = stateWithPointsAndWalls(game,
                new int[]{50, 30}, new int[]{0, 0});

        var ranks = new int[2];
        RankComputer.playersRank(state, ranks);

        assertEquals(0, ranks[0]); // P1 wins
        assertEquals(1, ranks[1]); // P2 second
    }

    @Test
    void playersRankWithEqualPointsDifferentRows() {
        var game = twoPlayerGame();
        // P1: 50 pts, wall with 1 full row
        // P2: 50 pts, wall with 2 full rows
        int wallP1 = 0b11111; // row 0 full
        int wallP2 = 0b1111111111; // rows 0 and 1 full

        var state = stateWithPointsAndWalls(game,
                new int[]{50, 50}, new int[]{wallP1, wallP2});

        var ranks = new int[2];
        RankComputer.playersRank(state, ranks);

        assertEquals(1, ranks[0]); // P1 second (fewer rows)
        assertEquals(0, ranks[1]); // P2 first (more rows)
    }

    @Test
    void playersRankWithExAequo() {
        var game = twoPlayerGame();
        // Both players: 50 pts, 0 full rows
        var state = stateWithPointsAndWalls(game,
                new int[]{50, 50}, new int[]{0, 0});

        var ranks = new int[2];
        RankComputer.playersRank(state, ranks);

        assertEquals(0, ranks[0]); // Ex aequo rank 0
        assertEquals(0, ranks[1]); // Ex aequo rank 0
    }

    @Test
    void playersRankFourPlayersExampleFromSpec() {
        // From spec: P1=45pts/3rows, P2=55pts/2rows, P3=55pts/1row, P4=55pts/2rows
        var game = fourPlayerGame();

        // Build walls with specific full row counts
        // row 0: bits 0-4, row 1: bits 5-9, row 2: bits 10-14
        // row 3: bits 15-19, row 4: bits 20-24
        int wallP1_3rows = 0b111111111111111; // 3 full rows (bits 0-14)
        int wallP2_2rows = 0b1111111111;      // 2 full rows (bits 0-9)
        int wallP3_1row = 0b11111;             // 1 full row (bits 0-4)
        int wallP4_2rows = 0b1111111111;      // 2 full rows (bits 0-9)

        var state = stateWithPointsAndWalls(game,
                new int[]{45, 55, 55, 55},
                new int[]{wallP1_3rows, wallP2_2rows, wallP3_1row, wallP4_2rows});

        var ranks = new int[4];
        RankComputer.playersRank(state, ranks);

        assertEquals(3, ranks[0]); // P1: rank 3
        assertEquals(0, ranks[1]); // P2: rank 0 (tied with P4)
        assertEquals(2, ranks[2]); // P3: rank 2
        assertEquals(0, ranks[3]); // P4: rank 0 (tied with P2)
    }

    @Test
    void playersRankAllExAequo() {
        var game = threePlayerGame();
        var state = stateWithPointsAndWalls(game,
                new int[]{30, 30, 30}, new int[]{0, 0, 0});

        var ranks = new int[3];
        RankComputer.playersRank(state, ranks);

        assertEquals(0, ranks[0]);
        assertEquals(0, ranks[1]);
        assertEquals(0, ranks[2]);
    }

    @Test
    void playersRankThreePlayersDistinct() {
        var game = threePlayerGame();
        // P1: 10 pts, P2: 30 pts, P3: 20 pts
        var state = stateWithPointsAndWalls(game,
                new int[]{10, 30, 20}, new int[]{0, 0, 0});

        var ranks = new int[3];
        RankComputer.playersRank(state, ranks);

        assertEquals(2, ranks[0]); // P1: last
        assertEquals(0, ranks[1]); // P2: first
        assertEquals(1, ranks[2]); // P3: second
    }

    @Test
    void playersRankTwoExAequoOneBelow() {
        var game = threePlayerGame();
        // P1: 40 pts, P2: 40 pts, P3: 20 pts
        var state = stateWithPointsAndWalls(game,
                new int[]{40, 40, 20}, new int[]{0, 0, 0});

        var ranks = new int[3];
        RankComputer.playersRank(state, ranks);

        assertEquals(0, ranks[0]); // P1: tied for first
        assertEquals(0, ranks[1]); // P2: tied for first
        assertEquals(2, ranks[2]); // P3: rank 2 (not 1!)
    }

    @Test
    void playersRankZeroPoints() {
        var game = twoPlayerGame();
        var state = stateWithPointsAndWalls(game,
                new int[]{0, 0}, new int[]{0, 0});

        var ranks = new int[2];
        RankComputer.playersRank(state, ranks);

        assertEquals(0, ranks[0]);
        assertEquals(0, ranks[1]);
    }

    @Test
    void playersRankRowsBreakTies() {
        var game = threePlayerGame();
        // All 50 pts, but P1 has 0 rows, P2 has 1 row, P3 has 2 rows
        int wallP2 = 0b11111;          // 1 full row
        int wallP3 = 0b1111111111;     // 2 full rows

        var state = stateWithPointsAndWalls(game,
                new int[]{50, 50, 50},
                new int[]{0, wallP2, wallP3});

        var ranks = new int[3];
        RankComputer.playersRank(state, ranks);

        assertEquals(2, ranks[0]); // P1: worst (0 rows)
        assertEquals(1, ranks[1]); // P2: middle (1 row)
        assertEquals(0, ranks[2]); // P3: best (2 rows)
    }

    @Test
    void playersRankAfterFullGameSimulation() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var game = twoPlayerGame();
        var state = new MutableGameState(ImmutableGameState.initial(game));
        state.fillFactories(rng);

        // Play a full game
        while (!state.isGameOver()) {
            var moves = new short[216];
            var count = state.uniqueValidMoves(moves);
            var idx = rng.nextInt(count);
            state.registerMove(moves[idx]);
            if (state.isRoundOver()) {
                state.endRound();
                if (!state.isGameOver()) state.fillFactories(rng);
            }
        }
        state.endGame();

        var ranks = new int[2];
        RankComputer.playersRank(state, ranks);

        // Both ranks should be valid (0 or 1)
        assertTrue(ranks[0] >= 0 && ranks[0] < 2);
        assertTrue(ranks[1] >= 0 && ranks[1] < 2);

        // If points differ, ranks should differ
        int p1pts = PkPlayerStates.points(state.pkPlayerStates(), PlayerId.P1);
        int p2pts = PkPlayerStates.points(state.pkPlayerStates(), PlayerId.P2);
        if (p1pts != p2pts) {
            assertNotEquals(ranks[0], ranks[1]);
            if (p1pts > p2pts) {
                assertEquals(0, ranks[0]);
                assertEquals(1, ranks[1]);
            } else {
                assertEquals(1, ranks[0]);
                assertEquals(0, ranks[1]);
            }
        }
    }
}
