/*
 * @(#)CubeSceneApplication.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.cubescene;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.stage.Stage;
import org.monte.demo.javafx.colorquantizer.scene3d.RotateScene3DMouseHandler;

public class CubeSceneApplication extends Application {
    public static void main(String... args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        var root = createScene();
        var camera = createCamera();

        Scene scene = new Scene(root, 1400, 1000, true);
        scene.setCamera(camera);
        scene.setFill(Color.GREY);

        primaryStage.setTitle("Cube Scene Application");
        primaryStage.setScene(scene);
        primaryStage.show();
        new RotateScene3DMouseHandler(scene, root, camera);
    }

    private Group createScene() {
        var root = new Group();
        var box = new Box(10, 10, 10);
        WritableImage image = new WritableImage(2, 2);
        var w = image.getPixelWriter();
        w.setColor(0, 0, Color.RED);
        w.setColor(1, 0, Color.RED);
        w.setColor(0, 1, Color.RED);
        w.setColor(1, 1, Color.RED);
        //  w.setColor(0, 1, Color.ORANGE);
        //  w.setColor(0, 2, Color.GREEN);
        box.setMaterial(new PhongMaterial(Color.WHITE, image, null, null, null));
        root.getChildren().add(box);
        return root;
    }

    private PerspectiveCamera createCamera() {
        PerspectiveCamera cam = new PerspectiveCamera(true);
        cam.setTranslateZ(-33.333);
        cam.setNearClip(0.0);
        cam.setFarClip(2.0);
        return cam;
    }
}
