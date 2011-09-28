package org.jbei.ice.client.view;

import org.jbei.ice.client.common.Footer;
import org.jbei.ice.client.common.HeaderView;
import org.jbei.ice.client.common.HeaderMenu;
import org.jbei.ice.client.component.entryview.EntryDetailView;
import org.jbei.ice.client.presenter.EntryPresenter;

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class EntryView extends Composite implements EntryPresenter.Display {

    //    private final Widget generalWidget;
    private final Widget seqAnalysisWidget;
    private final Label general;
    private final Label seqAnalysis;
    private EntryDetailView view;
    private FlexTable contents;

    public EntryView() {

        // main page layout
        FlexTable page = new FlexTable();
        page.setWidth("100%");
        page.setHeight("98%");
        page.setCellSpacing(0);
        page.setCellPadding(0);
        initWidget(page);

        // replacement widgets
        general = new Label("General");
        seqAnalysis = new Label("Sequence Analysis");
        initHeaderStyles();
        //        generalWidget = ; //createGeneralWidget();
        seqAnalysisWidget = createSequenceWidget();

        // inner table/contents

        FlexTable contentsLayout = new FlexTable(); // should be able to get rid of this and fold into page
        contentsLayout.setWidth("100%");
        contentsLayout.setHeight("100%");
        contentsLayout.setCellPadding(0);
        contentsLayout.setCellSpacing(0);
        contentsLayout.setWidget(0, 0, createLeftContents());
        contentsLayout.setWidget(0, 1, rightCol());
        contentsLayout.getCellFormatter()
                .setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
        contentsLayout.getCellFormatter()
                .setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_TOP);
        contentsLayout.getCellFormatter().setWidth(0, 1, "200px");
        contentsLayout.getFlexCellFormatter().setRowSpan(0, 1, 3);

        // set main page contents
        page.setWidget(0, 0, new HeaderView());
        page.setWidget(1, 0, new HeaderMenu());
        page.setWidget(2, 0, createTitleHeader());
        page.setWidget(3, 0, createContentTabHeaders());
        page.setWidget(4, 0, contentsLayout);
        page.getFlexCellFormatter().setVerticalAlignment(4, 0, HasVerticalAlignment.ALIGN_TOP);
        page.getCellFormatter().setHeight(4, 0, "100%");
        page.setWidget(5, 0, Footer.getInstance());
    }

    protected Widget createTitleHeader() {
        Label header = new Label("[Header]");
        header.addStyleName("panel_header");
        header.addStyleName("pad_top");
        return header;
    }

    protected Widget createContentTabHeaders() {
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(general);
        panel.add(seqAnalysis);
        return panel;
    }

    protected void initHeaderStyles() {

        general.setStyleName("base_tabs");
        general.addStyleName("tabs_active");
        general.addMouseOverHandler(new TabOverMouseHandler(general));
        general.addMouseOutHandler(new TabMouseOutHandler(general));
        general.addMouseDownHandler(new TabMouseClickHandler(general, seqAnalysis));

        seqAnalysis.setStyleName("base_tabs");
        seqAnalysis.addMouseOverHandler(new TabOverMouseHandler(seqAnalysis));
        seqAnalysis.addMouseOutHandler(new TabMouseOutHandler(seqAnalysis));
        seqAnalysis.addMouseDownHandler(new TabMouseClickHandler(seqAnalysis, general));
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

    protected Widget createLeftContents() {
        contents = new FlexTable();
        contents.setWidth("100%");
        contents.setCellPadding(0);
        contents.setCellSpacing(0);

        // left header label

        contents.setWidget(0, 0, view);

        general.addMouseUpHandler(new MouseUpHandler() {

            @Override
            public void onMouseUp(MouseUpEvent event) {
                contents.setWidget(0, 0, view);
            }
        });

        seqAnalysis.addMouseUpHandler(new MouseUpHandler() {

            @Override
            public void onMouseUp(MouseUpEvent event) {
                contents.setWidget(0, 0, seqAnalysisWidget);
            }
        });

        return contents;
    }

    //
    // right column
    //
    protected FlexTable rightCol() {

        FlexTable rightCol = new FlexTable();
        rightCol.addStyleName("pad_left");
        rightCol.setWidth("200px");
        rightCol.setCellPadding(0);
        rightCol.setCellSpacing(0);
        rightCol.setWidget(0, 0, createAttachmentsWidget());

        rightCol.setWidget(1, 0, createSamplesWidget());
        rightCol.getFlexCellFormatter().setStyleName(1, 0, "pad_top");

        rightCol.setWidget(2, 0, createPermissionsWidget());
        rightCol.getFlexCellFormatter().setStyleName(2, 0, "pad_top");

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
        contents.setHTML(0, 0, "<i>No Attachments</i>");
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
        contents.setHTML(0, 0, "<i>No Samples</i>");
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

    @Override
    public void setEntryDetailView(EntryDetailView view) {
        this.view = view;
        contents.setWidget(0, 0, view);
    }

    private static class TabOverMouseHandler implements MouseOverHandler {
        private final Widget widget;

        public TabOverMouseHandler(Widget widget) {
            this.widget = widget;
        }

        @Override
        public void onMouseOver(MouseOverEvent event) {
            widget.addStyleName("tabs_hover");
        }
    }

    private static class TabMouseOutHandler implements MouseOutHandler {

        private final Widget widget;

        public TabMouseOutHandler(Widget widget) {
            this.widget = widget;
        }

        @Override
        public void onMouseOut(MouseOutEvent event) {
            widget.removeStyleName("tabs_hover");
        }
    }

    private static class TabMouseClickHandler implements MouseDownHandler {

        private final Widget widget;
        private final Widget[] others;

        public TabMouseClickHandler(Widget widget, Widget... widgets) {
            this.widget = widget;
            this.others = widgets;
        }

        @Override
        public void onMouseDown(MouseDownEvent event) {
            for (Widget other : others)
                other.removeStyleName("tabs_active");

            this.widget.addStyleName("tabs_active");
        }
    }

}
