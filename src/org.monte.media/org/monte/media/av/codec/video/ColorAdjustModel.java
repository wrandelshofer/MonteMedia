/* @(#)ImageAdjustmentModel.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av.codec.video;

import java.beans.PropertyChangeListener;

/**
 * {@code ImageAdjustmentModel}.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public interface ColorAdjustModel {
    /** Level adjustment: white point in the range [0,1]. 
     */
    public final static String WHITE_POINT_PROPERTY = "whitePoint";
    /** Level adjustment: black point in the range [0,1]. 
     */
    public final static String BLACK_POINT_PROPERTY = "blackPoint";
    /** Level adjustment: mid point in the range [0,1]. 
     */
    public final static String MID_POINT_PROPERTY = "midPoint";
    
    /** Exposure adjustment in the range [-1,1].
     */
    public final static String EXPOSURE_PROPERTY = "exposure";
    /** Brightness adjustment in the range [-1,1].
     */
    public final static String BRIGHTNESS_PROPERTY = "brightness";
    /** Contrast adjustment in the range [-1,1].
     */
    public final static String CONTRAST_PROPERTY = "contrast";
    /** Saturation adjustment in the range [0,1].
     */
    public final static String SATURATION_PROPERTY = "saturation";
    
    /** Definition adjustment in the range [0,1].
     */
    public final static String DEFINITION_PROPERTY = "definition";
    /** Higlights adjustment in the range [0,1].
     */
    public final static String HIGHLIGHTS_PROPERTY = "highlights";
    /** Shadows adjustment in the range [0,1].
     */
    public final static String SHADOWS_PROPERTY = "shadows";
    
    /** Sharpness adjustment in the range [0,1].
     */
    public final static String SHARPNESS_PROPERTY = "sharpness";
    /** De-noise adjustment in the range [0,1].
     */
    public final static String DENOISE_PROPERTY = "denoise";
    
    /** Temperature adjustment in the range [-1,1].
     */
    public final static String TEMPERATURE_PROPERTY = "temperature";
    /** Tint adjustment in the range [-1,1].
     */
    public final static String TINT_PROPERTY = "tint";
    
    /** A 4-vector with the parameters for quadratic white balance adjustment.
     * The vector contains the values {Rmu, Rnu, Bmu, Bnu}.
     * 
     * <pre>
     *                                     [ R
     *                                       G
     *                                       B
     * [R'    [ Rnu  0   0  Rmu  0   0       R^2
     *  G'  =    0   1   0   0   0   0    *  G^2
     *  B']      0   0  Bnu  0   0  Bmu ]    B^2 ]
     * </pre>
     * 
     * <p>
     * Reference:<br>
     * Edmund Lam, Combining gray world and retinex theory for automatic 
     * white balance in	digital photography, Consumer Electronics, 2005. 
     * (ISCE 2005). Proceedings of the Ninth International Symposium on (2005), 
     * pp.134–139.
     * 
     */
    public final static String WHITE_BALANCE_QM_PROPERTY = "whiteBalanceQM";
    public final static String WHITE_BALANCE_QM_ENABLED_PROPERTY = "whiteBalanceQMEnabled";
    public final static String WHITE_BALANCE_TT_ENABLED_PROPERTY = "whiteBalanceTTEnabled";

    public float getWhitePoint();

    public void setWhitePoint(float newValue);

    public float getBlackPoint();

    public void setBlackPoint(float newValue);

    public float getMidPoint();

    public void setMidPoint(float newValue);

    public float getContrast();

    public void setContrast(float newValue);

    public float getDefinition();

    public void setDefinition(float newValue);

    public float getDenoise();

    public void setDenoise(float newValue);

    public float getBrightness();

    public void setBrightness(float newValue);
    
    public float getExposure();

    public void setExposure(float newValue);

    public float getHighlights();

    public void setHighlights(float newValue);

    public float getSaturation();

    public void setSaturation(float newValue);

    public float getShadows();

    public void setShadows(float newValue);

    public float getSharpness();

    public void setSharpness(float newValue);

    public float getTemperature();

    public void setTemperature(float newValue);

    public float getTint();

    public void setTint(float newValue);
    
    public void setWhiteBalanceTTEnabled(boolean newValue);
    public boolean isWhiteBalanceTTEnabled();
    public void setWhiteBalanceQMEnabled(boolean newValue);
    public boolean isWhiteBalanceQMEnabled();
    public void setWhiteBalanceQM(float[] newValue);
    public float[] getWhiteBalanceQM();
    
    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void removePropertyChangeListener(PropertyChangeListener listener);

    /** Resets all values. */
    public void reset();
    
    public void setTo(ColorAdjustModel that);
    
    /** Returns true, if the model does not perform any changes to the image. */
    public boolean isIdentity();
}
