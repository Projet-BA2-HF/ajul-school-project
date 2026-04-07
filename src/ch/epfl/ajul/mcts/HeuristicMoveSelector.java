package ch.epfl.ajul.mcts;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.PkMove;
import ch.epfl.ajul.gamestate.packed.PkPatterns;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkTileSet;

import java.util.random.RandomGenerator;

/// Selects a move according to the heuristic policy described in step 7:
/// exact pattern fill first, then partial pattern fill, then other moves.
///
/// @author Hedi Fourati
/// @author Fahd Ben Ismail
public final class HeuristicMoveSelector {

    /// Selects one move among the first {@code validMoveCount} packed moves stored in
    /// {@code packedMoves}, and returns its index.
    ///
    /// @param randomGenerator the random generator used for reservoir sampling
    /// @param gameState the current game state
    /// @param packedMoves the array containing packed valid moves
    /// @param validMoveCount the number of valid moves stored at the beginning of the array
    /// @return the index of the selected move
    public static int selectMove(
            RandomGenerator randomGenerator,
            ReadOnlyGameState gameState,
            short[] packedMoves,
            int validMoveCount
    ) {
        assert 0 < validMoveCount && validMoveCount <= packedMoves.length;

        IndexReservoir exactFillMoveIndices = new IndexReservoir(randomGenerator);
        IndexReservoir partialFillMoveIndices = new IndexReservoir(randomGenerator);
        IndexReservoir otherMoveIndices = new IndexReservoir(randomGenerator);

        int packedCurrentPlayerPatterns = PkPlayerStates.pkPatterns(
                gameState.pkPlayerStates(),
                gameState.currentPlayerId()
        );

        for (int moveIndex = 0; moveIndex < validMoveCount; moveIndex++) {
            short packedMove = packedMoves[moveIndex];
            TileDestination destination = PkMove.destination(packedMove);

            if (destination == TileDestination.FLOOR) {
                otherMoveIndices.consider(moveIndex);
            } else {
                TileDestination.Pattern destinationLine =
                        (TileDestination.Pattern) destination;

                int movedColoredTileCount = movedColoredTileCount(gameState, packedMove);
                int remainingSpaceInLine = remainingSpaceInLine(
                        packedCurrentPlayerPatterns,
                        destinationLine
                );

                if (movedColoredTileCount == remainingSpaceInLine) {
                    exactFillMoveIndices.consider(moveIndex);
                } else if (movedColoredTileCount < remainingSpaceInLine) {
                    partialFillMoveIndices.consider(moveIndex);
                } else {
                    otherMoveIndices.consider(moveIndex);
                }
            }
        }

        if (exactFillMoveIndices.containsSample()) {
            return exactFillMoveIndices.sample();
        }
        if (partialFillMoveIndices.containsSample()) {
            return partialFillMoveIndices.sample();
        }
        return otherMoveIndices.sample();
    }

    private static int movedColoredTileCount(ReadOnlyGameState gameState, short packedMove) {
        TileSource source = PkMove.source(packedMove);
        TileKind.Colored color = PkMove.color(packedMove);
        int packedSourceTiles = gameState.pkTileSources().get(source.index());
        return PkTileSet.countOf(packedSourceTiles, color);
    }

    private static int remainingSpaceInLine(
            int packedPatterns,
            TileDestination.Pattern line
    ) {
        int currentTileCount = PkPatterns.size(packedPatterns, line);
        return line.capacity() - currentTileCount;
    }

    /// Reservoir sampler of capacity 1 storing move indices.
    private static final class IndexReservoir {
        private final RandomGenerator randomGenerator;
        private int encounteredIndexCount;
        private int sampledIndex;

        IndexReservoir(RandomGenerator randomGenerator) {
            this.randomGenerator = randomGenerator;
            this.encounteredIndexCount = 0;
            this.sampledIndex = -1;
        }

        void consider(int moveIndex) {
            encounteredIndexCount += 1;

            if (randomGenerator.nextInt(encounteredIndexCount) == 0) {
                sampledIndex = moveIndex;
            }
        }

        boolean containsSample() {
            return encounteredIndexCount > 0;
        }

        int sample() {
            return sampledIndex;
        }
    }
}