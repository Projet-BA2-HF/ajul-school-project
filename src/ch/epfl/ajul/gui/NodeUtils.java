package ch.epfl.ajul.gui;

import javafx.scene.Node;

public final class NodeUtils {

    private NodeUtils() {}

    public static <T extends Node> T withClasses(T node, String ... classes){
        node.getStyleClass().addAll(classes);
        return node;
    }

    public static <T extends Node> T withId(T node, String id){
        node.setId(id);;
        return node;
    }


}
