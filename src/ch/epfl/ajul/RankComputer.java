package ch.epfl.ajul;

import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkWall;

import java.util.Arrays;

/// Computes the rank of all players at the end of a game of Ajul.
///
/// @author Hedi Fourati
/// @author Fahd Ben Ismail
public final class RankComputer {

    // Number of bits needed to store the number of complete rows (0 to 5)
    private static final int FULL_ROW_COUNT_BITS = 3;

    // Number of bits needed to store the player index (0 to 3)
    private static final int PLAYER_INDEX_BITS = 2;

    // Mask used to extract the player index from a packed player value
    private static final int PLAYER_INDEX_MASK = (1 << PLAYER_INDEX_BITS) - 1;

    /// Fills the given array with the rank of each player.
    /// Players are ranked by decreasing number of points, then by decreasing
    /// number of complete rows in their wall. Equal players receive the same rank.
    ///
    /// @param gameState the game state
    /// @param ranks the array to fill with players' ranks
    public static void playersRank(ReadOnlyGameState gameState, int[] ranks) {
        int playerCount = gameState.playerIds().size();
        assert ranks.length == playerCount;

        int[] packedPlayers = new int[playerCount];

        for (PlayerId playerId : gameState.playerIds()) {
            int playerIndex = playerId.ordinal();
            int playerPoints = PkPlayerStates.points(gameState.pkPlayerStates(), playerId);
            int fullRowCount = fullRowCount(gameState, playerId);

            int rankScore = (playerPoints << FULL_ROW_COUNT_BITS) | fullRowCount;
            packedPlayers[playerIndex] = (rankScore << PLAYER_INDEX_BITS) | playerIndex;
        }

        Arrays.sort(packedPlayers);

        int previousRankScore = -1;
        int currentRank = -1;

        for (int sortedIndex = playerCount - 1; sortedIndex >= 0; sortedIndex--) {
            int rankingPosition = playerCount - 1 - sortedIndex;
            int packedPlayer = packedPlayers[sortedIndex];

            int playerIndex = packedPlayer & PLAYER_INDEX_MASK;
            int rankScore = packedPlayer >>> PLAYER_INDEX_BITS;

            if (rankScore != previousRankScore) {
                currentRank = rankingPosition;
                previousRankScore = rankScore;
            }

            ranks[playerIndex] = currentRank;
        }
    }

    private static int fullRowCount(ReadOnlyGameState gameState, PlayerId playerId) {
        int packedWall = PkPlayerStates.pkWall(gameState.pkPlayerStates(), playerId);
        int fullRowCount = 0;

        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
            if (PkWall.isRowFull(packedWall, line)) {
                fullRowCount += 1;
            }
        }

        return fullRowCount;
    }
}