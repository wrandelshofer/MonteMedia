/* @(#)CustomButtonUI.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License. */

package org.monte.media.swing.plaf;

import org.monte.media.swing.border.BackdropBorder;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.plaf.*;

/**
 * CustomButtonUI draws a BackdropBorder in the background of the button.
 * This allows for easy visual customization of buttons.
 * <p>
 * Usage:
 * <pre>
 * JButton b = new JButton();
 * b.setUI((ButtonUI) CustomButtonUI.createUI(b));
 * b.setBorder(new BackdropBorder(....));
 * </pre>
 * 
 * @author Werner Randelshofer
 * @version 2.0 2006-09-24 Rewritten.
 * <br>1.0 2001-10-16 Created.
 */
public class CustomButtonUI
        extends BasicButtonUI
        implements PlafConstants {
    private final static CustomButtonUI imageButtonUI = new CustomButtonUI();
/*
    private boolean defaults_initialized = false;
 */
    /*
    protected Color focusColor;
    protected Color selectColor;
    protected Color disabledTextColor;
    */
    
    // ********************************
    //          Create PLAF
    // ********************************
    public CustomButtonUI() {
    }
    
    
    public static ComponentUI createUI(JComponent c) {
        return new CustomButtonUI();
    }
    
    // ********************************
    //          Install
    // ********************************
    @Override
    public void installDefaults(AbstractButton b) {
        super.installDefaults(b);
        /*
        if(!defaults_initialized) {
            PlafUtils.installBevelBorder(b, getPropertyPrefix() + "border");
         
            focusColor = UIManager.getColor(getPropertyPrefix() + "focus");
            selectColor = UIManager.getColor(getPropertyPrefix() + "select");
            disabledTextColor = UIManager.getColor(getPropertyPrefix() + "disabledText");
         
            LookAndFeel.installColors(b, getPropertyPrefix()+".background", getPropertyPrefix()+".foreground");
         
            defaults_initialized = true;
        }*/
    }
    
    @Override
    public void uninstallDefaults(AbstractButton b) {
        super.uninstallDefaults(b);
        /*
        defaults_initialized = false;
         */
    }
    
    // ********************************
    //         Create Listeners
    // ********************************
    @Override
    protected BasicButtonListener createButtonListener(AbstractButton b) {
        return new ImageButtonListener(b);
    }
    /*
    
    // ********************************
    //         Default Accessors
    // ********************************
    protected Color getSelectColor() {
        return selectColor;
    }
    
    protected Color getDisabledTextColor() {
        return disabledTextColor;
    }
    
    protected Color getFocusColor() {
        return focusColor;
    }*/
    
    // ********************************
    //          Paint Methods
    // ********************************
    @Override
    public void paint(Graphics g, JComponent c) {
        g.setColor(c.getBackground());
        g.fillRect(0, 0, c.getWidth(), c.getHeight());
        ButtonModel m = ((AbstractButton) c).getModel();
        /*
        PlafUtils.paintBevel(c, g, 0, 0, c.getWidth(), c.getHeight(), true /*m.isEnabled()* /, m.isPressed() & m.isArmed(), m.isSelected());
        */
                Border b = c.getBorder();
        if (b instanceof BackdropBorder) {
            ((BackdropBorder) b).getBackgroundBorder().paintBorder(c, g, 0, 0, c.getWidth(), c.getHeight());
        }
        super.paint(g, c);
    }
    @Override
    protected void paintButtonPressed(Graphics g, AbstractButton b) {
        // We don't paint button pressed, because
        // this has already been done by PlafUtils.paintBevel
        // at the beginning of the paint method.
        /*
        if ( b.isContentAreaFilled() ) {
            Dimension size = b.getSize();
            g.setColor(getSelectColor());
            g.fillRect(0, 0, size.width, size.height);
        }
         */
    }
    
    @Override
    protected void paintFocus(Graphics g, AbstractButton b,
            Rectangle viewRect, Rectangle textRect, Rectangle iconRect){
        // We don't paint focus
        /**
         * Rectangle focusRect = new Rectangle();
         * String text = b.getText();
         * boolean isIcon = b.getIcon() != null;
         *
         * // If there is text
         * if ( text != null && !text.equals( "" ) ) {
         * if ( !isIcon ) {
         * focusRect.setBounds( textRect );
         * }
         * else {
         * focusRect.setBounds( iconRect.union( textRect ) );
         * }
         * }
         * // If there is an icon and no text
         * else if ( isIcon ) {
         * focusRect.setBounds( iconRect );
         * }
         *
         * g.setColor(getFocusColor());
         * g.drawRect((focusRect.x-1), (focusRect.y-1),
         * focusRect.width+1, focusRect.height+1);
         */
    }
    
    
    @Override
    protected void paintText(Graphics g, JComponent c, Rectangle textRect, String text) {
        AbstractButton b = (AbstractButton) c;
        ButtonModel model = b.getModel();
        FontMetrics fm = g.getFontMetrics();
        
        /* Draw the Text */
        if(model.isEnabled()) {
            /*** paint the text normally */
            g.setColor(b.getForeground());
            BasicGraphicsUtils.drawString(g,text, model.getMnemonic(),
                    textRect.x,
                    textRect.y + fm.getAscent());
        } else {
            /*** paint the text disabled ***/
       //     g.setColor(getDisabledTextColor());
            g.setColor(b.getForeground().brighter());
            BasicGraphicsUtils.drawString(g,text,model.getMnemonic(),
                    textRect.x, textRect.y + fm.getAscent());
            
        }
    }
    
}

class ImageButtonListener extends BasicButtonListener {
    
    public ImageButtonListener(AbstractButton b) {
        super(b);
    }
    
    @Override
    public void focusGained(FocusEvent e) {
        Component c = (Component)e.getSource();
        c.repaint();
    }
}


