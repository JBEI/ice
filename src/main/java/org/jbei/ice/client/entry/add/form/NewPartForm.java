package org.jbei.ice.client.entry.add.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.BioSafetyOptions;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.PartInfo;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class NewPartForm extends NewSingleEntryForm<PartInfo> {

    private TextBox name;
    private TextBox alias;
    private TextBox pI;
    private TextBox fundingSource;
    private ListBox status;
    private ListBox bioSafety;
    private TextBox links;
    private TextBox keywords;
    private TextArea references;
    private TextArea ip;

    public NewPartForm(HashMap<AutoCompleteField, ArrayList<String>> data, String creatorName,
            String creatorEmail) {
        super(data, creatorName, creatorEmail, new PartInfo());
        initWidget(layout);
        init();
    }

    protected void init() {
        layout.setWidth("100%");
        layout.setCellPadding(2);
        layout.setCellSpacing(0);

        layout.setWidget(0, 0, createGeneralWidget());
        layout.setWidget(1, 0, createParametersWidget());
        layout.setWidget(2, 0, createSampleWidget());
        layout.setWidget(3, 0, createNotesWidget());
        layout.setWidget(4, 0, createSubmitCancelButtons());
    }

    protected Widget createTextBoxWithHelp(TextBox box, String helpText) {
        String html = "<span id=\"box_id\"></span><span class=\"help_text\">" + helpText
                + "</span>";
        HTMLPanel panel = new HTMLPanel(html);
        panel.addAndReplaceElement(box, "box_id");
        return panel;
    }

    private void setLabel(boolean required, String label, FlexTable layout, int row, int col) {
        Widget labelWidget;
        if (required)
            labelWidget = new HTML(label + " <span class=\"required\">*</span>");
        else
            labelWidget = new Label(label);

        layout.setWidget(row, col, labelWidget);
        layout.getFlexCellFormatter().setWidth(row, col, "170px");
    }

    private Widget createGeneralWidget() {
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
        general.setWidget(row, 2, new Label("Alias"));
        general.getFlexCellFormatter().setWidth(row, 2, "170px");
        general.setWidget(row, 3, alias);

        // creator
        row += 1;
        general.setWidget(row, 0, new HTML("Creator <span class=\"required\">*</span>"));
        widget = createTextBoxWithHelp(creator, "Who made this part?");
        general.setWidget(row, 1, widget);

        // PI
        general.setWidget(row, 2, new HTML(
                "Principal Investigator <span class=\"required\">*</span>"));
        general.setWidget(row, 3, pI);

        // creator's email
        row += 1;
        general.setWidget(row, 0, new Label("Creator's Email"));
        widget = createTextBoxWithHelp(creatorEmail, "If known");
        general.setWidget(row, 1, widget);

        // funding source
        general.setWidget(row, 2, new Label("Funding Source"));
        fundingSource = createStandardTextBox("205px");
        general.setWidget(row, 3, fundingSource);

        // status
        row += 1;
        general.setWidget(row, 0, new Label("Status"));
        status = new ListBox();
        status.setVisibleItemCount(1);
        status.addItem("Complete");
        status.addItem("In Progress");
        status.addItem("Planned");
        status.setStyleName("input_box");
        general.setWidget(row, 1, status);

        // bio safety level
        general.setWidget(row, 2, new Label("Bio Safety Level"));
        bioSafety = new ListBox();
        bioSafety.setVisibleItemCount(1);
        bioSafety.addItem(BioSafetyOptions.LEVEL_ONE.getDisplayName());
        bioSafety.addItem(BioSafetyOptions.LEVEL_TWO.getDisplayName());
        bioSafety.setStyleName("input_box");
        general.setWidget(row, 3, bioSafety);

        // links
        row += 1;
        general.setWidget(row, 0, new Label("Links"));
        links = createStandardTextBox("300px");
        widget = createTextBoxWithHelp(links, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // keywords
        row += 1;
        general.setWidget(row, 0, new Label("Keywords"));
        keywords = createStandardTextBox("640px");
        general.setWidget(row, 1, keywords);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // summary
        row += 1;
        general.setWidget(row, 0, new HTML("Summary <span class=\"required\">*</span>"));
        general.getFlexCellFormatter().setVerticalAlignment(row, 0, HasAlignment.ALIGN_TOP);
        general.setWidget(row, 1, summary);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // references
        row += 1;
        general.setWidget(row, 0, new Label("References"));
        general.getFlexCellFormatter().setVerticalAlignment(row, 0, HasAlignment.ALIGN_TOP);
        references = createTextArea("640px", "50px");
        general.setWidget(row, 1, references);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // intellectual property
        row += 1;
        general.setWidget(row, 0, new Label("Intellectual Property"));
        general.getFlexCellFormatter().setVerticalAlignment(row, 0, HasAlignment.ALIGN_TOP);
        ip = createTextArea("640px", "50px");
        general.setWidget(row, 1, ip);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        return general;
    }

    @Override
    public FocusWidget validateForm() {
        FocusWidget widget = super.validateForm();
        if (widget != null)
            return widget;

        if (name.getText().isEmpty()) {
            name.setStyleName("entry_input_error");
            widget = name;
        } else {
            name.setStyleName("input_box");
        }

        if (creator.getText().isEmpty()) {
            creator.setStyleName("entry_input_error");
            if (widget == null)
                widget = creator;
        } else {
            creator.setStyleName("input_box");
        }

        if (pI.getText().isEmpty()) {
            pI.setStyleName("entry_input_error");
            if (widget == null)
                widget = pI;
        } else {
            pI.setStyleName("input_box");
        }

        if (summary.getText().isEmpty()) {
            summary.setStyleName("entry_input_error");
            if (widget == null)
                widget = summary;
        } else {
            summary.setStyleName("input_box");
        }

        return widget;
    }

    @Override
    public Set<EntryInfo> getEntries() {
        // TODO Auto-generated method stub
        return null;
    }
}
