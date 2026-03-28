/*
 * @(#)ICC_ProfileReader.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.icc;

import org.monte.media.color.enumerator.PreorderSpliterator;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import java.awt.color.ICC_Profile;
import java.awt.color.ICC_ProfileGray;
import java.awt.color.ICC_ProfileRGB;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class ICC_ProfileReader {
    private final ICC_Profile icp;

    public ICC_ProfileReader(IIOMetadata meta) {
        icp = read(meta);
    }

    public ICC_Profile getProfile() {
        return icp;
    }

    public ICC_ProfileReader(byte[] data, boolean isDeflated) {
        icp = read(data, isDeflated);
    }

    public static ICC_Profile read(IIOMetadata iioMeta) {
        org.w3c.dom.Node node = iioMeta.getAsTree(iioMeta.getNativeMetadataFormatName());
        var it = new PreorderSpliterator<>((org.w3c.dom.Node n) -> () -> new Iterator<Node>() {
            final NodeList childNodes = n.getChildNodes();
            int index = 0;

            @Override
            public boolean hasNext() {
                return index < childNodes.getLength();
            }

            @Override
            public org.w3c.dom.Node next() {
                return childNodes.item(index++);
            }

        }, node);
        while (it.moveNext()) {
            var x = it.current();
            if (x instanceof IIOMetadataNode iioNode && iioNode.getUserObject() != null) {
                IO.println("iioMeta: " + x.getLocalName());
                if ("iCCP".equals(x.getLocalName()) && iioNode.getUserObject() instanceof byte[] data) {
                    boolean isDeflate = "deflate".equals(iioNode.getAttribute("compressionMethod"));
                    return read(data, isDeflate);
                } else if ("unknown".equals(x.getLocalName()) && iioNode.getUserObject() instanceof byte[] data) {
                    if (data.length > 12 && "ICC_PROFILE\0".equals(new String(data, 0, 12, StandardCharsets.US_ASCII))) {
                        IO.println("unknown is an ICC PROFILE");
                        byte[] dataCropped = Arrays.copyOfRange(data, 14, data.length);
                        return read(dataCropped, false);

                    }
                }
            }
        }
        return null;
    }

    public static ICC_Profile read(byte[] data, boolean isDeflated) {
        ICC_Profile p = null;
        if (isDeflated) {
            try {

                Inflater inflater = new Inflater();
                inflater.setInput(data);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];

                while (!inflater.finished()) {
                    int decompressedSize = 0;
                    decompressedSize = inflater.inflate(buffer);
                    outputStream.write(buffer, 0, decompressedSize);
                }
                data = outputStream.toByteArray();
            } catch (DataFormatException e) {
            }
        }
        p = ICC_Profile.getInstance(data);
        return p;
    }

    public ICC_ProfileReader(ICC_Profile icp) {
        this.icp = icp;
    }

    public String toString() {
        var buf = new StringBuffer();
        if (icp != null) {
            buf.append("Profile Description: ").append(getStringData(ICC_Profile.icSigProfileDescriptionTag)).append('\n');
        }
        if (icp instanceof ICC_ProfileGray p) {
            buf.append("White XYZ: ").append(Arrays.toString(p.getMediaWhitePoint())).append('\n');
            buf.append("White xy : ").append(Arrays.toString(toXY(p.getMediaWhitePoint()))).append('\n');
        } else if (icp instanceof ICC_ProfileRGB p) {
            buf.append("White XYZ: ").append(Arrays.toString(p.getMediaWhitePoint())).append('\n');
            buf.append("White xy : ").append(Arrays.toString(toXY(p.getMediaWhitePoint()))).append('\n');
            var matrix = p.getMatrix();
            buf.append("Red   XYZ: ").append(matrix[0][0]).append(", ").append(matrix[1][0]).append(", ").append(matrix[2][0]).append(", ").append('\n');
            buf.append("Red   xy : ").append(Arrays.toString(toXY(matrix[0][0], matrix[1][0], matrix[2][0]))).append(", ").append('\n');
            buf.append("Green XYZ: ").append(matrix[0][1]).append(", ").append(matrix[1][1]).append(", ").append(matrix[2][1]).append(", ").append('\n');
            buf.append("Green xy : ").append(Arrays.toString(toXY(matrix[0][1], matrix[1][1], matrix[2][1]))).append(", ").append('\n');
            buf.append("Blue  XYZ: ").append(matrix[0][2]).append(", ").append(matrix[1][2]).append(", ").append(matrix[2][1]).append(", ").append('\n');
            buf.append("Blue  xy : ").append(Arrays.toString(toXY(matrix[0][2], matrix[1][2], matrix[2][2]))).append(", ").append('\n');
        } else if (icp != null) {
            buf.append("ICC_Profile: ").append(icp.toString()).append('\n');
        } else {
            buf.append("null");
        }
        return buf.toString();
    }

    private float[] toXY(float... XYZ) {
        float sum = XYZ[0] + XYZ[1] + XYZ[2];
        if (sum == 0) sum = 1;
        return new float[]{XYZ[0] / sum, XYZ[1] / sum};
    }

    private String getStringData(int tag) {
        byte[] data = icp.getData(tag);
        if (data == null) {
            return "null";
        }
        int p = 0;
        while (p < data.length - 12 && data[p + 12] != 0) {
            p++;
        }
        return new String(data, 12, p, StandardCharsets.US_ASCII);
    }
}
