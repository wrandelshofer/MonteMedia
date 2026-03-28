/*
 * @(#)Main.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.colorquantizer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.monte.demo.javafx.colorquantizer.model.ColorQuantizerMainModel;

public class Main extends Application {
    private RenderedImageViewController imageView;
    private ColorDotsViewController colorDotsView;
    private MainInspectorController inspector;
    private Stage stage;

    /// The main() method is ignored in correctly deployed JavaFX application.
    /// main() serves only as fallback in case the application can not be
    /// launched through deployment artifacts, e.g., in IDEs with limited FX
    /// support. NetBeans ignores main().
    ///
    /// @param args the command line arguments
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        imageView = new RenderedImageViewController();
        colorDotsView = new ColorDotsViewController();
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getTabs().add(new Tab("Image", imageView.getRoot()));
        Tab colorsTab = new Tab("Colors", colorDotsView.getRoot());
        tabPane.getTabs().add(colorsTab);
        inspector = MainInspectorController.newInstance();
        BorderPane sp = new BorderPane();
        sp.setMouseTransparent(false);
        sp.setCenter(tabPane);
        sp.setRight(inspector.getRoot());
        Scene scene = new Scene(sp);
        scene.setFill(Color.BLACK);

        primaryStage.setTitle("Color Quantizer");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> System.exit(0));


        new DragSupport(imageView.getRoot(), Clipboard::hasFiles, this::onFilesDropped);
        new DragSupport(colorDotsView.getRoot(), Clipboard::hasFiles, this::onFilesDropped);

        colorDotsView.imageProperty().bind(getModel().renderedImageProperty());
        colorDotsView.getRoot().visibleProperty().bind(colorsTab.selectedProperty());
        imageView.imageProperty().bind(getModel().renderedImageProperty());
        imageView.zoomProperty().bind(getModel().zoomProperty());
        stage.titleProperty().bind(getModel().referenceFileProperty().map(p -> p == null ? null : p.getFileName().toString()));
    }

    private Boolean onFilesDropped(Dragboard dragboard) {
        var files = dragboard.getFiles();
        if (files.isEmpty()) return false;
        var file = files.getFirst();
        if (file.isFile()) {
            getModel().setReferenceFile(file.toPath());
            return true;
        }
        return false;
    }

    private ColorQuantizerMainModel getModel() {
        return inspector.getModel();
    }

}
