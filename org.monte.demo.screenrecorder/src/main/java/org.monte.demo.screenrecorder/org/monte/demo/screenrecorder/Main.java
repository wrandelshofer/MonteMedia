/*
 * @(#)Main.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.demo.screenrecorder;

import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys.MediaType;
import org.monte.media.math.Rational;
import org.monte.media.screenrecorder.JRecordingAreaFrame;
import org.monte.media.screenrecorder.MouseFormatKeys;
import org.monte.media.screenrecorder.ScreenRecorder;
import org.monte.media.screenrecorder.ScreenRecorderConfig;
import org.monte.media.screenrecorder.SimpleScreenRecorder;
import org.monte.media.screenrecorder.State;
import org.monte.media.swing.BackgroundTask;
import org.monte.media.swing.JLabelHyperlinkHandler;
import org.monte.media.swing.datatransfer.DropFileTransferHandler;
import org.monte.media.util.MathUtil;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.prefs.Preferences;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.FrameRateKey;
import static org.monte.media.av.FormatKeys.KeyFrameIntervalKey;
import static org.monte.media.av.FormatKeys.MIME_AVI;
import static org.monte.media.av.FormatKeys.MIME_MP4;
import static org.monte.media.av.FormatKeys.MIME_QUICKTIME;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ByteOrderKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ChannelsKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.SampleRateKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.SampleSizeInBitsKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.SignedKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.COMPRESSOR_NAME_QUICKTIME_ANIMATION;
import static org.monte.media.av.codec.video.VideoFormatKeys.COMPRESSOR_NAME_QUICKTIME_JPEG;
import static org.monte.media.av.codec.video.VideoFormatKeys.COMPRESSOR_NAME_QUICKTIME_PNG;
import static org.monte.media.av.codec.video.VideoFormatKeys.COMPRESSOR_NAME_QUICKTIME_RAW;
import static org.monte.media.av.codec.video.VideoFormatKeys.CompressorNameKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DepthKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_AVC1;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_AVI_DIB;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_AVI_MJPG;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_AVI_PNG;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_AVI_RLE8;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_QUICKTIME_ANIMATION;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_QUICKTIME_JPEG;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_QUICKTIME_PNG;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_QUICKTIME_RAW;
import static org.monte.media.av.codec.video.VideoFormatKeys.HeightKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.QualityKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.WidthKey;

/**
 * Main.
 *
 * @author Werner Randelshofer
 */
public class Main extends javax.swing.JFrame {

    private final static long serialVersionUID = 1L;
    public static final String COLORS_HUNDREDS = "Hundreds";
    public static final String COLORS_THOUSANDS = "Thousands";
    public static final String COLORS_MILLIONS = "Millions";
    public static final String ENC_SCREEN_CAPTURE = "Screen Capture";
    public static final String ENC_RUN_LENGTH = "Run Length";
    public static final String ENC_NONE = "None";
    public static final String ENC_PNG = "PNG";
    public static final String ENC_JPEG_100PERCENT = "JPEG 100 %";
    public static final String ENC_JPEG_50PERCENT = "JPEG  50 %";
    public static final String ENC_ANIMATION = "Animation";
    public static final String ENC_H264 = "H264";
    public static final String FMT_MP4 = "MP4";
    public static final String FMT_AVI = "AVI";
    public static final String FMT_QUICKTIME = "QuickTime";

    private class Handler implements BiConsumer<State, State> {

        @Override
        public void accept(State oldState, State newState) {
            ScreenRecorder r = screenRecorder;
            if (r != null && newState == State.FAILED) {
                recordingFailed();
            }
        }
    }

    private Handler handler = new Handler();
    private ScreenRecorder screenRecorder;
    private String depth;
    private String format;
    private String encoding;
    private int cursor;
    private String audioRate;
    private int audioSource;
    private int area;
    private double screenRate;
    private double mouseRate;
    private File movieFolder;


    private record AudioSourceItem(
            String title,
            Mixer.Info mixerInfo,
            AudioFormat format) {

        @Override
        public String toString() {
            return title;
        }
    }

    private static class AreaItem {

        private String title;
        /**
         * Area or null for entire screen.
         */
        private Dimension inputDimension;
        /**
         * null if same value as input dimension.
         */
        private Dimension outputDimension;
        /**
         * SwingConstants.CENTER, .NORTH_WEST, SOUTH_WEST.
         */
        private int alignment;
        private Point location;

