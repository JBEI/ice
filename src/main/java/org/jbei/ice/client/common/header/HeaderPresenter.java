package org.jbei.ice.client.common.header;

import org.jbei.ice.shared.dto.EntryType;
import org.jbei.ice.shared.dto.SearchFilterInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class HeaderPresenter {

    public static interface View {
        void createPullDownHandler();

        void addSearchClickHandler(ClickHandler handler);

        String getSearchInput();
    }

    private final View view;
    private SearchFilterInfo blastInfo;
    private EntryType type;           // select search type. null for all

    public HeaderPresenter(View view) {
        this.view = view;
        this.view.createPullDownHandler();
        view.addSearchClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String generalQuery = HeaderPresenter.this.view.getSearchInput();
            }
        });

    }

    public SearchFilterInfo getBlastInfo() {
        return blastInfo;
    }
}
