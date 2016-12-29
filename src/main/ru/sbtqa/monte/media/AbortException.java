/* @(#)AbortException.java
 * Copyright © 1999-2012 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media;

/**
 * This exception is thrown when the production of an image
 * has been aborted.
 *
 * @author  Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 *
 * @version  $Id: AbortException.java 364 2016-11-09 19:54:25Z werner $
 */
public class AbortException extends Exception {

    public static final long serialVersionUID = 1L;

    /**
    Creates a new exception.
     */
    public AbortException() {
        super();
    }

    /**
    Creates a new exception.
    
     */
    public AbortException(String message) {
        super(message);
    }
}
