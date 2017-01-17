/* @(#)Player.java
 * Copyright © 1999-2009 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import javax.swing.BoundedRangeModel;
import javax.swing.event.*;

/**
 * {@code Player} is a media handler for rendering and controlling time based
 * media data.
 *
 * @author Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version 1.4 2009-12-25 Methods for color cycling added.
 * <br>1.3 2003-04-21 Method setAudioEnabled() and isAudioEnabled() added.
 * <br>1.2 2002-02-06 ChangeListener methods added.
 * <br>1.1 2000-10-02 Methods #setPaused and #isPaused removed.
 * <br> 1.0 1999-10-19
 */
public interface Player
      extends StateModel {

    /**
     * A Player in the UNREALIZED state has been instantiated, but does not yet
     * know anything about its media. When a media Player is first created, its
     * unrealized.
     */
    public final static int UNREALIZED = 0;
    /**
     * When realize is called, a Player moves from the UNREALIZED state into the
     * REALIZING state. A realizing player is in the process of determining its
     * resource requirements. During realization, a Player acquires the
     * resources that it only needs to acquire once. These might include
     * rendering resources other than exclusive-use resources. (Exclusive-use
     * resources are limited resources such as particular hardware devices that
     * can only be used by one Player at a time; such resources are qcquired
     * during PREFETCHING.) A realizing Player often downlaods assets over the
     * net.
     */
    public final static int REALIZING = 1;
    /**
     * When a Player finishes realizing it moves into the REALIZED state. A
     * realized Player knows what resoures it needs and information about the
     * type of media it is to present. Because a realized Player knows how to
     * render its data, it can provide visual components and controls. Its
     * connections to other objects in the system are in place, but it does noct
     * own any resources that would prevent another Player from starting.
     */
    public final static int REALIZED = 2;
    /**
     * When prefetch is called, a Player moves from the Realized state into the
     * Prefetching state. A Prefetching Player is preparing to present its
     * media. During this phase, the Player preloads its media data, obtains
     * exclusive-use resources, and anything else it needs to do to prepare
     * itself to play. Prefetching might have to recur if a Player's media
     * presentation is repositioned, or if a change in the Player's rate
     * requires that additional buffers be acquired or alternate processing take
     * place.
     */
    public final static int PREFETCHING = 3;
    /**
     * When a Player finishes Prefetching, it moves into the Prefetched state. A
     * Prefetched Player is ready to be started; it is as ready to play as it
     * can be without actually being Started.
     */
    public final static int PREFETCHED = 4;
    /**
     * Calling start puts a Player into the Started state. A Started Player's
     * time-base time and media time are mapped and its clock is running, though
     * the Player might be waiting for a particular time to begin presenting its
     * media data.
     */
    public final static int STARTED = 5;

    /**
     * A player with this state has been explicitly closed or has encountered an
     * error and can not be used any more.
     */
    public final static int CLOSED = -1;

    /**
     * Sets the audio enabled state.
     *
     * @param b TODO
     */
    public void setAudioEnabled(boolean b);

    /**
     * Returns true if audio is enabled.
     *
     * @return TODO
     */
    public boolean isAudioEnabled();

    /**
     * Returns true if audio is available.
     *
     * @return TODO
     */
    public boolean isAudioAvailable();

    /**
     * Gets the current state of the player.
     *
     * @return TODO
     */
    public int getState();

    /**
     * Gets the target state.
     *
     * @return TODO
     */
    public int getTargetState();

    /**
     * Sets the target state we want the player to be in.
     *
     * @param state TODO
     */
    public void setTargetState(int state);

    /**
     * Initiates the following asynchronous state transitions: unrealized
     * -{@literal {@literal >}} realizing -{@literal >} realized realizing
     * -{@literal >} realized realized closed -{@literal >} throws
     * IllegalStateException
     */
    public void realize();

    /**
     * Initiates the following asynchronous state transitions: unrealized
     * -{@literal >} realizing -{@literal >} realized -{@literal >} prefetching
     * -{@literal >} prefetched realizing -{@literal >} realized -{@literal >}
     * prefetching -{@literal >} prefetched realized -{@literal >} prefetching
     * -{@literal >} prefetched prefetching -{@literal >} prefetched prefetched
     * closed -{@literal >} throws IllegalStateException
     */
    public void prefetch();

    /**
     * Initiates the following asynchronous state transitions: realizing
     * -{@literal >} unrealized prefetching -{@literal >} realized prefetched
     * -{@literal >} realized realized started -{@literal >} throws
     * IllegalStateException closed -{@literal >} throws IllegalStateException
     */
    public void deallocate();

    /**
     * Initiates the following asynchronous state transitions: unrealized
     * -{@literal >} realizing -{@literal >} realized -{@literal >} prefetching
     * -{@literal >} prefetched -{@literal >} started realizing -{@literal >}
     * realized -{@literal >} prefetching -{@literal >} prefetched -{@literal >}
     * started realized -{@literal >} prefetching -{@literal >} prefetched
     * -{@literal >} started prefetching -{@literal >} prefetched -{@literal >}
     * started prefetched -{@literal >} started started closed -{@literal >}
     * throws IllegalStateException
     */
    public void start();

    /**
     * Initiates the following asynchronous state transitions: started
     * -{@literal >} prefetched unrealized realizing prefetching prefetched
     * closed -{@literal >} throws IllegalStateException
     */
    public void stop();

    /**
     * Initiates the following asynchronous state transitions: any state
     * -{@literal >} closed
     */
    public void close();

    /**
     * Adds a listener that wants to be notified about state changes of the
     * player.
     *
     * @param listener TODO
     */
    public void addStateListener(StateListener listener);

    /**
     * Removes a listener.
     *
     * @param listener TODO
     */
    public void removeStateListener(StateListener listener);

    /**
     * Adds a listener that wants to be notified about state changes of the
     * player.
     *
     * @param listener TODO
     */
    public void addChangeListener(ChangeListener listener);

    /**
     * Removes a listener.
     *
     * @param listener TODO
     */
    public void removeChangeListener(ChangeListener listener);

    /**
     * Adds a listener that wants to be notified about property changes of the
     * player.
     *
     * @param listener TODO
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes a listener.
     *
     * @param listener TODO
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Gets the model representing the time line of the player.
     *
     * @return TODO
     */
    public BoundedRangeModel getTimeModel();

    /**
     * Gets the model representing the realizing progress of the player.
     *
     * @return TODO
     */
    public BoundedRangeModel getCachingModel();

    /**
     * Returns true when the player has completely cached all movie data. This
     * player informs all property change listeners, when the value of this
     * property changes. The name of the property is 'cached'.
     *
     * @return TODO
     */
    public boolean isCached();

    public Component getVisualComponent();

    public Component getControlPanelComponent();

    public long getTotalDuration();

    /**
     * Returns true when the target state of the player is equal to STARTED.
     *
     * @return TODO
     */
    public boolean isActive();
}
