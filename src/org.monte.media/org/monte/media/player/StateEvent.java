/* @(#)StateEvent.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.player;

import java.util.EventObject;
/**
 * Event for state changes.
 *
 * @author  Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version    1.0  1999-10-19
 */
public class StateEvent
extends EventObject {
    private final static long serialVersionUID = 1L;
    /**
     * State.
     */
    private int state_;
    
    public StateEvent(Object source, int state) {
        super(source);
        state_ = state;
    }
    
    public int getNewState() {
        return state_;
    }
    
    public String toString() {
        return getClass().getName() + "[source=" + getSource() + ",state=" + state_ + "]";
        
    }
}
