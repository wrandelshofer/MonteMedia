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
import javafx.scene.AmbientLight;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import org.monte.demo.javafx.colorquantizer.model.ModelColorSpace;
import org.monte.demo.javafx.colorquantizer.scene3d.Group3D;
import org.monte.demo.javafx.colorquantizer.scene3d.IcosphereMeshBuilder;
import org.monte.demo.javafx.colorquantizer.scene3d.RotateScene3DMouseHandler;
import org.monte.media.color.OKLabColorSpace;
import org.monte.media.color.op.ColorSpaceConvertOp;
import org.monte.media.math.Point3DFloat;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.IndexColorModel;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class ColorDotsViewController {
    private final ObjectProperty<BufferedImage> image = new SimpleObjectProperty<>();

    private static final double CAMERA_INITIAL_DISTANCE = -450;
    private static final double CAMERA_INITIAL_X_ANGLE = 0;
    private static final double CAMERA_INITIAL_Y_ANGLE = 0.0;
    private static final double CAMERA_NEAR_CLIP = 0.1;
    private static final double CAMERA_FAR_CLIP = 10000.0;
    private static final double AXIS_LENGTH = 150.0;
    private static final TriangleMesh DOT_MESH = new IcosphereMeshBuilder().icosahedron(1);
    //private static final TriangleMesh DOT_MESH = new PyramidMeshBuilder().pyramid(2, 2, 2);

    final Group3D boxGroup = new Group3D();
    Group3D colorDotsGroup = new Group3D();

    final Group3D world = new Group3D();
    final PerspectiveCamera camera = new PerspectiveCamera(true);
    final Group3D cameraXform = new Group3D();
    final Group3D cameraXform2 = new Group3D();
    final Group3D cameraXform3 = new Group3D();
    private final ObjectProperty<ModelColorSpace> colorSpaceProperty = new SimpleObjectProperty<>(ModelColorSpace.RGB);
    private final ListProperty<Color> palette = new SimpleListProperty<>(FXCollections.observableArrayList());

    private BorderPane borderPane = new BorderPane();
    private Group subSceneRoot = new Group();
    private SubScene subScene = new SubScene(subSceneRoot, 1024, 768, true, SceneAntialiasing.BALANCED) {
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
        final PhongMaterial whiteMaterial = createMaterial(Color.DARKGREY);
        whiteMaterial.setSpecularColor(Color.WHITE);


        // build the corners of the cube
        for (int i = 0; i < 8; i++) {
            double signX = (i & 1) == 0 ? -1 : 1;
            double signY = (i & 2) == 0 ? -1 : 1;
            double signZ = (i & 4) == 0 ? -1 : 1;
            Sphere corner = new Sphere(0.5);
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
        subSceneRoot.getChildren().add(cameraXform);
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
        //cameraXform3.setRotateZ(180.0);

        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);
        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
        cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
        cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);

        scene.setCamera(camera);
        // Add an AmbientLight to the scene to ensure the objects are evenly lit, as if they were self-illuminating
        AmbientLight ambient = new AmbientLight(Color.WHITE);
        world.getChildren().add(ambient);
    }

    private void buildColorDots() {
        colorDotsGroup.setVisible(true);
        world.getChildren().add(colorDotsGroup);
    }

    public ObjectProperty<ModelColorSpace> colorSpacePropertyProperty() {
        return colorSpaceProperty;
    }

    public ModelColorSpace getColorSpace() {
        return colorSpaceProperty.get();
    }

    public void setColorSpace(ModelColorSpace v) {
        colorSpaceProperty.set(v);
    }

    public ObservableList<Color> getPalette() {
        return palette.get();
    }

    public Node getRoot() {
        return borderPane;
    }

    private void handleKeyboard(final Node root) {


        root.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2 && event.isShiftDown()) {
                    setColorSpace(switch (getColorSpace()) {
                        case RGB -> ModelColorSpace.XYZ;
                        case XYZ -> ModelColorSpace.LAB;
                        default -> ModelColorSpace.RGB;
                    });
                }
            }
        });
    }


    private void handleMouse(SubScene scene, final Node root) {
        root.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                borderPane.requestFocus();
            }
        });
    }


    private void init() {
        subSceneRoot.getChildren().add(world);
        subSceneRoot.setDepthTest(DepthTest.ENABLE);
        subSceneRoot.setMouseTransparent(false);
        borderPane.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
        borderPane.setCenter(subScene);


        borderPane.setFocusTraversable(true);
        buildBoundingBox();
        buildColorDots();
        buildCamera(subScene);
        handleKeyboard(borderPane);
        handleMouse(subScene, borderPane);
        borderPane.visibleProperty().addListener((o, oldVal, newVal) -> {
            System.out.println("ColorDotsViewController borderPane visibleProperty(): " + newVal);
            if (newVal) updateViewIfNeeded();
        });
        new RotateScene3DMouseHandler(borderPane, world, camera);

        // Create the group
        ToggleGroup group = new ToggleGroup();

        // Create 3 ToggleButtons (or RadioButtons)
        ToggleButton tb1 = new ToggleButton("RGB");
        tb1.getProperties().put("colorSpace", ModelColorSpace.RGB);
        ToggleButton tb2 = new ToggleButton("XYZ");
        tb2.getProperties().put("colorSpace", ModelColorSpace.XYZ);
        ToggleButton tb3 = new ToggleButton("OKLAB");
        tb3.getProperties().put("colorSpace", ModelColorSpace.LAB);

        // Add them to the group
        tb1.setToggleGroup(group);
        tb2.setToggleGroup(group);
        tb3.setToggleGroup(group);

        // Optional: Set a default selection
        tb1.setSelected(true);
        var hbox = new HBox(tb1, tb2, tb3);
        borderPane.setBottom(hbox);

        group.selectedToggleProperty().addListener((o, oldVal, newVal) -> {
            if (newVal != null) {
                setColorSpace((ModelColorSpace) newVal.getProperties().get("colorSpace"));
            }
        });

        colorSpaceProperty.addListener(o -> this.updateViewLater());
        imageProperty().addListener(o -> this.updateViewLater());
        subSceneRoot.visibleProperty().addListener(o -> updateViewIfNeeded());
    }

    private boolean needsUpdate;

    public void updateViewLater() {
        if (!borderPane.isVisible()) {
            needsUpdate = true;
            return;
        }
        updateView();
    }

    public void updateViewIfNeeded() {
        if (needsUpdate) {
            updateView();
        }
    }

    public void updateView() {
        try {
            needsUpdate = false;
            if (getImage() == null) return;
            switch (getColorSpace()) {
                case RGB -> {
                    updateView(getImage().getColorModel().getColorSpace(), 1, 1, 1, 0, 0, 0);
                }
                case XYZ -> {
                    updateView(ColorSpace.getInstance(ColorSpace.CS_CIEXYZ), 1, 1, 1, 0, 0, 0);
                }
                case LAB -> {
                    updateView(OKLabColorSpace.getInstance(), 1, 1, 1, 0.5f, 0.5f, 0.5f);
                    // updateView(OKLchColorSpace.getInstance(), 1, 1, 1, 0.5f, 0.5f, 0f);
                    //updateView(ColorSpace.getInstance(ColorSpace.CS_PYCC), 1, 1, 1, 0, 0, 0);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void updateView(java.awt.color.ColorSpace cs, float xscale, float yscale, float zscale, float xoffset, float yoffset, float zoffset) {
        world.getChildren().remove(colorDotsGroup);
        List<Node> dots = new ArrayList<>();
        var componentImage = toComponentImage(getImage(), cs);
        DataBuffer dataBuffer = componentImage.getRaster().getDataBuffer();
//        var componentImage = getImage();
//        DataBuffer dataBuffer = getImage().getRaster().getDataBuffer();
        SampleModel sampleModel = componentImage.getRaster().getSampleModel();
        int transferType = sampleModel.getTransferType();
        float[] colorvalue = new float[componentImage.getColorModel().getNumComponents()];
        int[] colorvalueInt = new int[componentImage.getColorModel().getNumComponents()];
        float[] rgbvalue = new float[3];
        HashSet<Point3DFloat> done = new HashSet<>();
        int height = componentImage.getHeight();
        int width = componentImage.getWidth();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                if (transferType == DataBuffer.TYPE_INT) {
                    colorvalueInt = (int[]) sampleModel.getDataElements(x, y, colorvalueInt, dataBuffer);
                    int rgb = componentImage.getColorModel().getRGB(colorvalueInt);
                    colorvalue[0] = (float) ((rgb & 0xff0000) >>> 16) / 255f;
                    colorvalue[1] = (float) ((rgb & 0xff00) >>> 8) / 255f;
                    colorvalue[2] = (float) ((rgb & 0xff) >>> 0) / 255f;
                } else if (transferType == DataBuffer.TYPE_FLOAT) {
                    colorvalue = (float[]) sampleModel.getDataElements(x, y, colorvalue, dataBuffer);
                }
                if (done.add(new Point3DFloat(colorvalue[0], colorvalue[1], colorvalue[2]))) {
                    System.arraycopy(colorvalue, 0, rgbvalue, 0, rgbvalue.length);
                    rgbvalue = cs.toRGB(rgbvalue);
                    Shape3D dot = new MeshView(DOT_MESH);
                    Color color = Color.color(Math.clamp(rgbvalue[0], 0, 1), Math.clamp(rgbvalue[1], 0, 1), Math.clamp(rgbvalue[2], 0, 1));
                    final PhongMaterial material = createMaterial(color);
                    dot.setMaterial(material);

                    dots.add(dot);
                    dot.setTranslateX(-AXIS_LENGTH / 2 + AXIS_LENGTH * (colorvalue[1] + xoffset));
                    dot.setTranslateY(AXIS_LENGTH / 2 + AXIS_LENGTH * -colorvalue[0] + yoffset);
                    dot.setTranslateZ(AXIS_LENGTH / 2 + AXIS_LENGTH * -(colorvalue[2] + zoffset));
                }
            }
        }
        System.out.println("OKLab #colors=" + dots.size());
        colorDotsGroup = new Group3D(dots);
        world.getChildren().add(colorDotsGroup);
    }


    private static BufferedImage toComponentImage(BufferedImage image, ColorSpace cs) {
        return new ColorSpaceConvertOp(cs).filter(image, null);
        //var cm = new ComponentColorModel(cs, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_FLOAT);
        // return Images.toImageWithColorModel_usingColorConvertOp(image, cm);
        //return FloatImages.convertImage(image, cm, null);
    }

    private void updateViewAsRgb() {
        //updateViewAsRgb(ColorSpace.getInstance(ColorSpace.CS_sRGB));
        // updateViewAsRgb(SrgbColorSpace.getInstance());
        updateViewAsRgb(getImage().getColorModel().getColorSpace());
        //updateViewAsRgb(Rec709ColorSpace.INSTANCE);
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
        int[] rgbArray;
        float[] pixels;
        if (img.getColorModel() instanceof IndexColorModel icm) {
            rgbArray = new int[icm.getMapSize()];
            pixels = new float[icm.getMapSize() * 3];
            icm.getRGBs(rgbArray);
            for (int i = 0; i < rgbArray.length; i++) {
                inputF[0] = ((rgbArray[i] & 0xff0000) >>> 16) / 255f;
                inputF[1] = ((rgbArray[i] & 0xff00) >>> 8) / 255f;
                inputF[2] = ((rgbArray[i] & 0xff)) / 255f;
                csf = cs.fromRGB(inputF);
                System.arraycopy(csf, 0, pixels, i * 3, 3);
            }
        } else {
            var imgCs = new BufferedImage(cm, rasterCs, false, null);
            var g = imgCs.createGraphics();
            g.drawImage(img, 0, 0, null);
            g.dispose();
            rgbArray = new int[width * height];
            img.getRGB(0, 0, width, height, rgbArray, 0, width);
            pixels = ((DataBufferFloat) rasterCs.getDataBuffer()).getData(0);
        }
        for (int i = 0; i < rgbArray.length; i++) {
            int rgb = rgbArray[i];
            csf[0] = pixels[i * 3];
            csf[1] = pixels[i * 3 + 1];
            csf[2] = pixels[i * 3 + 2];
            if (done.add(new Point3DFloat(csf[0], csf[1], csf[2]))) {
                //Shape3D dot= new Sphere(1.0, 5);
                Shape3D dot = new MeshView(DOT_MESH);
                dot.setTranslateX(-AXIS_LENGTH / 2 + AXIS_LENGTH * csf[0]);
                dot.setTranslateY(AXIS_LENGTH / 2 + AXIS_LENGTH * -csf[1]);
                dot.setTranslateZ(AXIS_LENGTH / 2 + AXIS_LENGTH * -csf[2]);

                final PhongMaterial material = createMaterial(Color.rgb((rgb >>> 16) & 0xff, (rgb >>> 8) & 0xff, rgb & 0xff));
                dot.setMaterial(material);
                dots.add(dot);
            }
        }
        System.out.println("RGB #colors=" + dots.size());
        colorDotsGroup = new Group3D(dots);
        world.getChildren().add(colorDotsGroup);
    }

    private static PhongMaterial createMaterial(Color color) {
        final PhongMaterial material = new PhongMaterial();

        // Make the material self-illuminant - This is what we want, but it is very slow
        /*
        WritableImage selfIllumImage = new WritableImage(1, 1);
        selfIllumImage.getPixelWriter().setColor(0, 0, color);
        material.setSelfIlluminationMap(selfIllumImage);
        material.setDiffuseColor(Color.BLACK);
         */
        material.setDiffuseColor(color);
        material.setSpecularColor(color);

        return material;
    }

    record ColorData(float[][] X, float[] xWeights) {
    }
}
