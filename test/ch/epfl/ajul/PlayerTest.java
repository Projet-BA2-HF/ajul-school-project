//package ch.epfl.ajul;
//
//import ch.epfl.ajul.gamestate.ImmutableGameState;
//import ch.epfl.ajul.gamestate.MutableGameState;
//import ch.epfl.ajul.gamestate.ReadOnlyGameState;
//import ch.epfl.ajul.gamestate.Move;
//
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//import java.util.random.RandomGeneratorFactory;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class PlayerTest {
//
//    private static Game twoPlayerGame() {
//        return new Game(List.of(
//                new Game.PlayerDescription(PlayerId.P1, "P1",
//                        Game.PlayerDescription.PlayerKind.HUMAN),
//                new Game.PlayerDescription(PlayerId.P2, "P2",
//                        Game.PlayerDescription.PlayerKind.HUMAN)));
//    }
//
//    @Test
//    void playerIsFunctionalInterface() {
//        // Player should be usable as a lambda
//        Player player = gameState -> {
//            var moves = new short[Move.MAX_MOVES];
//            var count = gameState.uniqueValidMoves(moves);
//            return moves[0]; // Always play first valid move
//        };
//        assertNotNull(player);
//    }
//
//    @Test
//    void playerLambdaCanBeInvoked() {
//        var rng = RandomGeneratorFactory.getDefault().create(2026);
//        var game = twoPlayerGame();
//        var state = new MutableGameState(ImmutableGameState.initial(game));
//        state.fillFactories(rng);
//
//        Player player = gameState -> {
//            var moves = new short[Move.MAX_MOVES];
//            var count = gameState.uniqueValidMoves(moves);
//            return moves[0];
//        };
//
//        int move = player.nextMove(state);
//        // The returned move should be a valid packed move
//        assertDoesNotThrow(() -> Move.ofPacked((short) move));
//    }
//
//    @Test
//    void playerCanBeImplementedAsClass() {
//        class RandomPlayer implements Player {
//            private final java.util.random.RandomGenerator rng;
//
//            RandomPlayer(java.util.random.RandomGenerator rng) {
//                this.rng = rng;
//            }
//
//            @Override
//            public move nextMove(ReadOnlyGameState gameState) {
//                var moves = new short[Move.MAX_MOVES];
//                var count = gameState.uniqueValidMoves(moves);
//                return moves[rng.nextInt(count)];
//            }
//        }
//
//        var rng = RandomGeneratorFactory.getDefault().create(2026);
//        var game = twoPlayerGame();
//        var state = new MutableGameState(ImmutableGameState.initial(game));
//        state.fillFactories(rng);
//
//        var player = new RandomPlayer(rng);
//        int move = player.nextMove(state);
//        assertDoesNotThrow(() -> Move.ofPacked((short) move));
//    }
//
//    @Test
//    void playerCanPlayFullGame() {
//        var rng = RandomGeneratorFactory.getDefault().create(2026);
//        var game = twoPlayerGame();
//        var state = new MutableGameState(ImmutableGameState.initial(game));
//        state.fillFactories(rng);
//
//        Player p1 = gs -> {
//            var moves = new short[Move.MAX_MOVES];
//            var count = gs.uniqueValidMoves(moves);
//            return moves[rng.nextInt(count)];
//        };
//        Player p2 = gs -> {
//            var moves = new short[Move.MAX_MOVES];
//            var count = gs.uniqueValidMoves(moves);
//            return moves[rng.nextInt(count)];
//        };
//
//        Player[] players = {p1, p2};
//        int moveCount = 0;
//
//        while (!state.isGameOver()) {
//            var currentPlayer = players[state.currentPlayerId().ordinal()];
//            int move = currentPlayer.nextMove(state);
//            state.registerMove((short) move);
//            moveCount += 1;
//
//            if (state.isRoundOver()) {
//                state.endRound();
//                if (!state.isGameOver()) state.fillFactories(rng);
//            }
//
//            assertTrue(moveCount < 1000, "Game should end within 1000 moves");
//        }
//
//        state.endGame();
//        assertTrue(moveCount > 0);
//    }
//
//    @Test
//    void playerMethodReference() {
//        // Test that a method reference can serve as a Player
//        Player player = PlayerTest::firstValidMove;
//        assertNotNull(player);
//    }
//
//    private static int firstValidMove(ReadOnlyGameState gameState) {
//        var moves = new short[Move.MAX_MOVES];
//        gameState.uniqueValidMoves(moves);
//        return moves[0];
//    }
//}
