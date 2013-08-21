package org.jbei.ice.client.admin.setting;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Panel for displaying specific settings. New instances can be created
 * for different types of configurations)
 *
 * @author Hector Plahar
 */
public class SettingPanel extends Composite {

    private final FlexTable table;
    protected final HashMap<String, String> settings;
    private int row;
    private final ServiceDelegate<RowData> serviceDelegate;
    private final ArrayList<ConfigurationKey> keysList;

    public SettingPanel(HashMap<String, String> settings, String panelHeader,
            ServiceDelegate<RowData> delegate, ConfigurationKey... keys) {
        table = new FlexTable();
        table.setWidth("100%");
        table.setCellPadding(1);
        table.setCellSpacing(0);
        initWidget(table);
        this.serviceDelegate = delegate;
        keysList = new ArrayList<ConfigurationKey>();

        row = 0;
        HTMLPanel headerPanel = new HTMLPanel(
                "<span style=\"color: #233559; "
                        + "font-weight: bold; font-style: italic; font-size: 0.85em; text-transform: uppercase\">"
                        + panelHeader + "</span></div>");

        headerPanel.setStyleName("entry_sequence_sub_header");
        table.setWidget(row, 0, headerPanel);
        table.getFlexCellFormatter().setColSpan(row, 0, 3);
        this.settings = settings;
        for (ConfigurationKey key : keys) {
            addSetting(key);
            keysList.add(key);
        }
    }

    protected void addSetting(final ConfigurationKey configurationKey) {
        row += 1;
        final String value = settings.remove(configurationKey.name());
        table.setHTML(row, 0, configurationKey.toString());
        table.getCellFormatter().setStyleName(row, 0, "setting_key");
        table.setHTML(row, 1, value);
        table.getCellFormatter().setStyleName(row, 1, "setting_value");

        final Button edit = new Button("<i class=\"" + FAIconType.EDIT.getStyleName() + "\"></i> Edit");
        edit.setEnabled(configurationKey.isEditable());
        table.setWidget(row, 2, edit);

        final int editRow = row;
        edit.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final TextBox box = new TextBox();
                box.setStyleName("gray_border");
                box.setText(value);
                table.setWidget(editRow, 1, box);
                Widget save = createSaveButton(configurationKey, value, editRow, box, edit);
                table.setWidget(editRow, 2, save);
            }
        });
    }

    public Widget createSaveButton(final ConfigurationKey configurationKey, final String defaultValue,
            final int editRow, final TextBox box, final Button edit) {
        Button save = new Button("Save");
        HTML cancel = new HTML("Cancel");
        HTMLPanel panel = new HTMLPanel("<span id=\"save_setting\"></span><span id=\"cancel_setting_save\"></span>");

        panel.add(save, "save_setting");
        panel.add(cancel, "cancel_setting_save");

        cancel.setStyleName("footer_feedback_widget");
        cancel.addStyleName("font-70em");
        cancel.addStyleName("display-inline");

        // cancel Handler
        ClickHandler cancelHandler = createCancelHandler(defaultValue, editRow, edit);
        cancel.addClickHandler(cancelHandler);

        // save handler
        ClickHandler saveHandler = getSaveHandler(configurationKey, editRow, box, edit);
        save.addClickHandler(saveHandler);

        return panel;
    }

    protected ClickHandler createCancelHandler(final String defaultValue, final int editRow, final Button edit) {
        return new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                table.setHTML(editRow, 1, defaultValue);
                table.setWidget(editRow, 2, edit);
            }
        };
    }

    protected ClickHandler getSaveHandler(final ConfigurationKey key, final int editRow, final TextBox box,
            final Button edit) {
        return new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final RowData rowData = new RowData();
                rowData.setKey(key);
                rowData.setRow(editRow);
                rowData.setValue(box.getValue().trim());
                serviceDelegate.execute(rowData);
                table.setWidget(editRow, 2, edit);
            }
        };
    }

    public void updateConfigSetting(ConfigurationKey key, int row, String value) {
        if (!keysList.contains(key))
            return;

        table.setHTML(row, 1, value);
    }
}
