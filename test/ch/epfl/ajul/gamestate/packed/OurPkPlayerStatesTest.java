package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import org.junit.jupiter.api.Test;
import java.util.List;
import ch.epfl.ajul.Game.PlayerDescription;
import ch.epfl.ajul.Game.PlayerDescription.PlayerKind;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class OurPkPlayerStatesTest {
    // ==================== GAMES ====================
    static final Game GAME_2P = new Game(List.of(
            new PlayerDescription(PlayerId.P1, "Alice", PlayerKind.HUMAN),
            new PlayerDescription(PlayerId.P2, "Bob", Game.PlayerDescription.PlayerKind.AI)
    ));
    static final Game GAME_3P = new Game(List.of(
            new PlayerDescription(PlayerId.P1, "Alice", PlayerKind.HUMAN),
            new PlayerDescription(PlayerId.P2, "Bob", PlayerKind.AI),
            new PlayerDescription(PlayerId.P3, "Charlie", PlayerKind.HUMAN)
    ));
    static final Game GAME_4P = new Game(List.of(
            new PlayerDescription(PlayerId.P1, "Alice", PlayerKind.HUMAN),
            new PlayerDescription(PlayerId.P2, "Bob", PlayerKind.AI),
            new PlayerDescription(PlayerId.P3, "Charlie", PlayerKind.HUMAN),
            new PlayerDescription(PlayerId.P4, "Diana", PlayerKind.AI)
    ));

    // ==================== PLAYER IDS ====================
    static final PlayerId P1 = PlayerId.P1;
    static final PlayerId P2 = PlayerId.P2;
    static final PlayerId P3 = PlayerId.P3;
    static final PlayerId P4 = PlayerId.P4;

    // ==================== TILE KINDS ====================
    static final TileKind.Colored A = TileKind.Colored.A;
    static final TileKind.Colored B = TileKind.Colored.B;
    static final TileKind.Colored C = TileKind.Colored.C;
    static final TileKind.Colored D = TileKind.Colored.D;
    static final TileKind.Colored E = TileKind.Colored.E;

    // ==================== PATTERN LINES ====================
    static final TileDestination.Pattern PAT1 = TileDestination.Pattern.PATTERN_1;
    static final TileDestination.Pattern PAT2 = TileDestination.Pattern.PATTERN_2;
    static final TileDestination.Pattern PAT3 = TileDestination.Pattern.PATTERN_3;
    static final TileDestination.Pattern PAT4 = TileDestination.Pattern.PATTERN_4;
    static final TileDestination.Pattern PAT5 = TileDestination.Pattern.PATTERN_5;

    // ==================== PACKED PATTERNS ====================
    static final int PK_PATTERNS_EMPTY = PkPatterns.EMPTY;

    // Line 1: 1xA, rest empty
    static final int PK_PATTERNS_1A_ON_L1 =
            PkPatterns.withAddedTiles(PkPatterns.EMPTY, PAT1, 1, A);

    // Line 2: 2xB
    static final int PK_PATTERNS_2B_ON_L2 =
            PkPatterns.withAddedTiles(PkPatterns.EMPTY, PAT2, 2, B);

    // Line 3: 3xC (full)
    static final int PK_PATTERNS_3C_ON_L3 =
            PkPatterns.withAddedTiles(PkPatterns.EMPTY, PAT3, 3, C);

    // Line 5: 5xE (full)
    static final int PK_PATTERNS_5E_ON_L5 =
            PkPatterns.withAddedTiles(PkPatterns.EMPTY, PAT5, 5, E);

    // Multiple lines filled
    static final int PK_PATTERNS_MIXED_1 = PkPatterns.withAddedTiles(
            PkPatterns.withAddedTiles(
                    PkPatterns.withAddedTiles(PkPatterns.EMPTY, PAT1, 1, A),
                    PAT3, 2, C),
            PAT5, 4, D);

    // All lines full
    static final int PK_PATTERNS_ALL_FULL = PkPatterns.withAddedTiles(
            PkPatterns.withAddedTiles(
                    PkPatterns.withAddedTiles(
                            PkPatterns.withAddedTiles(
                                    PkPatterns.withAddedTiles(PkPatterns.EMPTY,
                                            PAT1, 1, A),
                                    PAT2, 2, B),
                            PAT3, 3, C),
                    PAT4, 4, D),
            PAT5, 5, E);

    // ==================== PACKED FLOORS ====================
    static final int PK_FLOOR_EMPTY = PkFloor.EMPTY;

    // Floor with 1 tile of color A
    static final int PK_FLOOR_1A = PkFloor.withAddedTiles(
            PkFloor.EMPTY, PkTileSet.of(1, A));

    // Floor with first player marker
    static final int PK_FLOOR_MARKER = PkFloor.withAddedTiles(
            PkFloor.EMPTY, PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER));

    // Floor with 3 tiles: marker + 2xB
    static final int PK_FLOOR_MARKER_2B = PkFloor.withAddedTiles(
            PkFloor.EMPTY,
            PkTileSet.union(
                    PkTileSet.of(2, B),
                    PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER)));

    // Full floor (7 tiles)
    static final int PK_FLOOR_FULL = PkFloor.withAddedTiles(
            PkFloor.EMPTY,
            PkTileSet.union(
                    PkTileSet.union(PkTileSet.of(3, A), PkTileSet.of(2, B)),
                    PkTileSet.of(2, C)));

    // ==================== PACKED WALLS ====================
    static final int PK_WALL_EMPTY = PkWall.EMPTY;

    // Wall with one tile at (PATTERN_1, A) => index 0
    static final int PK_WALL_ONE_TILE = PkWall.withTileAt(PkWall.EMPTY, PAT1, A);

    // Wall with first row full
    static final int PK_WALL_ROW1_FULL = PkWall.withTileAt(
            PkWall.withTileAt(
                    PkWall.withTileAt(
                            PkWall.withTileAt(
                                    PkWall.withTileAt(PkWall.EMPTY, PAT1, A),
                                    PAT1, B),
                            PAT1, C),
                    PAT1, D),
            PAT1, E);

    // Wall with diagonal (one per row)
    static final int PK_WALL_DIAGONAL = PkWall.withTileAt(
            PkWall.withTileAt(
                    PkWall.withTileAt(
                            PkWall.withTileAt(
                                    PkWall.withTileAt(PkWall.EMPTY, PAT1, A),
                                    PAT2, A),
                            PAT3, A),
                    PAT4, A),
            PAT5, A);

    // Full wall
    static final int PK_WALL_FULL = (1 << 25) - 1;

    // Wall with a few tiles (from doc example fig 3: indices 0,2,3,4,7,8,12)
    static final int PK_WALL_EXAMPLE;
    static {
        int w = PkWall.EMPTY;
        for (int idx : new int[]{0, 2, 3, 4, 7, 8, 12}) {
            var line = TileDestination.Pattern.ALL.get(idx / 5);
            int col = idx % 5;
            var color = PkWall.colorAt(line, col);
            w = PkWall.withTileAt(w, line, color);
        }
        PK_WALL_EXAMPLE = w;
    }

    // ==================== POINTS ====================
    static final int POINTS_0 = 0;
    static final int POINTS_10 = 10;
    static final int POINTS_50 = 50;
    static final int POINTS_100 = 100;
    static final int POINTS_240 = 240; // max

    // ==================== PACKED TILE SETS ====================
    static final int PK_TS_EMPTY = PkTileSet.EMPTY;
    static final int PK_TS_FULL = PkTileSet.FULL;
    static final int PK_TS_FULL_COLORED = PkTileSet.FULL_COLORED;
    static final int PK_TS_MARKER_ONLY = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);

    // 4 tiles for a factory: 2A + 2B
    static final int PK_TS_2A2B = PkTileSet.union(PkTileSet.of(2, A), PkTileSet.of(2, B));
    // 4A
    static final int PK_TS_4A = PkTileSet.of(4, A);
    // 2A + 1C + 1E
    static final int PK_TS_2A1C1E = PkTileSet.union(
            PkTileSet.union(PkTileSet.of(2, A), PkTileSet.of(1, C)),
            PkTileSet.of(1, E));
    // 1A + 1B + 1C + 1D
    static final int PK_TS_ABCD = PkTileSet.union(
            PkTileSet.union(PkTileSet.of(1, A), PkTileSet.of(1, B)),
            PkTileSet.union(PkTileSet.of(1, C), PkTileSet.of(1, D)));

    // ==================== INITIAL PLAYER STATES ====================
    static final ImmutableIntArray INITIAL_PS_2P = PkPlayerStates.initial(GAME_2P);
    static final ImmutableIntArray INITIAL_PS_3P = PkPlayerStates.initial(GAME_3P);
    static final ImmutableIntArray INITIAL_PS_4P = PkPlayerStates.initial(GAME_4P);

    // ==================== INITIAL GAME STATES ====================
    static final ImmutableGameState INITIAL_GS_2P = ImmutableGameState.initial(GAME_2P);
    static final ImmutableGameState INITIAL_GS_3P = ImmutableGameState.initial(GAME_3P);
    static final ImmutableGameState INITIAL_GS_4P = ImmutableGameState.initial(GAME_4P);

    // =====================================================
