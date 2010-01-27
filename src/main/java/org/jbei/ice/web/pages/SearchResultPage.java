package org.jbei.ice.web.pages;

import java.util.ArrayList;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.search.Search;
import org.jbei.ice.lib.search.SearchResult;
import org.jbei.ice.web.panels.EmptyMessagePanel;
import org.jbei.ice.web.panels.SearchResultPanel;

public class SearchResultPage extends ProtectedPage {
    String queryString = null;

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public SearchResultPage(PageParameters parameters) {
        super(parameters);
        setQueryString(parameters.getString("search"));

        Logger.info("Search query: " + getQueryString());

        ArrayList<SearchResult> searchResults = null;
        Panel searchResultPanel = null;
        try {
            searchResults = Search.getInstance().query(getQueryString());
            if (searchResults.size() == 0) {
                searchResultPanel = new EmptyMessagePanel("searchResultPanel", "No results found");
            } else {
                searchResultPanel = new SearchResultPanel("searchResultPanel", searchResults, 20);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        add(searchResultPanel);
    }
}
