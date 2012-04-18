package org.jbei.ice.client.entry.view.update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeSet;

import org.jbei.ice.client.collection.add.form.ParametersPanel;
import org.jbei.ice.client.collection.add.form.ParametersPanel.Parameter;
import org.jbei.ice.client.common.widget.MultipleTextBox;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.ParameterInfo;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
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
        IEntryFormUpdateSubmit {

    protected final FlexTable layout;
    protected final HashMap<AutoCompleteField, ArrayList<String>> data;

    protected Button cancel;
    protected Button submit;

    protected TextBox name;
    protected TextBox alias;
    protected TextBox principalInvestigator;
    protected TextArea summary;

    protected TextBox creator;
    protected TextBox creatorEmail;
    private final T entryInfo;
    private ListBox markupOptions;
    private TextArea notesText;
    private ParametersPanel parametersPanel;

    private HandlerRegistration submitRegistration;
    private HandlerRegistration cancelRegistration;

    public UpdateEntryForm(HashMap<AutoCompleteField, ArrayList<String>> data, T entryInfo) {
        layout = new FlexTable();
        initWidget(layout);

        this.data = data;

        this.entryInfo = entryInfo;
        submit = new Button("Submit");
        submit.setStyleName("btn_submit_entry_form");
        cancel = new Button("Cancel");
        cancel.setStyleName("btn_reset_entry_form");

        creator = createStandardTextBox("205px");
        creatorEmail = createStandardTextBox("205px");

        name = createStandardTextBox("205px");
        alias = createStandardTextBox("205px");
        principalInvestigator = createStandardTextBox("205px");

        summary = createTextArea("640px", "50px");

        this.creator.setText(entryInfo.getCreator());
        this.creatorEmail.setText(entryInfo.getCreatorEmail());
        this.name.setText(entryInfo.getName());
        this.alias.setText(entryInfo.getAlias());
        this.principalInvestigator.setText(entryInfo.getPrincipalInvestigator());
        this.summary.setText(entryInfo.getShortDescription());

        initComponents();

        this.notesText.setText(entryInfo.getLongDescription());
    }

    protected void initComponents() {

        layout.setWidth("100%");
        layout.setCellPadding(2);
        layout.setCellSpacing(0);

        layout.setWidget(0, 0, createGeneralWidget());
        layout.setWidget(1, 0, createParametersWidget());
        layout.setWidget(2, 0, createNotesWidget());
        layout.setWidget(3, 0, createSubmitCancelButtons());
    }

    protected abstract Widget createGeneralWidget();

    public T getEntryInfo() {
        return this.entryInfo;
    }

    @Override
    public void addSubmitHandler(ClickHandler handler) {
        if (submitRegistration != null)
            submitRegistration.removeHandler();
        submitRegistration = this.submit.addClickHandler(handler);
    }

    @Override
    public void addCancelHandler(ClickHandler handler) {
        if (cancelRegistration != null)
            cancelRegistration.removeHandler();
        cancelRegistration = this.cancel.addClickHandler(handler);
    }

    @Override
    public boolean hasSubmitHandler() {
        return (submitRegistration != null);
    }

    @Override
    public boolean hasCancelHandler() {
        return (cancelRegistration != null);
    }

    protected void setLabel(boolean required, String label, FlexTable layout, int row, int col) {
        Widget labelWidget;
        if (required)
            labelWidget = new HTML("<span class=\"font-80em\">" + label
                    + "</span> <span class=\"required\">*</span>");
        else
            labelWidget = new HTML("<span class=\"font-80em\">" + label + "</span>");

        layout.setWidget(row, col, labelWidget);
        layout.getFlexCellFormatter().setWidth(row, col, "170px");
    }

    protected Widget createTextBoxWithHelp(Widget box, String helpText) {
        String html = "<span id=\"box_id\"></span><span class=\"help_text\">" + helpText
                + "</span>";
        HTMLPanel panel = new HTMLPanel(html);
        panel.addAndReplaceElement(box, "box_id");
        return panel;
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

        notes.setWidget(2, 0, new HTML("<span class=\"font-80em\">Markup Type</span>"));
        notes.getFlexCellFormatter().setStyleName(2, 0, "entry_add_sub_label");
        markupOptions = new ListBox();
        markupOptions.setVisibleItemCount(1);
        markupOptions.addItem("Text");
        markupOptions.addItem("Wiki");
        markupOptions.addItem("Confluence");
        markupOptions.setStyleName("entry_add_standard_input_box");
        notes.setWidget(2, 1, markupOptions);

        // input
        notes.setWidget(3, 0, new Label(""));
        notes.getFlexCellFormatter().setWidth(3, 0, "170px");

        notesText = new TextArea();
        notesText.setStyleName("entry_add_notes_input");
        notes.setWidget(3, 1, notesText);

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

        parametersPanel = new ParametersPanel(parameters, 2, getEntry().getParameters());

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

    /**
     * @return text input for autocomplete data
     */
    private SuggestBox createSuggestBox(TreeSet<String> data) {

        MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
        oracle.addAll(data);
        SuggestBox box = new SuggestBox(oracle, new MultipleTextBox());
        box.setStyleName("input_box");
        return box;
    }

    public Widget createAutoCompleteForPromoters(String width) {

        SuggestBox box = createSuggestBox(new TreeSet<String>(data.get(AutoCompleteField.PROMOTERS)));
        box.setWidth(width);
        return box;
    }

    public SuggestBox createAutoCompleteForSelectionMarkers(String width) {

        SuggestBox box = this.createSuggestBox(new TreeSet<String>(data
                .get(AutoCompleteField.SELECTION_MARKERS)));
        box.setWidth(width);
        return box;
    }

    public Widget createAutoCompleteForPlasmidNames(String width) {

        SuggestBox box = this.createSuggestBox(new TreeSet<String>(data
                .get(AutoCompleteField.PLASMID_NAME)));
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

    @Override
    public FocusWidget validateForm() {

        FocusWidget invalid = null;

        // name
        if (name.getText().isEmpty()) {
            name.setStyleName("entry_input_error");
            invalid = name;
        } else {
            name.setStyleName("input_box");
        }

        // creator
        if (creator.getText().isEmpty()) {
            creator.setStyleName("entry_input_error");
            if (invalid == null)
                invalid = creator;
        } else {
            creator.setStyleName("input_box");
        }

        // principal investigator
        if (principalInvestigator.getText().isEmpty()) {
            principalInvestigator.setStyleName("entry_input_error");
            if (invalid == null)
                invalid = principalInvestigator;
        } else {
            principalInvestigator.setStyleName("input_box");
        }

        // summary
        if (summary.getText().isEmpty()) {
            summary.setStyleName("entry_input_error");
            if (invalid == null)
                invalid = summary;
        } else {
            summary.setStyleName("input_box");
        }

        // parameters
        LinkedHashMap<Integer, Parameter> map = parametersPanel.getParameterMap();

        for (Integer key : map.keySet()) {
            Parameter parameter = map.get(key);
            String name = parameter.getName();
            String value = parameter.getValue();

            if (name.isEmpty() && !value.isEmpty()) {
                parameter.getNameBox().setStyleName("entry_input_error");
                if (invalid == null)
                    invalid = parameter.getNameBox();
            } else {
                parameter.getNameBox().setStyleName("input_box");
            }

            if (value.isEmpty() && !name.isEmpty()) {
                parameter.getValueBox().setStyleName("entry_input_error");
                if (invalid == null)
                    invalid = parameter.getValueBox();
            } else {
                parameter.getValueBox().setStyleName("input_box");
            }
        }

        return invalid;
    }

    /**
     * populates the entry info fields that are common to all. this is meant to be sub-classed so
     * that the specializations can
     * input their class specific fields.
     */
    @Override
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

        // notes
        this.entryInfo.setShortDescription(summary.getText());
        this.entryInfo.setLongDescription(this.notesText.getText());
        String longDescType = this.markupOptions.getItemText(this.markupOptions.getSelectedIndex());
        this.entryInfo.setLongDescriptionType(longDescType);

        entryInfo.setAlias(this.alias.getText());
        entryInfo.setCreator(this.creator.getText());
        entryInfo.setCreatorEmail(this.creatorEmail.getText());
        entryInfo.setShortDescription(this.summary.getText());
    }

    @Override
    public EntryInfo getEntry() {
        return this.getEntryInfo();
    }
}
