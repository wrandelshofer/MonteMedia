/* @(#)DefaultIIOMetadata.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.exif;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import org.w3c.dom.Node;

/**
 * DefaultIIOMetadata.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class DefaultIIOMetadata extends IIOMetadata {
    private final IIOMetadataNode root;
    private final String formatName;
    public DefaultIIOMetadata(String formatName, IIOMetadataNode root) {
        super(true,// standardMetadataFormatSupported
                          formatName,
                          "javax.imageio.metadata.IIOMetadataNode",
                          null,
                          null);
        this.formatName=formatName;
        this.root=root;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public Node getAsTree(String formatName) {
        return root;
    }

    @Override
    public void mergeTree(String formatName, Node root) throws IIOInvalidTreeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
