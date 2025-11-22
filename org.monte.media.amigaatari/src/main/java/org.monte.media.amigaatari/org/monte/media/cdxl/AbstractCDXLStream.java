/*
 * @(#)AbstractCDXLStream.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.cdxl;

import java.io.IOException;

/// <pre>
/// // References:
/// //
/// // Carl Sassenrath & Ken Yeast, Pantaray, Inc. (Jan 1991).<br>
/// // Pan.h -- PAN file format used for multimedia sequencing.<br>
/// // CD-ROM CD32://CD32-Tools/cdxl-1/Include/cdxl/pan.h
/// //
/// // Markus Schordan. (2023).<br>
/// // AGABlaster - 68K Commodore Amiga CDXL Video Player.<br>
/// // https://mschordan.github.io/amiga/agablaster.html
///
///
/// magic AGABlasterPanFrame "CHUNK";
///
/// typedef struct {
///     ubyte enum TypeEnum Type;            // PAN structure type
///     ubits3 enum PixelValueOrientationEnum PixelValueOrientation;   // Info bits 5-7
///     ubits1 enum AudioTypeEnum AudioType;                           // Info bit 4
///     ubits4 enum VideoTypeEnum VideoType;                           // Info bits 0-3
///     uint32 Size;                         // Frame size including PAN
///     uint32 Back;                         // Bytes back to previous frame
///     uint32 Frame;                        // Frame sequence number
///     uint16 XSize;                        // Image width in pixels
///     uint16 YSize;                        // Image height in pixels
///     uint8  Reserved;                     // Must be zero for now
///     uint8  PixelSize;                    // Bits per pixel (depth)
///     uint16 ColorMapSize;                 // Size of color map in bytes
///     uint16 AudioSize;                    // Size of audio sample in bytes
///     AGABlasterExtension AGABlasterExtension; // 8 reserved bytes, must be zero when not used by AGABlaster
///     ubyte[ColorMapSize] ColorMap;        // Color map  (variable size)
///     ubyte[Size-32-ColorMapSize-AudioSize] Video;
///                                          // Video data (variable size)
///     ubyte[AudioSize] Audio;              // Audio data (variable size)
/// } AGABlasterPanFrame;
///
/// enum {
///     CUSTOM   = 0, // For your custom data formats
///     STANDARD = 1, // First standard format
///     SPECIAL  = 2, // Used for testing new ideas
/// } TypeEnum;
///
/// enum {
///     PLANES=0, // Data is bit plane oriented
///     PIXELS=1, // Data is pixel oriented (chunky)
///     LINES=2, // Data is line oriented
/// } PixelValueOrientationEnum;
///
/// enum {
///     MONO=0,   // 1 channel sound
///     STEREO=1  // 2 channel sound
/// } AudioTypeEnum;
///
/// enum {
///     STANDARD=0, // Normal RGB encoding
///     HAM=1,      // Amiga HAM encoding
///     YUV=2,      // YUV encoding
///     AVM=3,      // AVM & DCTV encoding
/// } VideoTypeEnum;
///
/// typedef struct {
///     uint16 Frequency;                                  // Provided in Hz (0=unspecified)
///
///     uint8  FramesPerSecond;                            // 1-255 (0=unspecified)
///
///     ubits1 Reserved;                                   // Info2, bit 7
///     ubits1 enum AgaBlasterFrameLengthModeEnum FrameLengthMode; // Info2, bit 6
///     ubits1 enum AgaBlasterKillEhbEnum KillEHB;         // Info2, bit 5
///     ubits1 enum AgaBlasterColorModeEnum ColorMode;     // Info2, bit 4
///     ubits4 enum AgaBlasterResolutionEnum Resolution;   // Info2, bits 3-0
///
///     ubits4 AudioPadding;                              //Audio padding: 0-15 (size of padding in number of bytes)
///     ubits4 VideoPadding;                              //Video padding: 0-15 (size of padding in number of bytes
///
///     ubits4 PalettePadding;                            //Palette padding: 0-15 (size of padding in number of bytes
///     ubits3 enum AgaBlasterPaddingModeEnum PaddingMode;//Padding mode
///     ubits1 Reserved
///     uint16 Reserved
/// } AGABlasterExtension;
///
/// enum {
///     UNSPECIFIED=0,
///     LORES=1,
///     HIRES=2,
///     SUPERHIRES=3,
/// } AgaBlasterResolutionEnum;
///
/// enum {
///     _12_BIT_COLORS=0,
///     _24_BIT_COLORS=1
/// } AgaBlasterColorModeEnum;
///
/// enum {
///     KEEP_EHB_MODE=0,
///     USE_AGA_6=1
/// } AgaBlasterKillEhbEnum;
///
///
/// enum {
///     UNSPECIFIED=0,
///     NO_PADDING=1,
///     _16_BIT_PADDING=2,
///     _32_BIT_PADDING=3,
///     _64_BIT_PADDING=4,
///     _128_BIT_PADDING=5
/// } AgaBlasterPaddingModeEnum;
///
/// enum {
///     FIXED_LENGTH=0,
///     VARIABLE_LENGTH=1
/// } AgaBlasterFrameLengthModeEnum;
/// </pre>
public class AbstractCDXLStream {
    enum PanStructureType {
        CUSTOM(0),
        STANDARD(1),
        SPECIAL(2);
        private final int encodedValue;

        PanStructureType(int encodedValue) {
            this.encodedValue = encodedValue;
        }

        public int getEncodedValue() {
            return encodedValue;
        }

        public static PanStructureType decode(int encodedValue) throws IOException {
            return switch (encodedValue) {
                case 0 -> CUSTOM;
                case 1 -> STANDARD;
                case 2 -> SPECIAL;
                default -> throw new IOException("Unsupported CDXL type=" + encodedValue);
            };
        }
    }

    public enum PixelValueOrientation {
        PLANES(0),
        PIXELS(1),
        LINES(2);
        private final int encodedValue;

        PixelValueOrientation(int encodedValue) {
            this.encodedValue = encodedValue;
        }

        public int encode() {
            return encodedValue;
        }

        public static PixelValueOrientation decode(int encodedValue) throws IOException {
            return switch (encodedValue) {
                case 0 -> PLANES;
                case 1 -> PIXELS;
                case 2 -> LINES;
                default -> throw new IOException("Unsupported pixelValueOrientation encoding=" + encodedValue);
            };
        }
    }

    public enum AudioType {
        MONO(0),
        STEREO(1);
        private final int encodedValue;

        AudioType(int encodedValue) {
            this.encodedValue = encodedValue;
        }

        public int encode() {
            return encodedValue;
        }

        public static AudioType decode(int encodedValue) throws IOException {
            return switch (encodedValue) {
                case 0 -> MONO;
                case 1 -> STEREO;
                default -> throw new IOException("Unsupported audio encoding=" + encodedValue);
            };
        }
    }

    public enum VideoType {
        STANDARD(0),
        HAM(1),
        YUV(2),
        AVM(3);
        private final int encodedValue;

        VideoType(int encodedValue) {
            this.encodedValue = encodedValue;
        }

        public int encode() {
            return encodedValue;
        }

        public static VideoType decode(int encodedValue) throws IOException {
            return switch (encodedValue) {
                case 0 -> STANDARD;
                case 1 -> HAM;
                case 2 -> YUV;
                case 3 -> AVM;
                default -> throw new IOException("Unsupported video encoding=" + encodedValue);
            };
        }

    }

    public enum AGABlasterResolution {
        UNSPECIFIED(0),
        LORES(1),
        HIRES(2),
        SUPERHIRES(3);
        private final int encodedValue;

        AGABlasterResolution(int encodedValue) {
            this.encodedValue = encodedValue;
        }

        public int encode() {
            return encodedValue;
        }

        public static AGABlasterResolution decode(int encodedValue) throws IOException {
            return switch (encodedValue) {
                case 0 -> UNSPECIFIED;
                case 1 -> LORES;
                case 2 -> HIRES;
                case 3 -> SUPERHIRES;
                default -> throw new IOException("Unsupported AGABlasterResolution=" + encodedValue);
            };
        }

    }

    public enum AgaBlasterFrameLengthMode {
        FIXED_LENGTH(0),
        VARIABLE_LENGTH(1);
        private final int encodedValue;

        AgaBlasterFrameLengthMode(int encodedValue) {
            this.encodedValue = encodedValue;
        }

        public int encode() {
            return encodedValue;
        }

        public static AgaBlasterFrameLengthMode decode(int encodedValue) throws IOException {
            return switch (encodedValue) {
                case 0 -> FIXED_LENGTH;
                case 1 -> VARIABLE_LENGTH;
                default -> throw new IOException("Unsupported AgaBlasterKillEhbEnum=" + encodedValue);
            };
        }
    }

    public enum AgaBlasterKillEhb {
        KEEP_EHB_MODE(0),
        USE_AGA_6(1);
        private final int encodedValue;

        AgaBlasterKillEhb(int encodedValue) {
            this.encodedValue = encodedValue;
        }

        public int encode() {
            return encodedValue;
        }

        public static AgaBlasterKillEhb decode(int encodedValue) throws IOException {
            return switch (encodedValue) {
                case 0 -> KEEP_EHB_MODE;
                case 1 -> USE_AGA_6;
                default -> throw new IOException("Unsupported AgaBlasterKillEhb=" + encodedValue);
            };
        }
    }

    public enum AGABlasterColorMode {
        _12_BIT_COLORS(0),
        _24_BIT_COLORS(1);
        private final int encodedValue;

        AGABlasterColorMode(int encodedValue) {
            this.encodedValue = encodedValue;
        }

        public int encode() {
            return encodedValue;
        }

        public static AGABlasterColorMode decode(int encodedValue) throws IOException {
            return switch (encodedValue) {
                case 0 -> _12_BIT_COLORS;
                case 1 -> _24_BIT_COLORS;
                default -> throw new IOException("Unsupported AGABlasterColorMode=" + encodedValue);
            };
        }
    }
}
