[![Maven Central](https://maven-badges.herokuapp.com/maven-central/ch.randelshofer/org.monte.media/badge.svg)](https://maven-badges.herokuapp.com/maven-central/ch.randelshofer/org.monte.media)

# MonteMedia

A Java library for processing still images, video, audio and meta-data.

## Supported formats:

Container formats:

| Container | Description                                | Operations  |
|-----------|--------------------------------------------|-------------|
| RIFF      | Microsoft Resource Interchange File Format | Read        |
| AVI       | Microsoft Audio Video Interchange          | Read,Write  |
| MOV       | Apple QuickTime                            | Write       |
| JFIF      | JPEG File Interchange Format               | Read, Write |
| MP3       | MP3 Elementary Stream                      | Read        |
| MPO       | MultiPicture Object Format                 | Read        |
| TIFF      | Tagged Image File Format                   | Read, Write |
| IFF       | Electronic Arts Interchange File Format    | Read, Write |

Video encodings:

| Video Encoding | Containers | Description                         | Operations     |
|----------------|------------|-------------------------------------|----------------|
| PNG            | AVI, MOV   | Portable Networks Graphics          | Decode, Encode |
| DIB            | AVI        | Microsoft Device Independent Bitmap | Encode         |
| Run Length     | AVI        | Run Length Encoding                 | Encode         |
| ZMBV           | AVI        | DosBox Capture Codec                | Decode         |
| TSCC           | AVI, MOV   | TechSmith Screen Capture Codec      | Decode, Encode |
| MJPG           | AVI, MOV   | Motion JPEG                         | Decode, Encode |
| Animation      | MOV        | QuickTime Animation                 | Encode         |
| Raw            | MOV        | QuickTime RAW                       | Encode         |
| Op5            | ANIM       | Amiga Animation                     | Decode, Encode |
| Op7 Short/Long | ANIM       | Amiga Animation                     | Decode         |
| Op8 Short/Long | ANIM       | Amiga Animation                     | Decode         |
| SEQ            | SEQ        | Atari Cyber Paint Sequence          | Decode         |

Audio encodings:

| Audio Encoding | Containers | Description           | Operations     |
|----------------|------------|-----------------------|----------------|
| PCM            | AVI, MOV   | Pulse Code Modulation | Decode, Encode |
| 8SVX           | 8SVX, ANIM | Pulse Code Modulation | Decode         |

Image encodings:

| Image Encoding | Containers  | Description                | Operations     |
|----------------|-------------|----------------------------|----------------|
| PGM            | PGM         | Netpbm grayscale image     | Decode         |
| CMYK           | JPEG (JFIF) | JPEG CMYK Image            | Decode         |
| MPO            | MPO (JFIF)  | MultiPicture Object Format | Decode         |
| ILBM           | IFF         | Amiga Interleaved Bitmap   | Decode, Encode |
| PBM            | IFF         | Amiga Packed Bitmap        | Decode, Encode |
| PGM            | PGM         | Netpbm grayscaleimage      | Decode         |

Meta-data encodings:

| Meta-data Encoding | Containers     | Description                    | Operations |
|--------------------|----------------|--------------------------------|------------|
| EXIF               | AVI, JPEG, MPO | Exchangeable Image File Format | Decode     |

## Modules

This project consists of the following modules:

| Module                         | Description                                                                               |
|--------------------------------|-------------------------------------------------------------------------------------------|
| org.monte.media                | Library for processing still images, video, audio and meta-data.                          |
| org.monte.media.screenrecorder | Screen recorder in pure Java (AVI and QuickTime) .                                        |
| org.monte.media.swing          | Swing components.                                                                         |
| org.monte.media.javafx         | JavaFX components.                                                                        |
| org.monte.media.jmf            | Provides the following codecs to JMF: TSCC, ZMBV.                                         |
| org.monte.media.amigaatari     | Additional codecs/container formats for processing Amiga and Atari audio and video files. |
| org.monte.media.animconverter  | Converts Amiga IFF ANIM files to QuickTime.                                               |

The project also provides the following demonstration modules:

| Module                          | Description                                                                               |
|---------------------------------|-------------------------------------------------------------------------------------------|
| org.monte.demo.animmerger       | Demonstrates how to merge two Amiga IFF ANIM files.                                       |
| org.monte.demo.audiorecorder    | Demonstrates how to record audio into an AVI file.                                        |
| org.monte.demo.aviwriter        | Demonstrates how to write a video-only AVI file.                                          |
| org.monte.demo.cleartype        | Demonstrates how to draw text with a ClearType antialiasing effect.                       |
| org.monte.demo.cmykimageviewer  | Demonstrates how to read a JPEG image with CMYK color model.                              |
| org.monte.demo.imageioviewer    | Demonstrates how to read images with ImageIO.                                             |
| org.monte.demo.io               | Demonstrates how to read/trim/concat AVI video files.                                     |
| org.monte.demo.jmfavi           | Demonstrates how write a TSCC encoded AVI file with the JMF library.                      |
| org.monte.demo.jmftsccdemo      | Demonstrates how to play back a TSCC encoded AVI file with the JMF library.               |
| org.monte.demo.moviconverter    | An utterly incomplete demo for a movie conversion tool.                                   |
| org.monte.demo.moviemaker       | Demonstrates how to create a QuickTime movie from a sequence of images and an audio file. |
| org.monte.demo.mpoimagesplitter | Demonstrates how to split up a MPO file into JPEG files.                                  |
| org.monte.demo.quicktimewriter  | Demonstrates how to write a video-only QuickTime file.                                    |
| org.monte.demo.rationalnumber   | Demonstrates how to work with rational numbers.                                           |
| org.monte.demo.screenrecorder   | Demonstrates how to implement a screen recorder.                                          |
| org.monte.demo.sift             | Demonstrates how to analyze the structure of an Amiga IFF file.                           |
