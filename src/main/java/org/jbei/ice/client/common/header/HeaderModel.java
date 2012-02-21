package org.jbei.ice.client.common.header;

import java.util.ArrayList;

import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.event.SearchEvent;
import org.jbei.ice.shared.dto.SearchFilterInfo;

import com.google.gwt.event.shared.HandlerManager;

public class HeaderModel {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;

    public HeaderModel(RegistryServiceAsync service, HandlerManager eventBus) {
        this.service = service;
        this.eventBus = eventBus;
    }

    public void submitSearch(final ArrayList<SearchFilterInfo> filters) {
        if (filters == null)
            return;

        SearchEvent searchInProgressEvent = new SearchEvent();
        searchInProgressEvent.setFilters(filters);
        eventBus.fireEvent(searchInProgressEvent);
    }
}
