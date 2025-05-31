References: https://web.archive.org/web/20030108081402/http://home.t-online.de/home/K_Andreas/CDXL.HTM

# Abmessungstabelle für verschiedene konstante Abspielgeschwindigkeiten?

Bei 300 Kb/Sek. würden ungefähr 36 Minuten Animation auf eine ISO CD-Rom [640 Mb] passen, was ungefähr der Hälfte der
Video-CD [MPEG-1 ~ 72 min. in 384x280x24] entspricht.

| Verhältnis | Video                             | Audio                   |
|------------|-----------------------------------|-------------------------|
| 1:1        | 180x180 in HAM6 mit 12 FPS        | 8 Bit Mono in 11.025 kH |
| 5:4        | PAL 200x164 in HAM6 mit 12 FPS    | 8 Bit Mono in 11.025 kH |
| 4:3        | VGA 208x158 in HAM6 mit 12 FPS    | 8 Bit Mono in 11.025 kH |
| 3:2        | NTSC 224x146 in HAM6 mit 12 FPS   | 8 Bit Mono in 11.025 kH |
| 16:9       | PAL+ 240x136 in HAM6 mit 12 FPS   | 8 Bit Mono in 11.025 kH |
| 2:1        | CINEMA 256x128 in HAM6 mit 12 FPS | 8 Bit Mono in 11.025 kH |

# CDXL-Auflösungs-Kombinationen für unterschiedliche CD-Rom Laufwerkstypen

jeweils mit 4096 Farben und 12 bis 24 Bildern pro Sekunde.

| Laufwerk               | Video                      | Audio                     |
|------------------------|----------------------------|---------------------------|
| 1fach-Speed (150 kB)   | 160x100 in HAM6 mit 12 FPS | 8 Bit Mono in 11.025 kHz  |
| 2fach-Speed (300 kB)   | 224x146 in HAM6 mit 12 FPS | 8 Bit Mono in 11.025 kHz  |
| 3fach-Speed (450 kB)   | 272x170 in HAM6 mit 12 FPS | 8 Bit Mono in 22.05 kHz   |
| 4fach-Speed (600 kB)   | 320x200 in HAM6 mit 12 FPS | 8 Bit Mono in 22.05 kHz   |
| 6fach-Speed (900 kB)   | 384x240 in HAM6 mit 12 FPS | 8 Bit Stereo in 22.05 kHz |
| 8fach-Speed (1200 kB)  | 384x274 in HAM6 mit 15 FPS | 8 Bit Stereo in 22.05 kHz |
| 10fach-Speed (1500 kB) | 384x274 in HAM6 mit 20 FPS | 8 Bit Stereo in 22.05 kHz |
| 12fach-Speed (1800 kB) | 384x274 in HAM6 mit 24 FPS | 8 Bit Stereo in 22.05 kHz |

# CDXL FORMAT-Beschreibung

```
Typenbedeutung:
BYTE 8 Bit vorzeichenlose ganze Zahl
WORD 16 Bit vorzeichenlose ganze Zahl im 'Motorola'-Byte-Sex
LONG 32 Bit vorzeichenlose ganze Zahl im 'Motorola'-Byte-Sex

CDXL-Format Beschreibung [CHUNK]

#01 BYTE: $00 {0}   [CUSTOM CDXL]
or BYTE: $01 {1}   [STANDARD CDXL]
or BYTE: $02 {2}   [SPECIAL CDXL]

Das CDXL-Info-Byte ensteht durch Addition
dreier Beschreibungsgruppen:

#02 BYTE: $00 {0}   [RGB]  ...VIDEO ENCODING
or BYTE: $01 {1}   [HAM]
or BYTE: $02 {2}   [YUV]
or BYTE: $03 {3}   [AVM & DCTV]
plus

#02 BYTE: $00 {0}   [BIT PLANAR] ...ORIENTATION
or BYTE: $20 {32}  [BYTE PLANAR]
or BYTE: $40 {64}  [CHUNKY]
or BYTE: $80 {128} [BIT LINE]
or BYTE: $C0 {192} [BYTE LINE]
plus

#02 BYTE: $00 {0}   [MONO]     ...AUDIO VALUES
or BYTE: $10 {16}  [STEREO]

#03 LONG: $0000295C {10588} [CURRENT CHUNKSIZE]
#04 LONG: $0000295C {10588} [PREVIOUS CHUNKSIZE]
#05 WORD: $0000 {0}         [RESERVED]
#06 WORD: $0001 {1}         [CURRENT FRAMENUMBER]
#07 WORD: $00CB {203}       [BITMAPWIDTH]
#08 WORD: $005E {94}        [BITMAPHEIGHT]
#09 WORD: $0004 {4}         [NUMBEROFBITPLANES]
#10 WORD: $0020 {32}        [COLORMAPSIZE]
#11 WORD: $02EC {748}       [RAWSOUNDSIZE]
#12 LONG: $00000000 {0}     [RESERVED]
#13 LONG: $00000000 {0}     [RESERVED]

BYTE #32 - #64 [COLORMAP] ; die Pens sind immer
WORT-kodiert mit führenden Nullen
d. h. die Palette ist immer 12 Bit (4096 Farben).

z.B. 0FFF  oder  0E9A

      |||_15      |||_10  ; RGB_Blau_Anteil (0-15)
      ||__15      ||__09  ; RGB_Grün_Anteil (0-15)
      |___15      |___14  ; RGB_Rot_Anteil  (0-15)

Die Daten liegen immer in dieser Reihenfolge vor:

#3  BITMAPDATA      [UNCOMPRESSED BODY]
#4  SOUNDDATA       [UNCOMPRESSED BODY]

#1  HEADER
#2  COLORMAPDATA
#3  BITMAPDATA
#4  SOUNDDATA

u.s.w. angeordnet.
``` 

