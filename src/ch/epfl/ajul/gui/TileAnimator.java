package ch.epfl.ajul.gui;

import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileKind.Colored;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.*;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;
import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.util.Duration;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/// Builds the animation that moves tiles on the board so that they reflect the
/// current state of the game, in a way that respects the game rules and is
/// visually pleasant.
///
/// For each tile kind, the class computes the supply (tile locations currently
/// occupied that should not be) and the demand (tile locations that should be
/// occupied but are not), then pairs them according to the matching algorithm
/// described in step 10, and finally produces a {@link ParallelTransition} that
/// moves each mismatched tile from its supply location to its paired demand
/// location.
///
/// @author Hedi Fourati
/// @author Fahd Ben Ismail
public final class TileAnimator {

    // Duration, in milliseconds, of an individual tile relocation animation.
    private static final Duration TILE_ANIMATION_DURATION = new Duration(500);

    /// Builds an animation that moves the tiles given as argument so that they
    /// occupy the positions expected by the given game state.
    ///
    /// @param pos       a function returning the position at which a tile placed
    ///                  on the given location must be drawn
    /// @param tilesRep  a map associating each tile kind to the list of nodes
    ///                  representing the tiles of that kind
    /// @param gameState the game state describing where each tile should be
    /// @return an animation displacing each mismatched tile from its current
    ///         location to its newly assigned location
    public static Animation animateTiles(
            Function<TileLocation, Point2D> pos,
            Map<TileKind, List<Node>> tilesRep,
            ReadOnlyGameState gameState){

        // Pairings produced by the matching algorithm: supply location -> demand location.
        Map<TileLocation, TileLocation> transaction = new HashMap<>();
        for (TileKind kind : TileKind.ALL) {

            Locations demand = demandFor(kind, gameState);
            Locations supply = supplyFor(tilesRep.get(kind), demand);
            // Wall demand: pair each newly occupied wall cell with the rightmost
            // tile of the pattern line on the same player's board.
            while(!demand.onWalls.isEmpty()){
                TileLocation.OnWall wallLocation = demand.onWalls.removeFirst();
                TileDestination.Pattern line = wallLocation.line();

                TileLocation.OnPattern wallSupply = new
                        TileLocation.OnPattern(wallLocation.playerId(), line, line.index());

                boolean isRemoved = supply.patterns.remove(wallSupply);
                assert isRemoved;

                transaction.put(wallSupply, wallLocation);
            }
            // Sources are consumed from the highest-indexed factory down to the center
            // area, so that center tiles are the last ones reassigned (visually clearer).
            // Within a single source, tiles are taken left to right.
            supply.onSources.sort(Comparator
                    .comparingInt( (TileLocation.OnSource onSource) ->
                            onSource.source().index()).reversed()
                    .thenComparing(TileLocation.OnSource::index));
            // Floor demand satisfied from sources.
            while(!demand.onFloors.isEmpty()){
                TileLocation.OnFloor floorLocation = demand.onFloors.removeFirst();
                TileLocation.OnSource floorSupply = supply.onSources.removeFirst();
                transaction.put(floorSupply, floorLocation);
            }
            // Pattern-line demand satisfied from sources.
            while(!demand.onPatterns.isEmpty()){
                TileLocation.OnPattern patternLocation = demand.onPatterns.removeFirst();
                TileLocation.OnSource patternSupply = supply.onSources.removeFirst();
                transaction.put(patternSupply, patternLocation);
            }
            // Off-board demand: floors first (tiles discarded at end of round),
            // then sources (tiles still on factories at end of round).
            while(!demand.offBoards.isEmpty() && !supply.onFloors.isEmpty()){
                TileLocation.OffBoard offBoardLocation = demand.offBoards.removeFirst();
                TileLocation.OnFloor offBoardSupply = supply.floors.removeFirst();
                transaction.put(offBoardSupply, offBoardLocation);
            }
            while(!demand.offBoards.isEmpty() && !supply.sources.isEmpty()){
                TileLocation.OffBoard offBoardLocation = demand.offBoards.removeFirst();
                TileLocation.OnSource offBoardSupply = supply.sources.removeFirst();
                transaction.put(offBoardSupply, offBoardLocation);
            }
            // Any remaining supply/demand is paired in an arbitrary order.
            Iterator<TileLocation> demandIterator = demand.iterator();
            Iterator<TileLocation> supplyIterator = supply.iterator();

            while (demandIterator.hasNext() && supplyIterator.hasNext())
                transaction.put(supplyIterator.next(), demandIterator.next());
        }

        // For every tile whose current location is a supply key, update the
        // location stored on the node and produce a relocation animation.
        return new ParallelTransition(
                tilesRep.values().stream()
                .flatMap(List::stream)
                .flatMap(node -> Stream
                        .ofNullable(transaction.get(Tiles.location(node)))
                        .map(tileLocation -> {
                            Tiles.setLocation(node, tileLocation);
                            return new RelocationTransition(node, pos.apply(tileLocation),
                                    TILE_ANIMATION_DURATION);
                        }))
                .toArray(Animation[]::new)
        );
    }


