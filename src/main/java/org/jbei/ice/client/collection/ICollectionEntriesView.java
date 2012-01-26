package org.jbei.ice.client.collection;

import org.jbei.ice.client.collection.menu.CollectionEntryMenu;
import org.jbei.ice.client.collection.menu.CollectionUserMenu;
import org.jbei.ice.client.collection.table.CollectionEntriesDataTable;

import com.google.gwt.user.client.ui.Widget;

/**
 * Interface for view that displays details of entry collections
 * 
 * @author Hector Plahar
 */

public interface ICollectionEntriesView {

    CollectionEntryMenu getSystemCollectionMenu();

    CollectionUserMenu getUserCollectionMenu();

    Widget asWidget();

    void setCollectionSubMenu(Widget widget);

    /**
     * active data view
     * 
     * @param table
     *            data view table. depends on user selection
     */
    void setDataView(CollectionEntriesDataTable table);

    // TODO : the following needs to be in abstract layout
    void setFeedback(Widget feedback);
}
