package org.jbei.ice.client.profile.preferences;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.shared.dto.search.SearchBoostField;

import java.util.HashMap;

/**
 * Panel for displaying and editting search preferences
 *
 * @author Hector Plahar
 */
public class SearchPreferencesPanel extends Composite {
    private final FlexTable table;
    protected final HashMap<String, String> settings;  // search settings with values
    private int row;
    private final ServiceDelegate<RowData> serviceDelegate;

    public SearchPreferencesPanel(HashMap<String, String> settings, String panelHeader,
                                  ServiceDelegate<RowData> delegate) {
        table = new FlexTable();
        table.setWidth("100%");
        table.setCellPadding(1);
        table.setCellSpacing(0);
        initWidget(table);
        this.serviceDelegate = delegate;

        row = 0;
        HTMLPanel headerPanel = new HTMLPanel(
                "<span style=\"color: #233559; "
                        + "font-weight: bold; font-style: italic; font-size: 0.85em; text-transform: uppercase\">"
                        + panelHeader + "</span></div>");

        headerPanel.setStyleName("entry_sequence_sub_header");
        table.setWidget(row, 0, headerPanel);
        table.getFlexCellFormatter().setColSpan(row, 0, 3);
        this.settings = settings;

        for (SearchBoostField field : SearchBoostField.values())
            addSetting(field);
    }

    protected void addSetting(final SearchBoostField boostField) {
        row += 1;
        final String value = settings.remove(boostField.name());

        table.setHTML(row, 0, boostField.toString());
        table.getCellFormatter().setStyleName(row, 0, "setting_key");
        table.setHTML(row, 1, value);
        table.getCellFormatter().setStyleName(row, 1, "setting_value");

        final Button edit = new Button("<i class=\"" + FAIconType.EDIT.getStyleName() + "\"></i> Edit");
        table.setWidget(row, 2, edit);

        final int editRow = row;
        edit.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final TextBox box = new TextBox();
                box.setStyleName("gray_border");
                String display = value == null ? "1" : value;
                box.setText(display);
                table.setWidget(editRow, 1, box);
                Widget save = createSaveButton(boostField, display, editRow, box, edit);
                table.setWidget(editRow, 2, save);
            }
        });
    }

    public Widget createSaveButton(final SearchBoostField field, final String defaultValue, final int editRow,
                                   final TextBox box, final Button edit) {
        Button save = new Button("Save");
        HTML cancel = new HTML("Cancel");
        HTMLPanel panel = new HTMLPanel("<span id=\"save_setting\"></span><span id=\"cancel_setting_save\"></span>");

        panel.add(save, "save_setting");
        panel.add(cancel, "cancel_setting_save");

        cancel.setStyleName("footer_feedback_widget");
        cancel.addStyleName("font-70em");
        cancel.addStyleName("display-inline");
        cancel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                table.setHTML(editRow, 1, defaultValue);
                table.setWidget(editRow, 2, edit);
            }
        });

        save.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final RowData rowData = new RowData();
                rowData.setField(field);
                rowData.setRow(editRow);
                rowData.setValue(box.getValue().trim());
                serviceDelegate.execute(rowData);
                table.setWidget(editRow, 2, edit);
            }
        });

        return panel;
    }

    public void updateConfigSetting(int row, String value) {
        table.setHTML(row, 1, value);
    }
}
