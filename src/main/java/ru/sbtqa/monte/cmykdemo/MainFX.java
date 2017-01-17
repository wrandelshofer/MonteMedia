/* @(#)Main.java
 * Copyright © 2011 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.cmykdemo;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import ru.sbtqa.monte.media.color.ColorSpaces;
import ru.sbtqa.monte.media.image.Images;
import ru.sbtqa.monte.media.jpeg.CMYKJPEGImageReader;

/**
 * MainFX.
 *
 * @author Werner Randelshofer
 */
public class MainFX extends Application {

    private ImageView imageView;
    private Label dropLabel;
    private Label infoLabel;
    private BorderPane root;
    private Stage stage;

    private static void checkForKCMS() {
        System.out.println("Running on Java " + System.getProperty("java.version") + " (" + System.getProperty("java.vm.version") + ")");
        String cmm = System.getProperty("sun.java2d.cmm");
        if (!"sun.java2d.cmm.kcms.KcmsServiceProvider".equals(cmm)) {
            if (cmm != null) {
                System.out.println("Your VM is running with the following color management system:");
                System.out.println(cmm);
            }
            System.out.println("For better color conversion performance, you may want to try the following VM option:");
            System.out.println("-Dsun.java2d.cmm=sun.java2d.cmm.kcms.KcmsServiceProvider");
        }
    }

    /**
     * Resizable image view.
     */
    private static class ResizableImageView extends ImageView {

        public ResizableImageView() {
            setPreserveRatio(true);
            setSmooth(true);
            setCache(true);
            setManaged(true);

        }

        @Override
        public double minWidth(double height) {
            return 0;
        }

        @Override
        public double minHeight(double height) {
            return 0;
        }

        @Override
        public double prefWidth(double height) {
            return getImage() == null ? 0 : getImage().getWidth();
        }

        @Override
        public double prefHeight(double height) {
            return getImage() == null ? 0 : getImage().getHeight();
        }

        @Override
        public boolean isResizable() {
            return true;
        }

        @Override
        public void resize(double width, double height) {
            super.resize(width, height);
            setFitWidth(width);
            setFitHeight(height);
        }
    };

