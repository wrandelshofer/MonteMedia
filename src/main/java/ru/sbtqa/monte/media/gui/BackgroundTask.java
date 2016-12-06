/* @(#)BackgroundTask.java
 * Copyright © 2001-2010 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.gui;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.SwingUtilities.invokeLater;

/**
 * This is an abstract class that you can subclass to perform GUI-related work
 * in a dedicated event dispatcher.
 * 
 * This class is similar to SwingWorker but less complex.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public abstract class BackgroundTask implements Runnable {

    private Throwable error;  // see getError(), setError()

    /**
     * Calls #construct on the current thread and invokes #done on the AWT event
     * dispatcher thread.
     */
    @Override
    public final void run() {
        try {
            construct();
        } catch (Throwable e) {
            setError(e);
            invokeLater(() -> {
                failed(getError());
                finished();
            });
            return;
        }
        invokeLater(() -> {
            try {
                done();
            } finally {
                finished();
            }
        });
    }

    /**
     * Compute the value to be returned by the <code>get</code> method.
     *
     * @throws java.lang.Exception TODO
     */
    protected abstract void construct() throws Exception;

    /**
     * Called on the event dispatching thread (not on the worker thread) after
     * the <code>construct</code> method has returned without throwing an error.
     * 
     * The default implementation does nothing. Subclasses may override this
     * method to perform done actions on the Event Dispatch Thread.
     *
     */
    protected void done() {
    }

    /**
     * Called on the event dispatching thread (not on the worker thread) after
     * the <code>construct</code> method has thrown an error.
     * 
     * The default implementation prints a stack trace. Subclasses may override
     * this method to perform failure actions on the Event Dispatch Thread.
     *
     * @param error The error thrown by construct.
     */
    protected void failed(Throwable error) {
        showMessageDialog(null, error.getMessage() == null ? error.toString() : error.getMessage(), "Error", ERROR_MESSAGE);
        error.printStackTrace();
    }

    /**
     * Called on the event dispatching thread (not on the worker thread) after
     * the <code>construct</code> method has finished and after done() or
     * failed() has been invoked.
     * 
     * The default implementation does nothing. Subclasses may override this
     * method to perform completion actions on the Event Dispatch Thread.
     */
    protected void finished() {
    }

    /**
     * Get the error produced by the worker thread, or null if it hasn't thrown
     * one.
     *
     * @return TODO
     */
    protected synchronized Throwable getError() {
        return error;
    }

    /**
     * Set the error thrown by constrct.
     */
    private synchronized void setError(Throwable x) {
        error = x;
    }

    /**
     * Starts the Worker on an internal worker thread.
     */
    public void start() {
        new Thread(this).start();
    }
}
