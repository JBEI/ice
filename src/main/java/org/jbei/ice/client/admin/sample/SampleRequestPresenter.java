package org.jbei.ice.client.admin.sample;

import java.util.ArrayList;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.admin.AdminPanelPresenter;
import org.jbei.ice.client.admin.IAdminPanel;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.client.service.RegistryServiceAsync;
import org.jbei.ice.lib.shared.dto.sample.SampleRequest;
import org.jbei.ice.lib.shared.dto.sample.SampleRequestStatus;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Presenter for sample requests
 *
 * @author Hector Plahar
 */
public class SampleRequestPresenter extends AdminPanelPresenter {

    private final SampleRequestPanel panel;
    private SampleRequestDataProvider provider;

    public SampleRequestPresenter(final RegistryServiceAsync service, HandlerManager eventBus) {
        super(service, eventBus);
        panel = new SampleRequestPanel(createDelegate());
        provider = new SampleRequestDataProvider(panel.getTable());
    }

    @Override
    public IAdminPanel getView() {
        return panel;
    }

    public void setData(ArrayList<SampleRequest> result) {
        provider.setData(result);
    }

    // if status is fulfilled, this changes it to pending and vice versa
    protected Delegate<SampleRequest> createDelegate() {
        return new Delegate<SampleRequest>() {
            @Override
            public void execute(final SampleRequest request) {
                if (request == null)
                    return;

                if (request.getStatus() == SampleRequestStatus.PENDING)
                    request.setStatus(SampleRequestStatus.FULFILLED);
                else if (request.getStatus() == SampleRequestStatus.FULFILLED)
                    request.setStatus(SampleRequestStatus.PENDING);
                else
                    return;

                new IceAsyncCallback<SampleRequest>() {

                    @Override
                    protected void callService(AsyncCallback<SampleRequest> callback) throws AuthenticationException {
                        service.updateSampleRequest(ClientController.sessionId, request, callback);
                    }

                    @Override
                    public void onSuccess(SampleRequest result) {
                        provider.updateRow(request, result);
                    }
                }.go(eventBus);

            }
        };
    }
}
