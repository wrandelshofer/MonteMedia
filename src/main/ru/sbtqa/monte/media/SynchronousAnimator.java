/**
 * @(#)SynchronousAnimator.java  1.0  Apr 28, 2008
 * Copyright © 2008 Werner Randelshofer
 * Hausmatt 10, CH-6405 Goldau, Switzerland
 * All rights reserved.
 *
 * The copyright of this software is owned by Werner Randelshofer. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Werner Randelshofer. For details see accompanying license terms. 
 */

package ru.sbtqa.monte.media;

import java.util.*;
import javax.swing.event.*;

/**
 * SynchronousAnimator.
 *
 * @author Werner Randelshofer
 * @version 1.0 Apr 28, 2008 Created.
 */
public class SynchronousAnimator implements Animator {
    protected EventListenerList listenerList = new EventListenerList();
    protected ChangeEvent changeEvent;
    private Object lock;
    private long currentTimeMillis;
    /**
     * List of active interpolators.
     * Implementation note: This vector is only accessed by the animationThread.
     */
    private ArrayList<Interpolator> activeInterpolators = new ArrayList<Interpolator>();
    /**
     * List of new interpolators.
     * Implementation note: The dispatcher thread adds items to this list, the
     * animationThread removes items.
     * This queue is used to synchronize the dispatcher thread with the animation
     * thread.
     * Note: the dispatcher thread is not necesseraly the  Event Dispatcher
     * thread. The dispatcher thread is any thread which dispatches interpolators.
     */
    private ArrayList<Interpolator> newInterpolators = new ArrayList<Interpolator>();

    public void setLock(Object lock) {
        this.lock = lock;
    }

    public boolean isActive() {
        return ! newInterpolators.isEmpty() || ! activeInterpolators.isEmpty();
    }

    public void start() {
    }

    public void stop() {
        newInterpolators.clear();
        activeInterpolators.clear();
    }
    
    public void setTime(long currentTimeMillis) {
        this.currentTimeMillis = currentTimeMillis;
    }

    public void dispatch(Interpolator interpolator) {
        newInterpolators.add(interpolator);
    }

    public void animateStep() {
        long now = currentTimeMillis;
        
        // Enqueue new interpolators into the activeInterpolators list
        // Avoid enqueuing new interpolators which must be run sequentally
        // with active interpolators.
        OuterLoop: for (int i=0; i < newInterpolators.size(); i++) {
            Interpolator candidate = newInterpolators.get(i);
            boolean isEnqueueable = true;
            for (int j=0; j < i; j++) {
                Interpolator before = newInterpolators.get(j);
                if (candidate.isSequential(before)) {
                    isEnqueueable = false;
                    break;
                }
            }
            if (isEnqueueable) {
                for (int j=0; j < activeInterpolators.size(); j++) {
                    Interpolator before = activeInterpolators.get(j);
                    if (candidate.replaces(before)) {
                        before.finish(now);
                    }
                    if (candidate.isSequential(before)) {
                        isEnqueueable = false;
                        break;
                    }
                }
            }
            if (isEnqueueable) {
                candidate.initialize(now);
                activeInterpolators.add(candidate);
                if (newInterpolators.size() > 0) {
                    newInterpolators.remove(i--);
                }
            }
        }
        
        // Animate the active interpolators
        // Remove finished interpolators.
        for (int i=0; i < activeInterpolators.size(); i++) {
            Interpolator active = activeInterpolators.get(i);
            if (active.isFinished()) {
                activeInterpolators.remove(i--);
            } else if (active.isElapsed(now)) {
                active.finish(now);
                activeInterpolators.remove(i--);
            } else {
                active.interpolate(now);
            }
        }
    }

    public void run() {
    }

    public void addChangeListener(ChangeListener listener) {
        listenerList.add(ChangeListener.class, listener);
    }
    
    public void removeChangeListener(ChangeListener listener) {
        listenerList.remove(ChangeListener.class, listener);
    }
    /**
     * Notify all listeners that have registered interest for
     * notification on this event type.
     */
    protected void fireStateChanged() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==ChangeListener.class) {
                // Lazily create the event:
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
            }
        }
    }

    public boolean isSynchronous() {
        return true;
    }

}
