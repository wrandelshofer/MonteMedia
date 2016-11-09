/* @(#)StateEvent.java
 * Copyright © 1999 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media;

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
