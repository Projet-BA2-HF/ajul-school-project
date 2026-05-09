# Revision Etapes 1-6 - Points cles de l'implementation

## 1. Interfaces scellees (sealed interfaces)

`TileKind`, `TileSource`, `TileDestination` sont des **sealed interfaces**.
- Seules les classes dans le meme fichier peuvent les implementer
- Permet d'avoir plusieurs types pour un meme concept :
  - `TileKind` (les 6 sortes) vs `TileKind.Colored` (les 5 couleurs seulement)
  - `TileSource` vs `TileSource.Factory` vs `TileSource.CenterArea`
  - `TileDestination` vs `TileDestination.Pattern` vs `TileDestination.Floor`
- Chaque sous-type est un **enum** imbrique qui implemente l'interface
- Interet : typage precis (ex: un coup prend un `TileKind.Colored`, pas un `TileKind`)

## 2. Enregistrements (records)

`Move` et `ImmutableGameState` sont des **records** :
- Java genere automatiquement : constructeur, accesseurs (sans `get`), `equals`, `hashCode`, `toString`
- Un record est toujours `final`
- Le **constructeur compact** (sans parentheses de parametres) permet d'ajouter de la validation sans reecrire tout le constructeur :
```java
public Move {
    Objects.requireNonNull(source);  // validation ajoutee
    // Java ajoute automatiquement : this.source = source; etc.
}
```

## 3. Empaquetage (bit packing)

### PkMove - Coup empaquete (short, 10 bits utiles)
```
bits:  [15..10] [9..7]      [6..4]    [3..0]
       unused   destination  color     source
       6 bits   3 bits       3 bits    4 bits
```
- Extraction : decalage + masquage (`(pkMove >> OFFSET) & MASK`)
- Construction : OR bit a bit des composantes decalees

### PkTileSet - Ensemble de tuiles (int, 31 bits utiles)
```
bits:  [31] [30] [29..24] [23..18] [17..12] [11..6] [5..0]
        0    M    EEEEE    DDDDD    CCCCC    BBBBB   AAAAA
```
- 6 bits par couleur (0-20), 1 bit pour le marqueur
- **Bits separateurs a 0** entre les compteurs → permet les operations SWAR
- `union = addition`, `difference = soustraction` (une seule operation !)

### Technique SWAR (Single Instruction Multiple Data)
Dans `PkTileSet.size()` :
```java
int sum = pkTileSet + (pkTileSet >>> 6);
// Produit 5 sommes partielles en parallele : A+B, B+C, C+D, D+E, E+M
// Ensuite on extrait A+B, C+D, E+M et on les additionne
```
Les bits separateurs a 0 empechent les retenues de deborder d'un compteur a l'autre.

### PkPatterns - Lignes de motif (int, 30 bits utiles)
```
Pour chaque ligne (5 lignes, 6 bits chacune) :
  3 bits = nombre de tuiles
  3 bits = index couleur (ou 0 si vide)
```
- Ligne 1 dans les bits de poids faible, ligne 5 dans les bits de poids fort

### PkFloor - Ligne plancher (int, 24 bits utiles)
```
bits 0-2 : taille (0-7)
bits 3-5 : sorte tuile position 0
bits 6-8 : sorte tuile position 1
...
```
- Cas special : le marqueur de premier joueur est TOUJOURS place, meme si le plancher est plein (remplace la derniere tuile)

### PkWall - Mur (int, 25 bits utiles)
- Bit i = 1 ssi la case i du mur contient une tuile
- Cases numerotees 0-24 de gauche a droite, haut en bas
- Colonne d'une tuile : `(line.index() + color.index()) % 5`
- Masques de bits pour lignes, colonnes et couleurs → verification de completude en O(1)

### PkIntSet32 - Ensemble d'entiers (int, 32 bits)
- Bit i = 1 ssi l'entier i appartient a l'ensemble
- Utilise pour `pkUniqueTileSources` (quelles sources sont "uniques")

## 4. Penalites du plancher (Points.java)

Stockage empaquete des penalites dans un int :
```java
private static final int FLOOR_PENALTY = 0x3322211;
// Chaque chiffre hex = penalite d'une position (pos 0→1, pos 1→1, pos 2→2, ...)
```
Penalites cumulees calculees par multiplication :
```java
private static final int TOTAL_FLOOR_PENALTY = (FLOOR_PENALTY * 0x1111111) << 4;
```
La multiplication par `0x1111111` somme les nibbles precedents dans chaque position.

## 5. Points du mur

Quand une tuile est ajoutee au mur :
- h = taille groupe horizontal, v = taille groupe vertical
- Points = h si v==1, v si h==1, h+v sinon
- `hGroupSize` / `vGroupSize` : parcours des voisins adjacents avec `countInDirection`

Bonus de fin de partie :
- Ligne complete : 2 pts
- Colonne complete : 7 pts
- Couleur complete : 10 pts

## 6. Algorithmes aleatoires

### Fisher-Yates (melange)
```
pour i de 0 a taille-2 :
    j = aleatoire entre i et taille-1 (inclus)
    echanger tableau[i] et tableau[j]
```
Chaque permutation a la meme probabilite → melange juste.

