package org.jbei.ice.client.entry.view.view;

import java.util.ArrayList;
import java.util.Date;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.common.AbstractLayout;
import org.jbei.ice.client.common.widget.Flash;
import org.jbei.ice.client.entry.view.detail.EntryDetailView;
import org.jbei.ice.client.entry.view.table.EntrySampleTable;
import org.jbei.ice.client.entry.view.table.SequenceTable;
import org.jbei.ice.client.entry.view.table.TablePager;
import org.jbei.ice.client.entry.view.update.UpdateEntryForm;
import org.jbei.ice.shared.dto.AttachmentInfo;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class EntryView extends AbstractLayout implements IEntryView {

    private CellList<AttachmentInfo> attachmentList;
    private FlexTable mainContent;
    private Button cancelAttachmentSubmission;
    private Button saveAttachment;
    private Widget attachmentForm;

    // general header
    private HTMLPanel generalHeaderPanel;
    private Button editGeneralButton;

    // sequence Analysis
    private HTMLPanel seqPanel;
    private Button addSeqButton;

    // samples
    private HTMLPanel samplesPanel;
    private Button addSampleButton;
    private CreateSampleForm sampleForm;

    // permissions
    private final PermissionsWidget permissions;

    private Widget uploadPanel;
    private FlexTable left; // left side of the page with menu

    // navigation buttons for context navigation.
    // TODO : create a widget for it
    private Button goBack;
    private Button leftBtn;
    private Button rightBtn;

    // menu
    private EntryDetailViewMenu detailMenu;

    public EntryView() {
        permissions = new PermissionsWidget();
        goBack = new Button("Back");
        leftBtn = new Button("Prev");
        rightBtn = new Button("Next");
    }

    @Override
    protected void initComponents() {
        super.initComponents();

        sampleForm = new CreateSampleForm();
        sampleForm.setVisible(false);

        uploadPanel = createSequenceUploadPanel();

        // attachments
        attachmentList = new CellList<AttachmentInfo>(new AbstractCell<AttachmentInfo>() {

            @Override
            public void render(Context context, AttachmentInfo value, SafeHtmlBuilder sb) {
                if (value == null)
                    return;

                sb.appendHtmlConstant("<b>");
                sb.appendEscaped(value.getFilename());
                sb.appendHtmlConstant("</b>");

                sb.appendHtmlConstant("<br /><span class=\"attachment_small_text\">");
                String description = value.getDescription();
                if (description.isEmpty())
                    description = "No description provided.";
                sb.appendEscaped(description);
                sb.appendHtmlConstant("</span>");
            }
        });

        // buttons 
        cancelAttachmentSubmission = new Button("Cancel", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                // TODO : clear the form field values
                attachmentForm.setVisible(false);
            }
        });

        saveAttachment = new Button("Save");
        attachmentForm = createAddToAttachment();
        attachmentForm.setVisible(false);
    }

    @Override
    protected Widget createContents() {
        FlexTable contentTable = new FlexTable();
        contentTable.setWidth("100%");
        contentTable.setCellPadding(3);
        contentTable.setCellSpacing(0);
        contentTable.setWidget(0, 0, createMenu());
        contentTable.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);

        // TODO : middle sliver goes here
        contentTable.setWidget(0, 1, createMainContent());
        contentTable.getCellFormatter().setWidth(0, 1, "100%");
        contentTable.getFlexCellFormatter().setVerticalAlignment(0, 1, HasAlignment.ALIGN_TOP);
        return contentTable;
    }

    @Override
    public void showUpdateForm(UpdateEntryForm<? extends EntryInfo> form) {
        mainContent.setWidget(1, 0, form);
    }

    @Override
    public void showPermissionsWidget() {
        mainContent.setWidget(0, 0, new HTML("Permissions"));
        mainContent.setWidget(1, 0, this.permissions);
    }

    /**
     * Center content
     */

    protected Widget createMainContent() {
        mainContent = new FlexTable();
        mainContent.setStyleName("entry_view_main_content_table");
        mainContent.setWidth("100%");
        mainContent.setCellPadding(3);
        mainContent.setCellSpacing(0);

        mainContent.setHTML(0, 0, "&nbsp;");
        mainContent.getFlexCellFormatter().setColSpan(0, 0, 2);

        // second row
        mainContent.setWidget(1, 0, new Label("Loading..."));
        mainContent.getFlexCellFormatter().setStyleName(1, 0, "entry_view_content");
        mainContent.getCellFormatter().setWidth(1, 0, "100%");
        mainContent.setWidget(1, 1, createRightMenu());
        mainContent.getFlexCellFormatter().setStyleName(1, 1, "entry_view_right_menu");

        return mainContent;
    }

    // currently shows only the attachments menu
    protected Widget createRightMenu() {
        FlexTable layout = new FlexTable();
        layout.setCellPadding(3);
        layout.setCellSpacing(0);
        layout.addStyleName("entry_view_right_menu_2"); // TODO cannot find what I am using 1 for
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(new HTML("Attachments"));
        final Button button = new Button("+", new ClickHandler() { // TODO : use a push button

                    @Override
                    public void onClick(ClickEvent event) {
                        boolean visible = attachmentForm.isVisible();
                        attachmentForm.setVisible(!visible);
                    }
                });

        panel.add(button);
        panel.setWidth("100%");
        panel.setCellHorizontalAlignment(button, HasAlignment.ALIGN_RIGHT);

        layout.setWidget(0, 0, panel);
        layout.getCellFormatter().setStyleName(0, 0, "entry_view_sub_menu_header");
        layout.setWidget(1, 0, attachmentForm);

        // cell to render value
        layout.setWidget(2, 0, attachmentList);
        return layout;
    }

    protected Widget createAddToAttachment() {
        VerticalPanel panel = new VerticalPanel();
        panel.add(new HTML("<b>File</b>"));
        panel.add(new FileUpload());
        panel.add(new HTML("<b>Description</b>"));
        panel.add(new TextArea());
        HorizontalPanel savePanel = new HorizontalPanel();

        savePanel.add(saveAttachment);
        savePanel.add(cancelAttachmentSubmission);
        panel.add(savePanel);
        panel.setCellHorizontalAlignment(savePanel, HasAlignment.ALIGN_RIGHT);

        return panel;
    }

    protected Widget createMenu() {
        left = new FlexTable();
        left.setCellPadding(0);
        left.setCellSpacing(0);
        this.detailMenu = new EntryDetailViewMenu();
        left.setHTML(0, 0, "");
        left.setWidget(1, 0, detailMenu);
        return left;
    }

    @Override
    public void addNextHandler(ClickHandler handler) {
        rightBtn.addClickHandler(handler);
    }

    @Override
    public void addGoBackHandler(ClickHandler handler) {
        goBack.addClickHandler(handler);
    }

    @Override
    public void addPrevHandler(ClickHandler handler) {
        leftBtn.addClickHandler(handler);
    }

    @Override
    public void enablePrev(boolean enabled) {
        leftBtn.setEnabled(enabled);
    }

    @Override
    public void enableNext(boolean enabled) {
        rightBtn.setEnabled(enabled);
    }

    @Override
    public void showContextNav(boolean show) {
        if (show) {
            HTMLPanel panel = new HTMLPanel(
                    "<span id=\"goBack\"></span> <span id=\"leftBtn\"></span> <span id=\"rightBtn\"></span>");
            panel.add(goBack, "goBack");
            panel.add(leftBtn, "leftBtn");
            panel.add(rightBtn, "rightBtn");

            left.setWidget(0, 0, panel);
        } else {
            left.setHTML(0, 0, "");
        }
    }

    @Override
    public EntryDetailViewMenu getDetailMenu() {
        return this.detailMenu;
    }

    private Widget createSequenceUploadPanel() {
        FlexTable table = new FlexTable();
        table.setWidth("100%");

        String html = "<div style=\"outline:none;\"><span id=\"upload\"></span><span style=\"color: #777777;font-size: 9px;\">Fasta, GenBank, or ABI formats, optionally in zip file.</span></div>";
        HTMLPanel panel = new HTMLPanel(html);

        // form panel for file uploads (required use)
        FormPanel formPanel = new FormPanel();
        final FileUpload fileUpload = new FileUpload();

        formPanel.addSubmitHandler(new SubmitHandler() {

            @Override
            public void onSubmit(SubmitEvent event) {
                // TODO : 
            }
        });

        formPanel.setAction(GWT.getModuleBaseURL() + "file");

        fileUpload.setStyleName("input_box");
        formPanel.add(fileUpload);
        panel.add(formPanel, "upload");
        table.setWidget(0, 0, panel);

        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.appendHtmlConstant("<span>");
        builder.appendEscaped(formatDate(new Date()));
        builder.appendHtmlConstant("</span><br /><span>");

        builder.appendHtmlConstant("by <a href='" + AppController.accountInfo.getEmail() + "'>"
                + AppController.accountInfo.getFirstName() + " "
                + AppController.accountInfo.getLastName() + "</a></span>");

        table.setWidget(0, 1, new HTML(builder.toSafeHtml().asString()));
        table.getFlexCellFormatter().setWidth(0, 1, "20%");

        return table;
    }

    protected String formatDate(Date date) {
        if (date == null)
            return "";

        DateTimeFormat format = DateTimeFormat.getFormat("EEE MMM d, y h:m a");
        return format.format(date);
    }

    @Override
    public void setMenuItems(ArrayList<MenuItem> items) {
        this.detailMenu.setMenuItems(items);
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public Button showEntryDetailView(EntryDetailView<? extends EntryInfo> view) {
        if (generalHeaderPanel == null) {
            generalHeaderPanel = new HTMLPanel(
                    "<span class=\"entry_general_info_header\">GENERAL INFORMATION</span> &nbsp; <span id=\"edit_button\"></span>");
            editGeneralButton = new Button("Edit");
            editGeneralButton.setStyleName("top_menu");
            generalHeaderPanel.add(editGeneralButton, "edit_button");
        }

        mainContent.setWidget(0, 0, generalHeaderPanel);
        mainContent.setWidget(1, 0, view);
        mainContent.getCellFormatter().setHeight(0, 0, "26px");
        return editGeneralButton;
    }

    @Override
    public Button showSampleView(EntrySampleTable table) {
        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        TablePager pager = new TablePager();
        pager.setDisplay(table);
        panel.add(pager);
        panel.setCellHorizontalAlignment(pager, HasAlignment.ALIGN_RIGHT);

        // add new sample 
        panel.add(sampleForm);

        // end add new sample
        panel.add(table);

        if (samplesPanel == null) {
            samplesPanel = new HTMLPanel(
                    "<span>Samples</span> &nbsp; <span id=\"add_sample_button\"></span>");
            addSampleButton = new Button("Add");
            addSampleButton.setStyleName("top_menu");
            samplesPanel.add(addSampleButton, "add_sample_button");
        }

        mainContent.setWidget(0, 0, samplesPanel);
        mainContent.setWidget(1, 0, panel);
        return addSampleButton;
    }

    @Override
    public Button showSequenceView(SequenceTable table, Flash flash) {
        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        TablePager pager = new TablePager();
        pager.setDisplay(table);
        panel.add(pager);
        panel.setCellHorizontalAlignment(pager, HasAlignment.ALIGN_RIGHT);
        panel.add(uploadPanel);
        panel.add(table);
        panel.add(flash);
        panel.setCellHeight(flash, "600px");

        if (seqPanel == null) {
            seqPanel = new HTMLPanel(
                    "<span>Sequence Analysis</span> <span id=\"add_trace_button\"></span>");
            addSeqButton = new Button("Add");
            addSeqButton.setStyleName("top_menu");
            seqPanel.add(addSeqButton, "add_trace_button");
        }

        mainContent.setWidget(0, 0, seqPanel);
        mainContent.setWidget(1, 0, panel);
        return addSeqButton;
    }

    @Override
    public void setEntryName(String name) {
        detailMenu.setHeader(name);
    }

    @Override
    public CreateSampleForm getSampleForm() {
        return this.sampleForm;
    }

    @Override
    public CellList<AttachmentInfo> getAttachmentList() {
        return attachmentList;
    }

    public EntryDetailViewMenu getEntryViewMenu() {
        return this.detailMenu;
    }

    @Override
    public PermissionsWidget getPermissionsWidget() {
        return this.permissions;
    }
}
