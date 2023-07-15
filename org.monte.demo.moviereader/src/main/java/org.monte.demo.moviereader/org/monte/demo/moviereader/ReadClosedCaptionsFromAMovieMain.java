/*
 * @(#)Main.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.demo.moviereader;


import org.monte.media.av.Buffer;
import org.monte.media.av.BufferFlag;
import org.monte.media.av.Format;
import org.monte.media.av.MovieReader;
import org.monte.media.av.Registry;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DataClassKey;

/**
 * Reads closed captions from a Movie with the Monte Media library.
 *
 * @author Werner Randelshofer
 */
public class ReadClosedCaptionsFromAMovieMain {
    /**
     * Main method.
     *
     * @param args the command line arguments
     */
    public static void main(String args[]) throws IOException {
        if (args.length != 1) {
            System.err.println("""
                    Usage:
                        Main <inputfile>
                                        
                    Where arguments are:  
                        inputfile
                            the path to a movie file (AVI)
                    """);
        }
        File file = new File(args[0]);
        ReadClosedCaptionsFromAMovieMain main = new ReadClosedCaptionsFromAMovieMain();
        SwingUtilities.invokeLater(() -> {
            main.createFrame();
            main.loadCaptions(file);
        });
    }

    private JSlider slider;
    private List<BufferedImage> images = List.of();
    private JLabel imageLabel;
    private JFrame frame;

    private void createFrame() {
        frame = new JFrame("please drop a file in this window");
        JPanel panel = new JPanel(new BorderLayout());
        imageLabel = new JLabel();
        slider = new JSlider();
        panel.add(imageLabel, BorderLayout.CENTER);
        panel.add(slider, BorderLayout.SOUTH);
        frame.getContentPane().add(panel);
        ChangeListener changeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateImage();
            }
        };
        slider.getModel().addChangeListener(changeListener);
        changeListener.stateChanged(new ChangeEvent(slider.getModel()));
        panel.setDropTarget(new DropTarget() {
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
                    List<File> droppedFiles = (List<File>)
                            evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    for (File file : droppedFiles) {
                        loadCaptions(file);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        frame.pack();
        frame.setVisible(true);
    }

    private void updateImage() {
        int value = slider.getValue();
        imageLabel.setIcon((value >= 0 && value < images.size()) ? new ImageIcon(images.get(value)) : null);
    }

    private void loadCaptions(File file) {
        frame.setTitle("Loading... " + file.getName());
        new SwingWorker<List<BufferedImage>, Integer>() {
            @Override
            protected List<BufferedImage> doInBackground() throws Exception {
                return readCaptions(file);
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
                slider.setMaximum(images.size() - 1);
                slider.setValue(0);
                updateImage();
                frame.pack();
                frame.setTitle(file.getName());
            }
        }.execute();
    }

    private List<BufferedImage> readCaptions(File file) throws IOException {
        List<BufferedImage> frames = new ArrayList<BufferedImage>();
        MovieReader in = Registry.getInstance().getReader(file);
        if (in == null)
            throw new IOException("could not find a reader for file " + file);
        Format format = new Format(DataClassKey, BufferedImage.class);

        int track = in.findTrack(0, new Format(EncodingKey, "clcp"));
        if (track < 0) {
            for (int i = 0; i < in.getTrackCount(); i++) {
                System.out.println("track " + i + " " + in.getFormat(i));
            }
            throw new IOException("could not find a closed caption track in file " + file);
        }
        try {
            Buffer inbuf = new Buffer();
            Buffer codecbuf = new Buffer();
            do {
                in.read(track, inbuf);
                if (!codecbuf.isFlag(BufferFlag.DISCARD)) {
                    byte[] data = (byte[]) inbuf.data;
                    for (int i = 0; i < data.length; i++) {
                        data[i] = (byte) (data[i] & 0x7f);
                    }
                    System.out.println(inbuf.timeStamp + " " + inbuf.sampleDuration + " " + inbuf.flags + " " + new String((byte[]) data, inbuf.offset, inbuf.length));
                }
            } while (!inbuf.isFlag(BufferFlag.END_OF_MEDIA));
        } finally {
            in.close();
        }
        return frames;
    }
}
