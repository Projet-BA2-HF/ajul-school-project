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

public final class Main extends Application{

    private static final int MCTS_ITERATIONS = 100_000;
    private static final long PAUSE_BEFORE_END_ROUND = 1000;
    private static final long PAUSE_AFTER_END_ROUND  =  700;

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parameters params = getParameters();
        String seedString = params.getNamed().get("seed");  // null si absent
        List<String> playerArgs = params.getUnnamed();

        if (playerArgs.size() < 2 || playerArgs.size() > 4)
            throw new Error("Nombre de joueurs invalide : " + playerArgs.size());

        byte[] seedBytes = (seedString != null)
                ? seedString.getBytes(StandardCharsets.UTF_8)
                : SecureRandom.getSeed(8);

        RandomGeneratorFactory<RandomGenerator> factory =
                RandomGeneratorFactory.of("L64X128MixRandom");
        RandomGenerator factoriesRandom = factory.create(seedBytes);

        List<Game.PlayerDescription> descriptions = new ArrayList<>();
        Map<PlayerId, Player> aiPlayers = new HashMap<>();

        for (int i = 0; i < playerArgs.size(); i++) {
            String arg = playerArgs.get(i);
            PlayerId id = PlayerId.ALL.get(i);
            if (arg.startsWith("_")) {
                descriptions.add(new Game.PlayerDescription(id,
                        arg.substring(1),
                        Game.PlayerDescription.PlayerKind.AI));
                aiPlayers.put(id, new MctsPlayer(factory, MCTS_ITERATIONS));
            } else {
                descriptions.add(new Game.PlayerDescription(id,
                        arg,
                        Game.PlayerDescription.PlayerKind.HUMAN));
            }
        }
        Game game = new Game(descriptions);

        ObjectProperty<ImmutableGameState> gameStateP =
                new SimpleObjectProperty<>(ImmutableGameState.initial(game));
        Set<Move> validMoves =
                Collections.synchronizedSet(new HashSet<>());
        Set<Move> potentialMoves = new HashSet<>();
        boolean[] moveAccepted = new boolean[]{false};
        BlockingQueue<Move> humanMove = new SynchronousQueue<>();

        Tiles tiles = Tiles.create(game);

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

        // ---------------------- FILS --------------------

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

        MutableGameState gameState = new MutableGameState(
                ImmutableGameState.initial(game), observer);

        // Helper de publication vers le fil JavaFX
        Runnable publish = () -> {
            ImmutableGameState snapshot = gameState.immutable();
            Platform.runLater(() -> gameStateP.set(snapshot));
        };

        Thread.startVirtualThread(() -> {
            short[] movesBuffer = new short[Move.MAX_MOVES];

            gameState.fillFactories(factoriesRandom);
            publish.run();

            while (!gameState.isGameOver()) {
                while (!gameState.isRoundOver()) {
                    PlayerId currentId = gameState.currentPlayerId();
                    Move move;

                    if (aiPlayers.containsKey(currentId)) {
                        move = aiPlayers.get(currentId).nextMove(gameState);
                    } else {
                        int count = gameState.uniqueValidMoves(movesBuffer);
                        for (int i = 0; i < count; i++)
                            validMoves.add(Move.ofPacked(movesBuffer[i]));
                        move = take(humanMove);
                        validMoves.clear();
                    }

                    gameState.registerMove(move.packed());
                    publish.run();
                }

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

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }

    private static <T> T take(BlockingQueue<T> queue) {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }

}
