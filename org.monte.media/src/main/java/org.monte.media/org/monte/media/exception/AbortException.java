/* @(#)AbortException.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.exception;

/**
 * This exception is thrown when the production of an image
 * has been aborted.
 *
 * @author Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version $Id$
 */
public class AbortException extends Exception {

    public static final long serialVersionUID = 1L;

    /**
     * Creates a new exception.
     */
    public AbortException() {
        super();
    }

    /**
     * Creates a new exception.
     */
    public AbortException(String message) {
        super(message);
    }
}
