package org.jbei.ice.client.entry.view.view;

import java.util.ArrayList;
import java.util.Date;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.collection.add.EntryFormFactory;
import org.jbei.ice.client.collection.add.form.IEntryFormSubmit;
import org.jbei.ice.client.collection.add.form.SampleLocation;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.Flash;
import org.jbei.ice.client.entry.view.HasAttachmentDeleteHandler;
import org.jbei.ice.client.entry.view.ViewFactory;
import org.jbei.ice.client.entry.view.detail.EntryDetailView;
import org.jbei.ice.client.entry.view.detail.SequenceViewPanel;
import org.jbei.ice.client.entry.view.detail.SequenceViewPanelPresenter;
import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.client.entry.view.table.EntrySampleTable;
import org.jbei.ice.client.entry.view.table.EntrySequenceTable;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.SequenceAnalysisInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.MultiSelectionModel;
import gwtupload.client.IUploader;
import gwtupload.client.IUploader.OnFinishUploaderHandler;
import gwtupload.client.IUploader.OnStartUploaderHandler;
import gwtupload.client.SingleUploader;

public class EntryView extends Composite implements IEntryView {

    private FlexTable mainContent;
    private final AttachmentListMenu attachmentMenu;

    // general header
    private HorizontalPanel generalHeaderPanel;
    private HTML editGeneralButton;
    private HTML deleteLabel;
    private HandlerRegistration deleteRegistration;
    private HTML pipe;

    // sequence Analysis
    private HTML addSeqLabel;

    // samples
    private HTML addSampleLabel;
    private CreateSampleForm sampleForm;

    // permissions
    private final PermissionsWidget permissions;

    // visibility
    private final VisibilityWidget visibility;

    private final Widget uploadPanel;
    private FlexTable entryDetailMenuWrapper; // left side of the page with menu

    // navigation buttons for context navigation.
    private final PagerWidget contextPager;
    private final Label headerLabel;
    private final EntrySampleTable sampleTable;
    private final EntrySequenceTable sequenceTable;
    private final SequenceAnalysisHeaderPanel traceHeaderPanel;
    private HandlerRegistration sequenceUploadFinish;
    private SingleUploader sequenceUploader;

    private Button sequenceAddCancelbutton;
    private long entryId;

    // menu
    private EntryDetailViewMenu detailMenu;

    public EntryView() {
        permissions = new PermissionsWidget();
        visibility = new VisibilityWidget();
        headerLabel = new Label();
        uploadPanel = createSequenceUploadPanel();
        uploadPanel.setVisible(false);
        attachmentMenu = new AttachmentListMenu();
        contextPager = new PagerWidget();

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
        sampleTable = new EntrySampleTable();

        // sequence panel
        sequenceTable = new EntrySequenceTable();
        traceHeaderPanel = new SequenceAnalysisHeaderPanel(sequenceTable.getSelectionModel());

        // general panel
        initGeneralPanel();

        // audit trail
//        initAuditTrailPanel();
    }

    @Override
    public MultiSelectionModel<SequenceAnalysisInfo> getSequenceTableSelectionModel() {
        return sequenceTable.getSelectionModel();
    }

    @Override
    public void setSequenceDeleteHandler(ClickHandler handler) {
        traceHeaderPanel.setDeleteHandler(handler);
    }