// FORMATS RAPPEL
// =====================================================
//
// PkPatterns (int, 30 bits utiles) :
//   Ligne 1: bits[0-2]=count, bits[3-5]=colorIdx
//   Ligne 2: bits[6-8]=count, bits[9-11]=colorIdx
//   Ligne 3: bits[12-14]=count, bits[15-17]=colorIdx
//   Ligne 4: bits[18-20]=count, bits[21-23]=colorIdx
//   Ligne 5: bits[24-26]=count, bits[27-29]=colorIdx
//   colorIdx: A=0, B=1, C=2, D=3, E=4
//
// PkFloor (int, 24 bits utiles) :
//   bits[0-2]=size (0-7)
//   bits[3-5]=tile0 kind index
//   bits[6-8]=tile1 kind index
//   ...
//   kindIdx: A=0, B=1, C=2, D=3, E=4, FIRST_PLAYER_MARKER=5
//
// PkWall (int, 25 bits) :
//   bit i = 1 ssi case i occupée (0-24, gauche->droite, haut->bas)
//
// PkTileSet (int, 32 bits) :
//   0M_0EEEEE_0DDDDD_0CCCCC_0BBBBB_0AAAAA
//   bits[0-4]=countA, bit5=0, bits[6-10]=countB, bit11=0, ...
//   bit30=marker, bit31=0
//
// PkIntSet32 (int) : bit i = 1 ssi i dans l'ensemble
//
// PkPlayerStates (int[4*n]) :
//   [0]=patterns P1, [1]=floor P1, [2]=wall P1, [3]=points P1,
//   [4]=patterns P2, [5]=floor P2, [6]=wall P2, [7]=points P2, ...

