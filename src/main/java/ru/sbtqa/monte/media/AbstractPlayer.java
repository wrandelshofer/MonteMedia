/* @(#)AbstractPlayer.java
 * Copyright © 1999-2011 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media;


import java.beans.*;
import javax.swing.event.*;
import ru.sbtqa.monte.media.concurrent.SequentialDispatcher;

/**
 * Generic interface for media players.
 *
 * @author  Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version 3.1 2011-02-17 Merged with CubeTwister.
 * <br>2.1 2009-12-25 Added support for color cycling.
 * <br>2.0.1 2009-11-23 Removed unused imports.
 * <br>2.0 2005-07-09 Uses now a SequentialDispatcher for multithreading
 * instead of individual threads. The SequentialDispatcher guarantees, that all
 * animation steps are performed sequentially. This removes the need for clients
 * to wait until a specific state has been reached, before they can request
 * another state from the player.
 * <br>1.1 2002-02-06 Support for ChangeListeners added.
 * <br>1.0  1999-10-19
 */
public abstract class AbstractPlayer
implements Player, Runnable {
    /**
     * Current state of the player.
     * Note: Only method run() may change the value of
     * this variable.
     */
    private int state = UNREALIZED;
    
    /**
     * Target state of the player.
     */
    private int targetState = UNREALIZED;
    
    /**
     * Listener support.
     */
    protected EventListenerList listenerList = new EventListenerList();
    
    /** Support for property change listeners. */
    protected PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    
    /**
     * The dispatcher.
     */
    protected SequentialDispatcher dispatcher = new SequentialDispatcher();
    
    /**
     * Creates a new instance.
     */
    public AbstractPlayer() {
    }
    
    /**
     * Gets the current state of the player.
     */
    @Override
    public int getState() {
        return state;
    }
    
    /**
     * Gets the target state.
     */
    @Override
    public int getTargetState() {
        return targetState;
    }
    
    /**
     * Sets the desired target state.
     */
    @Override
    public void setTargetState(final int state) {
        synchronized (this) {
            if (targetState != CLOSED) {
                targetState = state;
                AbstractPlayer.this.notifyAll();
                dispatcher.dispatch(this);
            }
        }
    }
    
    /**
     * Initiates the following asynchronous
     * state transitions:
     *   unrealized -> realizing -> realized
     *                 realizing -> realized
     *                              realized
     *   started -> throws IllegalStateException
     *   closed -> throws IllegalStateException
     */
    @Override
    public void realize() {
        switch (getState()) {
            case CLOSED :
                throw new IllegalStateException("Realize closed player.");
                //  break; not reached
            case STARTED :
                throw new IllegalStateException("Realize started player.");
                //  break; not reached
        }
        setTargetState(REALIZED);
    }
    
    /**
     * Initiates the following asynchronous
     * state transitions:
     *   unrealized -> realizing -> realized -> prefetching -> prefetched
     *                 realizing -> realized -> prefetching -> prefetched
     *                              realized -> prefetching -> prefetched
     *                                          prefetching -> prefetched
     *                                                         prefetched
     *   started -> throws IllegalStateException
     *   closed -> throws IllegalStateException
     */
    public void prefetch() {
        switch (getState()) {
            case CLOSED :
                throw new IllegalStateException("Prefetch closed player.");
                //  break; not reached
            case STARTED :
                throw new IllegalStateException("Prefetch started player.");
                //  break; not reached
        }
        setTargetState(PREFETCHED);
    }
    
    /**
     * Initiates the following asynchronous
     * state transitions:
     *   realizing -> unrealized
     *   prefetching -> realized
     *   prefetched -> realized
     *   realized
     *   started -> throws IllegalStateException
     *   closed -> throws IllegalStateException
     */
    public void deallocate() {
        switch (getState()) {
            case CLOSED :
                throw new IllegalStateException("Deallocate closed player.");
                //  break; not reached
            case REALIZING :
                setTargetState(UNREALIZED);
                break;
            case PREFETCHING :
                setTargetState(REALIZED);
                break;
            case PREFETCHED :
                setTargetState(REALIZED);
                break;
            case STARTED :
                throw new IllegalStateException("Deallocate started player.");
                //   break; not reached
        }
    }
    
    
    /**
     * Initiates the following asynchronous
     * state transitions:
     * unrealized -> realizing -> realized -> prefetching -> prefetched -> started
     *               realizing -> realized -> prefetching -> prefetched -> started
     *                            realized -> prefetching -> prefetched -> started
     *                                        prefetching -> prefetched -> started
     *                                                       prefetched -> started
     *                                                                     started
     * closed -> throws IllegalStateException
     */
    public void start() {
        switch (getState()) {
            case CLOSED :
                throw new IllegalStateException("Can't start closed player.");
                //  break; not reached
        }
        setTargetState(STARTED);
    }
    
    /**
     * Initiates the following asynchronous
     * state transitions:
     * started -> prefetched
     * unrealized
     * realizing
     * prefetching
     * prefetched
     * closed -> throws IllegalStateException
     */
    public void stop() {
        switch (getState()) {
            case CLOSED :
                //throw new IllegalStateException("Stop closed player.");
                //allow stop on closed player
                break;
            case STARTED :
                setTargetState(PREFETCHED);
                break;
        }
    }
    
    /**
     * Initiates the following asynchronous
     * state transitions:
     * any state -> closed
     */
    public void close() {
        setTargetState(CLOSED);
    }
    
    /**
     * Adds a listener that wants to be notified about
     * state changes of the player.
     */
    public void addStateListener(StateListener l) {
        listenerList.add(StateListener.class, l);
    }
    
    /**
     * Removes a listener.
     */
    public void removeStateListener(StateListener l) {
        listenerList.remove(StateListener.class, l);
    }
    
    /**
     * Adds a listener who is interested in changes of this object.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }
    
    /**
     * Removes a previously registered listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }
    
    
    /**
     * Notifies all registered state listeners and
     * all registered change listeners.
     */
    protected void fireStateChanged(int newState) {
        StateEvent stateEvent = null;
        ChangeEvent changeEvent = null;
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -=2 ) {
            if (listeners[i] == StateListener.class) {
                // lazily create the event object
                if (stateEvent == null) stateEvent = new StateEvent(this, newState);
                ((StateListener)listeners[i+1]).stateChanged(stateEvent);
            }
            if (listeners[i] == ChangeListener.class) {
                // lazily create the event object
                if (changeEvent == null) changeEvent = new ChangeEvent(this);
                ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
            }
        }
    }
    /**
     * Notifies all registered change listeners.
     */
    protected void fireStateChanged() {
        StateEvent stateEvent = null;
        ChangeEvent changeEvent = null;
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -=2 ) {
            if (listeners[i] == ChangeListener.class) {
                // lazily create the event object
                if (changeEvent == null) changeEvent = new ChangeEvent(this);
                ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
            }
        }
    }
    /** Most of the real work goes here. We have to decide when
     *  to post events like EndOfMediaEvent and StopAtTimeEvent
     *  and TimeLineEvent.
     */
    public void run() {
        while (state != targetState) {
            if (targetState > state) {
                state++;
            } else {
                state = targetState;
            }
            fireStateChanged(state);
            
            switch (state) {
                case CLOSED :
                    doClosed();
                    break;
                case UNREALIZED :
                    doUnrealized();
                    break;
                case REALIZING :
                    doRealizing();
                    break;
                case REALIZED :
                    doRealized();
                    break;
                case PREFETCHING :
                    doPrefetching();
                    break;
                case PREFETCHED :
                    doPrefetched();
                    break;
                case STARTED :
                    doStarted();
                    setTargetState(PREFETCHED);
                    break;
            }
        }
    }
    
    /**
     * Does the work for the closed state.
     */
    abstract protected void doClosed();
    
    /**
     * Does the work for the unrealized state.
     */
    abstract protected void doUnrealized();
    
    /**
     * Does the work for the realizing state.
     */
    abstract protected void doRealizing();
    
    /**
     * Does the work for the realized state.
     */
    abstract protected void doRealized();
    
    /**
     * Does the work for the prefetching state.
     */
    abstract protected void doPrefetching();
    
    /**
     * Does the work for the prefetched state.
     */
    abstract protected void doPrefetched();
    
    /**
     * Does the work for the started state.
     */
    abstract protected void doStarted();
    
    /**
     * Adds a listener that wants to be notified about
     * state changes of the player.
     */
    public void addChangeListener(ChangeListener listener) {
        listenerList.add(ChangeListener.class, listener);
    }
    
    /**
     * Removes a listener.
     */
    public void removeChangeListener(ChangeListener listener) {
        listenerList.remove(ChangeListener.class, listener);
    }
    
    /**
     * Returns true when the target state of the player is equal to STARTED.
     */
    public boolean isActive() {
        return getTargetState() == STARTED;
    }

}
