package org.jbei.ice.client.collection.menu;

import org.jbei.ice.client.collection.menu.CollectionUserMenu.MenuCell;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Click handler for menu cell selection
 * 
 * @author hplahar
 * 
 */
class CellSelectionHandler implements ClickHandler {

    private SingleSelectionModel<MenuItem> model;
    private MenuCell cell;

    public CellSelectionHandler(SingleSelectionModel<MenuItem> model, MenuCell cell) {
        this.model = model;
        this.cell = cell;
    }

    @Override
    public void onClick(ClickEvent event) {
        this.model.setSelected(this.cell.getMenuItem(), true);
    }
}
