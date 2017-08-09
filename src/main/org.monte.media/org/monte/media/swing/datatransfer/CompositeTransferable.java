/* @(#)CompositeTransferable.java
 * Copyright © 2001 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

package org.monte.media.swing.datatransfer;

import java.awt.datatransfer.*;
import java.io.*;
import java.util.*;
/**
 *
 *
 * @author  Werner Randelshofer
 * @version $Id: CompositeTransferable.java 364 2016-11-09 19:54:25Z werner $
 */
public class CompositeTransferable implements java.awt.datatransfer.Transferable {
    private HashMap<DataFlavor,Transferable> transferables = new HashMap<DataFlavor,Transferable>();
    private LinkedList<DataFlavor> flavors = new LinkedList<DataFlavor>();
    
    /** Creates a new instance of CompositeTransferable */
    public CompositeTransferable() {
    }
    
    public void add(Transferable t) {
        DataFlavor[] f = t.getTransferDataFlavors();
        for (int i=0; i < f.length; i++) {
            if (! transferables.containsKey(f[i])) {
                flavors.add(f[i]);
            }
            transferables.put(f[i], t);
            
        }
    }
    
    /**
     * Returns an object which represents the data to be transferred.  The class
     * of the object returned is defined by the representation class of the flavor.
     *
     * @param flavor the requested flavor for the data
     * @see DataFlavor#getRepresentationClass
     * @exception IOException                if the data is no longer available
     *             in the requested flavor.
     * @exception UnsupportedFlavorException if the requested data flavor is
     *             not supported.
     */
    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        Transferable t = transferables.get(flavor);
        if (t == null) {
            throw new UnsupportedFlavorException(flavor);
        }
        return t.getTransferData(flavor);
    }
    
    /**
     * Returns an array of DataFlavor objects indicating the flavors the data
     * can be provided in.  The array should be ordered according to preference
     * for providing the data (from most richly descriptive to least descriptive).
     * @return an array of data flavors in which this data can be transferred
     */
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return flavors.toArray(new DataFlavor[transferables.size()]);
    }
    
    /**
     * Returns whether or not the specified data flavor is supported for
     * this object.
     * @param flavor the requested flavor for the data
     * @return boolean indicating wjether or not the data flavor is supported
     */
    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return transferables.containsKey(flavor);
    }
}
