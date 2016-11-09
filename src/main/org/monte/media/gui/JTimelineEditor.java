/* @(#)JTimelineEditor.java
 * Copyright © 2011 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.gui;

import org.monte.media.Movie;
import org.monte.media.math.Rational;
import java.awt.Window;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.Icon;
import org.monte.media.gui.border.ImageBevelBorder;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import org.monte.media.image.Images;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.SwingUtilities;
import static java.lang.Math.*;

/**
 * JTimelineEditor visualizes the movie timeline, an insertion point and
 * the start and end position of a movie clip.
 * <p>
 * The insertion point (playhead) also shows the current time of the movie.
 * <p>
 * If a movie has n time steps, then there are n+1 insertion points.
 * 
 * @author Werner Randelshofer
 * @version 1.0 2011-09-01 Created.
 */
public class JTimelineEditor extends javax.swing.JPanel {
    private final static long serialVersionUID = 1L;

    private Movie movie;
    /** Track number of the time-base track. Specify -1 for disabling the time
     * track. 
     */
    private int timeTrack = 0;
    private Insets trackInsets = new Insets(6, 10, 6, 10);
    private Dimension inSize = new Dimension(9, 6);
    private Dimension outSize = new Dimension(9, 6);
    private Dimension playheadSize = new Dimension(15, 10);

    enum Handle {

        InsertionPoint, SelectionStart, SelectionEnd;
    }
    private Handle focusedHandle = null;

    private class Handler implements MouseListener, MouseMotionListener, KeyListener, PropertyChangeListener, FocusListener {

        private Handle pressedHandle = null;

        @Override
        public void mouseClicked(MouseEvent e) {
            // do nothing?
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (movie == null) {
                return;
            }

            Point p = e.getPoint();
            Rectangle phBounds = getInsertionPointBounds();
            Rectangle inBounds = getSelectionStartBounds();
            Rectangle outBounds = getSelectionEndBounds();
            if (phBounds.contains(p)) {
                pressedHandle = focusedHandle = Handle.InsertionPoint;
            } else if (inBounds.contains(p)) {
                pressedHandle = focusedHandle = Handle.SelectionStart;
                movie.setInsertionPoint(movie.getSelectionStart());
            } else if (outBounds.contains(p)) {
                pressedHandle = focusedHandle = Handle.SelectionEnd;
                movie.setInsertionPoint(movie.getSelectionEnd());
            } else {
                int y = e.getY();
                Rational time;
                if (phBounds.contains(e.getX(), phBounds.y)) {
                    // click occured in vertical area belonging to playhead => snap to playhead
                    time = movie.getInsertionPoint();
                } else {
                    time = posToTime(e.getX());
                }

                movie.setInsertionPoint(time);
                if (phBounds.contains(phBounds.x, y)) {
                    // click occured in horizontal area belonging to playhead => move playhead
                    focusedHandle = pressedHandle = Handle.InsertionPoint;
                } else if (inBounds.contains(inBounds.x, y)) {
                    // click occured in horizontal area belonging to in and out point => move in or out point
                    int splitPos = (outBounds.x - inBounds.x - inBounds.width) / 2 + inBounds.x + inBounds.width;
                    if (e.getX() < splitPos) {
                        movie.setSelectionStart(time);
                        movie.setSelectionEnd(Rational.max(movie.getSelectionStart(), movie.getSelectionEnd()));
                        focusedHandle = pressedHandle = Handle.SelectionStart;
                    } else {
                        movie.setSelectionEnd(time);
                        movie.setSelectionStart(Rational.min(movie.getSelectionStart(), movie.getSelectionEnd()));
                        focusedHandle = pressedHandle = Handle.SelectionEnd;
                    }
                }

            }
            if (focusedHandle != null) {
                requestFocus();
            }
            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            // do nothing?
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            // do nothing?
        }

