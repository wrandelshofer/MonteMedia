package org.monte.media.impl.jcodec.common.logging;

/**
 * Message.
 * <p>
 * References:
 * <p>
 * This code has been derived from JCodecProject.
 * <dl>
 *     <dt>JCodecProject. Copyright 2008-2019 JCodecProject.
 *     <br><a href="https://github.com/jcodec/jcodec/blob/7e5283408a75c3cdbefba98a57d546e170f0b7d0/LICENSE">BSD 2-Clause License.</a></dt>
 *     <dd><a href="https://github.com/jcodec/jcodec">github.com</a></dd>
 * </dl>
 */
public class Message {
    private LogLevel level;
    private String fileName;
    private String className;
    private int lineNumber;
    private String message;
    private String methodName;
    private Object[] args;

    public Message(LogLevel level, String fileName, String className, String methodName, int lineNumber, String message, Object[] args) {
        this.level = level;
        this.fileName = fileName;
        this.className = className;
        this.methodName = methodName;
        this.message = methodName;
        this.lineNumber = lineNumber;
        this.message = message;
        this.args = args;
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getFileName() {
        return fileName;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getMessage() {
        return message;
    }

    public Object[] getArgs() {
        return args;
    }
}