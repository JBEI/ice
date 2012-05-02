package org.jbei.ice.client.collection.add.form;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.dto.PartInfo;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;

public class NewPartForm extends NewSingleEntryForm<PartInfo> {

    public NewPartForm(HashMap<AutoCompleteField, ArrayList<String>> data, String creatorName,
            String creatorEmail) {
        super(data, creatorName, creatorEmail, new PartInfo());
    }

    protected Widget createGeneralWidget() {
        int row = 0;
        FlexTable general = new FlexTable();
        general.setWidth("100%");
        general.setStyleName("no_wrap");
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

        // links
        row += 1;
        setLabel(false, "Links", general, row, 0);
        widget = createTextBoxWithHelp(links, "Comma separated");
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
}
