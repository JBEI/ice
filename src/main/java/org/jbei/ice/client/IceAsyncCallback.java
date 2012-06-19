package org.jbei.ice.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jbei.ice.client.event.FeedbackEvent;
import org.jbei.ice.client.util.Utils;

/**
 * Abstract AsyncCallback that leaves it up to the user
 * to handle the success implementation. It displays a loading indicator
 * and uses a general failure message with retries
 *
 * @param <T> type of result expected
 * @author Hector Plahar
 */
public abstract class IceAsyncCallback<T> implements AsyncCallback<T> {

    private Timer timeoutTimer = null;
    static final int TIMEOUT = 30; // 30 second timeout
    private boolean abortFlag = false; // indicator when the computation should quit

    @Override
    public void onFailure(Throwable caught) {
        Window.alert("Error connecting to the server");
    }

    public void go(HandlerManager eventBus) {
        int retryCount = 3;
        showBusyIndicator();
        execute(retryCount, eventBus);
    }

    private void execute(final int retryCount, final HandlerManager eventBus) {
        if (timeoutTimer != null) {
            Window.alert("Command execution already in progress!");
            return;
        }

        // Create a timer to abort if the RPC takes too long
        timeoutTimer = new Timer() {
            public void run() {
                eventBus.fireEvent(new FeedbackEvent(true, "Server call took too long to complete!"));
                timeoutTimer = null;
                abortFlag = true;
                hideBusyIndicator();
            }
        };

        // (re)Initialize the abort flag and start the timer.
        abortFlag = false;
        timeoutTimer.schedule(TIMEOUT * 1000); // timeout is in milliseconds

        // call service
        callService(new AsyncCallback<T>() {

            @Override
            public void onFailure(Throwable caught) {
                GWT.log(caught.toString(), caught);

                if (retryCount <= 0) {
                    hideBusyIndicator();
                    cancelTimer();
                    IceAsyncCallback.this.onFailure(caught);
                } else {
                    execute(retryCount - 1, eventBus);
                }
            }

            @Override
            public void onSuccess(T result) {
                hideBusyIndicator();
                cancelTimer();
                if (abortFlag) {
                    // Timeout already occurred. discard result
                    return;
                }
                if (result == null) {
                    eventBus.fireEvent(new FeedbackEvent(true, "Server returned invalid results!"));
                    return;
                }
                IceAsyncCallback.this.onSuccess(result);
            }
        });
    }

    protected abstract void callService(AsyncCallback<T> callback);

    protected void showBusyIndicator() {
        Utils.showWaitCursor(null);
    }

    protected void hideBusyIndicator() {
        Utils.showDefaultCursor(null);
    }

    /**
     * Stop timeout timer if it is running
     */
    private void cancelTimer() {
        if (timeoutTimer != null) {
            timeoutTimer.cancel();
            timeoutTimer = null;
        }
    }
}
