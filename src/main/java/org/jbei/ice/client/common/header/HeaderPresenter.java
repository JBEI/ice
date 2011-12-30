package org.jbei.ice.client.common.header;

import org.jbei.ice.client.event.SearchEvent;
import org.jbei.ice.client.event.SearchEventHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class HeaderPresenter {
    private final HeaderView view;

    public HeaderPresenter(HeaderView view) {
        this.view = view;
    }

    public ClickHandler getSearchHandler() {
        return new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                // TODO Auto-generated method stub
                QuickSearchParser.parse(view.getSearchInput());

            }
        };
    }

    //
    // inner classes
    //
    private static class SearchHandler implements SearchEventHandler {

        @Override
        public void onSearch(SearchEvent event) {
            // TODO Auto-generated method stub
        }
    }
}
