/* @(#)DefaultImageAdjustmentModel.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av.codec.video;

import org.monte.media.beans.AbstractBean;

/**
 * {@code DefaultImageAdjustmentModel}.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class DefaultColorAdjustModel
        extends AbstractBean implements ColorAdjustModel {
    private final static long serialVersionUID = 1L;

    /** Level adjustment: white point in the range [0,1]. 
     */
    protected float whitePoint = 1.0f;
    /** Level adjustment: black point in the range [0,1]. 
     */
    protected float blackPoint = 0.0f;
    /** Level adjustment: mid point in the range [0,1]. 
     */
    protected float midPoint = 0.5f;
    /** Brightness adjustment in the range [-1,1].
     */
    protected float brightness = 0f;
    /** Exposure adjustment in the range [-1,1].
     */
    protected float exposure = 0f;
    /** Contrast adjustment in the range [-1,1].
     */
    protected float contrast = 0f;
    /** Saturation adjustment in the range [0,1].
     */
    protected float saturation = 0.5f;
    /** Definition adjustment in the range [0,1].
     */
    protected float definition = 0f;
    /** Higlights adjustment in the range [0,1].
     */
    protected float highlights = 0f;
    /** Shadows adjustment in the range [0,1].
     */
    protected float shadows = 0f;
    /** Sharpness adjustment in the range [0,1].
     */
    protected float sharpness = 0f;
    /** De-noise adjustment in the range [0,1].
     */
    protected float denoise = 0f;
    /** Temperature adjustment in the range [-1,1].
     */
    protected float temperature = 0f;
    /** Tint adjustment in the range [-1,1].
     */
    protected float tint = 0f;
    /** White balance terms {Rmu,Rnu, Bmu, Bnu}. */
    private float[] whiteBalanceQM = {0, 1, 0, 1};
    private boolean whiteBalanceTTEnabled = true;
    private boolean whiteBalanceQMEnabled = true;

    public DefaultColorAdjustModel() {
        reset();
    }

    @Override
    public void reset() {
        setWhitePoint(1);
        setBlackPoint(0);
        setMidPoint(0.5f);
        setBrightness(0f);
        setExposure(0f);
        setContrast(0f);
        setSaturation(0.5f);
        setDefinition(0f);
        setHighlights(0f);
        setShadows(0f);
        setSharpness(0f);
        setDenoise(0f);
        setTemperature(0f);
        setTint(0f);
        setWhiteBalanceQM(new float[]{0, 1, 0, 1});
        setWhiteBalanceQMEnabled(false);
        setWhiteBalanceTTEnabled(true);
    }

    /**
     * Get the value of whitePoint
     *
     * @return the value of whitePoint
     */
    @Override
    public float getWhitePoint() {
        return whitePoint;
    }

    /**
     * Set the value of whitePoint
     *
     * @param newValue new value of whitePoint
     */
    @Override
    public void setWhitePoint(float newValue) {
        float oldValue = whitePoint;
        whitePoint = newValue;
        firePropertyChange(WHITE_POINT_PROPERTY, oldValue, newValue);
    }

    @Override
    public float getBlackPoint() {
        return blackPoint;
    }

    @Override
    public void setBlackPoint(float newValue) {
        float oldValue = blackPoint;
        blackPoint = newValue;
        firePropertyChange(BLACK_POINT_PROPERTY, oldValue, newValue);
    }

    @Override
    public float getMidPoint() {
        return midPoint;
    }

    @Override
    public void setMidPoint(float newValue) {
        float oldValue = midPoint;
        midPoint = newValue;
        firePropertyChange(MID_POINT_PROPERTY, oldValue, newValue);
    }

    @Override
    public float getContrast() {
        return contrast;
    }

    @Override
    public void setContrast(float newValue) {
        float oldValue = contrast;
        this.contrast = newValue;
        firePropertyChange(CONTRAST_PROPERTY, oldValue, newValue);
    }

    @Override
    public float getDefinition() {
        return definition;
    }

    @Override
    public void setDefinition(float newValue) {
        float oldValue = definition;
        this.definition = newValue;
        firePropertyChange(DEFINITION_PROPERTY, oldValue, newValue);
    }

    @Override
    public float getDenoise() {
        return denoise;
    }

    @Override
    public void setDenoise(float newValue) {
        float oldValue = denoise;
        this.denoise = newValue;
        firePropertyChange(DENOISE_PROPERTY, oldValue, newValue);
    }

    @Override
    public float getBrightness() {
        return brightness;
    }

    @Override
    public void setBrightness(float newValue) {
        float oldValue = brightness;
        this.brightness = newValue;

        firePropertyChange(BRIGHTNESS_PROPERTY, oldValue, newValue);
    }

    @Override
    public float getExposure() {
        return exposure;
    }

    @Override
    public void setExposure(float newValue) {
        float oldValue = exposure;
        this.exposure = newValue;

        firePropertyChange(EXPOSURE_PROPERTY, oldValue, newValue);
    }

    @Override
    public float getHighlights() {
        return highlights;
    }

    @Override
    public void setHighlights(float newValue) {
        float oldValue = highlights;
        this.highlights = newValue;
        firePropertyChange(HIGHLIGHTS_PROPERTY, oldValue, newValue);
    }

    @Override
    public float getSaturation() {
        return saturation;
    }

    @Override
    public void setSaturation(float newValue) {
        float oldValue = saturation;
        this.saturation = newValue;
        firePropertyChange(SATURATION_PROPERTY, oldValue, newValue);
    }

    @Override
    public float getShadows() {
        return shadows;
    }

    @Override
    public void setShadows(float newValue) {
        float oldValue = shadows;
        this.shadows = newValue;
        firePropertyChange(SHADOWS_PROPERTY, oldValue, newValue);
    }

    @Override
    public float getSharpness() {
        return sharpness;
    }

    @Override
    public void setSharpness(float newValue) {
        float oldValue = sharpness;
        this.sharpness = newValue;
        firePropertyChange(SHARPNESS_PROPERTY, oldValue, newValue);
    }

    @Override
    public float getTemperature() {
        return temperature;
    }

    @Override
    public void setTemperature(float newValue) {
        float oldValue = temperature;
        this.temperature = newValue;
        firePropertyChange(TEMPERATURE_PROPERTY, oldValue, newValue);
    }

    @Override
    public float getTint() {
        return tint;
    }

    @Override
    public void setTint(float newValue) {
        float oldValue = tint;
        this.tint = newValue;
        firePropertyChange(TINT_PROPERTY, oldValue, newValue);
    }

    @Override
    public void setWhiteBalanceQM(float[] newValue) {
        float[] oldValue = whiteBalanceQM;
        this.whiteBalanceQM = newValue;
        firePropertyChange(WHITE_BALANCE_QM_PROPERTY, oldValue, newValue);
    }

    @Override
    public float[] getWhiteBalanceQM() {
        return whiteBalanceQM == null ? null : whiteBalanceQM.clone();
    }

    @Override
    public void setWhiteBalanceQMEnabled(boolean newValue) {
        boolean oldValue = whiteBalanceQMEnabled;
        this.whiteBalanceQMEnabled = newValue;
        firePropertyChange(WHITE_BALANCE_QM_ENABLED_PROPERTY, oldValue, newValue);
    }

    @Override
    public boolean isWhiteBalanceQMEnabled() {
        return whiteBalanceQMEnabled;
    }

    @Override
    public void setWhiteBalanceTTEnabled(boolean newValue) {
        boolean oldValue = whiteBalanceTTEnabled;
        this.whiteBalanceTTEnabled = newValue;
        firePropertyChange(WHITE_BALANCE_TT_ENABLED_PROPERTY, oldValue, newValue);
    }

    @Override
    public boolean isWhiteBalanceTTEnabled() {
        return whiteBalanceTTEnabled;
    }

    @Override
    public void setTo(ColorAdjustModel that) {
        whitePoint = that.getWhitePoint();
        blackPoint = that.getBlackPoint();
        midPoint = that.getMidPoint();
        brightness=that.getBrightness();
        exposure = that.getExposure();
        contrast = that.getContrast();
        saturation = that.getSaturation();
        definition = that.getDefinition();
        highlights = that.getHighlights();
        shadows = that.getShadows();
        sharpness = that.getSharpness();
        denoise = that.getDenoise();
        temperature = that.getTemperature();
        tint = that.getTint();
        whiteBalanceQM = that.getWhiteBalanceQM();
        whiteBalanceQMEnabled = that.isWhiteBalanceQMEnabled();
        whiteBalanceTTEnabled = that.isWhiteBalanceTTEnabled();
        firePropertyChange(null, false, true);
    }

    @Override
    public boolean isIdentity() {
        return whitePoint == 1//
                && blackPoint == 0//
                && midPoint == 0.5//
                && brightness == 0//
                && exposure == 0//
                && contrast == 0//
                && saturation == 0.5//
                && definition == 0//
                && highlights == 0//
                && shadows == 0//
                && sharpness == 0//
                && denoise == 0//
                && (!whiteBalanceTTEnabled
                || temperature == 0//
                && tint == 0)//
                && (!whiteBalanceQMEnabled
                || whiteBalanceQM[0] == 0//
                && whiteBalanceQM[1] == 1//
                && whiteBalanceQM[2] == 0//
                && whiteBalanceQM[3] == 1)//
                ;
    }
}
