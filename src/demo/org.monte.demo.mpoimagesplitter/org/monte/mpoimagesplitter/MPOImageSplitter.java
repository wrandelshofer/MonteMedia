/* @(#)MPOImageSplitter.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Under the MIT License.
 */
package org.monte.mpoimagesplitter;

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
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.monte.media.mpo.MPOFiles;
import org.monte.media.swing.BackgroundTask;

/**
 * MPOImageSplitter.
 *
 * @author Werner Randelshofer
 * @version $Id: MPOImageSplitter.java 364 2016-11-09 19:54:25Z werner $
 */
public class MPOImageSplitter extends javax.swing.JPanel {
    private final static long serialVersionUID = 1L;

    private class Handler implements DropTargetListener {

        /**
         * Called when a drag operation has
         * encountered the <code>DropTarget</code>.
         * <P>
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
         * The drag operation has departed
         * the <code>DropTarget</code> without dropping.
         * <P>
         * @param dte the <code>DropTargetEvent</code>
         */
        @Override
        public void dragExit(DropTargetEvent event) {
            // Nothing to do
        }

        /**
         * Called when a drag operation is ongoing
         * on the <code>DropTarget</code>.
         * <P>
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

        /**
         * The drag operation has terminated
         * with a drop on this <code>DropTarget</code>.
         * This method is responsible for undertaking
         * the transfer of the data associated with the
         * gesture. The <code>DropTargetDropEvent</code>
         * provides a means to obtain a <code>Transferable</code>
         * object that represents the data object(s) to
         * be transfered.<P>
         * From this method, the <code>DropTargetListener</code>
         * shall accept or reject the drop via the
         * acceptDrop(int dropAction) or rejectDrop() methods of the
         * <code>DropTargetDropEvent</code> parameter.
         * <P>
         * Subsequent to acceptDrop(), but not before,
         * <code>DropTargetDropEvent</code>'s getTransferable()
         * method may be invoked, and data transfer may be
         * performed via the returned <code>Transferable</code>'s
         * getTransferData() method.
         * <P>
         * At the completion of a drop, an implementation
         * of this method is required to signal the success/failure
         * of the drop by passing an appropriate
         * <code>boolean</code> to the <code>DropTargetDropEvent</code>'s
         * dropComplete(boolean success) method.
         * <P>
         * Note: The actual processing of the data transfer is not
         * required to finish before this method returns. It may be
         * deferred until later.
         * <P>
         * @param dtde the <code>DropTargetDropEvent</code>
         */
        @Override
        @SuppressWarnings("unchecked")
        public void drop(DropTargetDropEvent event) {
            if (event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                event.acceptDrop(DnDConstants.ACTION_COPY);

                try {
                    List<File> files = (List<File>) event.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

                    splitMPOFiles(files);

                } catch (IOException e) {
                    JOptionPane.showConfirmDialog(
                            MPOImageSplitter.this,
                            "Could not access the dropped data.",
                            "MPOImageSplitter: Drop Failed",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
                } catch (UnsupportedFlavorException e) {
                    JOptionPane.showConfirmDialog(
                            MPOImageSplitter.this,
                            "Unsupported data flavor.",
                            "MPOImageSplitter: Drop Failed",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                event.rejectDrop();
            }
        }

        /**
         * Called if the user has modified
         * the current drop gesture.
         * <P>
         * @param dtde the <code>DropTargetDragEvent</code>
         */
        @Override
        public void dropActionChanged(DropTargetDragEvent event) {
            // Nothing to do
        }
    }
    private Handler handler = new Handler();

    /** Creates new form MPOImageSplitter */
    public MPOImageSplitter() {
        initComponents();
        new DropTarget(this, handler);
        new DropTarget(label, handler);
    }

    public void splitMPOFiles(final List<File> files) {
        label.setEnabled(false);
        new BackgroundTask() {

            @Override
            protected void construct() throws Exception {
                for (File f : files) {
                    MPOFiles.splitMPOFile(f);
                }
            }

            @Override
            protected void finished() {
                label.setEnabled(true);
            }
        }.start();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        label = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label.setText("Drop MPO file here.");
        add(label, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                JFrame f = new JFrame("MPO Image Splitter");
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.add(new MPOImageSplitter());
                f.setSize(200, 200);
                f.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel label;
    // End of variables declaration//GEN-END:variables
}
