/*
 * @(#)AbstractPlayer.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.av;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Abstract base class for a media player.
 */
public abstract class AbstractPlayer implements Player {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition lockCondition = lock.newCondition();
    /**
     * The dispatcher.
     */
    protected ExecutorService dispatcher = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, AbstractPlayer.this + "-worker");
        }
    });
    /**
     * Current state of the player.
     * Note: Only method run() may change the value of
     * this variable.
     */
    private volatile int state = Player.UNREALIZED;

    /**
     * Target state of the player.
     */
    private volatile int targetState = Player.UNREALIZED;

    @Override
    public void realize() {
        switch (getState()) {
            case CLOSED:
                throw new IllegalStateException("Realize closed player.");
                //  break; not reached
            case STARTED:
                throw new IllegalStateException("Realize started player.");
                //  break; not reached
        }
        setTargetState(REALIZED);
    }


    @Override
    public void prefetch() {
        switch (getState()) {
            case CLOSED, STARTED -> {
                return;
            }
        }
        setTargetState(PREFETCHED);
    }

    @Override
    public void deallocate() {
        switch (getState()) {
            case CLOSED:
                throw new IllegalStateException("Deallocate closed player.");
                //  break; not reached
            case REALIZING:
                setTargetState(UNREALIZED);
                break;
            case PREFETCHING:
                setTargetState(REALIZED);
                break;
            case PREFETCHED:
                setTargetState(REALIZED);
                break;
            case STARTED:
                throw new IllegalStateException("Deallocate started player.");
                //   break; not reached
        }
    }

    @Override
    public void start() {
        switch (getState()) {
            case CLOSED:
                throw new IllegalStateException("Can't start closed player.");
                //  break; not reached
        }
        setTargetState(STARTED);
    }

    @Override
    public void stop() {
        switch (getState()) {
            case CLOSED:
                //throw new IllegalStateException("Stop closed player.");
                //allow stop on closed player
                break;
            case STARTED:
                setTargetState(PREFETCHED);
                break;
        }
    }

    @Override
    public void close() {
        setTargetState(CLOSED);
        dispatcher.shutdown();
    }

    @Override
    public int getState() {
        return state;
    }

    @Override
    public int getTargetState() {
        return targetState;
    }

    private static class Worker implements Runnable {
        private final AbstractPlayer player;
        private final int targetState;

        private Worker(AbstractPlayer player, int targetState) {
            this.player = player;
            this.targetState = targetState;
        }

        @Override
        public void run() {
            if (player.currentWorker != this) {
                return;
            }
            player.performRequestedState(targetState);
        }
    }

    private volatile Worker currentWorker;

    @Override
    public void setTargetState(int state) {
        lock.lock();
        try {
            if (targetState != Player.CLOSED) {
                targetState = state;
                lockCondition.signalAll();
                currentWorker = new Worker(this, targetState);
                dispatcher.execute(currentWorker);
            }
        } finally {
            lock.unlock();
        }

    }

    /**
     * Notifies all registered state listeners and
     * all registered change listeners.
     */
    protected abstract void fireStateChanged(int oldState, int newState);

    protected abstract void fireErrorHappened(Throwable error);

    /**
     * This method performs the desired state.
     * <p>
     * If the current state is not the same, transitions the state machine until
     * it reaches the desired state.
     */
    private void performRequestedState(int requestedState) {
        boolean error = false;
        do {
            int oldState = state;
            if (requestedState > state) {
                state++;
            } else {
                state = requestedState;
            }
            fireStateChanged(oldState, state);

            try {
                switch (state) {
                    case CLOSED:
                        doClosed();
                        break;
                    case UNREALIZED:
                        doUnrealized();
                        break;
                    case REALIZING:
                        doRealizing();
                        break;
                    case REALIZED:
                        doRealized();
                        break;
                    case PREFETCHING:
                        doPrefetching();
                        break;
                    case PREFETCHED:
                        doPrefetched();
                        break;
                    case STARTED:
                        doStarted();
                        setTargetState(PREFETCHED);
                        break;
                }
            } catch (Throwable t) {
                error = true;
                fireErrorHappened(t);
                if (state != CLOSED) {
                    oldState = state;
                    state = CLOSED;
                    fireStateChanged(oldState, state);
                    try {
                        doClosed();
                    } catch (Exception e) {
                        // suppress subsequent error
                    }
                }
            }
        } while (state != requestedState && !error);
    }

    /**
     * Does the work for the closed state.
     */
    abstract protected void doClosed() throws Exception;

    /**
     * Does the work for the unrealized state.
     */
    abstract protected void doUnrealized() throws Exception;

    /**
     * Does the work for the realizing state.
     */
    abstract protected void doRealizing() throws Exception;

    /**
     * Does the work for the realized state.
     */
    abstract protected void doRealized() throws Exception;

    /**
     * Does the work for the prefetching state.
     */
    abstract protected void doPrefetching() throws Exception;

    /**
     * Does the work for the prefetched state.
     */
    abstract protected void doPrefetched() throws Exception;

    /**
     * Does the work for the started state.
     */
    abstract protected void doStarted() throws Exception;
}
