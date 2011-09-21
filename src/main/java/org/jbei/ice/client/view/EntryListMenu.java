package org.jbei.ice.client.view;

import org.jbei.ice.shared.EntryMenu;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellList;

public class EntryListMenu extends CellList<EntryMenu> {

    protected interface MenuResources extends Resources {
        /**
         * The styles used in this widget.
         */
        @Override
        @Source("org/jbei/ice/client/resource/css/ListMenu.css")
        Style cellListStyle();
    }

    private static MenuResources resources = GWT.create(MenuResources.class);

    public EntryListMenu(EntryListMenuCell cell) {
        super(cell, resources);
    }
}