// =====================================================
// PATTERNS - valeurs manuelles
// =====================================================

    // Tout vide
    static final int PAT_EMPTY = 0b00_000_000_000_000_000_000_000_000_000_000;
//                                 L5       L4       L3       L2       L1

    // L1: 1xA(0)  => count=1, color=0
// L2 à L5 vides
    static final int PAT_1A_L1 = 0b00_000_000_000_000_000_000_000_000_000_001;
//                                                                 col cnt

    // L1: 1xC(2)
    static final int PAT_1C_L1 = 0b00_000_000_000_000_000_000_000_000_010_001;
//                                                              color=2 cnt=1

    // L2: 2xB(1)  => bits[6-8]=2, bits[9-11]=1
    static final int PAT_2B_L2 = 0b00_000_000_000_000_000_000_001_010_000_000;
//                                                         col=1 cnt=2

    // L3: 3xC(2) (pleine) => bits[12-14]=3, bits[15-17]=2
    static final int PAT_3C_L3 = 0b00_000_000_000_000_010_011_000_000_000_000;
//                                              col=2 cnt=3

    // L5: 5xE(4) (pleine) => bits[24-26]=5=101, bits[27-29]=4=100
    static final int PAT_5E_L5 = 0b00_100_101_000_000_000_000_000_000_000_000;
