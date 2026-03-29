/*
 * @(#)ColorQuantizerMainModel.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.colorquantizer.model;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import org.monte.demo.javafx.colorquantizer.codec.AmigaHAMColorModelEncoder;
import org.monte.demo.javafx.colorquantizer.codec.DirectColorModelEncoder;
import org.monte.demo.javafx.colorquantizer.codec.IndexColorModelEncoder;
import org.monte.demo.javafx.colorquantizer.codec.IndexColorModelFactory;
import org.monte.media.av.Buffer;
import org.monte.media.av.Codec;
import org.monte.media.av.CodecChain;
import org.monte.media.av.Format;
import org.monte.media.av.codec.video.ConvertColorSpaceCodec;
import org.monte.media.av.codec.video.CropImageCodec;
import org.monte.media.av.codec.video.ImageOpCodec;
import org.monte.media.av.codec.video.ReplaceColorSpaceCodec;
import org.monte.media.av.codec.video.ScaleImageCodec;
import org.monte.media.av.codec.video.VideoFormatKeys;
import org.monte.media.color.ColorSpaces;
import org.monte.media.color.RgbBitConverters;
import org.monte.media.color.icc.ICC_ProfileReader;
import org.monte.media.color.io.ColorManagedImageReader;
import org.monte.media.color.quant.OctreeColorQuantizer;
import org.monte.media.image.op.UnsharpMaskOp;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class ColorQuantizerMainModel {
    private final ObjectProperty<ColorSpace> referenceImageColorSpace = new SimpleObjectProperty<>();
    private final BooleanProperty lockPaletteIndex0 = new SimpleBooleanProperty();
    private final BooleanProperty scaleInLinearSpace = new SimpleBooleanProperty();
    private final BooleanProperty crop = new SimpleBooleanProperty();
    private final BooleanProperty scale = new SimpleBooleanProperty();
    private final BooleanProperty sharpen = new SimpleBooleanProperty();
    private final BooleanProperty preserveAspectRatio = new SimpleBooleanProperty(true);
    private final IntegerProperty paletteSize = new SimpleIntegerProperty(64);
    private final IntegerProperty zoom = new SimpleIntegerProperty(0);
    private final IntegerProperty width = new SimpleIntegerProperty();
    private final IntegerProperty height = new SimpleIntegerProperty();
    private final IntegerProperty scaledWidth = new SimpleIntegerProperty(320);
    private final IntegerProperty scaledHeight = new SimpleIntegerProperty(240);
    private final IntegerProperty cropLeft = new SimpleIntegerProperty();
    private final IntegerProperty cropTop = new SimpleIntegerProperty();
    private final IntegerProperty cropRight = new SimpleIntegerProperty();
    private final IntegerProperty cropBottom = new SimpleIntegerProperty();
    private final DoubleProperty ditherIntensityFactor = new SimpleDoubleProperty(15);
    private final DoubleProperty scaleRadiusFactor = new SimpleDoubleProperty(0.5);
    private final DoubleProperty sharpenAmount = new SimpleDoubleProperty(1);
    private final DoubleProperty sharpenRadius = new SimpleDoubleProperty(0.75);
    private final StringProperty batchOutputFormat = new SimpleStringProperty("PNG");
    private final ObjectProperty<Color> palette0Color = new SimpleObjectProperty<>(Color.BLACK);
    private final ObjectProperty<Path> referenceFile = new SimpleObjectProperty<>();
    private final ObjectProperty<Path> batchOutputDirectory = new SimpleObjectProperty<>();
    private final ObjectProperty<BufferedImage> rawReferenceImage = new SimpleObjectProperty<>();
    private final ObjectProperty<BufferedImage> colorCorrectedReferenceImage = new SimpleObjectProperty<>();
    private final ObjectProperty<ColorMode> colorMode = new SimpleObjectProperty<>(ColorMode._24_BIT_RGB);
    private final ObjectProperty<PaletteMode> paletteMode = new SimpleObjectProperty<>(PaletteMode.COMPUTED_K_MEANS);
    private final ObjectProperty<IndexColorModel> indexColorModel = new SimpleObjectProperty<>(null);
    private final ObjectProperty<DitheringMethod> ditheringMethod = new SimpleObjectProperty<>(DitheringMethod.NONE);
    private final ObjectProperty<BufferedImage> renderedImage = new SimpleObjectProperty<>();
    private final ExecutorService exec = Executors.newSingleThreadExecutor();
    private final ListProperty<Path> batchInputFiles = new SimpleListProperty<>(FXCollections.observableArrayList());
    int changing;
    private volatile Task<?> currentTask;

    public ColorQuantizerMainModel() {
        referenceFile.addListener(this::updateReferenceImage);
        referenceImageColorSpace.addListener(this::updateReferenceImage);
        rawReferenceImage.addListener(this::updateInputImage);
        scaledHeight.addListener(this::scaledHeightChanged);

        InvalidationListener updateScaledHeight = this::updateScaledHeight;
        scaledWidth.addListener(updateScaledHeight);
        preserveAspectRatio.addListener(updateScaledHeight);
        crop.addListener(updateScaledHeight);
        scale.addListener(updateScaledHeight);
        cropLeft.addListener(updateScaledHeight);
        cropRight.addListener(updateScaledHeight);
        cropTop.addListener(updateScaledHeight);
        cropBottom.addListener(updateScaledHeight);

        InvalidationListener updateRenderedImage = this::updateRenderedImage;
        sharpen.addListener(updateRenderedImage);
        cropLeft.addListener(updateRenderedImage);
        scaleRadiusFactor.addListener(updateRenderedImage);
        sharpenAmount.addListener(updateRenderedImage);
        sharpenRadius.addListener(updateRenderedImage);
        rawReferenceImage.addListener(updateRenderedImage);
        crop.addListener(updateRenderedImage);
        scale.addListener(updateRenderedImage);
        cropLeft.addListener(updateRenderedImage);
        cropRight.addListener(updateRenderedImage);
        cropTop.addListener(updateRenderedImage);
        cropBottom.addListener(updateRenderedImage);
        scaledWidth.addListener(updateRenderedImage);
        scaledHeight.addListener(updateRenderedImage);
        colorMode.addListener(updateRenderedImage);
        ditheringMethod.addListener(updateRenderedImage);
        ditherIntensityFactor.addListener(updateRenderedImage);
        paletteMode.addListener(updateRenderedImage);
        lockPaletteIndex0.addListener(updateRenderedImage);
        palette0Color.addListener(updateRenderedImage);
        scaleInLinearSpace.addListener(updateRenderedImage);
        paletteSize.addListener(updateRenderedImage);
    }

    public ListProperty<Path> batchInputFilesProperty() {
        return batchInputFiles;
    }

    public ObjectProperty<Path> batchOutputDirectoryProperty() {
        return batchOutputDirectory;
    }

    public StringProperty batchOutputFormatProperty() {
        return batchOutputFormat;
    }

    public ObjectProperty<BufferedImage> colorCorrectedReferenceImageProperty() {
        return colorCorrectedReferenceImage;
    }

    public ObjectProperty<ColorMode> colorModeProperty() {
        return colorMode;
    }

    public Task<Void> createBatchTask() {
        List<Path> files = List.copyOf(getBatchInputFiles());
        Path dir = getBatchOutputDirectory();
        String outputFormat = getBatchOutputFormat();
        BlockingQueue<Codec> codecs = new LinkedBlockingQueue<>();
        for (int i = 0, n = ForkJoinPool.commonPool().getParallelism(); i < n; i++) {
            Codec codec = createCodecPipeline();
            if (codec == null) {
                break;
            }
            codecs.add(codec);
        }

        Task<Void> newTask = new Task<Void>() {
            {
                updateProgress(0, files.size());
            }

            @Override
            protected Void call() throws Exception {

                if (files.isEmpty()) {
                    throw new RuntimeException("No input files.");
                }
                Path dir = getBatchOutputDirectory();
                if (dir == null) {
                    throw new RuntimeException("No output directory.");
                }
                if (codecs.isEmpty()) {
                    throw new RuntimeException("No processing instructions.");
                }
                String outputFormat = getBatchOutputFormat();
                if (outputFormat == null || outputFormat.isBlank()) {
                    throw new RuntimeException("No output format.");
                }


                AtomicInteger counter = new AtomicInteger();
                IntStream.range(0, files.size()).parallel().forEach(index -> {
                    if (isDone()) {
                        return;
                    }
                    Codec borrowedCodec = null;
                    int progress = counter.getAndIncrement();
                    Path p = files.get(progress);
                    try {
                        borrowedCodec = codecs.take();
                        updateMessage(p.getFileName().toString());
                        long startTime = System.nanoTime();
                        process(p, borrowedCodec, outputFormat, dir);
                        System.out.println(p.getFileName() + " elapsed=" + (System.nanoTime() - startTime) / 1_000_000 + "ms");
                        updateProgress(progress + 1, files.size());
                    } catch (Exception e) {
                        throw new RuntimeException(e.getMessage() + " " + p.getFileName(), e);
                    } finally {
                        if (borrowedCodec != null) {
                            codecs.add(borrowedCodec);
                        }
                    }
                });

                return null;
            }

            @Override
            protected void cancelled() {
                updateMessage("Batch cancelled");
            }

            @Override
            protected void failed() {
                var e = getException();
                if (e != null) {
                    //noinspection CallToPrintStackTrace
                    e.printStackTrace();
                    if (e.getMessage() != null) {
                        updateMessage(e.getMessage());
                    } else {
                        updateMessage("Batch failed");
                    }
                }
            }

            @Override
            protected void succeeded() {
                updateMessage("Batch completed");
            }
        };


        return newTask;
    }

    public Codec createCodecPipeline() {
        BufferedImage inputImg = getRawReferenceImage();
        if (inputImg == null) return null;
        List<Codec> codecs = new ArrayList<>();
        int outputWidth = inputImg.getWidth();
        int outputHeight = inputImg.getHeight();

        ColorSpace cs = getReferenceImageColorSpace();
        if (cs != null) {
            var codec = new ReplaceColorSpaceCodec();
            codec.setOutputFormat(new Format(ReplaceColorSpaceCodec.ReplaceColorSpaceKey, cs));
            codecs.add(codec);
        } else {
            cs = inputImg.getColorModel().getColorSpace();
        }

        if (isCrop()) {
            var codec = new CropImageCodec();
            codec.setOutputFormat(new Format(CropImageCodec.CropImageKey,
                    new Rectangle(getCropLeft(), getCropTop(), inputImg.getWidth() - getCropLeft() - getCropRight(),
                            inputImg.getHeight() - getCropTop() - getCropBottom())));

            codecs.add(codec);
        }
        if (isScale()) {
            outputWidth = getScaledWidth();
            outputHeight = getScaledHeight();
            if (isScaleInLinearSpace()) {
                var codec = new ConvertColorSpaceCodec();
                codec.setOutputFormat(new Format(ConvertColorSpaceCodec.ConvertColorSpaceKey,
                        ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB)));
                codecs.add(codec);
            }

            var codec = new ScaleImageCodec();
            codec.setOutputFormat(new Format(
                    VideoFormatKeys.WidthKey, outputWidth,
                    VideoFormatKeys.HeightKey, outputHeight,
                    ScaleImageCodec.ScaleGaussianBlurFactorKey, getScaleRadiusFactor()
            ));
            codecs.add(codec);
        }
        if (isSharpen() && getSharpenAmount() > 0 && getSharpenRadius() > 0) {
            var codec = new ImageOpCodec();
            codec.setOutputFormat(new Format(
                    VideoFormatKeys.WidthKey, outputWidth,
                    VideoFormatKeys.HeightKey, outputHeight,
                    ImageOpCodec.ImageOpKey, new UnsharpMaskOp((float) getSharpenRadius(), (float) getSharpenAmount(), 0.025f)));
            codecs.add(codec);
        }

        Format colorFormat = new Format(DirectColorModelEncoder.DitheringMethodKey, getDitheringMethod(),
                DirectColorModelEncoder.DitheringFactorKey, getDitherIntensityFactor());
        switch (getColorMode()) {
            case _18_BIT_RGB -> {
                var codec = new DirectColorModelEncoder();
                codec.setOutputFormat(colorFormat.append(DirectColorModelEncoder.DirectColorModelKey,
                        new DirectColorModel(cs, 18,
                                0b111111_000000_000000, 0b111111_000000, 0b111111, 0x000, false,
                                DataBuffer.TYPE_INT)));
                codecs.add(codec);
            }
            case _5_6_5_BIT_RGB -> {
                var codec = new DirectColorModelEncoder();
                codec.setOutputFormat(colorFormat.append(DirectColorModelEncoder.DirectColorModelKey,
                        new DirectColorModel(cs, 16,
                                0xf800, 0x07e0, 0x01f, 0x000, false,
                                DataBuffer.TYPE_USHORT)));
                codecs.add(codec);
            }
            case _15_BIT_RGB -> {
                var codec = new DirectColorModelEncoder();
                codec.setOutputFormat(colorFormat.append(DirectColorModelEncoder.DirectColorModelKey,
                        new DirectColorModel(cs, 15,
                                0x7c00, 0x03e0, 0x01f, 0x000, false,
                                DataBuffer.TYPE_USHORT)));
                codecs.add(codec);
            }
            case _12_BIT_RGB -> {
                var codec = new DirectColorModelEncoder();
                codec.setOutputFormat(colorFormat.append(DirectColorModelEncoder.DirectColorModelKey,
                        new DirectColorModel(cs, 12, 0xf00, 0x0f0, 0x00f, 0x000, false,
                                DataBuffer.TYPE_USHORT)));
                codecs.add(codec);
            }
            case _24_BIT_INDEXED_COLORS -> {
                var codec = createIndexColorModelCodec(colorFormat, inputImg, getPaletteSize());
                codecs.add(codec);
            }
            case AMIGA_HAM6 -> {
                colorFormat = colorFormat.prepend(AmigaHAMColorModelEncoder.BitsPerColorKey, 4);
                var codec = createHAMColorModelCodec(colorFormat, inputImg, getPaletteSize());
                codecs.add(codec);
            }
            case AMIGA_HAM8 -> {
                colorFormat = colorFormat.prepend(AmigaHAMColorModelEncoder.BitsPerColorKey, 8);
                var codec = createHAMColorModelCodec(colorFormat, inputImg, getPaletteSize());
                codecs.add(codec);
            }
            case AMIGA_HAM8_FLICKERFREE -> {
                colorFormat = colorFormat.prepend(AmigaHAMColorModelEncoder.BitsPerColorKey, 6);
                var codec = createHAMColorModelCodec(colorFormat, inputImg, getPaletteSize());
                codecs.add(codec);
            }
            case _12_BIT_INDEXED_COLORS -> {
                var dcm = new DirectColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), 12, 0xf00, 0x0f0, 0x00f, 0x000, false,
                        DataBuffer.TYPE_USHORT);
                WritableRaster raster = dcm.createCompatibleWritableRaster(inputImg.getWidth(),
                        inputImg.getHeight());

                BufferedImage _12bitImg = new BufferedImage(dcm, raster, false, null);
                Graphics2D g = _12bitImg.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
                g.drawImage(inputImg, 0, 0, null);
                var codec = createIndexColorModelCodec(colorFormat, _12bitImg, getPaletteSize());
                codecs.add(codec);
            }
            case null, default -> {
            }
        }
        return CodecChain.createCodecChain(codecs);
    }

    private AmigaHAMColorModelEncoder createHAMColorModelCodec(Format colorFormat, BufferedImage inputImg, int paletteSize) {
        var codec = new AmigaHAMColorModelEncoder();
        IndexColorModel icm;
        if (getColorMode() == ColorMode.AMIGA_HAM6) {
            paletteSize = Math.min(paletteSize, 16);
            colorFormat = colorFormat.append(AmigaHAMColorModelEncoder.BitsPerColorKey, 4);
        } else {
            paletteSize = Math.min(paletteSize, 64);
        }
        var c0 = getPalette0Color();
        if (c0 == null) c0 = Color.BLACK;
        var c0Rgb = RgbBitConverters.rgbFloatToRgb24(new float[]{(float) c0.getRed(), (float) c0.getGreen(), (float) c0.getBlue()});
        if (isLockPaletteIndex0()) {
            colorFormat = colorFormat.append(IndexColorModelEncoder.ReservedColorsKey, new IndexColorModel(8, 1, new int[]{c0Rgb}, 0, false, -1, DataBuffer.TYPE_BYTE));
        }
        int availableSize = isLockPaletteIndex0() ? paletteSize - 1 : paletteSize;

        colorFormat = colorFormat.append(AmigaHAMColorModelEncoder.NumberOfColorsKey, paletteSize);
        switch (getPaletteMode()) {

            case COMPUTED_OCTREE -> {
                var q = new OctreeColorQuantizer(availableSize);
                q.addImage(inputImg);
                icm = q.computeColorPalette();
            }
            case COMPUTED_ORDERED -> {
                icm = new IndexColorModelFactory().createOrderedPalette(availableSize);
            }
            case LOADED_PALETTE -> {
                icm = getIndexColorModel();
            }

            default -> {
                icm = null;
            }
        }
        if (icm != null && isLockPaletteIndex0()) {
            int[] cmap = new int[icm.getMapSize() + 1];
            icm.getRGBs(cmap);

            if (cmap[0] != c0Rgb) {
                System.arraycopy(cmap, 0, cmap, 1, cmap.length - 1);
                cmap[0] = c0Rgb;
                icm = new IndexColorModel(icm.getPixelSize(), cmap.length, cmap, 0, false, -1, DataBuffer.TYPE_BYTE);
            }
        }
        codec.setOutputFormat(colorFormat.append(IndexColorModelEncoder.IndexColorModelKey, icm));
        return codec;
    }

    private IndexColorModelEncoder createIndexColorModelCodec(Format colorFormat, BufferedImage inputImg, int paletteSize) {

        var codec = new IndexColorModelEncoder();
        IndexColorModel icm;
        paletteSize = Math.clamp(paletteSize, 2, 256);
        colorFormat = colorFormat.append(IndexColorModelEncoder.NumberOfColorsKey, paletteSize);
        if (getColorMode() == ColorMode._12_BIT_INDEXED_COLORS || getColorMode() == ColorMode.AMIGA_HAM6) {
            colorFormat = colorFormat.append(IndexColorModelEncoder.BitsPerColorKey, 4);
        }
        var c0 = getPalette0Color();
        if (c0 == null) c0 = Color.BLACK;
        var c0Rgb = RgbBitConverters.rgbFloatToRgb24(new float[]{(float) c0.getRed(), (float) c0.getGreen(), (float) c0.getBlue()});
        if (isLockPaletteIndex0()) {
            colorFormat = colorFormat.append(IndexColorModelEncoder.ReservedColorsKey, new IndexColorModel(8, 1, new int[]{c0Rgb}, 0, false, -1, DataBuffer.TYPE_BYTE));
        }
        int availableSize = isLockPaletteIndex0() ? paletteSize - 1 : paletteSize;
        switch (getPaletteMode()) {

            case COMPUTED_OCTREE -> {
                var q = new OctreeColorQuantizer(availableSize);
                q.addImage(inputImg);
                icm = q.computeColorPalette();
            }
            case COMPUTED_ORDERED -> {
                icm = new IndexColorModelFactory().createOrderedPalette(availableSize);
            }
            case LOADED_PALETTE -> {
                icm = getIndexColorModel();
            }

            default -> {
                icm = null;
            }
        }
        if (icm != null && isLockPaletteIndex0()) {
            int[] cmap = new int[icm.getMapSize() + 1];
            icm.getRGBs(cmap);

            if (cmap[0] != c0Rgb) {
                System.arraycopy(cmap, 0, cmap, 1, cmap.length - 1);
                cmap[0] = c0Rgb;
                icm = new IndexColorModel(icm.getPixelSize(), cmap.length, cmap, 0, false, -1, DataBuffer.TYPE_BYTE);
            }
        }
        codec.setOutputFormat(colorFormat.append(IndexColorModelEncoder.IndexColorModelKey, icm));
        return codec;
    }

    public IntegerProperty cropBottomProperty() {
        return cropBottom;
    }

    public IntegerProperty cropLeftProperty() {
        return cropLeft;
    }

    public BooleanProperty cropProperty() {
        return crop;
    }

    public IntegerProperty cropRightProperty() {
        return cropRight;
    }

    public IntegerProperty cropTopProperty() {
        return cropTop;
    }

    public DoubleProperty ditherIntensityFactorProperty() {
        return ditherIntensityFactor;
    }

    public ObjectProperty<DitheringMethod> ditheringMethodProperty() {
        return ditheringMethod;
    }

    public ObservableList<Path> getBatchInputFiles() {
        return batchInputFiles.get();
    }

    public Path getBatchOutputDirectory() {
        return batchOutputDirectory.get();
    }

    public void setBatchOutputDirectory(Path newValue) {
        batchOutputDirectory.set(newValue);
    }

    public String getBatchOutputFormat() {
        return batchOutputFormat.get();
    }

    public ColorMode getColorMode() {
        return colorMode.get();
    }

    public int getCropBottom() {
        return cropBottom.get();
    }

    public void setCropBottom(int newValue) {
        cropBottom.set(newValue);
    }

    public int getCropLeft() {
        return cropLeft.get();
    }

    public void setCropLeft(int newValue) {
        cropLeft.set(newValue);
    }

    public int getCropRight() {
        return cropRight.get();
    }

    public void setCropRight(int newValue) {
        cropRight.set(newValue);
    }

    public int getCropTop() {
        return cropTop.get();
    }

    public void setCropTop(int newValue) {
        cropTop.set(newValue);
    }

    public double getDitherIntensityFactor() {
        return ditherIntensityFactor.get();
    }

    public DitheringMethod getDitheringMethod() {
        return ditheringMethod.get();
    }

    public int getHeight() {
        return height.get();
    }

    public void setHeight(int newValue) {
        height.set(newValue);
    }

    public IndexColorModel getIndexColorModel() {
        return indexColorModel.get();
    }

    public void setIndexColorModel(IndexColorModel newValue) {
        indexColorModel.set(newValue);
    }

    public Color getPalette0Color() {
        return palette0Color.get();
    }

    public PaletteMode getPaletteMode() {
        return paletteMode.get();
    }

    public int getPaletteSize() {
        return paletteSize.get();
    }

    public BufferedImage getRawReferenceImage() {
        return rawReferenceImage.get();
    }

    public void setRawReferenceImage(BufferedImage newValue) {
        rawReferenceImage.set(newValue);
    }

    public Path getReferenceFile() {
        return referenceFile.get();
    }

    public void setReferenceFile(Path newValue) {
        referenceFile.set(newValue);
    }

    public ColorSpace getReferenceImageColorSpace() {
        return referenceImageColorSpace.get();
    }

    public BufferedImage getRenderedImage() {
        return renderedImage.get();
    }

    public void setRenderedImage(BufferedImage newValue) {
        renderedImage.set(newValue);
    }

    public double getScaleRadiusFactor() {
        return scaleRadiusFactor.get();
    }

    public int getScaledHeight() {
        return scaledHeight.get();
    }

    public void setScaledHeight(int newValue) {
        scaledHeight.set(newValue);
    }

    public int getScaledWidth() {
        return scaledWidth.get();
    }

    public void setScaledWidth(int newValue) {
        scaledWidth.set(newValue);
    }

    public double getSharpenAmount() {
        return sharpenAmount.get();
    }

    public double getSharpenRadius() {
        return sharpenRadius.get();
    }

    public int getWidth() {
        return width.get();
    }

    public void setWidth(int newValue) {
        width.set(newValue);
    }

    public int getZoom() {
        return zoom.get();
    }

    public IntegerProperty heightProperty() {
        return height;
    }

    public ObjectProperty<IndexColorModel> indexColorModelProperty() {
        return indexColorModel;
    }

    public boolean isCrop() {
        return crop.get();
    }

    public boolean isLockPaletteIndex0() {
        return lockPaletteIndex0.get();
    }

    public boolean isPreserveAspectRatio() {
        return preserveAspectRatio.get();
    }

    public boolean isScale() {
        return scale.get();
    }

    public boolean isScaleInLinearSpace() {
        return scaleInLinearSpace.get();
    }

    public boolean isSharpen() {
        return sharpen.get();
    }

    public BooleanProperty lockPaletteIndex0Property() {
        return lockPaletteIndex0;
    }

    public ObjectProperty<Color> palette0ColorProperty() {
        return palette0Color;
    }

    public ObjectProperty<PaletteMode> paletteModeProperty() {
        return paletteMode;
    }

    public IntegerProperty paletteSizeProperty() {
        return paletteSize;
    }

    public BooleanProperty preserveAspectRatioProperty() {
        return preserveAspectRatio;
    }

    private void printImageMetadata(IIOMetadata iioMeta) {

        var r = new ICC_ProfileReader(iioMeta);
        IO.println(r.toString());
    }

    private void process(Path p, Codec codec, String outputFormat, Path dir) throws IOException {
        BufferedImage in = ColorManagedImageReader.read(p.toFile());
        var src = new Buffer();
        var dst = new Buffer();
        src.data = in;
        var result = codec.process(src, dst);
        if (result != Codec.CODEC_OK) {
            throw new RuntimeException("Codec failed", dst.exception);
        }
        if (!(dst.data instanceof BufferedImage)) {
            throw new RuntimeException("Codec produced unexpected output: " + dst.data);
        }
        saveImageFileAs((BufferedImage) dst.data, dir.resolve(p.getFileName()), outputFormat);
    }

    public ObjectProperty<BufferedImage> rawReferenceImageProperty() {
        return rawReferenceImage;
    }

    public ObjectProperty<Path> referenceFileProperty() {
        return referenceFile;
    }

    public ObjectProperty<ColorSpace> referenceImageColorSpaceProperty() {
        return referenceImageColorSpace;
    }

    public ObjectProperty<BufferedImage> renderedImageProperty() {
        return renderedImage;
    }

    public boolean saveFileAs(Path path, String formatName) {
        try {
            return ImageIO.write(getRenderedImage(), formatName, path.toFile());
        } catch (IOException e) {
            return false;
        }
    }

    public void saveImageFileAs(BufferedImage image, Path newFile, String formatName) throws IOException {
        var iter = IIORegistry.getDefaultInstance().getServiceProviders(ImageWriterSpi.class,
                new ServiceRegistry.Filter() {
                    @Override
                    public boolean filter(Object provider) {
                        for (var n : ((ImageWriterSpi) provider).getFormatNames()) {
                            if (formatName.equals(n)) return true;
                        }
                        return false;
                    }
                },
                true);
        if (!iter.hasNext()) {
            throw new IOException("Unsupported format: " + formatName);
        }
        var spi = iter.next();
        var suffixes = spi.getFileSuffixes();
        if (suffixes.length > 0) {
            String fileName = newFile.getFileName().toString();
            var p = fileName.lastIndexOf('.');
            if (p != -1) fileName = fileName.substring(0, p);
            newFile = newFile.resolveSibling(fileName + "." + suffixes[0]);
        }
        if (!spi.canEncodeImage(image)) {
            BufferedImage newImg = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            var g = newImg.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            image = newImg;
        }
        var w = spi.createWriterInstance();
        if (Files.exists(newFile)) {
            Files.delete(newFile);
        }
        try (var out = new FileImageOutputStream(newFile.toFile())) {
            w.setOutput(out);
            w.write(image);
        }
    }

    public BooleanProperty scaleInLinearSpaceProperty() {
        return scaleInLinearSpace;
    }

    public BooleanProperty scaleProperty() {
        return scale;
    }

    public DoubleProperty scaleRadiusFactorProperty() {
        return scaleRadiusFactor;
    }

    private void scaledHeightChanged(Observable observable) {
        if (changing++ == 0) {
            if (isScale() && isPreserveAspectRatio()) {
                int w = isCrop() ? getWidth() - getCropLeft() - getCropRight() : getWidth();
                int h = isCrop() ? getHeight() - getCropTop() - getCropBottom() : getHeight();
                int sh = getScaledHeight();
                if (sh > 0) {
                    scaledWidth.set(Math.max(1, Math.round(w * sh / (float) h)));
                }
            }
        }
        changing--;
    }

    public IntegerProperty scaledHeightProperty() {
        return scaledHeight;
    }

    public IntegerProperty scaledWidthProperty() {
        return scaledWidth;
    }

    public void setColorCorrectedReferenceImage(BufferedImage newValue) {
        colorCorrectedReferenceImage.set(newValue);
    }

    public DoubleProperty sharpenAmountProperty() {
        return sharpenAmount;
    }

    public BooleanProperty sharpenProperty() {
        return sharpen;
    }

    public DoubleProperty sharpenRadiusProperty() {
        return sharpenRadius;
    }

    public void submit(Task<?> task) {
        exec.submit(task);
    }

    public void updateInputImage(Observable o, BufferedImage oldv, BufferedImage newv) {
        if (newv != null) {
            changing++;
            setWidth(newv.getWidth());
            setHeight(newv.getHeight());
            if (!isPreserveAspectRatio()) {
                setScaledHeight(newv.getHeight());
            }
            if (getReferenceImageColorSpace() == null || newv == null) {
                setColorCorrectedReferenceImage(newv);
            } else {
                var codec = new ReplaceColorSpaceCodec();
                codec.setOutputFormat(new Format(ReplaceColorSpaceCodec.ReplaceColorSpaceKey, getReferenceImageColorSpace()));
                var in = new Buffer();
                in.data = newv;
                var out = new Buffer();
                codec.process(in, out);
                if (out.data instanceof BufferedImage bimg) {
                    setColorCorrectedReferenceImage(bimg);
                } else {
                    setColorCorrectedReferenceImage(null);
                }
            }
            changing--;
        }
    }

    private void updateReferenceImage(Observable o) {
        Path newv = getReferenceFile();
        var cs = getReferenceImageColorSpace();
        exec.submit(currentTask = new Task<BufferedImage>() {

            @Override
            protected BufferedImage call() throws Exception {
                try (ImageInputStream iis = ImageIO.createImageInputStream(newv.toFile());
                     var reader = new ColorManagedImageReader()) {
                    reader.setInput(iis);
                    var newImage = reader.read(0);

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            setRawReferenceImage(newImage);
                        }
                    });
                    return newImage;
                }
            }

            @Override
            protected void failed() {
                System.err.println("failed");
                System.err.println(getException());
            }
        });
    }

    private void updateReferenceImageOLD(Observable o) {
        Path newv = getReferenceFile();
        var cs = getReferenceImageColorSpace();
        exec.submit(currentTask = new Task<BufferedImage>() {

            @Override
            protected BufferedImage call() throws Exception {


                //var finalNewImage = newv == null ? null : ImageIO.read(newv.toFile());
                try (ImageInputStream iis = ImageIO.createImageInputStream(newv.toFile())) {

                    Iterator<ImageReader> it = ImageIO.getImageReaders(iis);
                    if (!it.hasNext()) {
                        return null;
                    }
                    ImageReader imageReader = it.next();
                    iis.seek(0L);
                    imageReader.setInput(iis, false, false);
                    var newImage = imageReader.read(0);
                    var profile = new ICC_ProfileReader(imageReader.getImageMetadata(0)).getProfile();
                    if (profile != null && newImage.getColorModel() instanceof DirectColorModel dcm) {
                        ColorModel colorModel = new DirectColorModel(new ICC_ColorSpace(profile),
                                dcm.getPixelSize(),
                                dcm.getRedMask(), dcm.getGreenMask(), dcm.getBlueMask(), dcm.getAlphaMask(),
                                dcm.isAlphaPremultiplied(), dcm.getTransferType());

                        newImage = new BufferedImage(
                                colorModel,
                                newImage.getRaster(),
                                newImage.isAlphaPremultiplied(),
                                null
                        );
                    } else if (profile != null && newImage.getColorModel() instanceof ComponentColorModel dcm) {
                        ColorModel colorModel = new ComponentColorModel(new ICC_ColorSpace(profile),
                                dcm.hasAlpha(),
                                dcm.isAlphaPremultiplied(), dcm.getTransparency(), dcm.getTransferType());

                        newImage = new BufferedImage(
                                colorModel,
                                newImage.getRaster(),
                                newImage.isAlphaPremultiplied(),
                                null
                        );
                    }
                    var finalNewImage = newImage;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            setRawReferenceImage(finalNewImage);
                        }
                    });
                    return finalNewImage;
                }
            }

            @Override
            protected void failed() {
                System.err.println("failed");
                System.err.println(getException());
            }
        });
    }

    public void updateRenderedImage(Observable o) {
        var inputImg = getRawReferenceImage();
        if (inputImg == null) return;
        final Codec finalCodec = createCodecPipeline();
        final BufferedImage finalInputImg = inputImg;
        var newTask = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                long startTime = System.nanoTime();
                try {
                    if (isCancelled()) {
                        return null;
                    }
                    BufferedImage renderedImg;
                    if (finalCodec == null) {
                        renderedImg = finalInputImg;
                    } else {
                        var in = new Buffer();
                        var out = new Buffer();
                        in.data = finalInputImg;
                        int result = finalCodec.process(in, out);
                        if (result != Codec.CODEC_OK) {
                            System.out.println("CODEC failed " + out.exception);
                        }
                        renderedImg = (BufferedImage) out.data;
                    }
                    if (isCancelled()) {
                        return null;
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (renderedImg != null && renderedImg.getColorModel() instanceof IndexColorModel icm) {
                                setIndexColorModel(icm);
                            }
                            setRenderedImage(renderedImg);
                        }
                    });
                    System.out.println("ColorQuantizerMainModel updateRenderedImage elapsed=" + (System.nanoTime() - startTime) / 1_000_000 + "ms");
                    System.out.println("  rendered image color space: " + ColorSpaces.toString(renderedImg.getColorModel().getColorSpace()));
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                return null;
            }
        };
        if (currentTask != null) {
            currentTask.cancel(true);
        }
        currentTask = newTask;
        exec.submit(newTask);
    }

    private void updateScaledHeight(Observable observable) {
        if (changing++ == 0) {
            if (isScale() && isPreserveAspectRatio()) {
                int w = isCrop() ? getWidth() - getCropLeft() - getCropRight() : getWidth();
                int h = isCrop() ? getHeight() - getCropTop() - getCropBottom() : getHeight();
                int sw = getScaledWidth();
                if (sw > 0) {
                    scaledHeight.set(Math.max(1, Math.round(sw * h / (float) w)));
                }
            }
        }
        changing--;
    }

    public IntegerProperty widthProperty() {
        return width;
    }

    public IntegerProperty zoomProperty() {
        return zoom;
    }
}
