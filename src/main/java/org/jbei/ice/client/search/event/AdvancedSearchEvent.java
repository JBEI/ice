package org.jbei.ice.client.search.event;

import java.util.ArrayList;

import org.jbei.ice.client.search.event.AdvancedSearchEvent.AdvancedSearchEventHandler;
import org.jbei.ice.shared.dto.BlastResultInfo;
import org.jbei.ice.shared.dto.SearchResults;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class AdvancedSearchEvent extends GwtEvent<AdvancedSearchEventHandler> {

    public static Type<AdvancedSearchEventHandler> TYPE = new Type<AdvancedSearchEventHandler>();
    private ArrayList<BlastResultInfo> results;
    private SearchResults searchResults;
    private final boolean isBlast;

    public AdvancedSearchEvent(ArrayList<BlastResultInfo> results) {
        this.results = new ArrayList<BlastResultInfo>(results);
        isBlast = true;
    }

    public AdvancedSearchEvent(SearchResults searchResults) {
        this.searchResults = searchResults;
        isBlast = false;
    }

    public ArrayList<BlastResultInfo> getResults() {
        return this.results;
    }

    public SearchResults getSearchResults() {
        return this.searchResults;
    }

    @Override
    public Type<AdvancedSearchEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(AdvancedSearchEventHandler handler) {
        if (isBlast)
            handler.onBlastCompletion(this);
        else
            handler.onSearchCompletion(this);
    }

    public interface AdvancedSearchEventHandler extends EventHandler {
        void onBlastCompletion(AdvancedSearchEvent event);

        void onSearchCompletion(AdvancedSearchEvent event);
    }
}
