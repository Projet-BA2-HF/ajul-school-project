package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileKind;

import java.util.random.RandomGenerator;

public final class PkTileSet {
    public static final int EMPTY = 0; // Tuiles vides
    public static final int FULL = (0b01_010100_010100_010100_010100_010100); // Tuiles pleins
    public static final int FULL_COLORED = (0b00_010100_010100_010100_010100_010100);
    public static final int SUM_SHIFT = 6;
    public static final int MASK = (1 << SUM_SHIFT) -1;
    public static final int MAX_VALUE = 20;


    private static int computeFull(){
        int fullValue = EMPTY;
        for (int i=0; i<TileKind.COUNT; i++){
            fullValue = union(fullValue, of(MAX_VALUE, TileKind.ALL.get(i)));
        }
        return fullValue;
    }

    public static int of(int count, TileKind tileKind){
        return count << (tileKind.index() * 6);
    }

    //Verif
    public static boolean isEmpty(int pkTileSet){
        return pkTileSet == EMPTY;
    }

    public static int size(int pkTileSet){
        int pkTileSetShifted = pkTileSet >> SUM_SHIFT;
        int sum = pkTileSetShifted | pkTileSet;
        int partial1 = sum & MASK;
        int partial2 = (sum >> 2*SUM_SHIFT) & MASK;
        int partial3 = (sum >> 3*SUM_SHIFT) & MASK;
        return partial1 + partial2 + partial3;
    }

    public static int countOf(int pkTileSet, TileKind tileKind){
        int tileKindIndex = tileKind.index() * SUM_SHIFT;
        int tileKindShift = pkTileSet >> tileKindIndex;
        return tileKindShift & MASK;
    }

    // Verif
    public static int subsetOf(int pkTileSet, TileKind tileKind){
        return of(countOf(pkTileSet,tileKind), tileKind);
    }

    // Verif
    public static int add(int pkTileSet, TileKind tileKind){
        int tileCounter = 1 << (tileKind.index() * SUM_SHIFT);
        return union(pkTileSet, tileCounter);
    }

    // Verif
    public static int remove(int pkTileSet, TileKind tileKind){
        int tileCounter = 1 << (tileKind.index() * SUM_SHIFT);
        return difference(pkTileSet, tileCounter);
    }

    public static int union(int pkTileSet1, int pkTileSet2){
        return pkTileSet1 + pkTileSet2;
    }

    public static int difference(int pkTileSet1, int pkTileSet2){
        return pkTileSet1 - pkTileSet2;
    }

    // Verif
//    public static int copyColoredInto(int pkTileSet, TileKind.Colored[] destination) {
//        int index = 0;
//
//        // On parcourt les couleurs dans l'ordre (A, B, C, D, E)
//        for (TileKind.Colored color : TileKind.Colored.ALL) {
//            int count = countOf(pkTileSet, color); // On récupère la quantité
//
//            if (count > 0) {
//                // On remplit le tableau de 'index' à 'index + count' avec la couleur
//                // On déplace le curseur pour la prochaine couleur
//                index += count;
//            }
//        }
//
//        return index; // Retourne l'index libre suivant
//    }

    public static int sampleColoredInto(int pkTileSet, TileKind.Colored[] destination, int offset, RandomGenerator randomGenerator){
        return 0;
    }

    public static String toString(int pkTileSet){
        return "";
    }

    public static int isValid(int pkTileSet){
        return 0;
    }
}
