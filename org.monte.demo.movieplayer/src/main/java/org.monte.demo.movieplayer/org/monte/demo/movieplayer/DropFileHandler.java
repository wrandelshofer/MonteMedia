/*
 * @(#)DropFileHandler.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.movieplayer;

import javafx.application.Platform;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.Pane;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * Drag and Drop handler.
 */
public class DropFileHandler {
    private final Pane pane;
    private final Consumer<File> fileDroppedConsumer;

    public DropFileHandler(Pane pane, Consumer<File> fileDroppedConsumer) {
        this.pane = pane;
        this.fileDroppedConsumer = fileDroppedConsumer;
        pane.setOnDragEntered(this::dragEntered);
        pane.setOnDragExited(this::dragExited);
        pane.setOnDragOver(this::dragOver);
        pane.setOnDragDropped(this::dragDropped);
    }

    private void dragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    private void dragExited(DragEvent event) {
        pane.setBorder(null);
        event.consume();
    }

    private void dragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles() && db.getFiles() instanceof List<File> fileList && !fileList.isEmpty()) {
            fileDroppedConsumer.accept(fileList.getFirst());
            success = true;
        }
        event.setDropCompleted(success);
        event.consume();
    }

    private void dragEntered(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            pane.setBorder(new Border(new BorderStroke(Platform.getPreferences().getAccentColor(), BorderStrokeStyle.SOLID, null, null)));
        }
        event.consume();
    }
}