        @Override
        public void mouseExited(MouseEvent e) {
            // do nothing?
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (movie == null) {
                return;
            }
            if (pressedHandle != null) {
                Rational time = posToTime(e.getX());
                switch (pressedHandle) {
                    case SelectionStart:
                        time = Rational.min(time, movie.getSelectionEnd());
                        movie.setSelectionStart(time);
                        movie.setInsertionPoint(time);
                        break;
                    case SelectionEnd:
                        time = Rational.max(movie.getSelectionStart(), time);
                        movie.setSelectionEnd(time);
                        movie.setInsertionPoint(time);
                        break;
                    case InsertionPoint:
                        movie.setInsertionPoint(time);
                        break;
                }

            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            // do nothing?
        }

        @Override
        public void keyTyped(KeyEvent e) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (movie == null) {
                return;
            }

            if (focusedHandle != null) {
                Rational time;

                switch (focusedHandle) {
                    case SelectionStart:
                        time = movie.getSelectionStart();
                        break;
                    case SelectionEnd:
                        time = movie.getSelectionEnd();
                        break;
                    case InsertionPoint:
                        time = movie.getInsertionPoint();
                        break;
                    default:
                        return;
                }

                long sample = movie.timeToSample(0, time); // FIXME - Must be video track
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    time = movie.sampleToTime(0, sample - 1);
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    time = movie.sampleToTime(0, sample + 1);
                }
                switch (focusedHandle) {
                    case SelectionStart:
                        movie.setSelectionStart(time);
                        movie.setSelectionEnd(Rational.max(movie.getSelectionStart(), movie.getSelectionEnd()));
                        movie.setInsertionPoint(time);
                        break;
                    case SelectionEnd:
                        movie.setSelectionEnd(time);
                        movie.setSelectionStart(Rational.min(movie.getSelectionStart(), movie.getSelectionEnd()));
                        movie.setInsertionPoint(time);
                        break;
                    case InsertionPoint:
                        movie.setInsertionPoint(time);
                        break;
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("style")) {
                updateStyle();
            }
            repaint();
        }

        @Override
        public void focusGained(FocusEvent e) {
            repaint();
        }

        @Override
        public void focusLost(FocusEvent e) {
            repaint();
        }
    }
    private Handler handler = new Handler();

