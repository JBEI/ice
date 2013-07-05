package org.jbei.ice.client.admin.web;

import java.util.HashMap;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.admin.AdminPanelPresenter;
import org.jbei.ice.client.admin.IAdminPanel;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Hector Plahar
 */
public class WebOfRegistriesPresenter extends AdminPanelPresenter {

    private final WebOfRegistriesPanel panel;

    public WebOfRegistriesPresenter(final RegistryServiceAsync service, HandlerManager eventBus) {
        super(service, eventBus);
        panel = new WebOfRegistriesPanel(new AddPartnerDelegate());
        panel.addJoinBoxHandler(new JoinBoxChangeHandler());
    }

    public void setData(HashMap<String, String> settings) {
        panel.setData(settings);
    }

    @Override
    public IAdminPanel getView() {
        return panel;
    }

    private class JoinBoxChangeHandler implements ChangeHandler {

        @Override
        public void onChange(ChangeEvent event) {

            new IceAsyncCallback<Boolean>() {

                @Override
                protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                    String value = panel.getJoinSelectedValue();
                    service.setConfigurationSetting(ClientController.sessionId, ConfigurationKey.JOIN_WEB_OF_REGISTRIES,
                                                    value, callback);
                }

                @Override
                public void onSuccess(Boolean result) {
                    if (!result) {
                        Window.alert("There was a problem saving setting.");
                    }
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
                    service.addWebPartner(ClientController.sessionId, s, callback);
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
