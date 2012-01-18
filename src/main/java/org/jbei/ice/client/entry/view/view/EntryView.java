package org.jbei.ice.client.entry.view.view;

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

    private CellList<MenuItem> menu;
    private CellList<AttachmentInfo> attachmentList;
    private FlexTable mainContent;
    private FlexTable leftColumnLayout;
    private Button cancelAttachmentSubmission;
    private Button saveAttachment;
    private Widget attachmentForm;
    private Label permissionLink;

    // general header
    private HTMLPanel generalHeaderPanel;
    private Button editGeneralButton;

    // sequence Analysis
    private HTMLPanel seqPanel;
    private Button addSeqButton;

    // samples
    private HTMLPanel samplesPanel;
    private Button addSampleButton;

    // permissions
    private final PermissionsWidget permissions;

    private Widget uploadPanel;

    public EntryView() {
        permissions = new PermissionsWidget();
    }

    @Override
    protected void initComponents() {

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
        mainContent.setWidth("100%");
        mainContent.setWidget(0, 0, new Label(""));
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

    // aka createLeft()
    protected Widget createMenu() {
        leftColumnLayout = new FlexTable();
        leftColumnLayout.setCellPadding(3);
        leftColumnLayout.setCellSpacing(0);
        leftColumnLayout.addStyleName("entry_view_left_menu");
        leftColumnLayout.setHTML(0, 0, "&nbsp;");
        leftColumnLayout.getCellFormatter().setStyleName(0, 0, "entry_view_sub_menu_header");

        // cell to render value
        menu = new CellList<MenuItem>(new AbstractCell<MenuItem>() {

            @Override
            public void render(Context context, MenuItem value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<span>");
                sb.appendEscaped(value.getMenu().toString());
                if (value.getCount() >= 0) {
                    sb.appendHtmlConstant("<span class=\"menu_count\">");
                    sb.append(value.getCount());
                    sb.appendHtmlConstant("</span>");
                }

                sb.appendHtmlConstant("</span>");
            }
        });
        leftColumnLayout.setWidget(1, 0, menu);

        createPermissions();

        return leftColumnLayout;
    }

    private void createPermissions() {
        leftColumnLayout.setWidget(2, 0, new HTML("&nbsp;"));

        FlexTable permission = new FlexTable();
        permission.setCellPadding(0);
        permission.setCellSpacing(0);
        permissionLink = new HTML("</b>Permissions</b>");
        permission.setWidget(0, 0, permissionLink);

        leftColumnLayout.setWidget(3, 0, permission);
    }

    @Override
    public Label getPermissionLink() {
        return this.permissionLink;
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
    public CellList<MenuItem> getMenu() {
        return this.menu;
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public Button showEntryDetailView(EntryDetailView<? extends EntryInfo> view) {
        if (generalHeaderPanel == null) {
            generalHeaderPanel = new HTMLPanel(
                    "<span>General Information</span> &nbsp; <span id=\"edit_button\"></span>");
            editGeneralButton = new Button("Edit");
            editGeneralButton.setStyleName("top_menu");
            generalHeaderPanel.add(editGeneralButton, "edit_button");
        }

        mainContent.setWidget(0, 0, generalHeaderPanel);
        mainContent.setWidget(1, 0, view);
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

        CreateSampleForm sampleForm = new CreateSampleForm();
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
                    "<span>Sequence Analysis</span> &nbsp; <span id=\"add_trace_button\"></span>");
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
        leftColumnLayout.setHTML(0, 0, name);
    }

    @Override
    public CellList<AttachmentInfo> getAttachmentList() {
        return attachmentList;
    }

    @Override
    public PermissionsWidget getPermissionsWidget() {
        return this.permissions;
    }
}
