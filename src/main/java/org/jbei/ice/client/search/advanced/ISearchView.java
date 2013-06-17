package org.jbei.ice.client.search.advanced;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;
import org.jbei.ice.client.search.blast.BlastResultsTable;
import org.jbei.ice.shared.dto.search.SearchQuery;

/**
 * Interface for advanced search view
 *
 * @author Hector Plahar
 */
public interface ISearchView {

    Widget asWidget();

    void setSearchVisibility(SearchResultsTable table, boolean visible);

    void setBlastVisibility(Widget w, BlastResultsTable table, boolean visible);

    SearchQuery parseUrlForQuery();

    void showWebOfRegistryOptions(boolean show);

    public void setLocalSearchHandler(ClickHandler handler);

    public void setWebSearchHandler(ClickHandler handler);
}
