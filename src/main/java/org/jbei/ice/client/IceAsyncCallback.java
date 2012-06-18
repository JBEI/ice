package org.jbei.ice.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jbei.ice.client.event.FeedbackEvent;
import org.jbei.ice.client.util.Utils;

/**
 * Abstract AsyncCallback that leaves it up to the user
 * to handle the success implementation. It displays a loading indicator
 * and uses a general failure message with retries
 * 
 * @author Hector Plahar
 * 
 * @param <T>
 */
public abstract class IceAsyncCallback<T> implements AsyncCallback<T> {

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
        callService(new AsyncCallback<T>() {

            @Override
            public void onFailure(Throwable caught) {
                GWT.log(caught.toString(), caught);
                if (retryCount <= 0) {
                    hideBusyIndicator();
                    IceAsyncCallback.this.onFailure(caught);
                } else {
                    execute(retryCount - 1, eventBus);
                }
            }

            @Override
            public void onSuccess(T result) {
                hideBusyIndicator();
                if( result == null ) {
                    eventBus.fireEvent(new FeedbackEvent(true, "Error contacting server"));
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
}
