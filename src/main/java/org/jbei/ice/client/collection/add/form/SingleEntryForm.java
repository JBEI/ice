package org.jbei.ice.client.collection.add.form;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.collection.add.form.ParametersPanel.Parameter;
import org.jbei.ice.client.common.widget.MultipleTextBox;
import org.jbei.ice.client.entry.view.model.AutoCompleteSuggestOracle;
import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.BioSafetyOption;
import org.jbei.ice.shared.StatusType;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.ParameterInfo;
import org.jbei.ice.shared.dto.SampleInfo;
import org.jbei.ice.shared.dto.StorageInfo;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.*;

/**
 * Parent class for forms used to create single entries
 *
 * @author Hector Plahar
 */
public abstract class SingleEntryForm<T extends EntryInfo> extends Composite implements IEntryFormSubmit {

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
    private ListBox markupOptions;
    private TextArea notesText;
    private FlexTable sample;

    // sample
    private TextBox sampleName;
    private TextArea sampleNotes;
    private ListBox sampleLocation;
    private ArrayList<TextBox> sampleLocationScheme;

    public SingleEntryForm(T entryInfo) {
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
        layout.setWidget(2, 0, createSampleWidget());
        layout.setWidget(3, 0, createNotesWidget());
        layout.setWidget(4, 0, createSubmitCancelButtons());
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
    public Button getSubmit() {
        return this.submit;
    }

    @Override
    public Button getCancel() {
        return this.cancel;
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
            html = "<span class=\"font-80em\" style=\"white-space:nowrap\">" + label
                    + "  <span class=\"required\">*</span></span>";
        else
            html = "<span class=\"font-80em\" style=\"white-space:nowrap\">" + label + "</span>";

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

        notes.setWidget(2, 0, new HTML("<span class=\"font-80em\">Markup Type</span>"));
        notes.getFlexCellFormatter().setStyleName(2, 0, "entry_add_sub_label");
        markupOptions = new ListBox();
        markupOptions.setVisibleItemCount(1);
        markupOptions.addItem("Text");
        //        markupOptions.addItem("Wiki");
        //        markupOptions.addItem("Confluence");
        markupOptions.setStyleName("pull_down");
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

    @Override
    public void setSampleLocation(final SampleLocation sampleLocation) {
        // location
        sample.setWidget(4, 0, new HTML("<span class=\"font-80em\">Location</span>"));
        sample.getFlexCellFormatter().setStyleName(4, 0, "entry_add_sub_label");

        this.sampleLocation = new ListBox();
        this.sampleLocation.setStyleName("pull_down");
        this.sampleLocation.setVisibleItemCount(1);

        for (SampleInfo location : sampleLocation.getLocations()) {
            this.sampleLocation.addItem(location.getLocation(), location.getLocationId());
        }

        sample.setWidget(4, 1, this.sampleLocation);

        String value = this.sampleLocation.getValue(0);
        ArrayList<String> list = sampleLocation.getListForLocation(value);
        if (list == null) {
            sampleLocationScheme.clear();
            return;
        }

        int row = 4;

        for (final String item : list) {
            row += 1;
            sample.setWidget(row, 0, new HTML("&nbsp;"));
            sample.getFlexCellFormatter().setWidth(row, 0, "170px");
            final TextBox shelf = new TextBox();
            shelf.setStyleName("input_box");
            shelf.getElement().setAttribute("placeholder", item);

            sample.setWidget(row, 1, shelf);
            sampleLocationScheme.add(shelf);
        }

        this.sampleLocation.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {

                int index = SingleEntryForm.this.sampleLocation.getSelectedIndex();
                String value = SingleEntryForm.this.sampleLocation.getValue(index);
                ArrayList<String> list = sampleLocation.getListForLocation(value);
                if (list == null) {
                    sampleLocationScheme.clear();
                    return;
                }

                int row = 4;

                // clear any remaining left over rows
                sampleLocationScheme.clear();
                int rowCount = sample.getRowCount() - 1;
                while (rowCount > row) {
                    sample.removeRow(rowCount);
                    rowCount -= 1;
                }

                for (final String item : list) {
                    row += 1;
                    sample.setWidget(row, 0, new HTML("&nbsp;"));
                    sample.getFlexCellFormatter().setWidth(row, 0, "170px");
                    final TextBox shelf = new TextBox();
                    shelf.setStyleName("input_box");
                    shelf.getElement().setAttribute("placeholder", item);
                    sample.setWidget(row, 1, shelf);
                    sampleLocationScheme.add(shelf);
                }
            }
        });
    }

