/*
 * @(#)PlayerControl.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.player;

import java.awt.*;

/**
 * A @code PlayerControl} can be used to control a movie using a user interface.
 *
 * @author Werner Randelshofer
 */
public interface PlayerControl {
    public void setPlayer(Player player);

    public void setVisible(boolean newValue);

    public Component getComponent();

    public void setEnabled(boolean b);
}
