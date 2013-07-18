package org.jbei.ice.client.bulkupload;

import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.bulkupload.model.NewBulkInput;
import org.jbei.ice.client.bulkupload.widget.CreatorWidget;
import org.jbei.ice.client.bulkupload.widget.PermissionsSelection;
import org.jbei.ice.client.bulkupload.widget.SaveDraftInput;
import org.jbei.ice.client.bulkupload.widget.SavedDraftsMenu;
import org.jbei.ice.client.bulkupload.widget.UploadCSV;
import org.jbei.ice.client.collection.add.menu.CreateEntryMenu;
import org.jbei.ice.client.collection.view.OptionSelect;
import org.jbei.ice.client.common.AbstractLayout;
import org.jbei.ice.client.common.FeedbackPanel;
import org.jbei.ice.client.common.util.ImageUtil;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.bulkupload.BulkUploadInfo;
import org.jbei.ice.lib.shared.dto.group.GroupInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * View for the bulk import page. Works with {@link BulkUploadPresenter}
 *
 * @author Hector Plahar
 */
public class BulkUploadView extends AbstractLayout implements IBulkUploadView {

    private SavedDraftsMenu draftsMenu;
    private SavedDraftsMenu pendingDraftsMenu;
    private FlexTable mainContent;
    private CreateEntryMenu createEntryMenu;
    private FeedbackPanel feedback;
    private FlexTable layout;
    private ToggleButton toggle;
    private Button saveButton;
    private Button approveButton;
    private HTML reset;
    private SaveDraftInput draftInput;
    private PermissionsSelection selection;
    private UploadCSV uploadCSV;
    private HTML updating;
    private HTML uploadName;
    private String lastUpdated;
    private HTML bulkImportDisplay;
    private HTMLPanel bulkImportHeader;
//    private SampleSelectionWidget sampleSelection;

    private HorizontalPanel headerPanel;
    private NewBulkInput sheet;
    private HTMLPanel menuPanel;

    private CreatorWidget creator;

    @Override
    protected void initComponents() {
        super.initComponents();
        draftsMenu = new SavedDraftsMenu("SAVED DRAFTS");
        pendingDraftsMenu = new SavedDraftsMenu("PENDING APPROVAL");
        pendingDraftsMenu.setVisible(false);
        createEntryMenu = new CreateEntryMenu("Select Type");
        feedback = new FeedbackPanel("450px");
        toggle = new ToggleButton(ImageUtil.getShowSideImage(), ImageUtil.getHideSideImage());
        toggle.setStyleName("bulk_import_menu_toggle");
        toggle.setVisible(false);
        toggle.setTitle("Toggle Drafts Menu");

        saveButton = new Button("Submit");

        reset = new HTML("Reset");
        reset.setStyleName("display-inline");
        reset.addStyleName("footer_feedback_widget");
        reset.addStyleName("font-80em");

        approveButton = new Button("Approve");
        draftInput = new SaveDraftInput();
        draftInput.setVisible(false);

        updating = new HTML("<span style=\"font-size: 11px; font-weight: normal; color: #999\">"
                                    + "<i class=\"icon-spinner icon-spin icon-1x\"></i> Saving</span>");
        updating.setStyleName("display-inline");
        updating.addStyleName("relative_top_3");
        updating.setVisible(false);
        selection = new PermissionsSelection();
        uploadCSV = new UploadCSV();
        creator = new CreatorWidget(ClientController.account.getFullName(), ClientController.account.getEmail());

        uploadName = new HTML();
        uploadName.setStyleName("display-inline");
        uploadName.addStyleName("cursor_pointer");
        bulkImportDisplay = new HTML("");

        bulkImportHeader = new HTMLPanel(
                "<span id=\"bulk_import_display_type\"></span>"
                        + "<span style=\"vertical-align: middle; float:left\" id=\"upload_name\"></span>"
                        + "<span style=\"vertical-align: middle; float:left\" id=\"draft_name\"></span>"
                        + "&nbsp;&nbsp;<span style=\"vertical-align: middle; float:left\" "
                        + "id=\"updating_icon\"></span></span>"
                        + "<span style=\"float: right;\">"
//                        + "<span id=\"bulk_import_upload_csv\"></span>"
//                        + "<span style=\"font-weight: normal; color: #ccc\">&nbsp;&nbsp;|&nbsp;&nbsp;</span>"
                        + "<span id=\"bulk_import_permission_selection\"></span>"
                        + "<span style=\"font-weight: normal; color: #ccc\">&nbsp;&nbsp;|&nbsp;&nbsp;</span>"
                        + "<span id=\"creator\"></span>"
//                        + "<span style=\"font-weight: normal; color: #ccc\">&nbsp;&nbsp;|&nbsp;&nbsp;</span>"
//                        + "<span id=\"sample_selection_widget\"></span>
                        + "</span>");

        bulkImportHeader.setStyleName("bulk_import_header");
        bulkImportHeader.add(bulkImportDisplay, "bulk_import_display_type");
        bulkImportHeader.add(draftInput, "draft_name");
        bulkImportHeader.add(uploadName, "upload_name");
        bulkImportHeader.add(updating, "updating_icon");
//        bulkImportHeader.add(uploadCSV, "bulk_import_upload_csv");
        bulkImportHeader.add(selection.asWidget(), "bulk_import_permission_selection");
        bulkImportHeader.add(creator.asWidget(), "creator");
        initHandlers();
    }

