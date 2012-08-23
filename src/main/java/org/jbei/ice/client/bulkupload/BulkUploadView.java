package org.jbei.ice.client.bulkupload;

import java.util.ArrayList;

import org.jbei.ice.client.bulkupload.model.NewBulkInput;
import org.jbei.ice.client.bulkupload.widget.PermissionsSelection;
import org.jbei.ice.client.bulkupload.widget.SaveDraftInput;
import org.jbei.ice.client.bulkupload.widget.SavedDraftsMenu;
import org.jbei.ice.client.bulkupload.widget.SelectTypeMenu;
import org.jbei.ice.client.bulkupload.widget.UpdateDraftInput;
import org.jbei.ice.client.common.AbstractLayout;
import org.jbei.ice.client.common.FeedbackPanel;
import org.jbei.ice.client.common.util.ImageUtil;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.GroupInfo;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * View for the bulk import page. Works with {@link BulkUploadPresenter}
 *
 * @author Hector Plahar
 */
public class BulkUploadView extends AbstractLayout implements IBulkUploadView {

    private SavedDraftsMenu draftsMenu;
    private SavedDraftsMenu pendingDraftsMenu;
    private Label contentHeader;
    private FlexTable mainContent;
    private SelectTypeMenu create;
    private FeedbackPanel feedback;
    private FlexTable layout;
    private VerticalPanel menuPanel;
    private ToggleButton toggle;
    private Button saveButton;
    private Button approveButton;
    private Button resetButton;
    private SaveDraftInput draftInput;
    private UpdateDraftInput updateDraftInput;
    private PermissionsSelection selection;

    private HorizontalPanel headerPanel;
    private NewBulkInput sheet;

    @Override
    protected void initComponents() {
        super.initComponents();
        draftsMenu = new SavedDraftsMenu("SAVED DRAFTS");
        pendingDraftsMenu = new SavedDraftsMenu("PENDING");
        create = new SelectTypeMenu();
        feedback = new FeedbackPanel("450px");
        contentHeader = new Label("");
        contentHeader.setStyleName("display-inline");
        toggle = new ToggleButton(ImageUtil.getShowSideImage(), ImageUtil.getHideSideImage());
        toggle.setStyleName("bulk_import_menu_toggle");
        toggle.setVisible(false);

        saveButton = new Button("Submit");
        saveButton.setStyleName("saved_draft_button");
        resetButton = new Button("Reset");
        resetButton.setStyleName("saved_draft_button");
        approveButton = new Button("Approve");
        approveButton.setStyleName("saved_draft_button");
        draftInput = new SaveDraftInput();
        updateDraftInput = new UpdateDraftInput();

        selection = new PermissionsSelection();
    }

    @Override
    protected Widget createContents() {
        layout = new FlexTable();
        layout.setWidth("100%");
        layout.setCellPadding(0);
        layout.setCellSpacing(0);

        // placeholder for saved drafts menu
        menuPanel = new VerticalPanel();
        layout.setWidget(0, 0, menuPanel);
        layout.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);

        // right content. fills entire space when there are no drafts
        layout.setWidget(0, 2, createMainContent());
        layout.getFlexCellFormatter().setVerticalAlignment(0, 2, HasAlignment.ALIGN_TOP);

