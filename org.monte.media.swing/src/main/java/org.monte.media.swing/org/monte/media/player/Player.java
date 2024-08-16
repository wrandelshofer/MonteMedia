/*
 * @(#)Player.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.player;

import javax.swing.BoundedRangeModel;
import javax.swing.event.ChangeListener;
import java.awt.Component;
import java.beans.PropertyChangeListener;

/**
 * {@code Player} is a media handler for rendering and controlling time based
 * media data.
 *
 * @author Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 */
public interface Player
        extends org.monte.media.av.Player, StateModel {


  /**
   * Sets the audio enabled state.
   */
  void setAudioEnabled(boolean b);

  /**
   * Returns true if audio is enabled.
   */
  boolean isAudioEnabled();

  /**
   * Returns true if audio is available.
   */
  boolean isAudioAvailable();



  /**
   * Adds a listener that wants to be notified about
   * state changes of the player.
   */
  void addStateListener(StateListener listener);

  /**
   * Removes a listener.
   */
  void removeStateListener(StateListener listener);

  /**
   * Adds a listener that wants to be notified about
   * state changes of the player.
   */
  void addChangeListener(ChangeListener listener);

  /**
   * Removes a listener.
   */
  void removeChangeListener(ChangeListener listener);

  /**
   * Adds a listener that wants to be notified about
   * property changes of the player.
   */
  void addPropertyChangeListener(PropertyChangeListener listener);

  /**
   * Removes a listener.
   */
  void removePropertyChangeListener(PropertyChangeListener listener);

  /**
   * Gets the model representing the timeline of the player.
   */
  BoundedRangeModel getTimeModel();

  /**
   * Gets the model representing the realizing progress of
   * the player.
   */
  BoundedRangeModel getCachingModel();

  /**
   * Returns true when the player has completely cached all movie data.
   * This player informs all property change listeners, when the value of this
   * property changes. The name of the property is 'cached'.
   */
  boolean isCached();

    Component getVisualComponent();

    Component getControlPanelComponent();

    long getTotalDuration();


}
