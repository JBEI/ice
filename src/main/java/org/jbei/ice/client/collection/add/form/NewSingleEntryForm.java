package org.jbei.ice.client.collection.add.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.TreeSet;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.collection.add.form.ParametersPanel.Parameter;
import org.jbei.ice.client.common.widget.MultipleTextBox;
import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.ParameterInfo;
import org.jbei.ice.shared.dto.SampleInfo;
import org.jbei.ice.shared.dto.StorageInfo;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Parent class for forms used to create single entries
 * 
 * @author Hector Plahar
 */
public abstract class NewSingleEntryForm<T extends EntryInfo> extends Composite implements
        IEntryFormSubmit {

    protected final FlexTable layout;
    protected final HashMap<AutoCompleteField, ArrayList<String>> data;

    protected Button cancel;
    protected Button submit;
    protected TextBox creator;
    protected TextBox creatorEmail;
    protected TextBox name;
    protected TextBox alias;
    protected TextBox principalInvestigator;
    protected TextArea summary;

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
    private SampleLocation passedLocation;

    public NewSingleEntryForm(HashMap<AutoCompleteField, ArrayList<String>> data,
            String creatorName, String creatorEmail, T entryInfo) {
        layout = new FlexTable();
        this.data = data;
        initComponents();

        this.entryInfo = entryInfo;
        this.creator.setText(creatorName);
        this.creatorEmail.setText(creatorEmail);
    }

    protected void initComponents() {
        submit = new Button("Submit");
        submit.setStyleName("btn_submit_entry_form");
        cancel = new Button("Reset");
        cancel.setStyleName("btn_reset_entry_form");

        creator = createStandardTextBox("205px");
        creatorEmail = createStandardTextBox("205px");
        name = createStandardTextBox("205px");
        alias = createStandardTextBox("205px");
        principalInvestigator = createStandardTextBox("205px");

        summary = createTextArea("640px", "50px");

        sampleLocationScheme = new ArrayList<TextBox>();
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

        notes.setWidget(2, 0, new HTML("<span class=\"font-80em\">Markup Type</span>"));
        notes.getFlexCellFormatter().setStyleName(2, 0, "entry_add_sub_label");
        markupOptions = new ListBox();
        markupOptions.setVisibleItemCount(1);
        markupOptions.addItem("Text");
        markupOptions.addItem("Wiki");
        markupOptions.addItem("Confluence");
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
    public void setSampleLocation(final SampleLocation widget) {

        passedLocation = widget;

        // location
        sample.setWidget(4, 0, new HTML("<span class=\"font-80em\">Location</span>"));
        sample.getFlexCellFormatter().setStyleName(4, 0, "entry_add_sub_label");

        sampleLocation = new ListBox();
        sampleLocation.setStyleName("pull_down");
        sampleLocation.setVisibleItemCount(1);

        for (SampleInfo location : widget.getLocations()) {
            sampleLocation.addItem(location.getLocation(), location.getLocationId());
            if (location.getLocation().equals(location.getLabel()))
                sampleLocation.setSelectedIndex(sampleLocation.getItemCount() - 1);
        }

        sample.setWidget(4, 1, sampleLocation);

        String value = sampleLocation.getValue(0);
        ArrayList<String> list = widget.getListForLocation(value);
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
            shelf.setText(item);
            shelf.setStyleName("input_box");
            shelf.addFocusHandler(new FocusHandler() {

                @Override
                public void onFocus(FocusEvent event) {
                    if (item.equals(shelf.getText().trim()))
                        shelf.setText("");
                }
            });

            shelf.addBlurHandler(new BlurHandler() {

                @Override
                public void onBlur(BlurEvent event) {
                    if ("".equals(shelf.getText().trim()))
                        shelf.setText(item);
                }
            });
            sample.setWidget(row, 1, shelf);
            sampleLocationScheme.add(shelf);
        }

        sampleLocation.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {

                int index = sampleLocation.getSelectedIndex();
                String value = sampleLocation.getValue(index);
                ArrayList<String> list = widget.getListForLocation(value);
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
                    shelf.setText(item);
                    shelf.setStyleName("input_box");
                    shelf.addFocusHandler(new FocusHandler() {

                        @Override
                        public void onFocus(FocusEvent event) {
                            if (item.equals(shelf.getText().trim()))
                                shelf.setText("");
                        }
                    });

                    shelf.addBlurHandler(new BlurHandler() {

                        @Override
                        public void onBlur(BlurEvent event) {
                            if ("".equals(shelf.getText().trim()))
                                shelf.setText(item);
                        }
                    });
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

        // samples
        String location = sampleLocation.getValue(sampleLocation.getSelectedIndex());

        ArrayList<String> passedLocationList = passedLocation.getListForLocation(location);
        boolean hasValidScheme = false;
        for (TextBox scheme : sampleLocationScheme) {

            String schemeText = scheme.getText();

            if (passedLocationList != null && passedLocationList.contains(schemeText.trim()))
                continue;

            hasValidScheme = true;
            break;
        }

        if (hasValidScheme && sampleName.getText().trim().isEmpty()) {
            sampleName.setStyleName("entry_input_error");
            if (invalid == null)
                invalid = sampleName;
        } else {
            sampleName.setStyleName("input_box");
        }

        return invalid;
    }

    /**
     * populates the entry info fields that are common to all. this is meant to be sub-classed so
     * that the specializations can
     * input their class specific fields.
     * 
     * @return
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

        // notes
        this.entryInfo.setLongDescription(this.notesText.getText());
        String longDescType = this.markupOptions.getItemText(this.markupOptions.getSelectedIndex());
        this.entryInfo.setLongDescriptionType(longDescType);
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
        ArrayList<String> passedLocationList = passedLocation.getListForLocation(location);

        for (TextBox scheme : sampleLocationScheme) {
            StorageInfo storageInfo = new StorageInfo();
            String schemeText = scheme.getText();

            if (passedLocationList != null && passedLocationList.contains(schemeText.trim()))
                continue;

            storageInfo.setDisplay(schemeText);
            storageInfos.add(storageInfo);
        }

        SampleStorage storage = new SampleStorage(info, storageInfos);
        this.entryInfo.getSampleStorage().add(storage);
    }

    @Override
    public HashSet<EntryInfo> getEntries() {
        HashSet<EntryInfo> entries = new HashSet<EntryInfo>();
        entries.add(getEntryInfo());
        return entries;
    }
}
