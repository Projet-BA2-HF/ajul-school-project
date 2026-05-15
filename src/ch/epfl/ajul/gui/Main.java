package ch.epfl.ajul.gui;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.MutableGameState;
import ch.epfl.ajul.mcts.MctsPlayer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

/// Main entry point of the Ajul application.
///
/// Builds the JavaFX user interface by combining the {@link BoardUI} (background)
/// and the {@link TileOverlayUI} (tiles in the foreground), then starts the
/// virtual thread that runs the game: it fills the factories, asks each player
/// for their move, registers it, ends rounds and ends the game.
///
/// Game configuration is provided through command-line arguments: each unnamed
/// argument describes one player (prefixed by {@code _} for an AI-controlled
/// player), and the optional {@code --seed=...} named argument sets the random
/// seed used to fill the factories.
///
/// @author Hedi Fourati
/// @author Fahd Ben Ismail
public final class Main extends Application{

    private static final int MCTS_ITERATIONS = 100_000;
    private static final long PAUSE_BEFORE_END_ROUND = 1000;
    private static final long PAUSE_AFTER_END_ROUND  =  700;

    /// Launches the JavaFX application by forwarding the given arguments to
    /// {@link Application#launch(String...)}.
    ///
    /// @param args the command-line arguments
    public static void main(String[] args){
        launch(args);
    }

    /// Builds the user interface and starts the thread playing the game.
    ///
    /// @param primaryStage the primary stage of the JavaFX application
    /// @throws Exception if the application fails to start
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parameters params = getParameters();
        String seedString = params.getNamed().get("seed");
        List<String> playerArgs = params.getUnnamed();

        if (playerArgs.size() < 2 || playerArgs.size() > 4)
            throw new Error("Nombre de joueurs invalide : " + playerArgs.size());

        // Use the user-provided seed (UTF-8 bytes) if any, otherwise draw 8
        // truly random bytes to keep games unpredictable by default.
        byte[] seedBytes = (seedString != null)
                ? seedString.getBytes(StandardCharsets.UTF_8)
                : SecureRandom.getSeed(8);


        List<Game.PlayerDescription> descriptions = new ArrayList<>();
        Map<PlayerId, Player> players = new HashMap<>(); //juste players et on

        // Set of moves currently playable by a human; populated by the game
        // thread, consumed by the JavaFX thread on drag start. Synchronized
        // because both threads access it.
        Set<Move> validMoves =
                Collections.synchronizedSet(new HashSet<>());
        Set<Move> potentialMoves = new HashSet<>();
        boolean[] moveAccepted = new boolean[]{false};
        // Synchronous queue used by the game thread to wait until the JavaFX
        // thread delivers the move chosen by the human player.
        BlockingQueue<Move> humanMove = new SynchronousQueue<>();

        // Each unnamed argument describes one player, in PlayerId order.
        // A leading underscore means the player is AI-controlled (MCTS).
        for (int i = 0; i < playerArgs.size(); i++) {
            String arg = playerArgs.get(i);
            PlayerId id = PlayerId.ALL.get(i);
            if (arg.startsWith("_")) {
                descriptions.add(new Game.PlayerDescription(id,
                        arg.substring(1),
                        Game.PlayerDescription.PlayerKind.AI));
                players.put(id,
                        new MctsPlayer(RandomGeneratorFactory.getDefault(), MCTS_ITERATIONS));
            } else {

                descriptions.add(new Game.PlayerDescription(id,
                        arg,
                        Game.PlayerDescription.PlayerKind.HUMAN));
                        players.put(id, gameState -> {
                            // Human turn: publish the valid moves so the overlay
                            // recognizes the drop targets, then block until the
                            // JavaFX thread delivers the chosen move via humanMove.
                            try {
                                short[] movesBuffer = new short[Move.MAX_MOVES];
                                int count = gameState.uniqueValidMoves(movesBuffer);
                                for (int j = 0; j < count; j++)
                                    validMoves.add(Move.ofPacked(movesBuffer[j]));
                                Move move = humanMove.take();
                                validMoves.clear();
                                return move;
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        }

        Game game = new Game(descriptions);
        Tiles tiles = Tiles.create(game);

        // gameStateP is the shared, observable state of the game: the game
        // thread updates it (via Platform.runLater) and the UI components
        // listen to it to redraw themselves.
        ObjectProperty<ImmutableGameState> gameStateP =
                new SimpleObjectProperty<>(ImmutableGameState.initial(game));

        BoardUI boardUI = BoardUI.create(
                tiles.anchors(), gameStateP, potentialMoves, moveAccepted, humanMove);

        TileOverlayUI tileOverlay = TileOverlayUI.create(
                gameStateP, tiles, validMoves, potentialMoves, moveAccepted);

        StackPane sceneRoot = new StackPane(boardUI.root(), tileOverlay.root());
        sceneRoot.getStylesheets().add("ajul.css");

        primaryStage.setScene(new Scene(sceneRoot));
        primaryStage.setTitle("Ajul");
        primaryStage.setResizable(false);
        primaryStage.show();

        PointsObserver observer = new PointsObserver() {
            @Override
            public void newWallTile(PlayerId p, TileDestination.Pattern line,
                                    TileKind.Colored color, int points) {
                tileOverlay.showTilePoints(new TileLocation.OnWall(p, line, color), points);
            }

            @Override
            public void fullRow(PlayerId p, TileDestination.Pattern line, int points) {
                boardUI.showBonusPoints(p, line);
            }

            @Override
            public void fullColumn(PlayerId p, int column, int points) {
                boardUI.showBonusPoints(p, column);
            }

            @Override
            public void fullColor(PlayerId p, TileKind.Colored color, int points) {
                boardUI.showBonusPoints(p, color);
            }
        };



        publish(gameStateP, observer, seedBytes, players);
    }
        public static void publish(
                ObjectProperty<ImmutableGameState> gameStateP,
                PointsObserver observer,
                byte[] seedBytes,
                Map<PlayerId, Player> players) {

            MutableGameState gameState = new MutableGameState(
                    ImmutableGameState.initial(gameStateP.get().game()), observer);

            Runnable publish = () -> {
                ImmutableGameState snapshot = gameState.immutable();
                Platform.runLater(() -> gameStateP.set(snapshot));
            };

            RandomGenerator factoriesRandom = RandomGeneratorFactory.getDefault().create(seedBytes);

            // The virtual thread drives the whole game: it never touches the
            // scene graph directly; every visible change goes through publish().
            Thread.startVirtualThread(() -> {
                gameState.fillFactories(factoriesRandom);
                publish.run();

                while (!gameState.isGameOver()) {
                    while (!gameState.isRoundOver()) {
                        PlayerId currentId = gameState.currentPlayerId();
                        Move move;

                        move = players.get(currentId).nextMove(gameState);
                        gameState.registerMove(move.packed());
                        publish.run();
                    }

                    // Short pauses around the round transition so the user can
                    // visually parse the end-of-round movements.
                    sleep(PAUSE_BEFORE_END_ROUND);
                    gameState.endRound();
                    publish.run();

                    if (!gameState.isGameOver()) {
                        sleep(PAUSE_AFTER_END_ROUND);
                        gameState.fillFactories(factoriesRandom);
                        publish.run();
                    }
                }

                gameState.endGame();
                publish.run();
            });
        }


    // Sleeps the current thread for the given number of milliseconds.
    // InterruptedException is rethrown as an unchecked Error because the
    // game thread is never expected to be interrupted in this project.
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }
}
