/* @(#)SequentialDispatcher.java
 * Copyright © 2001-2013 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.concurrent;

/**
 * Processes Runnable objects sequentially on a processor thread.
 * The order in which the runnable objects are processed is 
 * the same in which they were added to the dispatcher.
 * <p>
 * Design pattern used: Acceptor
 * Role in design pattern: EventCollector and EventProcessor
 *
 * @author  Werner Randelshofef
 * @version $Id: SequentialDispatcher.java 364 2016-11-09 19:54:25Z werner $
 */
public class SequentialDispatcher extends EventLoop<Runnable> {
    /**
     * Creates new SequentialDispatcher which processes Runnable objects
     * at java.lang.Thread.NORM_PRIORITY.
     */
    public SequentialDispatcher() {
    }
    /**
     * Creates a new SequentialDispatcher which processes Runnable Objects
     * at the desired thread priority.
     *
     * @param priority The Thread priority of the event processor.
     */
    public SequentialDispatcher(int priority) {
        super(priority);
    }

    /**
     * This method processes an event on the event processor thread.
     *
     * @param event An event from the queue.
     */
    protected void processEvent(Runnable event) {
        event.run();
    }
    
    /**
     * Queues the Runnable object for later execution on the
     * processor thread.
     */
    public void dispatch(Runnable r) {
        collectEvent(r);
    }
}
