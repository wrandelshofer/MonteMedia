# The Monte Media Handbook

## Introduction

Monte Media is a Java library for processing media data. Supported media
formats include still images, video, audio and meta-data.

## License

The Monte Media Handbook. Copyright © 2013 Werner Randelshofer. MIT License.

## Getting Started

Download the latest version of the Monte Media library from the
Maven Central Repository.

https://search.maven.org/artifact/ch.randelshofer/org.monte.media

Or get the source code on GitHub.

https://github.com/wrandelshofer/montemedia

Or browse the project home page.

http://www.randelshofer.ch/monte/

## Movies

### Reading video frames from a movie file

To read video frames from a movie file, you have to perform the following steps:

* Get a MovieReader from the Registry.
* Determine which track contains video media.
* Get a Codec from the Registry.
* In a while-loop: read the media data into a Buffer, and decode the Buffer into
  a BufferedImage.

* The example code below shows how to read an array of BufferedImages from a
  movie file.

```java
    BufferedImage[] readMovie(File file) throws IOException { 
        ArrayList<BufferedImage> frames=new ArrayList<BufferedImage> ();
        MovieReader in = Registry.getInstance().getReader(file);
        Format format = new Format(DataClassKey, BufferedImage.class);
        int track = in.findTrack(0, new Format(MediaTypeKey,MediaType.VIDEO)); Codec codec=Registry.getInstance().getCodec(in.getFormat(track), format);
        try {
            Buffer inbuf = new Buffer(); Buffer codecbuf = new Buffer(); do {
        in.read(track, inbuf);
        codec.process(inbuf, codecbuf);
        if (!codecbuf.isFlag(BufferFlag.DISCARD)) {
        frames.add(Images.cloneImage((BufferedImage)codecbuf.data)); }
        } while (!inbuf.isFlag(BufferFlag.END_OF_MEDIA)); } finally {
        in.close(); }
        return frames.toArray(new BufferedImage[frames.size()]); }
```
