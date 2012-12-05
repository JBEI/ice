package org.jbei.ice.client.search.advanced;

import java.util.ArrayList;

import org.jbei.ice.client.search.blast.BlastResultsTable;
import org.jbei.ice.shared.dto.EntryType;
import org.jbei.ice.shared.dto.SearchFilterInfo;

import com.google.gwt.user.client.ui.Widget;

/**
 * Interface for advanced search view
 *
 * @author Hector Plahar
 */
public interface ISearchView {

    void setSearchFilters(ArrayList<SearchFilterInfo> filters);

    Widget asWidget();

    void setSearchVisibility(AdvancedSearchResultsTable table, boolean visible);

    void setBlastVisibility(BlastResultsTable table, boolean visible);

    ArrayList<SearchFilterInfo> parseUrlForFilters();

    EntryType[] getSearchTypes();
}
