package ch.epfl.ajul;

import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.MutableGameState;
import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.PkFloor;
import ch.epfl.ajul.gamestate.packed.PkPatterns;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkTileSet;
import ch.epfl.ajul.gamestate.packed.PkWall;
import ch.epfl.ajul.mcts.HeuristicMoveSelector;
import ch.epfl.ajul.mcts.MctsPlayer;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

/// Provides a textual user interface (TUI) for testing games of Ajul.
public final class AjulTUI {
    private static final Scanner SCANNER = new Scanner(System.in);

    private static final int DEFAULT_MATCH_COUNT = 3;
    private static final int DEFAULT_MCTS_ITERATIONS = 100_000;
    private static final boolean DEFAULT_PRINT_EACH_TURN = false;
    private static final int BOARD_WIDTH = 27;

    private static final Map<PlayerId, String> AI_TYPES = Map.of(
            PlayerId.P1, "MCTS",
            PlayerId.P2, "Heuristic",
            PlayerId.P3, "MCTS",
            PlayerId.P4, "MCTS"
    );

    public static void main(String[] args) {
        int matchCount = args.length >= 1
                ? Integer.parseInt(args[0])
                : DEFAULT_MATCH_COUNT;

        int mctsIterations = args.length >= 2
                ? Integer.parseInt(args[1])
                : DEFAULT_MCTS_ITERATIONS;

        boolean printEachTurn = args.length >= 3
                && Boolean.parseBoolean(args[2]);

        runAiMatches(matchCount, mctsIterations, printEachTurn);
    }

    static void main() {
        runAiMatches(
                DEFAULT_MATCH_COUNT,
                DEFAULT_MCTS_ITERATIONS,
                DEFAULT_PRINT_EACH_TURN
        );
    }

    static void printState(ReadOnlyGameState gameState) {
        printFactoriesCompact(gameState);
        System.out.println();
        printBoards(gameState);
        System.out.println();
    }

    static Move queryNextMove(String playerName,
                              ReadOnlyGameState gameState) {
        short[] validMoves = new short[Move.MAX_MOVES];
        int moveCount = gameState.uniqueValidMoves(validMoves);

        System.out.print("Coups possibles : ");
        for (int i = 0; i < moveCount; i++) {
            if (i > 0) System.out.print(", ");
            System.out.print(formatMove(Move.ofPacked(validMoves[i])));
        }
        System.out.println();

        while (true) {
            System.out.print("Quel coup désirez-vous jouer, " + playerName + " ? ");
            String input = SCANNER.nextLine().trim().toUpperCase();

            try {
                Move move = parseMove(input, gameState);
                if (isLegalMove(move, validMoves, moveCount)) {
                    return move;
                }
                System.out.println("Coup illégal.");
            } catch (IllegalArgumentException e) {
                System.out.println("Entrée invalide. Exemple : 4B2");
            }
        }
    }

