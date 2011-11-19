package org.jbei.ice.client.entry.view.view;

import java.util.Date;

import org.jbei.ice.client.AppController;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

public class CreateSampleForm extends Composite {

    private final FlexTable table;

    public CreateSampleForm() {
        table = new FlexTable();
        table.setWidth("100%");
        initWidget(table);

        addFirstColumn();

        addSecondColumn();

        addThirdColumn();
    }

    private void addFirstColumn() {
        String html = "<div style=\"outline:none;\"><b>Label</b><span id=\"sample_label\"></span><br /><b style=\"vertical-align: top\">Notes</b> <span id=\"sample_notes\"></span></div>";
        HTMLPanel panel = new HTMLPanel(html);
        TextBox sampleLabel = new TextBox();
        sampleLabel.setStyleName("input_box");
        panel.add(sampleLabel, "sample_label");

        TextArea sampleNotes = new TextArea();
        panel.add(sampleNotes, "sample_notes");
        table.setWidget(0, 0, panel);
    }

    private void addSecondColumn() {
        ListBox options = new ListBox();
        options.setVisibleItemCount(1);
        options.addItem("Strain Storage (Default)");
        options.addItem("Strain Storage Matrix Tubes");

        String html = "<div style=\"outline:none;\"><span id=\"storage_options\"></span></div>";
        HTMLPanel panel = new HTMLPanel(html);
        panel.add(options, "storage_options");
        table.setWidget(0, 1, panel);
    }

    private void addThirdColumn() {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.appendHtmlConstant("<span>");
        builder.appendEscaped(formatDate(new Date()));
        builder.appendHtmlConstant("</span><br /><span>");

        builder.appendHtmlConstant("by <a href='" + AppController.accountInfo.getEmail() + "'>"
                + AppController.accountInfo.getFirstName() + " "
                + AppController.accountInfo.getLastName() + "</a></span>");

        table.setWidget(0, 2, new HTML(builder.toSafeHtml().asString()));
        //        table.getFlexCellFormatter().setWidth(0, 1, "20%");
    }

    protected String formatDate(Date date) {
        if (date == null)
            return "";

        DateTimeFormat format = DateTimeFormat.getFormat("EEE MMM d, y h:m a");
        return format.format(date);
    }
}
