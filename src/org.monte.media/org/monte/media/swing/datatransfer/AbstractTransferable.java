/* @(#)AbstractTransferable.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.swing.datatransfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

/**
 * Base class for transferable objects.
 *
 * @author Werner Randelshofer
 * @version 1.0 22. August 2007 Created.
 */
public abstract class AbstractTransferable implements Transferable {
    private DataFlavor[] flavors;
    
    /** Creates a new instance. */
    public AbstractTransferable(DataFlavor[] flavors) {
        this.flavors = flavors;
    }

    public DataFlavor[] getTransferDataFlavors() {
        return flavors.clone();
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        for (DataFlavor f : flavors) {
            if (f.equals(flavor)) {
                return true;
            }
        }
        return false;
    }
}
