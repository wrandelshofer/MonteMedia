/* @(#)StateListener.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License. */
package org.monte.media.player;

import java.util.EventListener;
/**
 * Event for state changes.
 *
 * @author  Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version    1.0  1999-10-19
 */
public interface StateListener
extends EventListener {

  public void stateChanged(StateEvent event);
}
