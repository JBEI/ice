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

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;

public class BulkImportView extends AbstractLayout implements IBulkImportView {

    private CollectionMenu draftsMenu; // TODO
    private Label contentHeader;
    private FlexTable mainContent;
    private CreateEntryMenu create; // TODO: 
    private FeedbackPanel feedback;
    private FlexTable layout;

    @Override
    protected void initComponents() {
        super.initComponents();
        draftsMenu = new CollectionMenu(false, "SAVED DRAFTS");
        create = new CreateEntryMenu();
        feedback = new FeedbackPanel("450px");
    }

    @Override
    protected Widget createContents() {
        layout = new FlexTable();
        layout.setWidth("100%");
        layout.setCellPadding(0);
        layout.setCellSpacing(0);

        // placeholder for saved drafts menu
        layout.setHTML(0, 0, "");

        // right content. fills entire space when there are no drafts
        layout.setWidget(0, 1, createMainContent());
        layout.getFlexCellFormatter().setVerticalAlignment(0, 1, HasAlignment.ALIGN_TOP);

        return layout;
    }

    protected Widget createMainContent() {

        mainContent = new FlexTable(); // wrapper
        mainContent.setCellPadding(0);
        mainContent.setCellSpacing(0);
        mainContent.setWidth("100%");

        mainContent.setWidget(0, 0, create);
        mainContent.getFlexCellFormatter().setWidth(0, 0, "110px");

        int count = mainContent.getCellCount(0);

        // space
        mainContent.setHTML(1, 0, "&nbsp;");
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
        mainContent.setWidth("47%");

        // reset / save
        mainContent.setWidget(0, 4, header.getReset());
        mainContent.getFlexCellFormatter().setWidth(0, 4, "60px");
        mainContent.setWidget(0, 5, header.getSubmit());
        mainContent.getFlexCellFormatter().setWidth(0, 5, "60px");

        int index = mainContent.getCellCount(0);

        mainContent.setHTML(1, 0, "&nbsp;");
        mainContent.getFlexCellFormatter().setColSpan(1, 0, index);

        this.mainContent.setWidget(2, 0, input.getSheet());
        mainContent.getFlexCellFormatter().setColSpan(2, 0, index);
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
        layout.getFlexCellFormatter().setWidth(0, 0, "220px");
        layout.setWidget(0, 0, draftsMenu);
        layout.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);
        layout.setHTML(0, 1, "&nbsp;");
        layout.getFlexCellFormatter().setRowSpan(0, 1, 3);
    }

    @Override
    public SingleSelectionModel<MenuItem> getDraftMenuModel() {
        return draftsMenu.getSelectionModel();
    }

    @Override
    public SingleSelectionModel<EntryAddType> getImportCreateModel() {
        return create.getSelectionModel();
    }
}
