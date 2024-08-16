/*
 * @(#)StateEvent.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.player;

import java.util.EventObject;

/**
 * Event for state changes.
 *
 * @author Werner Randelshofer
 */
public class StateEvent
        extends EventObject {
    private final static long serialVersionUID = 2L;
    /**
     * The old State.
     */
    private final int oldState;
    /**
     * The new State.
     */
    private final int newState;

    public StateEvent(Object source, int oldState, int newState) {
        super(source);
        this.oldState = oldState;
        this.newState = newState;
    }

    public int getNewState() {
        return newState;
    }

    public int getOldState() {
        return oldState;
    }

    public String toString() {
        return getClass().getName() + "[source=" + getSource() + ",state=" + newState + "]";

    }
}
