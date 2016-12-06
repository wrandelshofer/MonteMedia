/**
 *
 */
package ru.sbtqa.monte.iodemo;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import ru.sbtqa.monte.media.Buffer;
import ru.sbtqa.monte.media.BufferFlag;
import ru.sbtqa.monte.media.Codec;
import ru.sbtqa.monte.media.Format;
import ru.sbtqa.monte.media.FormatKeys;
import ru.sbtqa.monte.media.FormatKeys.MediaType;
import ru.sbtqa.monte.media.MovieReader;
import ru.sbtqa.monte.media.Registry;
import ru.sbtqa.monte.media.VideoFormatKeys;
import ru.sbtqa.monte.media.image.Images;

/**
 * {@code ReadAVIDemo}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2013-01-10 Created.
 */
public class ReadAVIDemo {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException TODO
     */
    public static void main(String[] args) throws IOException {
        final ArrayList<BufferedImage> frames = new ArrayList<BufferedImage>();
        final File f = new File("/Users/Shared/Developer/Java/MonteMedia/current/trunk/MonteMedia/avidemo-tscc8.avi");
        MovieReader in = Registry.getInstance().getReader(f);
        try {
            Format format = new Format(VideoFormatKeys.EncodingKey, VideoFormatKeys.ENCODING_BUFFERED_IMAGE);
            int track = in.findTrack(0, new Format(FormatKeys.MediaTypeKey, MediaType.VIDEO));
            if (track == -1) {
                throw new IOException("Movie has no video track.");
            }
            Codec codec = Registry.getInstance().getCodec(in.getFormat(track), format);
            if (codec == null) {
                throw new IOException("Can not decode video track.");
            }
            Buffer inBuf = new Buffer();
            Buffer codecBuf = new Buffer();
            do {
                in.read(track, inBuf);
                if (codec.process(inBuf, codecBuf) == Codec.CODEC_FAILED) {
                    System.out.println("Can not decode buffer " + inBuf);
                }
                if (!codecBuf.isFlag(BufferFlag.DISCARD)) {
                    frames.add(Images.cloneImage((BufferedImage) codecBuf.data));
                }
            } while (!inBuf.isFlag(BufferFlag.END_OF_MEDIA));
        } finally {
            in.close();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame fr = new JFrame(f.getName());
                fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                final JLabel label = new JLabel(new ImageIcon(frames.get(0)));
                final JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, frames.size() - 1, 0);

                slider.addChangeListener(new ChangeListener() {

                    @Override
                    public void stateChanged(ChangeEvent e) {
                        label.setIcon(new ImageIcon(frames.get(slider.getValue())));
                    }
                });

                fr.add(BorderLayout.CENTER, label);
                fr.add(BorderLayout.SOUTH, slider);
                fr.pack();
                fr.setVisible(true);
            }
        });
    }
}