    private static void runAiMatches(int matchCount,
                                     int mctsIterations,
                                     boolean printEachTurn) {
        if (matchCount <= 0 || mctsIterations <= 0) {
            throw new IllegalArgumentException();
        }

        RandomGeneratorFactory<RandomGenerator> randomFactory =
                RandomGeneratorFactory.getDefault();

        List<Game.PlayerDescription> playerInfos = fourAiPlayerDescriptions();
        Game game = new Game(playerInfos);
        Map<PlayerId, String> playerNames = playerNames(game);

        System.out.println(
                "Configuration : "
                        + matchCount + " parties, "
                        + mctsIterations + " itérations MCTS par coup MCTS."
        );
        System.out.println();

        int playerCount = game.playersCount();

        int[] totalPoints = new int[playerCount];
        int[] minPoints = new int[playerCount];
        int[] maxPoints = new int[playerCount];
        int[] victoryCounts = new int[playerCount];
        int[] rankSums = new int[playerCount];
        int[] ranks = new int[playerCount];

        long[] totalMoveTimes = new long[playerCount];
        int[] moveCounts = new int[playerCount];

        Arrays.fill(minPoints, Integer.MAX_VALUE);

        for (int matchIndex = 1; matchIndex <= matchCount; matchIndex++) {
            RandomGenerator gameRandom = randomFactory.create(matchIndex);
            Map<PlayerId, Player> players =
                    aiPlayers(randomFactory, mctsIterations, matchIndex);

            MutableGameState gameState =
                    new MutableGameState(ImmutableGameState.initial(game));
            gameState.fillFactories(gameRandom);

            while (!gameState.isGameOver()) {
                if (printEachTurn) {
                    printState(gameState);
                }

                PlayerId currentPlayerId = gameState.currentPlayerId();
                Player currentPlayer = players.get(currentPlayerId);

                long moveStartTime = System.nanoTime();
                Move move = currentPlayer.nextMove(gameState);
                long moveDuration = System.nanoTime() - moveStartTime;

                int currentPlayerIndex = currentPlayerId.ordinal();
                totalMoveTimes[currentPlayerIndex] += moveDuration;
                moveCounts[currentPlayerIndex] += 1;

                if (printEachTurn) {
                    System.out.println(
                            playerNames.get(currentPlayerId)
                                    + " (" + aiLabel(currentPlayerId) + ") joue : "
                                    + formatMove(move)
                                    + " en " + formatDuration(moveDuration)
                    );
                }

                gameState.registerMove(move.packed());

                if (gameState.isRoundOver()) {
                    gameState.endRound();
                    if (!gameState.isGameOver()) {
                        gameState.fillFactories(gameRandom);
                    }
                }
            }

            gameState.endGame();
            RankComputer.playersRank(gameState, ranks);

            updateStatistics(
                    gameState,
                    ranks,
                    totalPoints,
                    minPoints,
                    maxPoints,
                    victoryCounts,
                    rankSums
            );

            if (printEachTurn) {
                printState(gameState);
            }

            printCompactFinalScores(matchIndex, gameState, playerNames, ranks);
        }

        printStatistics(
                game,
                playerNames,
                matchCount,
                totalPoints,
                minPoints,
                maxPoints,
                victoryCounts,
                rankSums,
                totalMoveTimes,
                moveCounts
        );
    }

    private static List<Game.PlayerDescription> fourAiPlayerDescriptions() {
        return List.of(
                new Game.PlayerDescription(
                        PlayerId.P1,
                        "Yari",
                        Game.PlayerDescription.PlayerKind.AI
                ),
                new Game.PlayerDescription(
                        PlayerId.P2,
                        "Lenny",
                        Game.PlayerDescription.PlayerKind.AI
                ),
                new Game.PlayerDescription(
                        PlayerId.P3,
                        "Adrien",
                        Game.PlayerDescription.PlayerKind.AI
                ),
                new Game.PlayerDescription(
                        PlayerId.P4,
                        "Schinz",
                        Game.PlayerDescription.PlayerKind.AI
                )
        );
    }

    private static Map<PlayerId, Player> aiPlayers(
            RandomGeneratorFactory<RandomGenerator> randomFactory,
            int mctsIterations,
            int matchIndex
    ) {
        Map<PlayerId, Player> players = new EnumMap<>(PlayerId.class);

        players.put(PlayerId.P1, new MctsPlayer(randomFactory, mctsIterations));
        players.put(PlayerId.P2, new HeuristicPlayer(randomFactory.create(10_000L + matchIndex)));
        players.put(PlayerId.P3, new MctsPlayer(randomFactory, mctsIterations));
        players.put(PlayerId.P4, new MctsPlayer(randomFactory, mctsIterations));

        return players;
    }

    private static Map<PlayerId, String> playerNames(Game game) {
        Map<PlayerId, String> playerNames = new EnumMap<>(PlayerId.class);

        for (Game.PlayerDescription playerDescription : game.playerDescriptions()) {
            playerNames.put(playerDescription.id(), playerDescription.name());
        }

        return playerNames;
    }

    private static String aiLabel(PlayerId playerId) {
        return AI_TYPES.getOrDefault(playerId, "IA");
    }

