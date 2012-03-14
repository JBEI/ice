package org.jbei.ice.client.entry.view.view;

import gwtupload.client.IUploadStatus.Status;
import gwtupload.client.IUploader;
import gwtupload.client.IUploader.OnFinishUploaderHandler;
import gwtupload.client.IUploader.OnStartUploaderHandler;
import gwtupload.client.SingleUploader;

import java.util.ArrayList;
import java.util.Date;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.common.widget.Flash;
import org.jbei.ice.client.entry.view.ViewFactory;
import org.jbei.ice.client.entry.view.detail.EntryDetailView;
import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.client.entry.view.table.EntrySampleTable;
import org.jbei.ice.client.entry.view.table.EntrySequenceTable;
import org.jbei.ice.client.entry.view.update.UpdateEntryForm;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.SequenceAnalysisInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class EntryView extends Composite implements IEntryView {

    private FlexTable mainContent;
    private AttachmentListMenu attachmentMenu;

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
    private final Button goBack;
    private final Button leftBtn;
    private final Label navText;
    private final Button rightBtn;

    private Label headerLabel;
    private EntrySampleTable sampleTable;
    private EntrySequenceTable sequenceTable;

    private Button sequenceAddCancelbutton;
    private long entryId;

    // menu
    private EntryDetailViewMenu detailMenu;

    public EntryView() {
        permissions = new PermissionsWidget();
        headerLabel = new Label();
        headerLabel.setStyleName("display-inline");
        goBack = new Button("Back");
        leftBtn = new Button("Prev");
        leftBtn.setStyleName("nav");
        leftBtn.addStyleName("nav-left");
        rightBtn = new Button("Next");
        rightBtn.setStyleName("nav");
        rightBtn.addStyleName("nav-right");
        navText = new Label();
        navText.setStyleName("display-inline");
        navText.addStyleName("font-80em");
        navText.addStyleName("pad-6");

        sampleForm = new CreateSampleForm();
        sampleForm.setVisible(false);

        uploadPanel = createSequenceUploadPanel();
        uploadPanel.setVisible(false);
        attachmentMenu = new AttachmentListMenu();

        sequenceAddCancelbutton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                uploadPanel.setVisible(false);
            }
        });

        createMenu();

        FlexTable contentTable = new FlexTable();
        initWidget(contentTable);
        contentTable.setWidth("100%");
        contentTable.setCellPadding(0);
        contentTable.setCellSpacing(0);
        contentTable.setWidget(0, 1, createMainContent());
        contentTable.getCellFormatter().setWidth(0, 1, "100%");
        contentTable.getFlexCellFormatter().setVerticalAlignment(0, 1, HasAlignment.ALIGN_TOP);

        // sample panel
        initSamplePanel();
        sampleTable = new EntrySampleTable();
        sequenceTable = new EntrySequenceTable();

        // general panel
        initGeneralPanel();

        // sequence
        initSequencePanel();
    }

    private void initSequencePanel() {
        seqPanel = new HTMLPanel(
                "<span class=\"entry_general_info_header\">Sequence Analysis</span> <span id=\"add_trace_button\"></span>");
        addSeqButton = new Button("Add");
        addSeqButton.setStyleName("top_menu");
        seqPanel.add(addSeqButton, "add_trace_button");
    }

    private void initGeneralPanel() {
        generalHeaderPanel = new HTMLPanel(
                "<span id=\"go_back_button\"></span> <span class=\"entry_general_info_header\" id=\"entry_header\"></span> &nbsp; <span id=\"edit_button\"></span>");
        editGeneralButton = new Button("Edit");
        editGeneralButton.setStyleName("top_menu");
        generalHeaderPanel.add(editGeneralButton, "edit_button");
        generalHeaderPanel.add(goBack, "go_back_button");
        generalHeaderPanel.add(headerLabel, "entry_header");
    }

    private void initSamplePanel() {
        samplesPanel = new HTMLPanel(
                "<span class=\"entry_general_info_header\">Samples</span> &nbsp; <span id=\"add_sample_button\"></span>");
        addSampleButton = new Button("Add");
        addSampleButton.setStyleName("top_menu");
        samplesPanel.add(addSampleButton, "add_sample_button");
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
    public void showUpdateForm(EntryInfo info) {
        UpdateEntryForm<? extends EntryInfo> form = ViewFactory.getUpdateForm(info);
        if (form == null)
            return;
        mainContent.setWidget(1, 0, form);
    }

    /**
     * Center content
     */

    protected Widget createMainContent() {
        mainContent = new FlexTable();
        mainContent.setStyleName("entry_view_main_content_table");
        mainContent.setWidth("100%");
        mainContent.setCellPadding(0);
        mainContent.setCellSpacing(0);

        mainContent.setHTML(0, 0, "&nbsp;");
        mainContent.getFlexCellFormatter().setColSpan(0, 0, 2);

        // second row
        mainContent.setWidget(1, 0, new Label("Loading..."));
        mainContent.getFlexCellFormatter().setStyleName(1, 0, "entry_view_content");
        mainContent.getFlexCellFormatter().setVerticalAlignment(1, 0, HasAlignment.ALIGN_TOP);
        mainContent.getCellFormatter().setWidth(1, 0, "100%");
        mainContent.getFlexCellFormatter().setRowSpan(1, 0, 5);

        mainContent.setWidget(1, 1, left);
        mainContent.getFlexCellFormatter().setVerticalAlignment(1, 1, HasAlignment.ALIGN_TOP);

        mainContent.setHTML(2, 0, "&nbsp");

        mainContent.setWidget(3, 0, attachmentMenu);
        mainContent.getFlexCellFormatter().setStyleName(3, 0, "entry_view_right_menu");
        mainContent.getFlexCellFormatter().setVerticalAlignment(3, 0, HasAlignment.ALIGN_TOP);

        /*
        mainContent.setHTML(4, 0, "&nbsp");
        mainContent.setWidget(5, 0, new Label("FOO"));
        mainContent.getFlexCellFormatter().setStyleName(5, 0, "entry_view_right_menu");
        mainContent.getFlexCellFormatter().setVerticalAlignment(5, 0, HasAlignment.ALIGN_TOP);
        */

        return mainContent;
    }

    @Override
    public void setNextHandler(ClickHandler handler) {
        rightBtn.addClickHandler(handler);
    }

    @Override
    public void setGoBackHandler(ClickHandler handler) {
        goBack.addClickHandler(handler);
    }

    @Override
    public void setPrevHandler(ClickHandler handler) {
        leftBtn.addClickHandler(handler);
    }

    @Override
    public void enablePrev(boolean enabled) {
        leftBtn.setEnabled(enabled);
        if (enabled) {
            leftBtn.removeStyleName("nav_disabled");
            leftBtn.addStyleName("nav");
        } else {
            leftBtn.removeStyleName("nav");
            leftBtn.addStyleName("nav_disabled");
        }
    }

    @Override
    public void enableNext(boolean enabled) {
        rightBtn.setEnabled(enabled);
        if (enabled) {
            rightBtn.removeStyleName("nav_disabled");
            rightBtn.addStyleName("nav");
        } else {
            rightBtn.removeStyleName("nav");
            rightBtn.addStyleName("nav_disabled");
        }
    }

    @Override
    public void setNavText(String text) {
        this.navText.setText(text);
    }

    @Override
    public void showContextNav(boolean show) {
        if (show) {
            HTMLPanel panel = new HTMLPanel(
                    "<span id=\"leftBtn\"></span> <span id=\"navText\" class=\"font-bold\"></span><span id=\"rightBtn\"></span>");
            panel.add(leftBtn, "leftBtn");
            panel.add(navText, "navText");
            panel.add(rightBtn, "rightBtn");

            left.setWidget(0, 0, panel);
            left.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasAlignment.ALIGN_CENTER);
            left.getFlexCellFormatter().setStyleName(0, 0, "pad-6");
        } else {
            left.setHTML(0, 0, "");
        }

        goBack.setVisible(show);
    }

    @Override
    public EntryDetailViewMenu getDetailMenu() {
        return this.detailMenu;
    }

    private Widget createSequenceUploadPanel() {
        FlexTable table = new FlexTable();
        table.setWidth("100%");

        final SingleUploader uploader = new SingleUploader();
        uploader.setAutoSubmit(true);

        uploader.addOnStartUploadHandler(new OnStartUploaderHandler() {

            @Override
            public void onStart(IUploader uploader) {
                uploader.setServletPath(uploader.getServletPath() + "?eid=" + entryId
                        + "&type=sequence&sid=" + AppController.sessionId);
            }
        });

        uploader.addOnFinishUploadHandler(new OnFinishUploaderHandler() {
            public void onFinish(IUploader uploader) {
                if (uploader.getStatus() == Status.SUCCESS) {
                    //                    UploadedInfo info = uploader.getServerInfo();
                    uploader.reset();
                    uploadPanel.setVisible(false);
                } else {
                    // TODO : notify user of error
                }
            }
        });

        String html = "<div style=\"outline:none; padding: 4px\"><span id=\"upload\"></span><span style=\"color: #777777;font-size: 9px;\">Fasta, GenBank, or ABI formats, optionally in zip file.</span></div>";
        HTMLPanel panel = new HTMLPanel(html);
        panel.add(uploader, "upload");

        table.setWidget(0, 0, panel);
        sequenceAddCancelbutton = new Button("Cancel");
        table.setWidget(0, 1, sequenceAddCancelbutton);
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
    public void showEntryDetailView(EntryInfo info) {
        EntryDetailView<? extends EntryInfo> detailView = ViewFactory.createDetailView(info);
        mainContent.setWidget(0, 0, generalHeaderPanel);
        mainContent.setWidget(1, 0, detailView);
        mainContent.getCellFormatter().setHeight(0, 0, "30px");
    }

    @Override
    public void addSampleButtonHandler(ClickHandler handler) {
        addSampleButton.addClickHandler(handler);
    }

    public void addGeneralEditButtonHandler(ClickHandler handler) {
        editGeneralButton.addClickHandler(handler);
    }

    @Override
    public void showSampleView() {
        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");

        // add new sample 
        panel.add(sampleForm);

        // end add new sample
        panel.add(sampleTable);

        mainContent.setWidget(0, 0, samplesPanel);
        mainContent.setWidget(1, 0, panel);
    }

    @Override
    public void showSequenceView(EntryInfo info, boolean showFlash) {

        mainContent.setWidget(0, 0, seqPanel);

        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        panel.add(uploadPanel);
        panel.add(sequenceTable);

        if (showFlash) {
            Flash.Parameters params = new Flash.Parameters();
            params.setSwfPath("sc/SequenceChecker.swf");
            params.setSessiondId(AppController.sessionId);
            params.setEntryId(info.getRecordId());

            Flash flash = new Flash(params);
            panel.add(flash);
            panel.setCellHeight(flash, "600px");
        }

        mainContent.setWidget(1, 0, panel);
    }

    public void addSequenceButtonHandler(ClickHandler handler) {
        addSeqButton.addClickHandler(handler);
    }

    @Override
    public void setEntryName(String name) {
        headerLabel.setText(name);
    }

    @Override
    public void setSampleData(ArrayList<SampleStorage> data) {
        sampleTable.setData(data);
    }

    @Override
    public boolean getSampleFormVisibility() {
        return this.sampleForm.isVisible();
    }

    @Override
    public void setSampleFormVisibility(boolean visible) {
        this.sampleForm.setVisible(visible);
    }

    @Override
    public PermissionsWidget getPermissionsWidget() {
        return this.permissions;
    }

    @Override
    public void setAttachments(ArrayList<AttachmentItem> items, long entryId) {
        attachmentMenu.setMenuItems(items, entryId);
    }

    @Override
    public void setSequenceData(ArrayList<SequenceAnalysisInfo> data, long entryId) {
        sequenceTable.setData(data);
        this.entryId = entryId;
    }

    @Override
    public void addSequenceAddButtonHandler(ClickHandler clickHandler) {
        addSeqButton.addClickHandler(clickHandler);
    }

    @Override
    public boolean getSequenceFormVisibility() {
        return uploadPanel.isVisible();
    }

    @Override
    public void setSequenceFormVisibility(boolean visible) {
        uploadPanel.setVisible(visible);
    }
}
