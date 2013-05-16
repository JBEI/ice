package org.jbei.ice.client.admin.search;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.admin.AdminPanelPresenter;
import org.jbei.ice.client.admin.IAdminPanel;
import org.jbei.ice.client.exception.AuthenticationException;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Panel Presenter for admin search  management
 *
 * @author Hector Plahar
 */
public class AdminSearchPresenter extends AdminPanelPresenter {

    private final AdminSearchPanel panel;

    public AdminSearchPresenter(final RegistryServiceAsync service, final HandlerManager eventBus) {
        super(service, eventBus);
        this.panel = new AdminSearchPanel();
        panel.setRebuildIndexesHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                new IceAsyncCallback<Boolean>() {

                    @Override
                    protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                        service.rebuildSearchIndex(ClientController.sessionId, callback);
                    }

                    @Override
                    public void onSuccess(Boolean result) {
                        if (!result)
                            Window.alert("There was a problem re-indexing");
                    }
                }.go(eventBus);
            }
        });
    }

    @Override
    public IAdminPanel getView() {
        return panel;
    }
}
