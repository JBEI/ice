package org.jbei.ice.client.collection.add.form;

import org.jbei.ice.client.common.widget.MultipleTextBox;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.entry.AutoCompleteField;
import org.jbei.ice.lib.shared.dto.entry.EntryType;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.entry.StrainData;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Form for creating new Strain record
 *
 * @author Hector Plahar
 */
public class StrainForm extends EntryForm<StrainData> {

    private TextBox host;
    private TextBox genPhen;
    private SuggestBox plasmids;
    private SuggestBox markers;

    public StrainForm(StrainData strainData) {
        super(strainData);

        host.setText(strainData.getHost());
        genPhen.setText(strainData.getGenotypePhenotype());
        String plasmidString = "";
        for (int i = 0; i < strainData.getLinkedParts().size(); i += 1) {
            if (i > 0)
                plasmidString += ", ";
            plasmidString += strainData.getLinkedParts().get(i);
        }
        plasmids.setText(plasmidString);
        markers.setText(strainData.getSelectionMarkers());
    }

    @Override
    protected void initComponents() {
        super.initComponents();

        host = createStandardTextBox("300px", 150);
        markers = createSuggestBox(AutoCompleteField.SELECTION_MARKERS, "300px");
        genPhen = createStandardTextBox("300px", 150);
        plasmids = createSuggestBox(AutoCompleteField.PLASMID_NAME, "300px");
    }

    protected Widget createGeneralWidget() {
        int row = 0;
        FlexTable general = new FlexTable();
        general.setStyleName("no_wrap");
        general.setWidth("100%");
        general.setCellPadding(3);
        general.setCellSpacing(0);

        // name
        setLabel(true, "Name", general, row, 0);
        Widget widget = createTextBoxWithHelp(name, "e.g. JBEI-0001");
        general.setWidget(row, 1, widget);

        // alias
        setLabel(false, "Alias", general, row, 2);
        general.setWidget(row, 3, alias);

        // creator
        row += 1;
        setLabel(true, "Creator", general, row, 0);
        widget = createTextBoxWithHelp(creator, "Who made this part?");
        general.setWidget(row, 1, widget);

        // PI
        setLabel(true, "Principal Investigator", general, row, 2);
        general.setWidget(row, 3, principalInvestigator);

        // creator's email
        row += 1;
        setLabel(true, "Creator's Email", general, row, 0);
        widget = createTextBoxWithHelp(creatorEmail, "If known");
        general.setWidget(row, 1, widget);

        // funding source
        setLabel(false, "Funding Source", general, row, 2);
        general.setWidget(row, 3, fundingSource);

        // status
        row += 1;
        setLabel(false, "Status", general, row, 0);
        general.setWidget(row, 1, status);

        // bio safety level
        setLabel(false, "Bio Safety Level", general, row, 2);
        general.setWidget(row, 3, bioSafety);

        // host strain
        row += 1;
        setLabel(false, "Parent Strain", general, row, 0);
        general.setWidget(row, 1, host);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // links
        row += 1;
        setLabel(false, "Links", general, row, 0);
        widget = createTextBoxWithHelp(links, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // selection markers
        row += 1;
        setLabel(true, "Selection Markers", general, row, 0);
        widget = createTextBoxWithHelp(markers, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // Genotype/Phenotype 
        row += 1;
        setLabel(false, "Genotype/Phenotype", general, row, 0);
        widget = createTextBoxWithHelp(genPhen, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // plasmids 
        row += 1;
        setLabel(false, "Plasmids", general, row, 0);
        widget = createTextBoxWithHelp(plasmids, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // keywords
        row += 1;
        setLabel(false, "Keywords", general, row, 0);
        general.setWidget(row, 1, keywords);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // summary
        row += 1;
        setLabel(true, "Summary", general, row, 0);
        general.setWidget(row, 1, summary);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // references
        row += 1;
        setLabel(false, "References", general, row, 0);
        general.setWidget(row, 1, references);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // intellectual property
        row += 1;
        setLabel(false, "Intellectual Property", general, row, 0);
        general.setWidget(row, 1, ip);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        return general;
    }

    @Override
    public void populateEntries() {
        super.populateEntries();

        StrainData strain = super.getEntryInfo();
        strain.setHost(this.host.getText());
        strain.setGenotypePhenotype(this.genPhen.getText());
        String plasmidString = ((MultipleTextBox) this.plasmids.getValueBox()).getWholeText();
        for (String plasmid : plasmidString.split(",")) {
            PartData data = new PartData();
            data.setType(EntryType.PLASMID);
            data.setPartId(plasmid.trim());
            strain.getLinkedParts().add(data);
        }
        String selectionMarkers = ((MultipleTextBox) this.markers.getValueBox()).getWholeText();
        strain.setSelectionMarkers(selectionMarkers);
    }

    @Override
    public FocusWidget validateForm() {
        FocusWidget widget = super.validateForm();
        if (markers.getValueBox().getText().trim().isEmpty()) {
            markers.setStyleName("input_box_error");
            if (widget == null)
                widget = markers.getValueBox();
        } else {
            markers.setStyleName("input_box");
        }
        return widget;
    }

    @Override
    public String getHeaderDisplay() {
        return EntryAddType.STRAIN.getDisplay();
    }
}
