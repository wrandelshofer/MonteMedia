/*
 * @(#)ICC_ProfileWriter.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.icc;

import org.monte.media.color.tonecurve.GammaToneCurve;
import org.monte.media.color.tonecurve.ToneCurve;
import org.monte.media.io.ByteArrayImageOutputStream;

import java.awt.color.ICC_Profile;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class ICC_ProfileBuilder {
    private ToneCurve redToneCurve;
    private ToneCurve greenToneCurve;
    private ToneCurve blueToneCurve;
    private float[] red;
    private float[] green;
    private float[] blue;
    private float[] white;
    private float[] black = new float[3];
    private static final int TOC_RECORD_SIZE = 12;
    private static final int HEADER_SIZE = 128;

    public void setRedXYZ(float... red) {
        this.red = red.clone();
    }

    public void setBlackXYZ(float... black) {
        this.black = black.clone();
    }

    public void setWhiteXYZ(float... white) {
        this.white = white.clone();
    }

    public void setGreenXYZ(float... green) {
        this.green = green.clone();
    }

    public void setBlueXYZ(float... blue) {
        this.blue = blue.clone();
    }

    public void setToneCurve(ToneCurve curve) {
        this.redToneCurve = curve;
        this.greenToneCurve = curve;
        this.blueToneCurve = curve;
    }

    public void setRedToneCurve(ToneCurve curve) {
        this.redToneCurve = curve;
    }

    public void setGreenToneCurve(ToneCurve curve) {
        this.greenToneCurve = curve;
    }

    public void setBlueToneCurve(ToneCurve curve) {
        this.blueToneCurve = curve;
    }

    /// ```
    /// typedef struct {
    ///   header profileHeader;                // 128 bytes
    ///   tagTable tagTable;                   // variable size
    ///   taggedElementData taggedElementData; // variable size
    /// } iccProfile;
    public ICC_Profile build() {
        ByteArrayImageOutputStream out = new ByteArrayImageOutputStream();
        try {
            var icc_out = new ICC_ProfileOutputStream(out);
            writeHeader(icc_out);
            ByteArrayImageOutputStream tagData = new ByteArrayImageOutputStream();
            var icc_tagData = new ICC_ProfileOutputStream(tagData);
            writeTags(icc_out, icc_tagData);
            icc_tagData.close();
            icc_out.write(tagData.toByteArray());
            icc_out.seek(0);
            icc_out.writeUInt32(icc_out.length());
            icc_out.seek(icc_out.length());
            return ICC_Profile.getInstance(out.getBuffer());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /// ```
    /// typedef struct {
    ///    uint32 tagCount;
    ///    tagElement[tagCount] tagElement;
    /// } tagTable
    /// ```
    private void writeTags(ICC_ProfileOutputStream tagTable, ICC_ProfileOutputStream tagData) throws IOException {
        var pos = tagTable.getStreamPosition();
        tagTable.writeUInt32(0);
        writeXYZ(tagTable, tagData, ICC_Profile.icSigMediaBlackPointTag, black);
        writeXYZ(tagTable, tagData, ICC_Profile.icSigMediaWhitePointTag, white);
        writeXYZ(tagTable, tagData, ICC_Profile.icSigRedColorantTag, red);
        writeXYZ(tagTable, tagData, ICC_Profile.icSigGreenColorantTag, green);
        writeXYZ(tagTable, tagData, ICC_Profile.icSigBlueColorantTag, blue);
        writeTRC(tagTable, tagData, ICC_Profile.icSigRedTRCTag, redToneCurve);
        writeTRC(tagTable, tagData, ICC_Profile.icSigGreenTRCTag, greenToneCurve);
        writeTRC(tagTable, tagData, ICC_Profile.icSigBlueTRCTag, blueToneCurve);
        tagTable.seek(pos);
        long numEntries = (tagTable.length() - pos) / TOC_RECORD_SIZE;
        tagTable.writeUInt32(numEntries);
        for (int index = 0; index < numEntries; index++) {
            int pointerOffset = 8 + HEADER_SIZE + index * TOC_RECORD_SIZE;
            tagTable.seek(pointerOffset);
            int pointer = tagTable.readInt();
            if (pointer % 4 != 0) {
                throw new RuntimeException("Pointer " + index + " does not start at a 4-byte boundary: pointer=" + pointer);
            }
            tagTable.seek(pointerOffset);
            tagTable.writeUInt32(pointer + HEADER_SIZE + numEntries * TOC_RECORD_SIZE + 4);
        }
        tagTable.seek(tagTable.length());
    }

    private void writeTag(ICC_ProfileOutputStream tagTable, ICC_ProfileOutputStream tagData, int signature) throws IOException {

    }

    /// ```
    /// typedef struct {
    ///   fourCC typeDescriptor; // "XYZ "
    ///   uint32 reserved; // must be 0
    ///   s15Fixed16Number[3] XYZ;
    ///
    /// ```
    /// } XYZTag;
    ///
    private void writeXYZ(ICC_ProfileOutputStream tagTable, ICC_ProfileOutputStream tagData, int signature, float[] xyz) throws IOException {
        long pointer = tagData.getStreamPosition();
        tagData.writeFourCC("XYZ ");
        tagData.writeUInt32(0);
        for (float v : xyz) {
            tagData.writeS15Fixed16Number(v);
        }
        long size = tagData.getStreamPosition() - pointer;
        writeTagElement(tagTable, tagData, signature, pointer, size);
    }

    private void writeTRC(ICC_ProfileOutputStream tagTable, ICC_ProfileOutputStream tagData, int signature, ToneCurve tc) throws IOException {
        switch (tc) {
            // case GammaToneCurve g when g.a() == 1 && g.b() == 0 && g.c() == 1 && g.d() == 0 ->
            //         writePiecewiseTRC(tagTable, tagData, signature, new float[]{g.gamma()});
            case GammaToneCurve g ->
                    writeParametricTRC(tagTable, tagData, signature, g.gamma(), 1 / g.a(), g.b() / g.a(), 1 / g.c(), g.d());
            default -> {
                throw new IllegalStateException("Unexpected value: " + tc);
            }
        }
    }

    /// ```
    /// typedef struct {
    ///   fourCC typeDescriptor; // "curv"
    ///   uint32 reserved; // must be 0
    ///   uint32 count;
    ///   uint16[count] values;
    /// ```
    /// } toneReproductionCurveTag;
    ///
    /// If count is 1, then the value is an u8fixed8 number
    /// that defines the gamma value of a gamma curve.
    /// ```
    /// f(x) = x^gamma
    /// ```
    ///
    private void writePiecewiseTRC(ICC_ProfileOutputStream tagTable, ICC_ProfileOutputStream tagData, int signature, float[] values) throws IOException {
        long pointer = tagData.getStreamPosition();
        tagData.writeFourCC("curv");
        tagData.writeUInt32(0);
        tagData.writeUInt32(values.length);
        for (float v : values) {
            tagData.writeU8Fixed8Number(v);
        }
        long size = tagData.getStreamPosition() - pointer;
        writeTagElement(tagTable, tagData, signature, pointer, size);
    }

    /// ```
    /// typedef struct {
    ///   fourCC typeDescriptor; // "para"
    ///   uint32 reserved; // must be 0
    ///   uint16 parameterType;
    ///                  // when parameterType is 3:
    ///                  //     f(x) = { (a*x+b)^gamma : if x>= d
    ///                  //            { (c*x)         : if x<d
    ///   uint16 reserved; // must be 0
    ///   s15fixed16 gamma;
    ///   s15fixed16 a;
    ///   s15fixed16 b;
    ///   s15fixed16 c;
    ///   s15fixed16 d;
    ///
    /// ```
    /// } toneReproductionCurveTag;
    ///
    private void writeParametricTRC(ICC_ProfileOutputStream tagTable, ICC_ProfileOutputStream tagData, int signature, float gamma, float a, float b, float c, float d) throws IOException {
        long pointer = tagData.getStreamPosition();
        tagData.writeFourCC("para");
        tagData.writeUInt32(0);
        tagData.writeUInt16(3);
        tagData.writeUInt16(0);
        tagData.writeS15Fixed16Number(gamma);
        tagData.writeS15Fixed16Number(a);
        tagData.writeS15Fixed16Number(b);
        tagData.writeS15Fixed16Number(c);
        tagData.writeS15Fixed16Number(d);
        long size = tagData.getStreamPosition() - pointer;
        writeTagElement(tagTable, tagData, signature, pointer, size);
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
    private void writeFloatingPointArray(ICC_ProfileOutputStream tagTable, ICC_ProfileOutputStream tagData, int signature, float[] array) throws IOException {
        long pointer = tagData.getStreamPosition();
        tagData.writeFourCC("sf32");
        tagData.writeUInt32(0);
        for (float v : array) {
            tagData.writeS15Fixed16Number(v);
        }
        long size = tagData.getStreamPosition() - pointer;
        writeTagElement(tagTable, tagData, signature, pointer, size);
    }

    /// ```
    /// typedef struct {
    ///    uint32 signature;
    ///    uint32 pointer;
    ///    uint32 size;
    /// } tagElement
    /// ``

    private void writeTagElement(ICC_ProfileOutputStream tagTable, ICC_ProfileOutputStream tagData, int signature, long pointer, long size) throws IOException {
        tagTable.writeUInt32(signature);
        tagTable.writeUInt32(pointer);
        tagTable.writeUInt32(size);
    }

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
    ///    uint64 deviceAttributes;
    ///    uint32 renderingIntent;
    ///    xyzNumber illuminant;// must be illuminant D50 [0.9642,1.0,0.8249]
    ///    fourCC creator;
    ///    byte[44] reserved;
    /// } header // 128 bytes
    private void writeHeader(ICC_ProfileOutputStream out) throws IOException {
        out.writeUInt32(0);
        out.writeFourCC("lcms");
        out.writeUInt32(0x2100000);
        out.writeFourCC("mntr");
        out.writeFourCC("RGB ");
        out.writeFourCC("XYZ ");
        out.writeDateTimeNumber(OffsetDateTime.of(2006, 12, 28, 18, 7, 22, 0, ZoneOffset.UTC));
        out.writeFourCC("acsp");
        out.writeFourCC("MSFT");
        out.writeUInt32(0);
        out.writeFourCC("none");
        out.writeUInt32(0);
        out.writeUInt64(0);
        out.writeUInt32(0);// 0=perceptual
        out.writeXYZNumber(new float[]{0.9642176f, 1.0000153f, 0.824918f});
        out.writeFourCC("lcms");//renderingIntent
        out.write(new byte[44]);
        if (out.length() != HEADER_SIZE)
            throw new AssertionError("length should be " + HEADER_SIZE + " but is " + out.length());
    }
}
