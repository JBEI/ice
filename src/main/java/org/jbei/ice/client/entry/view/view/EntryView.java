package org.jbei.ice.client.entry.view.view;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.MultiSelectionModel;
import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.collection.add.EntryFormFactory;
import org.jbei.ice.client.collection.add.form.IEntryFormSubmit;
import org.jbei.ice.client.collection.add.form.SampleLocation;
import org.jbei.ice.client.entry.view.ViewFactory;
import org.jbei.ice.client.entry.view.detail.EntryInfoView;
import org.jbei.ice.client.entry.view.detail.SequenceViewPanel;
import org.jbei.ice.client.entry.view.detail.SequenceViewPanelPresenter;
import org.jbei.ice.client.entry.view.handler.HasAttachmentDeleteHandler;
import org.jbei.ice.client.entry.view.model.FlagEntry;
import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.client.entry.view.panel.EntryCommentPanel;
import org.jbei.ice.client.entry.view.panel.EntrySamplePanel;
import org.jbei.ice.client.entry.view.panel.EntrySequenceAnalysisPanel;
import org.jbei.ice.shared.dto.SampleInfo;
import org.jbei.ice.shared.dto.comment.UserComment;
import org.jbei.ice.shared.dto.entry.AttachmentInfo;
import org.jbei.ice.shared.dto.entry.EntryInfo;
import org.jbei.ice.shared.dto.entry.EntryType;
import org.jbei.ice.shared.dto.entry.SequenceAnalysisInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Main view panel for showing details about a single entry
 *
 * @author Hector Plahar
 */
public class EntryView extends Composite implements IEntryView {

    private FlexTable mainContent;
    private final AttachmentListMenu attachmentMenu;

    // general header
    private FlexTable generalHeaderPanel;

    // edit / delete widget
    private EntryActionWidget entryAction;

    // permissions
    private final PermissionsWidget permissions;

    // visibility
    private final VisibilityWidget visibility;

    // navigation buttons for context navigation.
    private final PagerWidget contextPager;
    private final EntrySamplePanel samplePanel;
    private final EntryCommentPanel commentPanel;
    private final EntrySequenceAnalysisPanel sequencePanel;
    private DeleteSequenceHandler deleteSequenceHandler;
    private final EntryLoadingWidget loadingWidget;

    // menu
    private EntryViewMenu menu;
    private final HashMap<EntryType, EntryInfoView> viewCache;
    private EntryInfoView currentView;

    public EntryView(Delegate<Long> retrieveSequenceTracesDelegate) {
        permissions = new PermissionsWidget();
        visibility = new VisibilityWidget();

        attachmentMenu = new AttachmentListMenu();
        contextPager = new PagerWidget();
        entryAction = new EntryActionWidget();
        this.menu = new EntryViewMenu();

        FlexTable contentTable = new FlexTable();
        initWidget(contentTable);
        contentTable.setWidth("100%");
        contentTable.setCellPadding(0);
        contentTable.setCellSpacing(0);
        contentTable.setWidget(0, 1, createMainContent());
        contentTable.getCellFormatter().setWidth(0, 1, "100%");
        contentTable.getFlexCellFormatter().setVerticalAlignment(0, 1, HasAlignment.ALIGN_TOP);

        // sample panel
        samplePanel = new EntrySamplePanel();

        // sequence panel
        sequencePanel = new EntrySequenceAnalysisPanel(retrieveSequenceTracesDelegate);

        // general panel
        initGeneralPanel();

        // comment panel
        commentPanel = new EntryCommentPanel();

        // audit trail
//        initAuditTrailPanel();

        loadingWidget = new EntryLoadingWidget();
        entryAction.setVisible(false);
        viewCache = new HashMap<EntryType, EntryInfoView>();
    }

    @Override
    public void setDeleteSequenceHandler(DeleteSequenceHandler handler) {
        this.deleteSequenceHandler = handler;
    }

    @Override
    public MultiSelectionModel<SequenceAnalysisInfo> getSequenceTableSelectionModel() {
        return sequencePanel.getSelectionModel();
    }

    @Override
    public void setSequenceDeleteHandler(ClickHandler handler) {
        sequencePanel.setTraceSequenceDeleteHandler(handler);
    }

