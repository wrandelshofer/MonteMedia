/* @(#)Animator.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.player;

import javax.swing.event.ChangeListener;
import org.monte.media.av.Interpolator;
/**
 * Animator executes multiple Interpolator's on a worker thread.
 *
 * @author  Werner Randelshofer
 * @version $Id$
 */
public interface Animator extends Runnable {
    /**
     * Set the lock object, on which the animator synchronizes while
     * animating the interpolators.
     */
    public void setLock(Object lock);
    public boolean isActive();
    public void start();
    
    public void stop();
    
    /**
     * Dispatches an interpolator for the animation thread.
     * This will launch the animation thread if it is not already active.
     */
    public void dispatch(Interpolator interpolator);
    
    public void animateStep();
    
    public void run();
    public void addChangeListener(ChangeListener listener);
    
    public void removeChangeListener(ChangeListener listener);
    
    public boolean isSynchronous();
}
