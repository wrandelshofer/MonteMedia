/*
 * @(#)Main.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.player;

/**
 * Generic interface for Objects with state.
 *
 * @author Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 */
public interface StateModel {
    /**
     * Adds a listener that wants to be notified about
     * state changes of the model.
     */
    public void addStateListener(StateListener listener);

    /**
     * Removes a listener.
     */
    public void removeStateListener(StateListener listener);

    /**
     * Returns the current state of the model.
     */
    public int getState();
}
