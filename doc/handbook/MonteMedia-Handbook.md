# The Monte Media Handbook

## Introduction

Monte Media is a Java library for processing media data. Supported media
formats include still images, video, audio and meta-data.

## License

The Monte Media Handbook. Copyright © 2013 Werner Randelshofer. MIT License.

## Getting Started

### Getting the library

Download the latest version of the Monte Media library from the
Maven Central Repository:

https://search.maven.org/artifact/ch.randelshofer/org.monte.media

```xml

<dependency>
   <groupId>ch.randelshofer</groupId>
   <artifactId>org.monte.media</artifactId>
   <version>17.2</version>
</dependency>
```

Or get the source code on GitHub:

https://github.com/wrandelshofer/montemedia

Or browse the project home page:

http://www.randelshofer.ch/monte/

### Including the library into a project

Monte Media is a Java Module. It will work as intended, if your project is also a Java Module,
that includes the Monte Media using a `requires` statement.

Here is how your `module-info.java` file might look like:

```java
module MyFirstModule {
   requires org.monte.media;
}
```

## Movies

### Reading video frames from a movie file

To read video frames from a movie file, you have to perform the following steps:

1. Get a MovieReader from the Registry.
2. Determine which track contains video media.
3. Get a Codec from the Registry.
4. In a while-loop: read the media data into a Buffer, and decode the Buffer into
   a BufferedImage.
5. The example code below shows how to read an array of BufferedImages from a
   movie file.

```java
class HowToReadVideoFramesFromAMovieFile {

    BufferedImage[] readMovie(File file) throws IOException {
        ArrayList<BufferedImage> frames = new ArrayList<BufferedImage>();
        MovieReader in = Registry.getInstance().getReader(file);
        Format format = new Format(DataClassKey, BufferedImage.class);
        int track = in.findTrack(0, new Format(MediaTypeKey, MediaType.VIDEO));
        Codec codec = Registry.getInstance().getCodec(in.getFormat(track), format);
        try {
            Buffer inbuf = new Buffer();
            Buffer codecbuf = new Buffer();
            do {
                in.read(track, inbuf);
                codec.process(inbuf, codecbuf);
                if (!codecbuf.isFlag(BufferFlag.DISCARD)) {
                    frames.add(Images.cloneImage((BufferedImage) codecbuf.data));
                }
            } while (!inbuf.isFlag(BufferFlag.END_OF_MEDIA));
        } finally {
            in.close();
        }
        return frames.toArray(new BufferedImage[frames.size()]);
    }
}
```

### Writing video frames into a movie file

To write video frames from into movie file, you have to perform the following steps:

1. Get a MovieWriter from the Registry.
2. Add a track with the desired video format to the MovieWriter.
3. Create a Buffer
4. In a for-loop: encode a BufferedImage into the Buffer, and write it into the MovieWriter.

The example code below shows how to write an array of BufferedImages into a movie file.

```java
class HowToWriteVideoFramesIntoAMovieFile {
    
    void writeMovie(File file, BufferedImage[] frames) throws IOException {
        MovieWriter out = Registry.getInstance().getWriter(file);
        Format format = new Format(MediaTypeKey, MediaType.VIDEO, // EncodingKey, ENCODING_AVI_MJPG,
                FrameRateKey, new Rational(30, 1),//
                WidthKey, frames[0].getWidth(), //
                HeightKey, frames[0].getHeight()
        );
        int track = out.addTrack(format);
        try {
            Buffer buf = new Buffer();
            buf.format = new Format(DataClassKey, BufferedImage.class);
            buf.sampleDuration = format.get(FrameRateKey).inverse();
            for (int i = 0; i < frames.length; i++) {
                buf.data = frames[i];
                out.write(track, buf);
            }
        } finally {
            out.close();
        }
    }
}
```