package org.jbei.ice.client.common.header;

import java.util.ArrayList;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.event.FeedbackEvent;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.client.service.RegistryServiceAsync;
import org.jbei.ice.lib.shared.dto.sample.SampleRequest;
import org.jbei.ice.lib.shared.dto.sample.SampleRequestStatus;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Presenter for the header that essentially manages remote calls to the
 * server in response to actions on the header widgets
 *
 * @author Hector Plahar
 */
public class HeaderPresenter {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;

    public HeaderPresenter(RegistryServiceAsync service, HandlerManager eventBus) {
        this.service = service;
        this.eventBus = eventBus;
    }

    public void deleteSampleRequest(SampleRequest request) {
        HeaderView.getInstance().removeFromCart(request);
    }

    public void submitSampleRequests(ArrayList<SampleRequest> requests) {
        HeaderView.getInstance().resetRequestWidget();
        int size = requests.size();
        String requestString = (size == 1) ? "request" : "requests";
        FeedbackEvent event = new FeedbackEvent(false, size + " sample " + requestString + " submitted");
        eventBus.fireEvent(event);
    }

    public void retrievePendingSampleRequests() {
        new IceAsyncCallback<ArrayList<SampleRequest>>() {
            @Override
            protected void callService(AsyncCallback<ArrayList<SampleRequest>> callback)
                    throws AuthenticationException {
                service.getSampleRequests(ClientController.sessionId, SampleRequestStatus.IN_CART, callback);
            }

            @Override
            public void onSuccess(ArrayList<SampleRequest> result) {
                if (result == null)
                    return;
                HeaderView.getInstance().setSampleRequestData(result);
            }
        }.go(eventBus);
    }
}
