package org.jbei.ice.client.bulkimport.widget;

import org.jbei.ice.client.bulkimport.BulkImportMenuItem;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

class CellSelectionHandler implements ClickHandler {

    private SingleSelectionModel<BulkImportMenuItem> model;
    private SavedDraftsMenu.MenuCell cell;

    public CellSelectionHandler(SingleSelectionModel<BulkImportMenuItem> model, SavedDraftsMenu.MenuCell cell) {
        this.model = model;
        this.cell = cell;
    }

    @Override
    public void onClick(ClickEvent event) {
        // TODO : this is a workaround a bug. Should be handled properly
        boolean fireEvent = this.model.isSelected(this.cell.getMenuItem());
        this.model.setSelected(this.cell.getMenuItem(), true);
        if (fireEvent)
            SelectionChangeEvent.fire(this.model);
    }
}
