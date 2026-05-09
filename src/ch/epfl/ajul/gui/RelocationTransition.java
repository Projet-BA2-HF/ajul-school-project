package ch.epfl.ajul.gui;

import javafx.animation.Transition;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.util.Duration;

import java.util.Objects;

/// Represents an animation that moves a JavaFX node from its current position
/// to a given target position, over a given duration. It is intended to be
/// used for animating tile movements in the user interface.
///
/// @author Hedi Fourati
/// @author Fahd Ben Ismail
public final class RelocationTransition extends Transition {

    // The node to move during the animation.
    private final Node node;
    // Starting position, captured at construction time in the parent's coordinate system.
    private final Point2D from;
    // Target position at which the node must be located when the animation ends.
    private final Point2D to;

    /// Builds a new animation that will move {@code node} from its current
    /// position in its parent to {@code end}, over {@code duration}.
    ///
    /// @param node     the node to move
    /// @param end      the target position, in the parent's coordinate system
    /// @param duration the duration of the animation
    /// @throws NullPointerException if any argument is {@code null}
    public RelocationTransition(Node node, Point2D end, Duration duration) {
        Objects.requireNonNull(node);
        Objects.requireNonNull(end);
        Objects.requireNonNull(duration);

        setCycleDuration(duration);
        this.node = node;
        // The starting position is the node's current location in its parent,
        // obtained by translating the local origin to the parent's coordinates.
        this.from = node.localToParent(Point2D.ZERO);
        this.to = end;
    }

    @Override
    protected void interpolate(double frac) {
        // Linearly interpolates between from and to: returns from when frac == 0,
        // to when frac == 1, and a point along the line between them otherwise.
        Point2D current = from.interpolate(to, frac);
        node.relocate(current.getX(), current.getY());
    }
}