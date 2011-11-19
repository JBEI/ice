package org.jbei.ice.client.common;

import org.jbei.ice.client.event.SearchEvent;
import org.jbei.ice.client.event.SearchEventHandler;

public class HeaderPresenter {
    private final HeaderView view;

    public HeaderPresenter(HeaderView view) {
        this.view = view;
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
