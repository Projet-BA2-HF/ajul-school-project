package ch.epfl.ajul;

import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.ReadOnlyGameState;

/// Represents a player of a game of Ajul.
/// As this interface has a single abstract method, it can be used as a functional interface.
///
/// @author Hedi Fourati
/// @author Fahd Ben Ismail
@FunctionalInterface
public interface Player {

    /// Returns the move that this player wishes to play, given the current game state.
    /// The current player of the given state is assumed to be this player.
    ///
    /// @param gameState the current read-only game state
    /// @return the move chosen by this player
    Move nextMove(ReadOnlyGameState gameState);
}