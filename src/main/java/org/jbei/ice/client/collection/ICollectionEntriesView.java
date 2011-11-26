package org.jbei.ice.client.collection;

import org.jbei.ice.client.common.table.DataTable;
import org.jbei.ice.shared.FolderDetails;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;

public interface ICollectionEntriesView {

    HasData<FolderDetails> getSystemCollectionMenu();

    HasData<FolderDetails> getUserCollectionMenu();

    Widget asWidget();

    void setCollectionSubMenu(Widget widget);

    TextBox getQuickAddBox();

    Button getQuickAddButton();

    // active data view
    void setDataView(DataTable<?> table);
}
