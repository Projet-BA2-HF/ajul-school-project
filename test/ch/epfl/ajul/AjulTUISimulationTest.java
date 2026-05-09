package ch.epfl.ajul;

import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.MutableGameState;
import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.*;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import ch.epfl.ajul.intarray.MutableIntArray;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;
import ch.epfl.ajul.mcts.HeuristicMoveSelector;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.random.RandomGeneratorFactory;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class AjulTUISimulationTest {

    // ========================== HELPERS ==========================

    private static Game makeGame(int playerCount) {
        return makeGame(playerCount, Game.PlayerDescription.PlayerKind.AI);
    }

    private static Game makeGame(int playerCount, Game.PlayerDescription.PlayerKind kind) {
        var descriptions = new ArrayList<Game.PlayerDescription>();
        for (int i = 0; i < playerCount; i += 1) {
            descriptions.add(new Game.PlayerDescription(
                    PlayerId.ALL.get(i),
                    "Player" + (i + 1),
                    kind));
        }
        return new Game(descriptions);
    }

    private static MutableGameState simulateGame(int playerCount, long seed) {
        var rng = RandomGeneratorFactory.getDefault().create(seed);
        var game = makeGame(playerCount);
        var state = new MutableGameState(ImmutableGameState.initial(game));
        state.fillFactories(rng);

        int moveCount = 0;
        int roundCount = 1;

        while (!state.isGameOver()) {
            var moves = new short[Move.MAX_MOVES];
            var count = state.uniqueValidMoves(moves);
            assertTrue(count > 0, "Should always have at least one valid move");

            var idx = HeuristicMoveSelector.selectMove(rng, state, moves, count);
            assertTrue(idx >= 0 && idx < count,
                    "Selected index " + idx + " out of range [0," + count + ")");

            state.registerMove(moves[idx]);
            moveCount += 1;

            if (state.isRoundOver()) {
                state.endRound();
                if (!state.isGameOver()) {
                    state.fillFactories(rng);
                    roundCount += 1;
                }
            }

            assertTrue(moveCount < 2000,
                    "Game should finish within 2000 moves (stuck at round " + roundCount + ")");
        }

        state.endGame();
        return state;
    }

    /** Serializes a game state to the hex format used by the professor's tests. */
    private static String serializeGameState(ReadOnlyGameState gs) {
        var sb = new StringBuilder();
        sb.append(Integer.toHexString(gs.game().playersCount()));
        sb.append('|');
        sb.append(Integer.toHexString(gs.pkTileBag()));
        sb.append('|');
        var sources = gs.pkTileSources();
        for (int i = 0; i < sources.size(); i += 1) {
            if (i > 0) sb.append(',');
            sb.append(Integer.toHexString(sources.get(i)));
        }
        sb.append('|');
        sb.append(Integer.toHexString(gs.pkUniqueTileSources()));
        sb.append('|');
        var ps = gs.pkPlayerStates();
        for (int i = 0; i < ps.size(); i += 1) {
            if (i > 0) sb.append(',');
            sb.append(Integer.toHexString(ps.get(i)));
        }
        sb.append('|');
        sb.append(gs.currentPlayerId().name());
        return sb.toString();
    }

    /** Parses a game state from the hex format (same as professor's parseGameState). */
    static ImmutableGameState parseGameState(String string) {
        var allPlayers = List.of(
                new Game.PlayerDescription(PlayerId.P1, "P1", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "P2", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P3, "P3", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P4, "P4", Game.PlayerDescription.PlayerKind.HUMAN));

        var parts = string.split(Pattern.quote("|"));
        int i = 0;
        var game = new Game(allPlayers.subList(0, Integer.parseInt(parts[i++], 16)));
        var pkTileBag = Integer.parseInt(parts[i++], 16);
        var pkTileSources = parseIntArray(parts[i++]).immutable();
        var pkUniqueTileSources = Integer.parseInt(parts[i++], 16);
        var pkPlayerStates = parseIntArray(parts[i++]).immutable();
        var currentPlayerId = PlayerId.valueOf(parts[i++]);
        return new ImmutableGameState(
                game, pkTileBag, pkTileSources, pkUniqueTileSources,
                pkPlayerStates, currentPlayerId);
    }

    static ReadOnlyIntArray parseIntArray(String string) {
        return MutableIntArray.wrapping(
                Arrays.stream(string.split(Pattern.quote(",")))
                        .mapToInt(s -> Integer.parseInt(s, 16))
                        .toArray());
    }

    // ========================== COMPUTED STATE TESTS ==========================

    @Test
    void simulatedGameStateIsConsistentAfterFillFactories() {
        // After filling factories, the total tiles in sources + bag + discarded + players = 101
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var game = makeGame(2);
        var state = new MutableGameState(ImmutableGameState.initial(game));
        state.fillFactories(rng);

        int totalTiles = PkTileSet.size(state.pkTileBag());
        for (int i = 0; i < state.pkTileSources().size(); i += 1) {
            totalTiles += PkTileSet.size(state.pkTileSources().get(i));
        }
        for (var pid : state.playerIds()) {
            totalTiles += PkTileSet.size(PkPatterns.asPkTileSet(
                    PkPlayerStates.pkPatterns(state.pkPlayerStates(), pid)));
            totalTiles += PkTileSet.size(PkFloor.asPkTileSet(
                    PkPlayerStates.pkFloor(state.pkPlayerStates(), pid)));
            totalTiles += PkTileSet.size(PkWall.asPkTileSet(
                    PkPlayerStates.pkWall(state.pkPlayerStates(), pid)));
        }
        totalTiles += PkTileSet.size(state.pkDiscardedTiles());

        assertEquals(101, totalTiles, "Total tiles (bag+sources+players+discarded) must be 101");
    }

    @Test
    void gameStatePreservesTotalTilesThroughoutGame() {
        var rng = RandomGeneratorFactory.getDefault().create(42);
        var game = makeGame(2);
        var state = new MutableGameState(ImmutableGameState.initial(game));
        state.fillFactories(rng);

        int moveCount = 0;
        while (!state.isGameOver()) {
            // Check tile conservation at each step
            int total = countAllTiles(state);
            assertEquals(101, total,
                    "Tile conservation violated at move " + moveCount);

            var moves = new short[Move.MAX_MOVES];
            var count = state.uniqueValidMoves(moves);
            var idx = HeuristicMoveSelector.selectMove(rng, state, moves, count);
            state.registerMove(moves[idx]);
            moveCount += 1;

            if (state.isRoundOver()) {
                state.endRound();
                if (!state.isGameOver()) state.fillFactories(rng);
            }
        }
        state.endGame();
    }

    private static int countAllTiles(ReadOnlyGameState state) {
        int total = PkTileSet.size(state.pkTileBag());
        for (int i = 0; i < state.pkTileSources().size(); i += 1)
            total += PkTileSet.size(state.pkTileSources().get(i));
        for (var pid : state.playerIds()) {
            total += PkTileSet.size(PkPatterns.asPkTileSet(
                    PkPlayerStates.pkPatterns(state.pkPlayerStates(), pid)));
            total += PkTileSet.size(PkFloor.asPkTileSet(
                    PkPlayerStates.pkFloor(state.pkPlayerStates(), pid)));
            total += PkTileSet.size(PkWall.asPkTileSet(
                    PkPlayerStates.pkWall(state.pkPlayerStates(), pid)));
        }
        total += PkTileSet.size(state.pkDiscardedTiles());
        return total;
    }

    @Test
    void midGameStatesCanBeSerializedAndDeserialized() {
        // Simulate a game, capture mid-game states, serialize and deserialize them
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var game = makeGame(2);
        var state = new MutableGameState(ImmutableGameState.initial(game));
        state.fillFactories(rng);

        var capturedStates = new ArrayList<String>();
        int moveCount = 0;

        while (!state.isGameOver() && moveCount < 20) {
            if (moveCount % 5 == 0) {
                capturedStates.add(serializeGameState(state));
            }

            var moves = new short[Move.MAX_MOVES];
            var count = state.uniqueValidMoves(moves);
            var idx = HeuristicMoveSelector.selectMove(rng, state, moves, count);
            state.registerMove(moves[idx]);
            moveCount += 1;

            if (state.isRoundOver()) {
                state.endRound();
                if (!state.isGameOver()) state.fillFactories(rng);
            }
        }

        // Verify each captured state can be parsed back correctly
        for (var serialized : capturedStates) {
            var parsed = parseGameState(serialized);
            assertNotNull(parsed);
            assertEquals(2, parsed.game().playersCount());

            // Re-serialize and compare
            var reserialized = serializeGameState(parsed);
            assertEquals(serialized, reserialized,
                    "Serialization roundtrip should be lossless");
        }
    }

    @Test
    void computedGameStatesFromDifferentSeedsAreValid() {
        // Generate and validate game states from multiple seeds and player counts
        var stateStrings = new ArrayList<String>();

        for (int players = 2; players <= 4; players += 1) {
            for (long seed = 0; seed < 3; seed += 1) {
                var rng = RandomGeneratorFactory.getDefault().create(seed);
                var game = makeGame(players);
                var state = new MutableGameState(ImmutableGameState.initial(game));
                state.fillFactories(rng);

                // Play 10 moves
                for (int m = 0; m < 10; m += 1) {
                    if (state.isRoundOver() || state.isGameOver()) break;
                    var moves = new short[Move.MAX_MOVES];
                    var count = state.uniqueValidMoves(moves);
                    var idx = HeuristicMoveSelector.selectMove(rng, state, moves, count);
                    state.registerMove(moves[idx]);
                }

                var serialized = serializeGameState(state);
                stateStrings.add(serialized);

                // Parse back and verify
                var parsed = parseGameState(serialized);
                assertEquals(players, parsed.game().playersCount());
                assertEquals(state.pkTileBag(), parsed.pkTileBag());
                assertEquals(state.currentPlayerId(), parsed.currentPlayerId());
            }
        }

        // All states should be distinct
        var unique = new HashSet<>(stateStrings);
        assertEquals(stateStrings.size(), unique.size(),
                "All states from different seeds/players should be distinct");
    }

    // ========================== 2-PLAYER SIMULATIONS ==========================

    @Test
    void simulateTwoPlayerGameSeed2026() {
        var state = simulateGame(2, 2026);
        int p1 = PkPlayerStates.points(state.pkPlayerStates(), PlayerId.P1);
        int p2 = PkPlayerStates.points(state.pkPlayerStates(), PlayerId.P2);
        assertTrue(p1 >= 0, "P1 score should be non-negative");
        assertTrue(p2 >= 0, "P2 score should be non-negative");
    }

    @Test
    void simulateMultipleTwoPlayerGames() {
        int totalP1Wins = 0;
        int totalP2Wins = 0;

        for (long seed = 0; seed < 20; seed += 1) {
            var state = simulateGame(2, seed);
            int p1 = PkPlayerStates.points(state.pkPlayerStates(), PlayerId.P1);
            int p2 = PkPlayerStates.points(state.pkPlayerStates(), PlayerId.P2);

            assertTrue(p1 >= 0 && p2 >= 0);

            var ranks = new int[2];
            RankComputer.playersRank(state, ranks);
            assertTrue(ranks[0] >= 0 && ranks[0] <= 1);
            assertTrue(ranks[1] >= 0 && ranks[1] <= 1);

            if (ranks[0] == 0) totalP1Wins += 1;
            if (ranks[1] == 0) totalP2Wins += 1;
        }

        assertTrue(totalP1Wins + totalP2Wins > 0);
    }

    // ========================== 3-PLAYER SIMULATIONS ==========================

    @Test
    void simulateThreePlayerGame() {
        var state = simulateGame(3, 2026);
        for (var pid : state.playerIds()) {
            int pts = PkPlayerStates.points(state.pkPlayerStates(), pid);
            assertTrue(pts >= 0, pid + " score should be non-negative, got " + pts);
        }

        var ranks = new int[3];
        RankComputer.playersRank(state, ranks);
        for (int i = 0; i < 3; i += 1) {
            assertTrue(ranks[i] >= 0 && ranks[i] < 3,
                    "Rank of P" + (i + 1) + " should be in [0,3), got " + ranks[i]);
        }
    }

    @Test
    void simulateMultipleThreePlayerGames() {
        for (long seed = 0; seed < 10; seed += 1) {
            var state = simulateGame(3, seed);
            var ranks = new int[3];
            RankComputer.playersRank(state, ranks);
            boolean hasWinner = false;
            for (int r : ranks) if (r == 0) hasWinner = true;
            assertTrue(hasWinner, "Every game should have a winner (rank 0)");
        }
    }

    // ========================== 4-PLAYER SIMULATIONS ==========================

    @Test
    void simulateFourPlayerGame() {
        var state = simulateGame(4, 2026);
        for (var pid : state.playerIds()) {
            int pts = PkPlayerStates.points(state.pkPlayerStates(), pid);
            assertTrue(pts >= 0, pid + " score should be non-negative");
        }

        var ranks = new int[4];
        RankComputer.playersRank(state, ranks);
        for (int i = 0; i < 4; i += 1) {
            assertTrue(ranks[i] >= 0 && ranks[i] < 4);
        }
    }

    @Test
    void simulateMultipleFourPlayerGames() {
        for (long seed = 0; seed < 10; seed += 1) {
            var state = simulateGame(4, seed);
            var ranks = new int[4];
            RankComputer.playersRank(state, ranks);
            boolean hasWinner = false;
            for (int r : ranks) if (r == 0) hasWinner = true;
            assertTrue(hasWinner, "Every game should have a winner");
        }
    }

    // ========================== AI vs AI (MIXED CONFIGS) ==========================

    @Test
    void simulateGameWithMixedHumanAIDescriptions() {
        // Game with mixed HUMAN/AI descriptions but all played by heuristic AI
        var descriptions = List.of(
                new Game.PlayerDescription(PlayerId.P1, "Human1", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "AI1", Game.PlayerDescription.PlayerKind.AI));
        var game = new Game(descriptions);
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var state = new MutableGameState(ImmutableGameState.initial(game));
        state.fillFactories(rng);

        int moveCount = 0;
        while (!state.isGameOver()) {
            var moves = new short[Move.MAX_MOVES];
            var count = state.uniqueValidMoves(moves);
            var idx = HeuristicMoveSelector.selectMove(rng, state, moves, count);
            state.registerMove(moves[idx]);
            moveCount += 1;

            if (state.isRoundOver()) {
                state.endRound();
                if (!state.isGameOver()) state.fillFactories(rng);
            }
            assertTrue(moveCount < 2000);
        }
        state.endGame();

        var ranks = new int[2];
        RankComputer.playersRank(state, ranks);
        assertTrue(ranks[0] >= 0 && ranks[1] >= 0);
    }

    @Test
    void simulateFourPlayerMixedGame() {
        var descriptions = List.of(
                new Game.PlayerDescription(PlayerId.P1, "Alice", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "BotA", Game.PlayerDescription.PlayerKind.AI),
                new Game.PlayerDescription(PlayerId.P3, "Bob", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P4, "BotB", Game.PlayerDescription.PlayerKind.AI));
        var game = new Game(descriptions);
        var rng = RandomGeneratorFactory.getDefault().create(99);
        var state = new MutableGameState(ImmutableGameState.initial(game));
        state.fillFactories(rng);

        int moveCount = 0;
        while (!state.isGameOver()) {
            var moves = new short[Move.MAX_MOVES];
            var count = state.uniqueValidMoves(moves);
            var idx = HeuristicMoveSelector.selectMove(rng, state, moves, count);
            state.registerMove(moves[idx]);
            moveCount += 1;
            if (state.isRoundOver()) {
                state.endRound();
                if (!state.isGameOver()) state.fillFactories(rng);
            }
            assertTrue(moveCount < 2000);
        }
        state.endGame();

        var ranks = new int[4];
        RankComputer.playersRank(state, ranks);
        boolean hasWinner = false;
        for (int r : ranks) if (r == 0) hasWinner = true;
        assertTrue(hasWinner);
    }

    // ========================== SCORE SANITY ==========================

    @Test
    void scoresAreReasonableAcrossManyGames() {
        int maxScoreSeen = 0;
        int minScoreSeen = Integer.MAX_VALUE;

        for (long seed = 0; seed < 50; seed += 1) {
            var state = simulateGame(2, seed);
            for (var pid : state.playerIds()) {
                int pts = PkPlayerStates.points(state.pkPlayerStates(), pid);
                maxScoreSeen = Math.max(maxScoreSeen, pts);
                minScoreSeen = Math.min(minScoreSeen, pts);
            }
        }

        assertTrue(maxScoreSeen <= 240, "Max score " + maxScoreSeen + " exceeds theoretical max");
        assertTrue(maxScoreSeen > 0, "Max score should be positive across 50 games");
    }

    @Test
    void heuristicAIScoresBetterThanRandom() {
        // Compare heuristic AI scores vs random play
        int heuristicTotal = 0;
        int randomTotal = 0;

        for (long seed = 0; seed < 20; seed += 1) {
            // Heuristic game
            var hState = simulateGame(2, seed);
            for (var pid : hState.playerIds())
                heuristicTotal += PkPlayerStates.points(hState.pkPlayerStates(), pid);

            // Random game
            var rng = RandomGeneratorFactory.getDefault().create(seed);
            var game = makeGame(2);
            var rState = new MutableGameState(ImmutableGameState.initial(game));
            rState.fillFactories(rng);
            int mc = 0;
            while (!rState.isGameOver()) {
                var moves = new short[Move.MAX_MOVES];
                var count = rState.uniqueValidMoves(moves);
                rState.registerMove(moves[rng.nextInt(count)]);
                mc += 1;
                if (rState.isRoundOver()) {
                    rState.endRound();
                    if (!rState.isGameOver()) rState.fillFactories(rng);
                }
                if (mc > 2000) break;
            }
            rState.endGame();
            for (var pid : rState.playerIds())
                randomTotal += PkPlayerStates.points(rState.pkPlayerStates(), pid);
        }

        // Heuristic AI should generally score higher than pure random
        assertTrue(heuristicTotal > randomTotal,
                "Heuristic total (" + heuristicTotal + ") should beat random total ("
                        + randomTotal + ")");
    }

    // ========================== TERMINATION ==========================

    @Test
    void allGamesTerminate() {
        for (int players = 2; players <= 4; players += 1) {
            for (long seed = 0; seed < 5; seed += 1) {
                final int p = players;
                final long s = seed;
                assertDoesNotThrow(() -> simulateGame(p, s),
                        "Game with " + p + " players and seed " + s
                                + " should terminate");
            }
        }
    }

    @Test
    void gameEndsWithAtLeastOneFullRow() {
        for (long seed = 0; seed < 10; seed += 1) {
            var state = simulateGame(2, seed);
            boolean anyFullRow = false;
            for (var pid : state.playerIds()) {
                int wall = PkPlayerStates.pkWall(state.pkPlayerStates(), pid);
                if (PkWall.hasFullRow(wall)) anyFullRow = true;
            }
            assertTrue(anyFullRow,
                    "Game should end with at least one player having a full row (seed " + seed + ")");
        }
    }

    // ========================== RANK CONSISTENCY ==========================

    @Test
    void ranksAreConsistentWithScores() {
        for (long seed = 0; seed < 30; seed += 1) {
            var state = simulateGame(2, seed);
            int p1 = PkPlayerStates.points(state.pkPlayerStates(), PlayerId.P1);
            int p2 = PkPlayerStates.points(state.pkPlayerStates(), PlayerId.P2);

            var ranks = new int[2];
            RankComputer.playersRank(state, ranks);

            if (p1 > p2) {
                assertTrue(ranks[0] < ranks[1],
                        "P1 (" + p1 + " pts) should rank higher than P2 (" + p2 + " pts)");
            } else if (p2 > p1) {
                assertTrue(ranks[1] < ranks[0],
                        "P2 (" + p2 + " pts) should rank higher than P1 (" + p1 + " pts)");
            } else {
                assertTrue(ranks[0] >= 0 && ranks[1] >= 0);
            }
        }
    }

    @Test
    void fourPlayerRanksAreConsistent() {
        for (long seed = 0; seed < 10; seed += 1) {
            var state = simulateGame(4, seed);
            var ranks = new int[4];
            RankComputer.playersRank(state, ranks);

            // Collect (points, rank) pairs and verify ordering
            int[] points = new int[4];
            for (int i = 0; i < 4; i += 1) {
                points[i] = PkPlayerStates.points(state.pkPlayerStates(), PlayerId.ALL.get(i));
            }

            // If p_i > p_j then rank_i < rank_j (strictly)
            for (int i = 0; i < 4; i += 1) {
                for (int j = i + 1; j < 4; j += 1) {
                    if (points[i] > points[j]) {
                        assertTrue(ranks[i] < ranks[j],
                                "P" + (i + 1) + "(" + points[i] + "pts,rank" + ranks[i]
                                        + ") should outrank P" + (j + 1) + "(" + points[j]
                                        + "pts,rank" + ranks[j] + ") seed=" + seed);
                    } else if (points[j] > points[i]) {
                        assertTrue(ranks[j] < ranks[i],
                                "P" + (j + 1) + "(" + points[j] + "pts,rank" + ranks[j]
                                        + ") should outrank P" + (i + 1) + "(" + points[i]
                                        + "pts,rank" + ranks[i] + ") seed=" + seed);
                    }
                }
            }
        }
    }

    // ========================== ROUND COUNTING ==========================

    @Test
    void gamesLastAtLeastTwoRounds() {
        // With heuristic AI, games typically last multiple rounds
        for (long seed = 0; seed < 10; seed += 1) {
            var rng = RandomGeneratorFactory.getDefault().create(seed);
            var game = makeGame(2);
            var state = new MutableGameState(ImmutableGameState.initial(game));
            state.fillFactories(rng);

            int roundCount = 1;
            int moveCount = 0;

            while (!state.isGameOver()) {
                var moves = new short[Move.MAX_MOVES];
                var count = state.uniqueValidMoves(moves);
                var idx = HeuristicMoveSelector.selectMove(rng, state, moves, count);
                state.registerMove(moves[idx]);
                moveCount += 1;

                if (state.isRoundOver()) {
                    state.endRound();
                    if (!state.isGameOver()) {
                        state.fillFactories(rng);
                        roundCount += 1;
                    }
                }
                if (moveCount > 2000) break;
            }
            state.endGame();

            assertTrue(roundCount >= 2,
                    "Game should last at least 2 rounds, got " + roundCount + " (seed " + seed + ")");
        }
    }
}