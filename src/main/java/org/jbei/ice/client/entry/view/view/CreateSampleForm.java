package org.jbei.ice.client.entry.view.view;

import java.util.Date;

import org.jbei.ice.client.AppController;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

public class CreateSampleForm extends Composite {

    private final FlexTable table;
    private TextBox sampleLabel;
    private TextArea sampleNotes;
    private TextBox depositor;

    public CreateSampleForm() {
        table = new FlexTable();
        table.setWidth("100%");
        initWidget(table);

        addFirstColumn();

        addSecondColumn();

        addThirdColumn();
    }

    /**
     * Label and Notes
     */
    private void addFirstColumn() {
        // TODO use .css
        String html = "<b class=\"font-85em\">Label</b><span class=\"required\">*</span>&nbsp;<span id=\"sample_label\"></span>"
                + "<br><b style=\"vertical-align: top; font-size: 0.85em;\">Notes</b>&nbsp; <span id=\"sample_notes\"></span></div>";
        HTMLPanel panel = new HTMLPanel(html);
        sampleLabel = new TextBox();
        sampleLabel.setStyleName("input_box");
        panel.add(sampleLabel, "sample_label");

        sampleNotes = new TextArea();
        sampleNotes.setStyleName("input_box");

        panel.add(sampleNotes, "sample_notes");
        table.setWidget(0, 0, panel);
    }

    private void addSecondColumn() {
        ListBox options = new ListBox();
        options.setStyleName("pull_down");
        options.setVisibleItemCount(1);

        // TODO : actual values
        options.addItem("Strain Storage (Default)");
        options.addItem("Strain Storage Matrix Tubes");

        String html = "<b class=\"font-85em\">Location</b><span class=\"required\">*</span><span id=\"storage_options\"></span>";
        HTMLPanel panel = new HTMLPanel(html);
        panel.add(options, "storage_options");
        table.setWidget(0, 1, panel);
        table.getFlexCellFormatter().setVerticalAlignment(0, 1, HasAlignment.ALIGN_TOP);
    }

    private void addThirdColumn() {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.appendHtmlConstant("<span>");
        builder.appendEscaped(formatDate(new Date()));
        builder.appendHtmlConstant("</span><br /><span>by");

        depositor = new TextBox();
        depositor.setStyleName("input_box");
        depositor.setText(AppController.accountInfo.getEmail());
        String html = builder.toSafeHtml().asString() + "<span id=\"sample_depositor\"></span>";
        HTMLPanel panel = new HTMLPanel(html);

        panel.add(depositor, "sample_depositor");

        table.setWidget(0, 2, panel);
        table.getFlexCellFormatter().setStyleName(0, 2, "font-85em");
        table.getFlexCellFormatter().setVerticalAlignment(0, 2, HasAlignment.ALIGN_TOP);
    }

    protected String formatDate(Date date) {
        if (date == null)
            return "";

        DateTimeFormat format = DateTimeFormat.getFormat("EEE MMM d, y h:m a");
        return format.format(date);
    }
}
