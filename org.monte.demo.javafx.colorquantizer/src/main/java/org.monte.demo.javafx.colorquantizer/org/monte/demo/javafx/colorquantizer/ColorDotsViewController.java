/*
 * @(#)ColorDotsViewController.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.colorquantizer;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import org.monte.demo.javafx.colorquantizer.model.ModelColorSpace;
import org.monte.media.color.OKLabColorSpace;
import org.monte.media.color.SrgbColorSpace;
import org.monte.media.math.Point3DFloat;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ColorDotsViewController {
    private final ObjectProperty<BufferedImage> image = new SimpleObjectProperty<>();

    private static final double CAMERA_INITIAL_DISTANCE = -450;
    private static final double CAMERA_INITIAL_X_ANGLE = 70.0;
    private static final double CAMERA_INITIAL_Y_ANGLE = 320.0;
    private static final double CAMERA_NEAR_CLIP = 0.1;
    private static final double CAMERA_FAR_CLIP = 10000.0;
    private static final double AXIS_LENGTH = 150.0;
    private static final double CONTROL_MULTIPLIER = 0.1;
    private static final double SHIFT_MULTIPLIER = 10.0;
    private static final double MOUSE_SPEED = 0.1;
    private static final double ROTATION_SPEED = 8.0;
    private static final double TRACK_SPEED = 0.3;
    final Xform axisGroup = new Xform();
    final Xform boxGroup = new Xform();
    Xform colorDotsGroup = new Xform();
    final Xform moleculeGroup = new Xform();
    final Xform world = new Xform();
    final PerspectiveCamera camera = new PerspectiveCamera(true);
    final Xform cameraXform = new Xform();
    final Xform cameraXform2 = new Xform();
    final Xform cameraXform3 = new Xform();
    private final ObjectProperty<ModelColorSpace> colorSpaceProperty = new SimpleObjectProperty<>(ModelColorSpace.SRGB);
    private final ListProperty<Color> palette = new SimpleListProperty<>(FXCollections.observableArrayList());
    double mousePosX;
    double mousePosY;
    double mouseOldX;
    double mouseOldY;
    double mouseDeltaX;
    double mouseDeltaY;
    private BorderPane borderPane = new BorderPane();
    private Group root = new Group();
    private SubScene scene = new SubScene(root, 1024, 768, true, SceneAntialiasing.BALANCED) {
        @Override
        public boolean isResizable() {
            return true;
        }

        @Override
        public void resize(double width, double height) {
            setWidth(width);
            setHeight(height);
        }
    };


    public ColorDotsViewController() {
        init();
    }


    public BufferedImage getImage() {
        return image.get();
    }

    public ObjectProperty<BufferedImage> imageProperty() {
        return image;
    }

    private void buildBoundingBox() {
        System.out.println("buildBox()");

        final PhongMaterial whiteMaterial = new PhongMaterial();
        whiteMaterial.setDiffuseColor(Color.DARKGREY);
        whiteMaterial.setSpecularColor(Color.WHITE);


        // build the corners of the cube
        for (int i = 0; i < 8; i++) {
            double signX = (i & 1) == 0 ? -1 : 1;
            double signY = (i & 2) == 0 ? -1 : 1;
            double signZ = (i & 4) == 0 ? -1 : 1;
            Sphere corner = new Sphere(1.0);
            corner.setTranslateX(signX * AXIS_LENGTH / 2);
            corner.setTranslateY(signY * AXIS_LENGTH / 2);
            corner.setTranslateZ(signZ * AXIS_LENGTH / 2);
            boxGroup.getChildren().add(corner);
        }

        // build the edges of the cube
        for (int i = 0; i < 4; i++) {
            double signA = (i & 1) == 0 ? -1 : 1;
            double signB = (i & 2) == 0 ? -1 : 1;
            Box edgeX = new Box(AXIS_LENGTH, 1, 1);
            edgeX.setMaterial(whiteMaterial);
            edgeX.setTranslateY(signA * AXIS_LENGTH / 2);
            edgeX.setTranslateZ(signB * AXIS_LENGTH / 2);
            boxGroup.getChildren().add(edgeX);
            Box edgeY = new Box(1, AXIS_LENGTH, 1);
            edgeY.setMaterial(whiteMaterial);
            edgeY.setTranslateX(signA * AXIS_LENGTH / 2);
            edgeY.setTranslateZ(signB * AXIS_LENGTH / 2);
            boxGroup.getChildren().add(edgeY);
            Box edgeZ = new Box(1, 1, AXIS_LENGTH);
            edgeZ.setMaterial(whiteMaterial);
            edgeZ.setTranslateX(signA * AXIS_LENGTH / 2);
            edgeZ.setTranslateY(signB * AXIS_LENGTH / 2);
            boxGroup.getChildren().add(edgeZ);


        }
        boxGroup.setVisible(true);
        world.getChildren().addAll(boxGroup);
    }

    private void buildCamera(SubScene scene) {
        System.out.println("buildCamera()");
        root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
        cameraXform3.setRotateZ(180.0);

        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);
        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
        cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
        cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);

        scene.setCamera(camera);
    }

    private void buildColorDots() {
        colorDotsGroup.setVisible(true);
        world.getChildren().add(colorDotsGroup);
    }

    public ObjectProperty<ModelColorSpace> colorSpacePropertyProperty() {
        return colorSpaceProperty;
    }

    public ModelColorSpace getColorSpaceProperty() {
        return colorSpaceProperty.get();
    }

    public ObservableList<Color> getPalette() {
        return palette.get();
    }

    public Node getRoot() {
        return borderPane;
    }

    private void handleKeyboard(SubScene scene, final Node root) {
        root.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case Z:
                        cameraXform2.t.setX(0.0);
                        cameraXform2.t.setY(0.0);
                        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
                        cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
                        cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
                        break;
                    case X:
                        axisGroup.setVisible(!axisGroup.isVisible());
                        break;
                    case V:
                        moleculeGroup.setVisible(!moleculeGroup.isVisible());
                        break;
                }
            }
        });
    }

    private void handleMouse(SubScene scene, final Node root) {
        root.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseOldX = me.getSceneX();
                mouseOldY = me.getSceneY();
            }
        });
        root.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                mouseOldX = mousePosX;
                mouseOldY = mousePosY;
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseDeltaX = (mousePosX - mouseOldX);
                mouseDeltaY = (mousePosY - mouseOldY);

                double modifier = 1.0;

                if (me.isControlDown()) {
                    modifier = CONTROL_MULTIPLIER;
                }
                if (me.isShiftDown()) {
                    modifier = SHIFT_MULTIPLIER;
                }
                if (me.isPrimaryButtonDown()) {
                    cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX * MOUSE_SPEED * modifier * ROTATION_SPEED);
                    cameraXform.rx.setAngle(cameraXform.rx.getAngle() + mouseDeltaY * MOUSE_SPEED * modifier * ROTATION_SPEED);
                } else if (me.isSecondaryButtonDown()) {
                    double z = camera.getTranslateZ();
                    double newZ = z + mouseDeltaX * MOUSE_SPEED * modifier;
                    camera.setTranslateZ(newZ);
                } else if (me.isMiddleButtonDown()) {
                    cameraXform2.t.setX(cameraXform2.t.getX() + mouseDeltaX * MOUSE_SPEED * modifier * TRACK_SPEED);
                    cameraXform2.t.setY(cameraXform2.t.getY() + mouseDeltaY * MOUSE_SPEED * modifier * TRACK_SPEED);
                }
            }
        });
    }

    private void init() {
        root.getChildren().add(world);
        root.setDepthTest(DepthTest.ENABLE);
        root.setMouseTransparent(false);
        borderPane.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
        borderPane.setCenter(scene);
        buildBoundingBox();
        buildColorDots();
        buildCamera(scene);
        handleKeyboard(scene, borderPane);
        handleMouse(scene, borderPane);

        colorSpaceProperty.addListener(o -> this.updateView());
        imageProperty().addListener(o -> this.updateView());
    }


    public void updateView() {
        if (getImage() == null) return;
        switch (getColorSpaceProperty()) {
            case SRGB -> {
                updateViewAsSRgb();
            }
            case OKLAB -> {
                updateViewAsOKLab();
            }
        }
    }


    private void updateViewAsOKLab() {
        updateViewAsLab(new OKLabColorSpace());
    }

    private void updateViewAsLab(java.awt.color.ColorSpace cs) {
        world.getChildren().remove(colorDotsGroup);
        BufferedImage img = getImage();
        int height = img.getHeight();
        int width = img.getWidth();
        HashSet<Point3DFloat> done = new HashSet<>();
        float[] lab = new float[3];
        List<Node> dots = new ArrayList<>();
        ComponentColorModel cm = new ComponentColorModel(cs, false, false, Transparency.OPAQUE, DataBuffer.TYPE_FLOAT);
        WritableRaster raster = cm.createCompatibleWritableRaster(width, height);
        var imgOkLab = new BufferedImage(cm, raster, false, null);
        var g = imgOkLab.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        int[] rgbArray = new int[width * height];
        img.getRGB(0, 0, width, height, rgbArray, 0, width);
        var d0 = ((DataBufferFloat) raster.getDataBuffer()).getData(0);
        for (int i = 0; i < rgbArray.length; i++) {
            int rgb = rgbArray[i];
            lab[0] = d0[i * 3];
            lab[1] = d0[i * 3 + 1];
            lab[2] = d0[i * 3 + 2];
            if (done.add(new Point3DFloat(lab[0], lab[1], lab[2]))) {


                Sphere dot;
                final PhongMaterial colorMaterial;
                dot = new Sphere(1.0, 8);
                colorMaterial = new PhongMaterial();
                dot.setMaterial(colorMaterial);
                dots.add(dot);
                dot.setTranslateX(-AXIS_LENGTH / 2 + AXIS_LENGTH * (lab[1] + 0.5f));
                dot.setTranslateY(-AXIS_LENGTH / 2 + AXIS_LENGTH * lab[0]);
                dot.setTranslateZ(-AXIS_LENGTH / 2 + AXIS_LENGTH * (lab[2] + 0.5f));
                colorMaterial.setDiffuseColor(Color.rgb((rgb >>> 16) & 0xff, (rgb >>> 8) & 0xff, rgb & 0xff));
            }
        }
        colorDotsGroup = new Xform(dots);
        world.getChildren().add(colorDotsGroup);

        System.out.println("number of distinct colors " + done.size());
    }


    private void updateViewAsSRgb() {
        //updateViewAsRgb(ColorSpace.getInstance(ColorSpace.CS_sRGB));
        updateViewAsRgb(SrgbColorSpace.INSTANCE);
    }

    private void updateViewAsRgb(ColorSpace cs) {
        world.getChildren().remove(colorDotsGroup);
        BufferedImage img = getImage();
        int height = img.getHeight();
        int width = img.getWidth();
        HashSet<Point3DFloat> done = new HashSet<>();
        float[] inputF = new float[3];
        float[] csf = new float[3];
        List<Node> dots = new ArrayList<>();
        ComponentColorModel cm = new ComponentColorModel(cs, false, false, Transparency.OPAQUE, DataBuffer.TYPE_FLOAT);
        WritableRaster rasterCs = cm.createCompatibleWritableRaster(width, height);
        var imgCs = new BufferedImage(cm, rasterCs, false, null);
        var g = imgCs.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        int[] rgbArray = new int[width * height];
        img.getRGB(0, 0, width, height, rgbArray, 0, width);
        var d0 = ((DataBufferFloat) rasterCs.getDataBuffer()).getData(0);
        for (int i = 0; i < rgbArray.length; i++) {
            int rgb = rgbArray[i];
            csf[0] = d0[i * 3];
            csf[1] = d0[i * 3 + 1];
            csf[2] = d0[i * 3 + 2];
            if (done.add(new Point3DFloat(csf[0], csf[1], csf[2]))) {
                Sphere dot;
                dot = new Sphere(1.0, 8);
                dot.setTranslateX(-AXIS_LENGTH / 2 + AXIS_LENGTH * csf[0]);
                dot.setTranslateY(-AXIS_LENGTH / 2 + AXIS_LENGTH * csf[1]);
                dot.setTranslateZ(-AXIS_LENGTH / 2 + AXIS_LENGTH * csf[2]);

                final PhongMaterial colorMaterial = new PhongMaterial();
                colorMaterial.setDiffuseColor(Color.rgb((rgb >>> 16) & 0xff, (rgb >>> 8) & 0xff, rgb & 0xff));
                dot.setMaterial(colorMaterial);
                dots.add(dot);
            }
        }
        colorDotsGroup = new Xform(dots);
        world.getChildren().add(colorDotsGroup);
        System.out.println("number of distinct colors " + done.size());

    }

    record ColorData(float[][] X, float[] xWeights) {
    }
}