        public AreaItem(String title, Dimension dim, int alignment) {
            this(title, dim, null, alignment, new Point(0, 0));
        }

        public AreaItem(String title, Dimension inputDim, Dimension outputDim, int alignment, Point location) {
            this.title = title;
            this.inputDimension = inputDim;
            this.outputDimension = outputDim;
            this.alignment = alignment;
            this.location = location;
        }

        @Override
        public String toString() {
            return title;
        }

        public Rectangle getBounds(GraphicsConfiguration cfg) {
            Rectangle areaRect = null;
            if (inputDimension != null) {
                areaRect = new Rectangle(0, 0, inputDimension.width, inputDimension.height);
            }
            outputDimension = outputDimension;
            Rectangle screenBounds = cfg.getBounds();
            if (areaRect == null) {
                areaRect = (Rectangle) screenBounds.clone();
            }
            switch (alignment) {
                case SwingConstants.CENTER:
                    areaRect.x = screenBounds.x + (screenBounds.width - areaRect.width) / 2;
                    areaRect.y = screenBounds.y + (screenBounds.height - areaRect.height) / 2;
                    break;
                case SwingConstants.NORTH_WEST:
                    areaRect.x = screenBounds.x;
                    areaRect.y = screenBounds.y;
                    break;
                case SwingConstants.SOUTH_WEST:
                    areaRect.x = screenBounds.x;
                    areaRect.y = screenBounds.y + screenBounds.height - areaRect.height;
                    break;
                default:
                    break;
            }
            areaRect.translate(location.x, location.y);

            areaRect = areaRect.intersection(screenBounds);
            return areaRect;

        }
    }

    /**
     * Creates new form ScreenRecorderMain
     */
    public Main() {
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(12, 20, 20, 20));
        initComponents();

        String version = Main.class.getPackage().getImplementationVersion();
        if (version != null) {
            int p = version.indexOf(' ');
            setTitle(getTitle() + " " + version.substring(0, p == -1 ? version.length() : p));
        }

