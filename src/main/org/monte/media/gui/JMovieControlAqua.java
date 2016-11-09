/* @(#)JMovieControlAqua.java
 * Copyright © 2003-2012 Werner Randelshofer, Switzerland.
 * You may only use this file in compliance with the accompanying license terms. 
 */
package org.monte.media.gui;

import org.monte.media.ColorCyclePlayer;
import org.monte.media.Player;
import org.monte.media.MovieControl;
import org.monte.media.gui.border.*;
import org.monte.media.gui.plaf.CustomButtonUI;
import org.monte.media.image.Images;
import java.awt.*;
import java.beans.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.ResourceBundle;
import javax.swing.border.Border;
import javax.swing.event.*;
import javax.swing.plaf.ButtonUI;

/**
 * The MovieControlAqua contains an audio on/off button, a start/stop button,
 * a slider, a forward and a rewind button, and a color cycling on/off button.
 *
 * @author  Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version $Id: JMovieControlAqua.java 364 2016-11-09 19:54:25Z werner $
 */
public class JMovieControlAqua extends JComponent
        implements MovieControl, ActionListener, ChangeListener, PropertyChangeListener {
    private final static long serialVersionUID = 1L;

    private Player player;
    private JMovieSliderAqua slider;
    private JButton forwardButton, rewindButton;
    private JToggleButton startButton, audioButton, colorCyclingButton;
    private BoundedRangeModel boundedRangeModel;
    private Timer scrollTimer;
    private ScrollHandler scrollHandler;
    private JPanel spacer;

    public JMovieControlAqua() {
        // Set the background color to the border color of the buttons.
        // This way the toolbar won't look too ugly when the buttons
        // are displayed before they have been loaded completely.
        //setBackground(new Color(118, 118, 118));
        setBackground(Color.WHITE);

        Dimension buttonSize = new Dimension(16, 16);
        GridBagLayout gridbag = new GridBagLayout();
        Insets margin = new Insets(0, 0, 0, 0);
        setLayout(gridbag);
        GridBagConstraints c;

        ResourceBundle labels = ResourceBundle.getBundle("org.monte.media.Labels");
        colorCyclingButton = new JToggleButton();
        colorCyclingButton.setToolTipText(labels.getString("colorCycling.toolTipText"));
        colorCyclingButton.addActionListener(this);
        colorCyclingButton.setPreferredSize(buttonSize);
        colorCyclingButton.setMinimumSize(buttonSize);
        colorCyclingButton.setVisible(false);
        colorCyclingButton.setMargin(margin);
        c = new GridBagConstraints();
        //c.gridx = 0;
        //c.gridy = 0;
        gridbag.setConstraints(colorCyclingButton, c);
        add(colorCyclingButton);

        audioButton = new JToggleButton();
        audioButton.setToolTipText(labels.getString("audio.toolTipText"));
        audioButton.addActionListener(this);
        audioButton.setPreferredSize(buttonSize);
        audioButton.setMinimumSize(buttonSize);
        audioButton.setVisible(false);
        audioButton.setMargin(margin);
        c = new GridBagConstraints();
        //c.gridx = 0;
        //c.gridy = 0;
        gridbag.setConstraints(audioButton, c);
        add(audioButton);


        startButton = new JToggleButton();
        startButton.setToolTipText(labels.getString("play.toolTipText"));
        startButton.addActionListener(this);
        startButton.setPreferredSize(buttonSize);
        startButton.setMinimumSize(buttonSize);
        startButton.setMargin(margin);
        c = new GridBagConstraints();
        //c.gridx = 1;
        //c.gridy = 0;
        gridbag.setConstraints(startButton, c);
        add(startButton);

        slider = new JMovieSliderAqua();
        c = new GridBagConstraints();
        //c.gridx = 2;
        //c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        gridbag.setConstraints(slider, c);
        add(slider);

        rewindButton = new JButton();
        rewindButton.setToolTipText(labels.getString("previous.toolTipText"));
        rewindButton.setPreferredSize(buttonSize);
        rewindButton.setMinimumSize(buttonSize);
        rewindButton.setMargin(margin);
        c = new GridBagConstraints();
        //c.gridx = 3;
        //c.gridy = 0;

        gridbag.setConstraints(rewindButton, c);
        add(rewindButton);
        rewindButton.addActionListener(this);

        forwardButton = new JButton();
        forwardButton.setToolTipText(labels.getString("next.toolTipText"));
        buttonSize = new Dimension(17, 16);
        forwardButton.setPreferredSize(buttonSize);
        forwardButton.setMinimumSize(buttonSize);
        forwardButton.setMargin(margin);
        c = new GridBagConstraints();
        //c.gridx = 4;
        //c.gridy = 0;
        gridbag.setConstraints(forwardButton, c);
        add(forwardButton);
        forwardButton.addActionListener(this);

        // The spacer is used when the play controls are hidden
        spacer = new JPanel(new BorderLayout());
        spacer.setVisible(false);
        spacer.setPreferredSize(new Dimension(16, 16));
        spacer.setMinimumSize(new Dimension(16, 16));
        spacer.setOpaque(false);
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        gridbag.setConstraints(spacer, c);
        add(spacer);

        Border border = new BackdropBorder(
                new ButtonStateBorder(
                new ImageBevelBorder(
                Images.createImage(getClass(), "images/Player.border.png"),
                new Insets(1, 1, 1, 1), new Insets(0, 4, 1, 4)),
                new ImageBevelBorder(
                Images.createImage(getClass(), "images/Player.borderP.png"),
                new Insets(1, 1, 1, 1), new Insets(0, 4, 1, 4))));

        Border westBorder = new BackdropBorder(
                new ButtonStateBorder(
                new ImageBevelBorder(
                Images.createImage(getClass(), "images/Player.borderWest.png"),
                new Insets(1, 1, 1, 0), new Insets(0, 4, 1, 4)),
                new ImageBevelBorder(
                Images.createImage(getClass(), "images/Player.borderWestP.png"),
                new Insets(1, 1, 1, 0), new Insets(0, 4, 1, 4))));

        startButton.setBorder(westBorder);
        colorCyclingButton.setBorder(westBorder);
        audioButton.setBorder(westBorder);
        rewindButton.setBorder(westBorder);
        forwardButton.setBorder(border);
        startButton.setUI((ButtonUI) CustomButtonUI.createUI(startButton));
        colorCyclingButton.setUI((ButtonUI) CustomButtonUI.createUI(audioButton));
        audioButton.setUI((ButtonUI) CustomButtonUI.createUI(audioButton));
        rewindButton.setUI((ButtonUI) CustomButtonUI.createUI(rewindButton));
        forwardButton.setUI((ButtonUI) CustomButtonUI.createUI(forwardButton));

        colorCyclingButton.setIcon(new ImageIcon(Images.createImage(getClass(), "images/PlayerStartColorCycling.png")));
        colorCyclingButton.setSelectedIcon(new ImageIcon(Images.createImage(getClass(), "images/PlayerStartColorCycling.png")));
        colorCyclingButton.setDisabledIcon(new ImageIcon(Images.createImage(getClass(), "images/PlayerStartColorCycling.disabled.png")));
        audioButton.setIcon(new ImageIcon(Images.createImage(getClass(), "images/PlayerStartAudio.png")));
        audioButton.setSelectedIcon(new ImageIcon(Images.createImage(getClass(), "images/PlayerStopAudio.png")));
        audioButton.setDisabledIcon(new ImageIcon(Images.createImage(getClass(), "images/PlayerStartAudio.disabled.png")));
        startButton.setIcon(new ImageIcon(Images.createImage(getClass(), "images/PlayerStart.png")));
        startButton.setSelectedIcon(new ImageIcon(Images.createImage(getClass(), "images/PlayerStop.png")));
        startButton.setDisabledIcon(new ImageIcon(Images.createImage(getClass(), "images/PlayerStart.disabled.png")));
        rewindButton.setIcon(new ImageIcon(Images.createImage(getClass(), "images/PlayerBack.png")));
        rewindButton.setDisabledIcon(new ImageIcon(Images.createImage(getClass(), "images/PlayerBack.disabled.png")));
        forwardButton.setIcon(new ImageIcon(Images.createImage(getClass(), "images/PlayerNext.png")));
        forwardButton.setDisabledIcon(new ImageIcon(Images.createImage(getClass(), "images/PlayerNext.disabled.png")));

        // Automatic scrolling
        scrollHandler = new ScrollHandler();
        scrollTimer = new Timer(60, scrollHandler);
        scrollTimer.setInitialDelay(300);  // default InitialDelay?
        forwardButton.addMouseListener(scrollHandler);
        rewindButton.addMouseListener(scrollHandler);
    }

    @Override
    public synchronized void setPlayer(Player player) {
        if (this.player != null) {
            this.player.removeChangeListener(this);
            player.removePropertyChangeListener(this);
        }
        this.player = player;
        //        boundedRangeModel = player == null ? null : player.getBoundedRangeModel();
        boundedRangeModel = player == null ? null : player.getTimeModel();
        slider.setModel(boundedRangeModel);
        if (player != null) {
            if (player.getState() >= Player.REALIZED
                    && boundedRangeModel != null && boundedRangeModel.getMaximum() == 0) {
                setPlayerControlsVisible(false);
            }
            slider.setProgressModel(player.getCachingModel());
            startButton.setSelected(player.isActive());
            player.addChangeListener(this);
            player.addPropertyChangeListener(this);
            audioButton.setVisible(player.isAudioAvailable());
            audioButton.setSelected(player.isAudioEnabled());
            colorCyclingButton.setVisible((player instanceof ColorCyclePlayer) ? ((ColorCyclePlayer) player).isColorCyclingAvailable() : false);
            colorCyclingButton.setSelected((player instanceof ColorCyclePlayer) ? ((ColorCyclePlayer) player).isColorCyclingStarted() : false);
            validate();
            repaint();
            BoundedRangeModel cachingControlModel = slider.getProgressModel();
        }
    }

    public void setPlayerControlsVisible(boolean b) {
        boolean oldValue = forwardButton.isVisible();
        if (oldValue != b) {
            forwardButton.setVisible(b);
            rewindButton.setVisible(b);
            startButton.setVisible(b);
            slider.setVisible(b);
            spacer.setVisible(!b);
            revalidate();
        }
    }

    public void setProgressModel(BoundedRangeModel brm) {
        slider.setProgressModel(brm);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (boundedRangeModel != null) {
            int value = boundedRangeModel.getValue();
            if (source == forwardButton) {
                boundedRangeModel.setValue(value == boundedRangeModel.getMaximum() ? boundedRangeModel.getMinimum() : value + 1);
            } else if (source == rewindButton) {
                boundedRangeModel.setValue(value == boundedRangeModel.getMinimum() ? boundedRangeModel.getMaximum() : value - 1);
            } else if (source == startButton) {
                if (startButton.isSelected() != player.isActive()) {
                    if (startButton.isSelected()) {
                        player.start();
                    } else {
                        player.stop();
                    }
                }
            } else if (source == audioButton) {

                player.setAudioEnabled(audioButton.isSelected());
            } else if (source == colorCyclingButton) {
                if (player instanceof ColorCyclePlayer) {
                    ((ColorCyclePlayer) player).setColorCyclingStarted(colorCyclingButton.isSelected());
                }

            }
        }
    }

    @Override
    public void setEnabled(boolean b) {
        super.setEnabled(b);
        for (Component c : getComponents()) {
            c.setEnabled(b);
        }
    }

    @Override
    public void stateChanged(ChangeEvent event) {
        startButton.setSelected(player.isActive());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        if (name.equals("audioEnabled")) {
            audioButton.setSelected(player.isAudioEnabled());
        } else if (name.equals("audioAvailable")) {
            audioButton.setVisible(player.isAudioAvailable());
            validate();
            repaint();
        } else if (name.equals("colorCyclingStarted")) {
            colorCyclingButton.setSelected((player instanceof ColorCyclePlayer) ? ((ColorCyclePlayer) player).isColorCyclingStarted() : false);
        } else if (name.equals("colorCyclingAvailable")) {
            colorCyclingButton.setVisible((player instanceof ColorCyclePlayer) ? ((ColorCyclePlayer) player).isColorCyclingAvailable() : false);
            validate();
            repaint();
        } else if (name.equals("cached")) {
            setPlayerControlsVisible(player.getTimeModel().getMaximum() > 0);
        }
    }

    @Override
    public Component getComponent() {
        return this;
    }

    /**
     * Listener for scrolling events initiated in the
     * forward and backward buttons.
     */
    protected class ScrollHandler extends MouseAdapter implements ActionListener {

        /** The scroll direction. 1 for forward scrolling, -1 for backward scrolling. */
        private JButton button;

        public ScrollHandler() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (button.isEnabled() && button.getModel().isArmed()) {
                int value = boundedRangeModel.getValue();
                if (button == forwardButton) {
                    boundedRangeModel.setValue(value == boundedRangeModel.getMaximum() ? boundedRangeModel.getMinimum() : value + 1);
                } else {
                    boundedRangeModel.setValue(value == boundedRangeModel.getMinimum() ? boundedRangeModel.getMaximum() : value - 1);
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            scrollTimer.start();
            button = (JButton) e.getSource();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            scrollTimer.stop();
        }
    }
}

