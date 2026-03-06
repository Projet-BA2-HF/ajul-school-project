package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OurPkMoveTest {

    @Test
    void pack() {
        // On teste toutes les combinaisons possibles de source, couleur et destination
        // Pour chaque combinaison, on calcule manuellement le short attendu
        // et on vérifie que PkMove.pack() donne le même résultat
        //
        // Rappel du bit layout :
        //   bits 0-3  : index de la source      (4 bits, offset 0)
        //   bits 4-6  : index de la couleur     (3 bits, offset 4)
        //   bits 7-9  : index de la destination (3 bits, offset 7)
        //   bits 10-15: inutilisés (toujours 0)
        for (TileSource source : TileSource.ALL)
            for (TileKind.Colored color : TileKind.Colored.ALL)
                for (TileDestination destination : TileDestination.ALL) {
                    short actualPacked   = PkMove.pack(source, color, destination);
                    short expectedPacked = (short) (source.index() | (color.index() << 4) | (destination.index() << 7));
                    assertEquals(expectedPacked, actualPacked,
                            "pack failed for source=%s, color=%s, dest=%s".formatted(source, color, destination));
                }
    }

    @Test
    void source() {
        // On pack toutes les combinaisons et on vérifie que source() retrouve
        // bien la source d'origine (les autres champs ne doivent pas polluer)
        for (TileSource source : TileSource.ALL)
            for (TileKind.Colored color : TileKind.Colored.ALL)
                for (TileDestination destination : TileDestination.ALL) {
                    short packed = PkMove.pack(source, color, destination);
                    assertEquals(source, PkMove.source(packed),
                            "source failed for source=%s, color=%s, dest=%s".formatted(source, color, destination));
                }
    }

    @Test
    void color() {
        // On pack toutes les combinaisons et on vérifie que color() retrouve
        // bien la couleur d'origine (les autres champs ne doivent pas polluer)
        for (TileSource source : TileSource.ALL)
            for (TileKind.Colored color : TileKind.Colored.ALL)
                for (TileDestination destination : TileDestination.ALL) {
                    short packed = PkMove.pack(source, color, destination);
                    assertEquals(color, PkMove.color(packed),
                            "color failed for source=%s, color=%s, dest=%s".formatted(source, color, destination));
                }
    }

    @Test
    void destination() {
        // On pack toutes les combinaisons et on vérifie que destination() retrouve
        // bien la destination d'origine (les autres champs ne doivent pas polluer)
        for (TileSource source : TileSource.ALL)
            for (TileKind.Colored color : TileKind.Colored.ALL)
                for (TileDestination destination : TileDestination.ALL) {
                    short packed = PkMove.pack(source, color, destination);
                    assertEquals(destination, PkMove.destination(packed),
                            "destination failed for source=%s, color=%s, dest=%s".formatted(source, color, destination));
                }
    }
}
