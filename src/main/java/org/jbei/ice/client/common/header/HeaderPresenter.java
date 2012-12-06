package org.jbei.ice.client.common.header;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.Page;
import org.jbei.ice.shared.dto.EntryType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.History;

public class HeaderPresenter {

    public static interface View {
        void createPullDownHandler();

        void addSearchClickHandler(ClickHandler handler);

        String getSearchInput();

        EntryType[] getSearchEntryTypes();
    }

    private final View view;

    public HeaderPresenter(View view) {
        this.view = view;
        this.view.createPullDownHandler();
        view.addSearchClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // general query
                String generalQuery = HeaderPresenter.this.view.getSearchInput();
                String url = Page.QUERY.getLink() + AppController.URL_SEPARATOR;
                url += URL.encode(generalQuery);

                // search entry types
                EntryType[] types = HeaderPresenter.this.view.getSearchEntryTypes();
                if (types == null || types.length == EntryType.values().length) {
                    History.newItem(url);
                    return;
                }
                url += "&type=";
                for (EntryType type : types) {
                    url += (type.getName() + ",");
                }
                url = url.substring(0, url.length() - 1);

                History.newItem(url);
            }
        });
    }
}
