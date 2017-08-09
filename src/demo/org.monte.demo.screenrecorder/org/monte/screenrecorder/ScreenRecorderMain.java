/* @(#)ScreenRecorderMain.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Under the MIT License.
 */
package org.monte.screenrecorder;

import org.monte.media.screenrecorder.ScreenRecorder;
import org.monte.media.screenrecorder.JRecordingAreaFrame;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import org.monte.media.swing.datatransfer.DropFileTransferHandler;
import org.monte.media.swing.JLabelHyperlinkHandler;
import java.net.URISyntaxException;
import java.net.URI;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Point;
import java.io.File;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JComponent;
import javax.swing.DefaultComboBoxModel;
import org.monte.media.av.Format;
import org.monte.media.swing.Worker;
import org.monte.media.math.Rational;
import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Frame;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import static org.monte.media.av.codec.audio.AudioFormatKeys.*;
import static org.monte.media.av.codec.video.VideoFormatKeys.*;
import static java.lang.Math.*;
import java.nio.ByteOrder;
import java.util.Vector;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import org.monte.media.swing.BackgroundTask;

/**
 * ScreenRecorderMain.
 *
 * @author Werner Randelshofer
 * @version $Id: ScreenRecorderMain.java 364 2016-11-09 19:54:25Z werner $
 */
public class ScreenRecorderMain extends javax.swing.JFrame {

    private final static long serialVersionUID = 1L;

