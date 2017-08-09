/* @(#)AbstractMovie.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License.
 */
package org.monte.media.av;

import java.net.URI;
import org.monte.media.beans.AbstractBean;
import org.monte.media.math.Rational;

/**
 * {@code AbstractMovie}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-09-01 Created.
 */
public abstract class AbstractMovie extends AbstractBean implements Movie {
    private URI uri;
    private Rational playhead;
    private Rational in;
    private Rational out;

    @Override
    public URI getURI() {
        return uri;
    }
    public void setURI(URI newValue) {
        URI oldValue=uri;
        this.uri=newValue;
        firePropertyChange(URI_PROPERTY, oldValue, newValue);
    }    
    @Override
    public Rational getInsertionPoint() {
        return playhead;
    }

    @Override
    public void setInsertionPoint(Rational newValue) {
        Rational oldValue=this.playhead;
        this.playhead = newValue;
        firePropertyChange(INSERTION_POINT_PROPERTY, oldValue, newValue);
    }

    @Override
    public Rational getSelectionStart() {
        return in;
    }

    @Override
    public void setSelectionStart(Rational newValue) {
        Rational oldValue=in;
        this.in = newValue;
        firePropertyChange(SELECTION_START_PROPERTY, oldValue, newValue);
    }

    @Override
    public Rational getSelectionEnd() {
        return out;
    }

    @Override
    public void setSelectionEnd(Rational newValue) {
        Rational oldValue=out;
        this.out = newValue;
        firePropertyChange(SELECTION_END_PROPERTY, oldValue, newValue);
    }

}
