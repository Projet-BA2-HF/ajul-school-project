package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileKind;

import java.util.StringJoiner;
import java.util.random.RandomGenerator;

public final class PkTileSet {
    // Utilisation de FULL, FULL_COLORED ?
    public static final int EMPTY = 0;
    public static final int FULL = computeFull();
    public static final int FULL_COLORED = remove(FULL, TileKind.FIRST_PLAYER_MARKER);
    public static final int SUM_SHIFT = 6;
    public static final int MASK = (1 << SUM_SHIFT) -1;
    public static final int MAX_VALUE = 20;

    // Verif
    private static int computeFull(){
        int fullValue = EMPTY;
        for (TileKind tk : TileKind.ALL){
            fullValue = union(fullValue, of(tk.tilesCount(), tk));
        }
        return fullValue;
    }

    // Effet de bords pas pris en compte : valeur superieur a 21, valeur negative, first player marker n est pas majore a 1, possibilite d overflow
    public static int of(int count, TileKind tileKind){
        return count << (tileKind.index() * 6);
    }

    // Verif
    public static boolean isEmpty(int pkTileSet){
        assert isValid(pkTileSet);
        return pkTileSet == EMPTY;
    }

    public static int size(int pkTileSet){
        assert isValid(pkTileSet);
        int sum = 0;
        for(int i = 0; i < 5; i++){
           sum +=  (pkTileSet >> i*SUM_SHIFT) & MASK;
        }
        return sum + countOf(pkTileSet, TileKind.FIRST_PLAYER_MARKER);
    }

    public static int countOf(int pkTileSet, TileKind tileKind){
        assert isValid(pkTileSet);
        int tileKindIndex = tileKind.index() * SUM_SHIFT;
        int tileKindShift = pkTileSet >> tileKindIndex;
        return tileKindShift & MASK;
    }

    // Verif
    public static int subsetOf(int pkTileSet, TileKind tileKind){
        assert isValid(pkTileSet);
        return of(countOf(pkTileSet,tileKind), tileKind);
    }

    // Verif
    public static int add(int pkTileSet, TileKind tileKind){
        assert isValid(pkTileSet);
        int tileCounter = 1 << (tileKind.index() * SUM_SHIFT);
        return union(pkTileSet, tileCounter);
    }

    // Verif
    public static int remove(int pkTileSet, TileKind tileKind){
        assert isValid(pkTileSet);
        int tileCounter = 1 << (tileKind.index() * SUM_SHIFT);
        return difference(pkTileSet, tileCounter);
    }

    public static int union(int pkTileSet1, int pkTileSet2){
        assert isValid(pkTileSet1) && isValid(pkTileSet2);
        return pkTileSet1 + pkTileSet2;
    }

    public static int difference(int pkTileSet1, int pkTileSet2){
        assert isValid(pkTileSet1) && isValid(pkTileSet2);
        return pkTileSet1 - pkTileSet2;
    }

    // Verif
    public static int copyColoredInto(int pkTileSet, TileKind.Colored[] destination) {
        assert isValid(pkTileSet);
        int tileCount = 0;
        for(TileKind.Colored tileColor : TileKind.Colored.ALL){
            int colorCount = countOf(pkTileSet, tileColor);
            for(int i = 0; i < colorCount; i++){
                destination[tileCount] = tileColor;
                tileCount++;
            }
        }
        return tileCount;
    }

    public static int sampleColoredInto(int pkTileSet, TileKind.Colored[] destination, int offset, RandomGenerator randomGenerator){
        assert isValid(pkTileSet);
        int availableSpace = destination.length - offset;
        int drawnTiles = 0;
        for (TileKind.Colored color : TileKind.Colored.ALL ){
            int colorCount = countOf(pkTileSet, color);
            for (int j=0; j<colorCount; j++){
                if (drawnTiles <availableSpace){
                    destination[offset + drawnTiles] = color;
                } else {
                    int random = randomGenerator.nextInt(drawnTiles + 1);
                    if (random<availableSpace){
                        destination[offset + random] = color;
                    }
                }
                drawnTiles++;
            }
        }
        return offset + (Math.min(drawnTiles, availableSpace));
    }

    public static String toString(int pkTileSet){
        assert isValid(pkTileSet);
        StringJoiner outputString = new StringJoiner(",");
        for(TileKind.Colored tileColor : TileKind.Colored.ALL){
            int colorCount = countOf(pkTileSet, tileColor);
            if (colorCount == 0) continue;
            outputString.add(colorCount + "*" + tileColor);
        }
        int markerCount = countOf(pkTileSet, TileKind.FIRST_PLAYER_MARKER);
        if(markerCount > 0){
            outputString.add(countOf(pkTileSet, TileKind.FIRST_PLAYER_MARKER) + "*" + TileKind.FIRST_PLAYER_MARKER);
        }
        return "{" + outputString.toString() + "}";
    }

    private static boolean isValid(int pkTileSet) {
        for (TileKind tileKind : TileKind.ALL) {
            int count = (pkTileSet >> (tileKind.index() * SUM_SHIFT)) & MASK;
            if (count > tileKind.tilesCount())
                return false;
        }
        return true;
    }
}