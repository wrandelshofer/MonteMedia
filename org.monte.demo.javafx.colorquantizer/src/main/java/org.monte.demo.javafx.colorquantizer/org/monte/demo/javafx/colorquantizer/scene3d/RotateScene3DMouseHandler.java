/*
 * @(#)RotateScene3DMouseHandler.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.colorquantizer.scene3d;

import javafx.animation.AnimationTimer;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Affine;

public class RotateScene3DMouseHandler {
    private final Node root;
    private boolean dragged = false;
    private double prevX;
    private double prevY;
    private final Affine rootAffineTransform = new Affine();
    private final ObservableValue<Number> sceneWidth;
    private final ObservableValue<Number> sceneHeight;
    private final J3DIMatrix4 mouseRotationMatrix = new J3DIMatrix4();
    private final J3DIMatrix4 sceneRotationMatrix = new J3DIMatrix4();

    private AnimationTimer animationTimer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            RotateScene3DMouseHandler.this.onTime(now);
        }
    };
    private double before;
    private final EventHandler<MouseEvent> onMouseHandler = this::onMouse;
    private long prevTimeStamp;
    private J3DIVector3 v = new J3DIVector3();
    private double damping = 1.0;
    private final PerspectiveCamera camera;

    public RotateScene3DMouseHandler(SubScene scene, Node rootOf3DScene, PerspectiveCamera camera) {
        sceneWidth = scene.widthProperty();
        sceneHeight = scene.heightProperty();
        this.root = rootOf3DScene;
        this.camera = camera;
        rootOf3DScene.getTransforms().setAll(rootAffineTransform);
        updateRoot(sceneRotationMatrix);
        scene.addEventHandler(MouseEvent.ANY, onMouseHandler);
    }

    public RotateScene3DMouseHandler(Parent mouseTarget, Node rootOf3DScene, PerspectiveCamera camera) {
        sceneWidth = mouseTarget.layoutBoundsProperty().map(Bounds::getWidth);
        sceneHeight = mouseTarget.layoutBoundsProperty().map(Bounds::getHeight);
        this.root = rootOf3DScene;
        this.camera = camera;
        rootOf3DScene.getTransforms().setAll(rootAffineTransform);
        updateRoot(sceneRotationMatrix);
        mouseTarget.addEventHandler(MouseEvent.ANY, onMouseHandler);
    }

    public RotateScene3DMouseHandler(Scene scene, Node rootOf3DScene, PerspectiveCamera camera) {
        sceneWidth = scene.widthProperty();
        sceneHeight = scene.heightProperty();
        this.root = rootOf3DScene;
        this.camera = camera;
        rootOf3DScene.getTransforms().setAll(rootAffineTransform);
        updateRoot(sceneRotationMatrix);
        scene.addEventHandler(MouseEvent.ANY, onMouseHandler);
    }

    private void updateRoot(J3DIMatrix4 matrix) {
        double[] a = matrix.getAsRowMajorArray();
        rootAffineTransform.setMxx(a[0]);
        rootAffineTransform.setMxy(a[1]);
        rootAffineTransform.setMxz(a[2]);
        rootAffineTransform.setTx(a[3]);
        rootAffineTransform.setMyx(a[4]);
        rootAffineTransform.setMyy(a[5]);
        rootAffineTransform.setMyz(a[6]);
        rootAffineTransform.setTy(a[7]);
        rootAffineTransform.setMzx(a[8]);
        rootAffineTransform.setMzy(a[9]);
        rootAffineTransform.setMzz(a[10]);
        rootAffineTransform.setTz(a[11]);
    }

    public void start() {
        before = System.nanoTime();
        animationTimer.start();
    }

    private void onMouse(MouseEvent mouseEvent) {
        if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) onMouseClicked(mouseEvent);
        else if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) onMousePressed(mouseEvent);
        else if (mouseEvent.getEventType() == MouseEvent.MOUSE_DRAGGED) onMouseDragged(mouseEvent);
        else if (mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED) onMouseReleased(mouseEvent);
    }

    void stop() {
        animationTimer.stop();
    }

    void onTime(long now) {
        if (v.norm() < 0.001) {
            stop();
            return;
        }

        var h = (now - before) / 1_000_000_000.0;
        var rm = new J3DIVector3(v).multiply(h).exphat();
        rm.multiply(sceneRotationMatrix);
        sceneRotationMatrix.load(rm);
        var vv = new J3DIVector3(v);
        if (h * damping < 1) {
            v.subtract(vv.multiply(h * damping));
        } else {
            v.load(0, 0, 0);
        }
        updateRoot(sceneRotationMatrix);
        before = now;
    }

    void onMousePressed(MouseEvent e) {
        prevX = e.getX();
        prevY = e.getY();
        dragged = false;
        stop();
    }

    void onMouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            sceneRotationMatrix.makeIdentity();
            updateRoot(sceneRotationMatrix);
        }
    }

    void onMouseDragged(MouseEvent e) {
        var x = e.getX();
        var y = e.getY();
        dragged = true;
        start();

        var dxScreen = (this.prevY - y);
        var dyScreen = (this.prevX - x);

        var dCamera = camera.localToParent(dxScreen, dyScreen);
        var dxCamera = dCamera.getX();
        var dyCamera = dCamera.getY();

        double width = sceneWidth.getValue().doubleValue();
        double height = sceneHeight.getValue().doubleValue();
        var dxAngle = dxCamera * (360 / Math.min(width, height));
        var dyAngle = dyCamera * (360 / Math.min(width, height));

        mouseRotationMatrix.makeIdentity();
        mouseRotationMatrix.rotate(dyAngle, 0, 1, 0);
        mouseRotationMatrix.rotate(dxAngle, -1, 0, 0);
        long mouseTimestamp = System.nanoTime();

        // mouseTimestep is typically 1/60
        var mouseTimestep = (mouseTimestamp - this.prevTimeStamp) / 1_000_000_000.0;

        v = mouseRotationMatrix.loghat().divide(Math.max(0.01, mouseTimestep));
        mouseRotationMatrix.multiply(sceneRotationMatrix);
        sceneRotationMatrix.load(mouseRotationMatrix);

        prevX = x;
        prevY = y;
        prevTimeStamp = mouseTimestamp;
    }

    void onMouseReleased(MouseEvent e) {
        if (!dragged) {
            stop();
        }
    }

}
