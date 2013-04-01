package org.jbei.ice.client.event;

import org.jbei.ice.client.collection.presenter.EntryContext;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Used to show a list of entries in a specific context. E.g. when viewing individual entries in a collection,
 * this event is fired to show the full list
 * <p/>
 * // TODO : show current position in the list
 *
 * @author Hector Plahar
 */
public class ShowEntryListEvent extends GwtEvent<ShowEntryListEventHandler> {

    public static Type<ShowEntryListEventHandler> TYPE = new Type<ShowEntryListEventHandler>();
    private final EntryContext context;

    public ShowEntryListEvent(EntryContext context) {
        this.context = context;
    }

    @Override
    public Type<ShowEntryListEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ShowEntryListEventHandler handler) {
        handler.onEntryListContextAvailable(this);
    }

    public EntryContext getContext() {
        return this.context;
    }
}