    private void initGeneralPanel() {
        generalHeaderPanel = new FlexTable();
        generalHeaderPanel.setCellPadding(0);
        generalHeaderPanel.setCellSpacing(0);

        // go back icon
        generalHeaderPanel.setWidget(0, 0, contextPager.getGoBack());
        generalHeaderPanel.setHTML(0, 1, "&nbsp;");
        generalHeaderPanel.setWidget(0, 2, entryAction);
//        generalHeaderPanel.getFlexCellFormatter().setVerticalAlignment(0, 2, HasAlignment.ALIGN_TOP);
//        generalHeaderPanel.getFlexCellFormatter().setStyleName(0, 2, "entry_general_info_header");

        mainContent.setWidget(0, 0, generalHeaderPanel);
        mainContent.getCellFormatter().setHeight(0, 0, "30px");
        mainContent.getFlexCellFormatter().setStyleName(0, 0, "entry_general_header_td");

        mainContent.setWidget(0, 1, contextPager);
        mainContent.getFlexCellFormatter().setStyleName(0, 1, "entry_general_header_td");
    }

    @Override
    public IEntryFormSubmit showUpdateForm(EntryInfo info) {
        if (info.getCreatorEmail() == null || info.getCreatorEmail().isEmpty())
            info.setCreatorEmail(ClientController.account.getEmail());
        if (info.getCreator() == null || info.getCreator().isEmpty())
            info.setCreator(ClientController.account.getFullName());

        IEntryFormSubmit form = EntryFormFactory.updateForm(info);
        if (form == null)
            return form;

        mainContent.setWidget(1, 0, form.asWidget());
        return form;
    }

    @Override
    public void showNewForm(IEntryFormSubmit form) {
        entryAction.setVisible(false);
        contextPager.setVisible(false);

        mainContent.setWidget(1, 0, form.asWidget());
        permissions.resetPermissionDisplay();
        attachmentMenu.reset();
        sequencePanel.reset();
        samplePanel.reset();
        visibility.setVisible(false);
        menu.reset();
        History.newItem(Page.COLLECTIONS.getLink() + ";id=0", false);
    }

    @Override
    public void removeAttachment(AttachmentItem item) {
        attachmentMenu.removeAttachment(item);
    }

