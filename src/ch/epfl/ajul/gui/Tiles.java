package ch.epfl.ajul.gui;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static ch.epfl.ajul.gui.NodeUtils.withClasses;
import static ch.epfl.ajul.gui.NodeUtils.withId;

/// Holds the JavaFX nodes representing all anchors and tiles of a game of Ajul.
/// Anchors are fixed placeholders that mark every on-board location where a tile
/// may appear, while tiles are the movable nodes displayed on top of those anchors
/// during the game.
///
/// @param anchors a map from each on-board location to its anchor node
/// @param tiles   a map from each tile kind to the list of nodes representing
///                the tiles of that kind
///
/// @author Hedi Fourati
/// @author Fahd Ben Ismail
public record Tiles(Map<TileLocation, Node> anchors,
                    Map<TileKind, List<Node>> tiles) {

    /// The width, in JavaFX units, of the graphical representation of a tile.
    public static final double TILE_WIDTH = 30;

    /// The height, in JavaFX units, of the graphical representation of a tile.
    public static final double TILE_HEIGHT = 30;

    public Tiles {
        anchors = Map.copyOf(anchors);
        tiles = Map.copyOf(tiles);
    }

    /// Builds a new {@code Tiles} instance containing one anchor per on-board
    /// location of the given game, and all tiles of the game grouped by kind.
    ///
    /// @param game the game configuration
    /// @return a new {@code Tiles} instance for the given game
    /// @throws NullPointerException if {@code game} is {@code null}
    public static Tiles create(Game game) {
        Objects.requireNonNull(game);

        Map<TileLocation, Node> anchors = allLocations(game)
                .collect(Collectors.toUnmodifiableMap(
                        Function.identity(),
                        loc -> tileRectangle("anchor")));

        Map<TileKind, List<Node>> tiles = TileKind.ALL.stream()
                .collect(Collectors.toUnmodifiableMap(
                        Function.identity(),
                        kind -> IntStream.range(0, kind.tilesCount())
                                .mapToObj(i -> createTile(kind))
                                .toList()));

        return new Tiles(anchors, tiles);
    }

    /// Returns the location associated with the given node, or {@code null} if
    /// no location has been associated with it yet.
    ///
    /// @param node the node whose location is requested
    /// @return the location associated with {@code node}, or {@code null}
    public static TileLocation location(Node node) {
        return (TileLocation) node.getUserData();
    }

    /// Associates the given location with the given node. Any previously
    /// associated location is replaced.
    ///
    /// @param node     the node to update
    /// @param location the new location to associate with the node
    public static void setLocation(Node node, TileLocation location) {
        node.setUserData(location);
    }

    // Stream of all on-board locations: sources plus per-player boards.
    private static Stream<TileLocation> allLocations(Game game) {
        return Stream.concat(
                sourceLocations(game),
                game.playerIds().stream().flatMap(Tiles::playerLocations));
    }

    // Stream of source locations: factories (4 slots) and the center area.
    private static Stream<TileLocation> sourceLocations(Game game) {
        return game.tileSources().stream().flatMap(source -> {
            int capacity = (source instanceof TileSource.Factory)
                    ? TileSource.Factory.TILES_PER_FACTORY
                    : game.centralAreaMaxSize();
            return IntStream.range(0, capacity)
                    .mapToObj(i -> new TileLocation.OnSource(source, i));
        });
    }

    // Stream of locations on a player's board: pattern lines, wall, and floor line.
    private static Stream<TileLocation> playerLocations(PlayerId p) {
        Stream<TileLocation> patterns = TileDestination.Pattern.ALL.stream()
                .flatMap(line -> IntStream.range(0, line.capacity())
                        .mapToObj(i -> new TileLocation.OnPattern(p, line, i)));

        Stream<TileLocation> walls = TileDestination.Pattern.ALL.stream()
                .flatMap(line -> TileKind.Colored.ALL.stream()
                        .map(color -> new TileLocation.OnWall(p, line, color)));

        Stream<TileLocation> floors = IntStream.range(0, TileDestination.FLOOR.capacity())
                .mapToObj(i -> new TileLocation.OnFloor(p, i));

        return Stream.of(patterns, walls, floors).flatMap(s -> s);
    }

    // Builds a tile node: a plain rectangle for colored tiles, or a stack of a
    // rectangle and a "1" text node for the first-player marker.
    private static Node createTile(TileKind kind) {
        if (kind instanceof TileKind.Colored color)
            return tileRectangle(color.name());

        StackPane pane = new StackPane(tileRectangle(), new Text("1"));
        return withId(pane, "first-player-marker");
    }

    // Builds a rectangle of the standard tile size, with the "tile" style class
    // plus any extra classes given in argument.
    private static Rectangle tileRectangle(String... extraStyleClasses) {
        Rectangle rect = withClasses(new Rectangle(TILE_WIDTH, TILE_HEIGHT), "tile");
        return withClasses(rect, extraStyleClasses);
    }
}