    @Override
    public void start(Stage primaryStage) {
        checkForKCMS();
        stage = primaryStage;

        imageView = new ResizableImageView();

        dropLabel = new Label();
        dropLabel.setText("Drop CMYK image here");
        dropLabel.setWrapText(true);

        infoLabel = new Label();
        infoLabel.setWrapText(true);

        root = new BorderPane();
        root.setCenter(dropLabel);
        root.addEventHandler(DragEvent.ANY, new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                onDragEvent(event);

            }
        });

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("MonteMedia CMYK Demo " + MainFX.class.getPackage().getImplementationVersion());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Handles drag events.
     *
     * @param event a drag event
     */
    private void onDragEvent(DragEvent event) {
        EventType<DragEvent> type = event.getEventType();
        if (type == DragEvent.DRAG_EXITED) {
            dropLabel.setUnderline(false);
        } else if (type == DragEvent.DRAG_OVER) {
            if (event.getDragboard().hasContent(DataFormat.FILES)) {
                event.acceptTransferModes(TransferMode.COPY);
                dropLabel.setText("Drop CMYK image here");
                dropLabel.setUnderline(true);
            } else {
                event.acceptTransferModes(TransferMode.NONE);
            }
        } else if (type == DragEvent.DRAG_DROPPED) {
            if (event.getDragboard().hasContent(DataFormat.FILES)) {
                event.acceptTransferModes(TransferMode.COPY);
                @SuppressWarnings("unchecked")
                List<File> content = (List<File>) event.getDragboard().getContent(DataFormat.FILES);
                for (File f : content) {
                    loadAndDisplayImage(f);
                }
            } else {
                event.acceptTransferModes(TransferMode.NONE);
            }
        }
    }

    /**
     * Loads an image and displays it.
     *
     * @param file a file
     */
    private void loadAndDisplayImage(final File file) {
        stage.setTitle(file.getName());
        root.setCenter(dropLabel);
        root.setBottom(null);
        imageView.setImage(null);

        Platform.runLater(new RunLater(dropLabel, "..."));

        final long start = System.currentTimeMillis();

        CompletableFuture.supplyAsync(new Supplier<Object[]>() {
            @Override
            public Object[] get() {
                try {
                    Platform.runLater(new RunLater(dropLabel, "Loading..."));
                    BufferedImage sourceImage = loadImage(file);

                    Platform.runLater(new RunLater(dropLabel, "Converting image to RGB..."));
                    BufferedImage rgbImage = convertToRGB(sourceImage);

                    Platform.runLater(new RunLater(dropLabel, "Converting image to FX..."));
                    WritableImage fxImage = convertToFX(rgbImage);

                    return new Object[]{sourceImage, fxImage};
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        });

        CompletableFuture.supplyAsync(new Supplier<Object[]>() {
            @Override
            public Object[] get() {
                try {
                    Platform.runLater(new RunLater(dropLabel, "Loading..."));
                    BufferedImage sourceImage = loadImage(file);

                    Platform.runLater(new RunLater(dropLabel, "Converting image to RGB..."));
                    BufferedImage rgbImage = convertToRGB(sourceImage);

                    Platform.runLater(new RunLater(dropLabel, "Converting image to FX..."));
                    WritableImage fxImage = convertToFX(rgbImage);

                    return new Object[]{sourceImage, fxImage};
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }).handleAsync(new BiFunction<Object[], Throwable, Object[]>() {

            @Override
            public Object[] apply(Object[] images, Throwable ex) {
                long end = System.currentTimeMillis();
                System.out.println("elapsed:" + (end - start));

                if (ex != null) {
                    Throwable cause = ex;
                    while ((cause instanceof UncheckedIOException) || (cause instanceof CompletionException)) {
                        cause = cause.getCause();
                    }
                    String msg = (cause == null) ? ex.getLocalizedMessage() : cause.getLocalizedMessage();
                    dropLabel.setText(msg != null ? msg : ex.toString());
                    ex.printStackTrace();
                } else {
                    BufferedImage sourceImage = (BufferedImage) images[0];
                    WritableImage fxImage = (WritableImage) images[1];
                    if (sourceImage == null) {
                        Platform.runLater(new RunLater(dropLabel, "No image found"));
                    } else {
                        imageView.setImage(fxImage);
                        root.setCenter(imageView);
                        infoLabel.setText("Dimension: " + sourceImage.getWidth() + " x " + sourceImage.getHeight()
                                + "\n" + sourceImage.getColorModel()
                                + "\nColor Space: " + ColorSpaces.toString(sourceImage.getColorModel().getColorSpace()).replace(',', ' '));

                        root.setBottom(infoLabel);
                    }
                }
                return null;
            }
        }
        );
    }

    private WritableImage convertToFX(BufferedImage bufferedImage) {
        if (bufferedImage == null) {
            return null;
        }
        long start = System.currentTimeMillis();

        WritableImage fxImage = Images.toFXImage(bufferedImage, null);

        long end = System.currentTimeMillis();
        System.out.println("  convert to FX ms:" + (end - start));
        return fxImage;
    }

    private BufferedImage convertToRGB(BufferedImage bufferedImage) {
        if (bufferedImage == null) {
            return null;
        }

        long start = System.currentTimeMillis();

        BufferedImage rgbImage = Images.toRGBImage(bufferedImage);

        long end = System.currentTimeMillis();
        System.out.println("  convert to RGB ms:" + (end - start));
        System.out.println("  source image CM:" + bufferedImage.getColorModel());
        System.out.println("  RGB image CM:" + rgbImage.getColorModel());
        return rgbImage;
    }

    private BufferedImage loadImage(final File file) throws IOException {
        System.out.println("loading " + file);
        try (ImageInputStream iis = new FileImageInputStream(file)) {
            long start = System.currentTimeMillis();
            ImageReader r = new CMYKJPEGImageReader();
            r.setInput(iis);
            BufferedImage cmykImage = r.read(0);
            long end = System.currentTimeMillis();
            System.out.println("  load ms:" + (end - start));
            return cmykImage;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private static class RunLater implements Runnable {

        private final Label dropLabel;
        private final String text;

        public RunLater(Label dropLabel, String text) {
            this.text = text;
            this.dropLabel = dropLabel;
        }

        @Override
        public void run() {
            dropLabel.setText(text);

        }

    }

}
