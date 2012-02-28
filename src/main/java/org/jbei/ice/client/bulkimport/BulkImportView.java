package org.jbei.ice.client.bulkimport;

import java.util.ArrayList;

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

        // other headers can go here

        // feedback panel
        mainContent.setWidget(0, 1, feedback);
        mainContent.getFlexCellFormatter().setHorizontalAlignment(0, 1, HasAlignment.ALIGN_RIGHT);

        // space
        mainContent.setHTML(1, 0, "&nbsp;&nbsp;");
        mainContent.getFlexCellFormatter().setColSpan(1, 0, 2);

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
    public void setSheet(Widget sheet) {
        this.mainContent.setWidget(2, 0, sheet);
    }

    @Override
    public void showFeedback(String msg, boolean isError) {
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
