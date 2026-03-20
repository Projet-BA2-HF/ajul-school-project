package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.Points;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;

/// Contains static methods for manipulating the packed states of all players in a game of Ajul.
/// The states are stored in a flat {@code int} array of size {@code 4 * n}, where {@code n} is the
/// number of players. For each player, the 4 consecutive elements are, in order:
/// <ul>
///   <li>the packed pattern lines (see {@link PkPatterns}),</li>
///   <li>the packed floor line (see {@link PkFloor}),</li>
///   <li>the packed wall (see {@link PkWall}),</li>
///   <li>the player's current score (number of points).</li>
/// </ul>
/// Methods that only read from the array accept a {@link ReadOnlyIntArray}, while methods that
/// modify the array accept a primitive {@code int[]}.
public final class PkPlayerStates {
    /// The initial number of points for each player.
    private static final int INITIAL_POINTS = 0;
    /// The number of {@code int} elements used per player.
    private static final int PLAYER_SIZE = 4;
    /// The offset of the packed pattern lines within a player's block.
    private static final int PK_PATTERNS_OFFSET = 0;
    /// The offset of the packed floor line within a player's block.
    private static final int PK_FLOOR_OFFSET = PK_PATTERNS_OFFSET + 1;
    /// The offset of the packed wall within a player's block.
    private static final int PK_WALL_OFFSET = PK_FLOOR_OFFSET + 1;
    /// The offset of the points within a player's block.
    private static final int PK_POINTS_OFFSET = PK_WALL_OFFSET + 1;

    /// Returns an immutable array containing the initial packed state of all players in the given
    /// game. All pattern lines, floor lines and walls are empty, and all scores are 0.
    ///
    /// @param game the game whose players are initialized
    /// @return the initial packed player states
    public static ImmutableIntArray initial(Game game){
        int playerCount = game.playersCount();
        int[] initialStates = new int[playerCount * PLAYER_SIZE];
        for(int i = 0; i < playerCount; i++){
            initialStates[i * PLAYER_SIZE + PK_PATTERNS_OFFSET] = PkPatterns.EMPTY;
            initialStates[i * PLAYER_SIZE + PK_FLOOR_OFFSET] = PkFloor.EMPTY;
            initialStates[i * PLAYER_SIZE + PK_WALL_OFFSET] = PkWall.EMPTY;
            initialStates[i * PLAYER_SIZE + PK_POINTS_OFFSET] = INITIAL_POINTS;
        }
        return ImmutableIntArray.copyOf(initialStates);
    }

    /// Computes the index in the flat array for the given player and component offset.
    ///
    /// @param playerId the identity of the player
    /// @param offset   the component offset within the player's block
    /// @return the array index
    private static int computeIndex(PlayerId playerId, int offset) {
        return playerId.ordinal() * PLAYER_SIZE + offset;
    }

    /// Returns the packed pattern lines of the given player.
    ///
    /// @param pkPlayerStates the packed player states array
    /// @param playerId       the identity of the player
    /// @return the packed pattern lines (see {@link PkPatterns})
    public static int pkPatterns(ReadOnlyIntArray pkPlayerStates, PlayerId playerId){
        return pkPlayerStates.get(computeIndex(playerId, PK_PATTERNS_OFFSET));
    }

    /// Returns the packed floor line of the given player.
    ///
    /// @param pkPlayerStates the packed player states array
    /// @param playerId       the identity of the player
    /// @return the packed floor line (see {@link PkFloor})
    public static int pkFloor(ReadOnlyIntArray pkPlayerStates, PlayerId playerId){
        return pkPlayerStates.get(computeIndex(playerId, PK_FLOOR_OFFSET));
    }

    /// Returns the packed wall of the given player.
    ///
    /// @param pkPlayerStates the packed player states array
    /// @param playerId       the identity of the player
    /// @return the packed wall (see {@link PkWall})
    public static int pkWall(ReadOnlyIntArray pkPlayerStates, PlayerId playerId) {
        return pkPlayerStates.get(computeIndex(playerId, PK_WALL_OFFSET));
    }

    /// Returns the current score (number of points) of the given player.
    ///
    /// @param pkPlayerStates the packed player states array
    /// @param playerId       the identity of the player
    /// @return the player's score
    public static int points(ReadOnlyIntArray pkPlayerStates, PlayerId playerId) {
        return pkPlayerStates.get(computeIndex(playerId, PK_POINTS_OFFSET));
    }

    /// Sets the packed pattern lines of the given player in the mutable array.
    ///
    /// @param pkPlayerStates the mutable packed player states array
    /// @param playerId       the identity of the player
    /// @param pkPatterns     the new packed pattern lines value
    public static void setPkPatterns(int[] pkPlayerStates, PlayerId playerId, int pkPatterns) {
        pkPlayerStates[computeIndex(playerId, PK_PATTERNS_OFFSET)] = pkPatterns;
    }

    /// Sets the packed floor line of the given player in the mutable array.
    ///
    /// @param pkPlayerStates the mutable packed player states array
    /// @param playerId       the identity of the player
    /// @param pkFloor        the new packed floor line value
    public static void setPkFloor(int[] pkPlayerStates, PlayerId playerId, int pkFloor) {
        pkPlayerStates[computeIndex(playerId, PK_FLOOR_OFFSET)] = pkFloor;
    }

    /// Sets the packed wall of the given player in the mutable array.
    ///
    /// @param pkPlayerStates the mutable packed player states array
    /// @param playerId       the identity of the player
    /// @param pkWall         the new packed wall value
    public static void setPkWall(int[] pkPlayerStates, PlayerId playerId, int pkWall) {
        pkPlayerStates[computeIndex(playerId, PK_WALL_OFFSET)] = pkWall;
    }

    /// Adds the given number of points to the score of the given player.
    /// The value {@code pointsToAdd} may be negative.
    ///
    /// @param pkPlayerStates the mutable packed player states array
    /// @param playerId       the identity of the player
    /// @param pointsToAdd    the number of points to add (can be negative)
    public static void addPoints(int[] pkPlayerStates, PlayerId playerId, int pointsToAdd) {
        pkPlayerStates[computeIndex(playerId, PK_POINTS_OFFSET)] += pointsToAdd;
    }
}
