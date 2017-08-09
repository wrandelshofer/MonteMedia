/* @(#)ColorAdjustCodec.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License. */
package org.monte.media.av.codec.video;

import java.awt.image.BufferedImage;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import org.monte.media.av.Buffer;
import static org.monte.media.av.BufferFlag.DISCARD;
import org.monte.media.av.Format;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.MIME_JAVA;
import org.monte.media.av.FormatKeys.MediaType;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;
import org.monte.media.color.ColorModels;
import org.monte.media.image.Images;
import org.monte.media.interpolator.BezierInterpolator;
import org.monte.media.interpolator.SplineInterpolator;

/**
 * Adjusts the colors of a buffered image.
 *
 * @author Werner Randelshofer
 * @version 1.0 2012-01-16 Created.
 */
public class ColorAdjustCodec extends AbstractVideoCodec {

    private ColorAdjustModel model = new DefaultColorAdjustModel();

    public ColorAdjustCodec() {
        super(new Format[]{
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                    EncodingKey, ENCODING_BUFFERED_IMAGE), //
                },
                new Format[]{
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                    EncodingKey, ENCODING_BUFFERED_IMAGE), //
                }//
                );
        name = "Adjust Color";
    }

    @Override
    public Format setInputFormat(Format f) {
        Format fNew = super.setInputFormat(f);
        outputFormat = fNew;
        return fNew;
    }
    @Override
    public Format setOutputFormat(Format f) {
        // This codec always produces the same ottput format as the input format.
        outputFormat=inputFormat;
        return outputFormat;
    }

    @Override
    public int process(Buffer in, Buffer out) {

        out.setMetaTo(in);
        out.format = outputFormat;
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }
        BufferedImage imgIn = (BufferedImage) in.data;
        if (imgIn == null || model == null) {
            out.setFlag(DISCARD);
            return CODEC_FAILED;
        }

        BufferedImage imgOut = null;
        if (out.data instanceof BufferedImage) {
            imgOut = (BufferedImage) out.data;
            if (imgOut.getWidth() != imgIn.getWidth()//
                    || imgOut.getHeight() != imgIn.getHeight()//
                    || imgOut.getType()!=BufferedImage.TYPE_INT_RGB) {
                imgOut = null;
            }
        }
        if (imgOut == null) {
            imgOut = new BufferedImage(imgIn.getWidth(), imgIn.getHeight(), BufferedImage.TYPE_INT_RGB);
        }
        out.data = imgOut;

        int[] rgbIn = Images.toPixels(imgIn);
        int[] rgbOut = Images.toPixels(imgOut);

        float whitePoint = max(model.getWhitePoint(), model.getBlackPoint());
        float midPoint = model.getMidPoint();
        float blackPoint = min(model.getBlackPoint(), model.getWhitePoint());
        float invLevelsExtent = 1f / (whitePoint - blackPoint);
        if (whitePoint == 1 && blackPoint == 0 || whitePoint - blackPoint == 0) {
            invLevelsExtent = -1;
        }

        float saturation = model.getSaturation() * 2;
        float invsqrt2 = (float) (1.0 / sqrt(2.0));
        float cbShift, crShift;
        boolean TT = model.isWhiteBalanceTTEnabled();
        if (TT) {
            cbShift = (-model.getTemperature() - model.getTint()) * invsqrt2;
            crShift = (model.getTemperature() - model.getTint()) * invsqrt2;
//System.out.println("ColorAdjustCodec tmp,tnt="+model.getTemperature()+","+model.getTint());            
//System.out.println("ColorAdjustCodec cb,cr="+cbShift+","+crShift);            
        } else {
            cbShift = crShift = 0;
        }

        SplineInterpolator sint;
        sint = new SplineInterpolator(0.5f - model.getShadows() * 0.5f, 0.5f,//
                0.5f + model.getHighlights() * 0.5f, 0.5f);

        BezierInterpolator hilightsAndShadows;
        if (model.getShadows() == 0 && model.getHighlights() == 0) {
            hilightsAndShadows = null;
        } else {
            hilightsAndShadows = new BezierInterpolator(new double[][]{//
                        {0, 0},//
                        {0.5f - model.getShadows() * 0.5f,//
                            0.5f},//
                        {0.5, 0.5},
                        {0.5f + model.getHighlights() * 0.5f,//
                            0.5f}, //
                        {1, 1}//
                    });
        }
        float brightness = model.getBrightness();
        float exposure = 1f + model.getExposure();
        exposure *= exposure;
        float contrast = 1f + model.getContrast();
        boolean becAdjust = (model.getBrightness() != 0 || model.getExposure() != 0 || model.getContrast() != 0);

        float[] ycc = new float[3];
        float[] rgb = new float[3];
        boolean QM = model.isWhiteBalanceQMEnabled();
        float[] wbqm = (QM) ? model.getWhiteBalanceQM() : new float[]{0, 1, 0, 1};

        float rmu = wbqm[0];
        float rnu = wbqm[1];
        float bmu = wbqm[2];
        float bnu = wbqm[3];
        /*if (QM) {
            System.out.println("ColorAdjustCodec mur=" + rmu + " nur=" + rnu + " mub=" + bmu + " nub=" + bnu);
        }*/

        for (int i = 0; i < rgbIn.length; i++) {
            int p = rgbIn[i];
            rgb[0] = (p & 0xff0000) >>> 16;
            rgb[1] = (p & 0xff00) >> 8;
            rgb[2] = (p & 0xff);

            if (QM) {
                // Note: QM operates on rgb values in the range [0,255]
                float r = rgb[0], b = rgb[2];
                rgb[0] = r * r * rmu + r * rnu;
                rgb[2] = b * b * bmu + b * bnu;
            }
            
            // From now on, we work with values in the range [0,1].
            rgb[0] *= 1f / 255f;
            rgb[1] *= 1f / 255f;
            rgb[2] *= 1f / 255f;


            if (TT) {
                ColorModels.RGBtoYCC(rgb, ycc);
                ycc[1] = (ycc[1] + cbShift) * saturation;
                ycc[2] = (ycc[2] + crShift) * saturation;
                ColorModels.YCCtoRGB(ycc, rgb);
            }

            if (hilightsAndShadows != null) {
                rgb[0] = hilightsAndShadows.getFraction(rgb[0]);
                rgb[1] = hilightsAndShadows.getFraction(rgb[1]);
                rgb[2] = hilightsAndShadows.getFraction(rgb[2]);
            }

            if (becAdjust) {
                rgb[0] = max(0, min(1, ((rgb[0] - 0.5f) * contrast + 0.5f) * exposure + brightness));
                rgb[1] = max(0, min(1, ((rgb[1] - 0.5f) * contrast + 0.5f) * exposure + brightness));
                rgb[2] = max(0, min(1, ((rgb[2] - 0.5f) * contrast + 0.5f) * exposure + brightness));
            }
            if (invLevelsExtent != -1) {
                rgb[0] = (max(min(rgb[0], whitePoint), blackPoint) - blackPoint) * invLevelsExtent;
                rgb[1] = (max(min(rgb[1], whitePoint), blackPoint) - blackPoint) * invLevelsExtent;
                rgb[2] = (max(min(rgb[2], whitePoint), blackPoint) - blackPoint) * invLevelsExtent;
            }

            rgbOut[i] = ((int) (rgb[0] * 255) << 16)
                    | ((int) (rgb[1] * 255) << 8)
                    | ((int) (rgb[2] * 255) << 0);
        }


        return CODEC_OK;
    }

    public ColorAdjustModel getModel() {
        return model;
    }

    public void setModel(ColorAdjustModel newValue) {
        this.model = newValue;
    }
}
