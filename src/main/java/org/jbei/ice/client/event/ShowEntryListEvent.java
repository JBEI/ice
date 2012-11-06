package org.jbei.ice.client.event;

import org.jbei.ice.client.collection.presenter.EntryContext;

import com.google.gwt.event.shared.GwtEvent;

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
