package org.jbei.ice.client.collection.add.form;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.*;
import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.collection.add.form.ParametersPanel.Parameter;
import org.jbei.ice.client.common.widget.MultipleTextBox;
import org.jbei.ice.client.entry.view.model.AutoCompleteSuggestOracle;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.BioSafetyOption;
import org.jbei.ice.shared.StatusType;
import org.jbei.ice.shared.dto.ParameterInfo;
import org.jbei.ice.shared.dto.entry.EntryInfo;
import org.jbei.ice.shared.dto.user.PreferenceKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Parent class for forms used to create single entries
 *
 * @author Hector Plahar
 */
public abstract class EntryForm<T extends EntryInfo> extends Composite implements IEntryFormSubmit {

    protected final FlexTable layout;
    protected Button cancel;
    protected Button submit;
    protected TextBox creator;
    protected TextBox creatorEmail;
    protected TextBox name;
    protected TextBox alias;
    protected TextBox principalInvestigator;
    protected TextArea summary;
    protected TextBox fundingSource;
    protected ListBox status;
    protected ListBox bioSafety;
    protected TextBox links;
    protected TextBox keywords;
    protected TextArea references;
    protected TextArea ip;

    private final T entryInfo;
    private ParametersPanel parametersPanel;
    private TextArea notesText;

    private HandlerRegistration submitRegistration;
    private HandlerRegistration cancelRegistration;

    public EntryForm(T entryInfo) {
        layout = new FlexTable();
        initWidget(layout);

        initComponents();

        this.creator.setText(entryInfo.getCreator());
        this.creatorEmail.setText(entryInfo.getCreatorEmail());
        this.name.setText(entryInfo.getName());
        this.alias.setText(entryInfo.getAlias());
        this.principalInvestigator.setText(entryInfo.getPrincipalInvestigator());
        this.summary.setText(entryInfo.getShortDescription());
        this.references.setText(entryInfo.getReferences());
        this.fundingSource.setText(entryInfo.getFundingSource());
        status.setVisibleItemCount(1);
        for (StatusType type : StatusType.values()) {
            status.addItem(type.getDisplayName());
        }
        status.setStyleName("pull_down");

        if (entryInfo.getStatus() != null) {
            for (int i = 0; i < this.status.getItemCount(); i += 1) {
                if (status.getValue(i).equalsIgnoreCase(entryInfo.getStatus())) {
                    status.setSelectedIndex(i);
                    break;
                }
            }
        }

        bioSafety.setVisibleItemCount(1);
        for (BioSafetyOption options : BioSafetyOption.values()) {
            bioSafety.addItem(options.getDisplayName(), options.getValue());
        }
        bioSafety.setStyleName("pull_down");
        if (entryInfo.getBioSafetyLevel() != null) {
            for (int i = 0; i < this.bioSafety.getItemCount(); i += 1) {
                if (bioSafety.getValue(i).equalsIgnoreCase(entryInfo.getBioSafetyLevel().toString())) {
                    this.bioSafety.setSelectedIndex(i);
                    break;
                }
            }
        }
        links.setText(entryInfo.getLinks());
        keywords.setText(entryInfo.getKeywords());
        ip.setText(entryInfo.getIntellectualProperty());
        this.notesText.setText(entryInfo.getLongDescription());

        this.entryInfo = entryInfo;
        this.creator.setText(entryInfo.getCreator());
        this.creatorEmail.setText(entryInfo.getCreatorEmail());

        initLayout();
    }

    protected void initLayout() {
        layout.setWidth("100%");
        layout.setCellPadding(2);
        layout.setCellSpacing(0);

        layout.setWidget(0, 0, createGeneralWidget());
        layout.setWidget(1, 0, createParametersWidget());
//        layout.setWidget(2, 0, createSampleWidget());
//        layout.setWidget(2, 0, createSequenceWidget());
        layout.setWidget(2, 0, createNotesWidget());
        layout.setWidget(3, 0, createSubmitCancelButtons());
    }

    protected abstract Widget createGeneralWidget();

    protected void initComponents() {
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
//        sampleLocationScheme = new ArrayList<TextBox>();

        fundingSource = createStandardTextBox("205px");
        status = new ListBox();
        bioSafety = new ListBox();
        links = createStandardTextBox("300px");
        keywords = createStandardTextBox("640px");
        references = createTextArea("640px", "50px");
        ip = createTextArea("640px", "50px");
        notesText = new TextArea();
    }

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

    protected Widget createTextBoxWithHelp(Widget box, String helpText) {
        String html = "<span id=\"box_id\"></span><span class=\"help_text\">" + helpText + "</span>";
        HTMLPanel panel = new HTMLPanel(html);
        panel.addAndReplaceElement(box, "box_id");
        return panel;
    }

