package org.jbei.ice.client.event;

import java.util.List;

import org.jbei.ice.client.collection.presenter.EntryContext;
import org.jbei.ice.client.event.EntryViewEvent.EntryViewEventHandler;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class EntryViewEvent extends GwtEvent<EntryViewEventHandler> {

    public interface EntryViewEventHandler extends EventHandler {
        void onEntryView(EntryViewEvent event);
    }

    public static Type<EntryViewEventHandler> TYPE = new Type<EntryViewEventHandler>();
    private EntryContext context;

    public EntryViewEvent(long id) {
        this.context = new EntryContext();
        this.context.setCurrent(id);
    }

    //    public void setHasEntry(IHasEntryId hasEntry) {
    //        this.context.setHasEntry(hasEntry);
    //    }

    public void setList(List<Long> ids) {
        this.context.setList(ids);
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

}