    // Computes every location at which a tile of the given kind should appear in
    // the given game state (sources, pattern lines, walls, floor lines, and
    // off-board for tiles in the bag or discarded).
    private static Locations demandFor(TileKind demandedKind, ReadOnlyGameState gameState) {
        Locations demand = Locations.empty();

        ReadOnlyIntArray tileSources = gameState.pkTileSources();
        for (int index = 0; index < tileSources.size(); index++) {
            TileSource source = TileSource.ALL.get(index);
            int offset = 0;
            int kindIndex = 0;
            int pkSource = tileSources.get(index);
            TileKind currentKind;
            while ((currentKind = TileKind.ALL.get(kindIndex)) != demandedKind) {
                offset += PkTileSet.countOf(pkSource, currentKind);
                kindIndex++;
            }
            for (int i = 0; i < PkTileSet.countOf(pkSource, demandedKind) ; i++)
                Locations.add(demand, new TileLocation.OnSource(source, offset + i));
        }

        for (PlayerId playerId : gameState.playerIds()) {
            ReadOnlyIntArray pkPlayerStates = gameState.pkPlayerStates();
            int pkPattern = PkPlayerStates.pkPatterns(pkPlayerStates, playerId);
            int pkWall = PkPlayerStates.pkWall(pkPlayerStates, playerId);

            for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
                if (!(demandedKind instanceof Colored colored)) continue;

                if (PkPatterns.canContain(pkPattern, line, colored))
                    for (int i = 0; i < PkPatterns.size(pkPattern, line); i++)
                        Locations.add(demand, new TileLocation.OnPattern(playerId, line, i));

                if (PkWall.hasTileAt(pkWall, line, colored))
                    Locations.add(demand, new TileLocation.OnWall(playerId, line, colored));
            }

            int pkFloor = PkPlayerStates.pkFloor(pkPlayerStates, playerId);
            int size = PkFloor.size(pkFloor);

            for (int i = 0; i < size; i++)
                if (PkFloor.tileAt(pkFloor, i) == demandedKind)
                    Locations.add(demand, new TileLocation.OnFloor(playerId, i));
        }
        int notInGameTiles =
                PkTileSet.union(gameState.pkDiscardedTiles(), gameState.pkTileBag());

        for (int i = 0;i < PkTileSet.countOf(notInGameTiles, demandedKind); i++)
            Locations.add(demand, new TileLocation.OffBoard(demandedKind, i));

        return demand;
    }

    // Computes the supply for a given tile kind: every node whose current
    // location is not part of the demand contributes its location to the supply.
    // Locations that are part of the demand are simultaneously removed from it,
    // so the demand returned to the caller contains only locations that still
    // need a tile.
    private static Locations supplyFor (List<Node> nodes, Locations demand){
        Locations supply = Locations.empty();

        for (Node node : nodes) {
            TileLocation currentLocation = Tiles.location(node);
            if (!Locations.remove(demand, currentLocation))
                Locations.add(supply, currentLocation);
        }

        return supply;
    }

    // Partition of tile locations by category, used to express the supply and
    // the demand of a given tile kind so that the matching algorithm can
    // pick the appropriate sub-list at every step.
    private record Locations(
            List<TileLocation.OnSource> sources,
            List<TileLocation.OnPattern> patterns,
            List<TileLocation.OnWall> walls,
            List<TileLocation.OnFloor> floors,
            List<TileLocation.OffBoard> offBoards
    ) {
        static Locations empty() {
            return new Locations(
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>()
            );
        }

        private static void add(Locations location, TileLocation tileLocation) {
            switch (tileLocation) {
                case TileLocation.OnWall l -> location.walls().add(l);
                case TileLocation.OnFloor l -> location.floors().add(l);
                case TileLocation.OnSource l -> location.sources().add(l);
                case TileLocation.OnPattern l -> location.patterns().add(l);
                case TileLocation.OffBoard l -> location.offBoards().add(l);
            }
        }

        private static boolean remove(Locations location, TileLocation tileLocation) {
            return switch (tileLocation) {
                case TileLocation.OnSource l -> location.sources().remove(l);
                case TileLocation.OnPattern l -> location.patterns().remove(l);
                case TileLocation.OnWall l -> location.walls().remove(l);
                case TileLocation.OnFloor l -> location.floors().remove(l);
                case TileLocation.OffBoard l -> location.offBoards().remove(l);
            };
        }

        private Iterator<TileLocation> iterator(){
            return Stream.of(sources, patterns, walls, floors, offBoards)
                    .<TileLocation>flatMap(List::stream)
                    .iterator();
        }
    }
}
