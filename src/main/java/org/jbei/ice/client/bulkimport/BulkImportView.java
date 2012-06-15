package org.jbei.ice.client.bulkimport;

import java.util.ArrayList;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.*;
import org.jbei.ice.client.admin.bulkimport.BulkImportMenuItem;
import org.jbei.ice.client.admin.bulkimport.IDeleteMenuHandler;
import org.jbei.ice.client.admin.bulkimport.SavedDraftsMenu;
import org.jbei.ice.client.bulkimport.model.NewBulkInput;
import org.jbei.ice.client.bulkimport.widget.SaveDraftInput;
import org.jbei.ice.client.bulkimport.widget.SelectTypeMenu;
import org.jbei.ice.client.common.AbstractLayout;
import org.jbei.ice.client.common.FeedbackPanel;
import org.jbei.ice.client.common.util.ImageUtil;
import org.jbei.ice.shared.EntryAddType;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.view.client.SingleSelectionModel;

public class BulkImportView extends AbstractLayout implements IBulkImportView {

    private SavedDraftsMenu draftsMenu;
    private Label contentHeader;
    private FlexTable mainContent;
    private SelectTypeMenu create;
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
        draftsMenu = new SavedDraftsMenu("SAVED DRAFTS");
        create = new SelectTypeMenu();
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

        HorizontalPanel headerPanel = new HorizontalPanel();
        headerPanel.setWidth("140px");
        headerPanel.add(toggle);
        headerPanel.add(create);
        headerPanel.setCellHorizontalAlignment(create, HasAlignment.ALIGN_CENTER);
        mainContent.setWidget(0, 0, headerPanel);
        mainContent.getFlexCellFormatter().setWidth(0, 0, "140px");

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

        FlexTable panel = new FlexTable();
        panel.setWidth("100%");
        if (!isNew) {
            panel.setWidget(0, 0, new HTML(bulkImport.getName()));
            panel.setWidget(0, 1, updateButton);
            panel.setWidget(0, 2, feedback);
            panel.setWidget(0, 3, resetButton);
            panel.getFlexCellFormatter().setWidth(0, 2, "40px");

            panel.setWidget(0, 4, saveButton);
            panel.getFlexCellFormatter().setWidth(0, 3, "70px");
        } else {

            panel.setWidget(0, 0, draftInput);
            panel.setWidget(0, 1, feedback);

            panel.setWidget(0, 2, resetButton);
            panel.getFlexCellFormatter().setWidth(0, 2, "40px");

            panel.setWidget(0, 3, saveButton);
            panel.getFlexCellFormatter().setWidth(0, 3, "70px");
        }

        mainContent.setWidget(0, 1, panel);

        mainContent.setHTML(1, 0, "&nbsp;");
        int index = mainContent.getCellCount(0);
        mainContent.getFlexCellFormatter().setColSpan(1, 0, index);

        HTMLPanel bulkImportHeader = new HTMLPanel(
                "<span id=\"bulk_import_header_title\"></span><span style=\"float: right\" id=\"upload_csv_icon\"></span>");
        bulkImportHeader.add(contentHeader, "bulk_import_header_title");
        bulkImportHeader.add(uploadCsv, "upload_csv_icon");

        mainContent.setWidget(2, 0, bulkImportHeader);
        mainContent.getCellFormatter().setStyleName(2, 0, "bulk_import_header");
        mainContent.getFlexCellFormatter().setColSpan(2, 0, index);

        mainContent.setWidget(3, 0, bulkImport.getSheet());
        mainContent.getFlexCellFormatter().setColSpan(3, 0, index);
    }

    @Override
    public String getDraftName() {
        return this.draftInput.getDraftName();
    }

    @Override
    public HandlerRegistration setDraftUpdateHandler(ClickHandler handler) {
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
    public void setSavedDraftsData(ArrayList<BulkImportMenuItem> data, IDeleteMenuHandler handler) {
        draftsMenu.setMenuItems(data, handler);
        layout.setWidget(0, 0, draftsMenu);
        layout.getFlexCellFormatter().setWidth(0, 0, "220px");

        layout.setHTML(0, 1, "&nbsp;");
        layout.getFlexCellFormatter().setWidth(0, 1, "10px");

        toggle.setVisible(true);
        toggle.setDown(true);
    }

    @Override
    public void addSavedDraftData(BulkImportMenuItem item, IDeleteMenuHandler handler) {
        draftsMenu.addMenuItem(item, handler);
        toggle.setVisible(true);
        toggle.setDown(true);
    }

    @Override
    public void setDraftMenuVisibility(boolean visible) {
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
    public void setToggleMenuVisibility(boolean visible) {
        toggle.setVisible(visible);
    }

    @Override
    public SingleSelectionModel<BulkImportMenuItem> getDraftMenuModel() {
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
