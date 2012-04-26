package org.jbei.ice.client.bulkimport;

import java.util.ArrayList;

import org.jbei.ice.client.bulkimport.model.NewBulkInput;
import org.jbei.ice.client.bulkimport.widget.SaveDraftInput;
import org.jbei.ice.client.collection.add.menu.CreateEntryMenu;
import org.jbei.ice.client.collection.menu.CollectionMenu;
import org.jbei.ice.client.collection.menu.IDeleteMenuHandler;
import org.jbei.ice.client.collection.menu.MenuItem;
import org.jbei.ice.client.common.AbstractLayout;
import org.jbei.ice.client.common.FeedbackPanel;
import org.jbei.ice.client.common.util.ImageUtil;
import org.jbei.ice.shared.EntryAddType;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;

public class BulkImportView extends AbstractLayout implements IBulkImportView {

    private CollectionMenu draftsMenu; // TODO
    private Label contentHeader;
    private FlexTable mainContent;
    private CreateEntryMenu create; // TODO: needs its own menu
    private FeedbackPanel feedback;
    private FlexTable layout;
    private ToggleButton toggle;
    private Button updateButton;
    private Button saveButton;
    private Button resetButton;
    private SaveDraftInput draftInput;
    private Image uploadCsv;

    @Override
    protected void initComponents() {
        super.initComponents();
        draftsMenu = new CollectionMenu(false, "SAVED DRAFTS");
        create = new CreateEntryMenu();
        feedback = new FeedbackPanel("450px");
        contentHeader = new Label("");
        contentHeader.setStyleName("display-inline");
        toggle = new ToggleButton(ImageUtil.getShowSideImage(), ImageUtil.getHideSideImage());
        toggle.setStyleName("bulk_import_menu_toggle");
        toggle.setVisible(false);

        // bulk import draft update button
        updateButton = new Button("Update");
        updateButton.setStyleName("saved_draft_button");
        saveButton = new Button("Submit");
        saveButton.setStyleName("saved_draft_button");
        resetButton = new Button("Reset");
        resetButton.setStyleName("saved_draft_button");
        draftInput = new SaveDraftInput();
        uploadCsv = ImageUtil.getUploadImage();
    }

    @Override
    protected Widget createContents() {
        layout = new FlexTable();
        layout.setWidth("100%");
        layout.setCellPadding(0);
        layout.setCellSpacing(0);

        // placeholder for saved drafts menu
        layout.setHTML(0, 0, "");
        layout.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);

        layout.setHTML(0, 1, "&nbsp;");
        layout.getFlexCellFormatter().setWidth(0, 1, "10px");

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

        HTMLPanel panel = new HTMLPanel(
                "<span id=\"toggle_side\"></span>&nbsp<span id=\"create_btn\"></span>");
        panel.add(toggle, "toggle_side");
        panel.add(create.asWidget(), "create_btn");

        mainContent.setWidget(0, 0, panel);
        mainContent.getFlexCellFormatter().setWidth(0, 0, "150px");

        int count = mainContent.getCellCount(0);

