/* @(#)CommandlineRecorderMain.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License.
 */
package org.monte.demo.screenrecorder;

import org.monte.media.screenrecorder.ScreenRecorder;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;

/**
 * {@code CommandlineRecorderMain}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-08-05 Created.
 */
public class CommandlineRecorderMain {

    /**
     * FIXME - Add commandline arguments for recording time.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        GraphicsConfiguration gc = GraphicsEnvironment//
                .getLocalGraphicsEnvironment()//
                .getDefaultScreenDevice()//
                .getDefaultConfiguration();
        // FIXME - Implement me
        ScreenRecorder sr = new ScreenRecorder(
                gc/*,
                "QuickTime", 24,
                ScreenRecorder.CursorEnum.BLACK,
                30, 15,
                44100*/);
        sr.start();
        Thread.sleep(5000);
        sr.stop();
    }
}
