package org.monte.media.screenrecorder;

import org.monte.media.av.Buffer;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys;
import org.monte.media.color.Colors;
import org.monte.media.math.Rational;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

import static org.monte.media.av.FormatKeys.*;
import static org.monte.media.av.codec.video.VideoFormatKeys.DepthKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;

public class ScreenGrabber implements Runnable, AutoCloseable {
	
	private final Point prevDrawnMouseLocation = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
	private boolean prevMousePressed = false;
	private BufferedImage screenCapture;
	private final ScreenRecorder recorder;
	private final Robot robot;
	private final Rectangle captureArea;
	private final BufferedImage videoImg;
	private final Graphics2D videoGraphics;
	private final Format mouseFormat;
	private final ArrayBlockingQueue<Buffer> mouseCaptures;
	private Rational prevScreenCaptureTime;
	private final BufferedImage cursorImg;
	private final BufferedImage cursorImgPressed;
	private final Point cursorOffset;
	private final int videoTrack;
	private final long startTime;
	private final AtomicLong stopTime = new AtomicLong(Long.MAX_VALUE);
	private ScheduledFuture<?> future;
	private long sequenceNumber;
	
	public ScreenGrabber(ScreenRecorder recorder, long startTime) throws AWTException, IOException {
		this.recorder = recorder;
		this.captureArea = recorder.getCaptureArea();
		this.robot = new Robot(recorder.getCaptureDevice());
		this.mouseFormat = recorder.mouseFormat;
		this.mouseCaptures = recorder.getMouseCaptures();
		this.cursorImg = recorder.getCursorImg();
		this.cursorImgPressed = recorder.getCursorImgPressed();
		this.cursorOffset = recorder.getCursorOffset();
		this.videoTrack = recorder.videoTrackId;
		this.prevScreenCaptureTime = new Rational(startTime, 1000);
		this.startTime = startTime;
		
		Format screenFormat = recorder.getScreenFormat();
		this.videoImg = createVideoImage(screenFormat);
		this.videoGraphics = initializeVideoGraphics();
	}
	
	private BufferedImage createVideoImage(Format screenFormat) throws IOException {
		int depth = screenFormat.get(DepthKey, 24);
		return switch (depth) {
			case 24 -> new BufferedImage(captureArea.width, captureArea.height, BufferedImage.TYPE_INT_RGB);
			case 16 -> new BufferedImage(captureArea.width, captureArea.height, BufferedImage.TYPE_USHORT_555_RGB);
			case 8 -> new BufferedImage(captureArea.width, captureArea.height, BufferedImage.TYPE_BYTE_INDEXED, Colors.createMacColors());
			default -> throw new IOException("Unsupported color depth: " + depth);
		};
	}
	
	private Graphics2D initializeVideoGraphics() {
		Graphics2D graphics = videoImg.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
		graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		return graphics;
	}
	
	public void setFuture(ScheduledFuture<?> future) {
		this.future = future;
	}
	
	public void setStopTime(long newValue) {
		this.stopTime.set(newValue);
	}
	
	public long getStopTime() {
		return this.stopTime.get();
	}
	
	@Override
	public void run() {
		try {
			grabScreen();
		} catch (Throwable ex) {
			ex.printStackTrace();
			recorder.recordingFailed(ex);
		}
	}
	
	private void grabScreen() throws IOException, InterruptedException {
		BufferedImage previousScreenCapture = screenCapture;
		long timeBeforeCapture = System.currentTimeMillis();
		
		try {
			screenCapture = robot.createScreenCapture(captureArea);
		} catch (IllegalMonitorStateException e) {
			// Log the error and return instead of throwing an exception
			System.err.println("Screen capture failed: " + e.getMessage());
			return;
		}
		
		long timeAfterCapture = System.currentTimeMillis();
		previousScreenCapture = (previousScreenCapture == null) ? screenCapture : previousScreenCapture;
		videoGraphics.drawImage(previousScreenCapture, 0, 0, null);
		
		processMouseCaptures(timeBeforeCapture, timeAfterCapture, previousScreenCapture);
		
		if (timeBeforeCapture > getStopTime()) {
			future.cancel(false);
		}
	}
	
	private void processMouseCaptures(long timeBeforeCapture, long timeAfterCapture, BufferedImage previousScreenCapture) throws IOException, InterruptedException {
		Buffer buf = new Buffer();
		buf.format = new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO, EncodingKey, ENCODING_BUFFERED_IMAGE);
		
		boolean hasMouseCapture = processMouseCapturesWithCursor(timeBeforeCapture, timeAfterCapture, previousScreenCapture, buf);
		
