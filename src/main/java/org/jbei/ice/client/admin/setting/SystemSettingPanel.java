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

//        // sample settings
//        layout.setWidget(row, 0, createSampleSettingsPanel(settings));
//        layout.getFlexCellFormatter().setStyleName(row, 0, "pad_top");
//        layout.getFlexCellFormatter().setColSpan(row, 0, 3);
//        row += 1;

        // search settings
        layout.setWidget(row, 0, createSearchSettingsPanel(settings));
        layout.getFlexCellFormatter().setStyleName(row, 0, "pad_top");
        layout.getFlexCellFormatter().setColSpan(row, 0, 3);
        row += 1;

//        layout.setWidget(row, 0, createLDAPSettingsPanel(settings));
//        layout.getFlexCellFormatter().setStyleName(row, 0, "pad_top");
//        layout.getFlexCellFormatter().setColSpan(row, 0, 3);
//        row += 1;
    }

    private Widget createGeneralSettingPanel(HashMap<String, String> settings) {
        return new SettingPanel(settings, "General Settings", serviceDelegate,
                                ConfigurationKey.SITE_SECRET,
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
                                ConfigurationKey.DATA_DIRECTORY,
                                ConfigurationKey.SECRET_KEY,
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

//    private Widget createSampleSettingsPanel(HashMap<String, String> settings) {
//        return new SettingPanel(settings, "Sample Settings", serviceDelegate,
//                                ConfigurationKey.PART_STORAGE_DEFAULT,
//                                ConfigurationKey.PART_STORAGE_ROOT,
//                                ConfigurationKey.STRAIN_STORAGE_DEFAULT,
//                                ConfigurationKey.STRAIN_STORAGE_ROOT,
//                                ConfigurationKey.ARABIDOPSIS_STORAGE_DEFAULT,
//                                ConfigurationKey.ARABIDOPSIS_STORAGE_ROOT,
//                                ConfigurationKey.PLASMID_STORAGE_DEFAULT,
//                                ConfigurationKey.PLASMID_STORAGE_ROOT);
//    }

//    private Widget createLDAPSettingsPanel(HashMap<String, String> settings) {
//        return new SettingPanel(settings, "Authentication Settings", serviceDelegate,
//                                ConfigurationKey.LDAP_AUTHENTICATION_URL,
//                                ConfigurationKey.LDAP_QUERY,
//                                ConfigurationKey.AUTHENTICATION_BACKEND,
//                                ConfigurationKey.LDAP_SEARCH_URL);
//    }

    private Widget createSearchSettingsPanel(HashMap<String, String> settings) {
        return new SettingPanel(settings, "Search Settings", serviceDelegate,
                                ConfigurationKey.BLAST_BL2SEQ,
                                ConfigurationKey.BLAST_BLASTALL,
                                ConfigurationKey.BLAST_DIRECTORY,
                                ConfigurationKey.BLAST_DATABASE_NAME,
                                ConfigurationKey.BLAST_FORMATDB);
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
