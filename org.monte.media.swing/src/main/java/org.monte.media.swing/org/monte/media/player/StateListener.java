/*
 * @(#)Main.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.player;

import java.util.EventListener;

/**
 * Event for state changes.
 *
 * @author Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 */
public interface StateListener
        extends EventListener {

    public void stateChanged(StateEvent event);
}
