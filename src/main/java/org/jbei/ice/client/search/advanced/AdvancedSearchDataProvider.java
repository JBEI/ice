package org.jbei.ice.client.search.advanced;

import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.common.EntryDataViewDataProvider;

public class AdvancedSearchDataProvider extends EntryDataViewDataProvider {
    public AdvancedSearchDataProvider(AdvancedSearchResultsTable view, RegistryServiceAsync service) {
        super(view, service);
    }
}
