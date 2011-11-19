package org.jbei.ice.client.collection.view;

import org.jbei.ice.client.collection.menu.CollectionListMenu;
import org.jbei.ice.client.collection.presenter.CollectionsEntriesPresenter;
import org.jbei.ice.client.common.AbstractLayout;
import org.jbei.ice.client.common.table.DataTable;
import org.jbei.ice.client.common.table.EntryTablePager;
import org.jbei.ice.shared.FolderDetails;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;

public class CollectionsEntriesView extends AbstractLayout implements
        CollectionsEntriesPresenter.Display {

    private DataTable<?> table;
    private CollectionListMenu systemMenu;
    private CollectionListMenu userMenu;
    private FlexTable contents;
    private VerticalPanel tablePanel;
    private Widget selectionWidget;
    private TextBox quickAddBox;
    private Button addCollectionButton;

    public CollectionsEntriesView() {
    }

    protected void initComponents() {
        tablePanel = new VerticalPanel();
        tablePanel.setWidth("100%");

        quickAddBox = new TextBox();
        quickAddBox.setText("Enter new collection name...");
        quickAddBox.setWidth("100%");
    }

    @Override
    protected Widget createContents() {

        contents = new FlexTable();
        contents.setWidth("100%");
        contents.setCellSpacing(0);
        contents.setCellPadding(0);

        // systems collections menu
        contents.setWidget(0, 0, createSystemCollectionMenu());

        // user collection menu
        contents.setWidget(1, 0, createUserCollectionMenu());
        contents.getCellFormatter().setVerticalAlignment(1, 0, HasAlignment.ALIGN_TOP);

        // data table
        contents.setWidget(0, 1, new HTML("&nbsp;")); // place holder while content is set by the presenter

        contents.getFlexCellFormatter().setRowSpan(0, 1, 2);
        contents.getCellFormatter().setVerticalAlignment(0, 1, HasAlignment.ALIGN_TOP);
        contents.getCellFormatter().setWidth(0, 1, "100%");
        return contents;
    }

    /**
     * @return table of results panel
     */
    private VerticalPanel getTablePanel() {

        tablePanel.clear();

        if (selectionWidget != null)
            tablePanel.add(selectionWidget);

        tablePanel.add(table);
        EntryTablePager tablePager = new EntryTablePager();
        tablePager.setDisplay(table);
        tablePanel.add(tablePager);

        return tablePanel;
    }

    // left :: menu
    private Widget createSystemCollectionMenu() {
        FlexTable layout = new FlexTable();
        layout.setCellPadding(3);
        layout.setCellSpacing(0);
        layout.addStyleName("collection_menu_table");
        layout.setHTML(0, 0, "Collections");
        layout.getCellFormatter().setStyleName(0, 0, "collections_menu_header");

        // cell to render value
        systemMenu = new CollectionListMenu();
        layout.setWidget(1, 0, systemMenu);
        return layout;
    }

    // left :: menu
    private Widget createUserCollectionMenu() {
        FlexTable layout = new FlexTable();
        layout.setCellPadding(3);
        layout.setCellSpacing(0);
        layout.addStyleName("collection_menu_table");
        addCollectionButton = new Button("+");
        HorizontalPanel panel = new HorizontalPanel();
        panel.setWidth("100%");
        Label header = new Label("User Collections");
        panel.add(header);
        panel.add(addCollectionButton);
        panel.setCellHorizontalAlignment(addCollectionButton, HasAlignment.ALIGN_RIGHT);
        layout.setWidget(0, 0, panel);
        layout.getCellFormatter().setStyleName(0, 0, "collections_menu_header");

        // add to menu
        layout.setWidget(1, 0, quickAddBox);

        userMenu = new CollectionListMenu();
        layout.setWidget(2, 0, userMenu);
        return layout;
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void setDataView(DataTable<?> table) {
        this.table = table;
        contents.setWidget(0, 1, getTablePanel());
    }

    @Override
    public HasData<FolderDetails> getSystemCollectionMenu() {
        return this.systemMenu;
    }

    @Override
    public HasData<FolderDetails> getUserCollectionMenu() {
        return this.userMenu;
    }

    public Button getAddToCollectionButton() {
        return addCollectionButton;
    }

    @Override
    public void setSelectionMenu(Widget widget) {
        selectionWidget = widget;
    }

    @Override
    public TextBox getQuickAddBox() {
        return quickAddBox;
    }

    @Override
    public Button getQuickAddButton() {
        return addCollectionButton;
    }
}
