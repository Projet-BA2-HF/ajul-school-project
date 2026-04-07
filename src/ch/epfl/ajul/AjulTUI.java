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

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.random.RandomGenerator;

/// Provides a textual user interface (TUI) for playing a game of Ajul
/// between human players in the terminal. The game state is printed to
/// the console and players are prompted to enter their moves using a
/// compact 3-character format: source index, color letter, destination index.
///
/// @author Hedi Fourati
/// @author Fahd Ben Ismail
public final class AjulTUI {

    private static final Scanner SCANNER = new Scanner(System.in);

    /// Entry point for the Ajul TUI application.
    ///
    /// @param args command-line arguments (unused)
    public static void main(String[] args) {
        main();
    }

    /// Prints the current game state to the console, including factory contents,
    /// center area, and all player boards with patterns, walls, and floor lines.
    ///
    /// @param gameState the current game state to display
    static void printState(ReadOnlyGameState gameState) {
        printFactoriesCompact(gameState);
        System.out.println();

        if (gameState.playerIds().size() == 2) {
            printTwoBoards(
                    gameState,
                    gameState.playerIds().get(0),
                    gameState.playerIds().get(1)
            );
        } else {
            for (PlayerId playerId : gameState.playerIds()) {
                printSingleBoard(gameState, playerId);
                System.out.println();
            }
        }

        System.out.println();
    }

    /// Prompts the given player to enter a valid move and returns it.
    /// The method displays all valid moves and repeatedly asks for input
    /// until a legal move is entered.
    ///
    /// @param playerName the name of the player to prompt
    /// @param gameState  the current game state used to determine valid moves
    /// @return the move chosen by the player
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

    /// Runs a complete game of Ajul with two human players using the
    /// textual user interface.
    static void main() {
        RandomGenerator randomGenerator = RandomGenerator.getDefault();

        Game.PlayerDescription player1 =
                new Game.PlayerDescription(
                        PlayerId.P1,
                        "Fahd",
                        Game.PlayerDescription.PlayerKind.HUMAN
                );

        Game.PlayerDescription player2 =
                new Game.PlayerDescription(
                        PlayerId.P2,
                        "Hedi",
                        Game.PlayerDescription.PlayerKind.AI
                );

        List<Game.PlayerDescription> playerInfos = List.of(player1, player2);

        Map<PlayerId, String> playerNames = Map.of(
                PlayerId.P1, "Fahd",
                PlayerId.P2, "Hedi"
        );

        Game game = new Game(playerInfos);

        MutableGameState gameState =
                new MutableGameState(ImmutableGameState.initial(game));
        gameState.fillFactories(randomGenerator);

        while (!gameState.isGameOver()) {
            printState(gameState);

            String playerName = playerNames.get(gameState.currentPlayerId());
            Move move = nextMove(playerName, gameState, randomGenerator);

            gameState.registerMove(move.packed());

            if (gameState.isRoundOver()) {
                gameState.endRound();
                if (!gameState.isGameOver()) {
                    gameState.fillFactories(randomGenerator);
                }
            }
        }

        gameState.endGame();

        printState(gameState);
        System.out.println("Partie terminée ! Scores finaux :");
        for (PlayerId playerId : gameState.playerIds()) {
            int points = PkPlayerStates.points(gameState.pkPlayerStates(), playerId);
            System.out.println(playerNames.get(playerId) + " : " + points + " pts");
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
        } else if (destinationIndex >= 1 && destinationIndex <= TileDestination.Pattern.ALL.size()) {
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

    private static void printTwoBoards(ReadOnlyGameState gameState,
                                       PlayerId leftPlayer,
                                       PlayerId rightPlayer) {
        String[] left = boardLines(gameState, leftPlayer);
        String[] right = boardLines(gameState, rightPlayer);

        int leftWidth = 27;
        for (int i = 0; i < left.length; i++) {
            System.out.println(padRight(left[i], leftWidth) + " | " + right[i]);
        }
    }

    private static void printSingleBoard(ReadOnlyGameState gameState, PlayerId playerId) {
        for (String line : boardLines(gameState, playerId)) {
            System.out.println(line);
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

    private static String formatPatternLine(int pkPatterns, TileDestination.Pattern line) {
        int size = PkPatterns.size(pkPatterns, line);
        int capacity = line.capacity();

        if (size == 0) {
            return ".".repeat(capacity);
        }

        char color = PkPatterns.color(pkPatterns, line).name().charAt(0);
        return ".".repeat(capacity - size) + String.valueOf(color).repeat(size);
    }

    private static String formatWallRow(int pkWall, TileDestination.Pattern line) {
        StringBuilder sb = new StringBuilder();

        for (int column = 0; column < 5; column++) {
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