package org.jbei.ice.client.admin.search;

import java.util.HashMap;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.admin.AdminPanelPresenter;
import org.jbei.ice.client.admin.IAdminPanel;
import org.jbei.ice.client.admin.setting.RowData;
import org.jbei.ice.client.admin.setting.SettingPanel;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;

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
    private final ServiceDelegate<RowData> serviceDelegate;

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

        serviceDelegate = getServiceDelegate();
    }

    public void setData(HashMap<String, String> settings) {
        SettingPanel settingPanel = new SettingPanel(settings, "Search Settings", serviceDelegate,
                                                     ConfigurationKey.BLAST_DIR,
                                                     ConfigurationKey.BLAST_INSTALL_DIR);
        panel.setSearchSetting(settingPanel);
    }

    public ServiceDelegate<RowData> getServiceDelegate() {
        return new ServiceDelegate<RowData>() {
            @Override
            public void execute(final RowData rowData) {
                new IceAsyncCallback<Boolean>() {

                    @Override
                    protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                        service.setConfigurationSetting(ClientController.sessionId,
                                                        rowData.getKey(), rowData.getValue(), callback);
                    }

                    @Override
                    public void onSuccess(Boolean result) {
                        panel.setConfigValue(rowData.getKey(), rowData.getRow(), rowData.getValue());
                    }
                }.go(eventBus);
            }
        };
    }

    @Override
    public IAdminPanel getView() {
        return panel;
    }
}
