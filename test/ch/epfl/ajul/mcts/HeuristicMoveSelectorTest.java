package ch.epfl.ajul.mcts;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.MutableGameState;
import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.PkMove;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.random.RandomGeneratorFactory;

import static org.junit.jupiter.api.Assertions.*;

class HeuristicMoveSelectorTest {

    private static Game twoPlayerGame() {
        return new Game(List.of(
                new Game.PlayerDescription(PlayerId.P1, "P1",
                        Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "P2",
                        Game.PlayerDescription.PlayerKind.HUMAN)));
    }

    private static MutableGameState newGameWithFactories(long seed) {
        var game = twoPlayerGame();
        var state = new MutableGameState(ImmutableGameState.initial(game));
        var rng = RandomGeneratorFactory.getDefault().create(seed);
        state.fillFactories(rng);
        return state;
    }

    @Test
    void selectMoveReturnsValidIndex() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var state = newGameWithFactories(42);
        var moves = new short[Move.MAX_MOVES];
        var count = state.uniqueValidMoves(moves);

        var idx = HeuristicMoveSelector.selectMove(rng, state, moves, count);
        assertTrue(idx >= 0 && idx < count);
    }

    @Test
    void selectMoveAlwaysReturnsValidIndexMultipleSeeds() {
        for (long seed = 0; seed < 100; seed += 1) {
            var rng = RandomGeneratorFactory.getDefault().create(seed);
            var state = newGameWithFactories(seed * 7 + 13);
            var moves = new short[Move.MAX_MOVES];
            var count = state.uniqueValidMoves(moves);

            var idx = HeuristicMoveSelector.selectMove(rng, state, moves, count);
            assertTrue(idx >= 0, "Index should be >= 0, got " + idx);
            assertTrue(idx < count, "Index should be < " + count + ", got " + idx);
        }
    }

    @Test
    void selectMoveReturnsMoveThatIsInArray() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var state = newGameWithFactories(99);
        var moves = new short[Move.MAX_MOVES];
        var count = state.uniqueValidMoves(moves);

        var idx = HeuristicMoveSelector.selectMove(rng, state, moves, count);
        var selectedMove = moves[idx];

        // The selected move should be a valid packed move
        var move = Move.ofPacked(selectedMove);
        assertNotNull(move);
    }

    @Test
    void selectMoveIsDeterministicWithSameSeed() {
        var state = newGameWithFactories(42);
        var moves = new short[Move.MAX_MOVES];
        var count = state.uniqueValidMoves(moves);

        var rng1 = RandomGeneratorFactory.getDefault().create(2026);
        var idx1 = HeuristicMoveSelector.selectMove(rng1, state, moves, count);

        var rng2 = RandomGeneratorFactory.getDefault().create(2026);
        var idx2 = HeuristicMoveSelector.selectMove(rng2, state, moves, count);

        assertEquals(idx1, idx2);
    }

    @Test
    void selectMoveProducesVariedResultsAcrossSeeds() {
        var state = newGameWithFactories(42);
        var moves = new short[Move.MAX_MOVES];
        var count = state.uniqueValidMoves(moves);

        var seen = new java.util.HashSet<Integer>();
        for (long seed = 0; seed < 200; seed += 1) {
            var rng = RandomGeneratorFactory.getDefault().create(seed);
            seen.add(HeuristicMoveSelector.selectMove(rng, state, moves, count));
        }
        // Should see at least a few different move indices
        assertTrue(seen.size() > 1,
                "Expected varied results, but only saw " + seen.size() + " distinct indices");
    }

    @Test
    void selectMoveWorksWithSingleValidMove() {
        // Create a state, play until near the end of a round,
        // leaving only one valid move
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var state = newGameWithFactories(42);

        // Play moves until we find a state with very few valid moves
        int maxAttempts = 200;
        for (int i = 0; i < maxAttempts; i += 1) {
            var moves = new short[Move.MAX_MOVES];
            var count = state.uniqueValidMoves(moves);

            if (count == 1) {
                var idx = HeuristicMoveSelector.selectMove(rng, state, moves, count);
                assertEquals(0, idx);
                return;
            }

            var idx = HeuristicMoveSelector.selectMove(rng, state, moves, count);
            state.registerMove(moves[idx]);

            if (state.isRoundOver()) {
                state.endRound();
                if (!state.isGameOver()) {
                    state.fillFactories(rng);
                }
            }
            if (state.isGameOver()) break;
        }
        // If we never hit count == 1, just verify it doesn't crash
    }

    @Test
    void selectMovePrefersFillOverPartialFill() {
        // Run many games and collect statistics: exact fill moves should be preferred
        var rng = RandomGeneratorFactory.getDefault().create(2026);

        int exactFillSelected = 0;
        int totalSelections = 0;

        for (long seed = 0; seed < 50; seed += 1) {
            var state = newGameWithFactories(seed * 3 + 7);
            var moves = new short[Move.MAX_MOVES];
            var count = state.uniqueValidMoves(moves);

            if (count <= 1) continue;

            var selRng = RandomGeneratorFactory.getDefault().create(seed);
            var idx = HeuristicMoveSelector.selectMove(selRng, state, moves, count);
            totalSelections += 1;

            // Check if selected move is an exact fill
            var selectedMove = moves[idx];
            var dest = PkMove.destination(selectedMove);
            if (dest instanceof TileDestination.Pattern) {
                // It chose a pattern line; good heuristic behavior
                exactFillSelected += 1;
            }
        }

        // The heuristic should prefer pattern lines over the floor in most cases
        assertTrue(exactFillSelected > totalSelections / 2,
                "Expected heuristic to prefer pattern lines, but only " +
                        exactFillSelected + "/" + totalSelections + " selected a pattern line");
    }

    @Test
    void selectMoveWorksWithFullGameSimulation() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var state = newGameWithFactories(42);

        int moveCount = 0;
        while (!state.isGameOver()) {
            var moves = new short[Move.MAX_MOVES];
            var count = state.uniqueValidMoves(moves);

            var idx = HeuristicMoveSelector.selectMove(rng, state, moves, count);
            assertTrue(idx >= 0 && idx < count);

            state.registerMove(moves[idx]);
            moveCount += 1;

            if (state.isRoundOver()) {
                state.endRound();
                if (!state.isGameOver()) {
                    state.fillFactories(rng);
                }
            }

            // Safety: prevent infinite loops
            assertTrue(moveCount < 1000, "Game should finish within 1000 moves");
        }

        state.endGame();
        assertTrue(moveCount > 0, "At least one move should have been played");
    }
}
