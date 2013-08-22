package org.jbei.ice.client.profile.preferences;

import java.util.HashMap;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.client.profile.PanelPresenter;
import org.jbei.ice.client.profile.widget.IUserProfilePanel;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Hector Plahar
 */
public class UserPreferencesPresenter extends PanelPresenter {

    private final UserPreferencesPanel panel;

    public UserPreferencesPresenter(final RegistryServiceAsync service, HandlerManager eventBus) {
        super(service, eventBus);
        panel = new UserPreferencesPanel();
        panel.setServiceDelegate(getServiceDelegate());
    }

    public void setData(HashMap<String, String> settings) {
        panel.setData(settings);
    }

    public void setSearchPreferences(HashMap<String, String> settings) {
//        panel.setSearchData(settings);
    }

    public ServiceDelegate<RowData> getServiceDelegate() {
        return new ServiceDelegate<RowData>() {
            @Override
            public void execute(final RowData rowData) {
                new IceAsyncCallback<Boolean>() {

                    @Override
                    protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                        String key = rowData.getKey() == null ? rowData.getField().name() : rowData.getKey().name();
                        service.setPreferenceSetting(ClientController.sessionId, key, rowData.getValue(), callback);
                    }

                    @Override
                    public void onSuccess(Boolean result) {
                        panel.setConfigValue(rowData.getSection(), rowData.getRow(), rowData.getValue());
                    }
                }.go(eventBus);
            }
        };
    }

    @Override
    public IUserProfilePanel getView() {
        return panel;
    }
}
