/* @(#)Main.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License. */
package org.monte.demo.jmfavi;

import com.sun.media.format.AviVideoFormat;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.IndexColorModel;
import java.io.*;
import java.util.Random;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.DataSink;
import javax.media.EndOfMediaEvent;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoDataSinkException;
import javax.media.NoProcessorException;
import javax.media.Processor;
import javax.media.Time;
import javax.media.control.TrackControl;
import javax.media.datasink.DataSinkErrorEvent;
import javax.media.datasink.DataSinkEvent;
import javax.media.datasink.DataSinkListener;
import javax.media.datasink.EndOfStreamEvent;
import javax.media.format.IndexedColorFormat;
import javax.media.format.RGBFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.FileTypeDescriptor;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;

/**
 * {@code Main}.
 *
 * @author Werner Randelshofer
 * @version $Id: Main.java 348 2015-09-23 17:46:43Z werner $
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String folder;
        if (args.length > 0) {
            folder = args[0];
        } else {
            folder = System.getProperty("user.home");
        }
        try {
            test(new File(folder, "avidemo-tscc8.avi"), new AviVideoFormat("tscc", null, Format.NOT_SPECIFIED, null, 30f, Format.NOT_SPECIFIED, 8, Format.NOT_SPECIFIED, Format.NOT_SPECIFIED, Format.NOT_SPECIFIED, Format.NOT_SPECIFIED, Format.NOT_SPECIFIED, null));
            //test(new File(folder, "avidemo-tscc16.avi"), new AviVideoFormat("tscc", null, Format.NOT_SPECIFIED, null, 30f, Format.NOT_SPECIFIED, 16, Format.NOT_SPECIFIED, Format.NOT_SPECIFIED, Format.NOT_SPECIFIED, Format.NOT_SPECIFIED, Format.NOT_SPECIFIED, null));
            test(new File(folder, "avidemo-tscc24.avi"), new AviVideoFormat("tscc", null, Format.NOT_SPECIFIED, null, 30f, Format.NOT_SPECIFIED, 24, Format.NOT_SPECIFIED, Format.NOT_SPECIFIED, Format.NOT_SPECIFIED, Format.NOT_SPECIFIED, Format.NOT_SPECIFIED, null));

        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    private static void test(File file, AviVideoFormat format) throws IOException, NoProcessorException, NoDataSinkException {
        System.out.println("* Writing " + file);
        DataSource source = new ImageDataSource(format);
        Processor p = Manager.createProcessor(source);
        Handler h = new Handler();
        p.addControllerListener(h);
        p.configure();
        if (!h.waitForState(p, Processor.Configured)) {
            throw new IOException("Could not configure processor.");
        }
        p.setContentDescriptor(new ContentDescriptor(FileTypeDescriptor.MSVIDEO));
        TrackControl trackControls[] = p.getTrackControls();
        javax.media.Format formats[] = trackControls[0].getSupportedFormats();
        if (formats == null || formats.length <= 0) {
            throw new UnsupportedOperationException("No output formats available.");
        }
        String encoding = format.getEncoding();
        javax.media.Format selectedFormat = null;
        for (javax.media.Format f : formats) {
            if (f.getEncoding().equals(encoding)) {
                selectedFormat = f;
                break;
            }
        }
        if (selectedFormat == null) {
            throw new UnsupportedOperationException("No output format selected.");
        }
        trackControls[0].setFormat(selectedFormat);
        p.realize();
        if (!h.waitForState(p, Processor.Realized)) {
            throw new IOException("Could not realize processor.");
        }
        MediaLocator ml = new MediaLocator(file.toURI().toURL());
        DataSink sink = Manager.createDataSink(p.getDataOutput(), ml);
        sink.addDataSinkListener(h);
        sink.open();
        try {
            sink.start();
            p.start();
            if (!h.waitForEndOfMedia()) {
                throw new IOException("Processor reported an error.");
            }
            p.stop();
            sink.stop();
            /*
            if (!h.waitForFileDone()) {
            throw new IOException("DataSink reported an error.");
            }*/
        } finally {
            p.close();
            sink.close();
        }
    }

    private static class Handler implements ControllerListener, DataSinkListener {

        private boolean fileDone, fileSuccess;
        private final Object waitSync = new Object();
        private boolean endOfMedia;

        @Override
        public void controllerUpdate(ControllerEvent evt) {
              //  System.out.println(evt);
            if (evt instanceof EndOfMediaEvent) {
                endOfMedia = true;
            }
            synchronized (waitSync) {
                waitSync.notifyAll();
            }
        }

        @Override
        public void dataSinkUpdate(DataSinkEvent evt) {
              //  System.out.println(evt);
            if (evt instanceof EndOfStreamEvent) {
                fileDone = true;
            } else if (evt instanceof DataSinkErrorEvent) {
                fileDone = true;
                fileSuccess = false;
                endOfMedia=true;
            }
            synchronized (waitSync) {
                waitSync.notifyAll();
            }
        }

        public boolean waitForState(Processor p, int state) {
            synchronized (waitSync) {
                try {
                    while (p.getState() < state) {
                        waitSync.wait();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return p.getState() == state;
        }

        public boolean waitForEndOfMedia() {
            synchronized (waitSync) {
                try {
                    while (!endOfMedia) {
                        waitSync.wait();
                    }
                } catch (Exception e) {
                }
            }
            return endOfMedia;
        }

        public boolean waitForFileDone() {
            synchronized (waitSync) {
                try {
                    while (!fileDone) {
                        waitSync.wait();
                    }
                } catch (Exception e) {
                }
            }
            return fileSuccess;
        }
    }

    private static class ImageStream implements PullBufferStream {

        private int index;
        private int n;
        private Random rnd;
        private BufferedImage img;
        private IndexColorModel palette;
        private Graphics2D g;
        private javax.media.format.VideoFormat mediaFormat;
        private Format format;
        private Object data;

        public ImageStream(AviVideoFormat format) {
            index = 0;
            n = 100;
            rnd = new Random(0); // use seed 0 to get reproducable output
            this.format = format;

            int w = 320;
            int h = 160;

            int depth = format.getBitsPerPixel();
            switch (depth) {
                case 24:
                default: {
                    img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                    mediaFormat = new RGBFormat(new Dimension(w, h), w * h, int[].class,
                            30.0f, 24, 0xff0000, 0xff00, 0xff);
                    data = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
                    break;
                }
                case 16:{
                    img = new BufferedImage(w, h, BufferedImage.TYPE_USHORT_555_RGB);
                    mediaFormat = new RGBFormat(new Dimension(w, h), w * h, int[].class,
                            30.0f, 16, 0xff0000, 0xff00, 0xff);
                    data = ((DataBufferUShort) img.getRaster().getDataBuffer()).getData();
                    break;
                }
                case 8: {
                    byte[] red = new byte[256];
                    byte[] green = new byte[256];
                    byte[] blue = new byte[256];
                    for (int i = 0; i < 255; i++) {
                        red[i] = (byte) rnd.nextInt(256);
                        green[i] = (byte) rnd.nextInt(256);
                        blue[i] = (byte) rnd.nextInt(256);
                    }
                    rnd.setSeed(0); // set back to 0 for reproducable output
                    palette = new IndexColorModel(8, 256, red, green, blue);
                    img = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_INDEXED, palette);
                    data = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
                    mediaFormat = new IndexedColorFormat(new Dimension(w, h),
                            w * h, byte[].class,
                            30.0f, w, 256, red, green, blue);
                    break;
                }
                case 4: {
                    byte[] red = new byte[16];
                    byte[] green = new byte[16];
                    byte[] blue = new byte[16];
                    for (int i = 0; i < 15; i++) {
                        red[i] = (byte) rnd.nextInt(16);
                        green[i] = (byte) rnd.nextInt(16);
                        blue[i] = (byte) rnd.nextInt(16);
                    }
                    rnd.setSeed(0); // set back to 0 for reproducable output
                    IndexColorModel palette = new IndexColorModel(4, 16, red, green, blue);
                    img = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_INDEXED, palette);
                    data = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
                    mediaFormat = new IndexedColorFormat(new Dimension(w, h),
                            w * h, byte[].class,
                            30.0f, w, 16, red, green, blue);
                    break;
                }
            }

            g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setBackground(Color.WHITE);
            g.clearRect(0, 0, img.getWidth(), img.getHeight());
        }

        @Override
        public boolean willReadBlock() {
            return false;
        }

        @Override
        public void read(javax.media.Buffer buffer) throws IOException {
            buffer.setFlags(0);
            if (index >= n - 1) {
                // mark last frame
                buffer.setEOM(true);
            }
            if (index >= n) {
                // handle attempt to read past of stream
                buffer.setDiscard(true);
                return;
            }


            g.setColor(new Color(rnd.nextInt()));
            g.fillOval(rnd.nextInt(img.getWidth() - 30), rnd.nextInt(img.getHeight() - 30), 30, 30);
            buffer.setData(data);
            buffer.setOffset(0);
            buffer.setLength(img.getWidth() * img.getHeight());
            buffer.setSequenceNumber(index);
            buffer.setTimeStamp((long) (1e9 * index / mediaFormat.getFrameRate()));
            buffer.setDuration((long)(1e9/mediaFormat.getFrameRate()));

            index++;
        }

        @Override
        public javax.media.Format getFormat() {
            return mediaFormat;
        }

        @Override
        public ContentDescriptor getContentDescriptor() {
            return new ContentDescriptor(ContentDescriptor.RAW);
        }

        @Override
        public long getContentLength() {
            return 0;
        }

        @Override
        public boolean endOfStream() {
            return index >= n;
        }

        @Override
        public Object[] getControls() {
            return new Object[0];
        }

        @Override
        public Object getControl(String controlType) {
            return null;
        }
    }

    private static class ImageDataSource extends PullBufferDataSource {

        private PullBufferStream streams[];

        public ImageDataSource(AviVideoFormat format) {
            streams = new PullBufferStream[]{new ImageStream(format)};
        }

        @Override
        public PullBufferStream[] getStreams() {
            return streams.clone();
        }

        @Override
        public String getContentType() {
            return ContentDescriptor.RAW;
        }

        @Override
        public void connect() throws IOException {
            // nothing to do
        }

        @Override
        public void disconnect() {
            // nothing to do
        }

        @Override
        public void start() throws IOException {
            // nothing to do
        }

        @Override
        public void stop() throws IOException {
            // nothing to do
        }

        @Override
        public Object[] getControls() {
            return new Object[0];
        }

        @Override
        public Object getControl(String controlType) {
            return null;
        }

        @Override
        public Time getDuration() {
            return DURATION_UNKNOWN;
        }
    }
}
