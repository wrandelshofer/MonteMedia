/* @(#)AbstractCodec.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License.
 */
package org.monte.media.jmf.codec;

import javax.media.Buffer;
import javax.media.Codec;
import javax.media.Control;
import javax.media.ResourceUnavailableException;

/**
 * {@code AbstractCodec}.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public abstract class AbstractCodec implements Codec {

    protected String pluginName;
    protected Object[] controls = new Control[0];

    @Override
    public String getName() {
        return pluginName;
    }

    @Override
    public void open() throws ResourceUnavailableException {
    }

    @Override
    public void close() {
    }

    @Override
    public void reset() {
    }

    @Override
    public Object[] getControls() {
        return controls.clone();
    }

    @Override
    public Object getControl(String controlType) {
        return null;
    }

    protected static void setFlag(Buffer buf, int flag, boolean value) {
        int flags = buf.getFlags();
        buf.setFlags((value) ? flags | flag : flags & ~flag);
    }
    protected static boolean isSet(Buffer buf, int flag) {
        return (buf.getFlags()&flag)==flag;
        
    }
}
