package ch.epfl.ajul.gui;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.*;
import ch.epfl.ajul.gamestate.packed.*;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.function.*;
import java.util.stream.*;

import static ch.epfl.ajul.gui.NodeUtils.withClasses;
import static ch.epfl.ajul.gui.NodeUtils.withId;

/// Represents the JavaFX user interface of the game board.
/// The board contains the common tile sources on the left, and one individual
/// board for each player on the right.
/// It also stores the bonus texts that can be shown at the end of the game.
///
/// @author Hedi Fourati
/// @author Fahd Ben Ismail
public final class BoardUI {
    private static final String TILE_GROUP_STYLE = "tile-group";
    private static final String TILE_SOURCE_STYLE = "tile-source";
    private static final String CURRENT_PLAYER_STYLE = "current-player";
    private static final String ACCEPTING_STYLE = "accepting";

    private static final int FACTORY_COLUMNS = 2;
    private static final int PLAYER_BOARD_COLUMNS = 2;
    private static final int WALL_START_COLUMN = 2;

    private final Map<BonusKey, Text> bonusTexts;
    private final Node root;

    private record BonusKey(PlayerId playerId, Object key) {}

    private BoardUI(Node root, Map<BonusKey, Text> bonusTexts) {
        this.root = root;
        this.bonusTexts = bonusTexts;
    }

    /// Returns the root node of the board scene graph.
    ///
    /// @return the root node of the board
    public Node root() {
        return root;
    }

    /// Shows the bonus points associated with the given player and bonus key.
    /// The bonus key can be a pattern line, a wall column index, or a tile color.
    ///
    /// @param playerId the player whose bonus points must be shown
    /// @param bonusKey the key identifying the bonus points to show
    public void showBonusPoints(PlayerId playerId, Object bonusKey) {
        bonusTexts.get(new BonusKey(playerId, bonusKey)).setVisible(true);
    }

