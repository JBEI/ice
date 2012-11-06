package org.jbei.ice.client.home;

import org.jbei.ice.shared.dto.NewsItem;

import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SingleSelectionModel;

public class ArchiveMenuPresenter {

    private final SingleSelectionModel<NewsItem> selectionModel;

    public ArchiveMenuPresenter() {
        selectionModel = new SingleSelectionModel<NewsItem>(new ProvidesKey<NewsItem>() {

            @Override
            public String getKey(NewsItem item) {
                return item.getId();
            }
        });
    }

    void setSelected(NewsItem item) {
        NewsItem prevSelected = selectionModel.getSelectedObject();
        if (prevSelected != null)
            selectionModel.setSelected(prevSelected, false);
        selectionModel.setSelected(item, true);
    }

    public SingleSelectionModel<NewsItem> getSelectionModel() {
        return selectionModel;
    }
}
