package org.jbei.ice.client.admin.setting;

import java.util.HashMap;
import java.util.Map;

import org.jbei.ice.client.admin.AdminPanel;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.shared.dto.ConfigurationKey;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Hector Plahar
 */
public class SystemSettingPanel extends Composite implements AdminPanel {

    private final FlexTable layout;

    public SystemSettingPanel() {
        layout = new FlexTable();
        initWidget(layout);
    }

    public void setData(HashMap<String, String> settings) {
        layout.clear();
        int row = 0;

        layout.setWidget(row, 0, createGeneralSettingsPanel(settings));
        layout.getFlexCellFormatter().setColSpan(row, 0, 3);
        layout.getFlexCellFormatter().setStyleName(row, 0, "pad_top");
        row += 1;

        layout.setWidget(row, 0, createEmailSettingsPanel(settings));
        layout.getFlexCellFormatter().setStyleName(row, 0, "pad_top");
        layout.getFlexCellFormatter().setColSpan(row, 0, 3);
        row += 1;

        // display whatever settings remain.
        for (Map.Entry<String, String> entry : settings.entrySet()) {

            String key;
            ConfigurationKey configurationKey = ConfigurationKey.stringToEnum(entry.getKey());
            if (configurationKey == null || configurationKey.toString().isEmpty())
                key = entry.getKey();
            else
                key = configurationKey.toString();

            layout.setHTML(row, 0, "<b class=\"font-75em\" style=\"text-transform: uppercase\">" + key + "</b>");
            layout.setHTML(row, 1, "<span class=\"font-75em\" style=\"margin-left: 30px; margin-right: 30px\">"
                    + entry.getValue() + "</span>");
            layout.setHTML(row, 2, "<span class=\"font-70em\"><i class=\"" + FAIconType.EDIT.getStyleName()
                    + "\"> Edit</span>");
            row += 1;
        }
    }

    private Widget createGeneralSettingsPanel(HashMap<String, String> settings) {
        HTMLPanel headerPanel = new HTMLPanel(
                "<span style=\"color: #233559; "
                        + "font-weight: bold; font-style: italic; font-size: 0.80em; text-transform: uppercase\">"
                        + "General Settings"
                        + "</span><div style=\"float: right\"><span id=\"add_partner\"></span></div>");

        headerPanel.setStyleName("entry_sequence_sub_header");

        FlexTable table = new FlexTable();
        table.setWidth("100%");
        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setWidget(0, 0, headerPanel);
        table.getFlexCellFormatter().setColSpan(0, 0, 2);

        int row = 1;
        String key = ConfigurationKey.COOKIE_NAME.name();
        String value = settings.remove(key);
        table.setHTML(row, 0, ConfigurationKey.COOKIE_NAME.toString());
        table.setHTML(row, 1, value);
        row += 1;

        key = ConfigurationKey.PROFILE_EDIT_ALLOWED.name();
        value = settings.remove(key);
        table.setHTML(row, 0, ConfigurationKey.PROFILE_EDIT_ALLOWED.toString());
        table.setHTML(row, 1, value);
        row += 1;

        key = ConfigurationKey.WIKILINK_PREFIX.name();
        value = settings.remove(key);
        table.setHTML(row, 0, ConfigurationKey.WIKILINK_PREFIX.toString());
        table.setHTML(row, 1, value);
        row += 1;

        key = ConfigurationKey.PART_NUMBER_DELIMITER.name();
        value = settings.remove(key);
        table.setHTML(row, 0, ConfigurationKey.PART_NUMBER_DELIMITER.toString());
        table.setHTML(row, 1, value);
        row += 1;

        key = ConfigurationKey.PART_NUMBER_DIGITAL_SUFFIX.name();
        value = settings.remove(key);
        table.setHTML(row, 0, ConfigurationKey.PART_NUMBER_DIGITAL_SUFFIX.toString());
        table.setHTML(row, 1, value);
        row += 1;

        key = ConfigurationKey.PART_NUMBER_PREFIX.name();
        value = settings.remove(key);
        table.setHTML(row, 0, ConfigurationKey.PART_NUMBER_PREFIX.toString());
        table.setHTML(row, 1, value);
        row += 1;

        key = ConfigurationKey.URI_PREFIX.name();
        value = settings.remove(key);
        table.setHTML(row, 0, ConfigurationKey.URI_PREFIX.toString());
        table.setHTML(row, 1, value);
        row += 1;

        key = ConfigurationKey.PROJECT_NAME.name();
        value = settings.remove(key);
        table.setHTML(row, 0, ConfigurationKey.PROJECT_NAME.toString());
        table.setHTML(row, 1, value);
        row += 1;

        key = ConfigurationKey.NEW_REGISTRATION_ALLOWED.name();
        value = settings.remove(key);
        table.setHTML(row, 0, ConfigurationKey.NEW_REGISTRATION_ALLOWED.toString());
        table.setHTML(row, 1, value);
        row += 1;

        return table;
    }

    private Widget createEmailSettingsPanel(HashMap<String, String> settings) {
        HTMLPanel headerPanel = new HTMLPanel(
                "<span style=\"color: #233559; "
                        + "font-weight: bold; font-style: italic; font-size: 0.80em; text-transform: uppercase\">"
                        + "Email Settings"
                        + "</span><div style=\"float: right\"><span id=\"add_partner\"></span></div>");

        headerPanel.setStyleName("entry_sequence_sub_header");

        FlexTable table = new FlexTable();
        table.setWidth("100%");
        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setWidget(0, 0, headerPanel);
        table.getFlexCellFormatter().setColSpan(0, 0, 2);

        int row = 1;
        String key = ConfigurationKey.SMTP_HOST.name();
        String value = settings.remove(key);
        table.setHTML(row, 0, ConfigurationKey.SMTP_HOST.toString());
        table.setHTML(row, 1, value);
        row += 1;

        key = ConfigurationKey.BULK_UPLOAD_APPROVER_EMAIL.name();
        value = settings.remove(key);
        table.setHTML(row, 0, ConfigurationKey.BULK_UPLOAD_APPROVER_EMAIL.toString());
        table.setHTML(row, 1, value);
        row += 1;

        key = ConfigurationKey.SEND_EMAIL_ON_ERRORS.name();
        value = settings.remove(key);
        table.setHTML(row, 0, ConfigurationKey.SEND_EMAIL_ON_ERRORS.toString());
        table.setHTML(row, 1, value);
        row += 1;

        key = ConfigurationKey.ERROR_EMAIL_EXCEPTION_PREFIX.name();
        value = settings.remove(key);
        table.setHTML(row, 0, ConfigurationKey.ERROR_EMAIL_EXCEPTION_PREFIX.toString());
        table.setHTML(row, 1, value);
        row += 1;

        key = ConfigurationKey.ADMIN_EMAIL.name();
        value = settings.remove(key);
        table.setHTML(row, 0, ConfigurationKey.ADMIN_EMAIL.toString());
        table.setHTML(row, 1, value);
        row += 1;
        return table;
    }
}
