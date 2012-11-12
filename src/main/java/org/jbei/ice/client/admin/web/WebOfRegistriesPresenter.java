package org.jbei.ice.client.admin.web;

import java.util.HashMap;

import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.admin.AdminPanel;
import org.jbei.ice.client.admin.AdminPanelPresenter;

import com.google.gwt.event.shared.HandlerManager;

/**
 * @author Hector Plahar
 */
public class WebOfRegistriesPresenter extends AdminPanelPresenter {

    private final WebOfRegistriesPanel panel;

    public WebOfRegistriesPresenter(final RegistryServiceAsync service, HandlerManager eventBus) {
        super(service, eventBus);
        panel = new WebOfRegistriesPanel();
        panel.setAddPartnerDelegate(new ServiceDelegate<String>() {
            @Override
            public void execute(String s) {
                // add new web partner
            }
        });
    }

    public void setData(HashMap<String, String> settings) {
        panel.setData(settings);
    }

    @Override
    public AdminPanel getView() {
        return panel;
    }
}
