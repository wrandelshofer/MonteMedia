/* @(#)AVIMovie.java
 * Copyright © 2013 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.avi;

import org.monte.media.av.AbstractMovie;
import org.monte.media.av.Format;
import org.monte.media.av.MovieReader;
import org.monte.media.math.Rational;

/**
 * {@code AVIMovie}.
 *
 * FIXME - Implement me.
 * 
 * @author Werner Randelshofer
 * @version $Id$
 */
public class AVIMovie extends AbstractMovie {
    private final static long serialVersionUID = 1L;

    @Override
    public Rational getDuration() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setInsertionPoint(Rational seconds) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Rational getInsertionPoint() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Rational getSelectionStart() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setSelectionStart(Rational in) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Rational getSelectionEnd() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setSelectionEnd(Rational out) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long timeToSample(int track, Rational seconds) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Rational sampleToTime(int track, long sample) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getTrackCount() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Format getFormat(int track) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Format getFileFormat() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MovieReader getReader() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
