package ch.epfl.ajul;

import java.util.List;

/// Identifies one of the players in a game of Ajul.
///
/// @author Hedi Fourati
/// @author Fahd Ben Ismail
public enum PlayerId {

    /// Player 1.
    P1,

    /// Player 2.
    P2,

    /// Player 3.
    P3,

    /// Player 4.
    P4;

    /// An immutable list of all player identifiers, in definition order.
    public static final List<PlayerId> ALL = List.of(values());
}