        final Preferences prefs = Preferences.userNodeForPackage(Main.class);
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            movieFolder = new File(System.getProperty("user.home") + File.separator + "Videos");
        } else {
            movieFolder = new File(System.getProperty("user.home") + File.separator + "Movies");
        }
        movieFolder = new File(prefs.get("ScreenRecorder.movieFolder", movieFolder.toString()));

        final String infoLabelText = infoLabel.getText();
        infoLabel.setText(infoLabelText.replaceAll("\"Movies\"", "\"<a href=\"" + movieFolder.toURI() + "\">" + movieFolder.getName() + "</a>\""));
        new JLabelHyperlinkHandler(infoLabel, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    File f = new File(new URI(e.getActionCommand()));
                    if (!f.exists()) {
                        f.mkdirs();
                    }
                    Desktop.getDesktop().open(f);
                } catch (URISyntaxException ex) {
                    System.err.println("ScreenRecorderMain bad href " + e.getActionCommand() + ", " + ex);
                } catch (IOException ex) {
                    System.err.println("ScreenRecorderMain io exception: " + ex);
                }
            }
        });
        infoLabel.setTransferHandler(new DropFileTransferHandler(JFileChooser.DIRECTORIES_ONLY, null, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                movieFolder = new File(e.getActionCommand());
                prefs.put("ScreenRecorder.movieFolder", movieFolder.toString());
                infoLabel.setText(infoLabelText.replaceAll("\"Movies\"", "\"<a href=\"" + movieFolder.toURI() + "\">" + movieFolder.getName() + "</a>\""));
            }
        }));

        depth = prefs.get("ScreenRecording.colorDepth", COLORS_MILLIONS);
        colorsChoice.setSelectedIndex(findIndex(depth, colorsChoice.getModel()));
        format = prefs.get("ScreenRecording.format", FMT_MP4);
        formatChoice.setSelectedIndex(findIndex(format, formatChoice.getModel()));
        encoding = prefs.get("ScreenRecording.encoding", ENC_H264);
        encodingChoice.setSelectedIndex(findIndex(encoding, encodingChoice.getModel()));
        cursor = min(max(0, prefs.getInt("ScreenRecording.cursor", 1)), cursorChoice.getItemCount() - 1);
        cursorChoice.setSelectedIndex(cursor);

        screenRate = prefs.getDouble("ScreenRecording.screenRate", 15);
        SpinnerNumberModel screenRateModel = new SpinnerNumberModel(screenRate, 1, 30, 1);
        screenRateField.setModel(screenRateModel);

        mouseRate = prefs.getDouble("ScreenRecording.mouseRate", 30);
        SpinnerNumberModel mouseRateModel = new SpinnerNumberModel(mouseRate, 1, 30, 1);
        mouseRateField.setModel(mouseRateModel);

        audioSourceChoice.setModel(new DefaultComboBoxModel<>(getAudioSources()));
        audioSource = MathUtil.clamp(prefs.getInt("ScreenRecording.audioSource", 0), 0, audioSourceChoice.getItemCount() - 1);
        if (0 < audioSource && audioSource < audioSourceChoice.getItemCount()) {
            audioSourceChoice.setSelectedIndex(audioSource);
        }
        audioRate = prefs.get("AudioRate", audioRateChoice.getItemAt(0));
        audioRateChoice.setSelectedIndex(findIndex(audioRate, audioRateChoice.getModel()));
        Dimension customDim = new Dimension(prefs.getInt("ScreenRecording.customAreaWidth", 1024),
                prefs.getInt("ScreenRecording.customAreaHeight", 768));
        Point customLoc = new Point(
                prefs.getInt("ScreenRecording.customAreaX", 100),
                prefs.getInt("ScreenRecording.customAreaY", 100));
        areaChoice.setModel(new DefaultComboBoxModel<>(new AreaItem[]{
                new AreaItem("Entire Screen", null, SwingConstants.NORTH_WEST),
                new AreaItem("Center 1280 x 720", new Dimension(1280, 720), SwingConstants.CENTER),
                new AreaItem("Center 1024 x 768", new Dimension(1024, 768), SwingConstants.CENTER),
                new AreaItem("Center   800 x 600", new Dimension(800, 600), SwingConstants.CENTER),
                new AreaItem("Center   640 x 480", new Dimension(640, 480), SwingConstants.CENTER),
                new AreaItem("Top Left 1280 x 720", new Dimension(1280, 720), SwingConstants.NORTH_WEST),
                new AreaItem("Top Left 1024 x 768", new Dimension(1024, 768), SwingConstants.NORTH_WEST),
                new AreaItem("Top Left   800 x 600", new Dimension(800, 600), SwingConstants.NORTH_WEST),
                new AreaItem("Top Left   640 x 480", new Dimension(640, 480), SwingConstants.NORTH_WEST),
                new AreaItem("Bottom Left 1280 x 720", new Dimension(1280, 720), SwingConstants.SOUTH_WEST),
                new AreaItem("Bottom Left 1024 x 768", new Dimension(1024, 768), SwingConstants.SOUTH_WEST),
                new AreaItem("Bottom Left   800 x 600", new Dimension(800, 600), SwingConstants.SOUTH_WEST),
                new AreaItem("Bottom Left   640 x 480", new Dimension(640, 480), SwingConstants.SOUTH_WEST),
                new AreaItem("Custom: " + customLoc.x + ", " + customLoc.y + "; " + customDim.width + " x " + customDim.height + "", customDim, null, SwingConstants.NORTH_WEST, customLoc),}));
        areaChoice.setMaximumRowCount(16);
        area = prefs.getInt("ScreenRecording.area", 0);
        areaChoice.setSelectedIndex(min(areaChoice.getItemCount() - 1, max(0, area)));

        getRootPane().setDefaultButton(startStopButton);
        updateEncodingChoice();
        pack();
    }

    private int findIndex(String value, ComboBoxModel<String> model) {
        for (int i = 0, n = model.getSize(); i < n; i++) {
            if (Objects.equals(value, model.getElementAt(i))) {
                return i;
            }
        }
        return 0;
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        formatLabel = new javax.swing.JLabel();
        formatChoice = new javax.swing.JComboBox();
        colorsLabel = new javax.swing.JLabel();
        colorsChoice = new javax.swing.JComboBox();
        infoLabel = new javax.swing.JLabel();
        startStopButton = new javax.swing.JButton();
        mouseLabel = new javax.swing.JLabel();
        cursorChoice = new javax.swing.JComboBox();
        screenRateLabel = new javax.swing.JLabel();
        screenRateField = new javax.swing.JSpinner();
        mouseRateLabel = new javax.swing.JLabel();
        mouseRateField = new javax.swing.JSpinner();
        encodingLabel = new javax.swing.JLabel();
        encodingChoice = new javax.swing.JComboBox();
        areaLabel = new javax.swing.JLabel();
        areaChoice = new javax.swing.JComboBox();
        selectAreaButton = new javax.swing.JButton();
        stateLabel = new javax.swing.JLabel();
        audioSourceLabel = new javax.swing.JLabel();
        audioSourceChoice = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        audioRateChoice = new javax.swing.JComboBox<>();

        FormListener formListener = new FormListener();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Monte Screen Recorder");
        setResizable(false);
        addWindowListener(formListener);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        formatLabel.setText("Format:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 6, 0, 0);
        getContentPane().add(formatLabel, gridBagConstraints);

        formatChoice.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"MP4", "AVI", "QuickTime"}));
        formatChoice.addActionListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 0);
        getContentPane().add(formatChoice, gridBagConstraints);

        colorsLabel.setText("Colors:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 0, 0);
        getContentPane().add(colorsLabel, gridBagConstraints);

        colorsChoice.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Hundreds", "Thousands", "Millions"}));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 18, 0, 0);
        getContentPane().add(colorsChoice, gridBagConstraints);

        infoLabel.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        infoLabel.setText("<html>The recording will be stored in the folder \"Movies\".<br> Drop a folder on this text to change the storage location.<br> A new file will be created every hour or when the file size limit is reached.<br> <br>This window will be minized before the recording starts.<br> To stop the recording, restore this window and press the Stop button.<br> ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 82;
        gridBagConstraints.ipadx = 363;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 6, 0, 6);
        getContentPane().add(infoLabel, gridBagConstraints);

        startStopButton.setText("Start");
        startStopButton.addActionListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 33;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.gridwidth = 48;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 0);
        getContentPane().add(startStopButton, gridBagConstraints);

        mouseLabel.setText("Mouse:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 6, 0, 0);
        getContentPane().add(mouseLabel, gridBagConstraints);

        cursorChoice.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"No Cursor", "Black Cursor", "White Cursor"}));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 0);
        getContentPane().add(cursorChoice, gridBagConstraints);

        screenRateLabel.setText("Screen Rate:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 18, 0, 0);
        getContentPane().add(screenRateLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 15;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 37;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.ipadx = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 13, 0, 0);
        getContentPane().add(screenRateField, gridBagConstraints);

        mouseRateLabel.setText("Mouse Rate:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 0, 0, 0);
        getContentPane().add(mouseRateLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 15;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 37;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 13, 0, 0);
        getContentPane().add(mouseRateField, gridBagConstraints);

        encodingLabel.setText("Encoding:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(14, 18, 0, 0);
        getContentPane().add(encodingLabel, gridBagConstraints);

        encodingChoice.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"H264"}));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 15;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 19;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 13, 0, 0);
        getContentPane().add(encodingChoice, gridBagConstraints);

        areaLabel.setText("Area:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 6, 0, 0);
        getContentPane().add(areaLabel, gridBagConstraints);

        areaChoice.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Entire Screen", "0 0,  1024 x 768", " "}));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 0);
        getContentPane().add(areaChoice, gridBagConstraints);

        selectAreaButton.setText("Custom Area...");
        selectAreaButton.addActionListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        getContentPane().add(selectAreaButton, gridBagConstraints);

        stateLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        stateLabel.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.gridwidth = 18;
        gridBagConstraints.ipadx = 186;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 94, 0, 0);
        getContentPane().add(stateLabel, gridBagConstraints);

        audioSourceLabel.setText("Audio:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 6, 0, 0);
        getContentPane().add(audioSourceLabel, gridBagConstraints);

        audioSourceChoice.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"No Audio", "44.100 kHz"}));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 47;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 198;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 18, 0, 0);
        getContentPane().add(audioSourceChoice, gridBagConstraints);

        jLabel1.setText("Sample Rate:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 0, 0, 0);
        getContentPane().add(jLabel1, gridBagConstraints);

        audioRateChoice.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"48000", "44100"}));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 15;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 37;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 0);
        getContentPane().add(audioRateChoice, gridBagConstraints);

        pack();
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, java.awt.event.WindowListener {
        FormListener() {
        }

        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == formatChoice) {
                Main.this.formatChoicePerformed(evt);
            } else if (evt.getSource() == startStopButton) {
                Main.this.startStopPerformed(evt);
            } else if (evt.getSource() == selectAreaButton) {
                Main.this.selectAreaPerformed(evt);
            }
        }

        public void windowActivated(java.awt.event.WindowEvent evt) {
        }

        public void windowClosed(java.awt.event.WindowEvent evt) {
        }

        public void windowClosing(java.awt.event.WindowEvent evt) {
            if (evt.getSource() == Main.this) {
                Main.this.formWindowClosing(evt);
            }
        }

        public void windowDeactivated(java.awt.event.WindowEvent evt) {
        }

        public void windowDeiconified(java.awt.event.WindowEvent evt) {
            if (evt.getSource() == Main.this) {
                Main.this.formWindowDeiconified(evt);
            }
        }

        public void windowIconified(java.awt.event.WindowEvent evt) {
        }

        public void windowOpened(java.awt.event.WindowEvent evt) {
        }
    }// </editor-fold>//GEN-END:initComponents

    private static Vector<AudioSourceItem> getAudioSources() {
        Vector<AudioSourceItem> l = new Vector<>();
        for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo()) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            for (Line.Info targetLineInfo : mixer.getTargetLineInfo()) {
                if (targetLineInfo instanceof DataLine.Info) {
                    DataLine.Info dlInfo = (DataLine.Info) targetLineInfo;
                    for (AudioFormat format : dlInfo.getFormats()) {
                        if (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED) {
                            l.add(new AudioSourceItem(
                                    mixerInfo.getName() + ", " + format,
                                    mixerInfo, format));
                        }
                    }
                }
            }
        }
        return l;
    }

    private void updateValues() {
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        format = (String) formatChoice.getSelectedItem();
        prefs.put("ScreenRecording.format", format);
        encoding = (String) encodingChoice.getSelectedItem();
        prefs.put("ScreenRecording.encoding", encoding);
        depth = (String) colorsChoice.getSelectedItem();
        prefs.put("ScreenRecording.colorDepth", depth);
        cursor = cursorChoice.getSelectedIndex();
        prefs.putInt("ScreenRecording.cursor", cursor);
        audioSource = audioSourceChoice.getSelectedIndex();
        prefs.putInt("ScreenRecording.audioSource", audioSource);
        audioRate = (String) audioRateChoice.getSelectedItem();
        prefs.put("ScreenRecording.audioRate", audioRate);
        area = areaChoice.getSelectedIndex();
        prefs.putInt("ScreenRecording.area", area);
        if (screenRateField.getValue() instanceof Double) {
            screenRate = (Double) screenRateField.getValue();
            prefs.putDouble("ScreenRecording.screenRate", screenRate);
        }
        if (mouseRateField.getValue() instanceof Double) {
            mouseRate = (Double) mouseRateField.getValue();
            prefs.putDouble("ScreenRecording.mouseRate", mouseRate);
        }
    }

    private void start() throws IOException, AWTException {
        updateValues();

        if (screenRecorder == null) {
            setSettingsEnabled(false);
            stateLabel.setForeground(Color.RED);
            stateLabel.setText("Recording...");

            String mimeType;
            String videoFormatName, compressorName;
            float quality = 1.0f;
            int bitDepth;
            switch (depth) {
                default:
                case COLORS_HUNDREDS:
                    bitDepth = 8;
                    break;
                case COLORS_THOUSANDS:
                    bitDepth = 16;
                    break;
                case COLORS_MILLIONS:
                    bitDepth = 24;
                    break;
            }
            switch (format) {
                default:
                case FMT_MP4:
                    mimeType = MIME_MP4;
                    videoFormatName = compressorName = ENCODING_AVC1;
                    bitDepth = 24;
                    break;
                case FMT_AVI:
                    mimeType = MIME_AVI;
                    switch (encoding) {
                        case ENC_H264:
                            videoFormatName = compressorName = ENCODING_AVC1;
                            bitDepth = 24;
                            break;
                        case ENC_SCREEN_CAPTURE:
                        default:
                            videoFormatName = compressorName = ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE;
                            break;
                        case ENC_RUN_LENGTH:
                            videoFormatName = compressorName = ENCODING_AVI_RLE8;
                            bitDepth = 8;
                            break;
                        case ENC_NONE:
                            videoFormatName = compressorName = ENCODING_AVI_DIB;
                            if (bitDepth == 16) {
                                bitDepth = 24;
                            }
                            break;
                        case ENC_PNG:
                            videoFormatName = compressorName = ENCODING_AVI_PNG;
                            bitDepth = 24;
                            break;
                        case ENC_JPEG_100PERCENT:
                            videoFormatName = compressorName = ENCODING_AVI_MJPG;
                            bitDepth = 24;
                            break;
                        case ENC_JPEG_50PERCENT:
                            videoFormatName = compressorName = ENCODING_AVI_MJPG;
                            bitDepth = 24;
                            quality = 0.5f;
                            break;
                    }
                    break;
                case FMT_QUICKTIME:
                    mimeType = MIME_QUICKTIME;
                    switch (encoding) {
                        case ENC_H264:
                            videoFormatName = compressorName = ENCODING_AVC1;
                            bitDepth = 24;
                            break;
                        case ENC_SCREEN_CAPTURE:
                        default:
                            if (bitDepth == 8) {
                                // FIXME - 8-bit Techsmith Screen Capture is broken
                                videoFormatName = ENCODING_QUICKTIME_ANIMATION;
                                compressorName = COMPRESSOR_NAME_QUICKTIME_ANIMATION;
                            } else {
                                videoFormatName = ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE;
                                compressorName = ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE;
                            }
                            break;
                        case ENC_ANIMATION:
                            videoFormatName = ENCODING_QUICKTIME_ANIMATION;
                            compressorName = COMPRESSOR_NAME_QUICKTIME_ANIMATION;
                            break;
                        case ENC_NONE:
                            videoFormatName = ENCODING_QUICKTIME_RAW;
                            compressorName = COMPRESSOR_NAME_QUICKTIME_RAW;
                            break;
                        case ENC_PNG:
                            videoFormatName = ENCODING_QUICKTIME_PNG;
                            compressorName = COMPRESSOR_NAME_QUICKTIME_PNG;
                            bitDepth = 24;
                            break;
                        case ENC_JPEG_100PERCENT:
                            videoFormatName = ENCODING_QUICKTIME_JPEG;
                            compressorName = COMPRESSOR_NAME_QUICKTIME_JPEG;
                            bitDepth = 24;
                            break;
                        case ENC_JPEG_50PERCENT:
                            videoFormatName = ENCODING_QUICKTIME_JPEG;
                            compressorName = COMPRESSOR_NAME_QUICKTIME_JPEG;
                            bitDepth = 24;
                            quality = 0.5f;
                            break;
                    }
                    break;
            }

            Mixer.Info mixerInfo;
            int audioRate;
            int audioBitsPerSample;
            int audioChannels;
            ByteOrder audioByteOrder;
            boolean audioSigned;
            {
                AudioSourceItem src = (AudioSourceItem) audioSourceChoice.getItemAt(this.audioSource);
                if (src != null) {
                    mixerInfo = src.mixerInfo;
                    AudioFormat srcFormat = src.format;
                    audioRate = (int) srcFormat.getSampleRate();
                    if (audioRate <= 0) {
                        audioRate = Integer.parseInt((String) audioRateChoice.getSelectedItem());
                    }
                    audioBitsPerSample = srcFormat.getSampleSizeInBits();
                    audioChannels = srcFormat.getChannels();
                    audioByteOrder = srcFormat.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
                    audioSigned = srcFormat.getEncoding() == AudioFormat.Encoding.PCM_SIGNED;
                } else {
                    audioRate = 0;
                    audioBitsPerSample = 0;
                    audioChannels = 0;
                    audioByteOrder = null;
                    audioSigned = false;
                    mixerInfo = null;
                }
            }

            String crsr;
            switch (cursor) {
                default:
                case 0:
                    crsr = null;
                    break;
                case 1:
                    crsr = MouseFormatKeys.ENCODING_BLACK_CURSOR;
                    break;
                case 2:
                    crsr = MouseFormatKeys.ENCODING_WHITE_CURSOR;
                    break;
            }
            GraphicsConfiguration cfg = getGraphicsConfiguration();
            Rectangle areaRect = null;
            Dimension outputDimension = null;
            AreaItem item = (AreaItem) areaChoice.getItemAt(area);
            areaRect = item.getBounds(cfg);
            outputDimension = item.outputDimension;
            if (outputDimension == null) {
                outputDimension = areaRect.getSize();
            }

            screenRecorder = new SimpleScreenRecorder(new ScreenRecorderConfig(cfg.getDevice(), areaRect,
                    // the file format:
                    new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, mimeType),
                    //
                    // the output format for screen capture:
                    new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, videoFormatName,
                            CompressorNameKey, compressorName,
                            WidthKey, outputDimension.width,
                            HeightKey, outputDimension.height,
                            DepthKey, bitDepth, FrameRateKey, Rational.valueOf(screenRate),
                            QualityKey, quality,
                            KeyFrameIntervalKey, (int) (screenRate * 60) // one keyframe per minute is enough
                    ),
                    //
                    // the output format for mouse capture:
                    crsr == null ? null : new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, crsr,
                            FrameRateKey, Rational.valueOf(mouseRate)),

                    mixerInfo == null ? null : AudioSystem.getMixer(mixerInfo),
                    //
                    // the output format for audio capture:
                    audioRate == 0 ? null : new Format(MediaTypeKey, MediaType.AUDIO,
                            ChannelsKey, audioChannels,
                            SampleRateKey, Rational.valueOf(audioRate),
                            SampleSizeInBitsKey, audioBitsPerSample,
                            SignedKey, audioSigned,
                            ByteOrderKey, audioByteOrder
                    ),
                    //
                    // the storage location of the movie
                    movieFolder));

            startStopButton.setText("Stop");
            screenRecorder.addChangeListener(handler);
            screenRecorder.start();
        }
    }

    public void setSettingsEnabled(boolean b) {
        for (Component c : getContentPane().getComponents()) {
            if (c != startStopButton && c != stateLabel) {
                c.setEnabled(b);
            }
        }
        ((JComponent) getContentPane()).invalidate();
        ((JComponent) getContentPane()).revalidate();
    }

    private void stop() {
        if (screenRecorder != null) {
            final ScreenRecorder r = screenRecorder;
            startStopButton.setEnabled(false);
            stateLabel.setForeground(Color.RED);
            stateLabel.setText("Stopping...");
            screenRecorder = null;
            new BackgroundTask() {
                @Override
                protected void construct() throws Exception {
                    r.stop();
                }

                @Override
                protected void finished() {
                    setSettingsEnabled(true);
                    startStopButton.setEnabled(true);
                    startStopButton.setText("Start");
                    stateLabel.setForeground(Color.RED);
                    stateLabel.setText(" ");
                }
            }.start();
        }
    }

    private void recordingFailed() {
        if (screenRecorder != null) {
            screenRecorder = null;
            startStopButton.setEnabled(true);
            startStopButton.setText("Start");
            setExtendedState(Frame.NORMAL);
            JOptionPane.showMessageDialog(Main.this,
                    "<html><b>Sorry. Screen Recording failed.</b>",
                    "Screen Recorder", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateEncodingChoice() {
        String encodingItem = (String) encodingChoice.getSelectedItem();
        String colorItem = (String) colorsChoice.getSelectedItem();
        switch ((String) formatChoice.getSelectedItem()) {
            case FMT_MP4:
                colorsChoice.setModel(
                        new javax.swing.DefaultComboBoxModel<>(new String[]{COLORS_MILLIONS}));
                encodingChoice.setModel(
                        new javax.swing.DefaultComboBoxModel<>(new String[]{ENC_H264}));
                break;
            case FMT_AVI: // AVI
                colorsChoice.setModel(
                        new javax.swing.DefaultComboBoxModel<>(new String[]{COLORS_HUNDREDS, COLORS_THOUSANDS, COLORS_MILLIONS}));
                encodingChoice.setModel(
                        new javax.swing.DefaultComboBoxModel<>(new String[]{ENC_H264, ENC_SCREEN_CAPTURE, ENC_RUN_LENGTH, ENC_NONE, ENC_PNG, ENC_JPEG_100PERCENT, ENC_JPEG_50PERCENT}));
                break;
            case FMT_QUICKTIME:
                colorsChoice.setModel(
                        new javax.swing.DefaultComboBoxModel<>(new String[]{COLORS_HUNDREDS, COLORS_THOUSANDS, COLORS_MILLIONS}));
                encodingChoice.setModel(
                        new javax.swing.DefaultComboBoxModel<>(new String[]{ENC_H264, ENC_SCREEN_CAPTURE, ENC_ANIMATION, ENC_NONE, ENC_PNG, ENC_JPEG_100PERCENT, ENC_JPEG_50PERCENT}));
                break;
        }
        colorsChoice.setSelectedIndex(findIndex(colorItem, colorsChoice.getModel()));
        encodingChoice.setSelectedIndex(findIndex(encodingItem, encodingChoice.getModel()));
    }

    private void startStopPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startStopPerformed
        if (screenRecorder == null) {
            setExtendedState(Frame.ICONIFIED);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        start();
                    } catch (Throwable t) {
                        t.printStackTrace();
                        setExtendedState(Frame.NORMAL);
                        JOptionPane.showMessageDialog(Main.this,
                                "<html><b>Sorry. Screen Recording failed.</b><br>" + t.getMessage(),
                                "Screen Recorder", JOptionPane.ERROR_MESSAGE);
                        stop();
                    }
                }
            });
        } else {
            stop();
        }
    }//GEN-LAST:event_startStopPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        stop();
        setVisible(false);
        dispose();
    }//GEN-LAST:event_formWindowClosing

    private void formWindowDeiconified(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowDeiconified
        // stop();
    }//GEN-LAST:event_formWindowDeiconified

    private void formatChoicePerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formatChoicePerformed
        updateEncodingChoice();
    }//GEN-LAST:event_formatChoicePerformed

    private void selectAreaPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAreaPerformed
        final JRecordingAreaFrame f = new JRecordingAreaFrame();
        AreaItem ai = (AreaItem) areaChoice.getSelectedItem();
        Rectangle r = ai.getBounds(getGraphicsConfiguration());
        if (r.getWidth() > 16 && r.getHeight() > 16) {
            f.setBounds(r);
        }
        f.updateLabel();
        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                areaChoice.setSelectedIndex(areaChoice.getItemCount() - 1);
                AreaItem ai = (AreaItem) areaChoice.getSelectedItem();
                ai.location = f.getLocation();
                ai.inputDimension = f.getSize();
                ai.alignment = SwingConstants.NORTH_WEST;
                ai.outputDimension = null;
                ai.title = "Custom: " + ai.location.x + ", " + ai.location.y + "; " + ai.inputDimension.width + " x " + ai.inputDimension.height;
                f.setVisible(false);
                f.dispose();
                setVisible(true);
                f.removeWindowListener(this);
                final Preferences prefs = Preferences.userNodeForPackage(Main.class);
                prefs.putInt("ScreenRecording.customAreaX", ai.location.x);
                prefs.putInt("ScreenRecording.customAreaY", ai.location.y);
                prefs.putInt("ScreenRecording.customAreaWidth", ai.inputDimension.width);
                prefs.putInt("ScreenRecording.customAreaHeight", ai.inputDimension.height);
                ((JComponent) getContentPane()).invalidate();
                ((JComponent) getContentPane()).revalidate();
            }
        });
        setVisible(false);
        f.setVisible(true);
    }//GEN-LAST:event_selectAreaPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    //ignore
                }
                new Main().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox areaChoice;
    private javax.swing.JLabel areaLabel;
    private javax.swing.JComboBox<String> audioRateChoice;
    private javax.swing.JComboBox audioSourceChoice;
    private javax.swing.JLabel audioSourceLabel;
    private javax.swing.JComboBox colorsChoice;
    private javax.swing.JLabel colorsLabel;
    private javax.swing.JComboBox cursorChoice;
    private javax.swing.JComboBox encodingChoice;
    private javax.swing.JLabel encodingLabel;
    private javax.swing.JComboBox formatChoice;
    private javax.swing.JLabel formatLabel;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel mouseLabel;
    private javax.swing.JSpinner mouseRateField;
    private javax.swing.JLabel mouseRateLabel;
    private javax.swing.JSpinner screenRateField;
    private javax.swing.JLabel screenRateLabel;
    private javax.swing.JButton selectAreaButton;
    private javax.swing.JButton startStopButton;
    private javax.swing.JLabel stateLabel;
    // End of variables declaration//GEN-END:variables
}
