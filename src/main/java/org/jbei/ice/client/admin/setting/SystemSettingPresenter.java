package org.jbei.ice.client.admin.setting;

import java.util.HashMap;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.admin.AdminPanelPresenter;
import org.jbei.ice.client.admin.IAdminPanel;
import org.jbei.ice.client.exception.AuthenticationException;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Hector Plahar
 */
public class SystemSettingPresenter extends AdminPanelPresenter {

    private final SystemSettingPanel panel;

    public SystemSettingPresenter(RegistryServiceAsync service, HandlerManager eventBus) {
        super(service, eventBus);
        panel = new SystemSettingPanel();
        panel.setServiceDelegate(getServiceDelegate());
    }

    public void setData(HashMap<String, String> settings) {
        panel.setData(settings);
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
