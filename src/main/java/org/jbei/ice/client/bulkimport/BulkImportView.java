package org.jbei.ice.client.bulkimport;

import org.jbei.ice.client.common.AbstractLayout;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class BulkImportView extends AbstractLayout implements IBulkImportView {

    private BulkImportMenu menu;
    private SavedDraftsMenu draftsMenu;
    private Label contentHeader;
    private FlexTable mainContent;
    private VerticalPanel subContent;

    @Override
    protected void initComponents() {
        super.initComponents();
        draftsMenu = new SavedDraftsMenu();
        menu = new BulkImportMenu();
    }

    @Override
    protected Widget createContents() {
        FlexTable contentTable = new FlexTable();
        contentTable.setWidth("100%");
        contentTable.setCellPadding(0);
        contentTable.setCellSpacing(0);
        contentTable.setWidget(0, 0, menu);
        contentTable.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);
        contentTable.getFlexCellFormatter().setWidth(0, 0, "220px");

        contentTable.setHTML(1, 0, "&nbsp;");

        contentTable.setWidget(2, 0, draftsMenu);
        contentTable.getFlexCellFormatter().setVerticalAlignment(2, 0, HasAlignment.ALIGN_TOP);

        // TODO : middle sliver goes here
        contentTable.setHTML(0, 1, "&nbsp;");
        contentTable.getFlexCellFormatter().setRowSpan(0, 1, 3);

        contentTable.getFlexCellFormatter().setRowSpan(0, 2, 3);
        contentTable.setWidget(0, 2, createMainContent());
        contentTable.getFlexCellFormatter().setVerticalAlignment(0, 2, HasAlignment.ALIGN_TOP);

        return contentTable;
    }

    protected Widget createMainContent() {
        subContent = new VerticalPanel();
        subContent.setWidth("100%");
        contentHeader = new Label("Bulk Import");

        mainContent = new FlexTable(); // wrapper
        mainContent.setCellPadding(3);
        mainContent.setWidth("100%");
        mainContent.setCellSpacing(0);
        mainContent.addStyleName("add_new_entry_main_content_wrapper");
        mainContent.setWidget(0, 0, contentHeader);
        mainContent.getCellFormatter().setStyleName(0, 0, "add_new_entry_main_content_header");

        // sub content
        subContent.add(new HTML("<p>Please select the type of entry you wish to add. "
                + "<p>Fields indicated by <span class=\"required\">*</span> are required. "
                + "Other instructions here. Lorem ipsum."));
        mainContent.setWidget(1, 0, subContent);
        mainContent.getFlexCellFormatter().setStyleName(1, 0, "add_new_entry_sub_content");
        mainContent.getFlexCellFormatter().setColSpan(1, 0, 2);

        return mainContent;
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public BulkImportMenu getMenu() {
        return this.menu;
    }

    @Override
    public SavedDraftsMenu getDraftMenu() {
        return this.draftsMenu;
    }

    @Override
    public void setHeader(String header) {
        contentHeader.setText(header);
    }

    @Override
    public void setSheet(Widget sheet) {
        this.mainContent.setWidget(1, 0, sheet);
    }
}
