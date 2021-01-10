/* @(#)ButtonStateBorder.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.swing.border;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.border.Border;
/**
 * ButtonStateBorder.
 *
 * @author  Werner Randelshofer
 * @version $Id$
 */
public class ButtonStateBorder implements Border {
    public final static int E = 0;
    public final static int EP = 1;
    public final static int ES = 2;
    public final static int EPS = 3;
    public final static int D = 4;
    public final static int DS = 5;
    public final static int I = 6;
    public final static int IS = 7;
    public final static int DI = 8;
    public final static int DIS = 9;
    public final static int DEFAULT = 10;
    /**
     * Borders
     */
    private Border[] borders;

    /** Holds the icon pictures in a single image. This variable is used only
     *until we create the icons array. Then it is set to null.
     */
    private Image tiledImage;
    /**
     * The number of icons in the tiledImage.
     */
    private int tileCount;
    /**
     * Whether the tiledImage needs to be tiled horizontally or vertically
     * to get the icons out of it.
     */
    private boolean isTiledHorizontaly;

    private Insets borderInsets;
    /** Only used for tiled image. */
    private boolean fill;
    /** Only used for tiled image. */
    private Insets imageInsets;


    /**
     * Creates a new instance.
     * All borders must have the same insets.
     * If a border is null, nothing is drawn for this state.
     */
    public ButtonStateBorder(Border e, Border es) {
        borders = new Border[DEFAULT+1];
        borders[E] = e;
        borders[EP] = es;
        borders[ES] = es;
        borders[EPS] = es;
        borders[D] = e;
        borders[DS] = es;
        borders[I] = e;
        borders[IS] = es;
        borders[DI] = es;
        borders[DIS] = es;
    }
    /**
     * Creates a new instance.
     * All borders must have the same insets.
     * If a border is null, nothing is drawn for this state.
     */
    public ButtonStateBorder(Border e, Border ep, Border es, Border eps,
    Border d, Border ds, Border i, Border is, Border di, Border dis) {
        borders = new Border[DEFAULT+1];
        borders[E] = e;
        borders[EP] = ep;
        borders[ES] = es;
        borders[EPS] = eps;
        borders[D] = d;
        borders[DS] = ds;
        borders[I] = i;
        borders[IS] = is;
        borders[DI] = dis;
        borders[DIS] = dis;
    }
    /**
     * Creates a new instance.
     * All borders must have the same insets.
     */
    public ButtonStateBorder(Border[] borders) {
        this.borders = new Border[DEFAULT+1];
        System.arraycopy(borders, 0, this.borders, 0, Math.min(borders.length, this.borders.length));
    }

    /**
     * Creates a new instance.
     * All borders must have the same dimensions.
     */
    public ButtonStateBorder(Image tiledImage, int tileCount, boolean isTiledHorizontaly,
    Insets imageInsets, Insets borderInsets, boolean fill) {
        this.tiledImage = tiledImage;
        this.tileCount = tileCount;
        this.isTiledHorizontaly = isTiledHorizontaly;
        this.imageInsets = imageInsets;
        this.borderInsets = borderInsets;
        this.fill = fill;
    }

    public void setBorder(int key, Border b) {
        borders[key] = b;
    }


    public Insets getBorderInsets(Component c) {
        if (borderInsets != null) {
            return (Insets) borderInsets.clone();
        } else {
            return (Insets) borders[0].getBorderInsets(c).clone();
        }
    }

    public boolean isBorderOpaque() {
        return borders[0].isBorderOpaque();
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Border border = getBorder(c);
        if (border != null) {
            border.paintBorder(c, g, x, y, width, height);
        }
    }

    protected Border getBorder(Component c) {
        Border border;
        boolean isActive = true; //QuaquaUtilities.isOnActiveWindow(c);

        if (c instanceof AbstractButton) {
            ButtonModel model = ((AbstractButton) c).getModel();
            if (isActive) {
                if (model.isEnabled()) {
                    if (model.isPressed() && model.isArmed()) {
                        if (model.isSelected()) {
                            border = borders[EPS];
                        } else {
                            border = borders[EP];
                        }
                    } else if (model.isSelected()) {
                        border = borders[ES];
                    } else {
                        if (!model.isPressed() &&
                        borders[DEFAULT] != null &&
                        (c instanceof JButton) &&
                        ((JButton) c).isDefaultButton()
                        ) {
                            border = borders[DEFAULT];
                        } else {
                            border = borders[E];
                        }
                    }
                } else {
                    if (model.isSelected()) {
                        border = borders[DS];
                    } else {
                        border = borders[D];
                    }
                }
            } else {
                if (model.isEnabled()) {
                    if (model.isSelected()) {
                        border = borders[IS];
                    } else {
                        border = borders[I];
                    }
                } else {
                    if (model.isSelected()) {
                        border = borders[DIS];
                    } else {
                        border = borders[DI];
                    }
                }
            }
        } else {
            if (isActive) {
                if (c.isEnabled()) {
                    border = borders[E];
                } else {
                    border = borders[D];
                }
            } else {
                if (c.isEnabled()) {
                    border = borders[I];
                } else {
                    border = borders[DI];
                }
            }
        }
        return border;
    }
}