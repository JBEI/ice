package org.jbei.ice.client.collection;

import org.jbei.ice.client.collection.table.CollectionListTable;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;

/**
 * Interface for view of a list of collections
 * 
 * @author hplahar
 * 
 */
public interface ICollectionListView {

    Button getAddCollectionButton();

    void showAddCollectionWidget(Widget widget);

    void hideAddCollectionWidget();

    CollectionListTable getDataTable();

    CollectionListTable getUserDataTable();

    Widget asWidget();
}