    /** Creates new form JTimelineEditor */
    public JTimelineEditor() {
        initComponents();
        addMouseListener(handler);
        addMouseMotionListener(handler);
        addKeyListener(handler);
        addFocusListener(handler);
        setPreferredSize(new Dimension(200, 22));
        setMinimumSize(new Dimension(100, 22));
        setFocusable(true);
        // putClientProperty("style","textured");
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie newValue) {
        if (this.movie != null) {
            this.movie.removePropertyChangeListener(handler);
        }
        this.movie = newValue;
        if (this.movie != null) {
            this.movie.addPropertyChangeListener(handler);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics gr) {
        Graphics2D g = (Graphics2D) gr;
        super.paintComponent(g);


        boolean isEnabled = isEnabled();
        Window window = SwingUtilities.getWindowAncestor(this);
        boolean isOnActiveWindow = window != null && window.isActive();

        getBackgroundBorder().paintBorder(this, gr, 0, 0, getWidth(), getHeight());


        Rectangle tr = getTrackBounds();
        getTrackBorder(isEnabled && isOnActiveWindow && movie != null).paintBorder(this, gr, tr.x, tr.y, tr.width, tr.height);
        if (movie == null) {
            return;
        }
        int ppos = timeToPos(movie.getInsertionPoint());
        int inpos = timeToPos(movie.getSelectionStart());
        int outpos = timeToPos(movie.getSelectionEnd());
        getThumbBorder(isEnabled && isOnActiveWindow).paintBorder(this, gr, inpos, tr.y, outpos - inpos + 1, tr.height);

        if (isEnabled) {
            boolean isFocused = isFocusOwner();
            getSelectionStartIcon(isEnabled && isFocused && focusedHandle == Handle.SelectionStart).paintIcon(this, gr, inpos, tr.y);
            getSelectionEndIcon(isEnabled && isFocused && focusedHandle == Handle.SelectionEnd).paintIcon(this, gr, outpos, tr.y);
            getInsertionPointIcon(isEnabled && isFocused && focusedHandle == Handle.InsertionPoint).paintIcon(this, gr, ppos, tr.y);
        }
    }

    protected void paintComponentOld(Graphics gr) {
        Graphics2D g = (Graphics2D) gr;
        super.paintComponent(g);
        Rectangle tr = getTrackBounds();
        if (movie != null) {
            int x1 = timeToPos(movie.getSelectionStart());
            int x2 = timeToPos(movie.getSelectionEnd());
            g.setColor(new Color(0xacacac));
            g.fillRect(x1, tr.y + 1, x2 - x1 + 1, tr.height - 1);
        }
        g.setColor(new Color(0x8e8e8e));
        g.drawRect(tr.x, tr.y, tr.width, tr.height);
        if (movie == null) {
            return;
        }

        g.setColor(new Color(0x737373));
        int x = timeToPos(movie.getInsertionPoint());
        g.drawLine(x, tr.y, x, tr.y + tr.height - 1);
        Rectangle r = getInsertionPointBounds();
        Polygon p = new Polygon();
        p.addPoint(r.x, r.y);
        p.addPoint(r.x + r.width - 1, r.y);
        p.addPoint(r.x + r.width / 2, r.y + r.height - 1);
        p.addPoint(r.x, r.y);
        if (focusedHandle == Handle.InsertionPoint && isFocusOwner()) {
            g.setColor(new Color(0x737373));
        } else {
            g.setColor(new Color(0xdddddd));
        }
        g.fill(p);
        g.setColor(new Color(0x737373));
        g.draw(p);

        r = getSelectionStartBounds();
        p = new Polygon();
        p.addPoint(r.x, r.y + r.height - 1);
        p.addPoint(r.x + r.width - 1, r.y + r.height - 1);
        p.addPoint(r.x + r.width - 1, r.y);
        p.addPoint(r.x, r.y + r.height - 1);
        if (focusedHandle == Handle.SelectionStart && isFocusOwner()) {
            g.setColor(new Color(0x737373));
        } else {
            g.setColor(new Color(0xdddddd));
        }
        g.fill(p);
        g.setColor(new Color(0x737373));
        g.draw(p);

        r = getSelectionEndBounds();
        p = new Polygon();
        p.addPoint(r.x, r.y + r.height - 1);
        p.addPoint(r.x + r.width - 1, r.y + r.height - 1);
        p.addPoint(r.x, r.y);
        p.addPoint(r.x, r.y + r.height - 1);
        if (focusedHandle == Handle.SelectionEnd && isFocusOwner()) {
            g.setColor(new Color(0x737373));
        } else {
            g.setColor(new Color(0xdddddd));
        }
        g.fill(p);
        g.setColor(new Color(0x737373));
        g.draw(p);

    }

    protected int timeToPos(Rational time) {
        float fraction = time.divide(movie.getDuration()).floatValue();
        int pos = (int) (fraction * (getWidth() - trackInsets.left - trackInsets.right));
        return pos + trackInsets.left;
    }

    protected Rational posToTime(int pos) {
        Rational fraction = new Rational(pos - trackInsets.left, getWidth() - trackInsets.left - trackInsets.right);
        fraction = Rational.max(new Rational(0, 1), Rational.min(new Rational(1, 1), fraction));

        Rational time = fraction.multiply(movie.getDuration());

        if (timeTrack != -1) {
            long sample = movie.timeToSample(timeTrack, time);
            time = movie.sampleToTime(timeTrack, sample);
        }

        return time;
    }

    protected Rectangle getSelectionStartBounds() {

        int pos = timeToPos(movie.getSelectionStart());
        return new Rectangle(pos - inSize.width, getHeight() - trackInsets.bottom, inSize.width, inSize.height);
    }

    protected Rectangle getSelectionEndBounds() {
        int pos = timeToPos(movie.getSelectionEnd());
        return new Rectangle(pos, getHeight() - trackInsets.bottom, outSize.width, outSize.height);
    }

    protected Rectangle getInsertionPointBounds() {
        int pos = timeToPos(movie.getInsertionPoint());
        return new Rectangle(pos - playheadSize.width / 2, 0, playheadSize.width, playheadSize.height);
    }

    protected Rectangle getTrackBounds() {
        return new Rectangle(trackInsets.left, trackInsets.top, getWidth() - trackInsets.left - trackInsets.right, 10);
    }
    private Border backgroundBorder;

    protected Border getBackgroundBorder() {
        if (backgroundBorder == null) {
            backgroundBorder = readBorders(
                    "images/TimelineEditor.background.png", 1, false, new Insets(3, 3, 3, 3))[0];
        }
        return backgroundBorder;
    }
    private Border[] trackBorders;

    protected Border getTrackBorder(boolean isOnActiveWindow) {
        if (trackBorders == null) {
            trackBorders = readBorders(
                    "images/TimelineEditor.tracks.png", 2, false, new Insets(3, 3, 3, 3));
        }
        return trackBorders[isOnActiveWindow ? 0 : 1];
    }
    private Border[] thumbBorders;

    protected Border getThumbBorder(boolean isOnActiveWindow) {
        if (thumbBorders == null) {
            thumbBorders = readBorders(
                    "images/TimelineEditor.thumbs.png", 2, false, new Insets(3, 3, 3, 3));
        }
        return thumbBorders[isOnActiveWindow ? 0 : 1];
    }
    private Icon[] insertionPointIcon;

    protected Icon getInsertionPointIcon(boolean isFocused) {
        if (insertionPointIcon == null) {
            insertionPointIcon = readIcons("images/TimelineEditor.playHeads.png", 2, false, new Point(-8, -6));
        }
        return insertionPointIcon[isFocused ? 1 : 0];
    }
    private Icon[] selectionStartIcon;

    protected Icon getSelectionStartIcon(boolean isFocused) {
        if (selectionStartIcon == null) {
            selectionStartIcon = readIcons("images/TimelineEditor.inPoints.png", 2, false, new Point(-12, -6));
        }
        return selectionStartIcon[isFocused ? 1 : 0];
    }

    private void updateStyle() {
        trackBorders = null;
        thumbBorders = null;
        insertionPointIcon = null;
        selectionStartIcon = null;
        selectionEndIcon = null;
    }
    private Icon[] selectionEndIcon;

    protected Icon getSelectionEndIcon(boolean isFocused) {
        if (selectionEndIcon == null) {
            selectionEndIcon = readIcons("images/TimelineEditor.outPoints.png", 2, false, new Point(-3, -6));
        }
        return selectionEndIcon[isFocused ? 1 : 0];
    }

    protected Border[] readBorders(String resource, int count, boolean isHorizontal, Insets insets) {
        resource = resource.substring(0, resource.length() - 4) + getStyleSuffix() + ".png";
        try {
            BufferedImage[] imgs = Images.split(ImageIO.read(JTimelineEditor.class.getResource(resource)), count, false);
            Border[] borders = new Border[count];
            for (int i = 0; i < count; i++) {
                borders[i] = new ImageBevelBorder(imgs[i], new Insets(1, 3, 1, 3));
            }
            return borders;
        } catch (Throwable ex) {
            throw new InternalError("JTimelineEditor image not found:" + resource);
        }
    }

    protected Icon[] readIcons(String resource, int count, boolean isHorizontal, final Point offset) {
        resource = resource.substring(0, resource.length() - 4) + getStyleSuffix() + ".png";
        try {
            BufferedImage[] imgs = Images.split(ImageIO.read(JTimelineEditor.class.getResource(resource)), count, false);
            Icon[] icons = new Icon[count];
            for (int i = 0; i < count; i++) {

                icons[i] = new ImageIcon(imgs[i]) {
    private final static long serialVersionUID = 1L;

                    @Override
                    public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
                        super.paintIcon(c, g, x + offset.x, y + offset.y);
                    }
                };
            }
            return icons;
        } catch (Throwable ex) {
            throw new InternalError("JTimelineEditor image not found:" + resource);
        }
    }

    protected String getStyleSuffix() {
        String style = (String) getClientProperty("style");
        return style == null ? "" : "." + style;
    }

    /** Returns the track number used as a time base. If this value is -1,
     * then no track is used as a time base.
     * 
     * @return The track number or -1.
     */
    public int getTimeTrack() {
        return timeTrack;
    }

    /**
     * Sets the track number used as a time base.
     * 
     * @param timeTrack Track number or -1.
     */
    public void setTimeTrack(int timeTrack) {
        this.timeTrack = timeTrack;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.GridLayout(1, 0));
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
