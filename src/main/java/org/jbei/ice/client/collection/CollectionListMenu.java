package org.jbei.ice.client.collection;

import org.jbei.ice.shared.FolderDetails;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellList;

public class CollectionListMenu extends CellList<FolderDetails> {

    protected interface MenuResources extends Resources {
        /**
         * The styles used in this widget.
         */
        @Override
        @Source("org/jbei/ice/client/resource/css/ListMenu.css")
        Style cellListStyle();
    }

    private static MenuResources resources = GWT.create(MenuResources.class);

    public CollectionListMenu(CollectionListMenuCell cell) {
        super(cell, resources);
    }
}
