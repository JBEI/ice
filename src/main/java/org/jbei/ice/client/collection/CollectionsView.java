package org.jbei.ice.client.collection;

import org.jbei.ice.client.common.Footer;
import org.jbei.ice.client.common.HeaderView;
import org.jbei.ice.client.common.HeaderMenu;
import org.jbei.ice.client.component.ExportAsPanel;
import org.jbei.ice.client.component.table.DataTable;
import org.jbei.ice.client.component.table.EntryTablePager;
import org.jbei.ice.client.view.EntryListMenu;
import org.jbei.ice.client.view.EntryListMenuCell;
import org.jbei.ice.shared.EntryMenu;
import org.jbei.ice.shared.FolderDetails;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;

public class CollectionsView extends Composite implements CollectionsPresenter.Display {

    private DataTable<?> table;
    private CollectionListMenu listMenu;
    private EntryListMenu entryMenu;
    private FlexTable contents;

    public CollectionsView() {

        HeaderPanel layout = new HeaderPanel();
        layout.setWidth("100%");
        layout.setHeight("98%");
        initWidget(layout);

        layout.setHeaderWidget(getHeader());
        layout.setContentWidget(getContents());
        layout.setFooterWidget(getFooter());
    }

    private Widget getHeader() {

        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        panel.add(new HeaderView());
        panel.add(new HeaderMenu());
        return panel;
    }

    private Widget getContents() {

        contents = new FlexTable();
        contents.setWidth("100%");
        contents.setCellSpacing(0);
        contents.setCellPadding(0);
        Widget entriesView = entriesView();
        contents.setWidget(0, 0, entriesView);

        contents.getCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);
        contents.getCellFormatter().addStyleName(0, 0, "pad_right");
        contents.getCellFormatter().addStyleName(0, 0, "pad_top");

        // collections menu
        contents.setWidget(1, 0, collectionsView());
        contents.getCellFormatter().addStyleName(1, 0, "pad_top");

        // data table
        //        contents.setWidget(0, 1, getTablePanel());
        contents.setWidget(0, 1, new HTML("Fetching Data")); // TODO : this is initially displayed and then replaced in the presenter constructor

        contents.getFlexCellFormatter().setRowSpan(0, 1, 2);
        contents.getCellFormatter().setVerticalAlignment(0, 1, HasAlignment.ALIGN_TOP);
        contents.getCellFormatter().setWidth(0, 1, "100%");
        contents.getCellFormatter().addStyleName(0, 1, "pad_top");
        return contents;
    }

    private Widget getFooter() {
        return Footer.getInstance();
    }

    /**
     * @return table of results panel
     */
    private VerticalPanel getTablePanel() {

        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        //        table = new CollectionsDataTable();
        table.addStyleName("gray_border");
        panel.add(table);
        EntryTablePager tablePager = new EntryTablePager();
        tablePager.setDisplay(table);
        panel.add(tablePager);

        // Export as
        ExportAsPanel export = new ExportAsPanel();
        panel.add(export);
        return panel;
    }

    private Widget collectionsView() {

        FlexTable layout = new FlexTable();
        layout.addStyleName("gray_border");
        layout.setWidth("180px");
        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.setHTML(0, 0, "Collections");
        layout.getCellFormatter().setStyleName(0, 0, "collections_header");

        // cell to render value
        CollectionListMenuCell cell = new CollectionListMenuCell();
        listMenu = new CollectionListMenu(cell);
        layout.setWidget(1, 0, listMenu);
        return layout;
    }

    private Widget entriesView() {

        FlexTable layout = new FlexTable();
        layout.addStyleName("gray_border");
        layout.setWidth("180px");
        layout.setCellPadding(5);
        layout.setCellSpacing(0);
        layout.setHTML(0, 0, "Entries");
        layout.getCellFormatter().setStyleName(0, 0, "collections_header");

        // menu item
        entryMenu = new EntryListMenu(new EntryListMenuCell());
        layout.setWidget(1, 0, entryMenu);

        return layout;
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void setDataView(DataTable<?> table) {
        this.table = table;
        Widget widget = getTablePanel();
        contents.setWidget(0, 1, widget);
    }

    @Override
    public HasData<FolderDetails> getCollectionMenu() {
        return listMenu;
    }

    @Override
    public HasData<EntryMenu> getEntryMenu() {
        return entryMenu;
    }
}
