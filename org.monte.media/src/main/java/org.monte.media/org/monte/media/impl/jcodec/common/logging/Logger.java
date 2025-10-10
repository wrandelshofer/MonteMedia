package org.monte.media.impl.jcodec.common.logging;

import java.util.LinkedList;
import java.util.List;

import static org.monte.media.impl.jcodec.common.logging.LogLevel.DEBUG;

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
 * JCodec has to be dependancy free, so it can run both on Java SE and Android
 * hence defining here our small logger that can be plugged into the logging
 * framework of choice on the target platform
 *
 * @author The JCodec project
 */
public class Logger {

    private static List<LogSink> stageSinks = new LinkedList<LogSink>();
    private static List<LogSink> sinks;

    public static void debug(String message) {
        message(LogLevel.DEBUG, message, null);
    }

    public static void debug(String message, Object... args) {
        message(LogLevel.DEBUG, message, args);
    }

    public static void info(String message) {
        message(LogLevel.INFO, message, null);
    }

    public static void info(String message, Object... args) {
        message(LogLevel.INFO, message, args);
    }

    public static void warn(String message) {
        message(LogLevel.WARN, message, null);
    }

    public static void warn(String message, Object... args) {
        message(LogLevel.WARN, message, args);
    }

    public static void error(String message) {
        message(LogLevel.ERROR, message, null);
    }

    public static void error(String message, Object... args) {
        message(LogLevel.ERROR, message, args);
    }

    private static void message(LogLevel level, String message, Object[] args) {
        if (Logger.globalLogLevel.ordinal() > level.ordinal()) {
            return;
        }
        if (sinks == null) {
            synchronized (Logger.class) {
                if (sinks == null) {
                    sinks = stageSinks;
                    stageSinks = null;
                    if (sinks.isEmpty())
                        sinks.add(OutLogSink.createOutLogSink());
                }
            }
        }
        Message msg;
        if (DEBUG.equals(globalLogLevel)) {
            StackTraceElement tr = Thread.currentThread().getStackTrace()[3];
            msg = new Message(level, tr.getFileName(), tr.getClassName(), tr.getMethodName(), tr.getLineNumber(),
                    message, args);
        } else {
            msg = new Message(level, "", "", "", 0, message, args);
        }
        for (LogSink logSink : sinks) {
            logSink.postMessage(msg);
        }
    }

    private static LogLevel globalLogLevel = LogLevel.INFO;

    public synchronized static void setLevel(LogLevel level) {
        globalLogLevel = level;
    }

    public synchronized static LogLevel getLevel() {
        return globalLogLevel;
    }


    public static void addSink(LogSink sink) {
        if (stageSinks == null)
            throw new IllegalStateException("Logger already started");
        stageSinks.add(sink);
    }
}