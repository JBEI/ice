package org.jbei.ice.client.admin.setting;

import java.util.HashMap;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.admin.IAdminPanel;
import org.jbei.ice.shared.dto.ConfigurationKey;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;

/**
 * Panel for displaying system settings on the administrative page
 *
 * @author Hector Plahar
 */
public class SystemSettingPanel extends Composite implements IAdminPanel {

    private final FlexTable layout;
    private ServiceDelegate<RowData> serviceDelegate;

    public SystemSettingPanel() {
        layout = new FlexTable();
        initWidget(layout);
    }

    public void setData(HashMap<String, String> settings) {
        layout.clear();
        int row = 0;

        // general settings
        layout.setWidget(row, 0, createGeneralSettingPanel(settings));
        layout.getFlexCellFormatter().setColSpan(row, 0, 3);
        layout.getFlexCellFormatter().setStyleName(row, 0, "pad_top");
        row += 1;

        // email settings
        layout.setWidget(row, 0, createEmailSettingsPanel(settings));
        layout.getFlexCellFormatter().setStyleName(row, 0, "pad_top");
        layout.getFlexCellFormatter().setColSpan(row, 0, 3);
        row += 1;
    }

    private Widget createGeneralSettingPanel(HashMap<String, String> settings) {
        return new SettingPanel(settings, "General Settings", serviceDelegate,
                                ConfigurationKey.PROFILE_EDIT_ALLOWED,
                                ConfigurationKey.WIKILINK_PREFIX,
                                ConfigurationKey.PART_NUMBER_DELIMITER,
                                ConfigurationKey.PART_NUMBER_DIGITAL_SUFFIX,
                                ConfigurationKey.PART_NUMBER_PREFIX,
                                ConfigurationKey.PASSWORD_CHANGE_ALLOWED,
                                ConfigurationKey.PROJECT_NAME,
                                ConfigurationKey.NEW_REGISTRATION_ALLOWED,
                                ConfigurationKey.ATTACHMENTS_DIRECTORY,
                                ConfigurationKey.TRACE_FILES_DIRECTORY,
                                ConfigurationKey.TEMPORARY_DIRECTORY,
                                ConfigurationKey.DATABASE_SCHEMA_VERSION);
    }

    private Widget createEmailSettingsPanel(HashMap<String, String> settings) {
        return new SettingPanel(settings, "Email Settings", serviceDelegate,
                                ConfigurationKey.SMTP_HOST,
                                ConfigurationKey.ERROR_EMAIL_EXCEPTION_PREFIX,
                                ConfigurationKey.BULK_UPLOAD_APPROVER_EMAIL,
                                ConfigurationKey.SEND_EMAIL_ON_ERRORS,
                                ConfigurationKey.ADMIN_EMAIL);
    }

    public void setServiceDelegate(ServiceDelegate<RowData> serviceDelegate) {
        this.serviceDelegate = serviceDelegate;
    }

    public void setConfigValue(ConfigurationKey key, int row, String value) {
        for (int i = 0; i < layout.getRowCount(); i += 1) {
            Widget widget = layout.getWidget(i, 0);
            if (!(widget instanceof SettingPanel))
                continue;

            SettingPanel panel = (SettingPanel) widget;
            panel.updateConfigSetting(key, row, value);
        }
    }
}
