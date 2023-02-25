/* @(#)StringTransferable.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.swing.datatransfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * StringTransferable.
 * <p>
 * Note: This transferable should always be used in conjunction with
 * PlainTextTransferable.
 * <p>
 * Usage:
 * <pre>
 * String text = "bla";
 * CompositeTransfer t = new CompositeTransferable();
 * t.add(new StringTransferable(text));
 * t.add(new PlainTextTransferable(text));
 * </pre>
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class StringTransferable extends AbstractTransferable {
    private String string;

    public StringTransferable(String string) {
        this(getDefaultFlavors(), string);
    }

    public StringTransferable(DataFlavor flavor, String string) {
        this(new DataFlavor[]{flavor}, string);
    }

    public StringTransferable(DataFlavor[] flavors, String string) {
        super(flavors);
        this.string = string;
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        return string;
    }

    protected static DataFlavor[] getDefaultFlavors() {
        try {
            return new DataFlavor[]{
                    new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=java.lang.String"),
                    DataFlavor.stringFlavor
            };
        } catch (ClassNotFoundException cle) {
            InternalError ie = new InternalError(
                    "error initializing StringTransferable");
            ie.initCause(cle);
            throw ie;
        }
    }
}