        return layout;
    }

    @Override
    public void addToggleMenuHandler(ClickHandler handler) {
        this.toggle.addClickHandler(handler);
    }

    @Override
    public void setSubmitHandler(ClickHandler submitHandler) {
        this.saveButton.addClickHandler(submitHandler);
    }

    @Override
    public void setDraftSubmitHandler(ClickHandler handler) {
        this.updateDraftInput.getSaveButton().addClickHandler(handler);
    }

    @Override
    public void setApproveHandler(ClickHandler handler) {
        this.approveButton.addClickHandler(handler);
    }

    @Override
    public void setGroupPermissions(ArrayList<GroupInfo> result) {
        selection.setGroups(result);
    }

    @Override
    public void setSelectedGroupPermission(GroupInfo groupInfo) {
        selection.setSelected(groupInfo);
    }

    @Override
    public void setResetHandler(ClickHandler resetHandler) {
        this.resetButton.addClickHandler(resetHandler);
    }

    @Override
    public void setDraftSaveHandler(ClickHandler draftSaveHandler) {
        this.draftInput.addSaveDraftHandler(draftSaveHandler);
    }

    protected Widget createMainContent() {

        mainContent = new FlexTable(); // wrapper
        mainContent.setCellPadding(0);
        mainContent.setCellSpacing(0);
        mainContent.setWidth("100%");

        headerPanel = new HorizontalPanel();
        headerPanel.setWidth("140px");
        headerPanel.add(toggle);
        headerPanel.add(create);
        headerPanel.setCellHorizontalAlignment(create, HasAlignment.ALIGN_LEFT);
        mainContent.setWidget(0, 0, headerPanel);
        mainContent.getFlexCellFormatter().setWidth(0, 0, "140px");

        mainContent
                .setHTML(
                        1,
                        0,
                        "<br><div style=\"font-family: Arial; border: 1px solid #e4e4e4; padding: 10px; "
                                + "background-color: #f1f1f1\"><p>Select type the "
                                + "of entry you wish to bulk import.</p> <p>Please note that columns"
                                + " with headers indicated by <span class=\"required\">*</span> "
                                + "are required. You will not be able to submit the form until you enter a "
                                + "value for those fields. However, you may save incomplete forms as a named draft "
                                + "and continue working on it at a later time. "
                                + "Saved drafts will not be submitted and are only visible to you.</p>"
                                + "<p>After submitting a saved draft or bulk upload, an administrator must approve your"
                                + " submission before it will show up in the search listings. Contact them if you are "
                                + "in a hurry.</p></div>");
        return mainContent;
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void setHeader(String header) {
        contentHeader.setText(header);
    }

    @Override
    public void setSheet(NewBulkInput bulkImport, boolean isNew, boolean isValidation) {

        FlexTable panel = new FlexTable();
        panel.setCellPadding(0);
        panel.setCellSpacing(0);
        panel.setWidth("100%");
        sheet = bulkImport;
        feedback.setVisible(false);

        if (!isNew) {
            if (isValidation) {
                // if validating, only show a "validate" button
                panel.setWidget(0, 0, new Label(bulkImport.getName()));
                panel.setWidget(0, 1, feedback);
                panel.setWidget(0, 2, approveButton);
                panel.getFlexCellFormatter().setWidth(0, 2, "70px");
            } else {
                updateDraftInput.setDraftName(bulkImport.getName());
                panel.setWidget(0, 0, updateDraftInput);
                panel.setWidget(0, 1, feedback);

                panel.setWidget(0, 2, resetButton);
                panel.getFlexCellFormatter().setWidth(0, 2, "40px");

                panel.setHTML(0, 3, "");
                panel.getFlexCellFormatter().setWidth(0, 3, "5px");

                panel.setWidget(0, 4, this.updateDraftInput.getSaveButton());
                panel.getFlexCellFormatter().setWidth(0, 4, "70px");
            }
        } else {
            draftInput.reset();
            panel.setWidget(0, 0, draftInput);
            panel.setWidget(0, 1, feedback);

            panel.setWidget(0, 2, resetButton);
            panel.getFlexCellFormatter().setWidth(0, 2, "40px");

            panel.setHTML(0, 3, "");
            panel.getFlexCellFormatter().setWidth(0, 3, "5px");

            panel.setWidget(0, 4, saveButton);
            panel.getFlexCellFormatter().setWidth(0, 4, "70px");
        }

        mainContent.setWidget(0, 1, panel);

        mainContent.setHTML(1, 0, "&nbsp;");
        int index = mainContent.getCellCount(0);
        mainContent.getFlexCellFormatter().setColSpan(1, 0, index);

        HTMLPanel bulkImportHeader = new HTMLPanel(
                "<span style=\"text-transform: uppercase\" id=\"bulk_import_header_title\"></span>" +
                        "<span style=\"float:right\" id=\"bulk_import_permission_selection\"></span>");
        bulkImportHeader.add(contentHeader, "bulk_import_header_title");
        bulkImportHeader.add(selection, "bulk_import_permission_selection");
        bulkImportHeader.setStyleName("bulk_import_header");

        mainContent.setWidget(2, 0, bulkImportHeader);
        mainContent.getFlexCellFormatter().setColSpan(2, 0, index);

        mainContent.setWidget(3, 0, bulkImport.getSheet());
        mainContent.getFlexCellFormatter().setColSpan(3, 0, index);
    }

    @Override
    public String getDraftName() {
        return this.draftInput.getDraftName();
    }

    @Override
    public void setDraftUpdateHandler(ClickHandler handler) {
        updateDraftInput.setUpdateDraftHandler(handler);
    }

    @Override
    public void showFeedback(String msg, boolean isError) {
        if (isError)
            feedback.setFailureMessage(msg);
        else
            feedback.setSuccessMessage(msg);

        new Timer() {

            @Override
            public void run() {
                feedback.setVisible(false);
            }
        }.schedule(25000);
    }

    @Override
    public void setSavedDraftsData(ArrayList<BulkUploadMenuItem> data, IDeleteMenuHandler handler) {
        draftsMenu.setMenuItems(data, handler);
        menuPanel.add(draftsMenu);
        menuPanel.add(new HTML("&nbsp;"));

        layout.getFlexCellFormatter().setWidth(0, 0, "220px");

        layout.setHTML(0, 1, "&nbsp;");
        layout.getFlexCellFormatter().setWidth(0, 1, "10px");

        toggle.setVisible(true);
        toggle.setDown(true);
        headerPanel.setCellHorizontalAlignment(create, HasAlignment.ALIGN_CENTER);
    }

    @Override
    public void setPendingDraftsData(ArrayList<BulkUploadMenuItem> data, IDeleteMenuHandler handler) {
        pendingDraftsMenu.setMenuItems(data, handler);
        menuPanel.add(pendingDraftsMenu);
        menuPanel.add(new HTML("&nbsp;"));

        layout.getFlexCellFormatter().setWidth(0, 0, "220px");

        layout.setHTML(0, 1, "&nbsp;");
        layout.getFlexCellFormatter().setWidth(0, 1, "10px");

        toggle.setVisible(true);
        toggle.setDown(true);
        headerPanel.setCellHorizontalAlignment(create, HasAlignment.ALIGN_CENTER);
    }

    @Override
    public void updateSavedDraftsMenu(BulkUploadMenuItem item) {
        draftsMenu.updateMenuItem(item);
    }

//    @Override
//    public void addSavedDraftData(BulkUploadMenuItem item, IDeleteMenuHandler handler) {
//        draftsMenu.addMenuItem(item, handler);
//        toggle.setVisible(true);
//        toggle.setDown(true);
//        headerPanel.setCellHorizontalAlignment(create, HasAlignment.ALIGN_CENTER);
//    }

    @Override
    public void setDraftMenuVisibility(boolean visible, boolean isToggleClick) {

        menuPanel.setVisible(visible);
        toggle.setDown(visible);
        if (visible)
            headerPanel.setCellHorizontalAlignment(create, HasAlignment.ALIGN_CENTER);
        else
            headerPanel.setCellHorizontalAlignment(create, HasAlignment.ALIGN_LEFT);

        if (!visible) {
            layout.getFlexCellFormatter().setWidth(0, 0, "0px");
            layout.setHTML(0, 1, "");
            layout.getFlexCellFormatter().setWidth(0, 1, "0px");
            if (isToggleClick)
                sheet.getSheet().increaseWidthBy(230);
        } else {
            layout.getFlexCellFormatter().setWidth(0, 0, "220px");

            layout.getFlexCellFormatter().setWidth(0, 1, "10px");
            if (isToggleClick)
                sheet.getSheet().decreaseWidthBy(230);
        }
    }

    @Override
    public void setToggleMenuVisibility(boolean visible) {
        toggle.setVisible(visible);
    }

    @Override
    public SingleSelectionModel<BulkUploadMenuItem> getDraftMenuModel() {
        return draftsMenu.getSelectionModel();
    }

    @Override
    public SingleSelectionModel<BulkUploadMenuItem> getPendingMenuModel() {
        return this.pendingDraftsMenu.getSelectionModel();
    }

    @Override
    public SingleSelectionModel<EntryAddType> getImportCreateModel() {
        return create.getSelectionModel();
    }

    @Override
    public boolean getMenuVisibility() {
        return menuPanel.isVisible();
    }

    @Override
    public String getPermissionSelection() {
        return selection.getSelectedGroupUUID();
    }
}
