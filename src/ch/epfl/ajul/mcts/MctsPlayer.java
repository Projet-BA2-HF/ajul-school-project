package ch.epfl.ajul.mcts;

import ch.epfl.ajul.Player;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.RankComputer;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.MutableGameState;
import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;

import java.util.Arrays;
import java.util.Comparator;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

/// Represents an AI player using the Monte Carlo Tree Search algorithm.
///
/// This player builds a partial game tree from the current game state. For each
/// iteration, it searches for a node to evaluate, simulates the rest of the game
/// from that node using the heuristic move selector, computes generalized points
/// from the final ranks and scores, and propagates these points back through the
/// explored path.
///
/// @author Hedi Fourati
/// @author Fahd Ben Ismail
public final class MctsPlayer implements Player {
    private final RandomGeneratorFactory<RandomGenerator> random;
    private final int iterations;

    // Initial number of player/child pairs stored in the explored path
    private static final int INITIAL_PAIRS = 16;

    // Number of bits used to multiply the rank complement by 256
    private static final int POINT_SHIFT = 8;

    /// Constructs a new MCTS player.
    ///
    /// @param random the random generator factory used by the MCTS algorithm
    /// @param iterations the number of MCTS iterations performed to choose a move
    public MctsPlayer(RandomGeneratorFactory<RandomGenerator> random, int iterations){
        this.random = random;
        this.iterations = iterations;
    }

    /// Computes the next move to play from the given game state.
    ///
    /// The method repeatedly explores the MCTS tree, simulates full games from
    /// the selected nodes, evaluates the result of these simulations using
    /// generalized points, and finally returns the move associated with the child
    /// of the root having the highest average score.
    ///
    /// @param gameState the current read-only game state
    /// @return the move chosen by the MCTS algorithm
    @Override
    public Move nextMove(ReadOnlyGameState gameState) {
        int playerCount = gameState.game().playersCount();
        // Buffers and accumulators reused across iterations to avoid allocations
        // on the MCTS hot path.
        short[] validMoves = new short[Move.MAX_MOVES];
        int[] playersRank = new int[playerCount];
        byte[] browsedPath = new byte[INITIAL_PAIRS * 2];
        int[] generalizedPoints = new int[playerCount];
        // Single generator dedicated to random play-outs; seeded deterministically
        // from the initial bag so a given state always produces the same move.
        RandomGenerator simulationRandom = random.create(gameState.pkTileBag());

        MctsNode root = MctsNode.newRoot();

        for(int i = 0; i < iterations; i++){
            MctsNode node = root;
            MutableGameState game = new MutableGameState(gameState);
            int pathSize = 0;

            // --- Phase 1 : selection / expansion ---
            // Walk down the existing tree picking the highest-priority child,
            // expanding nodes lazily, until we reach a node that has not yet
            // been evaluated (gameCount == 0) or a terminal state.
            while(node.gameCount() > 0 && !game.isGameOver()){
                if (node.childNodes == null){
                    int validMovesCount = game.uniqueValidMoves(validMoves);

                    node.childNodes = new MctsNode[validMovesCount];

                    for(int j = 0; j < validMovesCount; j++){
                        node.childNodes[j] = MctsNode.newMoveNode(validMoves[j]);
                    }
                }
                int childIndexToExplore = node.indexOfChildToExplore();
                MctsNode child = node.childNodes[childIndexToExplore];

                int playerIndex = game.currentPlayerId().ordinal();

                // Remember the (current player, child index) pair so that
                // back-propagation can credit the right player on the right node.
                browsedPath = addPair(
                                browsedPath,
                                (byte) playerIndex,
                                (byte) childIndexToExplore,
                                pathSize);

                short move = child.pkMove();
                game.registerMove(move);
                node = child;
                pathSize++;
                if(game.isRoundOver()){
                    game.endRound();
                    // Same node visited again would use the same seed and thus
                    // observe the same factory refill, keeping the tree consistent.
                    if(!game.isGameOver())
                        game.fillFactories(random.create(node.pkMove()));
                }
            }

            // --- Phase 2 : random play-out ---
            // From the evaluated node, finish the game by playing heuristically
            // chosen moves until completion.
            while(!game.isGameOver()){
                int validMovesCount = game.validMoves(validMoves);
                int moveIndex = HeuristicMoveSelector.selectMove(simulationRandom,
                                                        game,
                                                        validMoves,
                                                        validMovesCount);

                game.registerMove(validMoves[moveIndex]);

                if(game.isRoundOver()){
                    game.endRound();

                    if(!game.isGameOver())
                        game.fillFactories(simulationRandom);
                }
            }
            game.endGame();

            RankComputer.playersRank(game, playersRank);

            // Generalized points combine the rank (high-order bits) with the
            // raw score, so the algorithm prioritizes ranking over points.
            for(PlayerId player : game.playerIds()) {
                int rankComplement = playerCount - 1 - playersRank[player.ordinal()];
                int realPoints = PkPlayerStates.points(game.pkPlayerStates(), player);
                generalizedPoints[player.ordinal()] = (rankComplement << POINT_SHIFT) + realPoints;
            }

            // --- Phase 3 : back-propagation ---
            // Increment the root's game count (no points), then add the
            // appropriate player's generalized points to each visited node.
            root.registerEvaluation(0);
            MctsNode currentNode = root;
            for(int nodeIndex = 0; nodeIndex < pathSize; nodeIndex++) {
                int playerIndex = getPlayerIndex(nodeIndex, browsedPath);
                int childIndex = getChildIndex(nodeIndex, browsedPath);
                int points = generalizedPoints[playerIndex];

                MctsNode childNode = currentNode.childNodes[childIndex];
                childNode.registerEvaluation(points);
                currentNode = childNode;
            }
        }

        // After all iterations, pick the child of the root with the highest
        // average score: it is the move with the best expected outcome.
        return Arrays.stream(root.childNodes)
                                .max(Comparator.comparingDouble(MctsNode::averagePoints))
                                .map(MctsNode::pkMove)
                                .map(Move::ofPacked)
                                .orElseThrow();
    }

    // Stores a pair in the path: player index at the even position,
    // child index at the following odd position. Doubles the array if full.
    private byte[] addPair(byte[] path, byte playerIndex, byte childIndex, int indexPair){
        if(indexPair * 2 >= path.length){
            byte[] path2 = Arrays.copyOf(path, path.length * 2);
            return addPair(path2, playerIndex, childIndex, indexPair);
        }
        path[indexPair * 2] = playerIndex;
        path[indexPair * 2 + 1] = childIndex ;
        return path;
    }

    // Returns the unsigned player index stored at the given pair index
    private int getPlayerIndex(int index, byte[] browsedPath){
        return Byte.toUnsignedInt(browsedPath[index * 2]);
    }

    // Returns the unsigned child index stored at the given pair index
    private int getChildIndex(int index, byte[] browsedPath){
        return Byte.toUnsignedInt(browsedPath[index * 2 + 1]);
    }
}
