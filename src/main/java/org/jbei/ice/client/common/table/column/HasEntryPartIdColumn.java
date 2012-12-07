package org.jbei.ice.client.common.table.column;

import org.jbei.ice.client.collection.menu.IHasEntryHandlers;
import org.jbei.ice.client.common.table.cell.HasEntryPartIDCell;
import org.jbei.ice.client.event.EntryViewEvent;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.entry.HasEntryInfo;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Part id column for tables that display data that {@link HasEntryInfo}
 *
 * @author Hector Plahar
 */
public class HasEntryPartIdColumn<T extends HasEntryInfo>
        extends DataTableColumn<T, HasEntryInfo> implements IHasEntryHandlers {

    private HandlerManager handlerManager;

    public HasEntryPartIdColumn(HasEntryPartIDCell<HasEntryInfo> cell) {
        super(cell, ColumnField.PART_ID);
    }

    @Override
    public HasEntryInfo getValue(HasEntryInfo object) {
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
