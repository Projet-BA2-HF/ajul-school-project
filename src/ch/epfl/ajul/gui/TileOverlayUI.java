package ch.epfl.ajul.gui;

import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

//VERIFIER AVEC ADAM ENCORE

/// Manages the graphical representation of the tiles overlaying the game board,
/// including drag-and-drop interaction by the user and animated tile movements
/// triggered by changes of the game state.
///
/// @author Hedi Fourati
/// @author Fahd Ben Ismail
public final class TileOverlayUI {

    // Coordinates assigned to off-board tile locations.
    private static final double OFF_BOARD_X = -30;
    private static final double OFF_BOARD_Y = -30;

    // View orders defining display layers (smaller value = closer to front).
    private static final int STATIC_LAYER = 0;
    private static final int MOVING_LAYER = -1;
    private static final int POINTS_LAYER = -2;

    // Duration of the bounce-back animation when a drag is cancelled.
    private static final Duration CANCEL_DURATION = Duration.millis(125);

    private final Pane root;
    private final Tiles tiles;

    private TileOverlayUI(Pane root,
                          Tiles tiles) {
        this.root = root;
        this.tiles = tiles;
    }

    /// Returns the root node of the scene graph managed by this instance.
    ///
    /// @return the root pane containing the tiles and the tile-point texts
    public Pane root() {
        return root;
    }

    /// Creates a new instance of {@code TileOverlayUI} managing the tiles given as
    /// argument over the game board, and reacting to changes of the given observable
    /// game state by animating the tiles accordingly.
    ///
    /// @param gameStateO     the observable game state to watch
    /// @param tiles          the anchors and tiles of the game
    /// @param validMoves     the (read-only) set of moves a human player could play
    /// @param potentialMoves the set, populated when the user starts a drag, of valid
    ///                       moves matching the source and color of the dragged tiles
    /// @param moveAccepted   single-element array set to {@code true} by destinations
    ///                       when a drag ends over a destination able to accept the tiles
    /// @return the newly created instance
    public static TileOverlayUI create(ObservableValue<ImmutableGameState> gameStateO,
                                       Tiles tiles,
                                       Set<Move> validMoves,
                                       Set<Move> potentialMoves,
                                       boolean[] moveAccepted) {
        Pane root = new Pane();

        // Add every tile to the panel, initially placed off-board.
        tiles.tiles().forEach((kind, nodes) -> {
            root.getChildren().addAll(nodes);
            for (int i = 0; i < nodes.size(); i++) {
                Node node = nodes.get(i);
                Tiles.setLocation(node, new TileLocation.OffBoard(kind, i));
                node.relocate(OFF_BOARD_X, OFF_BOARD_Y);
            }
        });

        for (TileKind.Colored color : TileKind.Colored.values())
            for (Node sourceTile : tiles.tiles().get(color))
                installDragHandlers(root, tiles, sourceTile, color,
                        validMoves, potentialMoves, moveAccepted);

        // The subscription is delayed via runLater so anchors have their final
        // position when subscribe fires its first call with the initial state.
        TileOverlayUI tileOverlayUI = new TileOverlayUI(root, tiles);
        Platform.runLater(() -> gameStateO.subscribe(state ->
                TileAnimator.animateTiles(tileOverlayUI::locationToPoint, tiles.tiles(), state).play()));

        return tileOverlayUI;
    }

    /// Displays the given number of points centered on the wall tile at the given
    /// location, in a layer above the tiles themselves.
    ///
    /// @param location the wall location whose tile carries the points
    /// @param points   the number of points to display
    public void showTilePoints(TileLocation.OnWall location, int points) {
        Text pointsText = new Text("%+d".formatted(points));
        pointsText.setViewOrder(POINTS_LAYER);

        Point2D tilePos = locationToPoint(location);
        Bounds textBounds = pointsText.getBoundsInLocal();
        double x = tilePos.getX() + (Tiles.TILE_WIDTH  - textBounds.getWidth())  / 2;
        double y = tilePos.getY() + (Tiles.TILE_HEIGHT - textBounds.getHeight()) / 2;
        pointsText.relocate(x, y);

        // Scene graph modifications must run on the JavaFX thread.
        Platform.runLater(() -> root.getChildren().add(pointsText));
    }