    protected Widget createSampleWidget() {
        int row = 0;
        sample = new FlexTable();
        sample.setCellPadding(0);
        sample.setCellSpacing(3);
        sample.setWidth("100%");

        sample.setWidget(row, 0, new Label("Sample"));
        sample.getFlexCellFormatter().setStyleName(row, 0, "entry_add_sub_header");
        sample.getFlexCellFormatter().setColSpan(row, 0, 2);

        row += 1;
        sample.setWidget(row, 0, new Label(""));
        sample.getFlexCellFormatter().setHeight(row, 0, "10px");
        sample.getFlexCellFormatter().setColSpan(row, 0, 2);

        // name
        row += 1;
        sample.setWidget(row, 0, new HTML("<span class=\"font-80em\">Name</span>"));
        sample.getFlexCellFormatter().setStyleName(row, 0, "entry_add_sub_label");
        sampleName = createStandardTextBox("204px");
        sample.setWidget(row, 1, sampleName);

        // notes
        row += 1;
        sample.setWidget(row, 0, new HTML("<span class=\"font-80em\">Notes</span>"));
        sample.getFlexCellFormatter().setStyleName(row, 0, "entry_add_sub_label");
        sample.getFlexCellFormatter().setVerticalAlignment(row, 0, HasAlignment.ALIGN_TOP);
        sampleNotes = new TextArea();
        sampleNotes.setStyleName("entry_add_sample_notes_input");
        sample.setWidget(row, 1, sampleNotes);

        return sample;
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

        // samples
        boolean userEnteredSampleName = !sampleName.getText().trim().isEmpty();
        sampleName.setStyleName("input_box");

        if (sampleLocationScheme != null) {
            for (TextBox scheme : sampleLocationScheme) {
                scheme.setStyleName("input_box");

                if (userEnteredSampleName) {
                    // if there is a name, all schemes must be filled
                    if (scheme.getText().trim().isEmpty()) {
                        scheme.setStyleName("entry_input_error");
                        if (invalid == null)
                            invalid = scheme;
                    }
                } else {
                    // there is no name all schemes must be empty
                    if (!scheme.getText().trim().isEmpty()) {
                        sampleName.setStyleName("entry_input_error");
                        if (invalid == null)
                            invalid = sampleName;
                    }
                }
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

        this.entryInfo.setOwner(AppController.accountInfo.getFullName());
        this.entryInfo.setOwnerEmail(AppController.accountInfo.getEmail());

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

        populateSamples();

        this.entryInfo.setShortDescription(summary.getText());
        this.entryInfo.setLongDescription(this.notesText.getText());
        String longDescType = this.markupOptions.getItemText(this.markupOptions.getSelectedIndex());
        this.entryInfo.setLongDescriptionType(longDescType);

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
    }

    protected void populateSamples() {

        if (sampleName.getText().isEmpty())
            return;

        SampleInfo info = new SampleInfo();
        info.setLabel(sampleName.getText());
        info.setNotes(sampleNotes.getText());

        String location = sampleLocation.getValue(sampleLocation.getSelectedIndex());
        info.setLocationId(location);

        LinkedList<StorageInfo> storageInfos = new LinkedList<StorageInfo>();

        for (TextBox scheme : sampleLocationScheme) {
            StorageInfo storageInfo = new StorageInfo();
            String schemeText = scheme.getText();

            storageInfo.setDisplay(schemeText);
            storageInfos.add(storageInfo);
        }

        SampleStorage storage = new SampleStorage(info, storageInfos);
        this.entryInfo.getSampleStorage().add(storage);
    }

    @Override
    public EntryInfo getEntry() {
        return getEntryInfo();
    }
}
