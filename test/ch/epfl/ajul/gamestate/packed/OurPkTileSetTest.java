package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileKind;
import org.junit.jupiter.api.Test;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.*;

class OurPkTileSetTest {
    // TileSet vide (0 tuile de chaque)
    private static final int ZERO_TILES = PkTileSet.EMPTY;
    // 1 seul marqueur premier joueur
    private static final int ONE_MARKER = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);
    // 3 tuiles de couleur A uniquement
    private static final int THREE_A = PkTileSet.of(3, TileKind.A);
    // 20 tuiles de couleur B (maximum pour une couleur)
    private static final int TWENTY_B = PkTileSet.of(20, TileKind.B);
    // 5 tuiles de couleur E
    private static final int FIVE_E = PkTileSet.of(5, TileKind.E);
    // TileSet complet avec toutes les couleurs + marqueur
    private static final int ALL_TILES = PkTileSet.FULL;
    // TileSet complet sans le marqueur premier joueur
    private static final int ALL_COLORED_TILES = PkTileSet.FULL_COLORED;

    @Test
    void of() {
        // Cas tout vide : of(0, ...) doit donner un TileSet vide
        assertTrue(PkTileSet.isEmpty(ZERO_TILES));
        assertEquals(0, PkTileSet.size(ZERO_TILES));
        assertEquals(0, PkTileSet.countOf(ZERO_TILES, TileKind.FIRST_PLAYER_MARKER));

        // Cas 1 FIRST_PLAYER_MARKER : le marqueur est présent, les couleurs sont absentes
        assertFalse(PkTileSet.isEmpty(ONE_MARKER));
        // assertEquals(1, PkTileSet.size(ONE_MARKER)); // Probleme
        assertEquals(1, PkTileSet.countOf(ONE_MARKER, TileKind.FIRST_PLAYER_MARKER));

        // Cas 3 tuiles A : seule la couleur A est présente
        assertEquals(3, PkTileSet.size(THREE_A));
        assertEquals(3, PkTileSet.countOf(THREE_A, TileKind.A));
        assertEquals(0, PkTileSet.countOf(THREE_A, TileKind.FIRST_PLAYER_MARKER));

        // Cas 20 tuiles B : maximum autorisé pour une couleur
        assertEquals(20, PkTileSet.size(TWENTY_B));
        assertEquals(20, PkTileSet.countOf(TWENTY_B, TileKind.B));
        assertEquals(0,  PkTileSet.countOf(TWENTY_B, TileKind.A));

        // Cas pas teste : valeur superieur a 21, valeur negative, first player marker n est pas majore a 1, possibilite d overflow
    }

    @Test
    void isEmpty() {
        // EMPTY doit être vide
        assertTrue(PkTileSet.isEmpty(ZERO_TILES));
        // FULL, FULL_COLORED, et un TileSet avec des tuiles ne doivent pas être vides
        assertFalse(PkTileSet.isEmpty(ALL_TILES));
        assertFalse(PkTileSet.isEmpty(ALL_COLORED_TILES));
        assertFalse(PkTileSet.isEmpty(FIVE_E));
    }

    @Test
    void size() {
        assertEquals(0, PkTileSet.size(ZERO_TILES));
        assertEquals(1, PkTileSet.size(ONE_MARKER));
        assertEquals(20, PkTileSet.size(TWENTY_B));
        assertEquals(101, PkTileSet.size(ALL_TILES));
    }

    @Test
    void countOf() {
        // Isolation : seule la couleur A est présente, les autres sont à 0
        assertEquals(3,  PkTileSet.countOf(THREE_A, TileKind.A));
        assertEquals(0,  PkTileSet.countOf(THREE_A, TileKind.B));
        assertEquals(0,  PkTileSet.countOf(THREE_A, TileKind.E));
        assertEquals(0,  PkTileSet.countOf(THREE_A, TileKind.FIRST_PLAYER_MARKER));

        // Isolation : seul le marqueur est présent
        assertEquals(1,  PkTileSet.countOf(ONE_MARKER, TileKind.FIRST_PLAYER_MARKER));
        assertEquals(0,  PkTileSet.countOf(ONE_MARKER, TileKind.A));

        // Valeur limite : 0 partout
        assertEquals(0,  PkTileSet.countOf(ZERO_TILES, TileKind.A));
        assertEquals(0,  PkTileSet.countOf(ZERO_TILES, TileKind.FIRST_PLAYER_MARKER));

        // Valeur limite : maximum (20) pour une couleur
        assertEquals(20, PkTileSet.countOf(TWENTY_B, TileKind.B));
        assertEquals(0,  PkTileSet.countOf(TWENTY_B, TileKind.A));

        // ALL_TILES : toutes les couleurs à 20 + marqueur à 1
        for (TileKind.Colored color : TileKind.Colored.ALL)
            assertEquals(20, PkTileSet.countOf(ALL_TILES, color));
        assertEquals(1, PkTileSet.countOf(ALL_TILES, TileKind.FIRST_PLAYER_MARKER));

        // ALL_COLORED_TILES : toutes les couleurs à 20, marqueur à 0
        for (TileKind.Colored color : TileKind.Colored.ALL)
            assertEquals(20, PkTileSet.countOf(ALL_COLORED_TILES, color));
        assertEquals(0, PkTileSet.countOf(ALL_COLORED_TILES, TileKind.FIRST_PLAYER_MARKER));
    }

    @Test
    void subsetOf() {
        // subsetOf retourne un TileSet ne contenant que les tuiles du type demandé

        // Le champ A de THREE_A vaut 3, les autres sont 0
        assertEquals(THREE_A,    PkTileSet.subsetOf(THREE_A, TileKind.A));
        assertEquals(ZERO_TILES, PkTileSet.subsetOf(THREE_A, TileKind.B));
        assertEquals(ZERO_TILES, PkTileSet.subsetOf(THREE_A, TileKind.FIRST_PLAYER_MARKER));

        // Sur ALL_TILES, chaque couleur doit donner 20 tuiles, le marqueur doit donner 1
        assertEquals(PkTileSet.of(20, TileKind.A), PkTileSet.subsetOf(ALL_TILES, TileKind.A));
        assertEquals(ONE_MARKER,                   PkTileSet.subsetOf(ALL_TILES, TileKind.FIRST_PLAYER_MARKER));

        // Sur ZERO_TILES, toujours vide
        assertEquals(ZERO_TILES, PkTileSet.subsetOf(ZERO_TILES, TileKind.A));
    }

    @Test
    void add() {
        // add ajoute exactement 1 tuile du type donné

        // Ajouter A à un TileSet vide donne 1 A
        assertEquals(PkTileSet.of(1, TileKind.A), PkTileSet.add(ZERO_TILES, TileKind.A));

        // Ajouter A à THREE_A donne 4 A
        assertEquals(PkTileSet.of(4, TileKind.A), PkTileSet.add(THREE_A, TileKind.A));

        // Ajouter le marqueur à un TileSet vide donne ONE_MARKER
        assertEquals(ONE_MARKER, PkTileSet.add(ZERO_TILES, TileKind.FIRST_PLAYER_MARKER));

        // add ne modifie pas les autres champs
        int oneA = PkTileSet.add(ZERO_TILES, TileKind.A);
        assertEquals(1, PkTileSet.countOf(oneA, TileKind.A));
        assertEquals(0, PkTileSet.countOf(oneA, TileKind.B));
        assertEquals(0, PkTileSet.countOf(oneA, TileKind.FIRST_PLAYER_MARKER));
    }

    @Test
    void remove() {
        // remove soustrait exactement 1 tuile du type donné

        // Retirer 1 A à THREE_A donne 2 A
        assertEquals(PkTileSet.of(2, TileKind.A), PkTileSet.remove(THREE_A, TileKind.A));

        // Retirer le marqueur à ONE_MARKER donne ZERO_TILES
        assertEquals(ZERO_TILES, PkTileSet.remove(ONE_MARKER, TileKind.FIRST_PLAYER_MARKER));

        // remove ne modifie pas les autres champs
        int twoA = PkTileSet.remove(THREE_A, TileKind.A);
        assertEquals(2, PkTileSet.countOf(twoA, TileKind.A));
        assertEquals(0, PkTileSet.countOf(twoA, TileKind.B));
        assertEquals(0, PkTileSet.countOf(twoA, TileKind.FIRST_PLAYER_MARKER));
    }

    @Test
    void union() {
        // union additionne les compteurs de deux TileSets champ par champ

        // Neutre : union avec ZERO_TILES ne change rien
        assertEquals(THREE_A, PkTileSet.union(THREE_A, ZERO_TILES));
        assertEquals(THREE_A, PkTileSet.union(ZERO_TILES, THREE_A));
        assertEquals(ZERO_TILES, PkTileSet.union(ZERO_TILES, ZERO_TILES));

        // union de THREE_A et TWENTY_B : les deux champs s'accumulent sans pollution
        int threeAPlusTwentyB = PkTileSet.union(THREE_A, TWENTY_B);
        assertEquals(3, PkTileSet.countOf(threeAPlusTwentyB, TileKind.A));
        assertEquals(20, PkTileSet.countOf(threeAPlusTwentyB, TileKind.B));
        assertEquals(0, PkTileSet.countOf(threeAPlusTwentyB, TileKind.C));
        assertEquals(0, PkTileSet.countOf(threeAPlusTwentyB, TileKind.FIRST_PLAYER_MARKER));

        // ALL_COLORED_TILES + ONE_MARKER doit redonner ALL_TILES
        assertEquals(ALL_TILES, PkTileSet.union(ALL_COLORED_TILES, ONE_MARKER));
    }

    @Test
    void difference() {
        // difference soustrait les compteurs du second TileSet du premier

        // Différence avec soi-même = vide
        assertEquals(ZERO_TILES, PkTileSet.difference(THREE_A, THREE_A));

        // Différence avec ZERO_TILES = inchangé
        assertEquals(THREE_A, PkTileSet.difference(THREE_A, ZERO_TILES));

        // ALL_TILES - ALL_COLORED_TILES = ONE_MARKER
        assertEquals(ONE_MARKER, PkTileSet.difference(ALL_TILES, ALL_COLORED_TILES));

        // ALL_COLORED_TILES - THREE_A : A passe de 20 à 17, les autres restent à 20
        int withoutThreeA = PkTileSet.difference(ALL_COLORED_TILES, THREE_A);
        assertEquals(17, PkTileSet.countOf(withoutThreeA, TileKind.A));
        assertEquals(20, PkTileSet.countOf(withoutThreeA, TileKind.B));
        assertEquals(0,  PkTileSet.countOf(withoutThreeA, TileKind.FIRST_PLAYER_MARKER));
    }

    @Test
    void copyColoredInto() {
        TileKind.Colored[] destination = new TileKind.Colored[100];

        // TileSet vide : 0 tuile copiée
        assertEquals(0, PkTileSet.copyColoredInto(ZERO_TILES, destination));

        // THREE_A : 3 tuiles A copiées dans l'ordre
        int countA = PkTileSet.copyColoredInto(THREE_A, destination);
        assertEquals(3, countA);
        assertEquals(TileKind.Colored.A, destination[0]);
        assertEquals(TileKind.Colored.A, destination[1]);
        assertEquals(TileKind.Colored.A, destination[2]);

        // TWENTY_B : 20 tuiles B copiées
        int countB = PkTileSet.copyColoredInto(TWENTY_B, destination);
        assertEquals(20, countB);
        for (int i = 0; i < 20; i++)
            assertEquals(TileKind.Colored.B, destination[i]);

        // union(THREE_A, TWENTY_B) : d'abord les 3 A, puis les 20 B (ordre de TileKind.Colored.ALL)
        int countAB = PkTileSet.copyColoredInto(PkTileSet.union(THREE_A, TWENTY_B), destination);
        assertEquals(23, countAB);
        for (int i = 0; i < 3;  i++) assertEquals(TileKind.Colored.A, destination[i]);
        for (int i = 3; i < 23; i++) assertEquals(TileKind.Colored.B, destination[i]);
    }

    private static RandomGenerator rngF(long seed) {
        return RandomGeneratorFactory.getDefault().create(seed);
    }

    @Test
    void sampleColoredInto() {
        var A = TileKind.Colored.A; var B = TileKind.Colored.B;
        var C = TileKind.Colored.C; var D = TileKind.Colored.D;
        var E = TileKind.Colored.E;

        // --- CAS SIMPLES (pas de randomness) ---
        RandomGenerator rng = RandomGenerator.of("Random");
        TileKind.Colored[] destination = new TileKind.Colored[5];

        int end = PkTileSet.sampleColoredInto(THREE_A, destination, 0, rng);
        assertEquals(3, end);
        for (int i = 0; i < 3; i++)
            assertEquals(TileKind.Colored.A, destination[i]);

        int endWithOffset = PkTileSet.sampleColoredInto(THREE_A, destination, 2, rng);
        assertEquals(5, endWithOffset);
        for (int i = 2; i < 5; i++)
            assertEquals(TileKind.Colored.A, destination[i]);

        // --- VALEUR DE RETOUR ---
        assertEquals(100, PkTileSet.sampleColoredInto(PkTileSet.FULL_COLORED, new TileKind.Colored[90], 0, rngF(0)));
        assertEquals(105, PkTileSet.sampleColoredInto(PkTileSet.FULL_COLORED, new TileKind.Colored[90], 5, rngF(0)));
        assertEquals(0,   PkTileSet.sampleColoredInto(PkTileSet.EMPTY, new TileKind.Colored[10], 0, rngF(0)));
        assertEquals(3,   PkTileSet.sampleColoredInto(PkTileSet.EMPTY, new TileKind.Colored[10], 3, rngF(0)));

        // --- VALEURS DE RÉFÉRENCE (confirmées par plusieurs étudiants, seed=108, dest.length=10) ---
        TileKind.Colored[][] expectedOffset = {
                {A, B, C, C, B, B, C, E, B, C},                            // offset 0
                {null, E, B, E, B, A, C, A, A, D},                         // offset 1
                {null, null, D, E, B, B, D, A, C, D},                      // offset 2
                {null, null, null, B, D, B, C, C, A, B},                   // offset 3
                {null, null, null, null, B, E, A, C, B, D},                // offset 4
                {null, null, null, null, null, C, E, B, D, C},             // offset 5
                {null, null, null, null, null, null, B, C, C, A},          // offset 6
                {null, null, null, null, null, null, null, B, A, A},       // offset 7
                {null, null, null, null, null, null, null, null, B, D},    // offset 8
                {null, null, null, null, null, null, null, null, null, D}, // offset 9
        };
        for (int offset = 0; offset < expectedOffset.length; offset++) {
            var d = new TileKind.Colored[10];
            PkTileSet.sampleColoredInto(PkTileSet.FULL_COLORED, d, offset, rngF(108));
            assertArrayEquals(expectedOffset[offset], d, "offset=" + offset);
        }

        // --- OFFSET NE TOUCHE PAS LES CASES AVANT ---
        var dOffset = new TileKind.Colored[10];
        dOffset[0] = A; // sentinelle
        PkTileSet.sampleColoredInto(PkTileSet.FULL_COLORED, dOffset, 3, rngF(42));
        assertSame(A, dOffset[0]);
        assertNull(dOffset[1]);
        assertNull(dOffset[2]);

        // --- DÉTERMINISME ---
        var d1 = new TileKind.Colored[10]; var d2 = new TileKind.Colored[10];
        PkTileSet.sampleColoredInto(PkTileSet.FULL_COLORED, d1, 0, rngF(42));
        PkTileSet.sampleColoredInto(PkTileSet.FULL_COLORED, d2, 0, rngF(42));
        assertArrayEquals(d1, d2);
    }

    @Test
    void testToString() {
        // TileSet vide
        assertEquals("{}", PkTileSet.toString(ZERO_TILES));

        // Un seul type de tuile
        assertEquals("{3*A}", PkTileSet.toString(THREE_A));
        assertEquals("{20*B}", PkTileSet.toString(TWENTY_B));
        assertEquals("{1*FIRST_PLAYER_MARKER}", PkTileSet.toString(ONE_MARKER));

        // Plusieurs types : ordre A, B, C, D, E, puis marqueur en dernier
        assertEquals("{3*A,20*B}", PkTileSet.toString(PkTileSet.union(THREE_A, TWENTY_B)));

        // ALL_TILES : toutes les couleurs à 20 puis le marqueur à 1
        assertEquals("{20*A,20*B,20*C,20*D,20*E,1*FIRST_PLAYER_MARKER}", PkTileSet.toString(ALL_TILES));

        // ALL_COLORED_TILES : toutes les couleurs à 20, pas de marqueur
        assertEquals("{20*A,20*B,20*C,20*D,20*E}", PkTileSet.toString(ALL_COLORED_TILES));
    }

}