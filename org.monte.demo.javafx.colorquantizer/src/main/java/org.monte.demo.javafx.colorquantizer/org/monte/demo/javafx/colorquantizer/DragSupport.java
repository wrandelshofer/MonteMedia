/*
 * @(#)DragSupport.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.colorquantizer;

import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.util.function.Function;
import java.util.function.Predicate;

public class DragSupport {
    private final Node node;
    private final Predicate<Dragboard> predicate;
    private final Function<Dragboard, Boolean> dropConsumer;
    private final Border defaultBorder;
    private final Border dragOverBorder;

    public DragSupport(Node node, Predicate<Dragboard> predicate, Function<Dragboard, Boolean> dropConsumer) {
        this.node = node;
        this.predicate = predicate;
        this.dropConsumer = dropConsumer;
        node.setOnDragOver(this::onDragOver);
        node.setOnDragEntered(this::onDragEntered);
        node.setOnDragExited(this::onDragExited);
        node.setOnDragDropped(this::onDragDropped);
        defaultBorder = new Border(new BorderStroke(
                Color.TRANSPARENT, BorderStrokeStyle.SOLID, null, null));
        dragOverBorder = new Border(new BorderStroke(
                Color.LIGHTBLUE,
                //Platform.getPreferences().getAccentColor(),
                BorderStrokeStyle.SOLID, null, null));
        if (node instanceof Region region) {
            region.setBorder(defaultBorder);
        }
    }

    private void onDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (predicate.test(event.getDragboard())) {
            success = dropConsumer.apply(db);
        }
        event.setDropCompleted(success);
        event.consume();
    }

    private void onDragExited(DragEvent event) {
        if (node instanceof Region region) {
            region.setBorder(defaultBorder);
        }
        event.consume();
    }

    private void onDragEntered(DragEvent event) {
        if (predicate.test(event.getDragboard())) {
            if (node instanceof Region region) {
                region.setBorder(dragOverBorder);
            }
        }
        event.consume();
    }

    private void onDragOver(DragEvent event) {
        if (predicate.test(event.getDragboard())) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }
}
