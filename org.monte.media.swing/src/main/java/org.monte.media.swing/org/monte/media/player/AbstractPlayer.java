/*
 * @(#)AbstractPlayer.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.player;


import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Abstract base class for media players.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractPlayer extends org.monte.media.av.AbstractPlayer
        implements Player {

    /**
     * Listener support.
     */
    protected EventListenerList listenerList = new EventListenerList();

    /**
     * Support for property change listeners.
     */
    protected PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);


    /**
     * Creates a new instance.
     */
    public AbstractPlayer() {
    }

    @Override
    protected void fireErrorHappened(Throwable error) {

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
    protected void fireStateChanged(int oldState, int newState) {
        SwingUtilities.invokeLater(() -> {
            StateEvent stateEvent = null;
            ChangeEvent changeEvent = null;
            Object[] listeners = listenerList.getListenerList();
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == StateListener.class) {
                    // lazily create the event object
                    if (stateEvent == null) stateEvent = new StateEvent(this, oldState, newState);
                    ((StateListener) listeners[i + 1]).stateChanged(stateEvent);
                }
                if (listeners[i] == ChangeListener.class) {
                    // lazily create the event object
                    if (changeEvent == null) changeEvent = new ChangeEvent(this);
                    ((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
                }
            }
        });
    }


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


}