    protected void initHandlers() {
        uploadName.addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                updating.setHTML("<span style=\"color: #999\"><i class=\"" + FAIconType.EDIT.getStyleName()
                                         + " font-80em\"></i></span>");
                updating.setVisible(true);
            }
        });

        uploadName.addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                if (lastUpdated != null) {
                    updating.setHTML("<span style=\"font-size: 11px; font-weight: normal; color: #999\">Updated: "
                                             + lastUpdated + "</span>");
                    updating.setVisible(true);
                    return;
                }
                updating.setHTML("<span style=\"font-size: 11px; font-weight: normal; color: #999\">"
                                         + "<i class=\"icon-spinner icon-spin icon-1x\"></i> Saving</span>");
                updating.setVisible(false);
            }
        });

        uploadName.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                uploadName.setVisible(false);
                draftInput.setRename(uploadName.getText());
                draftInput.setVisible(true);
                updating.setVisible(false);
            }
        });

        draftInput.setCancelHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                uploadName.setVisible(true);
                draftInput.setVisible(false);
                if (lastUpdated != null) {
                    updating.setHTML("<span style=\"font-size: 11px; font-weight: normal; color: #999\">Updated: "
                                             + lastUpdated + "</span>");
                    updating.setVisible(true);
                    return;
                }
            }
        });
    }

    @Override
    public void setPermissionDelegate(ServiceDelegate<Set<GroupInfo>> handler) {
        selection.setPermissionUpdateDelegate(handler);
    }

    @Override
    protected Widget createContents() {
        layout = new FlexTable();
        layout.setWidth("100%");
        layout.setCellPadding(0);
        layout.setCellSpacing(0);

        menuPanel = new HTMLPanel(
                "<br><span id=\"saved_drafts\"></span><br><span id=\"pending_approvals\"></span><br>");
        menuPanel.add(pendingDraftsMenu, "pending_approvals");
        menuPanel.add(draftsMenu, "saved_drafts");

        layout.setWidget(0, 0, menuPanel);
        layout.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);
        menuPanel.setVisible(false);

        // right content. fills entire space when there are no drafts
        layout.setWidget(0, 2, createMainContent());
        layout.getFlexCellFormatter().setVerticalAlignment(0, 2, HasAlignment.ALIGN_TOP);

        return layout;
    }

    @Override
    public void setPermissionGroups(ArrayList<GroupInfo> groups) {
        selection.setData(groups);
    }

    @Override
    public void setSelectedPermissionGroups(ArrayList<OptionSelect> groups) {
        selection.setEnabled(groups);
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
    public void setApproveHandler(ClickHandler handler) {
        this.approveButton.addClickHandler(handler);
    }

    @Override
    public String getCreator() {
        return this.creator.getCreator();
    }

    @Override
    public String getCreatorEmail() {
        return this.creator.getCreatorEmail();
    }

    @Override
    public void setCreatorInformation(String name, String email) {
        this.creator.setCreator(name);
        this.creator.setCreatorEmail(email);
    }

    @Override
    public void updateBulkUploadDraftInfo(BulkUploadInfo result) {
        lastUpdated = DateUtilities.formatShorterDate(result.getLastUpdate());
        String email = result.getAccount() == null ?
                ClientController.account.getEmail() : result.getAccount().getEmail();
        String name = result.getName() == null ? "Untitled" : result.getName();
        BulkUploadMenuItem item = new BulkUploadMenuItem(result.getId(), name, result.getCount(),
                                                         DateUtilities.formatMediumDate(result.getCreated()),
                                                         result.getType().getDisplay(), email);
        draftsMenu.updateMenuItem(item);
        if (draftsMenu.getCount() == 1) {
            setToggleMenuVisibility(true);
        }
    }

    @Override
    public void setLastUpdated(Date date) {
        lastUpdated = DateUtilities.formatShorterDate(date);
    }

    @Override
    public void setResetHandler(ClickHandler resetHandler) {
        this.reset.addClickHandler(resetHandler);
    }

    protected Widget createMainContent() {
        mainContent = new FlexTable(); // wrapper
        mainContent.setCellPadding(0);
        mainContent.setCellSpacing(0);
        mainContent.setWidth("100%");

        headerPanel = new HorizontalPanel();
        headerPanel.setWidth("140px");
        headerPanel.add(toggle);
        headerPanel.add(createEntryMenu);
        headerPanel.setCellHorizontalAlignment(createEntryMenu, HasAlignment.ALIGN_LEFT);
        mainContent.setWidget(0, 0, headerPanel);
        mainContent.getFlexCellFormatter().setWidth(0, 0, "140px");

        mainContent.setHTML(1, 0,
                            "<div style=\"font-family: Arial; border: 1px solid #e4e4e4; padding: 10px; "
                                    + "margin-top: 17px; background-color: #f1f1f1\"><p>Select the type "
                                    + "of entry you wish to bulk import.</p> <p>Please note that columns"
                                    + " with headers indicated by <span class=\"required\">*</span> "
                                    + "are required. You will not be able to submit the form until you enter a "
                                    + "value for those fields. The forms are automatically saved as a draft, "
                                    + "which will only be visible to you.</p>"
                                    + "<p>After submitting a saved draft or bulk upload, "
                                    + "an administrator must approve your"
                                    + " submission before they will show up in search listings for others. You will "
                                    + "however still be able to view and modify them on the collections page" +
                                    ".</p></div>");
        return mainContent;
    }

    @Override
    public void setLoading(boolean set) {
        HTML html = new HTML(
                "<div style=\"margin-top: 17px; border: 1px solid #e4e4e4; background-color: #f3f3f3; padding: 10px; "
                        + "opacity: 0.3\"><i class=\"icon-spinner icon-spin icon-3x\"></i><br><h2>LOADING " +
                        "CONTENT</h2></div>");
        mainContent.setWidget(1, 0, html);
        mainContent.getFlexCellFormatter().setHorizontalAlignment(1, 0, HasAlignment.ALIGN_CENTER);
        if (mainContent.getRowCount() > 2) {
            for (int i = 2; i < mainContent.getRowCount(); i += 1)
                mainContent.removeRow(i);
        }
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void setSheet(NewBulkInput bulkImport, boolean isNew, boolean isValidation) {
        FlexTable panel = new FlexTable();
        panel.setCellPadding(0);
        panel.setCellSpacing(0);
        panel.setWidth("100%");
        sheet = bulkImport;
        uploadCSV.setType(sheet.getImportType());
        feedback.setVisible(false);
        panel.setWidget(0, 0, feedback);
        Widget widget;

        if (!isNew) {
            if (isValidation) {
                widget = approveButton;
            } else {
                String html = "<span id=\"save_button\"></span> &nbsp; <span id=\"reset_label\"></span>";
                HTMLPanel htmlPanel = new HTMLPanel(html);
                htmlPanel.add(saveButton, "save_button");
                htmlPanel.add(reset, "reset_label");
                widget = htmlPanel;
            }
        } else {
            draftInput.reset();
            String html = "<span id=\"save_button\"></span>&nbsp;<span id=\"reset_label\"></span>";
            HTMLPanel htmlPanel = new HTMLPanel(html);
            htmlPanel.add(saveButton, "save_button");
            htmlPanel.add(reset, "reset_label");
            widget = htmlPanel;
            lastUpdated = null;
            updating.setVisible(false);
        }
        panel.setWidget(0, 1, widget);
        panel.getFlexCellFormatter().setHorizontalAlignment(0, 1, HasAlignment.ALIGN_RIGHT);
        mainContent.setWidget(0, 1, panel);

        String name = bulkImport.getName() == null ? "Untitled" : bulkImport.getName();
        uploadName.setHTML(name);
        bulkImportDisplay.setHTML("<span style=\"color: #888; letter-spacing: -1px; text-transform: uppercase; "
                                          + "vertical-align: middle; float: left\">"
                                          + bulkImport.getImportType().getDisplay() + " bulk upload&nbsp;</span>");

//        // TODO
//        if (sampleSelection == null) {
//            sampleSelection = bulkImport.getSampleSelectionWidget();
//            bulkImportHeader.add(sampleSelection, "sample_selection_widget");
//        } else
//            sampleSelection.setLocation(bulkImport.getSampleSelectionWidget().getCurrentLocation());

        mainContent.setWidget(1, 0, bulkImportHeader);
        mainContent.getFlexCellFormatter().setColSpan(1, 0, 3);

        mainContent.setWidget(2, 0, bulkImport.getSheet());
        mainContent.getFlexCellFormatter().setColSpan(2, 0, 3);
    }

    @Override
    public void setDraftNameSetHandler(final Delegate<String> handler) {
        draftInput.setKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getNativeEvent().getKeyCode() != KeyCodes.KEY_ENTER)
                    return;

                handler.execute(draftInput.getDraftName());
            }
        });
    }

    @Override
    public void setDraftName(long id, String name) {
        uploadName.setHTML(name);
        uploadName.setVisible(true);
        draftInput.setVisible(false);
    }

    @Override
    public void setUpdatingVisibility(boolean visible) {
        if (visible) {
            updating.setHTML("&nbsp;<span style=\"font-size: 11px; font-weight: normal; color: #999\">"
                                     + "<i class=\"icon-spinner icon-spin icon-1x\"></i> Saving</span>");
        } else {
            updating.setHTML(
                    "&nbsp;<span style=\"font-size: 11px; font-weight: normal; vertical-align: middle; color: #999\">"
                            + "Updated: " + lastUpdated + "</span>");
        }
        updating.setVisible(true);
    }

    @Override
    public void showFeedback(String msg, boolean isError) {
        if (isError)
            feedback.setFailureMessage(msg);
        else
            feedback.setSuccessMessage(msg);
    }

    @Override
    public void setSavedDraftsData(ArrayList<BulkUploadMenuItem> data, String lastSaved, IDeleteMenuHandler handler) {
        draftsMenu.setMenuItems(data, handler);
        this.lastUpdated = lastSaved;

        menuPanel.setVisible(true);
        layout.getFlexCellFormatter().setWidth(0, 0, "220px");

        layout.setHTML(0, 1, "&nbsp;");
        layout.getFlexCellFormatter().setWidth(0, 1, "10px");

        toggle.setVisible(true);
        toggle.setDown(true);
        headerPanel.setCellHorizontalAlignment(createEntryMenu, HasAlignment.ALIGN_CENTER);

        updating.setHTML("<span style=\"font-size: 11px; font-weight: normal; vertical-align: middle; color: #999\">"
                                 + "Updated: " + lastSaved + "</span>");
        updating.setVisible(true);
    }

    @Override
    public void setPendingDraftsData(ArrayList<BulkUploadMenuItem> data, IRevertBulkUploadHandler handler) {
        pendingDraftsMenu.setMenuItems(data, handler);
        pendingDraftsMenu.setVisible(true);
        menuPanel.setVisible(true);

        layout.getFlexCellFormatter().setWidth(0, 0, "220px");

        layout.setHTML(0, 1, "&nbsp;");
        layout.getFlexCellFormatter().setWidth(0, 1, "10px");

        toggle.setVisible(true);
        toggle.setDown(true);
        headerPanel.setCellHorizontalAlignment(createEntryMenu, HasAlignment.ALIGN_CENTER);
    }

    @Override
    public void setDraftMenuVisibility(boolean visible, boolean isToggleClick) {
        layout.getWidget(0, 0).setVisible(visible);
        toggle.setDown(visible);
        if (visible)
            headerPanel.setCellHorizontalAlignment(createEntryMenu, HasAlignment.ALIGN_CENTER);
        else
            headerPanel.setCellHorizontalAlignment(createEntryMenu, HasAlignment.ALIGN_LEFT);

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
        return createEntryMenu.getSelectionModel();
    }

    @Override
    public boolean getMenuVisibility() {
        return layout.getWidget(0, 0).isVisible();
    }
}
