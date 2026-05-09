package ch.epfl.ajul.mcts;

import ch.epfl.ajul.Preconditions;

import java.util.Objects;

/// Represents a node of the game tree built by the MCTS algorithm.
/// Each node stores the packed move (10 bits) and the simulated game count (22 bits)
/// in a single {@code int}, and the total points obtained in a second {@code int}.
/// The child array is initially {@code null} and is set later by the MCTS algorithm.
///
/// @author Hedi Fourati
/// @author Fahd Ben Ismail
public final class MctsNode {
    // Bits 0–9: packed move; bits 10–31: simulated game count N
    private int packedMove;
    // Total points S accumulated across all simulated games from this node
    private int totalPoints;
    // Child nodes; null until the MCTS algorithm expands this node
    MctsNode[] childNodes;
    // 10-bit sentinel value representing no move (used for the root)
    private static final int EMPTY = 0x3FF;
    // Bit offset separating the packed move from the game count
    private static final int POINTS_MASK = 10;
    // Exploration constant used in the priority formula
    private static final int C = 80;

    // Private constructor; use newRoot() or newMoveNode() instead.
    private MctsNode(
            int packedMove,
            int totalPoints
    ){
        this.packedMove = packedMove;
        this.totalPoints = totalPoints;
        this.childNodes = null;
    }

    /// Creates a new root node with no associated move, a game count of 1,
    /// total points of 0, and no children.
    ///
    /// @return a new root node
    public static MctsNode newRoot(){
        return new MctsNode(
                EMPTY | (1 << POINTS_MASK),
                0
        );
    }

    /// Creates a new node for the given packed move, with a game count
    /// and total points both equal to 0, and no children.
    ///
    /// @param packedMove the packed move (10 bits) of the incoming edge
    /// @return a new move node
    public static MctsNode newMoveNode(int packedMove){
        return new MctsNode(
                packedMove,
                0
        );
    }

    /// Returns the packed move (10 bits) of the edge leading to this node.
    /// For the root node, returns a sentinel value ({@code 0x3FF}).
    ///
    /// @return the packed move of the incoming edge
    public short pkMove(){
        return (short) (packedMove & EMPTY);
    }

    /// Returns the number of games that have been simulated through this node.
    ///
    /// @return the simulated game count N
    public int gameCount(){
        return packedMove >> POINTS_MASK;
    }

    /// Returns the average number of points per simulated game,
    /// computed as {@code totalPoints / gameCount}.
    ///
    /// @return the average points per game
    public double averagePoints(){
        return (double) totalPoints / gameCount();
    }

    /// Adds the given number of points to the total and increments
    /// the simulated game count by one.
    ///
    /// @param points the number of points to add
    public void registerEvaluation(int points){
        totalPoints += points;
        packedMove += 1 << POINTS_MASK;
    }

    /// Returns the index of the child node to explore next.
    /// If the game count is at most the number of children, returns
    /// {@code gameCount() - 1}. Otherwise, returns the index of the
    /// child with the highest priority.
    ///
    /// @return the index of the child to explore
    /// @throws NullPointerException if this node has no children
    public int indexOfChildToExplore(){
        Objects.requireNonNull(childNodes);

        if(gameCount() <= childNodes.length) return gameCount() - 1;

        double lastValue = priority(childNodes[0]);
        int lastIndex = 0;

        for(int i = 1; i < childNodes.length; i++){
            double newPriority = priority(childNodes[i]);
            if(lastValue < newPriority){
                lastValue = newPriority;
                lastIndex = i;
            }
        }
        return lastIndex;
    }

    // Computes the priority of the given child, combining average points (exploitation)
    // with an exploration term based on the parent and child game counts.
    private double priority(MctsNode child) {
        return child.averagePoints() + C * Math.sqrt(2 * Math.log(gameCount()) / child.gameCount());
    }
}
