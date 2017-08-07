/* @(#)AbstractCodec.java  1.0  2011-04-05
 * Copyright (c) 2011 Werner Randelshofer, Switzerland. 
 * You may only use this file in compliance with the accompanying license terms.
 */
package org.monte.media.jmf.codec;

import javax.media.Buffer;
import javax.media.Codec;
import javax.media.Control;
import javax.media.Format;
import javax.media.ResourceUnavailableException;

/**
 * {@code AbstractCodec}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-04-05 Created.
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
