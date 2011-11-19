package org.jbei.ice.client.entry.view.update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeSet;

import org.jbei.ice.client.common.widget.MultipleTextBox;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.ParameterInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Parent class for forms used to create new entries
 * 
 * @author Hector Plahar
 */
public abstract class UpdateEntryForm<T extends EntryInfo> extends Composite implements
        IEntryFormSubmit {

    protected final FlexTable layout;
    protected final HashMap<AutoCompleteField, ArrayList<String>> data;
    protected Button cancel;
    protected Button submit;
    protected TextBox creator;
    protected TextBox creatorEmail;
    private final T entryInfo;
    private ParametersPanel parametersPanel;

    public UpdateEntryForm(HashMap<AutoCompleteField, ArrayList<String>> data, T entryInfo) {
        layout = new FlexTable();
        this.data = data;
        initComponents();

        this.entryInfo = entryInfo;
        this.creator.setText(entryInfo.getCreator());
        this.creatorEmail.setText(entryInfo.getCreatorEmail());
    }

    protected void initComponents() {
        submit = new Button("Submit");
        submit.setStyleName("btn_submit_entry_form");
        cancel = new Button("Reset");
        cancel.setStyleName("btn_reset_entry_form");

        creator = createStandardTextBox("205px");
        creatorEmail = createStandardTextBox("205px");
    }

    public T getEntryInfo() {
        return this.entryInfo;
    }

    @Override
    public Button getSubmit() {
        return this.submit;
    }

    @Override
    public Button getCancel() {
        return this.cancel;
    }

    protected Widget createNotesWidget() {
        FlexTable notes = new FlexTable();
        notes.setCellPadding(0);
        notes.setCellSpacing(3);
        notes.setWidth("100%");

        notes.setWidget(0, 0, new Label("Notes"));
        notes.getFlexCellFormatter().setStyleName(0, 0, "entry_add_sub_header");
        notes.getFlexCellFormatter().setColSpan(0, 0, 2);
        notes.setWidget(1, 0, new Label(""));
        notes.getFlexCellFormatter().setHeight(1, 0, "10px");
        notes.getFlexCellFormatter().setColSpan(1, 0, 2);

        notes.setWidget(2, 0, new Label("Markup Type"));
        notes.getFlexCellFormatter().setStyleName(2, 0, "entry_add_sub_label");
        ListBox markupOptions = new ListBox();
        markupOptions.setVisibleItemCount(1);
        markupOptions.addItem("Text");
        markupOptions.addItem("Wiki");
        markupOptions.addItem("Confluence");
        markupOptions.setStyleName("entry_add_standard_input_box");
        notes.setWidget(2, 1, markupOptions);

        // input
        notes.setWidget(3, 0, new Label(""));
        notes.getFlexCellFormatter().setWidth(3, 0, "170px");

        TextArea area = new TextArea();
        area.setStyleName("entry_add_notes_input");
        notes.setWidget(3, 1, area);

        return notes;
    }

    protected Widget createSubmitCancelButtons() {
        FlexTable layout = new FlexTable();
        layout.setCellPadding(0);
        layout.setCellSpacing(3);
        layout.setWidth("100%");

        layout.setWidget(0, 0, new HTML("&nbsp;"));
        layout.getCellFormatter().setWidth(0, 0, "160px");

        layout.setWidget(0, 1, submit);
        layout.getFlexCellFormatter().setWidth(0, 1, "100px");
        layout.setWidget(0, 2, cancel);

        return layout;
    }

    protected Widget createParametersWidget() {
        FlexTable parameters = new FlexTable();
        parameters.setCellPadding(0);
        parameters.setCellSpacing(3);
        parameters.setWidth("100%");

        parameters.setWidget(0, 0, new Label("Parameters"));
        parameters.getFlexCellFormatter().setStyleName(0, 0, "entry_add_sub_header");
        parameters.getFlexCellFormatter().setColSpan(0, 0, 6);
        parameters.setWidget(1, 0, new Label(""));
        parameters.getFlexCellFormatter().setHeight(1, 0, "10px");
        parameters.getFlexCellFormatter().setColSpan(1, 0, 6);

        // TODO : parameter widget here
        parametersPanel = new ParametersPanel(parameters, 2);

        return parameters;
    }

    protected TextBox createStandardTextBox(String width) {
        final TextBox box = new TextBox();
        box.setStyleName("input_box");
        box.setWidth(width);
        return box;
    }

    protected TextArea createTextArea(String width, String height) {
        final TextArea area = new TextArea();
        area.setStyleName("input_box");
        area.setWidth(width);
        area.setHeight(height);
        return area;
    }

    protected class Parameter {
        private final TextBox name;
        private final TextBox value;
        private final Button plus;
        private final Button minus;

        public Parameter(FlexTable table, int row, boolean firstRow) {

            table.setWidget(row, 0, new Label("Name"));
            table.getFlexCellFormatter().setStyleName(row, 0, "entry_add_parameter");
            name = createStandardTextBox("205px");
            table.setWidget(row, 1, name);
            table.getFlexCellFormatter().setWidth(row, 1, "220px");

            table.setWidget(row, 2, new Label("Value"));
            table.getFlexCellFormatter().setWidth(row, 2, "50px");

            value = createStandardTextBox("205px");
            table.setWidget(row, 3, value);
            table.getFlexCellFormatter().setWidth(row, 3, "140px");

            plus = new Button("+");
            minus = new Button("-");
            table.setWidget(row, 4, plus);
            if (!firstRow) {
                table.getFlexCellFormatter().setWidth(row, 4, "20px");
                table.setWidget(row, 5, minus);
            }
        }

        public String getName() {
            return this.name.getText();
        }

        public TextBox getNameBox() {
            return this.name;
        }

        public TextBox getValueBox() {
            return this.value;
        }

        public String getValue() {
            return this.value.getText();
        }

        public Button getPlus() {
            return this.plus;
        }

        public Button getMinus() {
            return this.minus;
        }
    }

    protected class ParametersPanel {
        private final LinkedHashMap<Integer, Parameter> map = new LinkedHashMap<Integer, Parameter>();
        private int row;
        private int i;
        private final FlexTable table;

        public ParametersPanel(FlexTable table, int row) {
            this.table = table;
            this.row = row;
            Parameter param = new Parameter(table, row, true);
            param.getMinus().setVisible(false);
            this.row += 1;
            addClickHandlers(param);
            i = 0;
            addToMap(param);
        }

        public LinkedHashMap<Integer, Parameter> getParameterMap() {
            return new LinkedHashMap<Integer, Parameter>(map);
        }

        private void addToMap(Parameter param) {
            map.put(i, param);
            i += 1;
        }

        private void addClickHandlers(Parameter param) {
            param.getPlus().addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    addRow();
                }
            });

            param.getMinus().addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    Cell cell = table.getCellForEvent(event);
                    removeRow(cell.getRowIndex());
                }
            });
        }

        private void addRow() {
            Parameter param = new Parameter(table, row, false);
            addClickHandlers(param);
            addToMap(param);
            row += 1;
        }

        private void removeRow(int clickRow) {
            int toRemove = row - clickRow;
            map.remove(toRemove);
            table.removeRow(clickRow);
            row -= 1;
        }
    }

    /**
     * @return text input for autocomplete data
     */
    private SuggestBox createSuggestBox(TreeSet<String> data) {

        MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
        if (data != null)
            oracle.addAll(data);
        SuggestBox box = new SuggestBox(oracle, new MultipleTextBox());
        box.setStyleName("input_box");
        return box;
    }

    public Widget createAutoCompleteForPromoters(String width) {

        SuggestBox box = createSuggestBox(new TreeSet<String>());//data.get(AutoCompleteField.PROMOTER)));
        box.setWidth(width);
        return box;
    }

    public SuggestBox createAutoCompleteForSelectionMarkers(String width) {

        SuggestBox box = this.createSuggestBox(new TreeSet<String>()); //data
        //                .get(AutoCompleteField.SELECTION_MARKER)));
        box.setWidth(width);
        return box;
    }

    public Widget createAutoCompleteForPlasmidNames(String width) {

        SuggestBox box = this.createSuggestBox(new TreeSet<String>());//data
        //                .get(AutoCompleteField.PLASMID_NAME)));
        box.setWidth(width);
        return box;
    }

    public Widget createAutoCompleteForOriginOfReplication(String width) {

        SuggestBox box = this.createSuggestBox(new TreeSet<String>(data
                .get(AutoCompleteField.ORIGIN_OF_REPLICATION)));
        box.setWidth(width);
        return box;
    }

    //
    // Abstract Methods
    // 

    public FocusWidget validateForm() {
        LinkedHashMap<Integer, Parameter> map = parametersPanel.getParameterMap();
        for (Integer key : map.keySet()) {
            Parameter parameter = map.get(key);
            String name = parameter.getName();
            String value = parameter.getValue();

            if (name.isEmpty() && !value.isEmpty()) {
                parameter.getNameBox().setStyleName("entry_input_error");
                return parameter.getNameBox();
            }

            if (value.isEmpty() && !name.isEmpty()) {
                parameter.getValueBox().setStyleName("entry_input_error");
                return parameter.getValueBox();
            }
        }

        return null;
    }

    /**
     * populates the entry info fields that are common to all. this is meant to be sub-classed so
     * that the specializations can
     * input their class specific fields.
     */
    public void populateEntry() {
        // parameters
        ArrayList<ParameterInfo> parameters = new ArrayList<ParameterInfo>();
        LinkedHashMap<Integer, Parameter> map = parametersPanel.getParameterMap();

        for (Integer key : map.keySet()) {
            Parameter parameter = map.get(key);
            String name = parameter.getName();
            String value = parameter.getValue();

            if (!name.isEmpty() && !value.isEmpty()) {
                parameters.add(new ParameterInfo(name, value));
            }
        }

        this.entryInfo.setParameters(parameters);

        // TODO : samples

        // TODO : notes
    }
}
