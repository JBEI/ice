package org.jbei.ice.client.search.advanced;

import java.util.ArrayList;
import java.util.Set;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.common.table.EntryTablePager;
import org.jbei.ice.client.event.EntryViewEvent;
import org.jbei.ice.client.event.EntryViewEvent.EntryViewEventHandler;
import org.jbei.ice.client.search.blast.BlastResultsTable;
import org.jbei.ice.client.search.blast.BlastSearchDataProvider;
import org.jbei.ice.client.search.event.AdvancedSearchEvent;
import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.dto.SearchFilterInfo;
import org.jbei.ice.shared.dto.SearchResultInfo;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

/**
 * Presenter for searches
 *
 * @author Hector Plahar
 */
public class SearchPresenter extends AbstractPresenter {

    private enum Mode {
        BLAST, SEARCH;
    }

    private final ISearchView display;
    private final AdvancedSearchDataProvider dataProvider;
    private final BlastSearchDataProvider blastProvider;
    private final AdvancedSearchModel model;
    private AdvancedSearchResultsTable table;
    private BlastResultsTable blastTable;
    private Mode mode;

    public SearchPresenter(RegistryServiceAsync rpcService, HandlerManager eventBus, ISearchView view) {
        super(rpcService, eventBus);
        this.display = view;
        table = new AdvancedSearchResultsTable(new EntryTablePager()) {

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
        model = new AdvancedSearchModel(rpcService, eventBus);
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.display.asWidget());
    }

    public void addTableSelectionModelChangeHandler(Handler handler) {
        this.table.getSelectionModel().addSelectionChangeHandler(handler);
    }

    public Set<SearchResultInfo> getResultSelectedSet() {
        return this.table.getSelectionModel().getSelectedSet();
    }

    public void search() {
        // currently support only a single blast search with filters
        // search for blast operator

        ArrayList<SearchFilterInfo> searchFilters = display.parseUrlForFilters();
        ArrayList<SearchFilterInfo> searchFilterCopy = new ArrayList<SearchFilterInfo>(searchFilters);
        SearchFilterInfo blastInfo = null;
        for (SearchFilterInfo filter : searchFilterCopy) {
            QueryOperator operator = QueryOperator.operatorValueOf(filter.getOperator());
            if (operator == null)
                continue;

            if (operator == QueryOperator.TBLAST_X || operator == QueryOperator.BLAST_N) {
                if (searchFilterCopy.remove(filter)) {
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
            this.model.performBlast(searchFilterCopy, blastInfo.getOperand(), program, 0, 30,
                                    new EventHandler(searchFilters));
        } else {
            // regular search
            dataProvider.updateRowCount(0, false);
            display.setSearchVisibility(table, true);
            table.setVisibleRangeAndClearData(table.getVisibleRange(), false);
            this.model.retrieveSearchResults(searchFilterCopy, display.getSearchTypes(),
                                             0, 30, new EventHandler(searchFilters));
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

    public ISearchView getView() {
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
            dataProvider.setSearchData(event.getSearchResults());
            mode = Mode.SEARCH;
        }

        @Override
        public void onBlastCompletion(AdvancedSearchEvent event) {
            if (event == null)
                return;
            blastProvider.setBlastData(event.getResults());
            mode = Mode.BLAST;
        }
    }
}
