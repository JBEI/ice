package org.jbei.ice.client.view;

import org.jbei.ice.client.presenter.EntryPresenter;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.ResizeLayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class EntryView extends Composite implements EntryPresenter.Display {

    public EntryView() {

        FlexTable layout = new FlexTable();
        initWidget(layout);

        layout.setWidth("100%");
        layout.setHeight("100%");
        layout.setWidget(0, 0, createEntryTabViews());
        layout.setWidget(0, 1, rightCol());
        layout.getCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_TOP);
        layout.getCellFormatter().setWidth(0, 1, "200px");
        layout.getFlexCellFormatter().setRowSpan(0, 1, 3);
    }

    /**
     * Sequence widget for returning
     * 
     * @return
     */
    protected Widget createSequenceWidget() {

        FlexTable layout = new FlexTable();
        layout.setStyleName("gray_border");
        layout.setWidth("100%");
        layout.setCellPadding(3);
        layout.setCellSpacing(1);
        layout.setWidget(0, 0, createSequenceUploadPanel());
        return layout;
    }

    protected Widget createSequenceUploadPanel() {

        FlexTable layout = new FlexTable();
        layout.setHTML(0, 0, "Please provide either <b>File</b> or paste <b>Sequence</b>.");
        layout.getFlexCellFormatter().setColSpan(0, 0, 2);
        layout.setHTML(1, 0, "File:");
        FileUpload fileUpload = new FileUpload();
        layout.setWidget(1, 1, fileUpload);
        layout.setHTML(2, 0, "&nbsp;");
        layout.setHTML(2, 1, "or");
        layout.setHTML(3, 0, "Sequence:");
        layout.setWidget(3, 1, new TextArea());

        layout.setWidget(4, 1, new Button("Save"));
        return layout;
    }

    protected Widget createEntryTabViews() {

        ResizeLayoutPanel panel = new ResizeLayoutPanel();
        TabLayoutPanel tabPanel = new TabLayoutPanel(2.5, Unit.EM);
        panel.setHeight("800px");

        tabPanel.add(createGeneralWidget(), "General");
        tabPanel.add(new HTML("[Sequence Trace Files]"), "Seq. Analysis");
        panel.add(tabPanel);
        return panel;
    }

    protected Widget createGeneralWidget() {
        FlexTable general = new FlexTable();
        general.setWidth("100%");
        general.setCellPadding(0);
        general.setCellSpacing(0);

        general.setWidget(0, 0, createSequenceWidget());
        general.setWidget(1, 0, createNotesWidget());

        return general;
    }

    protected Widget createNotesWidget() {

        FlexTable layout = new FlexTable();
        layout.addStyleName("data_table");
        layout.setCellPadding(3);
        layout.setCellSpacing(1);
        layout.setHTML(0, 0, "Notes");
        layout.getCellFormatter().addStyleName(0, 0, "title_row_header");
        layout.getCellFormatter().addStyleName(1, 0, "background_white");

        // contents
        FlexTable contents = new FlexTable();
        contents.setCellPadding(0);
        contents.setCellSpacing(2);
        layout.setWidget(1, 0, contents);

        return layout;
    }

    // right column
    protected FlexTable rightCol() {

        FlexTable rightCol = new FlexTable();
        rightCol.setWidth("200px");
        rightCol.setCellPadding(4);
        rightCol.setWidget(0, 0, createAttachmentsWidget());
        rightCol.setWidget(1, 0, createSamplesWidget());
        rightCol.setWidget(2, 0, createPermissionsWidget());
        return rightCol;
    }

    protected Widget createAttachmentsWidget() {
        FlexTable attachments = new FlexTable();

        attachments.addStyleName("data_table");
        attachments.setCellPadding(3);
        attachments.setCellSpacing(1);
        attachments.setHTML(0, 0, "Attachments | <style='color: blue'>Edit</style>");
        attachments.getCellFormatter().addStyleName(0, 0, "title_row_header");
        attachments.getCellFormatter().addStyleName(1, 0, "background_white");

        // contents
        FlexTable contents = new FlexTable();
        contents.setCellPadding(0);
        contents.setCellSpacing(2);
        attachments.setWidget(1, 0, contents);

        return attachments;
    }

    protected Widget createSamplesWidget() {
        FlexTable samples = new FlexTable();

        samples.addStyleName("data_table");
        samples.setCellPadding(3);
        samples.setCellSpacing(1);
        samples.setHTML(0, 0, "Samples | <style='color: blue'>Edit</style>");
        samples.getCellFormatter().addStyleName(0, 0, "title_row_header");
        samples.getCellFormatter().addStyleName(1, 0, "background_white");

        // contents
        FlexTable contents = new FlexTable();
        contents.setCellPadding(0);
        contents.setCellSpacing(2);
        contents.setHTML(1, 0, "<i>No Samples</i>");
        samples.setWidget(1, 0, contents);

        return samples;
    }

    protected Widget createPermissionsWidget() {
        FlexTable permissions = new FlexTable();

        permissions.addStyleName("data_table");
        permissions.setCellPadding(3);
        permissions.setCellSpacing(1);
        permissions.setHTML(0, 0, "Permissions | <style='color: blue'>Edit</style>");
        permissions.getCellFormatter().addStyleName(0, 0, "title_row_header");
        permissions.getCellFormatter().addStyleName(1, 0, "background_white");

        // contents
        FlexTable contents = new FlexTable();
        contents.setCellPadding(0);
        contents.setCellSpacing(2);

        permissions.setWidget(1, 0, contents);

        return permissions;
    }

    @Override
    public Widget asWidget() {
        return this;
    }
}
