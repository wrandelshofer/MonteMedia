/*
 * @(#)StateListener.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.player;

import java.util.EventListener;

/// Event for state changes.
///
/// @author Werner Randelshofer
public interface StateListener
        extends EventListener {

    public void stateChanged(StateEvent event);
}
