package org.jbei.ice.client.common.table.column;

import org.jbei.ice.client.collection.menu.IHasEntryHandlers;
import org.jbei.ice.client.common.table.cell.PartIDCell;
import org.jbei.ice.client.event.EntryViewEvent;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.entry.EntryInfo;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * @author Hector Plahar
 */
public class EntryPartIdColumn<T extends EntryInfo> extends DataTableColumn<T, EntryInfo> implements IHasEntryHandlers {

    private HandlerManager handlerManager;

    public EntryPartIdColumn(PartIDCell<EntryInfo> cell) {
        super(cell, ColumnField.PART_ID);
    }

    @Override
    public EntryInfo getValue(EntryInfo object) {
        return object;
    }

    @Override
    public HandlerRegistration addEntryHandler(EntryViewEvent.EntryViewEventHandler handler) {
        if (handlerManager == null)
            handlerManager = new HandlerManager(this);
        return handlerManager.addHandler(EntryViewEvent.getType(), handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        if (handlerManager != null)
            handlerManager.fireEvent(event);
    }
}

