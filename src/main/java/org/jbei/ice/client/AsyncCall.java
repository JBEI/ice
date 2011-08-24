package org.jbei.ice.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Abstract AsyncCallback that leaves it up to the user
 * to handle the success implementation. It displays a loading indicator
 * and uses a general failure message with retries
 * 
 * @author Hector Plahar
 * 
 * @param <T>
 */
public abstract class AsyncCall<T> implements AsyncCallback<T> {

    @Override
    public void onFailure(Throwable caught) {
        // TODO generalized fail message
        Window.alert("There was a failure: " + caught.getMessage());
    }

    public void go() {
        int retryCount = 3;
        showBusyIndicator();
        execute(retryCount);
    }

    private void execute(final int retryCount) {
        callService(new AsyncCallback<T>() {

            @Override
            public void onFailure(Throwable caught) {
                GWT.log(caught.toString(), caught);
                if (retryCount <= 0) {
                    hideBusyIndicator();
                    AsyncCall.this.onFailure(caught);
                } else {
                    execute(retryCount - 1);
                }
            }

            @Override
            public void onSuccess(T result) {
                hideBusyIndicator();
                AsyncCall.this.onSuccess(result);
            }
        });
    }

    protected abstract void callService(AsyncCallback<T> callback);

    protected void showBusyIndicator() {

    }

    protected void hideBusyIndicator() {

    }

}