        // space
        mainContent
                .setHTML(
                    1,
                    0,
                    "<br><div style=\"font-family: Arial; border: 1px solid #e4e4e4; padding: 10px; background-color: #f1f1f1\"><p>Select type the "
                            + "of entry you wish to bulk import.</p> <p>Please note that columns"
                            + " with headers indicated by <span class=\"required\">*</span> "
                            + "are required. You will not be able to submit the form until you enter a "
                            + "value for those fields. However, you may save incomplete forms as a named draft and continue working on it at a later time. "
                            + "Saved drafts will not be submitted and are only visible to you.</p>"
                            + "<p>After submitting, an administrator must approve your "
                            + "submission before it will show up in the search listings. Contact them if you are in a "
                            + "hurry.</p></div>");
        mainContent.getFlexCellFormatter().setColSpan(1, 0, (count + 1));

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
    public void setSheet(NewBulkInput bulkImport, boolean isNew) {

        HTMLPanel panel;
        if (!isNew) {
            panel = new HTMLPanel(
                    "<span style=\"vertical-align: middle\">"
                            + bulkImport.getName()
                            + "</span>"
                            + "<span style=\"float: right; text-align: right\" id=\"bulk_import_submit\">"
                            + "<span id=\"bulk_import_feedback\"></span> &nbsp; <span id=\"bulk_import_draft_update\"></span>&nbsp;</span>");

            panel.add(updateButton, "bulk_import_draft_update");
            panel.add(feedback, "bulk_import_feedback");
            feedback.addStyleName("display-inline");
            panel.add(saveButton, "bulk_import_submit");
        } else {
            panel = new HTMLPanel(
                    "<span style=\"vertical-align: middle\" id=\"bulk_import_input\"></span> "
                            + "<span style=\"float: right;  text-align: right\" id=\"bulk_import_submit\">"
                            + "<span id=\"bulk_import_feedback\"></span> &nbsp; <span id=\"bulk_import_draft_reset\"></span>&nbsp;</span>");

            panel.add(draftInput, "bulk_import_input");
            panel.add(resetButton, "bulk_import_draft_reset");
            panel.add(feedback, "bulk_import_feedback");
            feedback.addStyleName("display-inline");
            panel.add(saveButton, "bulk_import_submit");
        }

        mainContent.setWidget(0, 1, panel);

        int index = mainContent.getCellCount(0);

        mainContent.setHTML(1, 0, "&nbsp;");
        mainContent.getFlexCellFormatter().setColSpan(1, 0, index);

        HTMLPanel bulkImportHeader = new HTMLPanel(
                "<span id=\"bulk_import_header_title\"></span><span style=\"float: right\" id=\"upload_csv_icon\"></span>");
        bulkImportHeader.add(contentHeader, "bulk_import_header_title");
        bulkImportHeader.add(uploadCsv, "upload_csv_icon");

        mainContent.setWidget(2, 0, bulkImportHeader);
        mainContent.getCellFormatter().setStyleName(2, 0, "bulk_import_header");
        mainContent.getFlexCellFormatter().setColSpan(2, 0, index);

        this.mainContent.setWidget(3, 0, bulkImport.getSheet());
        mainContent.getFlexCellFormatter().setColSpan(3, 0, index);
    }

    @Override
    public String getDraftName() {
        return this.draftInput.getDraftName();
    }

    @Override
    public HandlerRegistration setDraftUpateHandler(ClickHandler handler) {
        return updateButton.addClickHandler(handler);
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
    public void setSavedDraftsData(ArrayList<MenuItem> data, IDeleteMenuHandler handler) {
        draftsMenu.setMenuItems(data, handler);
        layout.setWidget(0, 0, draftsMenu);
        layout.getFlexCellFormatter().setWidth(0, 0, "220px");
        toggle.setVisible(true);
        toggle.setDown(true);
    }

    @Override
    public void addSavedDraftData(MenuItem item, IDeleteMenuHandler handler) {
        draftsMenu.addMenuItem(item, handler);
        toggle.setVisible(true);
        toggle.setDown(true);
    }

    @Override
    public void setMenuVisibility(boolean visible) {
        draftsMenu.setVisible(visible);
        toggle.setDown(visible);

        if (!visible) {
            layout.setHTML(0, 0, "");
            layout.getFlexCellFormatter().setWidth(0, 0, "0px");

            layout.setHTML(0, 1, "");
            layout.getFlexCellFormatter().setWidth(0, 1, "0px");
        } else {
            layout.setWidget(0, 0, draftsMenu);
            layout.getFlexCellFormatter().setWidth(0, 0, "220px");

            layout.setHTML(0, 1, "&nbsp;");
            layout.getFlexCellFormatter().setWidth(0, 1, "10px");
        }
    }

    @Override
    public void setToggleMenuVisiblity(boolean visible) {
        toggle.setVisible(visible);
    }

    @Override
    public SingleSelectionModel<MenuItem> getDraftMenuModel() {
        return draftsMenu.getSelectionModel();
    }

    @Override
    public SingleSelectionModel<EntryAddType> getImportCreateModel() {
        return create.getSelectionModel();
    }

    @Override
    public boolean getMenuVisibility() {
        return draftsMenu.isVisible();
    }
}
