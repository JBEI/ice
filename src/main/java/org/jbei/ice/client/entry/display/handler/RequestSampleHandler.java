package org.jbei.ice.client.entry.display.handler;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.common.entry.IHasPartData;
import org.jbei.ice.client.common.header.HeaderView;
import org.jbei.ice.client.event.FeedbackEvent;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.client.service.RegistryServiceAsync;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.sample.SampleRequest;
import org.jbei.ice.lib.shared.dto.sample.SampleRequestType;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Delegate for handling user sample requests
 *
 * @author Hector Plahar
 */
public class RequestSampleHandler implements Delegate<SampleRequestType> {

    private final IHasPartData<PartData> hasEntryId;
    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;

    public RequestSampleHandler(RegistryServiceAsync service, HandlerManager eventBus, IHasPartData<PartData> data) {
        this.hasEntryId = data;
        this.service = service;
        this.eventBus = eventBus;
    }

    @Override
    public void execute(final SampleRequestType sampleRequest) {
        new IceAsyncCallback<SampleRequest>() {

            @Override
            protected void callService(AsyncCallback<SampleRequest> callback) throws AuthenticationException {
                long entryId = hasEntryId.getPart().getId();
                service.requestSample(ClientController.sessionId, entryId, sampleRequest, callback);
            }

            @Override
            public void onSuccess(SampleRequest result) {
                String msg;
                if (result != null) {
                    msg = "Sample request added to cart";
                    HeaderView.getInstance().addToCart(result);
                } else
                    msg = "Could not add sample to cart";
                eventBus.fireEvent(new FeedbackEvent(result == null, msg));
            }
        }.go(eventBus);
    }
}