    private void initGeneralPanel() {
        generalHeaderPanel = new HorizontalPanel();
        generalHeaderPanel.setVerticalAlignment(HasAlignment.ALIGN_MIDDLE);

        editGeneralButton = new HTML("<i class=\"" + FAIconType.EDIT.getStyleName()
                                             + "\" style=\"font-size: 16px;\"></i>");
        editGeneralButton.setStyleName("entry_edit_link");
        editGeneralButton.setTitle("Edit");
        deleteLabel = new HTML("<i class=\"" + FAIconType.TRASH.getStyleName()
                                       + "\" style=\"font-size: 16px\"></i>");
        deleteLabel.setStyleName("entry_delete_link");
        deleteLabel.setTitle("Delete");
        addSampleLabel = new HTML("<i class=\"" + FAIconType.EDIT.getStyleName()
                                          + "\" style=\"margin-right: 2px\"></i>Add Sample");
        addSampleLabel.setStyleName("entry_edit_link");
        addSeqLabel = new HTML("<i class=\"" + FAIconType.EDIT.getStyleName()
                                       + "\" style=\"margin-right: 2px\"></i>Add Sequence");
        addSeqLabel.setStyleName("entry_edit_link");
        headerLabel.setStyleName("entry_general_info_header");
        pipe = new HTML("&nbsp;|&nbsp;");
        pipe.addStyleName("color_eee");

        generalHeaderPanel.add(contextPager.getGoBack());
        generalHeaderPanel.add(headerLabel);
        generalHeaderPanel.add(editGeneralButton);
        generalHeaderPanel.add(pipe);
        generalHeaderPanel.add(deleteLabel);

        mainContent.setWidget(0, 0, generalHeaderPanel);
        mainContent.getCellFormatter().setHeight(0, 0, "30px");
    }

