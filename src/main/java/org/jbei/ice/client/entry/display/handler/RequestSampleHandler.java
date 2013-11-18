package org.jbei.ice.client.entry.display.handler;

import org.jbei.ice.client.Callback;
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
    private final boolean remove;
    private Callback<SampleRequest> callback;

    public RequestSampleHandler(RegistryServiceAsync service, HandlerManager eventBus, IHasPartData<PartData> data) {
        this.hasEntryId = data;
        this.service = service;
        this.eventBus = eventBus;
        this.remove = false;
    }

    public RequestSampleHandler(RegistryServiceAsync service, HandlerManager eventBus, IHasPartData<PartData> data,
            boolean remove) {
        this.hasEntryId = data;
        this.service = service;
        this.eventBus = eventBus;
        this.remove = remove;
    }

    public void setCallback(Callback<SampleRequest> callback) {
        this.callback = callback;
    }

    @Override
    public void execute(final SampleRequestType sampleRequest) {
        if (remove) {
            removeSample();
            return;
        }

        if (sampleRequest == null)
            return;

        addSample(sampleRequest);
    }

    protected void removeSample() {
        new IceAsyncCallback<SampleRequest>() {

            @Override
            protected void callService(AsyncCallback<SampleRequest> callback) throws AuthenticationException {
                long entryId = hasEntryId.getPart().getId();
                service.removeSampleRequestFromCart(ClientController.sessionId, entryId, callback);
            }

            @Override
            public void onSuccess(SampleRequest result) {
                String msg;
                if (result != null) {
                    msg = "Sample removed to cart";
                    HeaderView.getInstance().removeFromCart(result);
                    if (callback != null)
                        callback.onSuccess(result);
                } else
                    msg = "Could not remove sample from cart";
                eventBus.fireEvent(new FeedbackEvent(result == null, msg));
            }
        }.go(eventBus);
    }

    protected void addSample(final SampleRequestType sampleRequest) {
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
                    msg = "Sample added to cart";
                    HeaderView.getInstance().addToCart(result);
                    if (callback != null)
                        callback.onSuccess(result);
                } else
                    msg = "Could not add sample to cart";
                eventBus.fireEvent(new FeedbackEvent(result == null, msg));
            }
        }.go(eventBus);
    }
}
