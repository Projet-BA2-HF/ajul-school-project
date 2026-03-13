    package ch.epfl.ajul.gamestate.packed;

    import ch.epfl.ajul.TileDestination;
    import ch.epfl.ajul.TileKind;
    import org.junit.jupiter.api.Test;

    import static ch.epfl.ajul.TileDestination.Pattern.*;
    import static ch.epfl.ajul.TileKind.Colored.*;
    import static org.junit.jupiter.api.Assertions.*;

    class OurPkWallTest {

    // Layout de référence :
    // col:     0   1   2   3   4
    // ligne 0: 0   1   2   3   4
    // ligne 1: 5   6   7   8   9
    // ligne 2: 10  11  12  13  14
    // ligne 3: 15  16  17  18  19
    // ligne 4: 20  21  22  23  24

    // ═══════════════════════════════════════════════
    // MURS DE BASE
    // ═══════════════════════════════════════════════

        int wallEmpty = 0b00000_00000_00000_00000_00000;
        int wallFull  = 0b11111_11111_11111_11111_11111;

    // ═══════════════════════════════════════════════
    // LIGNES COMPLÈTES (pour isRowFull, hasFullRow)
    // ═══════════════════════════════════════════════

        int wallRow0Full = 0b00000_00000_00000_00000_11111;
        int wallRow1Full = 0b00000_00000_00000_11111_00000;
        int wallRow2Full = 0b00000_00000_11111_00000_00000;
        int wallRow3Full = 0b00000_11111_00000_00000_00000;
        int wallRow4Full = 0b11111_00000_00000_00000_00000;

        // Deux lignes complètes
        int wallRow0AndRow4Full = 0b11111_00000_00000_00000_11111;

    // ═══════════════════════════════════════════════
    // COLONNES COMPLÈTES (pour isColumnFull)
    // ═══════════════════════════════════════════════
    //                        ligne4 ligne3 ligne2 ligne1 ligne0

        int wallCol0Full = 0b00001_00001_00001_00001_00001;
        int wallCol1Full = 0b00010_00010_00010_00010_00010;
        int wallCol2Full = 0b00100_00100_00100_00100_00100;
        int wallCol3Full = 0b01000_01000_01000_01000_01000;
        int wallCol4Full = 0b10000_10000_10000_10000_10000;

    // ═══════════════════════════════════════════════
    // TUILES ISOLÉES (hGroup=1, vGroup=1 → p=1)
    // ═══════════════════════════════════════════════

        int wallTileIsolatedTopLeft     = 0b00000_00000_00000_00000_00001; // case 0
        int wallTileIsolatedTopRight    = 0b00000_00000_00000_00000_10000; // case 4
        int wallTileIsolatedCenter      = 0b00000_00000_00100_00000_00000; // case 12
        int wallTileIsolatedBottomLeft  = 0b00001_00000_00000_00000_00000; // case 20
        int wallTileIsolatedBottomRight = 0b10000_00000_00000_00000_00000; // case 24

    // ═══════════════════════════════════════════════
    // GROUPES HORIZONTAUX (pour hGroupSize)
    // ═══════════════════════════════════════════════

        // Groupe h=2 ligne 0, cases 0,1
        int wallHGroup2_Row0_Cols01     = 0b00000_00000_00000_00000_00011;

        // Groupe h=2 ligne 0, cases 3,4 (à droite)
        int wallHGroup2_Row0_Cols34     = 0b00000_00000_00000_00000_11000;

        // Groupe h=3 ligne 1, cases 5,6,7
        int wallHGroup3_Row1_Cols012    = 0b00000_00000_00000_00111_00000;

        // Groupe h=4 ligne 2, cases 10,11,12,13
        int wallHGroup4_Row2_Cols0123   = 0b00000_00000_01111_00000_00000;

        // Groupe h=5 ligne 3 complète (cas limite)
        int wallHGroup5_Row3_Full       = 0b00000_11111_00000_00000_00000;

        // Deux groupes h séparés sur ligne 0 : cases 0,1 et 3,4 (sans la case 2)
        int wallHTwoGroups_Row0_Separated = 0b00000_00000_00000_00000_11011;

    // ═══════════════════════════════════════════════
    // GROUPES VERTICAUX (pour vGroupSize)
    // ═══════════════════════════════════════════════

        // Groupe v=2 colonne 0, cases 0,5
        int wallVGroup2_Col0_Rows01     = 0b00000_00000_00000_00001_00001;

        // Groupe v=3 colonne 2, cases 2,7,12
        int wallVGroup3_Col2_Rows012    = 0b00000_00000_00100_00100_00100;

        // Groupe v=4 colonne 4, cases 4,9,14,19
        int wallVGroup4_Col4_Rows0123   = 0b00000_10000_10000_10000_10000;

        // Groupe v=5 colonne 1 complète (cas limite)
        int wallVGroup5_Col1_Full       = 0b00010_00010_00010_00010_00010;

        // Deux groupes v séparés sur colonne 0 : cases 0,5 et 15,20 (sans la case 10)
        int wallVTwoGroups_Col0_Separated = 0b00001_00001_00000_00001_00001;

    // ═══════════════════════════════════════════════
    // GROUPES MIXTES (hGroup>1 ET vGroup>1 → p = h+v)
    // ═══════════════════════════════════════════════

        // Tuile 6 avec voisin droite (7) et voisin bas (11) → h=2, v=2 → p=4
        int wallHGroup2VGroup2_Tile6    = 0b00000_00000_00010_01100_00000;

        // Tuile 12 avec voisins h (11,13) et voisins v (7,17) → h=3, v=3 → p=6
        int wallHGroup3VGroup3_Cross12  = 0b00000_00010_01110_00100_00000;

        // Tuile 24 coin bas-droit avec h=3 (22,23,24) et v=3 (14,19,24) → p=6
        int wallHGroup3VGroup3_Corner24 = 0b11100_10000_10000_00000_00000;

    // ═══════════════════════════════════════════════
    // EXEMPLES DU SUJET
    // ═══════════════════════════════════════════════

        // Figure 3 : cases 0,2,3,4,7,8,12
    //    int wallFig3 = 0b00000_00000_00100_00001_10011101; // incorrect, voir ci-dessous
    //    int wallFig3Corrected = 0b00000_00000_00100_00110_00011101;

        // Figure 4 : cases 0,2,3,6,11,13,18,20,21,22,23,24
    //    int wallFig4 = 0b11111_00100_00101_01000_00001101;

        int wallAllA = 0b10000_01000_00100_00010_00001;
        int wallAllB = 0b00001_10000_01000_00100_00010;
        int wallAllC = 0b00010_00001_10000_01000_00100;
        int wallAllD = 0b00100_00010_00001_10000_01000;
        int wallAllE = 0b01000_00100_00010_00001_10000;

        int wallAllAPlusExtras = 0b10010_01000_00101_00010_10001;
        int wallAllBPlusExtras = 0b01001_10000_01010_00100_00011;
        int wallAllCPlusExtras = 0b10010_00001_10000_01010_01100;
        int wallAllDPlusExtras = 0b00101_00010_00001_10100_01100;
        int wallAllEPlusExtras = 0b01100_00100_10010_00001_10010;


        @Test
        void indexOf() {
            // Couleur A : cols 0,1,2,3,4 pour lignes 0,1,2,3,4
            assertEquals(PkWall.indexOf(PATTERN_1, A), 0);   // ligne0, col0 → 0*5+0
            assertEquals(PkWall.indexOf(PATTERN_2, A), 6);   // ligne1, col1 → 1*5+1
            assertEquals(PkWall.indexOf(PATTERN_3, A), 12);  // ligne2, col2 → 2*5+2
            assertEquals(PkWall.indexOf(PATTERN_4, A), 18);  // ligne3, col3 → 3*5+3
            assertEquals(PkWall.indexOf(PATTERN_5, A), 24);  // ligne4, col4 → 4*5+4

    // Couleur B
            assertEquals(PkWall.indexOf(PATTERN_1, B), 1);   // ligne0, col1 → 0*5+1
            assertEquals(PkWall.indexOf(PATTERN_2, B), 7);   // ligne1, col2 → 1*5+2
            assertEquals(PkWall.indexOf(PATTERN_3, B), 13);  // ligne2, col3 → 2*5+3
            assertEquals(PkWall.indexOf(PATTERN_4, B), 19);  // ligne3, col4 → 3*5+4
            assertEquals(PkWall.indexOf(PATTERN_5, B), 20);  // ligne4, col0 → 4*5+0

    // Couleur C
            assertEquals(PkWall.indexOf(PATTERN_1, C), 2);   // ligne0, col2
            assertEquals(PkWall.indexOf(PATTERN_2, C), 8);   // ligne1, col3
            assertEquals(PkWall.indexOf(PATTERN_3, C), 14);  // ligne2, col4
            assertEquals(PkWall.indexOf(PATTERN_4, C), 15);  // ligne3, col0
            assertEquals(PkWall.indexOf(PATTERN_5, C), 21);  // ligne4, col1

    // Couleur D
            assertEquals(PkWall.indexOf(PATTERN_1, D), 3);   // ligne0, col3
            assertEquals(PkWall.indexOf(PATTERN_2, D), 9);   // ligne1, col4
            assertEquals(PkWall.indexOf(PATTERN_3, D), 10);  // ligne2, col0
            assertEquals(PkWall.indexOf(PATTERN_4, D), 16);  // ligne3, col1
            assertEquals(PkWall.indexOf(PATTERN_5, D), 22);  // ligne4, col2

    // Couleur E
            assertEquals(PkWall.indexOf(PATTERN_1, E), 4);   // ligne0, col4
            assertEquals(PkWall.indexOf(PATTERN_2, E), 5);   // ligne1, col0
            assertEquals(PkWall.indexOf(PATTERN_3, E), 11);  // ligne2, col1
            assertEquals(PkWall.indexOf(PATTERN_4, E), 17);  // ligne3, col2
            assertEquals(PkWall.indexOf(PATTERN_5, E), 23);  // ligne4, col3
        }

        @Test
        void column() {
            assertEquals(PkWall.column(PATTERN_1, A), 0);
            assertEquals(PkWall.column(PATTERN_2, A), 1);
            assertEquals(PkWall.column(PATTERN_3, A), 2);
            assertEquals(PkWall.column(TileDestination.Pattern.PATTERN_4, A), 3);
            assertEquals(PkWall.column(TileDestination.Pattern.PATTERN_5, A), 4);
            assertEquals(PkWall.column(PATTERN_1, B), 1);
            assertEquals(PkWall.column(PATTERN_2, B), 2);
            assertEquals(PkWall.column(PATTERN_3, B), 3);
            assertEquals(PkWall.column(TileDestination.Pattern.PATTERN_4, B), 4);
            assertEquals(PkWall.column(TileDestination.Pattern.PATTERN_5, B), 0);
            assertEquals(PkWall.column(PATTERN_1, C), 2);
            assertEquals(PkWall.column(PATTERN_2, C), 3);
            assertEquals(PkWall.column(PATTERN_3, C), 4);
            assertEquals(PkWall.column(TileDestination.Pattern.PATTERN_4, C), 0);
            assertEquals(PkWall.column(TileDestination.Pattern.PATTERN_5, C), 1);
            assertEquals(PkWall.column(PATTERN_1, TileKind.Colored.D), 3);
            assertEquals(PkWall.column(PATTERN_2, TileKind.Colored.D), 4);
            assertEquals(PkWall.column(PATTERN_3, TileKind.Colored.D), 0);
            assertEquals(PkWall.column(TileDestination.Pattern.PATTERN_4, TileKind.Colored.D), 1);
            assertEquals(PkWall.column(TileDestination.Pattern.PATTERN_5, TileKind.Colored.D), 2);
            assertEquals(PkWall.column(PATTERN_1, TileKind.Colored.E), 4);
            assertEquals(PkWall.column(PATTERN_2, TileKind.Colored.E), 0);
            assertEquals(PkWall.column(PATTERN_3, TileKind.Colored.E), 1);
            assertEquals(PkWall.column(TileDestination.Pattern.PATTERN_4, TileKind.Colored.E), 2);
            assertEquals(PkWall.column(TileDestination.Pattern.PATTERN_5, TileKind.Colored.E), 3);
        }

        @Test
        void colorAt() {
            assertEquals(PkWall.colorAt(PATTERN_1, 0), A);
            assertEquals(PkWall.colorAt(PATTERN_1, 1), B);
            assertEquals(PkWall.colorAt(PATTERN_1, 2), C);
            assertEquals(PkWall.colorAt(PATTERN_1, 3), D);
            assertEquals(PkWall.colorAt(PATTERN_1, 4), E);
            assertEquals(PkWall.colorAt(PATTERN_2, 0), E);
            assertEquals(PkWall.colorAt(PATTERN_2, 1), A);
            assertEquals(PkWall.colorAt(PATTERN_2, 2), B);
            assertEquals(PkWall.colorAt(PATTERN_2, 3), C);
            assertEquals(PkWall.colorAt(PATTERN_2, 4), D);
            assertEquals(PkWall.colorAt(PATTERN_3, 0), D);
            assertEquals(PkWall.colorAt(PATTERN_3, 1), E);
            assertEquals(PkWall.colorAt(PATTERN_3, 2), A);
            assertEquals(PkWall.colorAt(PATTERN_3, 3), B);
            assertEquals(PkWall.colorAt(PATTERN_3, 4), C);
            assertEquals(PkWall.colorAt(PATTERN_4, 0), C);
            assertEquals(PkWall.colorAt(PATTERN_4, 1), D);
            assertEquals(PkWall.colorAt(PATTERN_4, 2), E);
            assertEquals(PkWall.colorAt(PATTERN_4, 3), A);
            assertEquals(PkWall.colorAt(PATTERN_4, 4), B);
            assertEquals(PkWall.colorAt(PATTERN_5, 0), B);
            assertEquals(PkWall.colorAt(PATTERN_5, 1), C);
            assertEquals(PkWall.colorAt(PATTERN_5, 2), D);
            assertEquals(PkWall.colorAt(PATTERN_5, 3), E);
            assertEquals(PkWall.colorAt(PATTERN_5, 4), A);
        }

        @Test
        void withTileAt() {
            assertEquals(PkWall.withTileAt(wallEmpty, PATTERN_1, A), wallTileIsolatedTopLeft);
            assertEquals(PkWall.withTileAt(wallEmpty, PATTERN_1, E), wallTileIsolatedTopRight);
            assertEquals(PkWall.withTileAt(wallEmpty, PATTERN_3, A), wallTileIsolatedCenter);
            assertEquals(PkWall.withTileAt(wallEmpty, PATTERN_5, A), wallTileIsolatedBottomRight);
            assertEquals(PkWall.withTileAt(wallEmpty, PATTERN_5, B), wallTileIsolatedBottomLeft);

            // Ajouter une tuile à un mur déjà occupé
            int wallWithTwoTiles = PkWall.withTileAt(wallTileIsolatedCenter, PATTERN_3, B);
            assertEquals(wallWithTwoTiles, wallTileIsolatedCenter | (1 << PkWall.indexOf(PATTERN_3, B)));

            int wallWithThreeTiles = PkWall.withTileAt(wallWithTwoTiles, PATTERN_3, C);
            assertEquals(wallWithThreeTiles, wallWithTwoTiles | (1 << PkWall.indexOf(PATTERN_3, C)));

            assertEquals(PkWall.withTileAt(wallWithTwoTiles, PATTERN_3, C), wallWithTwoTiles | (1 << PkWall.indexOf(PATTERN_3, C)));
            assertEquals(PkWall.withTileAt(wallWithTwoTiles, PATTERN_3, B), wallWithTwoTiles); // ajouter une tuile déjà présente ne change rien

            int empty = wallEmpty;
            while (empty != wallFull) {
                for(var line : TileDestination.Pattern.ALL){
                    for(var color : TileKind.Colored.ALL){
                        if((empty & (1 << PkWall.indexOf(line, color))) == 0){
                            empty = PkWall.withTileAt(empty, line, color);
                            assertTrue(PkWall.hasTileAt(empty, line, color));
                        }
                    }
                }
            }
            assertEquals(empty, wallFull);
        }

        @Test
        void hasTileAt() {
                assertFalse(PkWall.hasTileAt(wallEmpty, PATTERN_1, A));
                assertTrue(PkWall.hasTileAt(wallTileIsolatedTopLeft, PATTERN_1, A));
                assertFalse(PkWall.hasTileAt(wallTileIsolatedTopLeft, PATTERN_1, B));
                assertFalse(PkWall.hasTileAt(wallTileIsolatedTopLeft, PATTERN_2, A));
                assertFalse(PkWall.hasTileAt(wallTileIsolatedTopLeft, PATTERN_2, B));

                // Tester un mur complet
                for(var line : TileDestination.Pattern.ALL){
                    for(var color : TileKind.Colored.ALL){
                        assertTrue(PkWall.hasTileAt(wallFull, line, color));
                    }
                }

            for(var line : TileDestination.Pattern.ALL){
                for(var color : TileKind.Colored.ALL){
                    assertFalse(PkWall.hasTileAt(wallEmpty, line, color));
                }
            }
        }

        @Test
        void hGroupSize() {
            //assertEquals(0, PkWall.hGroupSize(wallEmpty, PATTERN_1, A));
            assertEquals(1, PkWall.hGroupSize(wallTileIsolatedTopLeft, PATTERN_1, A));
            //assertEquals(0, PkWall.hGroupSize(wallTileIsolatedTopLeft, PATTERN_1, B));
            //assertEquals(0, PkWall.hGroupSize(wallTileIsolatedTopLeft, PATTERN_2, A));
            //assertEquals(0, PkWall.hGroupSize(wallTileIsolatedTopLeft, PATTERN_2, B));
            assertEquals(2, PkWall.hGroupSize(wallHGroup2_Row0_Cols01, PATTERN_1, A));
            assertEquals(2, PkWall.hGroupSize(wallHGroup2_Row0_Cols34, PATTERN_1, D));
            //assertEquals(0, PkWall.hGroupSize(wallHGroup2_Row0_Cols34, PATTERN_1, A));
            assertEquals(3, PkWall.hGroupSize(wallHGroup3_Row1_Cols012, PATTERN_2, A));
            assertEquals(4, PkWall.hGroupSize(wallHGroup4_Row2_Cols0123, PATTERN_3, A));
            assertEquals(5, PkWall.hGroupSize(wallHGroup5_Row3_Full, PATTERN_4, A));
            assertEquals(2, PkWall.hGroupSize(wallHTwoGroups_Row0_Separated, PATTERN_1, A));
            //assertEquals(0, PkWall.hGroupSize(wallHTwoGroups_Row0_Separated, PATTERN_1, C));
            assertEquals(2, PkWall.hGroupSize(wallHTwoGroups_Row0_Separated, PATTERN_1, E));
            for(var line : TileDestination.Pattern.ALL){
                for(var color : TileKind.Colored.ALL){
                    assertEquals(5, PkWall.hGroupSize(wallFull, line, color));
                }
            }
            //for(var line : TileDestination.Pattern.ALL){
            //for(var color : TileKind.Colored.ALL){
            //assertEquals(0, PkWall.hGroupSize(wallEmpty, line, color));


        }

        @Test
        void vGroupSize() {
            //assertEquals(0, PkWall.vGroupSize(wallEmpty, PATTERN_1, A));
            assertEquals(1, PkWall.vGroupSize(wallTileIsolatedTopLeft, PATTERN_1, A));
            //assertEquals(0, PkWall.vGroupSize(wallTileIsolatedTopLeft, PATTERN_1, B));
            //assertEquals(0, PkWall.vGroupSize(wallTileIsolatedTopLeft, PATTERN_2, A));
            //assertEquals(0, PkWall.vGroupSize(wallTileIsolatedTopLeft, PATTERN_2, B));
            assertEquals(2, PkWall.vGroupSize(wallVGroup2_Col0_Rows01, PATTERN_1, A));
            assertEquals(3, PkWall.vGroupSize(wallVGroup3_Col2_Rows012, PATTERN_3, A));
            //assertEquals(0, PkWall.vGroupSize(wallVGroup4_Col4_Rows0123, PATTERN_5, E));
            assertEquals(4, PkWall.vGroupSize(wallVGroup4_Col4_Rows0123, PATTERN_4,B));
            assertEquals(5, PkWall.vGroupSize(wallVGroup5_Col1_Full, PATTERN_2, A));
            assertEquals(2, PkWall.vGroupSize(wallVTwoGroups_Col0_Separated, PATTERN_1, A));
            //assertEquals(0, PkWall.vGroupSize(wallVTwoGroups_Col0_Separated, PATTERN_3, D));
            assertEquals(2, PkWall.vGroupSize(wallVTwoGroups_Col0_Separated, PATTERN_4, C));
            for(var line : TileDestination.Pattern.ALL){
                for(var color : TileKind.Colored.ALL){
                    assertEquals(5, PkWall.vGroupSize(wallFull, line, color));
                }
            }

        }

        @Test
        void isRowFull() {
            assertFalse(PkWall.isRowFull(wallEmpty, PATTERN_1));
            assertTrue(PkWall.isRowFull(wallRow0Full, PATTERN_1));
            assertFalse(PkWall.isRowFull(wallRow0Full, PATTERN_2));
            assertTrue(PkWall.isRowFull(wallRow1Full, PATTERN_2));
            assertTrue(PkWall.isRowFull(wallRow2Full, PATTERN_3));
            assertTrue(PkWall.isRowFull(wallRow3Full, PATTERN_4));
            assertTrue(PkWall.isRowFull(wallRow4Full, PATTERN_5));
            assertFalse(PkWall.isRowFull(wallRow0AndRow4Full, PATTERN_2));
            assertTrue(PkWall.isRowFull(wallRow0AndRow4Full, PATTERN_1));
            assertTrue(PkWall.isRowFull(wallRow0AndRow4Full, PATTERN_5));
            for(var line : TileDestination.Pattern.ALL){
                assertTrue(PkWall.isRowFull(wallFull, line));
                assertFalse(PkWall.isRowFull(wallEmpty, line));
            }
        }

        @Test
        void isColumnFull() {
            assertFalse(PkWall.isColumnFull(wallEmpty, 0));
            assertTrue(PkWall.isColumnFull(wallCol0Full, 0));
            assertFalse(PkWall.isColumnFull(wallCol0Full, 1));
            assertTrue(PkWall.isColumnFull(wallCol1Full, 1));
            assertTrue(PkWall.isColumnFull(wallCol2Full, 2));
            assertTrue(PkWall.isColumnFull(wallCol3Full, 3));
            assertTrue(PkWall.isColumnFull(wallCol4Full, 4));
            for(int col = 0; col < PkWall.WALL_WIDTH; col++){
                assertTrue(PkWall.isColumnFull(wallFull, col));
                assertFalse(PkWall.isColumnFull(wallEmpty, col));
            }
        }

        @Test
        void isColorFull() {
            assertFalse(PkWall.isColorFull(wallEmpty, A));
            assertFalse(PkWall.isColorFull(wallEmpty, B));
            assertFalse(PkWall.isColorFull(wallEmpty, C));
            assertFalse(PkWall.isColorFull(wallEmpty, D));
            assertFalse(PkWall.isColorFull(wallEmpty, E));
            assertTrue(PkWall.isColorFull(wallFull, A));
            assertTrue(PkWall.isColorFull(wallFull, B));
            assertTrue(PkWall.isColorFull(wallFull, C));
            assertTrue(PkWall.isColorFull(wallFull, D));
            assertTrue(PkWall.isColorFull(wallFull, E));
            assertFalse(PkWall.isColorFull(wallHGroup2_Row0_Cols01, A));
            assertFalse(PkWall.isColorFull(wallHGroup2_Row0_Cols01, B));
            assertFalse(PkWall.isColorFull(wallHGroup2_Row0_Cols01, C));
            assertFalse(PkWall.isColorFull(wallHGroup2_Row0_Cols01, D));
            assertFalse(PkWall.isColorFull(wallHGroup2_Row0_Cols01, E));
            assertFalse(PkWall.isColorFull(wallHGroup2_Row0_Cols34, A));
            assertFalse(PkWall.isColorFull(wallHGroup2_Row0_Cols34, B));
            assertFalse(PkWall.isColorFull(wallHGroup2_Row0_Cols34, C));
            assertFalse(PkWall.isColorFull(wallHGroup2_Row0_Cols34, D));
            assertFalse(PkWall.isColorFull(wallHGroup2_Row0_Cols34, E));
            assertFalse(PkWall.isColorFull(wallHGroup3_Row1_Cols012, A));
            assertFalse(PkWall.isColorFull(wallHGroup3_Row1_Cols012, B));
            assertFalse(PkWall.isColorFull(wallHGroup3_Row1_Cols012, C));
            assertFalse(PkWall.isColorFull(wallHGroup3_Row1_Cols012, D));
            assertFalse(PkWall.isColorFull(wallHGroup3_Row1_Cols012, E));
            assertFalse(PkWall.isColorFull(wallHGroup4_Row2_Cols0123, A));
            assertFalse(PkWall.isColorFull(wallHGroup4_Row2_Cols0123, B));
            assertFalse(PkWall.isColorFull(wallHGroup4_Row2_Cols0123, C));
            assertFalse(PkWall.isColorFull(wallHGroup4_Row2_Cols0123, D));
            assertFalse(PkWall.isColorFull(wallHGroup4_Row2_Cols0123, E));
            assertFalse(PkWall.isColorFull(wallHGroup5_Row3_Full, A));
            assertFalse(PkWall.isColorFull(wallHGroup5_Row3_Full, B));
            assertFalse(PkWall.isColorFull(wallHGroup5_Row3_Full, C));
            assertFalse(PkWall.isColorFull(wallHGroup5_Row3_Full, D));
            assertFalse(PkWall.isColorFull(wallHGroup5_Row3_Full, E));
            assertFalse(PkWall.isColorFull(wallHTwoGroups_Row0_Separated, A));
            assertFalse(PkWall.isColorFull(wallHTwoGroups_Row0_Separated, B));
            assertFalse(PkWall.isColorFull(wallHTwoGroups_Row0_Separated, C));
            assertFalse(PkWall.isColorFull(wallHTwoGroups_Row0_Separated, D));
            assertFalse(PkWall.isColorFull(wallHTwoGroups_Row0_Separated, E));
            assertFalse(PkWall.isColorFull(wallVGroup2_Col0_Rows01, A));
            assertFalse(PkWall.isColorFull(wallVGroup2_Col0_Rows01, B));
            assertFalse(PkWall.isColorFull(wallVGroup2_Col0_Rows01, C));
            assertFalse(PkWall.isColorFull(wallVGroup2_Col0_Rows01, D));
            assertFalse(PkWall.isColorFull(wallVGroup2_Col0_Rows01, E));
            assertFalse(PkWall.isColorFull(wallVGroup3_Col2_Rows012, A));
            assertFalse(PkWall.isColorFull(wallVGroup3_Col2_Rows012, B));
            assertFalse(PkWall.isColorFull(wallVGroup3_Col2_Rows012, C));
            assertFalse(PkWall.isColorFull(wallVGroup3_Col2_Rows012, D));
            assertFalse(PkWall.isColorFull(wallVGroup3_Col2_Rows012, E));
            assertFalse(PkWall.isColorFull(wallVGroup4_Col4_Rows0123, A));
            assertFalse(PkWall.isColorFull(wallVGroup4_Col4_Rows0123, B));
            assertFalse(PkWall.isColorFull(wallVGroup4_Col4_Rows0123, C));
            assertFalse(PkWall.isColorFull(wallVGroup4_Col4_Rows0123, D));
            assertFalse(PkWall.isColorFull(wallVGroup4_Col4_Rows0123, E));
            assertFalse(PkWall.isColorFull(wallVGroup5_Col1_Full, A));
            assertFalse(PkWall.isColorFull(wallVGroup5_Col1_Full, B));
            assertFalse(PkWall.isColorFull(wallVGroup5_Col1_Full, C));
            assertFalse(PkWall.isColorFull(wallVGroup5_Col1_Full, D));
            assertFalse(PkWall.isColorFull(wallVGroup5_Col1_Full, E));
            assertFalse(PkWall.isColorFull(wallVTwoGroups_Col0_Separated, A));
            assertFalse(PkWall.isColorFull(wallVTwoGroups_Col0_Separated, B));
            assertFalse(PkWall.isColorFull(wallVTwoGroups_Col0_Separated, C));
            assertFalse(PkWall.isColorFull(wallVTwoGroups_Col0_Separated, D));
            assertFalse(PkWall.isColorFull(wallVTwoGroups_Col0_Separated, E));
            assertFalse(PkWall.isColorFull(wallHGroup2VGroup2_Tile6, A));
            assertFalse(PkWall.isColorFull(wallHGroup2VGroup2_Tile6, B));
            assertFalse(PkWall.isColorFull(wallHGroup2VGroup2_Tile6, C));
            assertFalse(PkWall.isColorFull(wallHGroup2VGroup2_Tile6, D));
            assertFalse(PkWall.isColorFull(wallHGroup2VGroup2_Tile6, E));
            assertFalse(PkWall.isColorFull(wallHGroup3VGroup3_Cross12, A));
            assertFalse(PkWall.isColorFull(wallHGroup3VGroup3_Cross12, B));
            assertFalse(PkWall.isColorFull(wallHGroup3VGroup3_Cross12, C));
            assertFalse(PkWall.isColorFull(wallHGroup3VGroup3_Cross12, D));
            assertFalse(PkWall.isColorFull(wallHGroup3VGroup3_Cross12, E));
            assertFalse(PkWall.isColorFull(wallHGroup3VGroup3_Corner24, A));
            assertFalse(PkWall.isColorFull(wallHGroup3VGroup3_Corner24, B));
            assertFalse(PkWall.isColorFull(wallHGroup3VGroup3_Corner24, C));
            assertFalse(PkWall.isColorFull(wallHGroup3VGroup3_Corner24, D));
            assertFalse(PkWall.isColorFull(wallHGroup3VGroup3_Corner24, E));
            assertFalse(PkWall.isColorFull(wallHGroup3VGroup3_Corner24, A));
            assertFalse(PkWall.isColorFull(wallHGroup3VGroup3_Corner24, B));
            assertFalse(PkWall.isColorFull(wallHGroup3VGroup3_Corner24, C));
            assertFalse(PkWall.isColorFull(wallHGroup3VGroup3_Corner24, D));
            assertFalse(PkWall.isColorFull(wallHGroup3VGroup3_Corner24, E));
            assertTrue(PkWall.isColorFull(wallAllA, A));
            assertFalse(PkWall.isColorFull(wallAllA, B));
            assertFalse(PkWall.isColorFull(wallAllA, C));
            assertFalse(PkWall.isColorFull(wallAllA, D));
            assertFalse(PkWall.isColorFull(wallAllA, E));
            assertTrue(PkWall.isColorFull(wallAllB, B));
            assertFalse(PkWall.isColorFull(wallAllB, A));
            assertFalse(PkWall.isColorFull(wallAllB, C));
            assertFalse(PkWall.isColorFull(wallAllB, D));
            assertFalse(PkWall.isColorFull(wallAllB, E));
            assertTrue(PkWall.isColorFull(wallAllC, C));
            assertFalse(PkWall.isColorFull(wallAllC, A));
            assertFalse(PkWall.isColorFull(wallAllC, B));
            assertFalse(PkWall.isColorFull(wallAllC, D));
            assertFalse(PkWall.isColorFull(wallAllC, E));
            assertTrue(PkWall.isColorFull(wallAllD, D));
            assertFalse(PkWall.isColorFull(wallAllD, A));
            assertFalse(PkWall.isColorFull(wallAllD, B));
            assertFalse(PkWall.isColorFull(wallAllD, C));
            assertFalse(PkWall.isColorFull(wallAllD, E));
            assertTrue(PkWall.isColorFull(wallAllE, E));
            assertFalse(PkWall.isColorFull(wallAllE, A));
            assertFalse(PkWall.isColorFull(wallAllE, B));
            assertFalse(PkWall.isColorFull(wallAllE, C));
            assertFalse(PkWall.isColorFull(wallAllE, D));
            assertTrue(PkWall.isColorFull(wallAllAPlusExtras, A));
            assertFalse(PkWall.isColorFull(wallAllAPlusExtras, B));
            assertFalse(PkWall.isColorFull(wallAllAPlusExtras, C));
            assertFalse(PkWall.isColorFull(wallAllAPlusExtras, D));
            assertFalse(PkWall.isColorFull(wallAllAPlusExtras, E));
            assertTrue(PkWall.isColorFull(wallAllBPlusExtras, B));
            assertFalse(PkWall.isColorFull(wallAllBPlusExtras, A));
            assertFalse(PkWall.isColorFull(wallAllBPlusExtras, C));
            assertFalse(PkWall.isColorFull(wallAllBPlusExtras, D));
            assertFalse(PkWall.isColorFull(wallAllBPlusExtras, E));
            assertTrue(PkWall.isColorFull(wallAllCPlusExtras, C));
            assertFalse(PkWall.isColorFull(wallAllCPlusExtras, A));
            assertFalse(PkWall.isColorFull(wallAllCPlusExtras, B));
            assertFalse(PkWall.isColorFull(wallAllCPlusExtras, D));
            assertFalse(PkWall.isColorFull(wallAllCPlusExtras, E));
            assertTrue(PkWall.isColorFull(wallAllDPlusExtras, D));
            assertFalse(PkWall.isColorFull(wallAllDPlusExtras, A));
            assertFalse(PkWall.isColorFull(wallAllDPlusExtras, B));
            assertFalse(PkWall.isColorFull(wallAllDPlusExtras, C));
            assertFalse(PkWall.isColorFull(wallAllDPlusExtras, E));
            assertTrue(PkWall.isColorFull(wallAllEPlusExtras, E));
            assertFalse(PkWall.isColorFull(wallAllEPlusExtras, A));
            assertFalse(PkWall.isColorFull(wallAllEPlusExtras, B));
            assertFalse(PkWall.isColorFull(wallAllEPlusExtras, C));
            assertFalse(PkWall.isColorFull(wallAllEPlusExtras, D));
        }

        @Test
        void asPkTileSetOnEmptyWallIsEmpty() {
            assertEquals(PkTileSet.EMPTY, PkWall.asPkTileSet(wallEmpty));
        }

        @Test
        void asPkTileSetOnFullWallContainsFiveOfEachColor() {
            int pkTileSet = PkWall.asPkTileSet(wallFull);

            assertEquals(5, PkTileSet.countOf(pkTileSet, A));
            assertEquals(5, PkTileSet.countOf(pkTileSet, B));
            assertEquals(5, PkTileSet.countOf(pkTileSet, C));
            assertEquals(5, PkTileSet.countOf(pkTileSet, D));
            assertEquals(5, PkTileSet.countOf(pkTileSet, E));
            assertEquals(25, PkTileSet.size(pkTileSet));
        }

        @Test
        void asPkTileSetOnSingleTileWallWorks() {
            int pkTileSet = PkWall.asPkTileSet(wallTileIsolatedTopLeft);

            assertEquals(1, PkTileSet.countOf(pkTileSet, A));
            assertEquals(0, PkTileSet.countOf(pkTileSet, B));
            assertEquals(0, PkTileSet.countOf(pkTileSet, C));
            assertEquals(0, PkTileSet.countOf(pkTileSet, D));
            assertEquals(0, PkTileSet.countOf(pkTileSet, E));
            assertEquals(1, PkTileSet.size(pkTileSet));
        }

        @Test
        void asPkTileSetOnFullFirstRowContainsOneOfEachColor() {
            int pkTileSet = PkWall.asPkTileSet(wallRow0Full);

            assertEquals(1, PkTileSet.countOf(pkTileSet, A));
            assertEquals(1, PkTileSet.countOf(pkTileSet, B));
            assertEquals(1, PkTileSet.countOf(pkTileSet, C));
            assertEquals(1, PkTileSet.countOf(pkTileSet, D));
            assertEquals(1, PkTileSet.countOf(pkTileSet, E));
            assertEquals(5, PkTileSet.size(pkTileSet));
        }

        @Test
        void asPkTileSetCountsColorsCorrectlyOnConstructedWall() {
            int wall = PkWall.EMPTY;
            wall = PkWall.withTileAt(wall, PATTERN_1, A);
            wall = PkWall.withTileAt(wall, PATTERN_2, A);
            wall = PkWall.withTileAt(wall, PATTERN_3, C);
            wall = PkWall.withTileAt(wall, PATTERN_4, E);
            wall = PkWall.withTileAt(wall, PATTERN_5, E);

            int pkTileSet = PkWall.asPkTileSet(wall);

            assertEquals(2, PkTileSet.countOf(pkTileSet, A));
            assertEquals(0, PkTileSet.countOf(pkTileSet, B));
            assertEquals(1, PkTileSet.countOf(pkTileSet, C));
            assertEquals(0, PkTileSet.countOf(pkTileSet, D));
            assertEquals(2, PkTileSet.countOf(pkTileSet, E));
            assertEquals(5, PkTileSet.size(pkTileSet));
        }

        @Test
        void asPkTileSetSizeMatchesNumberOfOccupiedWallCells() {
            int[] walls = {
                    wallEmpty,
                    wallFull,
                    wallRow0Full,
                    wallCol0Full,
                    wallTileIsolatedCenter,
                    wallHGroup3VGroup3_Cross12,
            };

            for (int wall : walls) {
                int pkTileSet = PkWall.asPkTileSet(wall);
                assertEquals(Integer.bitCount(wall), PkTileSet.size(pkTileSet));
            }
        }

        @Test
        void testToString() {
            assertEquals("[abcde, eabcd, deabc, cdeab, bcdea]", PkWall.toString(wallEmpty));
            assertEquals("[ABCDE, EABCD, DEABC, CDEAB, BCDEA]", PkWall.toString(wallFull));

            assertEquals("[ABCDE, eabcd, deabc, cdeab, bcdea]", PkWall.toString(wallRow0Full));
            assertEquals("[abcde, EABCD, deabc, cdeab, bcdea]", PkWall.toString(wallRow1Full));
            assertEquals("[abcde, eabcd, DEABC, cdeab, bcdea]", PkWall.toString(wallRow2Full));
            assertEquals("[abcde, eabcd, deabc, CDEAB, bcdea]", PkWall.toString(wallRow3Full));
            assertEquals("[abcde, eabcd, deabc, cdeab, BCDEA]", PkWall.toString(wallRow4Full));
            assertEquals("[ABCDE, eabcd, deabc, cdeab, BCDEA]", PkWall.toString(wallRow0AndRow4Full));

            assertEquals("[Abcde, eabcd, deabc, cdeab, bcdea]", PkWall.toString(wallTileIsolatedTopLeft));
            assertEquals("[abcdE, eabcd, deabc, cdeab, bcdea]", PkWall.toString(wallTileIsolatedTopRight));
            assertEquals("[abcde, eabcd, deAbc, cdeab, bcdea]", PkWall.toString(wallTileIsolatedCenter));
            assertEquals("[abcde, eabcd, deabc, cdeab, Bcdea]", PkWall.toString(wallTileIsolatedBottomLeft));
            assertEquals("[abcde, eabcd, deabc, cdeab, bcdeA]", PkWall.toString(wallTileIsolatedBottomRight));

            assertEquals("[ABcde, eabcd, deabc, cdeab, bcdea]", PkWall.toString(wallHGroup2_Row0_Cols01));
            assertEquals("[abcDE, eabcd, deabc, cdeab, bcdea]", PkWall.toString(wallHGroup2_Row0_Cols34));
            assertEquals("[abcde, EABcd, deabc, cdeab, bcdea]", PkWall.toString(wallHGroup3_Row1_Cols012));
            assertEquals("[abcde, eabcd, DEABc, cdeab, bcdea]", PkWall.toString(wallHGroup4_Row2_Cols0123));
            assertEquals("[abcde, eabcd, deabc, CDEAB, bcdea]", PkWall.toString(wallHGroup5_Row3_Full));
            assertEquals("[ABcDE, eabcd, deabc, cdeab, bcdea]", PkWall.toString(wallHTwoGroups_Row0_Separated));

            assertEquals("[Abcde, Eabcd, deabc, cdeab, bcdea]", PkWall.toString(wallVGroup2_Col0_Rows01));
            assertEquals("[abCde, eaBcd, deAbc, cdeab, bcdea]", PkWall.toString(wallVGroup3_Col2_Rows012));
            assertEquals("[abcdE, eabcD, deabC, cdeaB, bcdea]", PkWall.toString(wallVGroup4_Col4_Rows0123));
            assertEquals("[aBcde, eAbcd, dEabc, cDeab, bCdea]", PkWall.toString(wallVGroup5_Col1_Full));
            assertEquals("[Abcde, Eabcd, deabc, Cdeab, Bcdea]", PkWall.toString(wallVTwoGroups_Col0_Separated));

            assertEquals("[Abcde, eAbcd, deAbc, cdeAb, bcdeA]", PkWall.toString(wallAllA));
            assertEquals("[aBcde, eaBcd, deaBc, cdeaB, Bcdea]", PkWall.toString(wallAllB));
            assertEquals("[abCde, eabCd, deabC, Cdeab, bCdea]", PkWall.toString(wallAllC));
            assertEquals("[abcDe, eabcD, Deabc, cDeab, bcDea]", PkWall.toString(wallAllD));
            assertEquals("[abcdE, Eabcd, dEabc, cdEab, bcdEa]", PkWall.toString(wallAllE));
        }

        @Test
        void hasFullRow() {
            assertFalse(PkWall.hasFullRow(wallEmpty));

            assertTrue(PkWall.hasFullRow(wallFull));

            assertTrue(PkWall.hasFullRow(wallRow0Full));
            assertTrue(PkWall.hasFullRow(wallRow1Full));
            assertTrue(PkWall.hasFullRow(wallRow2Full));
            assertTrue(PkWall.hasFullRow(wallRow3Full));
            assertTrue(PkWall.hasFullRow(wallRow4Full));
            assertTrue(PkWall.hasFullRow(wallRow0AndRow4Full));

            assertFalse(PkWall.hasFullRow(wallCol0Full));
            assertFalse(PkWall.hasFullRow(wallCol1Full));
            assertFalse(PkWall.hasFullRow(wallCol2Full));
            assertFalse(PkWall.hasFullRow(wallCol3Full));
            assertFalse(PkWall.hasFullRow(wallCol4Full));

            assertFalse(PkWall.hasFullRow(wallTileIsolatedTopLeft));
            assertFalse(PkWall.hasFullRow(wallTileIsolatedTopRight));
            assertFalse(PkWall.hasFullRow(wallTileIsolatedCenter));
            assertFalse(PkWall.hasFullRow(wallTileIsolatedBottomLeft));
            assertFalse(PkWall.hasFullRow(wallTileIsolatedBottomRight));

            assertFalse(PkWall.hasFullRow(wallHGroup2_Row0_Cols01));
            assertFalse(PkWall.hasFullRow(wallHGroup2_Row0_Cols34));
            assertFalse(PkWall.hasFullRow(wallHGroup3_Row1_Cols012));
            assertFalse(PkWall.hasFullRow(wallHGroup4_Row2_Cols0123));
            assertTrue(PkWall.hasFullRow(wallHGroup5_Row3_Full));
            assertFalse(PkWall.hasFullRow(wallHTwoGroups_Row0_Separated));

            assertFalse(PkWall.hasFullRow(wallVGroup2_Col0_Rows01));
            assertFalse(PkWall.hasFullRow(wallVGroup3_Col2_Rows012));
            assertFalse(PkWall.hasFullRow(wallVGroup4_Col4_Rows0123));
            assertFalse(PkWall.hasFullRow(wallVGroup5_Col1_Full));
            assertFalse(PkWall.hasFullRow(wallVTwoGroups_Col0_Separated));

            assertFalse(PkWall.hasFullRow(wallHGroup2VGroup2_Tile6));
            assertFalse(PkWall.hasFullRow(wallHGroup3VGroup3_Cross12));
            assertFalse(PkWall.hasFullRow(wallHGroup3VGroup3_Corner24));

            assertFalse(PkWall.hasFullRow(wallAllA));
            assertFalse(PkWall.hasFullRow(wallAllB));
            assertFalse(PkWall.hasFullRow(wallAllC));
            assertFalse(PkWall.hasFullRow(wallAllD));
            assertFalse(PkWall.hasFullRow(wallAllE));

            assertFalse(PkWall.hasFullRow(wallAllAPlusExtras));
            assertFalse(PkWall.hasFullRow(wallAllBPlusExtras));
            assertFalse(PkWall.hasFullRow(wallAllCPlusExtras));
            assertFalse(PkWall.hasFullRow(wallAllDPlusExtras));
            assertFalse(PkWall.hasFullRow(wallAllEPlusExtras));
        }

        @Test
        void asPkTileSetOnAllColorWallsWorks() {
            int pkA = PkWall.asPkTileSet(wallAllA);
            assertEquals(5, PkTileSet.countOf(pkA, A));
            assertEquals(0, PkTileSet.countOf(pkA, B));
            assertEquals(0, PkTileSet.countOf(pkA, C));
            assertEquals(0, PkTileSet.countOf(pkA, D));
            assertEquals(0, PkTileSet.countOf(pkA, E));
            assertEquals(5, PkTileSet.size(pkA));

            int pkB = PkWall.asPkTileSet(wallAllB);
            assertEquals(0, PkTileSet.countOf(pkB, A));
            assertEquals(5, PkTileSet.countOf(pkB, B));
            assertEquals(0, PkTileSet.countOf(pkB, C));
            assertEquals(0, PkTileSet.countOf(pkB, D));
            assertEquals(0, PkTileSet.countOf(pkB, E));
            assertEquals(5, PkTileSet.size(pkB));

            int pkC = PkWall.asPkTileSet(wallAllC);
            assertEquals(0, PkTileSet.countOf(pkC, A));
            assertEquals(0, PkTileSet.countOf(pkC, B));
            assertEquals(5, PkTileSet.countOf(pkC, C));
            assertEquals(0, PkTileSet.countOf(pkC, D));
            assertEquals(0, PkTileSet.countOf(pkC, E));
            assertEquals(5, PkTileSet.size(pkC));

            int pkD = PkWall.asPkTileSet(wallAllD);
            assertEquals(0, PkTileSet.countOf(pkD, A));
            assertEquals(0, PkTileSet.countOf(pkD, B));
            assertEquals(0, PkTileSet.countOf(pkD, C));
            assertEquals(5, PkTileSet.countOf(pkD, D));
            assertEquals(0, PkTileSet.countOf(pkD, E));
            assertEquals(5, PkTileSet.size(pkD));

            int pkE = PkWall.asPkTileSet(wallAllE);
            assertEquals(0, PkTileSet.countOf(pkE, A));
            assertEquals(0, PkTileSet.countOf(pkE, B));
            assertEquals(0, PkTileSet.countOf(pkE, C));
            assertEquals(0, PkTileSet.countOf(pkE, D));
            assertEquals(5, PkTileSet.countOf(pkE, E));
            assertEquals(5, PkTileSet.size(pkE));
        }

        private static final TileDestination.Pattern[] PATTERNS = TileDestination.Pattern.values();
        private static final TileKind.Colored[] COLORS = TileKind.Colored.values();

        private static int rowOf(TileDestination.Pattern line) {
            return line.ordinal();
        }

        private static int colOf(TileKind.Colored color) {
            return color.ordinal();
        }

        // colonne = (couleur + ligne) % 5
        private static int expectedColumn(TileDestination.Pattern line, TileKind.Colored color) {
            return (colOf(color) + rowOf(line)) % 5;
        }

        // index = ligne * 5 + colonne
        private static int expectedIndex(TileDestination.Pattern line, TileKind.Colored color) {
            return rowOf(line) * 5 + expectedColumn(line, color);
        }

        private static boolean expectedHasTile(int pkWall, TileDestination.Pattern line, TileKind.Colored color) {
            return ((pkWall >> expectedIndex(line, color)) & 1) == 1;
        }

        private static int expectedHGroupSize(int pkWall, TileDestination.Pattern line, TileKind.Colored color) {
            int row = rowOf(line);
            int col = expectedColumn(line, color);
            int size = 1;
            // vers la gauche
            for (int c = col - 1; c >= 0; c--) {
                if (((pkWall >> (row * 5 + c)) & 1) == 1) size++;
                else break;
            }
            // vers la droite
            for (int c = col + 1; c < 5; c++) {
                if (((pkWall >> (row * 5 + c)) & 1) == 1) size++;
                else break;
            }
            return size;
        }

        private static int expectedVGroupSize(int pkWall, TileDestination.Pattern line, TileKind.Colored color) {
            int row = rowOf(line);
            int col = expectedColumn(line, color);
            int size = 1;
            // vers le haut
            for (int r = row - 1; r >= 0; r--) {
                if (((pkWall >> (r * 5 + col)) & 1) == 1) size++;
                else break;
            }
            // vers le bas
            for (int r = row + 1; r < 5; r++) {
                if (((pkWall >> (r * 5 + col)) & 1) == 1) size++;
                else break;
            }
            return size;
        }

        private static boolean expectedIsRowFull(int pkWall, TileDestination.Pattern line) {
            int row = rowOf(line);
            return ((pkWall >> (row * 5)) & 0b11111) == 0b11111;
        }

        private static boolean expectedIsColumnFull(int pkWall, int col) {
            for (int r = 0; r < 5; r++) {
                if (((pkWall >> (r * 5 + col)) & 1) == 0) return false;
            }
            return true;
        }

        private static boolean expectedHasFullRow(int pkWall) {
            for (TileDestination.Pattern line : PATTERNS) {
                if (expectedIsRowFull(pkWall, line)) return true;
            }
            return false;
        }

        private static boolean expectedIsColorFull(int pkWall, TileKind.Colored color) {
            for (TileDestination.Pattern line : PATTERNS) {
                if (!expectedHasTile(pkWall, line, color)) return false;
            }
            return true;
        }

        // ═══════════════════════════════════════════════
        // TESTS EXHAUSTIFS
        // ═══════════════════════════════════════════════

        @Test
        void exhaustiveHasTileAt() {
            for (int pkWall = 0; pkWall < (1 << 25); pkWall++) {
                for (TileDestination.Pattern line : PATTERNS) {
                    for (TileKind.Colored color : COLORS) {
                        assertEquals(
                                expectedHasTile(pkWall, line, color),
                                PkWall.hasTileAt(pkWall, line, color),
                                "hasTileAt échoue pour pkWall=" + pkWall + " line=" + line + " color=" + color
                        );
                    }
                }
            }
        }

        @Test
        void exhaustiveWithTileAt() {
            for (int pkWall = 0; pkWall < (1 << 25); pkWall++) {
                for (TileDestination.Pattern line : PATTERNS) {
                    for (TileKind.Colored color : COLORS) {
                        int idx = expectedIndex(line, color);
                        int expected = pkWall | (1 << idx);
                        assertEquals(
                                expected,
                                PkWall.withTileAt(pkWall, line, color),
                                "withTileAt échoue pour pkWall=" + pkWall + " line=" + line + " color=" + color
                        );
                    }
                }
            }
        }

        @Test
        void exhaustiveHGroupSize() {
            for (int pkWall = 0; pkWall < (1 << 25); pkWall++) {
                for (TileDestination.Pattern line : PATTERNS) {
                    for (TileKind.Colored color : COLORS) {
                        if (!expectedHasTile(pkWall, line, color)) continue; // tuile absente, on skip
                        assertEquals(
                                expectedHGroupSize(pkWall, line, color),
                                PkWall.hGroupSize(pkWall, line, color),
                                "hGroupSize échoue pour pkWall=" + pkWall + " line=" + line + " color=" + color
                        );
                    }
                }
            }
        }

        @Test
        void exhaustiveVGroupSize() {
            for (int pkWall = 0; pkWall < (1 << 25); pkWall++) {
                for (TileDestination.Pattern line : PATTERNS) {
                    for (TileKind.Colored color : COLORS) {
                        if (!expectedHasTile(pkWall, line, color)) continue; // tuile absente, on skip
                        assertEquals(
                                expectedVGroupSize(pkWall, line, color),
                                PkWall.vGroupSize(pkWall, line, color),
                                "vGroupSize échoue pour pkWall=" + pkWall + " line=" + line + " color=" + color
                        );
                    }
                }
            }
        }

        @Test
        void exhaustiveHasFullRow() {
            for (int pkWall = 0; pkWall < (1 << 25); pkWall++) {
                assertEquals(
                        expectedHasFullRow(pkWall),
                        PkWall.hasFullRow(pkWall),
                        "hasFullRow échoue pour pkWall=" + pkWall
                );
            }
        }

        @Test
        void exhaustiveIsRowFull() {
            for (int pkWall = 0; pkWall < (1 << 25); pkWall++) {
                for (TileDestination.Pattern line : PATTERNS) {
                    assertEquals(
                            expectedIsRowFull(pkWall, line),
                            PkWall.isRowFull(pkWall, line),
                            "isRowFull échoue pour pkWall=" + pkWall + " line=" + line
                    );
                }
            }
        }

        @Test
        void exhaustiveIsColumnFull() {
            for (int pkWall = 0; pkWall < (1 << 25); pkWall++) {
                for (int col = 0; col < 5; col++) {
                    assertEquals(
                            expectedIsColumnFull(pkWall, col),
                            PkWall.isColumnFull(pkWall, col),
                            "isColumnFull échoue pour pkWall=" + pkWall + " col=" + col
                    );
                }
            }
        }

        @Test
        void exhaustiveIsColorFull() {
            for (int pkWall = 0; pkWall < (1 << 25); pkWall++) {
                for (TileKind.Colored color : COLORS) {
                    assertEquals(
                            expectedIsColorFull(pkWall, color),
                            PkWall.isColorFull(pkWall, color),
                            "isColorFull échoue pour pkWall=" + pkWall + " color=" + color
                    );
                }
            }
        }
    }

