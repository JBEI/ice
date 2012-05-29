package org.jbei.ice.client.entry.view.view;

import gwtupload.client.IUploadStatus.Status;
import gwtupload.client.IUploader;
import gwtupload.client.IUploader.OnFinishUploaderHandler;
import gwtupload.client.IUploader.OnStartUploaderHandler;
import gwtupload.client.IUploader.UploadedInfo;
import gwtupload.client.SingleUploader;

import java.util.ArrayList;
import java.util.Date;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.collection.add.form.SampleLocation;
import org.jbei.ice.client.common.util.ImageUtil;
import org.jbei.ice.client.common.widget.Flash;
import org.jbei.ice.client.entry.view.ViewFactory;
import org.jbei.ice.client.entry.view.detail.EntryDetailView;
import org.jbei.ice.client.entry.view.detail.SequenceViewPanel;
import org.jbei.ice.client.entry.view.detail.SequenceViewPanelPresenter;
import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.client.entry.view.table.EntrySampleTable;
import org.jbei.ice.client.entry.view.table.EntrySequenceTable;
import org.jbei.ice.client.entry.view.update.IEntryFormUpdateSubmit;
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
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class EntryView extends Composite implements IEntryView {

    private FlexTable mainContent;
    private final AttachmentListMenu attachmentMenu;

    // general header
    private HorizontalPanel generalHeaderPanel;
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

    private final Widget uploadPanel;
    private FlexTable entryDetailMenuWrapper; // left side of the page with menu

    // navigation buttons for context navigation.
    // TODO : create a widget for it
    private final Image goBack;
    private final Button leftBtn;
    private final Label navText;
    private final Button rightBtn;

    private final Label headerLabel;
    private final EntrySampleTable sampleTable;
    private final EntrySequenceTable sequenceTable;

    private Button sequenceAddCancelbutton;
    private long entryId;

    // menu
    private EntryDetailViewMenu detailMenu;

    public EntryView() {
        permissions = new PermissionsWidget();
        headerLabel = new Label();
        goBack = ImageUtil.getPrevIcon();
        goBack.setTitle("Back");
        goBack.setStyleName("cursor_pointer");

        leftBtn = new Button("&lt;");
        leftBtn.setStyleName("nav");
        leftBtn.addStyleName("nav-left");
        rightBtn = new Button("&gt;");
        rightBtn.setStyleName("nav");
        rightBtn.addStyleName("nav-right");
        navText = new Label();
        navText.setStyleName("display-inline");
        navText.addStyleName("font-80em");
        navText.addStyleName("pad-6");

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
        //        generalHeaderPanel = new HTMLPanel(
        //                "<span id=\"go_back_button\"></span> <span class=\"entry_general_info_header\" id=\"entry_header\"></span> &nbsp; <span id=\"edit_button\"></span>");
        generalHeaderPanel = new HorizontalPanel();
        generalHeaderPanel.setVerticalAlignment(HasAlignment.ALIGN_MIDDLE);
        editGeneralButton = new Button("Edit");
        editGeneralButton.setStyleName("top_menu");
        generalHeaderPanel.add(goBack);
        generalHeaderPanel.add(headerLabel);
        headerLabel.setStyleName("entry_general_info_header");
        generalHeaderPanel.add(editGeneralButton);
    }

    private void initSamplePanel() {
        samplesPanel = new HTMLPanel(
                "<span class=\"entry_general_info_header\">Samples</span> &nbsp; <span id=\"add_sample_button\"></span>");
        addSampleButton = new Button("Add");
        addSampleButton.setStyleName("top_menu");
        samplesPanel.add(addSampleButton, "add_sample_button");
    }

    protected Widget createMenu() {
        entryDetailMenuWrapper = new FlexTable();
        entryDetailMenuWrapper.setCellPadding(0);
        entryDetailMenuWrapper.setCellSpacing(0);
        this.detailMenu = new EntryDetailViewMenu();
        entryDetailMenuWrapper.setHTML(0, 0, "");
        entryDetailMenuWrapper.setWidget(1, 0, detailMenu);
        return entryDetailMenuWrapper;
    }

    @Override
    public IEntryFormUpdateSubmit showUpdateForm(EntryInfo info) {
        UpdateEntryForm<? extends EntryInfo> form = ViewFactory.getUpdateForm(info,
            AppController.autoCompleteData);
        if (form == null)
            return form;

        mainContent.setWidget(1, 0, form);
        return form;
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
        mainContent.setWidget(1, 0, new EntryLoadingWidget());
        mainContent.getFlexCellFormatter().setStyleName(1, 0, "entry_view_content");
        mainContent.getFlexCellFormatter().setVerticalAlignment(1, 0, HasAlignment.ALIGN_TOP);
        mainContent.getCellFormatter().setWidth(1, 0, "100%");

        HTMLPanel panel = new HTMLPanel(
                "<div class=\"entry_view_right_menu\" id=\"entry_sub_header_div\"></div>&nbsp;"
                        + "<div class=\"entry_view_right_menu\" id=\"attachments_div\"></div>"
                        + "<div style=\"padding-top: 20px\" class=\"entry_view_right_menu\" id=\"permissions_div\"></div>&nbsp;");

        panel.add(entryDetailMenuWrapper, "entry_sub_header_div");
        panel.add(attachmentMenu, "attachments_div");
        panel.add(permissions, "permissions_div");

        mainContent.setWidget(1, 1, panel);
        mainContent.getFlexCellFormatter().setVerticalAlignment(1, 1, HasAlignment.ALIGN_TOP);

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

            entryDetailMenuWrapper.setWidget(0, 0, panel);
            entryDetailMenuWrapper.getFlexCellFormatter().setHorizontalAlignment(0, 0,
                HasAlignment.ALIGN_CENTER);
            entryDetailMenuWrapper.getFlexCellFormatter().setStyleName(0, 0, "pad-6");
        } else {
            entryDetailMenuWrapper.setHTML(0, 0, "");
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
            @Override
            public void onFinish(IUploader uploader) {
                if (uploader.getStatus() == Status.SUCCESS) {
                    UploadedInfo info = uploader.getServerInfo();
                    // TODO : if info.message not empty, then we have a problem
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
    public SequenceViewPanelPresenter showEntryDetailView(EntryInfo info, boolean showEdit,
            DeleteSequenceHandler deleteHandler) {
        EntryDetailView<? extends EntryInfo> detailView = ViewFactory.createDetailView(info);
        detailView.getSequencePanel().setDeleteHandler(deleteHandler);
        editGeneralButton.setVisible(showEdit);
        mainContent.setWidget(0, 0, generalHeaderPanel);
        mainContent.getCellFormatter().setHeight(0, 0, "30px");
        mainContent.setWidget(1, 0, detailView);
        SequenceViewPanel sequencePanel = detailView.getSequencePanel();

        this.permissions.addReadWriteLinks(showEdit);
        sequencePanel.getPresenter().setIsCanEdit(showEdit, deleteHandler);
        return sequencePanel.getPresenter();
    }

    @Override
    public void addSampleButtonHandler(ClickHandler handler) {
        addSampleButton.addClickHandler(handler);
    }

    @Override
    public void addGeneralEditButtonHandler(ClickHandler handler) {
        editGeneralButton.addClickHandler(handler);
    }

    @Override
    public void showSampleView() {
        mainContent.setWidget(0, 0, samplesPanel);
        mainContent.setWidget(1, 0, sampleTable);
    }

    @Override
    public void showLoadingIndicator() {
        mainContent.setWidget(1, 0, new EntryLoadingWidget());
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
            params.setMovieName("SequenceChecker.swf");
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
    public void setSampleOptions(SampleLocation options) {
        sampleForm = new CreateSampleForm(options);
        sampleForm.setVisible(false);
        mainContent.setWidget(0, 0, samplesPanel);
        HTMLPanel panel = new HTMLPanel(
                "<div id=\"create_sample_form\"></div><div id=\"sample_table\"></div>");
        panel.add(sampleForm, "create_sample_form");
        panel.add(sampleTable, "sample_table");

        mainContent.setWidget(1, 0, panel);
    }

    @Override
    public void addSampleSaveHandler(ClickHandler handler) {
        if (sampleForm == null)
            return;
        sampleForm.addSaveHandler(handler);
    }

    @Override
    public SampleStorage getSampleAddFormValues() {
        if (sampleForm == null)
            return null;
        return sampleForm.populateSample();
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
    public PermissionsPresenter getPermissionsWidget() {
        return this.permissions.getPresenter();
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
