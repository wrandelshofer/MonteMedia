/* @(#)JLabelHyperlinkHandler.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.swing;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import javax.accessibility.AccessibleText;
import javax.swing.JLabel;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;

/**
 * {@code JLabelHyperlinkHandler} makes HTML hyperlinks in a {@code JLabel}
 * clickable.
 * <p>
 * You can add an action listener to this handler to perform the desired action.
 * The {@code command} contains the content of the href attribute in the hyperlink.
 * <p>
 * Example:
 * <pre>
 * File f=new File(System.getProperty("user.home"));
 * JLabel l=new JLabel("&lt;html&gt;Click this &lt;a href="\""+
 *                      f.toURI();+
 *                      "\""&gt;link&lt;/a&gt; to open your home folder.");
 * new JLabelHyperlinkHandler(l, new ActionListener() {
 *      public void ActionPerformed(ActionEvent evt) {
 *              try {
 *                  File f = new File(new URI(e.getActionCommand()));
 *                  Desktop.getDesktop().open(f);
 *              } catch (URISyntaxException ex) {
 *                  ex.printStackTrace();
 *              } catch (IOException ex) {
 *                  ex.printStackTrace();
 *              }
 *      }
 * });
 * </pre>
 * 
 * @author Werner Randelshofer
 * @version $Id: JLabelHyperlinkHandler.java 364 2016-11-09 19:54:25Z werner $
 */
public class JLabelHyperlinkHandler {

    private class Handler implements MouseListener, MouseMotionListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            Point point = e.getPoint();
            AccessibleText at = (AccessibleText) label.getAccessibleContext();
            int pos = at.getIndexAtPoint(point);
            AttributeSet as = at.getCharacterAttribute(pos);
            if (label.isEnabled() && as.getAttribute(HTML.Tag.A) != null) {
                label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                SimpleAttributeSet attr=(SimpleAttributeSet)as.getAttribute(HTML.Tag.A);
                String href=(String)attr.getAttribute(HTML.Attribute.HREF);
                fireActionPerformed(
                new ActionEvent(label,ActionEvent.ACTION_PERFORMED,href));
            } else {
                label.setCursor(Cursor.getDefaultCursor());
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
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
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            Point point = e.getPoint();
            AccessibleText at = (AccessibleText) label.getAccessibleContext();
            int pos = at.getIndexAtPoint(point);
            AttributeSet as = at.getCharacterAttribute(pos);
            if (label.isEnabled() && as.getAttribute(HTML.Tag.A) != null) {
                label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                label.setCursor(Cursor.getDefaultCursor());
            }
        }
    }
    private JLabel label;
    private Handler handler = new Handler();
    private ArrayList<ActionListener> actionListeners = new ArrayList<ActionListener>();

    public JLabelHyperlinkHandler() {
        this(null, null);
    }

    public JLabelHyperlinkHandler(JLabel label, ActionListener l) {
        setLabel(label);
        if (l != null) {
            addActionListener(l);
        }
    }

    public void setLabel(JLabel newValue) {
        if (label != null) {
            label.removeMouseListener(handler);
            label.removeMouseMotionListener(handler);
            label.setCursor(Cursor.getDefaultCursor());
        }
        label = newValue;
        if (label != null) {
            label.addMouseListener(handler);
            label.addMouseMotionListener(handler);
        }
    }

    public JLabel getLabel() {
        return label;
    }

    public void addActionListener(ActionListener l) {
        if (l != null) {
            actionListeners.add(l);
        }
    }

    public void removeActionListener(ActionListener l) {
        if (l != null) {
            actionListeners.remove(l);
        }
    }
    
    private void fireActionPerformed(ActionEvent evt) {
        for (ActionListener l:actionListeners) {
            l.actionPerformed(evt);
        }
    }
}
