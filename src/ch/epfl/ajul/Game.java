package ch.epfl.ajul;

import java.util.List;
import java.util.Objects;

/// Represents a game configuration, i.e. the set of players and the derived game parameters
/// such as the number of factories and tile sources.
/// Instances are immutable.
///
/// @author Hedi Fourati
/// @author Fahd Ben Ismail
public final class Game {

    // Minimum number of players allowed in a game
    private static final int MIN_PLAYERS = 2;
    // Maximum number of players allowed in a game
    private static final int MAX_PLAYERS = PlayerId.ALL.size();

    // Extra factory count added per player beyond the base (2n+1 formula)
    private static final int EXTRA_FACTORY = 1;

    // Number of center areas in a game 
    private static final int CENTER_AREA_COUNT = 1;

    // Immutable list of all tile sources used in this game
    private final List<TileSource> tileSources;
    // Immutable list of factories used in this game
    private final List<TileSource.Factory> factories;
    // Immutable list of player identities in this game
    private final List<PlayerId> playerIds;
    // Immutable list of player descriptions in this game
    private final List<PlayerDescription> playerDescriptions;

    // Max tiles a single factory can contribute to the center area
    private static final int MAX_TILES_TO_CENTER_PER_FACTORY =
            TileSource.Factory.TILES_PER_FACTORY - 1;

    // Number of first-player markers in the game (always 1)
    private static final int FIRST_PLAYER_MARKER_COUNT =
            TileKind.FIRST_PLAYER_MARKER.tilesCount();

    /// Describes a player in a game, combining their identity, display name, and kind.
    ///
    /// @param id   the identity of the player
    /// @param name the display name of the player
    /// @param kind the kind of the player (human or AI)
    public record PlayerDescription(PlayerId id, String name, PlayerKind kind) {

        /// The kind of player: either human-controlled or AI-controlled.
        public enum PlayerKind {
            /// A human player.
            HUMAN,
            /// An AI-controlled player.
            AI
        }

        /// Constructs a {@link PlayerDescription}, verifying that none of the arguments is null.
        ///
        /// @throws NullPointerException if any argument is null
        public PlayerDescription {
            Objects.requireNonNull(id);
            Objects.requireNonNull(name);
            Objects.requireNonNull(kind);
        }
    }

    /// Constructs a new game configuration for the players described by {@code playerDescriptions}.
    ///
    /// @param playerDescriptions the list of player descriptions, in player-identity order
    /// @throws IllegalArgumentException if the number of players is not between 2 and 4,
    ///                                  or if the position of a player in the list does not
    ///                                  match their identity
    public Game(List<PlayerDescription> playerDescriptions) {
        Preconditions.checkArgument(
                playerDescriptions.size() >= MIN_PLAYERS
                        && playerDescriptions.size() <= MAX_PLAYERS
        );

        int size = playerDescriptions.size();
        for (int i = 0; i < size; i++) {
            Preconditions.checkArgument(
                    playerDescriptions.get(i).id() == PlayerId.ALL.get(i));
        }

        this.playerDescriptions = List.copyOf(playerDescriptions);
        this.factories = TileSource.Factory.ALL.subList(0, factoriesCount());
        this.tileSources = TileSource.ALL.subList(0, tileSourcesCount());
        this.playerIds = PlayerId.ALL.subList(0, size);
    }

    /// Returns the immutable list of player descriptions for this game,
    /// in the same order as they were passed to the constructor.
    ///
    /// @return the list of player descriptions
    public List<PlayerDescription> playerDescriptions() {
        return playerDescriptions;
    }

    /// Returns the immutable list of player identities for this game,
    /// which is a prefix of {@link PlayerId#ALL}.
    ///
    /// @return the list of player identities
    public List<PlayerId> playerIds() {
        return playerIds;
    }

    /// Returns the number of players in this game.
    ///
    /// @return the number of players
    public int playersCount() {
        return playerDescriptions.size();
    }

    /// Returns the immutable list of factory tile sources used in this game,
    /// which is a prefix of {@link TileSource.Factory#ALL}.
    ///
    /// @return the list of factories
    public List<TileSource.Factory> factories() {
        return factories;
    }

    /// Returns the number of factories used in this game.
    /// For a game with {@code n} players, this equals {@code 2n + 1}.
    ///
    /// @return the number of factories
    public int factoriesCount() {
        return 2 * playersCount() + EXTRA_FACTORY;
    }

    /// Returns the immutable list of all tile sources used in this game (factories plus
    /// the central area), which is a prefix of {@link TileSource#ALL}.
    ///
    /// @return the list of tile sources
    public List<TileSource> tileSources() {
        return tileSources;
    }

    /// Returns the total number of tile sources in this game
    /// (factories plus one central area).
    ///
    /// @return the number of tile sources
    public int tileSourcesCount() {
        return factoriesCount() + CENTER_AREA_COUNT;
    }

    /// Returns the maximum number of tiles that can be in the central area at any point
    /// during the game, including the first player marker.
    /// For a game with {@code m} factories, this equals {@code 3m + 1}.
    ///
    /// @return the maximum size of the central area
    public int centralAreaMaxSize() {
        return MAX_TILES_TO_CENTER_PER_FACTORY * factoriesCount() + FIRST_PLAYER_MARKER_COUNT;
    }
}
