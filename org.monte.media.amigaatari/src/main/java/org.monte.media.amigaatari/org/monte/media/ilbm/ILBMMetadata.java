/*
 * @(#)ILBMMetadata.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.ilbm;

import org.w3c.dom.Node;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;

public class ILBMMetadata extends IIOMetadata {
    public static final String nativeMetadataFormatName =
            "javax_imageio_ilbm_1.0";
    private static final String CAMG_NODE = "CAMG";
    private int camg;

    public ILBMMetadata() {
        super(true,
                nativeMetadataFormatName,
                ILBMMetadata.class.getName(),
                null, null);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    public int getCamg() {
        return camg;
    }

    public void setCamg(int camg) {
        this.camg = camg;
    }

    @Override
    public Node getAsTree(String formatName) {
        if (formatName.equals(nativeMetadataFormatName)) {
            return getNativeTree();
        } else if (formatName.equals
                (IIOMetadataFormatImpl.standardMetadataFormatName)) {
            return getStandardTree();
        } else {
            throw new IllegalArgumentException("unsupported formatName=" + formatName);
        }
    }

    @Override
    public void mergeTree(String formatName, Node root) throws IIOInvalidTreeException {

    }

    @Override
    public void reset() {

    }

    private Node getNativeTree() {
        IIOMetadataNode root =
                new IIOMetadataNode(nativeMetadataFormatName);

        addChildNode(root, CAMG_NODE, camg);

        return root;
    }

    private IIOMetadataNode addChildNode(IIOMetadataNode root,
                                         String name,
                                         Object object) {
        IIOMetadataNode child = new IIOMetadataNode(name);
        if (object != null) {
            child.setUserObject(object);
            child.setNodeValue(org.monte.media.image.ImageUtil.convertObjectToString(object));
        }
        root.appendChild(child);
        return child;
    }
}
