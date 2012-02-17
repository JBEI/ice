package org.jbei.ice.client.search.advanced;

import java.util.ArrayList;

import org.jbei.ice.client.event.SearchSelectionHandler;
import org.jbei.ice.shared.dto.SearchFilterInfo;

import com.google.gwt.user.client.ui.Widget;

/**
 * Interface for advanced search view
 * 
 * @author Hector Plahar
 */
public interface IAdvancedSearchView {

    ArrayList<SearchFilterInfo> getSearchFilters();

    AdvancedSearchResultsTable getResultsTable();

    void setResultsVisibility(boolean visible);

    void setSelectionMenu(Widget menu);

    void setFilterPanelChangeHandler(SearchSelectionHandler handler);

    Widget asWidget();
}
