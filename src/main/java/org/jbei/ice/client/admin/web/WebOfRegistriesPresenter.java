package org.jbei.ice.client.admin.web;

import java.util.ArrayList;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.admin.AdminPanelPresenter;
import org.jbei.ice.client.admin.IAdminPanel;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.lib.shared.dto.web.RegistryPartner;
import org.jbei.ice.lib.shared.dto.web.WebOfRegistries;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Presenter for the web of registry panel
 *
 * @author Hector Plahar
 */
public class WebOfRegistriesPresenter extends AdminPanelPresenter {

    private final WebOfRegistriesPanel panel;

    public WebOfRegistriesPresenter(final RegistryServiceAsync service, HandlerManager eventBus) {
        super(service, eventBus);
        panel = new WebOfRegistriesPanel(new AddPartnerDelegate());
        panel.addJoinBoxHandler(new JoinBoxChangeHandler());
    }

    public void setData(WebOfRegistries settings) {
        panel.setData(settings);
    }

    @Override
    public IAdminPanel getView() {
        return panel;
    }

    /**
     * Handler for the join web of registries button
     */
    private class JoinBoxChangeHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            final boolean enable = panel.isToggled();
            new IceAsyncCallback<ArrayList<RegistryPartner>>() {

                @Override
                protected void callService(AsyncCallback<ArrayList<RegistryPartner>> callback)
                        throws AuthenticationException {
                    service.setEnableWebOfRegistries(ClientController.sessionId, enable, callback);
                }

                @Override
                public void onSuccess(ArrayList<RegistryPartner> result) {
                    if (result == null) {
                        Window.alert("There was a problem processing your request");
                        return;
                    }

                    if (!enable)
                        return;

                    // only display values if enabling
                    WebOfRegistries web = new WebOfRegistries();
                    web.setWebEnabled(enable);
                    web.setPartners(result);
                    panel.setData(web);
                }
            }.go(eventBus);
        }
    }

    private class AddPartnerDelegate implements ServiceDelegate<String> {

        @Override
        public void execute(final String s) {
            new IceAsyncCallback<Boolean>() {

                @Override
                protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                    // TODO
//                    service.addWebPartner(ClientController.sessionId, s, callback);
                }

                @Override
                public void onSuccess(Boolean result) {
                    if (!result) {
                        Window.alert("There was a problem adding.");
                        return;
                    }

                    // display partner
                    panel.addPartner(s);
                }
            }.go(eventBus);
        }
    }
}
