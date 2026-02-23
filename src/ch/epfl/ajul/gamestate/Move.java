package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.packed.PkMove;

import java.util.Objects;

//Verif
public record Move(TileSource source, TileKind.Colored tileColor, TileDestination destination) {

    public static final int MAX_MOVES = 9 * 4 * 6;

    public Move{
        Objects.requireNonNull(source);
        Objects.requireNonNull(tileColor);
        Objects.requireNonNull(destination);
    }

    public static Move ofPacked(short pkMove){
        return new Move(PkMove.source(pkMove), PkMove.color(pkMove), PkMove.destination(pkMove));
    }

    public short packed(){
        return PkMove.pack(source, tileColor, destination);
    }
}
