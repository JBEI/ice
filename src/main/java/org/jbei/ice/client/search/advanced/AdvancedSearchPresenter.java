package org.jbei.ice.client.search.advanced;

import java.util.ArrayList;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.common.EntryDataViewDataProvider;
import org.jbei.ice.client.common.FilterOperand;
import org.jbei.ice.client.event.SearchEvent;
import org.jbei.ice.client.event.SearchEventHandler;
import org.jbei.ice.client.util.Utils;
import org.jbei.ice.shared.FolderDetails;
import org.jbei.ice.shared.dto.SearchFilterInfo;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.ListDataProvider;

public class AdvancedSearchPresenter {

    private final RegistryServiceAsync rpcService;
    private final HandlerManager eventBus;
    private final AdvancedSearchView display;
    private final EntryDataViewDataProvider dataProvider;
    private final ListDataProvider<FolderDetails> userCollectionDataProvider;

    public AdvancedSearchPresenter(final RegistryServiceAsync rpcService,
            final HandlerManager eventBus) {

        this.rpcService = rpcService;
        this.eventBus = eventBus;
        this.display = new AdvancedSearchView();
        userCollectionDataProvider = new ListDataProvider<FolderDetails>();

        retrieveUserCollections();

        // hide the results table
        this.display.setResultsVisibility(false);
        dataProvider = new AdvancedSearchDataProvider(display.getResultsTable(), rpcService);

        // register for search events
        eventBus.addHandler(SearchEvent.TYPE, new SearchEventHandler() {

            @Override
            public void onSearch(SearchEvent event) {
                search(event.getOperands());
            }
        });
    }

    public AdvancedSearchPresenter(final RegistryServiceAsync rpcService,
            final HandlerManager eventBus, ArrayList<FilterOperand> operands) {
        this(rpcService, eventBus);
        search(operands);
    }

    protected void search(ArrayList<FilterOperand> operands) {
        if (operands == null)
            return;

        // TODO : model
        ArrayList<SearchFilterInfo> searchFilters = new ArrayList<SearchFilterInfo>();
        for (FilterOperand operand : operands) {
            SearchFilterInfo info = new SearchFilterInfo(operand.getType().name(), operand
                    .getSelectedOperator().name(), operand.getOperand());
            searchFilters.add(info);
        }

        rpcService.retrieveSearchResults(searchFilters, new AsyncCallback<ArrayList<Long>>() {

            @Override
            public void onSuccess(ArrayList<Long> result) {
                display.setResultsVisibility(true);
                dataProvider.setValues(result);
                reset();
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Call failed: " + caught.getMessage());

                // TODO: Hide the table and show a red error msg in the position where it states
                // "no records found"
                display.setResultsVisibility(false);
                reset();
            }

            public void reset() {
                Utils.showDefaultCursor(null);
            }
        });
    }

    public AdvancedSearchView getView() {
        return this.display;
    }

    public void retrieveUserCollections() {
        this.rpcService.retrieveUserCollections(AppController.sessionId,
            AppController.accountInfo.getEmail(), new AsyncCallback<ArrayList<FolderDetails>>() {

                @Override
                public void onFailure(Throwable caught) {
                    Window.alert("Call for user collection failed: " + caught.getMessage());
                }

                @Override
                public void onSuccess(ArrayList<FolderDetails> result) {
                    userCollectionDataProvider.setList(result);
                }
            });
    }

}