    protected Widget createMenu() {
        entryDetailMenuWrapper = new FlexTable();
        entryDetailMenuWrapper.setCellPadding(0);
        entryDetailMenuWrapper.setCellSpacing(0);
        this.detailMenu = new EntryDetailViewMenu();
        entryDetailMenuWrapper.setWidget(0, 0, contextPager);
        entryDetailMenuWrapper.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasAlignment.ALIGN_CENTER);
        entryDetailMenuWrapper.setWidget(1, 0, detailMenu);
        return entryDetailMenuWrapper;
    }

    @Override
    public IEntryFormSubmit showUpdateForm(EntryInfo info) {
        IEntryFormSubmit form = EntryFormFactory.updateForm(info);
        if (form == null)
            return form;

        mainContent.setWidget(1, 0, form.asWidget());
        return form;
    }

    @Override
    public void removeAttachment(AttachmentItem item) {
        attachmentMenu.removeAttachment(item);
    }

    @Override
    public void setSequenceFinishUploadHandler(OnFinishUploaderHandler handler) {
        if (sequenceUploadFinish != null)
            sequenceUploadFinish.removeHandler();

        sequenceUploadFinish = sequenceUploader.addOnFinishUploadHandler(handler);
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
                        + "<div style=\"padding-top: 20px\" class=\"entry_view_right_menu\" " +
                        "id=\"permissions_div\"></div>"
                        + "<div class=\"entry_view_right_menu\" id=\"visibility_div\"></div>&nbsp;");

        panel.add(entryDetailMenuWrapper, "entry_sub_header_div");
        panel.add(attachmentMenu, "attachments_div");
        panel.add(permissions, "permissions_div");
        panel.add(visibility, "visibility_div");

        mainContent.setWidget(1, 1, panel);
        mainContent.getFlexCellFormatter().setVerticalAlignment(1, 1, HasAlignment.ALIGN_TOP);

        return mainContent;
    }

    @Override
    public void setNextHandler(ClickHandler handler) {
        contextPager.setNextHandler(handler);
    }

    @Override
    public void setGoBackHandler(ClickHandler handler) {
        contextPager.setGoBackHandler(handler);
    }

    @Override
    public void setPrevHandler(ClickHandler handler) {
        contextPager.setPrevHandler(handler);
    }

    @Override
    public void enablePrev(boolean enabled) {
        contextPager.enablePrev(enabled);
    }

    @Override
    public void enableNext(boolean enabled) {
        contextPager.enableNext(enabled);
    }

    @Override
    public void setNavText(String text) {
        this.contextPager.setNavText(text);
    }

    @Override
    public void showContextNav(boolean show) {
        contextPager.setVisible(show);
    }

    @Override
    public EntryDetailViewMenu getDetailMenu() {
        return this.detailMenu;
    }

    private Widget createSequenceUploadPanel() {
        FlexTable table = new FlexTable();
        table.setWidth("100%");

        sequenceUploader = new SingleUploader();
        sequenceUploader.setAutoSubmit(true);
        sequenceUploader.addOnStartUploadHandler(new OnStartUploaderHandler() {

            @Override
            public void onStart(IUploader uploader) {
                uploader.setServletPath(uploader.getServletPath() + "?eid=" + entryId
                                                + "&type=sequence&sid=" + AppController.sessionId);
            }
        });

        String html = "<div style=\"outline:none; padding: 4px\"><span id=\"upload\"></span><span style=\"color: " +
                "#777777;font-size: 9px;\">Fasta, GenBank, or ABI formats, optionally in zip file.</span></div>";
        HTMLPanel panel = new HTMLPanel(html);
        panel.add(sequenceUploader, "upload");

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
        deleteLabel.setVisible(showEdit);
        generalHeaderPanel.remove(addSampleLabel);
        generalHeaderPanel.remove(addSeqLabel);
        generalHeaderPanel.add(editGeneralButton);
        generalHeaderPanel.add(pipe);
        generalHeaderPanel.add(deleteLabel);

        mainContent.setWidget(1, 0, detailView);
        SequenceViewPanel sequencePanel = detailView.getSequencePanel();

        this.permissions.addReadWriteLinks(showEdit);
        sequencePanel.getPresenter().setIsCanEdit(showEdit, deleteHandler);
        return sequencePanel.getPresenter();
    }

    @Override
    public void addSampleButtonHandler(ClickHandler handler) {
        addSampleLabel.addClickHandler(handler);
    }

    @Override
    public void addGeneralEditButtonHandler(ClickHandler handler) {
        editGeneralButton.addClickHandler(handler);
    }

    @Override
    public void addDeleteEntryHandler(ClickHandler handler) {
        if (deleteRegistration != null)
            deleteRegistration.removeHandler();

        deleteRegistration = deleteLabel.addClickHandler(handler);
    }

    @Override
    public void showSampleView() {
        generalHeaderPanel.remove(editGeneralButton);
        generalHeaderPanel.remove(deleteLabel);
        generalHeaderPanel.remove(addSeqLabel);
        generalHeaderPanel.remove(pipe);
        generalHeaderPanel.add(addSampleLabel);
        mainContent.setWidget(1, 0, sampleTable);
    }

    @Override
    public void showLoadingIndicator() {
        mainContent.setWidget(1, 0, new EntryLoadingWidget());
    }

    @Override
    public void showSequenceView(EntryInfo info, boolean showFlash) {
        generalHeaderPanel.remove(editGeneralButton);
        generalHeaderPanel.remove(deleteLabel);
        generalHeaderPanel.remove(addSampleLabel);
        generalHeaderPanel.remove(pipe);
        generalHeaderPanel.add(addSeqLabel);

        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        panel.add(traceHeaderPanel);
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
        generalHeaderPanel.remove(editGeneralButton);
        generalHeaderPanel.remove(deleteLabel);
        generalHeaderPanel.remove(addSeqLabel);
        generalHeaderPanel.remove(pipe);
        generalHeaderPanel.add(addSampleLabel);
        HTMLPanel panel = new HTMLPanel("<div id=\"create_sample_form\"></div><div id=\"sample_table\"></div>");
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
    public VisibilityWidgetPresenter getVisibilityWidget() {
        return this.visibility.getPresenter();
    }

    @Override
    public void setAttachments(ArrayList<AttachmentItem> items, long entryId) {
        attachmentMenu.setMenuItems(items, entryId);
    }

    @Override
    public void setAttachmentDeleteHandler(HasAttachmentDeleteHandler handler) {
        attachmentMenu.setDeleteHandler(handler);
    }

    @Override
    public void setTraceSequenceStartUploader(OnStartUploaderHandler handler) {
        sequenceUploader.addOnStartUploadHandler(handler);
    }

    @Override
    public void setSequenceData(ArrayList<SequenceAnalysisInfo> data, EntryInfo info) {
        sequenceTable.setData(data);
        this.entryId = info.getId();

        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        panel.add(traceHeaderPanel);
        panel.add(uploadPanel);
        panel.add(sequenceTable);

        if (info.isHasSequence()) {
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

    @Override
    public void addSequenceAddButtonHandler(ClickHandler clickHandler) {
        addSeqLabel.addClickHandler(clickHandler);
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
