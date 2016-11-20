/* @(#)ScreenRecorderMain.java
 * Copyright © 2011 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.screenrecorder;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import static java.lang.Math.*;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

/**
 * JRecordingAreaFrame.
 *
 * @author Werner Randelshofer
 * @version 1.0 2012-05-03 Created.
 */
public class JRecordingAreaFrame extends javax.swing.JFrame {
    private final static long serialVersionUID = 1L;

    private final static Color backgroundColor = new Color(0x88ffffff, true);
    /** One of -1, SwingConstants.CENTER */
    private int dragWhich = -1;
    private Insets dragInsets = new Insets(10, 10, 10, 10);
    private Dimension minSize = new Dimension(100, 100);

    private class Handler implements MouseListener, MouseMotionListener {

        private Point prevp;
        private int region;

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        private int getRegion(MouseEvent e) {
            Point p = getLocationOnRootPane(e);

            int w = getWidth(), h = getHeight();
            if (p.x < dragInsets.left) {
                if (p.y < dragInsets.top) {
                    return SwingConstants.NORTH_WEST;
                } else if (p.y > h - dragInsets.bottom) {
                    return SwingConstants.SOUTH_WEST;
                } else {
                    return SwingConstants.WEST;
                }
            } else if (p.x > w - dragInsets.right) {
                if (p.y < dragInsets.top) {
                    return SwingConstants.NORTH_EAST;
                } else if (p.y > h - dragInsets.bottom) {
                    return SwingConstants.SOUTH_EAST;
                } else {
                    return SwingConstants.EAST;
                }
            } else if (p.y < dragInsets.top) {
                return SwingConstants.NORTH;
            } else if (p.y > h - dragInsets.bottom) {
                return SwingConstants.SOUTH;
            }
            return SwingConstants.CENTER;
        }

        private Point getLocationOnRootPane(MouseEvent e) {
            Point mp = e.getLocationOnScreen();
            Point rp = getRootPane().getLocationOnScreen();
            mp.x -= rp.x;
            mp.y -= rp.y;
            return mp;

        }

        @Override
        public void mousePressed(MouseEvent e) {
            prevp = getLocationOnRootPane(e);
            prevp = e.getLocationOnScreen();
            region = getRegion(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            Point p = e.getLocationOnScreen();
            Point l = getLocation();
            Dimension s = getSize();
            Point d = new Point(p.x - prevp.x, p.y - prevp.y);


            switch (region) {
                case SwingConstants.NORTH:
                    setLocation(l.x, l.y + d.y);
                    setSize(s.width, max(minSize.height, s.height - d.y));
                    break;
                case SwingConstants.SOUTH:
                    setLocation(l.x, min(l.y + d.y + s.height - minSize.height, l.y));
                    setSize(s.width, max(minSize.height, s.height + d.y));
                    break;
                case SwingConstants.WEST:
                    setLocation(l.x + d.x, l.y);
                    setSize(max(minSize.width, s.width - d.x), s.height);
                    break;
                case SwingConstants.EAST:
                    setLocation(min(l.x + d.x + s.width - minSize.width, l.x), l.y);
                    setSize(max(minSize.width, s.width + d.x), s.height);
                    break;
                case SwingConstants.NORTH_EAST:
                    setLocation(min(l.x + d.x + s.width - minSize.width, l.x), l.y + d.y);
                    setSize(max(minSize.width, s.width + d.x), max(minSize.height, s.height - d.y));
                    break;
                case SwingConstants.SOUTH_EAST:
                    setLocation(min(l.x + d.x + s.width - minSize.width, l.x), min(l.y + d.y + s.height - minSize.height, l.y));
                    setSize(max(minSize.width, s.width + d.x), max(minSize.height, s.height + d.y));
                    break;
                case SwingConstants.NORTH_WEST:
                    setLocation(l.x + d.x, l.y + d.y);
                    setSize(max(minSize.width, s.width - d.x), max(minSize.height, s.height - d.y));
                    break;
                case SwingConstants.SOUTH_WEST:
                    setLocation(l.x + d.x, min(l.y + d.y + s.height - minSize.height, l.y));
                    setSize(max(minSize.width, s.width - d.x), max(minSize.height, s.height + d.y));
                    break;
                case SwingConstants.CENTER:
                    setLocation(l.x + d.x, l.y + d.y);
                    break;
                default:
                    break;
            }
            prevp = p;


            updateLabel();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            int r = getRegion(e);
            switch (r) {
                case SwingConstants.NORTH:
                    setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                    break;
                case SwingConstants.SOUTH:
                    setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                    break;
                case SwingConstants.WEST:
                    setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
                    break;
                case SwingConstants.EAST:
                    setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                    break;
                case SwingConstants.NORTH_EAST:
                    setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
                    break;
                case SwingConstants.SOUTH_EAST:
                    setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                    break;
                case SwingConstants.NORTH_WEST:
                    setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
                    break;
                case SwingConstants.SOUTH_WEST:
                    setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
                    break;
                case SwingConstants.CENTER:
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    break;
                default:
                    setCursor(Cursor.getDefaultCursor());
            }
        }
    }
    private Handler handler = new Handler();