    private class Handler implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            ScreenRecorder r = screenRecorder;
            if (r != null && r.getState() == ScreenRecorder.State.FAILED) {
                recordingFailed();
            }
        }
    }
    private Handler handler = new Handler();
    private ScreenRecorder screenRecorder;
    private int depth;
    private int format;
    private int encoding;
    private int cursor;
    private int audioRate;
    private int audioSource;
    private int area;
    private double screenRate;
    private double mouseRate;
    private File movieFolder;

    private static class AudioRateItem {

        private String title;
        private int sampleRate;
        private int bitsPerSample;

        public AudioRateItem(String title, int sampleRate, int bitsPerSample) {
            this.title = title;
            this.sampleRate = sampleRate;
            this.bitsPerSample = bitsPerSample;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    private static class AudioSourceItem {

        private String title;
        private Mixer.Info mixerInfo;
        private boolean isEnabled;

        public AudioSourceItem(String title, Mixer.Info mixerInfo) {
            this(title, mixerInfo, true);
        }

        public AudioSourceItem(String title, Mixer.Info mixerInfo, boolean isEnabled) {
            this.title = title;
            this.mixerInfo = mixerInfo;
            this.isEnabled = isEnabled;
        }

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
    public ScreenRecorderMain() {
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(12, 20, 20, 20));
        initComponents();

        String version = ScreenRecorderMain.class.getPackage().getImplementationVersion();
        if (version != null) {
            int p = version.indexOf(' ');
            setTitle(getTitle() + " " + version.substring(0, p == -1 ? version.length() : p));
        }

        final Preferences prefs = Preferences.userNodeForPackage(ScreenRecorderMain.class);
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

        depth = min(max(0, prefs.getInt("ScreenRecording.colorDepth", 3)), colorsChoice.getItemCount() - 1);
        colorsChoice.setSelectedIndex(depth);
        format = min(max(0, prefs.getInt("ScreenRecording.format", 0)), formatChoice.getItemCount() - 1);
        formatChoice.setSelectedIndex(format);
        encoding = min(max(0, prefs.getInt("ScreenRecording.encoding", 0)), encodingChoice.getItemCount() - 1);
        encodingChoice.setSelectedIndex(encoding);
        cursor = min(max(0, prefs.getInt("ScreenRecording.cursor", 1)), cursorChoice.getItemCount() - 1);
        cursorChoice.setSelectedIndex(cursor);

        screenRate = prefs.getDouble("ScreenRecording.screenRate", 15);
        SpinnerNumberModel screenRateModel = new SpinnerNumberModel(screenRate, 1, 30, 1);
        screenRateField.setModel(screenRateModel);

        mouseRate = prefs.getDouble("ScreenRecording.mouseRate", 30);
        SpinnerNumberModel mouseRateModel = new SpinnerNumberModel(mouseRate, 1, 30, 1);
        mouseRateField.setModel(mouseRateModel);

        // FIXME - 8-bit recording is currently broken
        audioRateChoice.setModel(new DefaultComboBoxModel<AudioRateItem>(new AudioRateItem[]{
            //new AudioItem("No Audio", 0, 0),
            //new AudioItem("8.000 Hz, 8-bit",8000,8),
            new AudioRateItem("8.000 Hz", 8000, 16),
            //new AudioItem("11.025 Hz, 8-bit",11025,8),
            new AudioRateItem("11.025 Hz", 11025, 16),
            //new AudioItem("22.050 Hz, 8-bit",22050,8),
            new AudioRateItem("22.050 Hz", 22050, 16),
            //new AudioItem("44.100 Hz, 8-bit",44100,8),
            new AudioRateItem("44.100 Hz", 44100, 16),}));
        audioRate = prefs.getInt("ScreenRecording.audioRate", 0);
        audioRateChoice.setSelectedIndex(audioRate);
        audioSourceChoice.setModel(new DefaultComboBoxModel<AudioSourceItem>(getAudioSources()));
        audioSource = prefs.getInt("ScreenRecording.audioSource", 0);
        audioSourceChoice.setSelectedIndex(audioSource);

        Dimension customDim = new Dimension(prefs.getInt("ScreenRecording.customAreaWidth", 1024),
                prefs.getInt("ScreenRecording.customAreaHeight", 768));
        Point customLoc = new Point(
                prefs.getInt("ScreenRecording.customAreaX", 100),
                prefs.getInt("ScreenRecording.customAreaY", 100));
        areaChoice.setModel(new DefaultComboBoxModel<AreaItem>(new AreaItem[]{
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        formatLabel = new javax.swing.JLabel();
        formatChoice = new javax.swing.JComboBox();
        colorsLabel = new javax.swing.JLabel();
        colorsChoice = new javax.swing.JComboBox();
        infoLabel = new javax.swing.JLabel();
        startStopButton = new javax.swing.JButton();
        mouseLabel = new javax.swing.JLabel();
        cursorChoice = new javax.swing.JComboBox();
        audioRateLabel = new javax.swing.JLabel();
        audioRateChoice = new javax.swing.JComboBox();
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

        FormListener formListener = new FormListener();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Monte Screen Recorder");
        setResizable(false);
        addWindowListener(formListener);

        formatLabel.setText("Format:");

        formatChoice.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "AVI", "QuickTime" }));
        formatChoice.addActionListener(formListener);

        colorsLabel.setText("Colors:");

        colorsChoice.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Hundreds", "Thousands", "Millions" }));

        infoLabel.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        infoLabel.setText("<html>The recording will be stored in the folder \"Movies\".<br> Drop a folder on this text to change the storage location.<br> A new file will be created every hour or when the file size limit is reached.<br> <br>This window will be minized before the recording starts.<br> To stop the recording, restore this window and press the Stop button.<br> ");

        startStopButton.setText("Start");
        startStopButton.addActionListener(formListener);

        mouseLabel.setText("Mouse:");

        cursorChoice.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Cursor", "Black Cursor", "White Cursor" }));

        audioRateLabel.setText("Audio Rate:");

        audioRateChoice.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "44.100 kHz" }));

        screenRateLabel.setText("Screen Rate:");

        mouseRateLabel.setText("Mouse Rate:");

        encodingLabel.setText("Encoding:");

        encodingChoice.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Screen Capture", "Run Length", "None", "PNG", "JPEG 100 %", "JPEG   50 %" }));

        areaLabel.setText("Area:");

        areaChoice.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Entire Screen", "0 0,  1024 x 768", " " }));

        selectAreaButton.setText("Custom Area...");
        selectAreaButton.addActionListener(formListener);

        stateLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        stateLabel.setText(" ");

        audioSourceLabel.setText("Audio:");

        audioSourceChoice.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Audio", "44.100 kHz" }));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(stateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(startStopButton)
                        .addGap(10, 10, 10))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(colorsLabel)
                            .addComponent(mouseLabel)
                            .addComponent(formatLabel)
                            .addComponent(areaLabel)
                            .addComponent(audioSourceLabel))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(formatChoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(colorsChoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cursorChoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(audioSourceChoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(screenRateLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(mouseRateLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                                    .addComponent(encodingLabel)
                                    .addComponent(audioRateLabel))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(audioRateChoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(encodingChoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(screenRateField, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(mouseRateField, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(areaChoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(selectAreaButton))))
                    .addComponent(infoLabel))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {audioRateChoice, colorsChoice, cursorChoice, formatChoice});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(formatChoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(formatLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(colorsChoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(colorsLabel))
                        .addGap(11, 11, 11)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cursorChoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(mouseLabel)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(encodingChoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(encodingLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(screenRateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(screenRateLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(mouseRateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(mouseRateLabel))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(audioRateChoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(audioRateLabel)
                    .addComponent(audioSourceLabel)
                    .addComponent(audioSourceChoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(areaChoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(areaLabel)
                    .addComponent(selectAreaButton))
                .addGap(18, 18, 18)
                .addComponent(infoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startStopButton)
                    .addComponent(stateLabel))
                .addContainerGap())
        );

        pack();
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, java.awt.event.WindowListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == formatChoice) {
                ScreenRecorderMain.this.formatChoicePerformed(evt);
            }
            else if (evt.getSource() == startStopButton) {
                ScreenRecorderMain.this.startStopPerformed(evt);
            }
            else if (evt.getSource() == selectAreaButton) {
                ScreenRecorderMain.this.selectAreaPerformed(evt);
            }
        }

        public void windowActivated(java.awt.event.WindowEvent evt) {
        }

        public void windowClosed(java.awt.event.WindowEvent evt) {
        }

        public void windowClosing(java.awt.event.WindowEvent evt) {
            if (evt.getSource() == ScreenRecorderMain.this) {
                ScreenRecorderMain.this.formWindowClosing(evt);
            }
        }

        public void windowDeactivated(java.awt.event.WindowEvent evt) {
        }

        public void windowDeiconified(java.awt.event.WindowEvent evt) {
            if (evt.getSource() == ScreenRecorderMain.this) {
                ScreenRecorderMain.this.formWindowDeiconified(evt);
            }
        }

        public void windowIconified(java.awt.event.WindowEvent evt) {
        }

        public void windowOpened(java.awt.event.WindowEvent evt) {
        }
    }// </editor-fold>//GEN-END:initComponents

    private static Vector<AudioSourceItem> getAudioSources() {
        Vector<AudioSourceItem> l = new Vector<AudioSourceItem>();

        l.add(new AudioSourceItem("None", null, false));
        l.add(new AudioSourceItem("Default Input", null, true));
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        DataLine.Info lineInfo = new DataLine.Info(
                TargetDataLine.class,
                new javax.sound.sampled.AudioFormat(
                        44100.0f,
                        16,
                        2,
                        true,
                        true));

        for (Mixer.Info info : mixers) {
            Mixer mixer = AudioSystem.getMixer(info);
            if (mixer.isLineSupported(lineInfo)) {
                l.add(new AudioSourceItem(info.getName(), info));
            }
        }
        return l;
    }

    private void updateValues() {
        Preferences prefs = Preferences.userNodeForPackage(ScreenRecorderMain.class);
        format = formatChoice.getSelectedIndex();
        prefs.putInt("ScreenRecording.format", format);
        encoding = encodingChoice.getSelectedIndex();
        prefs.putInt("ScreenRecording.encoding", encoding);
        depth = colorsChoice.getSelectedIndex();
        prefs.putInt("ScreenRecording.colorDepth", depth);
        cursor = cursorChoice.getSelectedIndex();
        prefs.putInt("ScreenRecording.cursor", cursor);
        audioRate = audioRateChoice.getSelectedIndex();
        prefs.putInt("ScreenRecording.audioRate", audioRate);
        audioSource = audioSourceChoice.getSelectedIndex();
        prefs.putInt("ScreenRecording.audioSource", audioSource);
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
                case 0:
                    bitDepth = 8;
                    break;
                case 1:
                    bitDepth = 16;
                    break;
                case 2:
                    bitDepth = 24;
                    break;
            }
            switch (format) {
                default:
                case 0:
                    mimeType = MIME_AVI;
                    switch (encoding) {
                        case 0:
                        default:
                            videoFormatName = compressorName = ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE;
                            break;
                        case 1:
                            videoFormatName = compressorName = ENCODING_AVI_RLE8;
                            bitDepth = 8;
                            break;
                        case 2:
                            videoFormatName = compressorName = ENCODING_AVI_DIB;
                            if (bitDepth == 16) {
                                bitDepth = 24;
                            }
                            break;
                        case 3:
                            videoFormatName = compressorName = ENCODING_AVI_PNG;
                            bitDepth = 24;
                            break;
                        case 4:
                            videoFormatName = compressorName = ENCODING_AVI_MJPG;
                            bitDepth = 24;
                            break;
                        case 5:
                            videoFormatName = compressorName = ENCODING_AVI_MJPG;
                            bitDepth = 24;
                            quality = 0.5f;
                            break;
                    }
                    break;
                case 1:
                    mimeType = MIME_QUICKTIME;
                    switch (encoding) {
                        case 0:
                        default:
                            if (bitDepth == 8) {
                                // FIXME - 8-bit Techsmith Screen Capture is broken
                                videoFormatName = ENCODING_QUICKTIME_ANIMATION;
                                compressorName = COMPRESSOR_NAME_QUICKTIME_ANIMATION;
                            } else {
                                videoFormatName = compressorName = ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE;
                                compressorName = ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE;
                            }
                            break;
                        case 1:
                            videoFormatName = ENCODING_QUICKTIME_ANIMATION;
                            compressorName = COMPRESSOR_NAME_QUICKTIME_ANIMATION;
                            break;
                        case 2:
                            videoFormatName = ENCODING_QUICKTIME_RAW;
                            compressorName = COMPRESSOR_NAME_QUICKTIME_RAW;
                            break;
                        case 3:
                            videoFormatName = ENCODING_QUICKTIME_PNG;
                            compressorName = COMPRESSOR_NAME_QUICKTIME_PNG;
                            bitDepth = 24;
                            break;
                        case 4:
                            videoFormatName = ENCODING_QUICKTIME_JPEG;
                            compressorName = COMPRESSOR_NAME_QUICKTIME_JPEG;
                            bitDepth = 24;
                            break;
                        case 5:
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
            {
                AudioSourceItem src = (AudioSourceItem) audioSourceChoice.getItemAt(this.audioSource);
                AudioRateItem rate = (AudioRateItem) audioRateChoice.getItemAt(this.audioRate);
                mixerInfo = src.mixerInfo;
                audioRate = src.isEnabled ? rate.sampleRate : 0;
                audioBitsPerSample = rate.bitsPerSample;
            }

            String crsr;
            switch (cursor) {
                default:
                case 0:
                    crsr = null;
                    break;
                case 1:
                    crsr = ScreenRecorder.ENCODING_BLACK_CURSOR;
                    break;
                case 2:
                    crsr = ScreenRecorder.ENCODING_WHITE_CURSOR;
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

            screenRecorder = new ScreenRecorder(cfg, areaRect,
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
                    //
                    // the output format for audio capture:
                    audioRate == 0 ? null : new Format(MediaTypeKey, MediaType.AUDIO,
                                    //EncodingKey, audioFormatName,
                                    SampleRateKey, Rational.valueOf(audioRate),
                                    SampleSizeInBitsKey, audioBitsPerSample),
                    //
                    // the storage location of the movie
                    movieFolder);

            if (mixerInfo != null) {
                screenRecorder.setAudioMixer(AudioSystem.getMixer(mixerInfo));
            }
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
                    ScreenRecorder.State state = r.getState();
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
            JOptionPane.showMessageDialog(ScreenRecorderMain.this,
                    "<html><b>Sorry. Screen Recording failed.</b>",
                    "Screen Recorder", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateEncodingChoice() {
        int index = encodingChoice.getSelectedIndex();
        switch (formatChoice.getSelectedIndex()) {
            case 0: // AVI
                encodingChoice.setModel(
                        new javax.swing.DefaultComboBoxModel<String>(new String[]{"Screen Capture", "Run Length", "None", "PNG", "JPEG 100 %", "JPEG  50 %"}));
                break;
            case 1: // QuickTime
                encodingChoice.setModel(
                        new javax.swing.DefaultComboBoxModel<String>(new String[]{"Screen Capture", "Animation", "None", "PNG", "JPEG 100 %", "JPEG  50 %"}));
                break;
        }
        encodingChoice.setSelectedIndex(index);
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
                        JOptionPane.showMessageDialog(ScreenRecorderMain.this,
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
                final Preferences prefs = Preferences.userNodeForPackage(ScreenRecorderMain.class);
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
                new ScreenRecorderMain().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox areaChoice;
    private javax.swing.JLabel areaLabel;
    private javax.swing.JComboBox audioRateChoice;
    private javax.swing.JLabel audioRateLabel;
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