    // Installs the press/drag handlers that allow the user to move the given
    // colored tile and its source-mates from a source to a destination.
    private static void installDragHandlers(Pane root,
                                            Tiles tiles,
                                            Node sourceTile,
                                            TileKind.Colored color,
                                            Set<Move> validMoves,
                                            Set<Move> potentialMoves,
                                            boolean[] moveAccepted) {
        sourceTile.setOnMousePressed(e -> e.setDragDetect(true));
        sourceTile.setOnDragDetected(dragStartEvent -> {
            if (!(Tiles.location(sourceTile) instanceof TileLocation.OnSource sourceLoc)
                    || validMoves.isEmpty())
                return;
            TileSource clickedSource = sourceLoc.source();

            potentialMoves.clear();
            for (Move move : validMoves)
                if (move.source() == clickedSource && move.tileColor() == color)
                    potentialMoves.add(move);

            sourceTile.startFullDrag();
            root.setMouseTransparent(true);

            List<Node> tilesToMove = new ArrayList<>();
            for (Node peerTile : tiles.tiles().get(color))
                if (Tiles.location(peerTile) instanceof TileLocation.OnSource peerLoc
                        && peerLoc.source() == clickedSource)
                    tilesToMove.add(peerTile);

            for (Node movingTile : tilesToMove) movingTile.setViewOrder(MOVING_LAYER);

            double initialSceneX = dragStartEvent.getSceneX();
            double initialSceneY = dragStartEvent.getSceneY();

            sourceTile.setOnMouseDragged(draggedEvent -> {
                double offsetX = draggedEvent.getSceneX() - initialSceneX;
                double offsetY = draggedEvent.getSceneY() - initialSceneY;
                for (Node movingTile : tilesToMove) {
                    movingTile.setTranslateX(offsetX);
                    movingTile.setTranslateY(offsetY);
                }
            });

            sourceTile.setOnMouseDragDone(dragDoneEvent -> {
                sourceTile.setOnMouseDragged(null);
                sourceTile.setOnMouseDragDone(null);
                root.setMouseTransparent(false);

                if (moveAccepted[0]) {
                    // Freeze the visual position by absorbing the drag offset
                    // into the layout, so the next animation starts from there.
                    for (Node movingTile : tilesToMove) {
                        movingTile.setLayoutX(movingTile.getLayoutX() +
                                movingTile.getTranslateX());
                        movingTile.setLayoutY(movingTile.getLayoutY() +
                                movingTile.getTranslateY());
                        movingTile.setTranslateX(0);
                        movingTile.setTranslateY(0);
                        movingTile.setViewOrder(STATIC_LAYER);
                    }
                    moveAccepted[0] = false;
                } else {
                    ParallelTransition cancelAnim = new ParallelTransition();
                    for (Node movingTile : tilesToMove) {
                        TranslateTransition tt = new TranslateTransition(CANCEL_DURATION,
                                movingTile);
                        tt.setToX(0);
                        tt.setToY(0);
                        cancelAnim.getChildren().add(tt);
                    }
                    cancelAnim.setOnFinished(e -> {
                        for (Node movingTile : tilesToMove) movingTile.setViewOrder(STATIC_LAYER);
                    });
                    cancelAnim.play();
                }
            });
        });
    }

    private Point2D locationToPoint(TileLocation tileLocation){
        return tileLocation instanceof TileLocation.OffBoard
                ? new Point2D(OFF_BOARD_X, OFF_BOARD_Y)
                : root.sceneToLocal(tiles.anchors().get(tileLocation).localToScene(0, 0));
    }
}