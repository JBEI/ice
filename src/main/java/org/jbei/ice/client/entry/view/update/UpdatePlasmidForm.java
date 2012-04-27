package org.jbei.ice.client.entry.view.update;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.common.widget.MultipleTextBox;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.dto.PlasmidInfo;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Form for updating plasmid
 * 
 * @author Hector Plahar
 */

public class UpdatePlasmidForm extends UpdateEntryForm<PlasmidInfo> {

    private CheckBox circular;
    private TextBox backbone;
    private TextBox origin;
    private TextBox promoters;
    private SuggestBox markers;

    public UpdatePlasmidForm(HashMap<AutoCompleteField, ArrayList<String>> data, PlasmidInfo info) {
        super(data, info);

        circular.setValue(info.getCircular());
        backbone.setText(info.getBackbone());
        origin.setText(info.getOriginOfReplication());
        promoters.setText(info.getPromoters());
    }

    @Override
    protected void initComponents() {
        super.initComponents();

        markers = createAutoCompleteForSelectionMarkers("300px");
        circular = new CheckBox();
        origin = createStandardTextBox("300px");
        promoters = createStandardTextBox("300px");
        backbone = createStandardTextBox("300px");
    }

    private void addField(FlexTable table, String label, int row, int col, TextBox box,
            String help, boolean required) {
        setLabel(required, label, table, row, col);
        if (help != null) {
            Widget widget = createTextBoxWithHelp(box, help);
            table.setWidget(row, col + 1, widget);
        } else
            table.setWidget(row, col + 1, box);
    }

    @Override
    protected Widget createGeneralWidget() {
        int row = 0;
        FlexTable general = new FlexTable();
        general.setWidth("100%");
        general.setCellPadding(2);
        general.setCellSpacing(0);

        // name
        addField(general, "Name", row, 0, name, "e.g. pTSH117", true);

        // alias
        row += 1;
        addField(general, "Alias", row, 0, alias, null, false);

        // creator
        row += 1;
        setLabel(true, "Creator", general, row, 0);
        Widget widget = createTextBoxWithHelp(creator, "Who made this part?");
        general.setWidget(row, 1, widget);

        // PI
        row += 1;
        setLabel(true, "Principal Investigator", general, row, 0);
        general.setWidget(row, 1, principalInvestigator);

        // creator's email
        row += 1;
        setLabel(false, "Creator's Email", general, row, 0);
        widget = createTextBoxWithHelp(creatorEmail, "If known");
        general.setWidget(row, 1, widget);

        // funding source
        row += 1;
        setLabel(false, "Funding Source", general, row, 0);
        fundingSource = createStandardTextBox("205px");
        general.setWidget(row, 1, fundingSource);

        // status
        row += 1;
        setLabel(false, "Status", general, row, 0);
        general.setWidget(row, 1, status);

        // bio safety level
        row += 1;
        setLabel(false, "Bio Safety Level", general, row, 0);
        general.setWidget(row, 1, bioSafety);

        // circular
        row += 1;
        setLabel(false, "Circular", general, row, 0);
        general.setWidget(row, 1, circular);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // backbone
        row += 1;
        setLabel(false, "Backbone", general, row, 0);
        general.setWidget(row, 1, backbone);
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

        // origin of replication
        row += 1;
        setLabel(false, "Origin of Replication", general, row, 0);
        widget = createTextBoxWithHelp(origin, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // promoters
        row += 1;
        setLabel(false, "Promoters", general, row, 0);
        widget = createTextBoxWithHelp(promoters, "Comma separated");
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

        // plasmid specific fields
        PlasmidInfo info = super.getEntryInfo();

        String selectionMarkers = ((MultipleTextBox) markers.getTextBox()).getWholeText();
        info.setSelectionMarkers(selectionMarkers);
        info.setBackbone(this.backbone.getText());
        info.setOriginOfReplication(this.origin.getText());
        info.setPromoters(this.promoters.getText());
        info.setCircular(this.circular.getValue());
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
