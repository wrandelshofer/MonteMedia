# CDXL file format

## Introduction

CDXL is a video file format developed by Commodore for the Amiga computer platform.

## CDXL File Format

A CDXL file uses the `PAN` file format (see [[1]](#1)).

A `PAN` file is a sequence of self-contained `PanFrame`s.
Each `PanFrame` consists of a header, color palette, bitmap data and audio data.

    /**
    ***
    ***  Pan Head Structure
    ***
    *** Minimum required for all types of PAN files, including
    *** custom PAN files.
    ***
    **/
    struct PanHead
    {
        UBYTE   Type;   // PAN structure type
        UBYTE   Info;   // PAN flags and other info
        ULONG   Size;   // Frame size including PAN
    };
    
    // Currently defined Types:
    
    #define PAN_CUSTOM    0    // For your custom data formats
    #define PAN_STANDARD  1    // First standard format
    #define PAN_SPECIAL   2    // Used for testing new ideas


    /**
    *** Pan Frame Structure
    ***
    *** Standard frame structure used for most files.
    **/
    struct PanFrame
    {
        UBYTE    Type;            // Type = PAN_STANDARD
        UBYTE    Info;            // PAN flags and other info (below)
        ULONG    Size;            // Frame size including PAN
        ULONG    Back;            // Bytes back to previous frame
        ULONG    Frame;           // Frame sequence number
        UWORD    XSize;           // Image width in pixels
        UWORD    YSize;           // Image height in pixels
        UBYTE    Reserved;        // Must be zero for now
        UBYTE    PixelSize;       // Bits per pixel (depth)
        UWORD    ColorMapSize;    // Size of color map in bytes
        UWORD    AudioSize;       // Size of audio sample in bytes per channel
        UBYTE    PadBytes[8];     // Reserved for future
        
        UWORD    ColorMap[]       // Color map  (variable size)
        UWORD    Video[];         // Video data (variable size)
        UWORD    Audio[];         // Audio data (variable size)
    };

    #define    PAN_SIZE        sizeof( PAN )

    // PanFrame.Info: Video Types
    #define    PIV_MASK        0x0F    // Mask for video values:
    #define    PIV_STANDARD       0    // Normal RGB encoding
    #define    PIV_HAM            1    // Amiga HAM encoding
    #define    PIV_YUV            2    // YUV encoding
    #define    PIV_AVM            3    // AVM & DCTV encoding
    #define    PI_VIDEO( p )        ( (p)->Info & PIV_MASK )
    
    // PanFrame.Info: Pixel Value Orientation
    #define    PIF_MASK        0xC0    // Mask for orientation values:
    #define    PIF_PLANES      0x00    // Data is bit plane oriented
    #define    PIF_PIXELS      0x40    // Data is pixel oriented (chunky)
    #define    PIF_LINES       0x80    // Data is line oriented
    #define    PI_PIXEL( p )        ( (p)->Info & PIF_MASK )
    
    // PanFrame.Info: Audio type
    #define    PIA_MASK        0x10    // Mask for audio values:
    #define    PIA_MONO        0x00    // 1 channel sound
    #define    PIA_STEREO      0x10    // 2 channel sound
    #define    PI_AUDIO( p )        ( (p)->Info & PIA_MASK )
    
    // Size macros
    #define    ROW_SIZE( x )        ( ( ( (x) + 15 ) / 16 ) << 1 )
    #define    CMAP_SIZE( p )       ( (p)->ColorMapSize )
    #define    PLANE_SIZE( p )      ( ROW_SIZE( (p)->XSize ) * (p)->YSize )
    #define    IMAGE_SIZE( p )      ( PLANE_SIZE( p ) * (p)->PixelSize )
    #define    FRAME_SIZE( p )      ( PAN_SIZE + CMAP_SIZE( p ) + IMAGE_SIZE( p ) + (p)->AudioSize )

## AGABlaster File Format

AGABlaster is a video player for the Amiga Commodore computer. It uses a customized version of the CDXL format
supporting the AGA chipset with 24 bit colors and variable length frames. (see [[2]](#2)).

AGABlaster files store additional data in the `PadBytes` field of a `PanFrame`.

    /**
    *** AGABlaster Pan Frame Structure
    ***
    *** Standard frame structure used for most files.
    **/
    struct AGABlasterPanFrame
    {
        UBYTE      Type;            // Type = PAN_CUSTOM
        UBYTE      Info;            // 
        ULONG      Size;            // 
        ULONG      Back;            // 
        ULONG      Frame;           // 
        UWORD      XSize;           // 
        UWORD      YSize;           // 
        UBYTE      Reserved;        // 
        UBYTE      PixelSize;       // 
        UWORD      ColorMapSize;    // 
        UWORD      AudioSize;       // 
        AGABlasterExtension  PadBytes; // Used by AGABlaster 
        
        UWORD      ColorMap[]       // 
        UWORD      Video[];         // 
        UWORD      Audio[];         // 
    };

    struct AGABlasterExtension {
        UWORD Frequency             // Provided in Hz (0=unspecified)
        UBYTE FramesPerSecond       // 1-255 (0=unspecified)
        UBYTE InfoByte2             // bits  0 - 3 : Resolution
                                    // bit   4     : 0
                                    // bit   5     : Kill EHB
                                    // bit   6     : Frame length mode
                                    // bit   7     : ColorMode
        UWORD Padding               // bits  0 - 3 : Audio padding (size of padding in number of bytes)
                                    // bits  4 - 7 : Video padding (size of padding in number of bytes)
                                    // bits  8 -11 : Palette padding (size of padding in number of bytes)
                                    // bits 12 -14 : Padding mode
                                    // bit  15     : 0
       UWORD Reserved               //
    }

    // AGABlasterExtension.InfoByte2: Resolution Types
    #define    RES_UNSPECIFIED      0   
    #define    RES_LORES            1   
    #define    RES_HIRES            2   
    #define    RES_SUPERHIRES       3   

    // AGABlasterExtension.InfoByte2: ColorMode Types
    #define    CLR_12_BIT_COLORS    0   
    #define    CLR_24_BIT_COLORS    1   


    // AGABlasterExtension.InfoByte2: Kill EHB Types
    #define    KEHB_KEEP_EHB_MODE   0   
    #define    KEHB_USE_AGA6        1   

    // AGABlasterExtension.InfoByte2: Frame length modes
    #define    FRLEN_FIXED_LENGTH     0   
    #define    FRLEN_VARIABLE_LENGTH  1   

    // AGABlasterExtension.Padding: Padding modes
    #define    PADD_UNSPECIFIED      0   
    #define    PADD_NO_PADDING       1   
    #define    PADD_16_BIT_PADDING   2   
    #define    PADD_32_BIT_PADDING   3   
    #define    PADD_64_BIT_PADDING   4   
    #define    PADD_128_BIT_PADDING  5   

## References:

<a id="1">[1]</a>
Carl Sassenrath & Ken Yeast, Pantaray, Inc. (Jan 1991).<br>
Pan.h -- PAN file format used for multimedia sequencing.<br>
CD-ROM CD32://CD32-Tools/cdxl-1/Include/cdxl/pan.h

<a id="2">[2]</a>
Markus Schordan. (2023).<br>
AGABlaster - 68K Commodore Amiga CDXL Video Player.<br>
https://mschordan.github.io/amiga/agablaster.html

