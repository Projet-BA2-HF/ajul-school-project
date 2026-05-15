package ch.epfl.ajul.mcts;

import ch.epfl.ajul.TileDestination;
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

        IndexReservoir exactFillMoveIndices = new IndexReservoir();
        IndexReservoir partialFillMoveIndices = new IndexReservoir();
        IndexReservoir otherMoveIndices = new IndexReservoir();

        int pkPatterns = PkPlayerStates.pkPatterns(
                gameState.pkPlayerStates(),
                gameState.currentPlayerId()
        );

        for (int moveIndex = 0; moveIndex < validMoveCount; moveIndex++) {
            short packedMove = packedMoves[moveIndex];

            // Default category is "other"; refined below for pattern-line destinations.
            IndexReservoir reservoir = otherMoveIndices;

            if(PkMove.destination(packedMove) instanceof TileDestination.Pattern line) {
                int movedColoredTileCount = PkTileSet.countOf(
                        gameState.pkTileSources().get(PkMove.source(packedMove).index()),
                        PkMove.color(packedMove)
                );
                int remainingSpaceInLine = line.capacity() - PkPatterns.size(pkPatterns, line);
                // Equal: exact fill. Less: partial fill. Greater (overflow): stays "other".
                if (movedColoredTileCount == remainingSpaceInLine)
                    reservoir = exactFillMoveIndices;
                else if (movedColoredTileCount < remainingSpaceInLine)
                    reservoir = partialFillMoveIndices;
            }
            reservoir.consider(randomGenerator, moveIndex);
        }

        // Try categories in decreasing priority. The "other" reservoir is guaranteed
        // non-empty when the two others are empty, because validMoveCount > 0.
        if (exactFillMoveIndices.nonEmpty())
            return exactFillMoveIndices.sample();
        if (partialFillMoveIndices.nonEmpty())
            return partialFillMoveIndices.sample();
        return otherMoveIndices.sample();
    }

    // Reservoir sampler of capacity 1 storing move indices.
    private static final class IndexReservoir {
        int encounteredIndexCount;
        int sampledIndex;

        // Creates a new empty reservoir using the given random generator.
        IndexReservoir() {
            this.encounteredIndexCount = 0;
            this.sampledIndex = -1;
        }

        // Considers the given move index for inclusion in the reservoir.
        void consider(RandomGenerator randomGenerator, int moveIndex) {
            encounteredIndexCount += 1;
            if (randomGenerator.nextInt(encounteredIndexCount) == 0)
                sampledIndex = moveIndex;
        }

        // Returns true if at least one element has been considered.
        boolean nonEmpty() {
            return encounteredIndexCount != 0;
        }

        // Returns the move index currently held in the reservoir.
        int sample() {
            return sampledIndex;
        }
    }
}