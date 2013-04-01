package org.jbei.ice.client.event;

import org.jbei.ice.client.collection.presenter.EntryContext;
import org.jbei.ice.client.common.IHasNavigableData;
import org.jbei.ice.client.event.EntryViewEvent.EntryViewEventHandler;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Event used to signal entry detail view of a specific entry id is desired
 *
 * @author Hector Plahar
 */
public class EntryViewEvent extends GwtEvent<EntryViewEventHandler> {

    public interface EntryViewEventHandler extends EventHandler {
        void onEntryView(EntryViewEvent event);
    }

    public static Type<EntryViewEventHandler> TYPE = new Type<EntryViewEventHandler>();
    private EntryContext context;

    public EntryViewEvent(long id, String recordId, EntryContext.Type mode) {
        this.context = new EntryContext(mode);
        this.context.setRecordId(recordId);
        this.context.setId(id);
    }

    public EntryContext getContext() {
        return this.context;
    }

    /**
     * Handler hook.
     *
     * @return the handler hook
     */
    public static Type<EntryViewEventHandler> getType() {
        return TYPE;
    }

    @Override
    public Type<EntryViewEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(EntryViewEventHandler handler) {
        handler.onEntryView(this);
    }

    public void setNavigable(IHasNavigableData nav) {
        context.setNav(nav);
    }
}
