package org.jbei.ice.client.search.advanced;

import java.util.ArrayList;
import java.util.Set;

import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.event.EntryViewEvent;
import org.jbei.ice.client.event.EntryViewEvent.EntryViewEventHandler;
import org.jbei.ice.client.search.blast.BlastResultsTable;
import org.jbei.ice.client.search.blast.BlastSearchDataProvider;
import org.jbei.ice.client.search.event.AdvancedSearchEvent;
import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.SearchFilterInfo;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

/**
 * Presenter for searches
 *
 * @author Hector Plahar
 */
public class AdvancedSearchPresenter {

    private enum Mode {
        BLAST, SEARCH;
    }

    private final AdvancedSearchView display;
    private final AdvancedSearchDataProvider dataProvider;
    private final BlastSearchDataProvider blastProvider;
    private final AdvancedSearchModel model;
    private AdvancedSearchResultsTable table;
    private BlastResultsTable blastTable;
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

        blastTable = new BlastResultsTable() {
            @Override
            protected EntryViewEventHandler getHandler() {
                return new EntryViewEventHandler() {
                    @Override
                    public void onEntryView(EntryViewEvent event) {
                        event.setNavigable(blastProvider);
                        model.getEventBus().fireEvent(event);
                    }
                };
            }
        };

        // hide the results table
        dataProvider = new AdvancedSearchDataProvider(table, rpcService);
        blastProvider = new BlastSearchDataProvider(blastTable, rpcService);

        this.model = new AdvancedSearchModel(rpcService, eventBus);
    }

    public void addTableSelectionModelChangeHandler(Handler handler) {
//        final EntrySelectionModel<EntryInfo> selectionModel = this.table.getSelectionModel();
//        selectionModel.addSelectionChangeHandler(handler);
    }

    public Set<EntryInfo> getResultSelectedSet() {
//        return this.table.getSelectionModel().getSelectedSet();
        return null;
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
            display.setBlastVisibility(blastTable, true);
            blastTable.setVisibleRangeAndClearData(blastTable.getVisibleRange(), false);

            // get blast results and filter 
            QueryOperator program = QueryOperator.operatorValueOf(blastInfo.getOperator());
            this.model.performBlast(filterCopy, blastInfo.getOperand(), program, 0, 15, new EventHandler(
                    searchFilters));
        } else {
            dataProvider.updateRowCount(0, false);
            display.setSearchVisibility(table, true);
            table.setVisibleRangeAndClearData(table.getVisibleRange(), false);

            this.model.retrieveSearchResults(filterCopy, 0, 15, new EventHandler(searchFilters));
        }
    }

    public Set<Long> getEntrySet() {
        switch (mode) {
            case SEARCH:
            default:
//                if (table.getSelectionModel().isAllSelected()) {
//                    return dataProvider.getData();
//                }
                return table.getSelectedEntrySet();

            case BLAST:
                return blastTable.getSelectedEntrySet();
        }
    }

    public AdvancedSearchView getView() {
        return this.display;
    }

    // 
    // inner class
    //

    private class EventHandler implements AdvancedSearchEvent.AdvancedSearchEventHandler {

        public EventHandler(ArrayList<SearchFilterInfo> filters) {
            display.setSearchFilters(filters);
        }

        @Override
        public void onSearchCompletion(AdvancedSearchEvent event) {
            if (event == null)
                return;
            dataProvider.setData(event.getSearchResults());
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
