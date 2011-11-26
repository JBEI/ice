package org.jbei.ice.client.collection.menu;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;

/**
 * Sub menu widget that enables user to move entries and export in
 * specified formats, based on user selection
 * 
 * @author Hector Plahar
 */
public class EntrySelectionModelMenu extends Composite {

    private final HorizontalPanel menuHolder;

    private final ExportAsMenu exportAs;
    private final CollectionSubMenu collectionsMenu;

    public EntrySelectionModelMenu(UserCollectionMultiSelect add, UserCollectionMultiSelect move) {

        menuHolder = new HorizontalPanel();
        initWidget(menuHolder);

        collectionsMenu = new CollectionSubMenu(add, move);
        menuHolder.add(collectionsMenu);

        // create ExportMenu
        exportAs = new ExportAsMenu();
        menuHolder.add(exportAs);
    }

    public ExportAsMenu getExportAsMenu() {
        return this.exportAs;
    }

    public CollectionSubMenu getCollectionMenu() {
        return this.collectionsMenu;
    }
}
