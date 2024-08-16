/*
 * @(#)StateTracker.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.player;

/**
 * Tracks state changes in a StateModel.
 *
 * @author Werner Randelshofer
 */
public class StateTracker
        implements StateListener {

    private StateModel model_;
    private int[] targetStates_;

    /**
     * Creates a StateTracker for the indicated StateModel.
     *
     * @param model The model to be tracked.
     */
    public StateTracker(StateModel model) {
        setStateModel(model);
    }

    /**
     * Sets the StateModel.
     * Note: This method must not be called while one of the
     * waitForState methods is working.
     *
     * @param model StateModel to be tracked.
     */
    public void setStateModel(StateModel model) {
        if (model_ != null) {
            model_.removeStateListener(this);
        }

        model_ = model;

        if (model_ != null) {
            model_.addStateListener(this);
        }
    }

    /**
     * Waits until the StateModel reaches the indicated
     * state.
     * Note: waitForState methods may not be called from
     * multiple threads simoultaneously.
     *
     * @param state to wait for.
     */
    public void waitForState(int state) {
        int[] statelist = {state};
        waitForState(statelist);
    }

    /**
     * Waits until the StateModel reaches one of the indicated
     * states.
     * <p>
     * Note: waitForState methods may not be called from
     * multiple threads simoultaneously.
     *
     * @param states choice of states to wait for.
     */
    public int waitForState(int[] states) {
        synchronized (this) {
            targetStates_ = states;

            while (true) {
                int state = model_.getState();
                for (int i = 0; i < targetStates_.length; i++) {
                    if (state == targetStates_[i]) {
                        return targetStates_[i];
                    }
                }
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /**
     * XXX This method is public as an implementation side effect.
     * " Do not call or override.
     */
    public void stateChanged(StateEvent event) {
        synchronized (this) {
            if (targetStates_ != null) {
                int state = event.getNewState();

                for (int i = 0; i < targetStates_.length; i++) {
                    if (state == targetStates_[i]) {
                        notifyAll();
                        break;
                    }
                }
            }
        }
    }
}
