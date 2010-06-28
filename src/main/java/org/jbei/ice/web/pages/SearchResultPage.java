package org.jbei.ice.web.pages;

import java.util.ArrayList;

import org.apache.wicket.PageParameters;
import org.jbei.ice.controllers.SearchController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.search.lucene.SearchResult;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.panels.EmptyMessagePanel;
import org.jbei.ice.web.panels.SearchResultPanel;

public class SearchResultPage extends ProtectedPage {
    private final int NUMBER_OF_ENTRIES_PER_PAGE = 15;
    private final String SEARCH_RESULT_PANEL_NAME = "searchResultPanel";

    public SearchResultPage(PageParameters parameters) {
        super(parameters);

        if (parameters.getString("search") != null) {
            String queryString = parameters.getString("search");

            ArrayList<SearchResult> searchResults;

            SearchController searchController = new SearchController(IceSession.get().getAccount());

            try {
                searchResults = searchController.find(queryString);

                if (searchResults == null || searchResults.size() == 0) {
                    add(new EmptyMessagePanel(SEARCH_RESULT_PANEL_NAME, "No results found"));
                } else {
                    add(new SearchResultPanel(SEARCH_RESULT_PANEL_NAME, searchResults,
                            NUMBER_OF_ENTRIES_PER_PAGE));
                }
            } catch (ControllerException e) {
                throw new ViewException(e);
            }
        } else {
            add(new EmptyMessagePanel(SEARCH_RESULT_PANEL_NAME, "No results found"));
        }

    }

    @Override
    protected String getTitle() {
        return "Search - " + super.getTitle();
    }
}