    private static void updateStatistics(ReadOnlyGameState gameState,
                                         int[] ranks,
                                         int[] totalPoints,
                                         int[] minPoints,
                                         int[] maxPoints,
                                         int[] victoryCounts,
                                         int[] rankSums) {
        for (PlayerId playerId : gameState.playerIds()) {
            int playerIndex = playerId.ordinal();
            int points = PkPlayerStates.points(gameState.pkPlayerStates(), playerId);

            totalPoints[playerIndex] += points;
            minPoints[playerIndex] = Math.min(minPoints[playerIndex], points);
            maxPoints[playerIndex] = Math.max(maxPoints[playerIndex], points);
            rankSums[playerIndex] += ranks[playerIndex];

            if (ranks[playerIndex] == 0) {
                victoryCounts[playerIndex] += 1;
            }
        }
    }

    private static void printCompactFinalScores(int matchIndex,
                                                ReadOnlyGameState gameState,
                                                Map<PlayerId, String> playerNames,
                                                int[] ranks) {
        System.out.print("Partie " + matchIndex + " : ");

        boolean first = true;
        for (PlayerId playerId : gameState.playerIds()) {
            if (!first) System.out.print(" | ");
            first = false;

            int points = PkPlayerStates.points(gameState.pkPlayerStates(), playerId);
            String winnerText = ranks[playerId.ordinal()] == 0 ? "*" : "";

            System.out.print(playerNames.get(playerId) + " " + points + " pts" + winnerText);
        }

        System.out.println();
    }

    private static void printStatistics(Game game,
                                        Map<PlayerId, String> playerNames,
                                        int matchCount,
                                        int[] totalPoints,
                                        int[] minPoints,
                                        int[] maxPoints,
                                        int[] victoryCounts,
                                        int[] rankSums,
                                        long[] totalMoveTimes,
                                        int[] moveCounts) {
        System.out.println();
        System.out.println("Résumé sur " + matchCount + " parties :");

        for (PlayerId playerId : game.playerIds()) {
            int playerIndex = playerId.ordinal();

            double averagePoints = (double) totalPoints[playerIndex] / matchCount;
            double averageRank = (double) rankSums[playerIndex] / matchCount;

            String averageMoveTime = moveCounts[playerIndex] == 0
                    ? "-"
                    : formatDuration((double) totalMoveTimes[playerIndex] / moveCounts[playerIndex]);

            System.out.printf(
                    Locale.ROOT,
                    "%-8s (%-9s) : moyenne %.1f pts, min %d, max %d, victoires %d, rang moyen %.2f, temps moyen/coup %s%n",
                    playerNames.get(playerId),
                    aiLabel(playerId),
                    averagePoints,
                    minPoints[playerIndex],
                    maxPoints[playerIndex],
                    victoryCounts[playerIndex],
                    averageRank,
                    averageMoveTime
            );
        }
    }

    private static String formatDuration(long durationInNanoseconds) {
        return formatDuration((double) durationInNanoseconds);
    }

    private static String formatDuration(double durationInNanoseconds) {
        double milliseconds = durationInNanoseconds / 1_000_000.0;

        if (milliseconds < 1_000.0) {
            return String.format(Locale.ROOT, "%.1f ms", milliseconds);
        }

        return String.format(Locale.ROOT, "%.2f s", milliseconds / 1_000.0);
    }

    private record HeuristicPlayer(RandomGenerator randomGenerator) implements Player {
        @Override
        public Move nextMove(ReadOnlyGameState gameState) {
            short[] validMoves = new short[Move.MAX_MOVES];
            int moveCount = gameState.uniqueValidMoves(validMoves);

            int selectedMoveIndex = HeuristicMoveSelector.selectMove(
                    randomGenerator,
                    gameState,
                    validMoves,
                    moveCount
            );

            return Move.ofPacked(validMoves[selectedMoveIndex]);
        }
    }

    private static Move parseMove(String input, ReadOnlyGameState gameState) {
        if (input.length() != 3) {
            throw new IllegalArgumentException();
        }

        int sourceIndex = parseDigit(input.charAt(0));
        TileKind.Colored color = parseColor(input.charAt(1));
        int destinationIndex = parseDigit(input.charAt(2));

        if (sourceIndex < 0 || sourceIndex >= gameState.pkTileSources().size()) {
            throw new IllegalArgumentException();
        }

        TileSource source = TileSource.ALL.get(sourceIndex);

        TileDestination destination;
        if (destinationIndex == 0) {
            destination = TileDestination.FLOOR;
        } else if (destinationIndex >= 1
                && destinationIndex <= TileDestination.Pattern.ALL.size()) {
            destination = TileDestination.Pattern.ALL.get(destinationIndex - 1);
        } else {
            throw new IllegalArgumentException();
        }

        return new Move(source, color, destination);
    }

