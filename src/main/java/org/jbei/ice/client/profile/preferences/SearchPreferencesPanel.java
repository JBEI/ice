package org.jbei.ice.client.profile.preferences;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.lib.shared.dto.search.SearchBoostField;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * Panel for displaying and editing search preferences
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
                        + panelHeader
                        + "</span>"
                        + " <span class=\"pref_panel_desc\">(Select a value to boost the result for that field. "
                        + "<b>1=least important, 5=most important</b>)</span>"
                        + "</div>");

        headerPanel.setStyleName("entry_sequence_sub_header");
        table.setWidget(row, 0, headerPanel);
        table.getFlexCellFormatter().setColSpan(row, 0, 3);
        this.settings = settings;

        List<SearchBoostField> list = Arrays.asList(SearchBoostField.values());
        Collections.sort(list, new Comparator<SearchBoostField>() {
            @Override
            public int compare(SearchBoostField o1, SearchBoostField o2) {
                return o1.name().compareTo(o2.name());
            }
        });

        for (SearchBoostField field : list) {
            if (!field.isUserBoostable())
                continue;
            addSetting(field);
        }
    }

    protected void addSetting(final SearchBoostField boostField) {
        row += 1;
        String value = settings.remove(boostField.name());
        if (value == null)
            value = "1";

        String label = boostField.toString().replaceAll("_", " ");

        table.setHTML(row, 0, label);
        table.getCellFormatter().setStyleName(row, 0, "setting_key");

        Widget panel = createRadioButton(boostField, value);

        table.setWidget(row, 1, panel);
        table.getCellFormatter().setStyleName(row, 1, boostField.name());

//        final Button edit = new Button("<i class=\"" + FAIconType.EDIT.getStyleName() + "\"></i> Edit");
//        table.setWidget(row, 2, edit);
//
//        final int editRow = row;
//        edit.addClickHandler(new ClickHandler() {
//            @Override
//            public void onClick(ClickEvent event) {
//                final TextBox box = new TextBox();
//                box.setStyleName("gray_border");
//                String display = value == null ? "1" : value;
//                box.setText(display);
//                table.setWidget(editRow, 1, box);
//                Widget save = createSaveButton(boostField, display, editRow, box, edit);
//                table.setWidget(editRow, 2, save);
//            }
//        });
    }

    public Widget createRadioButton(SearchBoostField field, String value) {
        String label = field.name();
        HorizontalPanel panel = new HorizontalPanel();
        for (int i = 1; i <= 5; i += 1) {
            String integerString = Integer.toString(i);
            RadioButton button = new RadioButton(label, SafeHtmlUtils.fromSafeConstant(
                    "<sup>" + integerString + "</sup>"));
            button.setValue(integerString.equals(value));
            button.addValueChangeHandler(new SearchPreferenceChangeHandler(integerString));
            panel.add(button);
        }

        return panel;
    }

    private class SearchPreferenceChangeHandler implements ValueChangeHandler<Boolean> {

        private final String value;

        public SearchPreferenceChangeHandler(String value) {
            this.value = value;
        }

        @Override
        public void onValueChange(ValueChangeEvent<Boolean> event) {
            if (!event.getValue())
                return;

            RadioButton source = (RadioButton) event.getSource();
            String name = source.getName().replaceAll(" ", "_").toUpperCase();
            SearchBoostField field = SearchBoostField.valueOf(name);

            RowData rowData = new RowData();
            rowData.setField(field);
            rowData.setValue(value);
            serviceDelegate.execute(rowData);
        }
    }
}