		if (!hasMouseCapture && shouldCaptureFrame(timeAfterCapture)) {
			captureFrameWithoutMouseMovement(timeAfterCapture, buf);
		}
	}
	
	private boolean processMouseCapturesWithCursor(long timeBeforeCapture, long timeAfterCapture, BufferedImage previousScreenCapture, Buffer buf) throws IOException, InterruptedException {
		boolean hasMouseCapture = false;
		if (mouseFormat != null && mouseFormat.get(FrameRateKey).intValue() > 0) {
			while (!mouseCaptures.isEmpty() && shouldProcessMouseCapture(timeAfterCapture)) {
				Buffer mouseCapture = mouseCaptures.poll();
				if (isValidMouseCapture(mouseCapture, timeBeforeCapture)) {
					hasMouseCapture = true;
					processMouseCapture(mouseCapture, previousScreenCapture, buf);
				}
			}
		}
		return hasMouseCapture;
	}
	
	private boolean shouldProcessMouseCapture(long timeAfterCapture) {
		return mouseCaptures.peek().timeStamp.compareTo(new Rational(timeAfterCapture, 1000)) < 0;
	}
	
	private boolean isValidMouseCapture(Buffer mouseCapture, long timeBeforeCapture) {
		return Objects.requireNonNull(mouseCapture).timeStamp.compareTo(prevScreenCaptureTime) > 0
		       && mouseCapture.timeStamp.compareTo(new Rational(timeBeforeCapture, 1000)) < 0
		       && mouseCapture.timeStamp.compareTo(new Rational(getStopTime(), 1000)) <= 0;
	}
	
	private void processMouseCapture(Buffer mouseCapture, BufferedImage previousScreenCapture, Buffer buf) throws IOException, InterruptedException {
		Point mcp = (Point) mouseCapture.data;
		prevMousePressed = (Boolean) mouseCapture.header;
		prevDrawnMouseLocation.setLocation(mcp.x - captureArea.x, mcp.y - captureArea.y);
		Point p = prevDrawnMouseLocation;
		
		drawCursor(p);
		writeBuffer(buf, mouseCapture.timeStamp, p);
		eraseCursor(p, previousScreenCapture);
	}
	
	private void drawCursor(Point p) {
		BufferedImage cursorToDraw = prevMousePressed ? cursorImgPressed : cursorImg;
		videoGraphics.drawImage(cursorToDraw, p.x + cursorOffset.x, p.y + cursorOffset.y, null);
	}
	
	private void writeBuffer(Buffer buf, Rational timestamp, Point p) throws IOException, InterruptedException {
		buf.clearFlags();
		buf.data = videoImg;
		buf.sampleDuration = timestamp.subtract(prevScreenCaptureTime);
		buf.timeStamp = prevScreenCaptureTime.subtract(new Rational(startTime, 1000));
		buf.track = videoTrack;
		buf.sequenceNumber = sequenceNumber++;
		buf.header = p.x == Integer.MAX_VALUE ? null : p;
		recorder.write(buf);
		prevScreenCaptureTime = timestamp;
	}
	
	private void eraseCursor(Point p, BufferedImage previousScreenCapture) {
		videoGraphics.drawImage(previousScreenCapture,
				p.x + cursorOffset.x, p.y + cursorOffset.y,
				p.x + cursorOffset.x + cursorImg.getWidth() - 1, p.y + cursorOffset.y + cursorImg.getHeight() - 1,
				p.x + cursorOffset.x, p.y + cursorOffset.y,
				p.x + cursorOffset.x + cursorImg.getWidth() - 1, p.y + cursorOffset.y + cursorImg.getHeight() - 1,
				null);
	}
	
	private boolean shouldCaptureFrame(long timeAfterCapture) {
		return prevScreenCaptureTime.compareTo(new Rational(getStopTime(), 1000)) < 0;
	}
	
	private void captureFrameWithoutMouseMovement(long timeAfterCapture, Buffer buf) throws IOException, InterruptedException {
		Point p = prevDrawnMouseLocation;
		if (p != null) {
			drawCursor(p);
		}
		
		buf.data = videoImg;
		buf.sampleDuration = new Rational(timeAfterCapture, 1000).subtract(prevScreenCaptureTime);
		buf.timeStamp = prevScreenCaptureTime.subtract(new Rational(startTime, 1000));
		buf.track = videoTrack;
		buf.sequenceNumber = sequenceNumber++;
		buf.header = p.x == Integer.MAX_VALUE ? null : p;
		recorder.write(buf);
		prevScreenCaptureTime = new Rational(timeAfterCapture, 1000);
		
		if (p != null) {
			eraseCursor(p, screenCapture);
		}
	}
	
	@Override
	public void close() {
		videoGraphics.dispose();
		videoImg.flush();
	}
}