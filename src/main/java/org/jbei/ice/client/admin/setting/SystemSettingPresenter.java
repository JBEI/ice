package org.jbei.ice.client.admin.setting;

import java.util.HashMap;

import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.admin.AdminPanel;
import org.jbei.ice.client.admin.AdminPanelPresenter;

import com.google.gwt.event.shared.HandlerManager;

/**
 * @author Hector Plahar
 */
public class SystemSettingPresenter extends AdminPanelPresenter {

    private final SystemSettingPanel panel;

    public SystemSettingPresenter(RegistryServiceAsync service, HandlerManager eventBus) {
        super(service, eventBus);
        panel = new SystemSettingPanel();
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
