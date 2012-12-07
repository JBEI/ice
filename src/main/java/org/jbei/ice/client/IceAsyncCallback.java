package org.jbei.ice.client;

import org.jbei.ice.client.event.FeedbackEvent;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.client.util.Utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Abstract AsyncCallback that leaves it up to the user
 * to handle the success implementation. It displays a loading indicator
 * and uses a general failure message with retries
 *
 * @param <T> type of result expected
 * @author Hector Plahar
 */
public abstract class IceAsyncCallback<T> implements AsyncCallback<T> {

    private Timer timeoutTimer;
    static final int TIMEOUT = 60;
    private boolean abortFlag;
    private HandlerManager eventBus;

    @Override
    public void onFailure(Throwable caught) {
        GWT.log(caught.getMessage());
    }

    public void go(HandlerManager eventBus) {
        if (timeoutTimer != null) {
            Window.alert("Server is in the middle of executing a command. Please wait!");
            return;
        }

        this.eventBus = eventBus;
        int retryCount = 1;
        showBusyIndicator();
        execute(retryCount, eventBus);
    }

    private void execute(final int retryCount, final HandlerManager eventBus) {
        // Create a timer to abort if the RPC takes too long
        if (timeoutTimer != null)
            timeoutTimer.cancel();

        timeoutTimer = new Timer() {
            public void run() {
                eventBus.fireEvent(new FeedbackEvent(true, "Timeout executing server call!"));
                GWT.log("Timeout executing server call");
                timeoutTimer = null;
                abortFlag = true;
                hideBusyIndicator();
            }
        };

        // (re)Initialize the abort flag and start the timer.
        abortFlag = false;
        timeoutTimer.schedule(TIMEOUT * 1000); // timeout is in milliseconds

        // call service
        try {
            callService(new AsyncCallback<T>() {

                @Override
                public void onFailure(Throwable caught) {
                    GWT.log(caught.toString(), caught);

                    if (retryCount <= 0) {
                        hideBusyIndicator();
                        cancelTimer();
                        eventBus.fireEvent(new FeedbackEvent(true, "Server call failure detected"));
                        IceAsyncCallback.this.onFailure(caught);  // implementor is notified
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
                        onNullResult();
                        return;
                    }
                    IceAsyncCallback.this.onSuccess(result);
                }
            });
        } catch (AuthenticationException ae) {
            GWT.log(ae.getMessage(), ae);
            History.newItem(Page.LOGIN.getLink());
        }
    }

    // server returned successfully but result was null
    public void onNullResult() {
        if (eventBus != null)
            eventBus.fireEvent(new FeedbackEvent(true, "Server returned invalid results!"));
    }

    protected abstract void callService(AsyncCallback<T> callback) throws AuthenticationException;

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
