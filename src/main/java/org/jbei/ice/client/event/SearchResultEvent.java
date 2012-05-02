package org.jbei.ice.client.event;

import org.jbei.ice.client.event.SearchResultEvent.SearchResultEventHandler;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class SearchResultEvent extends GwtEvent<SearchResultEventHandler> {

    public static Type<SearchResultEventHandler> TYPE = new Type<SearchResultEventHandler>();

    @Override
    public Type<SearchResultEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SearchResultEventHandler handler) {
        handler.onResultsAvailable(this);
    }

    public interface SearchResultEventHandler extends EventHandler {
        void onResultsAvailable(SearchResultEvent event);
    }
}
