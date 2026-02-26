package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileKind;

import java.util.List;
import java.util.StringJoiner;
import java.util.random.RandomGenerator;

public final class PkTileSet {
    // Utilisation de FULL, FULL_COLORED ?
    public static final int EMPTY = 0;
    public static final int FULL = computeFull();
    public static final int FULL_COLORED = remove(FULL, TileKind.FIRST_PLAYER_MARKER);
    public static final int COLOR_SHIFT = 6;
    public static final int MASK = (1 << COLOR_SHIFT) -1;
    public static final int MAX_VALUE = 20;

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

    public static boolean isEmpty(int pkTileSet){
        assert isValid(pkTileSet);
        return pkTileSet == EMPTY;
    }

    // Verif mais je pense que c'est good
    public static int size(int pkTileSet) {
        assert isValid(pkTileSet);
        int sum = pkTileSet + (pkTileSet >>> COLOR_SHIFT);
        int AB = sum & MASK;
        int CD = (sum >>> (2 *COLOR_SHIFT)) & MASK;
        int EM = (sum >>> (4 *COLOR_SHIFT)) & MASK;
        return AB + CD + EM;
    }

    public static int countOf(int pkTileSet, TileKind tileKind){
        int tileKindIndex = tileKind.index() * COLOR_SHIFT;
        int tileKindShift = pkTileSet >> tileKindIndex;
        return tileKindShift & MASK;
    }

    public static int subsetOf(int pkTileSet, TileKind tileKind){
        assert isValid(pkTileSet);
        return of(countOf(pkTileSet,tileKind), tileKind);
    }

    public static int add(int pkTileSet, TileKind tileKind){
        assert isValid(pkTileSet);
        int tileCounter = 1 << (tileKind.index() * COLOR_SHIFT);
        return union(pkTileSet, tileCounter);
    }

    public static int remove(int pkTileSet, TileKind tileKind){
        assert isValid(pkTileSet);
        int tileCounter = 1 << (tileKind.index() * COLOR_SHIFT);
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

//    public static int sampleColoredInto(int pkTileSet, TileKind.Colored[] destination, int offset, RandomGenerator randomGenerator){
//        int reservoirSize = destination.length - offset ;
//        int i  = 0;
//        for(TileKind.Colored coloredTile : TileKind.Colored.ALL) {
//            for (int j = 0; j < PkTileSet.countOf(pkTileSet, coloredTile); j++) {
//                if (i < reservoirSize) {
//                    destination[offset + i] = coloredTile;
//                } else {
//                    int randomValue = randomGenerator.nextInt(0, i+1);
//                    if (randomValue < reservoirSize) {
//                        destination[offset + randomValue] = coloredTile;
//                    }
//                }
//                i++;
//            }
//        }
//       return offset + i;
//    }

    public static int sampleColoredInto(int pkTileSet, TileKind.Colored[] destination, int offset, RandomGenerator randomGenerator) {
        assert isValid(pkTileSet);
        int reservoirSize = destination.length - offset;
        int reservoirEnd = offset + reservoirSize;
        int currentIndex = offset;
        for (TileKind.Colored color : TileKind.Colored.ALL) {
            int colorCount = countOf(pkTileSet, color);
            for (int j = 0; j < colorCount; j++) {
                if (currentIndex < reservoirEnd) {
                    destination[currentIndex] = color;
                } else {
                    int randomIndex = randomGenerator.nextInt(offset, currentIndex + 1);
                    if (randomIndex < reservoirEnd) {
                        destination[randomIndex] = color;
                    }
                }
                currentIndex++;
            }
        }
        return currentIndex;
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
            if (countOf(pkTileSet, tileKind) > tileKind.tilesCount())
                return false;
        }
        return true;
    }
}