    /// Builds the JavaFX user interface of the game board.
    ///
    /// @param anchors the map associating tile locations with their anchor nodes
    /// @param gameStateP the observable immutable game state
    /// @param potentialMoves the valid moves for the group of tiles being dragged
    /// @param moveAccepted true iff the dragged tiles were dropped on a valid destination
    /// @param moveQueue the queue receiving the move played by the human player
    /// @return the newly created board user interface
    public static BoardUI create(Map<TileLocation, Node> anchors,
                                 ObservableValue<ImmutableGameState> gameStateP,
                                 Set<Move> potentialMoves,
                                 boolean[] moveAccepted,
                                 BlockingQueue<Move> moveQueue) {
        Map<BonusKey, Text> bonusTexts = new HashMap<>();
        Game game = gameStateP.getValue().game();

        // Creates the left part of the board, containing the factories.
        GridPane sourcesArea = new GridPane();
        game.factories().stream()
                .map(factory -> withClasses(new HBox(IntStream
                    .range(0, TileSource.Factory.TILES_PER_FACTORY)
                    .mapToObj(i -> anchors.get(new TileLocation.OnSource(factory, i)))
                    .toArray(Node[]::new)),
                    TILE_GROUP_STYLE, TILE_SOURCE_STYLE))
                .gather(Gatherers.windowFixed(FACTORY_COLUMNS))
                .forEach(row -> sourcesArea.addRow(
                    sourcesArea.getRowCount(), row.toArray(Node[]::new)));

        // Creates the central area, with anchors grouped in rows of 8.
        GridPane centerArea = new GridPane();
        IntStream.range(0, game.centralAreaMaxSize())
                .mapToObj(i -> anchors.get(new TileLocation.OnSource(TileSource.CENTER_AREA, i)))
                .gather(Gatherers.windowFixed(
                        TileSource.Factory.TILES_PER_FACTORY * FACTORY_COLUMNS))
                .forEach(row -> centerArea.addRow(
                        centerArea.getRowCount(), row.toArray(Node[]::new)));

        sourcesArea.add(withClasses(centerArea, TILE_GROUP_STYLE, TILE_SOURCE_STYLE),
                0, sourcesArea.getRowCount(), FACTORY_COLUMNS, 1);
        withId(sourcesArea, "tile-sources");

        GridPane playerBoardsGrid = new GridPane();
        withId(playerBoardsGrid, "player-boards");

        for (int i = 0; i < game.playersCount(); i++) {
            Game.PlayerDescription playerDescription = game.playerDescriptions().get(i);
            PlayerId playerId = playerDescription.id();

            // Adds the drag-and-drop behavior to a pattern line or to the floor.
            BiConsumer<HBox, TileDestination> configureDestination =
                    (destinationBox, destination) -> {
                        withClasses(destinationBox, "tile-destination", TILE_GROUP_STYLE);

                        destinationBox.addEventHandler(MouseDragEvent.ANY, event -> {
                            if (event.getEventType() == MouseDragEvent.MOUSE_DRAG_EXITED) {
                                destinationBox.getStyleClass().remove(ACCEPTING_STYLE);
                                event.consume();
                                return;
                            }

                            // A player can only drop tiles on their own board.
                            if (gameStateP.getValue().currentPlayerId() != playerId)
                                return;

                            Optional<Move> moveToDestination = potentialMoves.stream()
                                    .filter(m -> m.destination().equals(destination))
                                    .findFirst();

                            if (moveToDestination.isEmpty())
                                return;

                            if (event.getEventType() == MouseDragEvent.MOUSE_DRAG_ENTERED) {
                                if (!destinationBox.getStyleClass().contains(ACCEPTING_STYLE))
                                    destinationBox.getStyleClass().add(ACCEPTING_STYLE);
                            }

                            if (event.getEventType() == MouseDragEvent.MOUSE_DRAG_RELEASED) {
                                destinationBox.getStyleClass().remove(ACCEPTING_STYLE);
                                moveAccepted[0] = true;
                                try {
                                    moveQueue.put(moveToDestination.get());
                                } catch (InterruptedException e) {
                                    throw new Error(e);
                                }
                            }

                            event.consume();
                        });
                    };

            // The text is bound to the observable game state, so the score stays updated.
            Text playerInfo = new Text();
            playerInfo.textProperty().bind(
                    Bindings.format("%s%nPoints : %d",
                            playerDescription.name(),
                            gameStateP.map(gs ->
                                    PkPlayerStates.points(gs.pkPlayerStates(),
                                    playerId))
                    ));
            StackPane.setAlignment(playerInfo, Pos.TOP_LEFT);

            GridPane patternWallGrid = new GridPane();
            patternWallGrid.getStyleClass().add("lines-and-wall");

            for (TileDestination.Pattern pattern : TileDestination.Pattern.ALL) {
                int row = pattern.index();

                // Pattern line.
                HBox patternLine = new HBox(IntStream.range(0, pattern.capacity())
                        .mapToObj(j -> anchors.get(
                                new TileLocation.OnPattern(playerId, pattern, j)))
                        .toArray(Node[]::new));
                configureDestination.accept(patternLine, pattern);
                GridPane.setHalignment(patternLine, HPos.RIGHT);
                GridPane.setFillWidth(patternLine, false);
                patternWallGrid.add(patternLine, 0, row);

                // Color bonus text, shown later by showBonusPoints.
                Text colorBonus = new Text("+" + Points.FULL_COLOR_BONUS_POINTS);
                colorBonus.setVisible(false);
                bonusTexts.put(new BonusKey(playerId, PkWall.colorAt(pattern, 0)), colorBonus);
                patternWallGrid.add(colorBonus, 1, row);

                // Wall row.
                for (int column = 0; column < PkWall.WALL_WIDTH; column++) {
                    TileKind.Colored color = PkWall.colorAt(pattern, column);
                    Node wallAnchor = anchors.get(new TileLocation.OnWall(playerId, pattern, color));
                    wallAnchor.getStyleClass().addAll("wall-background", color.name());
                    patternWallGrid.add(wallAnchor, WALL_START_COLUMN + column, row);
                }

                // Row bonus text, shown later by showBonusPoints.
                Text rowBonus = new Text("+" + Points.FULL_ROW_BONUS_POINTS);
                rowBonus.setVisible(false);
                bonusTexts.put(new BonusKey(playerId, pattern), rowBonus);
                patternWallGrid.add(rowBonus, WALL_START_COLUMN + PkWall.WALL_WIDTH, row);
            }

            // Column bonus texts, shown later by showBonusPoints.
            for (int column = 0; column < PkWall.WALL_WIDTH; column++) {
                Text columnBonus = new Text("+" + Points.FULL_COLUMN_BONUS_POINTS);
                columnBonus.setVisible(false);
                bonusTexts.put(new BonusKey(playerId, column), columnBonus);
                patternWallGrid.add(columnBonus,
                        WALL_START_COLUMN + column, TileDestination.Pattern.COUNT);
            }

            // Floor line, with the penalty text below each floor anchor.
            HBox floorArea = new HBox(IntStream.range(0, TileDestination.FLOOR.capacity())
                    .mapToObj(j -> new VBox(
                            anchors.get(new TileLocation.OnFloor(playerId, j)),
                            new Text(Integer.toString(-Points.floorPenalty(j)))))
                    .toArray(Node[]::new));
            configureDestination.accept(withClasses(floorArea, "floor"), TileDestination.FLOOR);

            StackPane playerBoard = new StackPane(new VBox(patternWallGrid, floorArea), playerInfo);
            playerBoard.getStyleClass().add("player-board");
            playerBoardsGrid.add(playerBoard,
                    i % PLAYER_BOARD_COLUMNS, i / PLAYER_BOARD_COLUMNS);

            // Keeps the current-player style synchronized with the observable game state.
            gameStateP.map(ImmutableGameState::currentPlayerId).subscribe(newId -> {
                if (playerId == newId) playerBoard.getStyleClass().add(CURRENT_PLAYER_STYLE);
                else playerBoard.getStyleClass().remove(CURRENT_PLAYER_STYLE);
            });
        }

        return new BoardUI(new HBox(sourcesArea, playerBoardsGrid), bonusTexts);
    }
}