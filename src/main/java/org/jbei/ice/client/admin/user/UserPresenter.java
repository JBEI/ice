package org.jbei.ice.client.admin.user;

import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.admin.AdminPanelPresenter;
import org.jbei.ice.client.admin.IAdminPanel;
import org.jbei.ice.shared.dto.AccountResults;

import com.google.gwt.event.shared.HandlerManager;

public class UserPresenter extends AdminPanelPresenter {

    private final UserPanel view;
    private final UserDataProvider provider;

    public UserPresenter(RegistryServiceAsync service, HandlerManager eventBus) {
        super(service, eventBus);
        this.view = new UserPanel();
        this.provider = new UserDataProvider(view.getUserTable(), service);
    }

    public void setData(AccountResults data) {
        provider.setResultsData(data, true);
    }

    @Override
    public IAdminPanel getView() {
        return this.view;
    }
}
