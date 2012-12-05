package org.jbei.ice.client.common.header;

import org.jbei.ice.client.Page;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.History;

public class HeaderPresenter {

    public static interface View {
        void createPullDownHandler();

        void addSearchClickHandler(ClickHandler handler);

        String getSearchInput();
    }

    private final View view;

    public HeaderPresenter(View view) {
        this.view = view;
        this.view.createPullDownHandler();
        view.addSearchClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String generalQuery = HeaderPresenter.this.view.getSearchInput();
                if (generalQuery.trim().isEmpty())
                    return;

                String url = Page.QUERY.getLink() + ";";
                url += URL.encode(generalQuery);
                History.newItem(url);
            }
        });

    }
}
