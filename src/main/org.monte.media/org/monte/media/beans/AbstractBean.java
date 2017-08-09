/* @(#)AbstractBean.java
 * Copyright © 2004 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.beans;

import java.beans.*;

/**
 * Abstract class for models that have to support property change listeners.<p>
 * Implements the methods required for adding and removing property change
 * listeners.
 *
 * @author Werner Randelshofer
 * @version 1.1 2004-01-18 
 * <br>1.0 2001-08-04
 */
public class AbstractBean extends Object implements java.io.Serializable {
    private final static long serialVersionUID = 1L;
	protected PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);

	/**
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(listener);
	}
	/**
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(String, PropertyChangeListener)
	 */
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener( propertyName, listener);
	}
	/**
	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(listener);
	}
	/**
	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(String, PropertyChangeListener)
	 */
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(propertyName, listener);
	}
    
    protected void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
            propertySupport.firePropertyChange(propertyName, oldValue, newValue);
    }
    protected void firePropertyChange(String propertyName, int oldValue, int newValue) {
            propertySupport.firePropertyChange(propertyName, oldValue, newValue);
    }
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
            propertySupport.firePropertyChange(propertyName, oldValue, newValue);
    }
}