    protected void setLabel(boolean required, String label, FlexTable layout, int row, int col) {
        String html;
        if (required)
            html = "<span class=\"font-85em\" style=\"white-space:nowrap\">" + label
                    + "  <span class=\"required\">*</span></span>";
        else
            html = "<span class=\"font-85em\" style=\"white-space:nowrap\">" + label + "</span>";

        layout.setHTML(row, col, html);
        layout.getFlexCellFormatter().setVerticalAlignment(row, col, HasAlignment.ALIGN_TOP);
        layout.getFlexCellFormatter().setWidth(row, col, "170px");
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

        // input
        notes.setWidget(2, 0, new Label(""));
        notes.getFlexCellFormatter().setWidth(3, 0, "170px");

        notesText.setStyleName("entry_add_notes_input");
        notes.setWidget(2, 1, notesText);

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

    /**
     * @return text input for auto complete data
     */
    private SuggestBox createSuggestBox(AutoCompleteField field, String width) {
        AutoCompleteSuggestOracle oracle = new AutoCompleteSuggestOracle(field);
        SuggestBox box = new SuggestBox(oracle, new MultipleTextBox());
        box.setStyleName("input_box");
        box.setWidth(width);
        return box;
    }

    public SuggestBox createAutoCompleteForPromoters(String width) {
        return createSuggestBox(AutoCompleteField.PROMOTERS, width);
    }

    public SuggestBox createAutoCompleteForSelectionMarkers(String width) {
        return createSuggestBox(AutoCompleteField.SELECTION_MARKERS, width);
    }

    public SuggestBox createAutoCompleteForPlasmidNames(String width) {
        return createSuggestBox(AutoCompleteField.PLASMID_NAME, width);
    }

    public SuggestBox createAutoCompleteForOriginOfReplication(String width) {
        return createSuggestBox(AutoCompleteField.ORIGIN_OF_REPLICATION, width);
    }

    @Override
    public FocusWidget validateForm() {

        FocusWidget invalid = null;

        // name
        if (name.getText().trim().isEmpty()) {
            name.setStyleName("input_box_error");
            invalid = name;
        } else {
            name.setStyleName("input_box");
        }

        // creator
        if (creator.getText().trim().isEmpty()) {
            creator.setStyleName("input_box_error");
            if (invalid == null)
                invalid = creator;
        } else {
            creator.setStyleName("input_box");
        }

        // principal investigator
        if (principalInvestigator.getText().trim().isEmpty()) {
            principalInvestigator.setStyleName("input_box_error");
            if (invalid == null)
                invalid = principalInvestigator;
        } else {
            principalInvestigator.setStyleName("input_box");
        }

        // summary
        if (summary.getText().trim().isEmpty()) {
            summary.setStyleName("input_box_error");
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

            if (name.trim().isEmpty() && !value.isEmpty()) {
                parameter.getNameBox().setStyleName("input_box_error");
                if (invalid == null)
                    invalid = parameter.getNameBox();
            } else {
                parameter.getNameBox().setStyleName("input_box");
            }

            if (value.trim().isEmpty() && !name.isEmpty()) {
                parameter.getValueBox().setStyleName("input_box_error");
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
    public void populateEntries() {
        if (this.entryInfo.getOwnerEmail() == null || this.entryInfo.getOwnerEmail().isEmpty()) {
            this.entryInfo.setOwner(ClientController.account.getFullName());
            this.entryInfo.setOwnerEmail(ClientController.account.getEmail());
        }

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

        this.entryInfo.setShortDescription(summary.getText());
        this.entryInfo.setLongDescription(this.notesText.getText());
        this.entryInfo.setLongDescriptionType("text");

        entryInfo.setName(name.getText());
        entryInfo.setAlias(this.alias.getText());
        entryInfo.setCreator(this.creator.getText());
        entryInfo.setCreatorEmail(this.creatorEmail.getText());
        entryInfo.setShortDescription(this.summary.getText());

        entryInfo.setStatus(status.getValue(status.getSelectedIndex()));
        entryInfo.setLinks(links.getText());
        entryInfo.setKeywords(keywords.getText());
        entryInfo.setReferences(references.getText());
        int bioSafetySelectedIndex = bioSafety.getSelectedIndex();
        int value = Integer.parseInt(bioSafety.getValue(bioSafetySelectedIndex));
        entryInfo.setBioSafetyLevel(value);
        entryInfo.setIntellectualProperty(ip.getText());
        entryInfo.setPrincipalInvestigator(principalInvestigator.getText());
        entryInfo.setFundingSource(fundingSource.getText());
    }

    @Override
    public void setPreferences(HashMap<PreferenceKey, String> preferences) {
        if (preferences.containsKey(PreferenceKey.FUNDING_SOURCE)) {
            if (fundingSource.getText().isEmpty())
                fundingSource.setText(preferences.get(PreferenceKey.FUNDING_SOURCE));
        }

        if (preferences.containsKey(PreferenceKey.PRINCIPAL_INVESTIGATOR)) {
            if (principalInvestigator.getText().isEmpty())
                principalInvestigator.setText(preferences.get(PreferenceKey.PRINCIPAL_INVESTIGATOR));
        }
    }

    @Override
    public EntryInfo getEntry() {
        return getEntryInfo();
    }
}
