package org.jbei.ice.client.entry.add;

import org.jbei.ice.client.common.AbstractLayout;
import org.jbei.ice.client.entry.add.form.NewEntryForm;
import org.jbei.ice.client.entry.add.menu.NewEntryMenu;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class EntryAddView extends AbstractLayout implements IEntryAddView {

    private Label contentHeader;
    private NewEntryMenu menu;
    private NewEntryForm<EntryInfo> currentForm;
    private VerticalPanel subContent;

    public EntryAddView() {
        super();
    }

    @Override
    protected Widget createContents() {
        FlexTable contentTable = new FlexTable();
        contentTable.setWidth("100%");
        contentTable.setWidget(0, 0, createMenu());
        contentTable.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);
        // TODO : middle sliver goes here
        contentTable.setWidget(0, 1, createMainContent());
        contentTable.getCellFormatter().setWidth(0, 1, "100%");
        contentTable.getFlexCellFormatter().setVerticalAlignment(0, 1, HasAlignment.ALIGN_TOP);
        return contentTable;
    }

    protected Widget createMenu() {
        FlexTable layout = new FlexTable();
        layout.setCellPadding(3);
        layout.setCellSpacing(0);
        layout.addStyleName("collection_menu_table");
        layout.setHTML(0, 0, "Select A Type");
        layout.getCellFormatter().setStyleName(0, 0, "collections_menu_header");

        // cell to render value
        menu = new NewEntryMenu();
        layout.setWidget(1, 0, menu);
        return layout;
    }

    protected Widget createMainContent() {
        subContent = new VerticalPanel();
        subContent.setWidth("100%");
        contentHeader = new Label("Add New Entry");

        FlexTable mainContent = new FlexTable(); // wrapper
        mainContent.setCellPadding(3);
        mainContent.setWidth("100%");
        mainContent.setCellSpacing(0);
        mainContent.addStyleName("add_new_entry_main_content_wrapper");
        mainContent.setWidget(0, 0, contentHeader);
        mainContent.getCellFormatter().setStyleName(0, 0, "add_new_entry_main_content_header");

        // sub content

        subContent
                .add(new HTML(
                        "<p>Please select the type of entry you wish to add. <p>Fields indicated by <span class=\"required\">*</span> are required. Other instructions here. Lorem ipsum."));
        mainContent.setWidget(1, 0, subContent);
        return mainContent;
    }

    @Override
    public void setCurrentForm(NewEntryForm<EntryInfo> form, String title) {
        this.currentForm = form;
        subContent.clear();
        subContent.add(this.currentForm);
        contentHeader.setText(title);
    }

    @Override
    public NewEntryForm<EntryInfo> getCurrentForm() {
        return this.currentForm;
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public NewEntryMenu getMenu() {
        return menu;
    }
}
