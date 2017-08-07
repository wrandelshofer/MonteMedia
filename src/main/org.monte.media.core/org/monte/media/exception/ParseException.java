/* @(#)ParseException.java
 * Copyright © 1999-2012 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.exception;

/**
 * Exception thrown by IFFParse.
 * 
 * @author  Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version  $Id: ParseException.java 364 2016-11-09 19:54:25Z werner $
 */
public class ParseException extends Exception {

    public static final long serialVersionUID = 1L;

    public ParseException(String message) {
        super(message);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
