package org.jbei.ice.client.view;

import org.jbei.ice.client.EntryMenu;
import org.jbei.ice.client.component.EntryTable;
import org.jbei.ice.client.component.EntryTablePager;
import org.jbei.ice.client.component.ExportAsPanel;
import org.jbei.ice.client.panel.Footer;
import org.jbei.ice.client.panel.Header;
import org.jbei.ice.client.panel.HeaderMenu;
import org.jbei.ice.client.presenter.CollectionsPresenter;
import org.jbei.ice.shared.EntryDataView;
import org.jbei.ice.shared.Folder;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;

public class CollectionsView extends Composite implements CollectionsPresenter.Display {

    private EntryTable table;
    private CollectionListMenu listMenu;
    private EntryListMenu entryMenu;

    public CollectionsView() {

        FlexTable layout = new FlexTable();
        layout.setWidth("100%");
        layout.setHeight("98%");
        layout.setCellSpacing(0);
        layout.setCellPadding(0);
        initWidget(layout);

        // header
        layout.setWidget(0, 0, new Header());
        layout.setWidget(1, 0, new HeaderMenu());

        // entries menu
        FlexTable contents = new FlexTable();
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
        contents.setWidget(0, 1, getTablePanel());
        contents.getFlexCellFormatter().setRowSpan(0, 1, 2);
        contents.getCellFormatter().setVerticalAlignment(0, 1, HasAlignment.ALIGN_TOP);
        contents.getCellFormatter().setWidth(0, 1, "100%");
        contents.getCellFormatter().addStyleName(0, 1, "pad_top");

        // add contents
        layout.setWidget(2, 0, contents);
        layout.getCellFormatter().setHeight(2, 0, "100%");
        layout.getCellFormatter().setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_TOP);

        // footer
        layout.setWidget(3, 0, Footer.getInstance());
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    /**
     * @return table of results panel
     */
    private VerticalPanel getTablePanel() {
        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        table = new EntryTable();
        table.addStyleName("gray_border");
        panel.add(table);
        EntryTablePager tablePager = new EntryTablePager();
        tablePager.setDisplay(table);
        panel.add(tablePager);

        // Export as
        ExportAsPanel export = new ExportAsPanel(table);
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
    public HasData<EntryDataView> getDataView() {
        return this.table;
    }

    @Override
    public HasData<Folder> getCollectionMenu() {
        return listMenu;
    }

    @Override
    public HasData<EntryMenu> getEntryMenu() {
        return entryMenu;
    }
}