//                              col=4 cnt=5

    // L1:1xA, L2:2xB, L3:3xC, L4:4xD(3), L5:5xE  (tout plein)
    static final int PAT_ALL_FULL =
            0b00_100_101_011_100_010_011_001_010_000_001;
//          L5e=5  L4d=4  L3c=3  L2b=2     L1a=1

    // Exemple de l'énoncé fig.1: L1:1xC, L2:2xA, L3:3xA(vide en fait non),
// recalculons: [C, AA, ...3, EEE., .....]
// L1: 1xC(2) => 010_001
// L2: 2xA(0) => 000_010
// L3: 3xA(0)? Non, regardons: "000 011" => 3x col0=A?
// Prenons l'exemple brut de l'énoncé:
// 00  000 000  100 011  000 011  000 010  010 001
    static final int PAT_EXAMPLE_FIG1 = 0b00_000_000_100_011_000_011_000_010_010_001;

// =====================================================
// FLOOR - valeurs manuelles
// =====================================================

    // Vide: size=0
    static final int FLOOR_EMPTY = 0b000_000_000_000_000_000_000_000;
//                                t6  t5  t4  t3  t2  t1  t0  size

    // 1 tuile A(0): size=1, t0=0
    static final int FLOOR_1A = 0b000_000_000_000_000_000_000_001;
//                                                      A=0 size=1

    // 1 marqueur(5): size=1, t0=5=101
    static final int FLOOR_1M = 0b000_000_000_000_000_000_101_001;

    // Exemple énoncé fig.2: [FIRST_PLAYER_MARKER, B, B]
// size=3, t0=5(marker), t1=1(B), t2=1(B)
    static final int FLOOR_M_B_B = 0b000_000_000_000_001_001_101_011;

    // 7 tuiles (plein): 3xA, 2xB, 2xC => trié par couleur
// t0=A(0), t1=A(0), t2=A(0), t3=B(1), t4=B(1), t5=C(2), t6=C(2), size=7
    static final int FLOOR_FULL_3A2B2C =
            0b010_010_001_001_000_000_000_111;
//       C=2 C=2 B=1 B=1 A=0 A=0 A=0 size=7

// =====================================================
// WALL - valeurs manuelles
// =====================================================

    static final int WALL_EMPTY = 0;
    static final int WALL_FULL = (1 << 25) - 1;  // 0x1FFFFFF

    // Case 0 seule (haut-gauche)
    static final int WALL_CELL0 = 1;            // 0b1

    // Première ligne pleine (cases 0-4)
    static final int WALL_ROW0 = 0b11111;       // 0x1F

    // Dernière ligne pleine (cases 20-24)
    static final int WALL_ROW4 = 0b11111_00000_00000_00000_00000;

    // Colonne 0 pleine
    static final int WALL_COL0 = 0b00001_00001_00001_00001_00001;

    // Exemple fig.3: cases 0,2,3,4,7,8,12
    static final int WALL_FIG3 = 0b00000_00000_00001_00011_00011_10_1;
    // Vérifions: bit0=1, bit2=1, bit3=1, bit4=1, bit7=1, bit8=1, bit12=1
// = (1<<0)|(1<<2)|(1<<3)|(1<<4)|(1<<7)|(1<<8)|(1<<12)
// = 1 + 4 + 8 + 16 + 128 + 256 + 4096 = 4509
    static final int WALL_FIG3_HEX = 0x119D;    // = 4509

    // Diagonale couleur A: cases (0, 6, 12, 18, 24)
    static final int WALL_DIAG_A = (1) | (1 << 6) | (1 << 12) | (1 << 18) | (1 << 24);

// =====================================================
// TILE SETS - valeurs manuelles
// =====================================================
// Format: 0M_0EEEEE_0DDDDD_0CCCCC_0BBBBB_0AAAAA

    static final int TS_EMPTY = 0;

    // 1 marqueur seul
    static final int TS_MARKER = 1 << 30;  // 0x40000000

    // 20 de chaque + marqueur (FULL)
