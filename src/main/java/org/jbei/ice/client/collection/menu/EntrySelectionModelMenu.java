package org.jbei.ice.client.collection.menu;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Sub menu widget that enables user to move entries and export in
 * specified formats, based on user selection
 * 
 * @author Hector Plahar
 */
public class EntrySelectionModelMenu extends Composite {

    private final HTMLPanel menuHolder;
    private final ExportAsMenu exportAs;
    private final CollectionSubMenu collectionsMenu;

    public EntrySelectionModelMenu(UserCollectionMultiSelect add, UserCollectionMultiSelect move) {

        String html = "<span style=\"float: left\" class=\"buttonGroup\" id=\"collection_sub_menu\"></span>"
                + "<span style=\"margin-left: 15px\" class=\"buttonGroup\" id=\"export_as_menu\"></span>";
        menuHolder = new HTMLPanel(html);
        initWidget(menuHolder);

        collectionsMenu = new CollectionSubMenu(add, move);
        menuHolder.add(collectionsMenu.asWidget(), "collection_sub_menu");

        // create ExportMenu
        exportAs = new ExportAsMenu();
        menuHolder.add(exportAs.asWidget(), "export_as_menu");
    }

    public ExportAsMenu getExportAsMenu() {
        return this.exportAs;
    }

    public CollectionSubMenu getCollectionMenu() {
        return this.collectionsMenu;
    }
}