    private static int parseDigit(char c) {
        if (!Character.isDigit(c)) {
            throw new IllegalArgumentException();
        }
        return c - '0';
    }

    private static TileKind.Colored parseColor(char c) {
        return switch (c) {
            case 'A' -> TileKind.Colored.A;
            case 'B' -> TileKind.Colored.B;
            case 'C' -> TileKind.Colored.C;
            case 'D' -> TileKind.Colored.D;
            case 'E' -> TileKind.Colored.E;
            default -> throw new IllegalArgumentException();
        };
    }

    private static boolean isLegalMove(Move move, short[] validMoves, int moveCount) {
        short packed = move.packed();

        for (int i = 0; i < moveCount; i++) {
            if (validMoves[i] == packed) {
                return true;
            }
        }

        return false;
    }

    private static String formatMove(Move move) {
        int source = move.source().index();
        char color = move.tileColor().name().charAt(0);

        int destination =
                (move.destination() == TileDestination.FLOOR)
                        ? 0
                        : move.destination().index() + 1;

        return "" + source + color + destination;
    }

    private static void printFactoriesCompact(ReadOnlyGameState gameState) {
        List<TileSource.Factory> factories = gameState.game().factories();

        for (int start = 0; start < factories.size(); start += 3) {
            String prefix = (start == 0) ? "Fabriques : " : "            ";
            System.out.print(prefix);

            int end = Math.min(start + 3, factories.size());
            for (int i = start; i < end; i++) {
                TileSource.Factory factory = factories.get(i);
                String text = "[" + factory.index() + "] "
                        + formatTileSetCompact(gameState.pkTileSources().get(factory.index()));
                System.out.print(padRight(text, 16));
            }

            System.out.println();
        }

        String center = formatTileSetCompact(
                gameState.pkTileSources().get(TileSource.CENTER_AREA.index()));
        System.out.println("   Centre : [0] " + center);
    }

    private static void printBoards(ReadOnlyGameState gameState) {
        List<PlayerId> playerIds = gameState.playerIds();
        String[][] boards = new String[playerIds.size()][];

        for (int i = 0; i < playerIds.size(); i++) {
            boards[i] = boardLines(gameState, playerIds.get(i));
        }

        for (int line = 0; line < boards[0].length; line++) {
            for (int player = 0; player < boards.length; player++) {
                if (player > 0) System.out.print(" | ");
                System.out.print(padRight(boards[player][line], BOARD_WIDTH));
            }

            System.out.println();
        }
    }

    private static String[] boardLines(ReadOnlyGameState gameState, PlayerId playerId) {
        int pkPatterns = PkPlayerStates.pkPatterns(gameState.pkPlayerStates(), playerId);
        int pkWall = PkPlayerStates.pkWall(gameState.pkPlayerStates(), playerId);
        int pkFloor = PkPlayerStates.pkFloor(gameState.pkPlayerStates(), playerId);
        int points = PkPlayerStates.points(gameState.pkPlayerStates(), playerId);

        String name = gameState.game().playerDescriptions().get(playerId.ordinal()).name();

        String[] lines = new String[8];

        lines[0] = padRight(name, 10)
                + formatPatternLine(pkPatterns, TileDestination.Pattern.PATTERN_1)
                + " "
                + formatWallRow(pkWall, TileDestination.Pattern.PATTERN_1);

        lines[1] = padRight(points + " pts", 10)
                + formatPatternLine(pkPatterns, TileDestination.Pattern.PATTERN_2)
                + " "
                + formatWallRow(pkWall, TileDestination.Pattern.PATTERN_2);

        lines[2] = padRight("", 10)
                + formatPatternLine(pkPatterns, TileDestination.Pattern.PATTERN_3)
                + " "
                + formatWallRow(pkWall, TileDestination.Pattern.PATTERN_3);

        lines[3] = padRight("", 10)
                + formatPatternLine(pkPatterns, TileDestination.Pattern.PATTERN_4)
                + " "
                + formatWallRow(pkWall, TileDestination.Pattern.PATTERN_4);

        lines[4] = padRight("", 10)
                + formatPatternLine(pkPatterns, TileDestination.Pattern.PATTERN_5)
                + " "
                + formatWallRow(pkWall, TileDestination.Pattern.PATTERN_5);

        lines[5] = padRight("", 10) + formatFloorTiles(pkFloor);
        lines[6] = "Pté " + formatFloorPenalties();
        lines[7] = "";

        return lines;
    }