    private static class JContentPane extends JPanel {
    private final static long serialVersionUID = 1L;

        @Override
        protected void paintComponent(Graphics gr) {
            int w = getWidth(), h = getHeight();
            Graphics2D g = (Graphics2D) gr;
            g.setComposite(AlphaComposite.Src);
            g.setColor(backgroundColor);
            g.fillRect(0, 0, w, h);
            /*
            g.setColor(Color.WHITE);
            g.drawRect(0, 0, w - 1, h - 1);
            g.drawRect(4, 4, w - 9, h - 9);*/
            g.setColor(Color.BLACK);
            g.drawRect(1, 1, w - 3, h - 3);
            g.drawRect(2, 2, w - 5, h - 5);
            g.drawRect(3, 3, w - 7, h - 7);

            g.setColor(Color.WHITE);
            float dash_phase = (System.currentTimeMillis() % 1000) / 50;
            BasicStroke s = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[]{5f, 5f}, dash_phase);
            g.setStroke(s);
            g.drawRect(2, 2, w - 5, h - 5);
            repaint(100);
        }
    }

    private static class EraseBorder implements Border {

        private Insets insets;

        public EraseBorder(Insets insets) {
            this.insets = insets;
        }

        @Override
        public void paintBorder(Component c, Graphics gr, int x, int y, int width, int height) {
            Graphics2D g = (Graphics2D) gr;
            g.setComposite(AlphaComposite.Src);
            g.setColor(new Color(0x99000000, true));
            g.fillRect(x, y, width, height);
            g.setComposite(AlphaComposite.SrcOver);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return (Insets) insets.clone();
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }

    /** Creates new form JRecordingAreaFrame */
    public JRecordingAreaFrame() {
        // The following two lines must be executed before the window
        // heavyweight component is created.
        setAlwaysOnTop(true);
        setUndecorated(true);
        getRootPane().putClientProperty("apple.awt.draggableWindowBackground", Boolean.FALSE);

        setBackground(backgroundColor);
        JContentPane cp = new JContentPane();
        setContentPane(cp);
        getRootPane().setOpaque(true);
        cp.setOpaque(false);

        initComponents();


        infoLabel.addMouseListener(handler);
        infoLabel.addMouseMotionListener(handler);
        cp.addMouseListener(handler);
        cp.addMouseMotionListener(handler);

        //infoLabel.setBackground(new Color(0, true));
        infoLabel.setOpaque(false);
        infoPanel.setBorder(new EraseBorder(new Insets(8, 10, 8, 10)));
        //infoLabel.setBackground(new Color(0x88000000, true));
        infoLabel.setForeground(new Color(0xffffff));
        closeButton.setOpaque(false);

        setSize(600, 400);
        updateLabel();
    }

    public void updateLabel() {
        Rectangle r = getBounds();
        //r=r.intersection(getGraphicsConfiguration().getBounds());
        infoLabel.setText("Recording Area: " + r.x + ", " + r.y + "; " + r.width + " x " + r.height);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        infoPanel = new javax.swing.JPanel();
        infoLabel = new javax.swing.JLabel();
        closeButton = new javax.swing.JButton();

        FormListener formListener = new FormListener();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        infoPanel.setLayout(new java.awt.GridBagLayout());

        infoLabel.setText("Recording Area: 0,0,640,480");
        infoPanel.add(infoLabel, new java.awt.GridBagConstraints());

        getContentPane().add(infoPanel, new java.awt.GridBagConstraints());

        closeButton.setText("Close");
        closeButton.addActionListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        getContentPane().add(closeButton, gridBagConstraints);

        pack();
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == closeButton) {
                JRecordingAreaFrame.this.closeButtonPerformed(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonPerformed

        dispose();     }//GEN-LAST:event_closeButtonPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new JRecordingAreaFrame().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JPanel infoPanel;
    // End of variables declaration//GEN-END:variables
}
