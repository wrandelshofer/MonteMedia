/* @(#)ParseException.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License.
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