### Echantillonnage par reservoir (reservoir sampling)
Pour tirer n elements d'un ensemble de taille inconnue :
```
pour chaque element e (index i) :
    si i < n : reservoir[i] = e
    sinon :
        j = aleatoire entre 0 et i
        si j < n : reservoir[j] = e
```
Utilise dans `PkTileSet.sampleColoredInto()` pour tirer des tuiles du sac.

## 7. Hierarchie des tableaux d'entiers

```
ReadOnlyIntArray (interface)
    └── AbstractIntArray (classe abstraite, stocke int[])
        ├── ImmutableIntArray  → copyOf(int[]) copie le tableau
        └── MutableIntArray    → wrapping(int[]) ne copie PAS
```
- **Read-only** : on peut lire mais pas ecrire ; le contenu PEUT changer (si quelqu'un modifie le tableau sous-jacent)
- **Immutable** : le contenu ne changera JAMAIS
- `immutable()` sur un `ImmutableIntArray` retourne `this` (pas de copie inutile)
- Constructeurs prives + methodes fabriques (`copyOf`, `wrapping`) pour controler la creation

## 8. Etat de partie

### ReadOnlyGameState (interface avec methodes par defaut)
6 methodes abstraites (accesseurs) + methodes par defaut calculees :
- `isRoundOver()` : aucune source ne contient de tuile coloree (`pkUniqueTileSources == EMPTY`)
- `isGameOver()` : manche terminee ET au moins un joueur a une ligne complete du mur
- `pkDiscardedTiles()` : tuiles sorties = FULL - sac - sources - patterns - floors - murs
- `validMoves()` / `uniqueValidMoves()` : enumere source × couleur × destination, verifie les contraintes

### ImmutableGameState (record)
- Snapshot immutable de l'etat
- `initial(game)` : etat de depart (sac plein, centre = marqueur, tout le reste vide)
- `immutable()` retourne `this`

### MutableGameState
Pattern **vue duale** :
```java
private int[] pkTileSourcesList;                           // mutable, pour modifier
private final ReadOnlyIntArray pkTileSources;               // read-only, pour exposer
// pkTileSources = MutableIntArray.wrapping(pkTileSourcesList)
```
Modifier `pkTileSourcesList[i]` change automatiquement ce que `pkTileSources.get(i)` retourne.

### fillFactories(random)
1. Calculer n = nombre de tuiles necessaires
2. Si le sac a assez : tirer n tuiles (reservoir sampling)
3. Sinon : vider le sac, le remplir avec les tuiles defaussees, tirer le reste
4. Melanger toutes les tuiles extraites (Fisher-Yates)
5. Distribuer par groupes de 4 sur les fabriques
6. Mettre a jour `pkUniqueTileSources`

### registerMove(pkMove)
1. Identifier source, couleur, destination
2. Retirer les tuiles de la source :
   - Fabrique : reste va au centre, fabrique videe
   - Centre : on prend couleur + eventuel marqueur
3. Placer les tuiles sur la destination :
   - Ligne de motif : min(capacite restante, tuiles dispo), excedent → plancher
   - Plancher : tout y va
4. Mettre a jour `pkUniqueTileSources` (gerer fabrique videe, doublon eventuel)
5. Passer au joueur suivant

### endRound()
Pour chaque joueur :
1. Lignes de motif pleines → tuile sur le mur + points (hGroup × vGroup)
2. Penalite plancher deduite (plafonnee par le score actuel)
3. Plancher vide, marqueur de premier joueur retourne au centre
4. Le joueur qui avait le marqueur devient joueur courant

### endGame()
Pour chaque joueur : bonus lignes completes (2) + colonnes (7) + couleurs (10)

## 9. Sources uniques (pkUniqueTileSources)

Une source est "unique" ssi :
- elle contient au moins une tuile coloree, ET
- aucune fabrique AVANT elle (dans l'ordre des index) n'a le meme contenu

Interet : eviter d'evaluer des coups equivalents (meme contenu = memes coups possibles).
Represente par un `PkIntSet32` (ensemble d'index de sources).

## 10. Observer de points (PointsObserver)

- Interface avec des **methodes par defaut vides**
- `EMPTY` : instance qui ne fait rien (pour l'IA)
- Les methodes de `MutableGameState` (`endRound`, `endGame`) appellent l'observer a chaque changement de score
- Permet au GUI d'afficher les details des points sans coupler la logique de jeu a l'affichage

## 11. Validation des arguments

Deux approches dans le projet :
- **Preconditions.checkArgument()** : leve `IllegalArgumentException`, toujours active → pour les API publiques (constructeur de `Game`, etc.)
- **assert** : desactivable, zero cout en production → pour les classes packed (`PkTileSet.isValid()`, etc.) ou la performance compte

## 12. Nombre max de coups valides

`Move.MAX_MOVES = 216` = 9 fabriques × 4 couleurs/fabrique × 6 destinations.
C'est le pire cas : 4 joueurs, toutes les fabriques ont 4 couleurs differentes, toutes les lignes de motif vides.
