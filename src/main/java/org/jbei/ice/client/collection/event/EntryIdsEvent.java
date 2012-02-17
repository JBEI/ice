package org.jbei.ice.client.collection.event;

import java.util.ArrayList;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event for availability of entry ids from user action
 * 
 * @author Hector Plahar
 * 
 */
public class EntryIdsEvent extends GwtEvent<EntryIdsEventHandler> {

    public static Type<EntryIdsEventHandler> TYPE = new Type<EntryIdsEventHandler>();
    private final ArrayList<Long> ids;

    public EntryIdsEvent(ArrayList<Long> ids) {
        this.ids = new ArrayList<Long>(ids);
    }

    @Override
    public GwtEvent.Type<EntryIdsEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(EntryIdsEventHandler handler) {
        handler.onEntryIdsEvent(this);
    }

    public ArrayList<Long> getIds() {
        return this.ids;
    }
}
