package org.jbei.ice.client.search.advanced;

import java.util.ArrayList;
import java.util.Set;

import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.common.EntryDataViewDataProvider;
import org.jbei.ice.client.event.EntryViewEvent;
import org.jbei.ice.client.event.EntryViewEvent.EntryViewEventHandler;
import org.jbei.ice.client.search.blast.BlastSearchDataProvider;
import org.jbei.ice.client.search.event.AdvancedSearchEvent;
import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.dto.BlastResultInfo;
import org.jbei.ice.shared.dto.SearchFilterInfo;

import com.google.gwt.event.shared.HandlerManager;

/**
 * Presenter for searches
 * 
 * @author Hector Plahar
 * 
 */
public class AdvancedSearchPresenter {

    private enum Mode {
        BLAST, SEARCH;
    }

    private final AdvancedSearchView display;
    private final EntryDataViewDataProvider dataProvider;
    private final BlastSearchDataProvider blastProvider;
    private final AdvancedSearchModel model;
    private AdvancedSearchResultsTable table;
    private Mode mode;

    public AdvancedSearchPresenter(RegistryServiceAsync rpcService, HandlerManager eventBus) {
        this.display = new AdvancedSearchView();

        table = new AdvancedSearchResultsTable() {

            @Override
            protected EntryViewEventHandler getHandler() {
                return new EntryViewEventHandler() {
                    @Override
                    public void onEntryView(EntryViewEvent event) {
                        event.setNavigable(dataProvider);
                        model.getEventBus().fireEvent(event);
                    }
                };
            }
        };

        // hide the results table
        dataProvider = new AdvancedSearchDataProvider(table, rpcService);
        blastProvider = new BlastSearchDataProvider(display.getBlastResultTable(),
                new ArrayList<BlastResultInfo>(), rpcService);

        this.model = new AdvancedSearchModel(rpcService, eventBus);
    }

    public void search(final ArrayList<SearchFilterInfo> searchFilters) {
        if (searchFilters == null)
            return;

        // currently support only a single blast search with filters
        // search for blast operator
        ArrayList<SearchFilterInfo> filterCopy = new ArrayList<SearchFilterInfo>(searchFilters);
        SearchFilterInfo blastInfo = null;
        for (SearchFilterInfo filter : filterCopy) {
            QueryOperator operator = QueryOperator.operatorValueOf(filter.getOperator());
            if (operator == null)
                continue;

            if (operator == QueryOperator.TBLAST_X || operator == QueryOperator.BLAST_N) {
                if (filterCopy.remove(filter)) {
                    blastInfo = filter;
                }
                break;
            }
        }

        if (blastInfo != null) {

            // show blast table loading
            blastProvider.updateRowCount(0, false);
            display.setBlastVisibility(true);
            display.getBlastResultTable().setVisibleRangeAndClearData(
                display.getBlastResultTable().getVisibleRange(), false);

            // get blast results and filter 
            QueryOperator program = QueryOperator.operatorValueOf(blastInfo.getOperator());
            this.model.performBlast(filterCopy, blastInfo.getOperand(), program, new Handler(
                    searchFilters));
        } else {
            display.setSearchVisibility(table, true);
            table.setVisibleRangeAndClearData(table.getVisibleRange(), false);

            this.model.retrieveSearchResults(filterCopy, new Handler(searchFilters));
        }
    }

    public Set<Long> getEntrySet() {
        switch (mode) {
        case SEARCH:
        default:
            return table.getSelectedEntrySet();

        case BLAST:
            return display.getBlastResultTable().getSelectedEntrySet();
        }

    }

    public AdvancedSearchView getView() {
        return this.display;
    }

    // 
    // inner class
    //

    private class Handler implements AdvancedSearchEvent.AdvancedSearchEventHandler {

        public Handler(ArrayList<SearchFilterInfo> filters) {
            display.setSearchFilters(filters);
        }

        @Override
        public void onSearchCompletion(AdvancedSearchEvent event) {
            if (event == null)
                return;
            dataProvider.setValues(event.getSearchResults());
            mode = Mode.SEARCH;
        }

        @Override
        public void onBlastCompletion(AdvancedSearchEvent event) {
            if (event == null)
                return;
            blastProvider.setData(event.getResults());
            mode = Mode.BLAST;
        }
    }
}
