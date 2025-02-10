package org.monte.media.impl.jcodec.common.logging;

import java.util.LinkedList;
import java.util.List;

/**
 * References:
 * <p>
 * This code has been derived from JCodecProject.
 * <dl>
 *     <dt>JCodecProject. Copyright 2008-2019 JCodecProject.
 *     <br><a href="https://github.com/jcodec/jcodec/blob/7e5283408a75c3cdbefba98a57d546e170f0b7d0/LICENSE">BSD 2-Clause License.</a></dt>
 *     <dd><a href="https://github.com/jcodec/jcodec">github.com</a></dd>
 * </dl>
 *
 * <p>
 * Just stores log messages to be extracted at later point
 *
 * @author The JCodec project
 */
public class BufferLogSink implements LogSink {

    private List<Message> messages;

    public BufferLogSink() {
        this.messages = new LinkedList<Message>();
    }

    @Override
    public void postMessage(Message msg) {
        messages.add(msg);
    }

    public List<Message> getMessages() {
        return messages;
    }
}
