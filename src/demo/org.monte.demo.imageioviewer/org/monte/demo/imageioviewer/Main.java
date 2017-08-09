/* @(#)Main.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License. */
package org.monte.demo.imageioviewer;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.monte.media.image.AnimatedImageReader;
import org.monte.media.swing.Worker;

/**
 * Main.
 *
 * @author Werner Randelshofer
 * @version $Id: Main.java 364 2016-11-09 19:54:25Z werner $
 */
public class Main extends javax.swing.JPanel {

    private final static long serialVersionUID = 1L;

    private class Handler implements DropTargetListener {

        /**
         * Called when a drag operation has encountered the
         * <code>DropTarget</code>.
         * <P>
         *
         * @param dtde the <code>DropTargetDragEvent</code>
         */
        @Override
        public void dragEnter(DropTargetDragEvent event) {
            if (event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                event.acceptDrag(DnDConstants.ACTION_COPY);
            } else {
                event.rejectDrag();
            }
        }

        /**
         * The drag operation has departed the <code>DropTarget</code> without
         * dropping.
         * <P>
         *
         * @param dte the <code>DropTargetEvent</code>
         */
        @Override
        public void dragExit(DropTargetEvent event) {
            // Nothing to do
        }

        /**
         * Called when a drag operation is ongoing on the
         * <code>DropTarget</code>.
         * <P>
         *
         * @param dtde the <code>DropTargetDragEvent</code>
         */
        @Override
        public void dragOver(DropTargetDragEvent event) {
            if (event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                event.acceptDrag(DnDConstants.ACTION_COPY);
            } else {
                event.rejectDrag();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void drop(DropTargetDropEvent event) {
            if (event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                event.acceptDrop(DnDConstants.ACTION_COPY);

                try {
                    List<File> files = (List<File>) event.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

                    showImages(files);

                } catch (IOException e) {
                    JOptionPane.showConfirmDialog(Main.this,
                            "Could not access the dropped data.",
                            "ImageIOViewer: Drop Failed",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
                } catch (UnsupportedFlavorException e) {
                    JOptionPane.showConfirmDialog(Main.this,
                            "Unsupported data flavor.",
                            "ImageIOViewer: Drop Failed",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                event.rejectDrop();
            }
        }

        /**
         * Called if the user has modified the current drop gesture.
         * <P>
         *
         * @param dtde the <code>DropTargetDragEvent</code>
         */
        @Override
        public void dropActionChanged(DropTargetDragEvent event) {
            // Nothing to do
        }
    }
    private Handler handler = new Handler();

    /**
     * Creates new form ImageIOViewer
     */
    public Main() {
        initComponents();
        new DropTarget(this, handler);
        new DropTarget(label, handler);
    }

    protected Image readImage(File f) throws IOException {
        try (ImageInputStream in = new FileImageInputStream(f)) {
            for (Iterator<ImageReader> i = ImageIO.getImageReaders(in); i.hasNext();) {
                ImageReader r = i.next();
                    r.setInput(in);
                    if (r instanceof AnimatedImageReader) {
                        AnimatedImageReader rr = (AnimatedImageReader) r;
                        return rr.readAnimatedImage(0);
                    } else {
                        return r.read(0);
                    }
            }
        }
        return null;
    }

    public void showImages(final List<File> files) {
        label.setEnabled(false);
        if (label.getIcon() instanceof ImageIcon) {
            ImageIcon icon = (ImageIcon) label.getIcon();
            label.setIcon(null);
            label.setDisabledIcon(null);
            icon.getImage().flush();
        }
        new Worker<Image>() {
            @Override
            protected Image construct() throws Exception {
                for (File f : files) {
                    return readImage(f);
                }
                return null;
            }

            @Override
            protected void done(Image value) {
                if (value == null) {
                    failed(new IOException("Could not load image."));
                    return;
                }
                label.setText(null);
                ImageIcon icon = new ImageIcon(value);
                label.setIcon(icon);
                label.setDisabledIcon(icon);
                SwingUtilities.getWindowAncestor(Main.this).pack();
            }

            @Override
            protected void failed(Throwable error) {
                error.printStackTrace();
                label.setText("<html><b>Error</b><br>" + error.getMessage());
                SwingUtilities.getWindowAncestor(Main.this).pack();
            }

            @Override
            protected void finished() {
                label.setEnabled(true);
            }
        }.start();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        label = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label.setText("Drop image file here.");
        add(label, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame f = new JFrame("MonteMedia ImageIO Viewer");
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.add(new Main());
                f.setSize(200, 200);
                f.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel label;
    // End of variables declaration//GEN-END:variables
}
