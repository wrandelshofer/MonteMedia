/*
 * @(#)Main.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.demo.moviereader;


import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Reads closed captions from a Movie with the Monte Media library.
 *
 * @author Werner Randelshofer
 */
public class ReadClosedCaptionsFromAMovieMain {
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
        ReadClosedCaptionsFromAMovieMain main = new ReadClosedCaptionsFromAMovieMain();
        SwingUtilities.invokeAndWait(() -> {
            main.createFrame();
            if (args.length == 1) {
                File file = new File(args[0]);
                main.loadClosedCaptions(file);
            }
        });
    }

    private JSlider slider;
    private List<String> closedCaptions = List.of();
    private JLabel ccLabel;
    private JLabel ccNbLabel;
    private JFrame frame;

    /**
     * Creates the frame.
     * <pre>
     *     +-----------------------+
     *     |      ccLabel          |
     *     +--------+--------------+
     *     | slider |    ccNbLabel |
     *     +--------+--------------+
     * </pre>
     */
    private void createFrame() {
        frame = new JFrame("please drop a file in this window");
        JPanel panel = new JPanel(new BorderLayout());
        ccLabel = new JLabel();
        ccLabel.setBackground(Color.BLUE);
        ccLabel.setBorder(new EmptyBorder(4, 4, 4, 4));
        ccLabel.setOpaque(true);
        slider = new JSlider();
        ccNbLabel = new JLabel();
        JPanel controls = new JPanel(new GridBagLayout());
        controls.add(slider, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        controls.add(ccNbLabel, new GridBagConstraints(1, 0, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 4, 0, 8), 0, 0));
        panel.add(ccLabel, BorderLayout.CENTER);
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
                        loadClosedCaptions(file);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        panel.setDropTarget(dt);
        ccLabel.setDropTarget(dt);
        ccNbLabel.setDropTarget(dt);
        slider.setDropTarget(dt);

        frame.pack();
        frame.setVisible(true);
    }

    private void updateImage() {
        int value = slider.getValue();
        ccLabel.setText((value >= 0 && value < closedCaptions.size()) ? closedCaptions.get(value) : null);
        ccNbLabel.setText(Integer.toString(value));
    }

    private void loadClosedCaptions(File file) {
        frame.setTitle("Loading... " + file.getName());
        ccNbLabel.setText("-");
        new SwingWorker<List<String>, Integer>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return new ReadClosedCaptionsFromAMovie().readClosedCaptions(file);
            }

            @Override
            protected void done() {
                try {
                    closedCaptions = get();
                } catch (InterruptedException | ExecutionException e) {
                    frame.setTitle("Couldn't load " + file.getName());
                    closedCaptions = List.of();
                    e.printStackTrace();
                }
                slider.setMinimum(0);
                slider.setMaximum(Math.max(0, closedCaptions.size() - 1));
                slider.setValue(0);
                updateImage();
                frame.pack();
                frame.setTitle(file.getName());
            }
        }.execute();
    }
}
