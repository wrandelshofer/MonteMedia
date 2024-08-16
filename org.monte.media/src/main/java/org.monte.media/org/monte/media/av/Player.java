/*
 * @(#)Player.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.av;

/**
 * Interface for a media player.
 */
public interface Player {
    /**
     * A Player in the UNREALIZED state has been instantiated, but
     * does not yet know anything about its media. When a media Player
     * is first created, it is unrealized.
     */
    int UNREALIZED = 0;
    /**
     * When realize is called, a Player moves from the UNREALIZED state
     * into the REALIZING state. A realizing player is in the process
     * of determining its resource requirements. During realization, a
     * Player acquires the resources that it only needs to acquire once.
     * These might include rendering resources other than exclusive-use
     * resources. (Exclusive-use resources are limited resources such
     * as particular hardware devices that can only be used by one Player
     * at a time; such resources are qcquired during PREFETCHING.) A
     * realizing Player often downlaods assets over the net.
     */
    int REALIZING = 1;
    /**
     * When a Player finishes realizing it moves into the REALIZED state.
     * A realized Player knows what resoures it needs and information about
     * the type of media it is to present. Because a realized Player knows
     * how to render its data, it can provide visual components and controls.
     * Its connections to other objects in the system are in place, but it
     * does not own any resources that would prevent another Player from
     * starting.
     */
    int REALIZED = 2;
    /**
     * When prefetch is called, a Player moves from the Realized state into
     * the Prefetching state. A Prefetching Player is preparing to present its
     * media. During this phase, the Player preloads its media data, obtains
     * exclusive-use resources, and anything else it needs to do to prepare
     * itself to play. Prefetching might have to recur if a Player's media
     * presentation is repositioned, or if a change in the Player's rate
     * requires that additional buffers be acquired or alternate processing
     * take place.
     */
    int PREFETCHING = 3;
    /**
     * When a Player finishes Prefetching, it moves into the Prefetched state. A
     * Prefetched Player is ready to be started; it is as ready to play as it can
     * be without actually being Started.
     */
    int PREFETCHED = 4;
    /**
     * Calling start puts a Player into the Started state. A Started Player's
     * time-base time and media time are mapped and its clock is running,
     * though the Player might be waiting for a particular time to begin
     * presenting its media data.
     */
    int STARTED = 5;

    /**
     * A player with this state has been explicitly closed or has
     * encountered an error and can not be used anymore.
     */
    int CLOSED = -1;

    /**
     * Initiates the following asynchronous
     * state transitions:
     * unrealized → realizing → realized
     * realizing → realized
     * realized
     * closed → throws IllegalStateException
     */
    void realize();

    /**
     * Initiates the following asynchronous
     * state transitions:
     * unrealized → realizing → realized → prefetching → prefetched
     * realizing → realized → prefetching → prefetched
     * realized → prefetching → prefetched
     * prefetching → prefetched
     * prefetched
     * closed → throws IllegalStateException
     */
    void prefetch();

    /**
     * Initiates the following asynchronous
     * state transitions:
     * realizing → unrealized
     * prefetching → realized
     * prefetched → realized
     * realized
     * started → throws IllegalStateException
     * closed → throws IllegalStateException
     */
    void deallocate();

    /**
     * Initiates the following asynchronous
     * state transitions:
     * unrealized → realizing → realized → prefetching → prefetched → started
     * realizing → realized → prefetching → prefetched → started
     * realized → prefetching → prefetched → started
     * prefetching → prefetched → started
     * prefetched → started
     * started
     * closed → throws IllegalStateException
     */
    void start();

    /**
     * Initiates the following asynchronous
     * state transitions:
     * started → prefetched
     * unrealized
     * realizing
     * prefetching
     * prefetched
     * closed → throws IllegalStateException
     */
    void stop();

    /**
     * Initiates the following asynchronous
     * state transitions:
     * any state → closed
     */
    void close();

    /**
     * Gets the current state of the player.
     */
    int getState();

    /**
     * Gets the target state.
     */
    int getTargetState();

    /**
     * Sets the target state we want the player to be in.
     */
    void setTargetState(int state);

    /**
     * Returns true when the target state of the player is equal to STARTED.
     */
    default boolean isActive() {
        return getTargetState() == STARTED;
    }
}
