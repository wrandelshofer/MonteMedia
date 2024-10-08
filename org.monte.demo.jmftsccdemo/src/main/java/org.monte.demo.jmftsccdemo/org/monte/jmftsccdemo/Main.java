/*
 * @(#)Main.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.jmftsccdemo;

import com.sun.media.format.AviVideoFormat;
import org.monte.media.jmf.codec.video.TSCCCodec;
import org.monte.media.swing.BackgroundTask;

import javax.media.Controller;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.DataSink;
import javax.media.EndOfMediaEvent;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoDataSinkException;
import javax.media.NoProcessorException;
import javax.media.Player;
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
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * Demonstrates how to use the Monte Media {@link TSCCCodec}
 * with the Java Media Framework (JMF).
 *
 * @author Werner Randelshofer
 */
public class Main extends javax.swing.JFrame {
    private final static long serialVersionUID = 1L;

    private JFileChooser generateChooser;
    private JFileChooser openChooser;
    private Player player;

    /**
     * Creates new form Main
     */
    public Main() {
        initComponents();
        setSize(400, 400);
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        infoLabel = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        generateVideoItem = new javax.swing.JMenuItem();
        openVideoItem = new javax.swing.JMenuItem();

        FormListener formListener = new FormListener();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("TSCC Codec for JMF - Demo");

        infoLabel.setText("<html><b>This is a demo for the TSCCCodec class.</b><br>\nThis class supports encoding and decoding of the Techsmith Screen Capture format with the Java Media Framework ( JMF).<br>\nCopyright © 2011 Werner Randelshofer.<br>\nThis software can be licensed under Creative Commons Attribution 3.0<br>");
        infoLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 12, 12, 12));
        getContentPane().add(infoLabel, java.awt.BorderLayout.PAGE_START);

        fileMenu.setText("File");

        generateVideoItem.setText("Generate Sample Movies...");
        generateVideoItem.addActionListener(formListener);
        fileMenu.add(generateVideoItem);

        openVideoItem.setText("Open Movie...");
        openVideoItem.addActionListener(formListener);
        fileMenu.add(openVideoItem);

        menuBar.add(fileMenu);

        setJMenuBar(menuBar);

        pack();
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        FormListener() {
        }

        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == generateVideoItem) {
                Main.this.generatePerformed(evt);
            } else if (evt.getSource() == openVideoItem) {
                Main.this.openPerformed(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void generatePerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generatePerformed
        if (generateChooser == null) {
            generateChooser = new JFileChooser();
            generateChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        if (JFileChooser.APPROVE_OPTION == generateChooser.showDialog(this, "Choose Output Folder")) {
            final File f = generateChooser.getSelectedFile();
            final JProgressBar pb = new JProgressBar();
            pb.setIndeterminate(true);
            getContentPane().add(pb, BorderLayout.SOUTH);
            ((JComponent) getContentPane()).revalidate();
            BackgroundTask w = new BackgroundTask() {

                @Override
                protected void construct() throws Exception {
                    new Thread(() -> generateVideos(f.getPath())).start();
                }

                @Override
                protected void finished() {
                    pb.setIndeterminate(false);
                    getContentPane().remove(pb);
                    ((JComponent) getContentPane()).revalidate();
                    getContentPane().repaint();
                }

            };
            new Thread(w).start();
        }
    }//GEN-LAST:event_generatePerformed

    private void openPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openPerformed
        if (openChooser == null) {
            openChooser = new JFileChooser();
        }
        if (JFileChooser.APPROVE_OPTION == openChooser.showOpenDialog(this)) {
            final File f = openChooser.getSelectedFile();
            try {
                final Player newPlayer = Manager.createPlayer(Manager.createDataSource(f.toURI().toURL()));
                newPlayer.addControllerListener(new ControllerListener() {

                    @Override
                    public void controllerUpdate(ControllerEvent event) {
                        if (newPlayer.getState() == Controller.Realized) {
                            newPlayer.removeControllerListener(this);

                            getContentPane().removeAll();
                            JPanel panel = new JPanel(new BorderLayout());
                            panel.add(newPlayer.getVisualComponent(), BorderLayout.CENTER);
                            panel.add(newPlayer.getControlPanelComponent(), BorderLayout.SOUTH);
                            add(panel, BorderLayout.CENTER);
                            pack();

                            if (player != null) {
                                player.close();
                            }
                            player = newPlayer;
                            getRootPane().putClientProperty("Window.documentFile", f);
                            setTitle(f.getName());
                        }
                    }
                });
                newPlayer.realize();
            } catch (Exception ex) {
                JLabel label = new JLabel(ex.toString());
                add(label, BorderLayout.CENTER);
                ((JComponent) getContentPane()).revalidate();
                getContentPane().repaint();
                ex.printStackTrace();
            }
        }

    }//GEN-LAST:event_openPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        TSCCCodec.registerWithJMF();
        Manager.setHint(Manager.LIGHTWEIGHT_RENDERER, true);

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new Main().setVisible(true);
            }
        });
    }

    private void generateVideos(String path) {
        try {
            doGenerateVideo(new File(path, "avidemo-tscc8.avi"), new AviVideoFormat("tscc", null, Format.NOT_SPECIFIED, null, 30f, Format.NOT_SPECIFIED, 8, Format.NOT_SPECIFIED, Format.NOT_SPECIFIED, Format.NOT_SPECIFIED, Format.NOT_SPECIFIED, Format.NOT_SPECIFIED, null));
            //doGenerateVideo(new File(path, "avidemo-tscc16.avi"), new AviVideoFormat("tscc", null, Format.NOT_SPECIFIED, null, 30f, Format.NOT_SPECIFIED, 16, Format.NOT_SPECIFIED, Format.NOT_SPECIFIED, Format.NOT_SPECIFIED, Format.NOT_SPECIFIED, Format.NOT_SPECIFIED, null));
            doGenerateVideo(new File(path, "avidemo-tscc24.avi"), new AviVideoFormat("tscc", null, Format.NOT_SPECIFIED, null, 30f, Format.NOT_SPECIFIED, 24, Format.NOT_SPECIFIED, Format.NOT_SPECIFIED, Format.NOT_SPECIFIED, Format.NOT_SPECIFIED, Format.NOT_SPECIFIED, null));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void doGenerateVideo(File file, AviVideoFormat format) throws IOException, NoProcessorException, NoDataSinkException {
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
        System.out.println("* Wrote " + file);
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
                case 16: {
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
            buffer.setDuration((long) (1e9 / mediaFormat.getFrameRate()));

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
                endOfMedia = true;
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem generateVideoItem;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem openVideoItem;
    // End of variables declaration//GEN-END:variables
}