    /**
     * Center content
     */
    protected Widget createMainContent() {
        mainContent = new FlexTable();
        mainContent.setWidth("100%");
        mainContent.setCellPadding(0);
        mainContent.setCellSpacing(0);

        mainContent.setHTML(0, 0, "&nbsp;");
        mainContent.setHTML(0, 1, "&nbsp;");

        // second row
        mainContent.setWidget(1, 0, new EntryLoadingWidget());
        mainContent.getFlexCellFormatter().setVerticalAlignment(1, 0, HasAlignment.ALIGN_TOP);
        mainContent.getCellFormatter().setWidth(1, 0, "100%");

        HTMLPanel panel = new HTMLPanel(
                "<div class=\"entry_view_right_menu\" id=\"entry_sub_header_div\"></div>&nbsp;"
                        + "<div class=\"entry_view_right_menu\" id=\"attachments_div\"></div>"
                        + "<div style=\"padding-top: 20px\" class=\"entry_view_right_menu\" "
                        + "id=\"permissions_div\"></div>"
                        + "<br><div class=\"entry_view_right_menu\" id=\"visibility_div\"></div>&nbsp;");

        panel.add(menu, "entry_sub_header_div");
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
    public EntryViewMenu getMenu() {
        return this.menu;
    }

    protected String formatDate(Date date) {
        if (date == null)
            return "";

        return DateTimeFormat.getFormat("MMM dd, yyyy h:mm a").format(date);
    }

    @Override
    @SuppressWarnings("unchecked")
    public SequenceViewPanelPresenter setEntryInfoForView(EntryInfo info, ServiceDelegate<SampleInfo> handler) {
        boolean showEdit = info.isCanEdit();
        currentView = viewCache.get(info.getType());
        if (currentView == null) {
            currentView = ViewFactory.createDetailView(info);
            viewCache.put(info.getType(), currentView);
        } else {
            currentView.setInfo(info);
        }

        deleteSequenceHandler.setEntryId(info.getId());
        currentView.getSequencePanel().setDeleteHandler(deleteSequenceHandler);
        entryAction.setVisible(showEdit);
        entryAction.setHasSample(info.isHasSample());

        mainContent.setWidget(1, 0, currentView);
        SequenceViewPanel sequenceViewPanel = currentView.getSequencePanel();

        this.permissions.addReadWriteLinks(showEdit);
        sequenceViewPanel.getPresenter().setIsCanEdit(showEdit, deleteSequenceHandler);

        getPermissionsWidget().setCanEdit(showEdit);
        getVisibilityWidget().setVisibility(info.getVisibility());

        String ownerId = info.getOwnerId() == 0 ? null : Long.toString(info.getOwnerId());
        setEntryHeader(info.getType().getDisplay(), info.getName(), info.getOwner(), ownerId, info.getCreationTime());

        // attachments
        ArrayList<AttachmentInfo> attachments = info.getAttachments();
        ArrayList<AttachmentItem> items = new ArrayList<AttachmentItem>();
        if (attachments != null) {
            for (AttachmentInfo attachmentInfo : attachments) {
                AttachmentItem item = new AttachmentItem(
                        attachmentInfo.getId(), attachmentInfo.getFilename(), attachmentInfo.getDescription());
                item.setFileId(attachmentInfo.getFileId());
                items.add(item);
            }
        }

        attachmentMenu.setMenuItems(items, info.getId());
        attachmentMenu.setCanEdit(info.isCanEdit());

        samplePanel.setData(info.getSampleStorage(), handler);
        sequencePanel.setSequenceData(info.getSequenceAnalysis(), info);
        commentPanel.setSampleOptions(info.getSampleStorage());
        return sequenceViewPanel.getPresenter();
    }


    @Override
    public void addSubmitCommentDelegate(ServiceDelegate<UserComment> delegate) {
        commentPanel.setCommentSubmitDelegate(delegate);
    }

    @Override
    public void addFlagDelegate(Delegate<FlagEntry> delegate) {
        entryAction.setFlagDelegate(delegate);
    }

    @Override
    public void addSampleButtonHandler(ClickHandler handler) {
        samplePanel.setAddSampleHandler(handler);
    }

    @Override
    public ArrayList<AttachmentItem> getAttachmentItems() {
        return attachmentMenu.getAttachmentItems();
    }

    @Override
    public void addGeneralEditButtonHandler(ClickHandler handler) {
        entryAction.addEditButtonHandler(handler);
    }

    @Override
    public void addDeleteEntryHandler(ClickHandler handler) {
        entryAction.addDeleteEntryHandler(handler);
    }

    @Override
    public void showSampleView() {
        mainContent.setWidget(1, 0, samplePanel);
    }

    @Override
    public void showCommentView(ArrayList<UserComment> comments) {
        commentPanel.setData(comments);
        mainContent.setWidget(1, 0, commentPanel);
    }

    @Override
    public void addComment(UserComment comment) {
        commentPanel.addComment(comment);
        menu.incrementMenuCount(MenuItem.Menu.COMMENTS);
    }

    @Override
    public void showLoadingIndicator(boolean showErrorLoad) {
        if (showErrorLoad)
            loadingWidget.showErrorLoad();
        else
            loadingWidget.showLoad();
        mainContent.setWidget(1, 0, loadingWidget);
        generalHeaderPanel.setHTML(0, 1, "");
    }

    @Override
    public void showSequenceView(EntryInfo info) {
        sequencePanel.setCurrentInfo(info);
        mainContent.setWidget(1, 0, sequencePanel);
    }

    @Override
    public void showEntryDetailView() {
        mainContent.setWidget(1, 0, currentView);
    }

    @Override
    public void setEntryHeader(String typeDisplay, String name, String owner, String ownerId, Date creationDate) {
        String html = "<span style=\"color: #888; letter-spacing: -1px;\">"
                + typeDisplay.toUpperCase() + "</span> "
                + name + "<br><span style=\"font-weight: normal; font-size: 10px; text-transform: uppercase; "
                + "color: #999;\">" + formatDate(creationDate);

        if (ownerId == null)
            html += " - <i>" + owner + "</i>";
        else
            html += " - <a href=\"#" + Page.PROFILE.getLink() + ";id=" + ownerId + ";s=profile\">" + owner + "</a>";

        html += "</span>";
        generalHeaderPanel.setHTML(0, 1, html);
        generalHeaderPanel.getFlexCellFormatter().setStyleName(0, 1, "entry_general_info_header");
    }

    @Override
    public void setSampleData(ArrayList<SampleStorage> data, ServiceDelegate<SampleInfo> deleteSampleHandler) {
        samplePanel.setData(data, deleteSampleHandler);
    }

    @Override
    public void setSampleOptions(SampleLocation options) {
        samplePanel.setSampleOptions(options);
    }

    @Override
    public void addSampleSaveHandler(ClickHandler handler) {
        samplePanel.addSampleSaveHandler(handler);
    }

    @Override
    public SampleStorage getSampleAddFormValues() {
        return samplePanel.getSampleAddFormValues();
    }

    @Override
    public boolean getSampleFormVisibility() {
        return samplePanel.getSampleFormVisibility();
    }

    @Override
    public void setSampleFormVisibility(boolean visible) {
        samplePanel.setSampleFormVisibility(visible);
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
    public void setAttachmentDeleteHandler(HasAttachmentDeleteHandler handler) {
        attachmentMenu.setDeleteHandler(handler);
    }

    @Override
    public void setSequenceData(ArrayList<SequenceAnalysisInfo> data, EntryInfo info) {
        sequencePanel.setSequenceData(data, info);
        mainContent.setWidget(1, 0, sequencePanel);
    }
}
