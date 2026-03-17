package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.Points;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;

public final class PkPlayerStates {
    private static final int INITIAL_POINTS = 0;
    private static final int PLAYER_SIZE = 4;
    private static final int PK_PATTERNS_OFFSET = 0;
    private static final int PK_FLOOR_OFFSET = PK_PATTERNS_OFFSET + 1;
    private static final int PK_WALL_OFFSET = PK_FLOOR_OFFSET + 1;
    private static final int PK_POINTS_OFFSET = PK_WALL_OFFSET + 1;

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

    private static int computePkPlayerStatesIndex(PlayerId playerId, int offset) {
        return playerId.ordinal() * PLAYER_SIZE + offset;
    }

    public static int pkPatterns(ReadOnlyIntArray pkPlayerStates, PlayerId playerId){
        return pkPlayerStates.get(computePkPlayerStatesIndex(playerId, PK_PATTERNS_OFFSET));
    }

    public static int pkFloor(ReadOnlyIntArray pkPlayerStates, PlayerId playerId){
        return pkPlayerStates.get(computePkPlayerStatesIndex(playerId, PK_PATTERNS_OFFSET));
    }

    public static int pkWall(ReadOnlyIntArray pkPlayerStates, PlayerId playerId) {
        return pkPlayerStates.get(computePkPlayerStatesIndex(playerId, PK_WALL_OFFSET));
    }

    public static int points(ReadOnlyIntArray pkPlayerStates, PlayerId playerId) {
        return pkPlayerStates.get(computePkPlayerStatesIndex(playerId, PK_POINTS_OFFSET));
    }

    public static void setPkPatterns(int[] pkPlayerStates, PlayerId playerId, int pkPatterns) {
        pkPlayerStates[computePkPlayerStatesIndex(playerId, PK_PATTERNS_OFFSET)] = pkPatterns;
    }

    public static void setPkFloor(int[] pkPlayerStates, PlayerId playerId, int pkFloor) {
        pkPlayerStates[computePkPlayerStatesIndex(playerId, PK_FLOOR_OFFSET)] = pkFloor;
    }

    public static void setPkWall(int[] pkPlayerStates, PlayerId playerId, int pkWall) {
        pkPlayerStates[computePkPlayerStatesIndex(playerId, PK_WALL_OFFSET)] = pkWall;
    }

    public static void addPoints(int[] pkPlayerStates, PlayerId playerId, int pointsToAdd) {
        pkPlayerStates[computePkPlayerStatesIndex(playerId, PK_POINTS_OFFSET)] += pointsToAdd;
    }
}
