/* @(#)MovieControl.java
 * Copyright © 2007 Werner Randelshofer, Switzerland.
 * You may only use this file in compliance with the accompanying license terms. 
 */

package org.monte.media.movie;

import java.awt.Component;
import org.monte.media.player.Player;

/**
 * A @code MovieControl} can be used to control a movie using a user interface.
 *
 * @author Werner Randelshofer
 * @version 1.0 January 10, 2007 Created.
 */
public interface MovieControl {
    public void setPlayer(Player player);
    public void setVisible(boolean newValue);
    public Component getComponent();
    public void setEnabled(boolean b);
}
