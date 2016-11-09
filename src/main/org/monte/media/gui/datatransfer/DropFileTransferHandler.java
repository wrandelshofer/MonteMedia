/* @(#)DropFileTransferHandler.java
 * Copyright © 2011 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.gui.datatransfer;

import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.im.InputContext;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;

/**
 * The DropFileTransferHandler can be used to add drag and drop
 * support. When a file is dropped, the supplied ActionListner is invoked.
 * The filename is passed in the action command.
 *
 * @author Werner Randelshofer
 * @version 1.2 2010-10-03 Adds support for file filter.
 * <br>1.1 2008-12-03 Added file selection mode.
 * <br>1.0 September 8, 2007 Created.
 */
public class DropFileTransferHandler extends TransferHandler {
    private final static long serialVersionUID = 1L;

    private boolean shouldRemove;
    private int p0;
    private int p1;
    private int fileSelectionMode;
    private FileFilter fileFilter;
    private ActionListener actionListener;

    /** Creates a new instance. */
    public DropFileTransferHandler() {
        this(JFileChooser.FILES_ONLY);
    }

    /** Creates a new instance.
     * @param fileSelectionMode JFileChooser file selection mode.
     */
    public DropFileTransferHandler(int fileSelectionMode) {
        this(fileSelectionMode, null);
    }

    /** Creates a new instance.
     * @param fileSelectionMode JFileChooser file selection mode.
     */
    public DropFileTransferHandler(int fileSelectionMode, FileFilter filter) {
        this(fileSelectionMode,filter,null);
    }
    /** Creates a new instance.
     * @param fileSelectionMode JFileChooser file selection mode.
     */
    public DropFileTransferHandler(int fileSelectionMode, FileFilter filter,ActionListener l) {
        this.fileFilter = filter;
        if (fileSelectionMode != JFileChooser.FILES_AND_DIRECTORIES
                && fileSelectionMode != JFileChooser.FILES_ONLY
                && fileSelectionMode != JFileChooser.DIRECTORIES_ONLY) {
            throw new IllegalArgumentException("illegal file selection mode:" + fileSelectionMode);
        }
        this.fileSelectionMode = fileSelectionMode;
        setActionListener(l);
    }
    
    public void setActionListener(ActionListener l) {
        this.actionListener=l;
    }

    @Override
    public boolean importData(JComponent c, Transferable t) {

        boolean imported = false;
        if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            InputContext ic = c.getInputContext();
            if (ic != null) {
                ic.endComposition();
            }

            try {
                @SuppressWarnings("unchecked")
                List<File> list = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                if (!list.isEmpty()) {
                    File file = list.get(0);

                    switch (fileSelectionMode) {
                        case JFileChooser.FILES_AND_DIRECTORIES:
                            break;
                        case JFileChooser.FILES_ONLY:
                            if (file.isDirectory()) {
                                return false;
                            }
                            break;
                        case JFileChooser.DIRECTORIES_ONLY:
                            if (!file.isDirectory()) {
                                return false;
                            }
                            break;
                    }
                    if (fileFilter != null && !fileFilter.accept(file)) {
                        return false;
                    }
                    actionListener.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,file.getPath()));
                }
                imported = true;
            } catch (UnsupportedFlavorException ex) {
                //   ex.printStackTrace();
            } catch (IOException ex) {
                //   ex.printStackTrace();
            }
        }

        return imported;
    }

    @Override
    protected Transferable createTransferable(JComponent comp) {
        return null;
    }

    @Override
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        if (!(comp.isEnabled())) {
            return false;
        }

        for (DataFlavor flavor : transferFlavors) {
            if (flavor.isFlavorJavaFileListType()
                    || flavor.isFlavorTextType()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Try to find a flavor that can be used to import a Transferable.
     * The set of usable flavors are tried in the following order:
     * <ol>
     *     <li>First, an attempt to find a text/plain flavor is made.
     *     <li>Second, an attempt to find a flavor representing a String reference
     *         in the same VM is made.
     *     <li>Lastly, DataFlavor.stringFlavor is searched for.
     * </ol>
     */
    protected DataFlavor getImportFlavor(DataFlavor[] flavors, JComponent c) {
        DataFlavor plainFlavor = null;
        DataFlavor refFlavor = null;
        DataFlavor stringFlavor = null;

        for (int i = 0; i < flavors.length; i++) {
            String mime = flavors[i].getMimeType();
            if (mime.startsWith("text/plain")) {
                return flavors[i];
            } else if (refFlavor == null && mime.startsWith("application/x-java-jvm-local-objectref") && flavors[i].getRepresentationClass() == java.lang.String.class) {
                refFlavor = flavors[i];
            } else if (stringFlavor == null && flavors[i].equals(DataFlavor.stringFlavor)) {
                stringFlavor = flavors[i];
            }
        }
        if (refFlavor != null) {
            return refFlavor;
        } else if (stringFlavor != null) {
            return stringFlavor;
        }
        return null;
    }

    // --- TransferHandler methods ------------------------------------
    /**
     * This is the type of transfer actions supported by the source.  Some models are
     * not mutable, so a transfer operation of COPY only should
     * be advertised in that case.
     *
     * @param comp  The component holding the data to be transfered.  This
     *  argument is provided to enable sharing of TransferHandlers by
     *  multiple components.
     * @return  This is implemented to return NONE if the component is a JPasswordField
     *  since exporting data via user gestures is not allowed.  If the text component is
     *  editable, COPY_OR_MOVE is returned, otherwise just COPY is allowed.
     */
    @Override
    public int getSourceActions(JComponent comp) {
            return NONE;
        
    }

    /**
     * This method is called after data has been exported.  This method should remove
     * the data that was transfered if the action was MOVE.
     *
     * @param comp The component that was the source of the data.
     * @param data   The data that was transferred or possibly null
     *               if the action is <code>NONE</code>.
     * @param action The actual action that was performed.
     */
    @Override
    protected void exportDone(JComponent comp, Transferable data, int action) {
        //
    }

    /**
     * @return the fileFilter
     */
    public FileFilter getFileFilter() {
        return fileFilter;
    }

    /**
     * @param fileFilter the fileFilter to set
     */
    public void setFileFilter(FileFilter fileFilter) {
        this.fileFilter = fileFilter;
    }
}

