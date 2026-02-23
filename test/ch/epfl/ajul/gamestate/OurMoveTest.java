package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OurMoveTest {

    @Test
    void ofPacked() {
        // On vérifie que ofPacked(move.packed()) redonne bien le même Move
        // c'est-à-dire que l'aller-retour pack/unpack est sans perte d'information

        // Cas minimum : tous les indices à 0
        Move moveMin = new Move(TileSource.ALL.getFirst(), TileKind.Colored.ALL.getFirst(), TileDestination.ALL.getFirst());
        assertEquals(moveMin, Move.ofPacked(moveMin.packed()));

        // Cas maximum : tous les indices au max
        Move moveMax = new Move(TileSource.ALL.getLast(), TileKind.Colored.ALL.getLast(), TileDestination.ALL.getLast());
        assertEquals(moveMax, Move.ofPacked(moveMax.packed()));

        // Test exhaustif : toutes les combinaisons possibles (9 sources × 5 couleurs × 6 destinations = 270 cas)
        for (TileSource source : TileSource.ALL)
            for (TileKind.Colored color : TileKind.Colored.ALL)
                for (TileDestination destination : TileDestination.ALL) {
                    Move move = new Move(source, color, destination);
                    assertEquals(move, Move.ofPacked(move.packed()),
                            "ofPacked failed for source=%s, color=%s, dest=%s".formatted(source, color, destination));
                }
    }

    @Test
    void packed() {
        // On vérifie que packed() produit exactement les bons bits
        // Rappel du bit layout :
        //   bits 0-3  : index de la source      (4 bits, offset 0)
        //   bits 4-6  : index de la couleur     (3 bits, offset 4)
        //   bits 7-9  : index de la destination (3 bits, offset 7)
        //   bits 10-15: inutilisés (toujours 0)

        // Isolation source : couleur et destination à 0, seule la source varie
        // source=0 → 0, source=3 → 3
        assertEquals((short) 0,   new Move(TileSource.ALL.get(0), TileKind.Colored.ALL.get(0), TileDestination.ALL.get(0)).packed());
        assertEquals((short) 3,   new Move(TileSource.ALL.get(3), TileKind.Colored.ALL.get(0), TileDestination.ALL.get(0)).packed());

        // Isolation couleur : source et destination à 0, seule la couleur varie
        // couleur=2 → 2 << 4 = 32
        assertEquals((short) 32,  new Move(TileSource.ALL.get(0), TileKind.Colored.ALL.get(2), TileDestination.ALL.get(0)).packed());

        // Isolation destination : source et couleur à 0, seule la destination varie
        // destination=2 → 2 << 7 = 256
        assertEquals((short) 256, new Move(TileSource.ALL.get(0), TileKind.Colored.ALL.get(0), TileDestination.ALL.get(2)).packed());

        // Cas maximum : source=9, couleur=4, destination=5
        // 9 | (4 << 4) | (5 << 7) = 9 | 64 | 640 = 713
        assertEquals((short) 713, new Move(TileSource.ALL.getLast(), TileKind.Colored.ALL.getLast(), TileDestination.ALL.getLast()).packed());
    }

    @Test
    void source() {
        // On vérifie que l'accesseur source() retourne bien la source passée au constructeur
        TileSource expectedSource = TileSource.ALL.get(3);
        assertEquals(expectedSource, new Move(expectedSource, TileKind.Colored.ALL.get(0), TileDestination.ALL.get(0)).source());
    }

    @Test
    void tileColor() {
        // On vérifie que l'accesseur tileColor() retourne bien la couleur passée au constructeur
        TileKind.Colored expectedColor = TileKind.Colored.ALL.get(2);
        assertEquals(expectedColor, new Move(TileSource.ALL.get(0), expectedColor, TileDestination.ALL.get(0)).tileColor());
    }

    @Test
    void destination() {
        // On vérifie que l'accesseur destination() retourne bien la destination passée au constructeur
        TileDestination expectedDestination = TileDestination.ALL.get(2);
        assertEquals(expectedDestination, new Move(TileSource.ALL.get(0), TileKind.Colored.ALL.get(0), expectedDestination).destination());
    }

    @Test
    void constructorRejectsNulls() {
        // Move appelle Objects.requireNonNull sur chaque paramètre
        // On vérifie qu'un null sur n'importe lequel des 3 champs lève bien une NullPointerException
        TileSource source           = TileSource.ALL.get(0);
        TileKind.Colored color      = TileKind.Colored.ALL.get(0);
        TileDestination destination = TileDestination.ALL.get(0);

        assertThrows(NullPointerException.class, () -> new Move(null,   color, destination));
        assertThrows(NullPointerException.class, () -> new Move(source, null,  destination));
        assertThrows(NullPointerException.class, () -> new Move(source, color, null));
    }
}
