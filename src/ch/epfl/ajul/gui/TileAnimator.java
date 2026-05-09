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
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class TileAnimator {

    public static Animation animateTiles(
            Function<TileLocation, Point2D> pos,
            Map<TileKind, List<Node>> tilesRep,
            ReadOnlyGameState gameState){

        Map<TileLocation, TileLocation> transaction = new HashMap<>();
        for (TileKind kind : TileKind.ALL) {

            Locations demand = demandFor(kind, gameState);
            Locations supply = supplyFor(tilesRep.get(kind), demand);
            while(!demand.walls.isEmpty()){
                TileLocation.OnWall wallLocation = demand.walls.removeFirst();
                TileDestination.Pattern line = wallLocation.line();

                TileLocation.OnPattern wallSupply = new
                        TileLocation.OnPattern(wallLocation.playerId(), line, line.index());

                boolean isRemoved = supply.patterns.remove(wallSupply);
                assert isRemoved;

                transaction.put(wallSupply, wallLocation);
            }
            supply.sources.sort(Comparator.comparingInt(TileLocation.OnSource::index));
            while(!demand.floors.isEmpty()){
                TileLocation.OnFloor floorLocation = demand.floors.removeFirst();
                TileLocation.OnSource floorSupply = supply.sources.removeFirst();
                transaction.put(floorSupply, floorLocation);
            }
            while(!demand.patterns.isEmpty()){
                TileLocation.OnPattern patternLocation = demand.patterns.removeFirst();
                TileLocation.OnSource patternSupply = supply.sources.removeFirst();
                transaction.put(patternSupply, patternLocation);
            }
            while(!demand.offBoards.isEmpty() && !supply.floors.isEmpty()){
                TileLocation.OffBoard offBoardLocation = demand.offBoards.removeFirst();
                TileLocation.OnFloor offBoardSupply = supply.floors.removeFirst();
                transaction.put(offBoardSupply, offBoardLocation);
            }
            while(!demand.offBoards.isEmpty() && !supply.sources.isEmpty()){
                TileLocation.OffBoard offBoardLocation = demand.offBoards.removeFirst();
                TileLocation.OnSource offBoardSupply = supply.sources.removeFirst();
                transaction.put(offBoardSupply, offBoardLocation);
            }
            Iterator<TileLocation> demandIterator = demand.iterator();
            Iterator<TileLocation> supplyIterator = supply.iterator();

            while (demandIterator.hasNext() && supplyIterator.hasNext())
                transaction.put(supplyIterator.next(), demandIterator.next());
        }

        return new ParallelTransition(
                tilesRep.values().stream()
                .flatMap(List::stream)
                .flatMap(node -> Stream
                        .ofNullable(transaction.get(Tiles.location(node)))
                        .map(tileLocation -> {
                            Tiles.setLocation(node, tileLocation);
                            return new RelocationTransition(node, pos.apply(tileLocation),
                                    new Duration(500));
                        }))
                .toArray(Animation[]::new)
        );
    }


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

    private static Locations supplyFor (List<Node> nodes, Locations demand){
        Locations supply = Locations.empty();

        for (Node node : nodes) {
            TileLocation currentLocation = Tiles.location(node);
            if (!Locations.remove(demand, currentLocation))
                Locations.add(supply, currentLocation);
        }

        return supply;
    }

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
