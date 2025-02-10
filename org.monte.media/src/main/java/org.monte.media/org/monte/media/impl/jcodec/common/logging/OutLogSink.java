package org.monte.media.impl.jcodec.common.logging;

import org.monte.media.impl.jcodec.common.tools.MainUtils;
import org.monte.media.impl.jcodec.common.tools.MainUtils.ANSIColor;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import static org.monte.media.impl.jcodec.common.tools.MainUtils.colorString;

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
 * Outputs messages to standard output
 *
 * @author The JCodec project
 */
public class OutLogSink implements LogSink {

    private static String empty = "                                                                                                                                                                                                                                                ";

    public static class SimpleFormat implements MessageFormat {
        private String fmt;
        private static Map<LogLevel, ANSIColor> colorMap = new HashMap<LogLevel, MainUtils.ANSIColor>();

        static {
            colorMap.put(LogLevel.DEBUG, ANSIColor.BROWN);
            colorMap.put(LogLevel.INFO, ANSIColor.GREEN);
            colorMap.put(LogLevel.WARN, ANSIColor.MAGENTA);
            colorMap.put(LogLevel.ERROR, ANSIColor.RED);
        }

        ;

        public SimpleFormat(String fmt) {
            this.fmt = fmt;
        }

        @Override
        public String formatMessage(Message msg) {
            String str = fmt.replace("#level", String.valueOf(msg.getLevel()))
                    .replace("#color_code", String.valueOf(30 + colorMap.get(msg.getLevel()).ordinal()))
                    .replace("#class", msg.getClassName()).replace("#method", msg.getMethodName())
                    .replace("#file", msg.getFileName()).replace("#line", String.valueOf(msg.getLineNumber()))
                    .replace("#message", msg.getMessage());
            return str;
        }
    }

    ;

    public static SimpleFormat DEFAULT_FORMAT = new SimpleFormat(
            colorString("[#level]", "#color_code") + MainUtils.bold("\t#class.#method (#file:#line):") + "\t#message");

    public static OutLogSink createOutLogSink() {
        return new OutLogSink(System.out, DEFAULT_FORMAT, LogLevel.INFO);
    }

    private PrintStream out;
    private MessageFormat fmt;
    private LogLevel minLevel;

    public OutLogSink(PrintStream out, MessageFormat fmt, LogLevel minLevel) {
        this.out = out;
        this.fmt = fmt;
        this.minLevel = minLevel;
    }

    @Override
    public void postMessage(Message msg) {
        if (msg.getLevel().ordinal() < minLevel.ordinal())
            return;
        String str = fmt.formatMessage(msg);
        out.println(str);
    }

    public static interface MessageFormat {
        String formatMessage(Message msg);
    }
}