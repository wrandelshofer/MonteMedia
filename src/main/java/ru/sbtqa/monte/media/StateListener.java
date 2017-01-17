/* @(#)StateListener.java
 * Copyright © 1999 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media;

import java.util.EventListener;

/**
 * Event for state changes.
 *
 * @author Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version 1.0 1999-10-19
 */
public interface StateListener
      extends EventListener {

    public void stateChanged(StateEvent event);
}
