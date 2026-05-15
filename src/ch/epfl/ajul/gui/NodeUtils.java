package ch.epfl.ajul.gui;

import javafx.scene.Node;

/// Utility methods for configuring JavaFX nodes in a fluent style.
/// These helpers return the node they receive so they can be chained
/// inline during scene-graph construction.
///
/// @author Hedi Fourati
/// @author Fahd Ben Ismail
public final class NodeUtils {

    private NodeUtils() {}

    /// Adds the given style classes to the given node, and returns it.
    ///
    /// @param node    the node to configure
    /// @param classes the style classes to add
    /// @param <T>     the concrete type of {@code node}
    /// @return the same {@code node}, for chaining
    public static <T extends Node> T withClasses(T node, String ... classes){
        node.getStyleClass().addAll(classes);
        return node;
    }

    /// Sets the identifier of the given node, and returns it.
    ///
    /// @param node the node to configure
    /// @param id   the identifier to set
    /// @param <T>  the concrete type of {@code node}
    /// @return the same {@code node}, for chaining
    public static <T extends Node> T withId(T node, String id){
        node.setId(id);
        return node;
    }
}
