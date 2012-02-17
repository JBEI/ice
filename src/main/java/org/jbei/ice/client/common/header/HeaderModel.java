package org.jbei.ice.client.common.header;

import java.util.ArrayList;

import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.common.FilterOperand;
import org.jbei.ice.client.event.SearchEvent;
import org.jbei.ice.shared.dto.SearchFilterInfo;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class HeaderModel {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;

    public HeaderModel(RegistryServiceAsync service, HandlerManager eventBus) {
        this.service = service;
        this.eventBus = eventBus;
    }

    public void submitSearch(final ArrayList<FilterOperand> filters) {
        if (filters == null || filters.isEmpty())
            return;

        ArrayList<SearchFilterInfo> searchFilters = new ArrayList<SearchFilterInfo>();
        for (FilterOperand operand : filters) {
            SearchFilterInfo info = new SearchFilterInfo(operand.getType().name(), operand
                    .getSelectedOperator().name(), operand.getOperand());
            searchFilters.add(info);
        }

        service.retrieveSearchResults(searchFilters, new AsyncCallback<ArrayList<Long>>() {

            @Override
            public void onSuccess(ArrayList<Long> result) {
                SearchEvent event = new SearchEvent();
                event.setOperands(filters);
                event.setResults(result);
                eventBus.fireEvent(event);
            }

            @Override
            public void onFailure(Throwable caught) {
                // TODO Auto-generated method stub
            }
        });
    }
}