# AGABlaster

| Size  | Content                                    | Comment                                                                                                                  |
|-------|--------------------------------------------|--------------------------------------------------------------------------------------------------------------------------|
| BYTE  | 	File type                                 | Supported file types: 0=Custom CDXL, 1=Standard CDXL.                                                                    |
| BYTE  | 	Info byte                                 |                                                                                                                          |
|       | bits 0-3: Video encoding                   | Supported video encodings: 0=RGB, 1=HAM.                                                                                 |
|       | bit  4  : Audio mode (stereo flag)         | Supported audio modes: 0=mono, 1=stereo                                                                                  |
|       | bits 5-7: Plane arrangement                | Supported plane arrangements: 0=bit planar                                                                               |
| LONG	 | Current chunk size                         | = 32 + (Palette size + Palette Padding)                                                                                  |
|       |                                            | + (Video width in bytes)*(Video height)*(Number of bitplanes)                                                            |
|       |                                            | + Video padding + (Audio data size (twice as long if stereo) + Audio padding                                             |
| LONG  | 	Previous chunk size                       | chunk size of previous frame                                                                                             |
| LONG  | 	Current frame number                      | 1-4294967295                                                                                                             |
| WORD  | 	Video width                               | Supported >= 128                                                                                                         |
| WORD  | 	Video height                              | Supported >= 128. Overscan is supported.                                                                                 |
| BYTE	 | reserved                                   | 0                                                                                                                        |
| BYTE  | 	Number of bit planes                      | 0-8 (if 0, then only background color is displayed; allows for empty video frame data)                                   |
| WORD  | 	Palette size in bytes                     | 0-768 (must be a multiple of 2 or 3)                                                                                     |
| WORD  | 	Audio data size in bytes                  | 2-65534 (only even numbers; sample length; in stereo mode total audio size is twice as long; audio data must be present) |
| WORD  | 	Frequency [extension]                     | Provided in Hz (0=unspecified)                                                                                           |
| BYTE  | 	Frames per second [extension]             | 1-255 (0=unspecified)                                                                                                    |
| BYTE  | 	Info byte 2 [extension]                   |                                                                                                                          |
|       | bits 0-3: Resolution                       | Supported resolutions: 0=unspecified, 1=lores, 2=hires, 3=superhires                                                     |
|       | bit  4  : Color mode                       | Supported color mode: 0=12 bit colors, 1=24 bit colors                                                                   |
|       | bit  5  : Kill EHB                         | Kill EHB: 0=keep EHB mode, 1: kill EHB, use AGA6 [since V0.9.82, only required for 12-bit CDXL videos]                   |
|       | bit  6  : Frame length mode (AGAConv 1.1+) | Frame length mode: 1=variable-length, 0=fixed-length                                                                     |
|       | bit  7  : 0                                |                                                                                                                          |
| WORD  | 	Padding [extension]                       |                                                                                                                          |
|       | bits  0- 3: Audio padding                  | Audio padding: 0-15 (size of padding in number of bytes)                                                                 |
|       | bits  4- 7: Video padding                  | Video padding: 0-15 (size of padding in number of bytes)                                                                 |                  
|       | bits  8-11: Palette padding                | Palette padding: 0-15 (size of padding in number of bytes)                                                               |
|       | bits 12-14: Padding mode                   | Padding modes: 0=unspecified, 1=no padding, 2=16 bit padding, 3=32 bit padding, 4=64 bit padding, 5=128 bit padding      |
|       | bit     15: 0                              |                                                                                                                          |
| WORD  | 	reserved                                  | 	0                                                                                                                       |