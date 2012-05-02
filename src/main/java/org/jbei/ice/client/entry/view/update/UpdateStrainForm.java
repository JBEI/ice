package org.jbei.ice.client.entry.view.update;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.common.widget.MultipleTextBox;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.dto.StrainInfo;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class UpdateStrainForm extends UpdateEntryForm<StrainInfo> {

    private TextBox host;
    private TextBox genPhen;
    private TextBox plasmids;
    private SuggestBox markers;

    public UpdateStrainForm(HashMap<AutoCompleteField, ArrayList<String>> data, StrainInfo info) {
        super(data, info);

        host.setText(info.getHost());
        genPhen.setText(info.getGenotypePhenotype());
        plasmids.setText(info.getPlasmids());
        markers.setText(info.getSelectionMarkers());
    }

    @Override
    protected void initComponents() {
        super.initComponents();

        host = createStandardTextBox("300px");
        markers = createAutoCompleteForSelectionMarkers("300px");
        genPhen = createStandardTextBox("300px");
        plasmids = createStandardTextBox("300px");
    }

    @Override
    protected Widget createGeneralWidget() {
        int row = 0;
        FlexTable general = new FlexTable();
        general.setWidth("100%");
        general.setCellPadding(3);
        general.setCellSpacing(0);

        // name
        setLabel(true, "Name", general, row, 0);
        Widget widget = createTextBoxWithHelp(name, "e.g. JBEI-0001");
        general.setWidget(row, 1, widget);

        // alias
        row += 1;
        setLabel(false, "Alias", general, row, 0);
        general.getFlexCellFormatter().setWidth(row, 0, "170px");
        general.setWidget(row, 1, alias);

        // creator
        row += 1;
        setLabel(true, "Creator", general, row, 0);
        widget = createTextBoxWithHelp(creator, "Who made this part?");
        general.setWidget(row, 1, widget);

        // creator's email
        row += 1;
        setLabel(false, "Creator's Email", general, row, 0);
        widget = createTextBoxWithHelp(creatorEmail, "If known");
        general.setWidget(row, 1, widget);

        // PI
        row += 1;
        setLabel(true, "Principal Investigator", general, row, 0);
        general.setWidget(row, 1, principalInvestigator);

        // funding source
        row += 1;
        setLabel(false, "Funding Source", general, row, 0);
        general.setWidget(row, 1, fundingSource);

        // status
        row += 1;
        setLabel(false, "Status", general, row, 0);
        general.setWidget(row, 1, status);

        // bio safety level
        row += 1;
        setLabel(false, "Bio Safety Level", general, row, 0);
        general.setWidget(row, 1, bioSafety);

        // host strain
        row += 1;
        setLabel(false, "Host Strain", general, row, 0);
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
    public void populateEntry() {
        super.populateEntry();

        StrainInfo strain = super.getEntryInfo();
        strain.setHost(host.getText());
        strain.setGenotypePhenotype(genPhen.getText());
        strain.setPlasmids(plasmids.getText());
        String selectionMarkers = ((MultipleTextBox) markers.getTextBox()).getWholeText();
        strain.setSelectionMarkers(selectionMarkers);
    }

    @Override
    public FocusWidget validateForm() {
        FocusWidget widget = super.validateForm();
        if (markers.getTextBox().getText().isEmpty()) {
            markers.setStyleName("entry_input_error");
            if (widget == null)
                widget = markers.getTextBox();
        } else {
            markers.setStyleName("input_box");
        }
        return widget;
    }
}
