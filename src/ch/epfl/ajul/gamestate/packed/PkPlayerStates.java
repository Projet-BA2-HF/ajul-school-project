package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.Points;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;

public final class PkPlayerStates {
    // Tableau de 4n entier avec n le nombre de joueurs.
    // index 0 le cotenu des lignes de motif du premier joueur,
    // L index 1, le contenu de la ligne plancher du premier joueur
    // l index 2, le contenu du mur du premier joueur
    // l index 3, le nombre de points du premier joueur

    // Soit lecture seule, : un tableau sous la forme ReadOnlyIntArray
    // soit modifiable : tableau prmitive int[]

    public static ImmutableIntArray initial(Game game){
        // Retourne un tableau immuable contenant l'etat empaquete intital des joueurs dans la partie gagme, dans lequel toutes les lignes de motif, lignes de motif, lgines planher et murs sont vide et pt vaut 0
        int playerCount = game.playersCount();
        int[] initialStates = new int[playerCount * 4];
        for(int i = 0; i < playerCount; i++){
            initialStates[i * 4] = PkPatterns.EMPTY;
            initialStates[i * 4 + 1] = PkFloor.EMPTY;
            initialStates[i * 4 + 2] = PkWall.EMPTY;
            initialStates[i * 4 + 3] = 0;
        }
        return ImmutableIntArray.copyOf(initialStates);
    }

    public static int pkPatterns(ReadOnlyIntArray pkPlayerStates, PlayerId playerId){
        return pkPlayerStates.get(playerId.ordinal() * 4);
    }

    public static int pkFloor(ReadOnlyIntArray pkPlayerStates, PlayerId playerId){
        return pkPlayerStates.get(playerId.ordinal() * 4 + 1);
    }

    public static int pkWall(ReadOnlyIntArray pkPlayerStates, PlayerId playerId) {
        return pkPlayerStates.get(playerId.ordinal() * 4 + 2);
    }

    public static int points(ReadOnlyIntArray pkPlayerStates, PlayerId playerId) {
        return pkPlayerStates.get(playerId.ordinal() * 4 + 3);
    }

    public static void setPkPatterns(int[] pkPlayerStates, PlayerId playerId, int pkPatterns) {
        pkPlayerStates[playerId.ordinal() * 4] = pkPatterns;
    }

    public static void setPkFloor(int[] pkPlayerStates, PlayerId playerId, int pkFloor) {
        pkPlayerStates[playerId.ordinal() * 4 + 1] = pkFloor;
    }

    public static void setPkWall(int[] pkPlayerStates, PlayerId playerId, int pkWall) {
        pkPlayerStates[playerId.ordinal() * 4 + 2] = pkWall;
    }

    public static void setPoints(int[] pkPlayerStates, PlayerId playerId, int pointsToAdd) {
        pkPlayerStates[playerId.ordinal() * 4 + 3] += pointsToAdd; // ?
    }

}
