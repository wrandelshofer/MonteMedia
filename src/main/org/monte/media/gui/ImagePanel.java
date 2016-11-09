/* @(#)ImagePanel.java
 * Copyright © 1999-2009 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.gui;

import java.awt.*;
import java.awt.image.*;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

/**
 * Displays images on a panel.
 *
 * @author  Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland.
 * @version 2009-12-25 Display multi-line messages.
 * <br>2009-12-24 Request focus on mouse click.
 * <br>2006-10-01 Fixed message displaying.
 * <br>2006-07-23 Support for 16:9 anamorph pixel aspect added. 
 * <br>1.3.1 2005-01-22 Changing the pixel aspect triggers validation of
 * the parent component.
 * <br>1.3 2004-12-25 Access methods for rendering hints added. Method
 * setTexture added. Underline suffixes from instance variables removed.
 * <br>1.2 2002-04-02 Workaround for MRJ 1.3.1 Update 1 on Mac OS X.
 * <br>1.1 2000-09-28 Does not update scale factor property during paints anymore.
 * <br>1.0  1999-10-19
 */
public class ImagePanel
        extends JComponent {
    private final static long serialVersionUID = 1L;

    private final static RenderingHints RENDER_SPEED;

    static {
        RenderingHints rh = new RenderingHints(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        rh.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        rh.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        rh.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        rh.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        rh.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        rh.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        rh.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        RENDER_SPEED = rh;
    }
    /**
     * Pixel aspect policy: Ignore pixel aspect.
     */
    public final static int IGNORE_PIXEL_ASPECT = 0;
    /**
     * Pixel aspect policy: Preserves only extreme pixel aspects,
     * where one dimension is a multiple of the other dimension.
     */
    public final static int ROUNDED_PIXEL_ASPECT = 1;
    /**
     * Pixel aspect policy: Preserve the exact pixel aspect.
     */
    public final static int EXACT_PIXEL_ASPECT = 2;
    /**
     * Pixel aspect policy: Use Anamoprh 16:9 pixel aspect (=16/9*4/5=1.4222).
     */
    public final static int ANAMORPH_PIXEL_ASPECT = 3;
    /**
     * Image scaling policy: Scale as defined by #setAspectRatio
     * and the image aspect.
     */
    public final static int SCALE_TO_IMAGE_SIZE = 0;
    /**
     * Image scaling policy: Scale to panel size.
     */
    public final static int SCALE_TO_VIEW_SIZE = 1;
    /**
     * Image scaling policy: Scale to panel but keep the
     * image aspect.
     */
    public final static int SCALE_TO_IMAGE_ASPECT = 2;
    private Image image;
    private double scaleFactor = 1d;
    private double aspectRatioX = 1d;
    private double aspectRatioY = 1d;
    private int imageScalePolicy = SCALE_TO_VIEW_SIZE;
    private int pixelAspectPolicy = EXACT_PIXEL_ASPECT;
    private String message;
    private BufferedImage texture;
    /** Support for property change listeners. */
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private RenderingHints renderingHints = RENDER_SPEED;

    public ImagePanel() {
        super();
    }

    @Override
    public void paintComponent(Graphics gr) {
        Graphics2D g = (Graphics2D) gr;
        Dimension size = getSize();
        Rectangle clipRect = g.getClipBounds();
        g.setRenderingHints(renderingHints);
        g.setColor(getBackground());
        g.fillRect(clipRect.x, clipRect.y, clipRect.width, clipRect.height);
        if (image == null || image.getWidth(this) == -1) {
        } else {
            final int iw = image.getWidth(this);
            final int ih = image.getHeight(this);
            double xAspect = getPixelAspectX();
            double yAspect = getPixelAspectY();

            int w, h, x, y;

            switch (imageScalePolicy) {
                case SCALE_TO_VIEW_SIZE: {
                    double factor = Math.min(size.width / (double) iw, size.height / (double) ih);

                    x = 0;
                    y = 0;
                    w = size.width;
                    h = size.height;

                    break;
                }

                case SCALE_TO_IMAGE_ASPECT: {
                    double factor = Math.min(size.width / (iw * xAspect), size.height / (ih * yAspect));
                    setScaleFactor(factor, false);
                    w = (int) Math.ceil(iw * xAspect * factor);
                    h = (int) Math.ceil(ih * yAspect * factor);
                    x = (size.width - w) / 2;
                    y = (size.height - h) / 2;

                    if (size.width > w) {
                        g.setColor(getForeground());
                        g.drawLine(x - 1, 0, x - 1, size.height - 1);
                        g.drawLine(x + w, 0, x + w, size.height - 1);
                        //  g.setColor(getBackground());
                        //  g.fillRect(0, 0, x-1, size.height);
                        //  g.fillRect(x+w+1, 0, size.width, size.height);
                    } else if (size.height > h) {
                        g.setColor(getForeground());
                        g.drawLine(0, y - 1, size.width - 1, y - 1);
                        g.drawLine(0, y + h, size.width - 1, y + h);
                        //  g.setColor(getBackground());
                        //  g.fillRect(0, 0, size.width, y-1);
                        //  g.fillRect(0,y+h+1, size.width, size.height);
                    }
                    break;
                }

                case SCALE_TO_IMAGE_SIZE:
                default: {
                    w = (int) (iw * xAspect * getScaleFactor());
                    h = (int) (ih * yAspect * getScaleFactor());
                    x = (size.width - w) / 2;
                    y = (size.height - h) / 2;

                    g.setColor(getForeground());
                    g.drawRect(x - 1, y - 1, w + 1, h + 1);
                    break;
                }
            }

            if (texture != null) {
                g.setPaint(
                        new TexturePaint(
                        texture,
                        new Rectangle(x, y, texture.getWidth(), texture.getHeight())));
                g.fillRect(x, y, w, h);

                g.drawImage(
                        image,
                        x, y, w, h,
                        this);
            } else {
                g.drawImage(
                        image,
                        x, y, w, h,
                        getBackground(),
                        this);
            }
        }
        if (message != null) {
            int y = g.getFontMetrics().getMaxAscent();
            for (String msg : message.split("\n")) {
                g.setColor(getBackground());
                g.drawString(msg, 1, y + 1);
                g.drawString(msg, 1, y - 1);
                g.drawString(msg, 0, y);
                g.drawString(msg, 2, y);
                g.setColor(getForeground());
                g.drawString(msg, 1, y);
                y += g.getFontMetrics().getMaxAscent();
            }
        }
    }

    /*
    private void drawCheckerboard(Graphics g, Rectangle r) {
    g.setColor(Color.white);
    g.fillRect(r.x,r.y,r.width,r.height);

    final int squareSize = 8;
    g.setColor(Color.lightGray);
    // Draw from the left to the right.
    int shift = 0;
    for(int x = 0; x < r.width; x+=squareSize) {
    for(int y = shift*squareSize; y < r.height; y+=squareSize*2) {
    g.fillRect(r.x+x,r.y+y,squareSize,squareSize);
    }
    shift = shift == 0 ? 1 : 0;
    }
    }
     */
    /**
     * Sets the pixel aspect policy.
     *
     * @param  policy  PixelAspectPolicy must be one of IGNORE_PIXEL_ASPECT,
     *         ROUNDED_PIXEL_ASPECT, EXACT_PIXEL_ASPECT.
     *
     * @exception  IllegalArgumentException  When passing invalid policy.
     */
    public synchronized void setPixelAspectPolicy(int policy) {
        int old = pixelAspectPolicy;
        if (old != policy) {
            if (policy != IGNORE_PIXEL_ASPECT
                    && policy != ROUNDED_PIXEL_ASPECT
                    && policy != EXACT_PIXEL_ASPECT) {
                throw new IllegalArgumentException("Invalid policy:" + policy);
            }

            pixelAspectPolicy = policy;
            propertyChangeSupport.firePropertyChange("pixelAspectPolicy", new Integer(old), new Integer(policy));

            invalidate();
            Component parent = getParent();
            if (parent != null) {
                parent.validate();
            }
            repaint();
        }
    }

    /**
     * Returns the pixel aspect policy.
     */
    public int getPixelAspectPolicy() {
        return pixelAspectPolicy;
    }

    /**
     * Sets the image scale policy.
     */
    public synchronized void setImageScalePolicy(int policy) {
        int old = imageScalePolicy;
        if (old != policy) {
            if (policy != SCALE_TO_IMAGE_SIZE
                    && policy != SCALE_TO_VIEW_SIZE
                    && policy != SCALE_TO_IMAGE_ASPECT) {
                throw new IllegalArgumentException("Invalid policy:" + policy);
            }
            imageScalePolicy = policy;
            propertyChangeSupport.firePropertyChange("imageScalePolicy", new Integer(old), new Integer(policy));
            invalidate();
            Component parent = getParent();
            if (parent != null) {
                parent.validate();
            }
            repaint();
        }
    }

    /**
     * Returns the image scale policy.
     */
    public int getImageScalePolicy() {
        return imageScalePolicy;
    }

    /**
     * Gets the horizontal pixel aspect of the image according to the
     * pixel aspect policy that is in affect.
     *
     * @return  Horizontal pixel aspect.
     */
    public double getPixelAspectX() {
        if (image == null) {
            return 0;
        }

        Object property = image.getProperty("aspect", this);
        if (property == null) {
            return 1;
        }
        double ratio = (property == null || property == Image.UndefinedProperty) ? 1d : ((Double) property).doubleValue();

        switch (pixelAspectPolicy) {
            case IGNORE_PIXEL_ASPECT:
                return 1d;
            case EXACT_PIXEL_ASPECT:
                return ratio >= 1d ? ratio : 1d;
            case ROUNDED_PIXEL_ASPECT:
                return ratio >= 1d ? Math.floor(ratio + 0.5d) : 1d;
            default:
                throw new InternalError("Invalid pixel aspect policy: " + pixelAspectPolicy);
        }
    }

    /**
     * Gets the vertical pixel aspect of the image according to the
     * pixel aspect policy that is in effect.
     *
     * @return  Vertical pixel aspect.
     */
    public double getPixelAspectY() {
        if (image == null) {
            return 0;
        }

        Object property = image.getProperty("aspect", this);
        if (property == null) {
            return 1;
        }
        double ratio = (property == null || property == Image.UndefinedProperty) ? 1d : ((Double) property).doubleValue();

        switch (pixelAspectPolicy) {
            case IGNORE_PIXEL_ASPECT:
                return 1d;
            case EXACT_PIXEL_ASPECT:
                return ratio < 1d ? 1d / ratio : 1d;
            case ROUNDED_PIXEL_ASPECT:
                return ratio < 1d ? Math.floor(1d / ratio + 0.5d) : 1d;
            default:
                throw new InternalError("Invalid pixel aspect policy: " + pixelAspectPolicy);
        }
    }

    /**
     * Gets the preferred image size.
     *
     * @return  Image dimension after applying the
     *  pixel aspect policy.
     */
    public Dimension getPreferredImageSize() {
        if (image == null) {
            return new Dimension(0, 0);
        }
        if (image.getWidth(this) == -1) {
            try {
                MediaTracker tracker = new MediaTracker(this);
                tracker.addImage(image, 0);
                tracker.waitForID(0);
            } catch (InterruptedException e) {
            }
        }
        return new Dimension(
                (int) Math.ceil(image.getWidth(this) * getPixelAspectX()),
                (int) Math.ceil(image.getHeight(this) * getPixelAspectY()));
    }

    /**
     * Gets the scaled and pixel aspect corrected image size.
     *
     * @return  Image dimension after scaling and applying the
     *   pixel aspect policy.
     */
    public Dimension getScaledImageSize() {
        if (image == null) {
            return new Dimension(0, 0);
        }
        if (image.getWidth(this) == -1) {
            try {
                MediaTracker tracker = new MediaTracker(this);
                tracker.addImage(image, 0);
                tracker.waitForID(0);
            } catch (InterruptedException e) {
            }
        }
        return new Dimension(
                (int) Math.ceil(image.getWidth(this) * getPixelAspectX() * getAspectRatioX() * getScaleFactor()),
                (int) Math.ceil(image.getHeight(this) * getPixelAspectY() * getAspectRatioY() * getScaleFactor()));
    }

    /**
     * Sets the image and displays it in this
     * image panel.
     */
    public void setImage(Image image) {
        Image old = this.image;
        this.image = image;
        invalidate();
        Component parent = getParent();
        if (parent != null) {
            parent.validate();
        }
        repaint();
        propertyChangeSupport.firePropertyChange("image", old, image);
    }

    /**
     * Gets the image that is displayed in this
     * image panel.
     *
     * @return image.
     */
    public Image getImage() {
        return image;
    }

    /**
     * Sets the texture for the backdrop paint.
     */
    public void setTexture(BufferedImage newValue) {
        BufferedImage old = this.texture;
        this.texture = newValue;
        propertyChangeSupport.firePropertyChange("texture", old, newValue);
    }

    /**
     * Gets the backdrop paint.
     */
    public BufferedImage getTexture() {
        return texture;
    }

    /**
     * Sets the scale factor.
     * The scale factor scales images shown in
     * this image panel.
     *
     * @param  scaleFactor    The scale factor.
     */
    public void setScaleFactor(double scaleFactor) {
        setScaleFactor(scaleFactor, true);
    }

    private synchronized void setScaleFactor(double scaleFactor, boolean repaint) {
        double old = this.scaleFactor;
        this.scaleFactor = scaleFactor;
        if (scaleFactor != old) {
            propertyChangeSupport.firePropertyChange("scaleFactor", new Double(old), new Double(scaleFactor));
            if (repaint) {
                Component parent = getParent();
                if (parent != null) {
                    parent.invalidate();
                    parent.validate();
                }
                repaint();
            }
        }
    }

    /**
     * Gets the scale factor.
     *
     * @return  Scale factor.
     */
    public synchronized double getScaleFactor() {
        return scaleFactor;
    }

    /**
     * Sets the rendering hints.
     *
     * @param  newValue    The new rendering hints.
     */
    public void setRenderingHints(RenderingHints newValue) {
        setRenderingHints(newValue, true);
    }

    private synchronized void setRenderingHints(RenderingHints newValue, boolean repaint) {
        RenderingHints old = this.renderingHints;
        this.renderingHints = newValue;
        if (newValue != old) {
            propertyChangeSupport.firePropertyChange("renderingHints", old, newValue);
            if (repaint) {
                Component parent = getParent();
                if (parent != null) {
                    parent.invalidate();
                    parent.validate();
                }
                repaint();
            }
        }
    }

    /**
     * Gets the rendering hints.
     *
     * @return  Rendering Hints.
     */
    public synchronized RenderingHints getRenderingHints() {
        return renderingHints;
    }

    /**
     * Sets the aspect ratio.
     * The aspect ratio distorts images shown in  this image panel.
     *
     * @param  ratioX  Horizontal scale factor.
     * @param  ratioY  Vertical scale factor.
     */
    public void setAspectRatio(double ratioX, double ratioY) {
        if (aspectRatioX != ratioX || aspectRatioY != ratioY) {
            setAspectRatio0(ratioX, ratioY);
            Component parent = getParent();
            if (parent != null) {
                parent.invalidate();
                parent.validate();
            }
            repaint();
        }
    }

    protected void setAspectRatio0(double ratioX, double ratioY) {
        double oldX = aspectRatioX;
        double oldY = aspectRatioY;
        aspectRatioX = ratioX;
        aspectRatioY = ratioY;
        propertyChangeSupport.firePropertyChange("aspectRatioX", new Double(oldX), new Double(ratioX));
        propertyChangeSupport.firePropertyChange("aspectRatioY", new Double(oldY), new Double(ratioY));
    }

    /**
     * Gets the horizontal scale factor that is
     * used to scale images shown in this image panel.
     *
     * @return  Horizontal scale factor.
     */
    public double getAspectRatioX() {
        return aspectRatioX;
    }

    /**
     * Gets the vertical scale factor that is
     * used to scale images shown in this image panel.
     *
     * @return  Vertical scale factor.
     */
    public double getAspectRatioY() {
        return aspectRatioY;
    }

    /**
     * Gets the preferred size of this image panel.
     * The preferred size depends on the image size,
     * the scale factor and the pixel aspect policy.
     */
    @Override
    public Dimension getPreferredSize() {
        if (image == null) {
            return new Dimension(50, 15);
        } else {
            return getScaledImageSize();
        }
    }

    /**
     * Adds a listener who is interested in changes of this object.
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Removes a previously registered listener.
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void setMessage(String message) {
        String old = this.message;
        // if ( ((old == null || message == null) && old != message)
        // || (old != null && old.equals(this.message) == false) ) {
        this.message = message;
        propertyChangeSupport.firePropertyChange("message", old, message);
        repaint();
        // }
    }

    /**
     * XXX Netscape gets very slow when
     * painting all SOMEBITS of an image.
     */
    @Override
    public boolean imageUpdate(Image img, int flags,
            int x, int y, int w, int h) {
        if (flags == SOMEBITS) {
            // suppress painting of SOMEBITS.
            return true;
        } else {
            return super.imageUpdate(img, flags, x, y, w, h);
        }
    }
}
