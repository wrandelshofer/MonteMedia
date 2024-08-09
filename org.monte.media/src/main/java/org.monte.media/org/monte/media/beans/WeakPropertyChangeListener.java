/*
 * @(#)WeakPropertyChangeListener.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

/**
 * Property change listener that holds weak reference to a
 * target property change listener.  If the weak reference
 * becomes null (meaning the delegate has been GC'ed) then this
 * listener will remove itself from any beans that it receives
 * events from.  It isn't perfect, but it's a lot better than
 * nothing... and presumably beans that no longer send out events
 * probably don't care if their listeners weren't properly cleaned
 * up.
 * <p>
 * Design pattern: Proxy.
 *
 * @author Paul Speed
 */
public class WeakPropertyChangeListener implements PropertyChangeListener {
    private WeakReference<PropertyChangeListener> weakRef;

    public WeakPropertyChangeListener(PropertyChangeListener target) {
        this.weakRef = new WeakReference<>(target);
    }

    /**
     * Method that can be subclassed to provide additional remove
     * support.  Default implementation only supports StandardBeans.
     */
    protected void removeFromSource(PropertyChangeEvent event) {
        // Remove ourselves from the source
        Object src = event.getSource();
        try {
            src.getClass().getMethod("removePropertyChangeListener", new Class<?>[]{PropertyChangeListener.class}).invoke(src, this);
        } catch (Exception ex) {
            InternalError ie = new InternalError("Could not remove WeakPropertyChangeListener from " + src + ".");
            ie.initCause(ex);
            throw ie;
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        PropertyChangeListener listener = weakRef.get();
        if (listener == null) {
            removeFromSource(event);
            return;
        }
        listener.propertyChange(event);
    }

    /**
     * Returns the target of this proxy. Returns null if the target has been
     * garbage collected.
     *
     * @return The target or null.
     */
    public PropertyChangeListener getTarget() {
        return weakRef.get();
    }

    @Override
    public String toString() {
        return super.toString() + "[" + weakRef.get() + "]";
    }
}
