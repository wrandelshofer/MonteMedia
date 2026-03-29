/*
 * @(#)ICC_ProfileReader.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.icc;

import org.monte.media.color.enumerator.PreorderSpliterator;
import org.monte.media.io.ByteArrayImageInputStream;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import java.awt.color.ICC_Profile;
import java.awt.color.ICC_ProfileGray;
import java.awt.color.ICC_ProfileRGB;
import java.awt.color.ProfileDataException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/// Reads an ICC_Profile from IIOMetadata
public class ICC_ProfileReader {
    private final static String[] RGB_NAMES = {"red  ", "green", "blue "};
    private final ICC_Profile icp;

    public ICC_ProfileReader(IIOMetadata meta) {
        icp = read(meta);
    }

    public ICC_ProfileReader(byte[] data, boolean isDeflated) {
        icp = read(data, isDeflated);
    }

    public ICC_ProfileReader(ICC_Profile icp) {
        this.icp = icp;
    }

    private static String getStringData(ICC_Profile icp, int tag) {
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

    /// ```
    /// typedef struct {
    ///   header profileHeader;                // 128 bytes
    ///   tagTable tagTable;                   // variable size
    ///   taggedElementData taggedElementData; // variable size
    /// } iccProfile;
    ///
    /// typedef struct {
    ///    uint32 profileSize;
    ///    fourCC cmmType;
    ///    uint32 profileVersion;
    ///    fourCC deviceClass;
    ///    fourCC colorSpaceOfData;
    ///    fourCC profileConnectionSpace;
    ///    dateTimeNumber creationDateTime;
    ///    fourCC profileFileSignature;
    ///    fourCC primaryPlatformTarget;
    ///    uint32 cmmFlags;
    ///    fourCC deviceManufacturer;
    ///    uint32 deviceModel;
    ///    uint32 deviceAttributes;
    ///    uint32 renderingIntent;
    ///    xyzNumber illuminant;// must be illuminant D50 [0.9642, 1.0, 0.8249]
    ///    fourCC creator;
    ///    byte[44] reserved;
    /// } header
    ///
    /// typedef struct {
    ///    uint32 tagCount;
    ///    tagElement[tagCount] tagElement;
    /// } tagTable
    ///
    /// typedef struct {
    ///    uint32 signature;
    ///    uint32 pointer;
    ///    uint32 size;
    /// } tagElement
    ///
    /// typedef struct {
    ///    byte[] bytes;
    /// } taggedElementData
    /// ```
    private static int[] getTagSignatures(ICC_Profile icp) {
        var data = icp.getData();

        try (ICC_ProfileInputStream in = new ICC_ProfileInputStream(new ByteArrayImageInputStream(data))) {
            // read the header
            long profileSize = in.readUnsignedInt();// must be data.length
            String cmmType = in.readFourCC();
            long profileVersion = in.readUnsignedInt();
            String deviceClass = in.readFourCC();
            String colorSpaceOfData = in.readFourCC();
            String connectionSpace = in.readFourCC();
            OffsetDateTime creationDateTime = in.readDateTimeNumber();
            String profileFileSignature = in.readFourCC();
            String primaryPlatformTarget = in.readFourCC();
            int profileFlags = in.readInt32();
            String deviceManufacturer = in.readFourCC();
            long deviceModel = in.readUInt32();
            long deviceAttributes = in.readInt64();
            long renderingIntent = in.readUInt32();
            float[] illuminantD50 = in.readXYZNumber();
            String profileCreator = in.readFourCC();
            in.skipBytes(44);

            // read the tag table
            long tagCount = in.readUInt32();
            int[] signatures = new int[(int) tagCount];
            for (int i = 0; i < tagCount; i++) {
                signatures[i] = in.readInt32();
                long pointer = in.readUInt32();
                long size = in.readUInt32();
            }
            return signatures;

        } catch (IOException e) {
            // data is corrupt
            return new int[0];
        }
    }

    public static ICC_Profile read(IIOMetadata iioMeta) {
        Node node = iioMeta.getAsTree(iioMeta.getNativeMetadataFormatName());
        var it = new PreorderSpliterator<>((Node n) -> () -> new Iterator<Node>() {
            final NodeList childNodes = n.getChildNodes();
            int index = 0;

            @Override
            public boolean hasNext() {
                return index < childNodes.getLength();
            }

            @Override
            public Node next() {
                return childNodes.item(index++);
            }

        }, node);
        while (it.moveNext()) {
            var x = it.current();
            if (x instanceof IIOMetadataNode iioNode && iioNode.getUserObject() != null) {
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

    /// ```
    /// typedef struct {
    ///   fourCC typeDescriptor; // "sf32"
    ///   uint32 reserved; // must be 0
    ///   s15Fixed16Number[*] value;
    ///
    /// ```
    /// } floatingPointArrayTag;
    ///
    private static float[] readFloatingPointArrayTag(ICC_ProfileInputStream in) {
        try {
            String typeDescriptor = in.readFourCC();//  typeDescriptor: "sf32"
            long reserved = in.readUInt32();
            var values = new float[(int) (in.length() - 8) / 4];
            for (int i = 0; i < values.length; i++) {
                values[i] = in.readS15Fixed16Number();
            }
            return values;

        } catch (IOException e) {
            return new float[0];
        }
    }

    /// ```
    /// typedef struct {
    ///   fourCC typeDescriptor; // "para"
    ///   uint32 reserved; // must be 0
    ///   uint16 parameterType;
    ///                  // when parameterType is 3:
    ///                  //     f(x) = { (a*x+b)^gamma : if x>= d
    ///                  //            { (c*x)         : if x<d
    ///   s15fixed16 gamma;
    ///   s15fixed16 a;
    ///   s15fixed16 b;
    ///   s15fixed16 c;
    ///   s15fixed16 d;
    ///
    /// ```
    /// } toneReproductionCurveTag;
    ///
    private static Object readParametricToneReproductionCurveTag(ICC_ProfileInputStream in) {
        try {
            String typeDescriptor = in.readFourCC();//  typeDescriptor: "para"
            long reserved = in.readUInt32();
            int parameterType = in.readUInt16();
            if (parameterType == 3) {
                in.skipBytes(2);// reserved
                float gamma = in.readS15Fixed16Number();
                float a = in.readS15Fixed16Number();
                float b = in.readS15Fixed16Number();
                float c = in.readS15Fixed16Number();
                float d = in.readS15Fixed16Number();
                return new GammaTRC(gamma, a, b, c, d);
            }

        } catch (IOException e) {
            //bail
            return null;
        }
        return null;//unknown parameter type
    }

    /// ```
    /// typedef struct {
    ///   fourCC typeDescriptor; // "curv"
    ///   uint32 reserved; // must be 0
    ///
    ///
    /// ```
    /// } toneReproductionCurveTag;
    ///
    private static Object readPiecewiseToneReproductionCurveTag(ICC_ProfileInputStream in) {
        try {
            String typeDescriptor = in.readFourCC();//  typeDescriptor: "curv"
            long reserved = in.readUInt32();
            long count = in.readUInt32();
            char[] points = new char[(int) count];
            for (int i = 0; i < count; i++) {
                points[i] = (char) in.readUInt16();
            }
            return new PiecewiseTRC(points);
        } catch (IOException e) {
            return e;
        }
    }

    private static Object readTag(byte[] data) {
        try (var in = new ICC_ProfileInputStream(new ByteArrayImageInputStream(data))) {
            String type = in.readFourCC();
            in.seek(0);
            return switch (type) {
                case "desc" -> readTextDescriptionTag(in);
                case "mluc" -> readTextDescriptionTag(in);
                case "para" -> readParametricToneReproductionCurveTag(in);
                case "curv" -> readPiecewiseToneReproductionCurveTag(in);
                case "sf32" -> readFloatingPointArrayTag(in);
                case "XYZ " -> readXYZTag(in);
                case "chrm" -> readFloatingPointArrayTag(in);
                default -> null;
            };
        } catch (IOException e) {
            return null;
        }
    }

    /// ```
    /// typedef struct {
    ///   fourCC typeDescriptor; // "mluc"
    ///   uint32 reserved; // must be 0
    ///
    ///   uint32 asciiLength; // including terminating 0
    ///   byte[asciiLength] asciiDescription; // 7-bit ascii, null-terminated
    ///   byte[3] padding;  // why? this is not in the specification?
    ///   fourCC unicodeLanguageCode;
    ///   uint32 unicodeLength;
    ///   byte[4] padding;  // why? this is not in the specification?
    ///   uint16[unicodeLength] unicodeDescription; // 16-bit unicode characters
    ///   uint16 scriptCodeCode;
    ///   uint8 macintoshLength;
    ///   byte[macintoshLength] macintoshDescription;
    ///
    /// ```
    /// } textDescriptionTag;
    ///
    private static String readTextDescriptionTag(ICC_ProfileInputStream in) {
        try {
            String typeDescriptor = in.readFourCC();
            in.readUInt32();
            byte[] byteBuf = new byte[(int) in.length()];
            long asciiLength = in.readUInt32();
            in.readFully(byteBuf, 0, (int) asciiLength);
            String asciiDescription = new String(byteBuf, 0, (int) asciiLength - 1, StandardCharsets.US_ASCII);
            in.skipBytes(3);
            String languageCode = in.readFourCC();
            long unicodeLength = in.readUInt32();
            int scriptCodeCode = in.readUInt16();
            int macintoshLength = in.readUInt8();
            in.skipBytes(1);
            in.readFully(byteBuf, 0, (int) unicodeLength);
            String unicodeDescription = new String(byteBuf, 0, (int) unicodeLength, StandardCharsets.UTF_16);
            in.readFully(byteBuf, 0, (int) macintoshLength);
            String macintoshDescription = new String(byteBuf, 0, (int) macintoshLength, StandardCharsets.ISO_8859_1);
            return unicodeDescription.isEmpty() ? asciiDescription : unicodeDescription;
        } catch (IOException e) {
            return "";
        }
    }

    /// ```
    /// typedef struct {
    ///   fourCC typeDescriptor; // "XYZ "
    ///   uint32 reserved; // must be 0
    ///   s15Fixed16Number[3] XYZ;
    ///
    /// ```
    /// } toneReproductionCurveTag;
    ///
    private static float[] readXYZTag(ICC_ProfileInputStream in) {
        try {
            String typeDescriptor = in.readFourCC();//  typeDescriptor: "XYZ "
            long reserved = in.readUInt32();
            return new float[]{
                    in.readS15Fixed16Number(),
                    in.readS15Fixed16Number(),
                    in.readS15Fixed16Number()
            };

        } catch (IOException e) {
            return new float[0];
        }
    }

    public static String toString(ICC_Profile icp) {
        var buf = new StringBuffer();
        buf.append("ICC_Profile {").append('\n');
        if (icp == null) {
            buf.append("  null").append('\n');
            return buf.toString();
        }

        if (icp instanceof ICC_ProfileGray p) {
            buf.append("  white XYZ: ").append(Arrays.toString(p.getMediaWhitePoint())).append('\n');
            //buf.append("  white xy : ").append(Arrays.toString(toXY(p.getMediaWhitePoint()))).append('\n');
            try {
                float gamma = p.getGamma();
                buf.append("  gamma: ").append(gamma).append('\n');
            } catch (ProfileDataException e) {
                // trc is not a gamma
            }
            try {
                short[] trc = p.getTRC();
                buf.append("  tone reproduction curve: ").append(Arrays.toString(trc)).append('\n');
            } catch (ProfileDataException | ArrayIndexOutOfBoundsException e) {
                // trc is not a trc
            }
        } else if (icp instanceof ICC_ProfileRGB p) {
            buf.append("  white XYZ: ").append(Arrays.toString(p.getMediaWhitePoint())).append('\n');
            //buf.append("  white xy : ").append(Arrays.toString(toXY(p.getMediaWhitePoint()))).append('\n');
            var matrix = p.getMatrix();
            for (int i = 0; i < matrix.length; i++) {
                buf.append("  ").append(RGB_NAMES[i]).append(" XYZ: ").append(matrix[0][i]).append(", ").append(matrix[1][i]).append(", ").append(matrix[2][i]).append(", ").append('\n');
                //buf.append("  ").append(RGB_NAMES[i]).append(" xy : ").append(Arrays.toString(toXY(matrix[0][i], matrix[1][i], matrix[2][i]))).append(", ").append('\n');
            }
            for (int i = 0; i < 3; i++) {
                try {
                    float gamma = p.getGamma(i);
                    buf.append("  ").append(RGB_NAMES[i]).append(" gamma: ").append(gamma).append('\n');
                } catch (ProfileDataException e) {
                    // trc is not a gamma
                }
                try {
                    short[] trc = p.getTRC(i);
                    buf.append("  ").append(RGB_NAMES[i]).append(" tone reproduction curve: ").append(Arrays.toString(trc)).append('\n');
                } catch (ProfileDataException | ArrayIndexOutOfBoundsException e) {
                    // trc is not a trc
                }
            }
        } else {
            buf.append(icp.toString()).append('\n');
        }

        for (int sig : getTagSignatures(icp)) {
            buf.append("  ").append(ICC_ProfileInputStream.toFourCC(sig))
                    .append(": ");
            byte[] data = icp.getData(sig);
            Object obj = readTag(data);
            //noinspection SwitchStatementWithTooFewBranches
            switch (obj) {
                case null -> buf.append(new String(data, StandardCharsets.US_ASCII));
                case float[] floats -> buf.append(Arrays.toString(floats));
                default -> buf.append(obj);
            }
            buf.append('\n');
        }
        buf.append('}');

        return buf.toString();
    }

    public static float[] toXY(float... XYZ) {
        float sum = XYZ[0] + XYZ[1] + XYZ[2];
        if (sum == 0) sum = 1;
        return new float[]{XYZ[0] / sum, XYZ[1] / sum};
    }

    public ICC_Profile getProfile() {
        return icp;
    }

    public String toString() {
        return toString(icp);
    }

    private record GammaTRC(float gamma, float a, float b, float c, float d) {
    }

    private record PiecewiseTRC(char[] points) {
        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            b.append("PiecewiseTRC[points=");
            for (int i = 0; i < points.length; i++) {
                if (i != 0) b.append(", ");
                b.append((int) points[i]);
            }
            b.append(']');
            return b.toString();
        }
    }
}
