/*
 * @(#)BufferLogSink.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.h264.impl.jcodec.common.logging;

import java.util.LinkedList;
import java.util.List;

/// References:
///
/// JCodecProject. Copyright 2008-2019 JCodecProject.
/// : [BSD 2-Clause License.](https://github.com/jcodec/jcodec/blob/7e5283408a75c3cdbefba98a57d546e170f0b7d0/LICENSE)
/// : [github.com](https://github.com/jcodec/jcodec)
///
///
/// Just stores log messages to be extracted at later point
///
/// @author The JCodec project
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
