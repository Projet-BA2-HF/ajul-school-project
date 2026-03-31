package ch.epfl.ajul;

/// Represents a points observer, i.e. an object that is notified each time
/// a player's points change during a game of Ajul.
/// Each method corresponds to a specific situation in which a player can gain
/// or lose points, and is called when that situation occurs.
/// All methods are default methods with empty bodies, so implementations can
/// selectively override only the events they are interested in.
///
/// @author Hedi Fourati
/// @author Fahd Ben Ismail
public interface PointsObserver {

    /// An empty points observer whose methods do nothing.
    PointsObserver EMPTY = new PointsObserver() {};

    /// Called when the player with the given identity has placed a tile of the
    /// given color on the given line of their wall, earning the given number
    /// of points.
    ///
    /// @param playerId the identity of the player who placed the tile
    /// @param line     the pattern line on the wall where the tile was placed
    /// @param color    the color of the placed tile
    /// @param points   the number of points earned
    default void newWallTile(PlayerId playerId,
                             TileDestination.Pattern line,
                             TileKind.Colored color,
                             int points){}

    /// Called at the end of a round when the player with the given identity
    /// loses the given penalty due to tiles on their floor line.
    ///
    /// @param playerId the identity of the player losing points
    /// @param penalty  the number of points lost (strictly positive)
    default void floor(PlayerId playerId, int penalty){}

    /// Called at the end of the game when the player with the given identity
    /// earns bonus points for having a complete row on their wall.
    ///
    /// @param playerId the identity of the player earning bonus points
    /// @param line     the complete row on the wall
    /// @param points   the number of bonus points earned
    default void fullRow(PlayerId playerId, TileDestination.Pattern line, int points){}

    /// Called at the end of the game when the player with the given identity
    /// earns bonus points for having a complete column on their wall.
    ///
    /// @param playerId the identity of the player earning bonus points
    /// @param column   the index of the complete column on the wall
    /// @param points   the number of bonus points earned
    default void fullColumn(PlayerId playerId, int column, int points){}

    /// Called at the end of the game when the player with the given identity
    /// earns bonus points for having all tiles of the given color on their wall.
    ///
    /// @param playerId the identity of the player earning bonus points
    /// @param color    the color that is fully present on the wall
    /// @param points   the number of bonus points earned
    default void fullColor(PlayerId playerId, TileKind.Colored color, int points){}
}
