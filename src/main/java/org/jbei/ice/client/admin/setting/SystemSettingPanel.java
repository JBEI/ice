package org.jbei.ice.client.admin.setting;

import java.util.HashMap;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.admin.IAdminPanel;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;
import org.jbei.ice.lib.shared.dto.search.IndexType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Panel for displaying system settings on the administrative page
 *
 * @author Hector Plahar
 */
public class SystemSettingPanel extends Composite implements IAdminPanel {

    private FlexTable layout;
    private ServiceDelegate<RowData> serviceDelegate;
    private Button reIndex;
    private Button blastReIndex;

    public SystemSettingPanel() {
        initComponents();
        layout.setHTML(0, 0, "&nbsp;");
        HTMLPanel panel = new HTMLPanel("<span id=\"rebuild_indexes\"></span>&nbsp;<span id=\"rebuild_blast\"></span>");
        panel.add(reIndex, "rebuild_indexes");
        panel.add(blastReIndex, "rebuild_blast");
        layout.setWidget(1, 0, panel);
        initWidget(layout);
    }

    protected void initComponents() {
        layout = new FlexTable();
//        layout.setWidth("100%");
        reIndex = new Button("<i class=\"blue " + FAIconType.REFRESH.getStyleName() + "\"></i>&nbsp; Rebuild Indexes");
        blastReIndex = new Button("<i class=\"blue " + FAIconType.REFRESH.getStyleName()
                                          + "\"></i>&nbsp; Rebuild BLAST");
    }

    public void setRebuildIndexesHandler(final ServiceDelegate<IndexType> delegate) {
        reIndex.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (!Window.confirm(
                        "This action will cause search results to be incomplete until rebuilding is done. Continue?"))
                    return;

                delegate.execute(IndexType.LUCENE);
            }
        });

        blastReIndex.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (!Window.confirm(
                        "This action will cause search results to be incomplete until rebuilding is done. Continue?"))
                    return;

                delegate.execute(IndexType.BLAST);
            }
        });
    }

    public void setData(HashMap<String, String> settings) {
        for (int i = 2; i < layout.getRowCount(); i += 1)
            layout.removeRow(i);

        int row = 2;

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
                                ConfigurationKey.PART_NUMBER_DELIMITER,
                                ConfigurationKey.PART_NUMBER_DIGITAL_SUFFIX,
                                ConfigurationKey.PART_NUMBER_PREFIX,
                                ConfigurationKey.PASSWORD_CHANGE_ALLOWED,
                                ConfigurationKey.PROJECT_NAME,
                                ConfigurationKey.NEW_REGISTRATION_ALLOWED,
                                ConfigurationKey.DATA_DIRECTORY,
                                ConfigurationKey.TEMPORARY_DIRECTORY,
                                ConfigurationKey.BLAST_INSTALL_DIR);
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
