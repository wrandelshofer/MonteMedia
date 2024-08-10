/*
 * @(#)Main.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.demo.moviereader;


import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Reads images from a movie with the Monte Media library and displays them in a JFrame.
 *
 * @author Werner Randelshofer
 */
public class ReadImagesFromAMovieMain {
    /**
     * Main method.
     * <p>
     * Arguments:
     * <pre>{@literal
     * <file>
     * }</pre>
     * <dl>
     *     <dt>file</dt>
     *     <dd>Optional. The movie file to be loaded.</dd>
     * </dl>
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        ReadImagesFromAMovieMain main = new ReadImagesFromAMovieMain();
        SwingUtilities.invokeAndWait(() -> {
            main.createFrame();
            if (args.length == 1) {
                File file = new File(args[0]);
                main.loadImages(file);
            }
        });
    }

    private JSlider slider;
    private List<BufferedImage> images = List.of();
    private JLabel imageLabel;
    private JLabel imageNbLabel;
    private JFrame frame;

    /**
     * Creates the frame.
     * <pre>
     *     +-----------------------+
     *     |      videoLabel       |
     *     +--------+--------------+
     *     | slider | imageNbLabel |
     *     +--------+--------------+
     * </pre>
     */
    private void createFrame() {
        frame = new JFrame("please drop a file in this window");
        JPanel panel = new JPanel(new BorderLayout());
        imageLabel = new JLabel();
        slider = new JSlider();
        imageNbLabel = new JLabel();
        JPanel controls = new JPanel(new GridBagLayout());
        controls.add(slider, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        controls.add(imageNbLabel, new GridBagConstraints(1, 0, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 4, 0, 8), 0, 0));
        panel.add(imageLabel, BorderLayout.CENTER);
        panel.add(controls, BorderLayout.SOUTH);
        frame.getContentPane().add(panel);
        ChangeListener changeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateImage();
            }
        };
        slider.getModel().addChangeListener(changeListener);
        changeListener.stateChanged(new ChangeEvent(slider.getModel()));
        DropTarget dt = new DropTarget() {
            @Override
            public synchronized void dragOver(DropTargetDragEvent evt) {
                if (evt.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    evt.acceptDrag(DnDConstants.ACTION_COPY);
                } else {
                    evt.rejectDrag();
                }
            }

            public void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    for (File file : droppedFiles) {
                        loadImages(file);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        panel.setDropTarget(dt);
        imageLabel.setDropTarget(dt);
        imageNbLabel.setDropTarget(dt);
        slider.setDropTarget(dt);

        frame.pack();
        frame.setVisible(true);
    }

    private void updateImage() {
        int value = slider.getValue();
        imageLabel.setIcon((value >= 0 && value < images.size()) ? new ImageIcon(images.get(value)) : null);
        imageNbLabel.setText(Integer.toString(value));
    }

    private void loadImages(File file) {
        frame.setTitle("Loading... " + file.getName());
        imageNbLabel.setText("-");
        new SwingWorker<List<BufferedImage>, Integer>() {
            @Override
            protected List<BufferedImage> doInBackground() throws Exception {
                return new ReadImagesFromAMovie().readImages(file);
            }

            @Override
            protected void done() {
                try {
                    images = get();
                } catch (InterruptedException | ExecutionException e) {
                    frame.setTitle("Couldn't load " + file.getName());
                    images = List.of();
                    e.printStackTrace();
                }
                slider.setMinimum(0);
                slider.setMaximum(Math.max(0, images.size() - 1));
                slider.setValue(0);
                updateImage();
                frame.pack();
                frame.setTitle(file.getName());
            }
        }.execute();
    }
}