// 20 = 10100 en binaire
// 0b0_1_0_10100_0_10100_0_10100_0_10100_0_10100
//    static final int TS_FULL = 0x40_A5_29_4A_14;
    // Plus lisible:
    static final int TS_FULL_V2 =
            (20) | (20 << 6) | (20 << 12) | (20 << 18) | (20 << 24) | (1 << 30);

    // 20 de chaque sans marqueur
    static final int TS_FULL_COLORED = TS_FULL_V2 & ~(1 << 30);

    // 2xA + 2xB = {2*A, 2*B}
    static final int TS_2A_2B = 2 | (2 << 6);  // A=2, B=2

    // 4xA
    static final int TS_4A = 4;

    // 2xA + 1xC + 1xE
    static final int TS_2A_1C_1E = 2 | (1 << 12) | (1 << 24);

// =====================================================
// PLAYER STATES - tableaux bruts
// =====================================================

    // 2 joueurs, tout à zéro
    static final int[] PS_2P_ZEROS = {0, 0, 0, 0, 0, 0, 0, 0};

    // 2 joueurs: P1 a 1xA sur L1, floor vide, wall vide, 10 pts
//            P2 tout vide, 0 pts
    static final int[] PS_2P_SIMPLE = {
            PAT_1A_L1, FLOOR_EMPTY, WALL_EMPTY, 10,  // P1
            PAT_EMPTY, FLOOR_EMPTY, WALL_EMPTY, 0    // P2
    };

    // 2 joueurs mid-game
    static final int[] PS_2P_MID = {
            PAT_3C_L3, FLOOR_M_B_B, WALL_FIG3_HEX, 25,   // P1
            PAT_2B_L2, FLOOR_1A,    WALL_CELL0,     12    // P2
    };

// =====================================================
// SOURCES DE TUILES - tableaux bruts (2P = 6 sources)
// =====================================================

    // Initial: central=marker, fabriques vides
    static final int[] SRCS_INITIAL_2P = {TS_MARKER, 0, 0, 0, 0, 0};

    // Fabriques remplies (exemple §2.2.1 de l'énoncé)
    static final int[] SRCS_FILLED_2P = {
            TS_MARKER,    // source 0: central
            TS_2A_2B,     // source 1
            TS_2A_2B,     // source 2
            TS_2A_2B,     // source 3
            TS_2A_1C_1E,  // source 4
            TS_4A         // source 5
    };
    // => unique sources = {1, 4, 5} => bits 1,4,5 = 0b110010 = 0x32
    static final int UNIQUE_1_4_5 = (1 << 1) | (1 << 4) | (1 << 5);

    // Toutes vides (round over)
    static final int[] SRCS_ALL_EMPTY_2P = {0, 0, 0, 0, 0, 0};

    @Test
    void initial() {
        assertArrayEquals(INITIAL_PS_2P.toArray(), new int[]{PkPatterns.EMPTY, PkFloor.EMPTY, PkWall.EMPTY, 0, PkPatterns.EMPTY, PkFloor.EMPTY, PkWall.EMPTY, 0});
        assertArrayEquals(INITIAL_PS_3P.toArray(), new int[]{PkPatterns.EMPTY, PkFloor.EMPTY, PkWall.EMPTY, 0, PkPatterns.EMPTY, PkFloor.EMPTY, PkWall.EMPTY, 0, PkPatterns.EMPTY, PkFloor.EMPTY, PkWall.EMPTY, 0});
        assertArrayEquals(INITIAL_PS_4P.toArray(), new int[]{PkPatterns.EMPTY, PkFloor.EMPTY, PkWall.EMPTY, 0, PkPatterns.EMPTY, PkFloor.EMPTY, PkWall.EMPTY, 0, PkPatterns.EMPTY, PkFloor.EMPTY, PkWall.EMPTY, 0, PkPatterns.EMPTY, PkFloor.EMPTY, PkWall.EMPTY, 0});

    }

    @Test
    void pkPatterns() {

    }


    @Test
    void pkFloor() {
    }

    @Test
    void pkWall() {
    }

    @Test
    void points() {
    }

    @Test
    void setPkPatterns() {
    }

    @Test
    void setPkFloor() {
    }

    @Test
    void setPkWall() {
    }

    @Test
    void addPoints() {

    }
}