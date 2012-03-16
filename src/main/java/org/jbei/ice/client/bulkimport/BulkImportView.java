package org.jbei.ice.client.bulkimport;

import java.util.ArrayList;

import org.jbei.ice.client.bulkimport.model.NewBulkInput;
import org.jbei.ice.client.bulkimport.panel.SheetHeaderPanel;
import org.jbei.ice.client.collection.add.menu.CreateEntryMenu;
import org.jbei.ice.client.collection.menu.CollectionMenu;
import org.jbei.ice.client.collection.menu.MenuItem;
import org.jbei.ice.client.common.AbstractLayout;
import org.jbei.ice.client.common.FeedbackPanel;
import org.jbei.ice.shared.EntryAddType;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;

public class BulkImportView extends AbstractLayout implements IBulkImportView {

    private CollectionMenu draftsMenu; // TODO
    private Label contentHeader;
    private FlexTable mainContent;
    private CreateEntryMenu create; // TODO: needs its own menu
    private FeedbackPanel feedback;
    private FlexTable layout;
    //    private ToggleButton toggle;

    private Button toggle;

    @Override
    protected void initComponents() {
        super.initComponents();
        draftsMenu = new CollectionMenu(false, "SAVED DRAFTS");
        create = new CreateEntryMenu();
        feedback = new FeedbackPanel("450px");
        contentHeader = new Label("");
        //        toggle = new ToggleButton("+", "-");

        toggle = new Button("+");
        //                toggle.setWidth("10px");
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
        layout.setHTML(0, 1, "<span style=\"width: 10px\">&nbsp;</span>");

        // right content. fills entire space when there are no drafts
        layout.setWidget(0, 2, createMainContent());
        //        layout.getFlexCellFormatter().setVerticalAlignment(0, 2, HasAlignment.ALIGN_TOP);

        return layout;
    }

    @Override
    public void addToggleMenuHandler(ClickHandler handler) {
        this.toggle.addClickHandler(handler);
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
                    "<br><div style=\"font-family: Arial; border: 1px solid #e4e4e4; padding: 10px; background-color: #f1f1f1\"><p>Select type "
                            + "of entry you wish to bulk import.</p> <p>Please note that columns"
                            + " with headers indicated by <span class=\"required\">*</span> "
                            + "are required. You will not be able to submit the form until you enter a "
                            + "value for those fields.</p>"
                            + "<p>After submitting, an administrator must approve your "
                            + "submission before it will show up in the search listings. Contact them if you are in a "
                            + "hurry.You may optionally save a named draft of a bulk import you are working on. This will not "
                            + "be submitted and you can continue working on it at a later time</p></div>");
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
    public void setSheet(NewBulkInput input) { // mainContent.setWidget(0, 0, create);

        SheetHeaderPanel header = input.getSheetHeaderPanel();
        mainContent.setWidget(0, 1, header.getDraftInput());
        mainContent.getFlexCellFormatter().setWidth(0, 1, "302px");
        mainContent.setWidget(0, 2, header.getDraftSave());
        mainContent.getFlexCellFormatter().setWidth(0, 2, "100px");

        // feedback
        mainContent.setWidget(0, 3, feedback);
        mainContent.setWidth("44%");

        // reset / save
        mainContent.setWidget(0, 4, header.getReset());
        mainContent.getFlexCellFormatter().setWidth(0, 4, "60px");
        mainContent.setWidget(0, 5, header.getSubmit());
        mainContent.getFlexCellFormatter().setWidth(0, 5, "60px");

        int index = mainContent.getCellCount(0);

        mainContent.setHTML(1, 0, "&nbsp;");
        mainContent.getFlexCellFormatter().setColSpan(1, 0, index);

        mainContent.setWidget(2, 0, contentHeader);
        mainContent.getCellFormatter().setStyleName(2, 0, "bulk_import_header");
        mainContent.getFlexCellFormatter().setColSpan(2, 0, index);

        this.mainContent.setWidget(3, 0, input.getSheet());
        mainContent.getFlexCellFormatter().setColSpan(3, 0, index);
    }

    @Override
    public void showFeedback(String msg, boolean isError) {
        if (isError)
            feedback.setFailureMessage(msg);
        else
            feedback.setSuccessMessage(msg);
    }

    @Override
    public void clearFeedback() {
        feedback.setVisible(false);
    }

    @Override
    public void setSavedDraftsData(ArrayList<MenuItem> data) {
        draftsMenu.setMenuItems(data);
        layout.setWidget(0, 0, draftsMenu);
    }

    @Override
    public void setMenuVisibility(boolean visible) {
        draftsMenu.setVisible(visible);

        if (!visible) {

            //            layout.setHTML(0, 0, "");
            layout.setHTML(0, 1, "");
        } else {
            //            layout.setWidget(0, 0, draftsMenu);
            layout.setHTML(0, 1, "<span style=\"width: 10px\">&nbsp;</span>");
        }
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