    private static String formatTileSetCompact(int pkTileSet) {
        StringBuilder sb = new StringBuilder();

        for (TileKind.Colored color : TileKind.Colored.ALL) {
            int count = PkTileSet.countOf(pkTileSet, color);

            if (count > 0) {
                if (!sb.isEmpty()) sb.append(' ');
                sb.append(String.valueOf(color.name().charAt(0)).repeat(count));
            }
        }

        if (PkTileSet.countOf(pkTileSet, TileKind.FIRST_PLAYER_MARKER) > 0) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append("1");
        }

        return sb.toString();
    }

    private static String formatPatternLine(int pkPatterns,
                                            TileDestination.Pattern line) {
        int size = PkPatterns.size(pkPatterns, line);
        int capacity = line.capacity();

        if (size == 0) {
            return ".".repeat(capacity);
        }

        char color = PkPatterns.color(pkPatterns, line).name().charAt(0);
        return ".".repeat(capacity - size) + String.valueOf(color).repeat(size);
    }

    private static String formatWallRow(int pkWall,
                                        TileDestination.Pattern line) {
        StringBuilder sb = new StringBuilder();

        for (int column = 0; column < PkWall.WALL_WIDTH; column++) {
            TileKind.Colored color = PkWall.colorAt(line, column);
            char c = Character.toLowerCase(color.name().charAt(0));

            if (PkWall.hasTileAt(pkWall, line, color)) {
                c = Character.toUpperCase(c);
            }

            sb.append(c);
        }

        return sb.toString();
    }

    private static String formatFloorTiles(int pkFloor) {
        int size = PkFloor.size(pkFloor);

        if (size == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < size; i++) {
            if (i > 0) sb.append(' ');
            sb.append(tileToChar(PkFloor.tileAt(pkFloor, i)));
        }

        return sb.toString();
    }

    private static String formatFloorPenalties() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < TileDestination.FLOOR.capacity(); i++) {
            if (i > 0) sb.append(' ');
            sb.append(Points.floorPenalty(i));
        }

        return sb.toString();
    }

    private static char tileToChar(TileKind tileKind) {
        if (tileKind == TileKind.FIRST_PLAYER_MARKER) {
            return '1';
        }

        return tileKind.toString().charAt(0);
    }

    private static String padRight(String s, int width) {
        return s + " ".repeat(Math.max(0, width - s.length()));
    }

    static Move nextMove(String playerName,
                         ReadOnlyGameState gameState,
                         RandomGenerator randomGenerator) {
        Game.PlayerDescription.PlayerKind currentPlayerKind =
                gameState.game()
                        .playerDescriptions()
                        .get(gameState.currentPlayerId().ordinal())
                        .kind();

        if (currentPlayerKind == Game.PlayerDescription.PlayerKind.AI) {
            return queryAiMove(playerName, gameState, randomGenerator);
        }

        return queryNextMove(playerName, gameState);
    }

    private static Move queryAiMove(String playerName,
                                    ReadOnlyGameState gameState,
                                    RandomGenerator randomGenerator) {
        short[] validMoves = new short[Move.MAX_MOVES];
        int moveCount = gameState.uniqueValidMoves(validMoves);

        int selectedMoveIndex = HeuristicMoveSelector.selectMove(
                randomGenerator,
                gameState,
                validMoves,
                moveCount
        );

        Move selectedMove = Move.ofPacked(validMoves[selectedMoveIndex]);

        System.out.println(playerName + " (IA) joue : " + formatMove(selectedMove));
        return selectedMove;
    }
}