/* @(#)PlayerControl.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.player;

import java.awt.Component;
import org.monte.media.player.Player;

/**
 * A @code PlayerControl} can be used to control a movie using a user interface.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public interface PlayerControl {
    public void setPlayer(Player player);
    public void setVisible(boolean newValue);
    public Component getComponent();
    public void setEnabled(boolean b);
}
