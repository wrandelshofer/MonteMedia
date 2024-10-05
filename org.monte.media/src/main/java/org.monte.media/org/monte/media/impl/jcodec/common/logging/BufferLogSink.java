package org.monte.media.impl.jcodec.common.logging;

import java.util.LinkedList;
import java.util.List;

/**
 * This class is part of JCodec ( www.jcodec.org ) This software is distributed
 * under FreeBSD License
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
