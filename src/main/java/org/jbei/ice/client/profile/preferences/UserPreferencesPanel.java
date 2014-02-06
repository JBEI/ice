package org.jbei.ice.client.profile.preferences;

import java.util.HashMap;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.profile.widget.IUserProfilePanel;
import org.jbei.ice.lib.shared.dto.user.PreferenceKey;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;

/**
 * Panel for user preference settings
 *
 * @author Hector Plahar
 */
public class UserPreferencesPanel extends Composite implements IUserProfilePanel {

    private final FlexTable layout;
    private ServiceDelegate<RowData> serviceDelegate;

    public UserPreferencesPanel() {
        layout = new FlexTable();
        initWidget(layout);
    }

    public void setData(HashMap<String, String> settings) {
        // general settings
        layout.setWidget(0, 0, createGeneralSettingPanel(settings));
        layout.getFlexCellFormatter().setColSpan(0, 0, 3);
        layout.getFlexCellFormatter().setStyleName(0, 0, "pad_top");
    }

    public void setSearchData(HashMap<String, String> settings) {
        // search settings
        layout.setWidget(1, 0, createSearchSettings(settings));
        layout.getFlexCellFormatter().setColSpan(1, 0, 3);
        layout.getFlexCellFormatter().setStyleName(1, 0, "pad_top");
    }

    public void setServiceDelegate(ServiceDelegate<RowData> serviceDelegate) {
        this.serviceDelegate = serviceDelegate;
    }

    private Widget createGeneralSettingPanel(HashMap<String, String> settings) {
        return new PreferencesPanel(settings, "Create Entry Defaults", serviceDelegate,
                                    PreferenceKey.PRINCIPAL_INVESTIGATOR,
                                    PreferenceKey.FUNDING_SOURCE);
    }

    private Widget createSearchSettings(HashMap<String, String> settings) {
        return new SearchPreferencesPanel(settings, "Search Preferences", serviceDelegate);
    }

    public void setConfigValue(int section, int row, String value) {
        for (int i = 0; i < layout.getRowCount(); i += 1) {
            Widget widget = layout.getWidget(i, 0);
            if ((widget instanceof PreferencesPanel)) {
                PreferencesPanel panel = (PreferencesPanel) widget;
                panel.updateConfigSetting(row, value);
            }

//            else if (widget instanceof SearchPreferencesPanel) {
//                SearchPreferencesPanel panel = (SearchPreferencesPanel) widget;
//                panel.updateConfigSetting(row, value);
//            }
        }
    }
}
