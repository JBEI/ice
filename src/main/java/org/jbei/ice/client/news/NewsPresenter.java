package org.jbei.ice.client.news;

import java.util.ArrayList;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.dto.NewsItem;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Presenter for the news page. Currently relies on the fact that
 * there are very few news items and so it loads all of them in memory.
 * Should be re-done to handle large number of news postings
 *
 * @author Hector Plahar
 */

public class NewsPresenter extends AbstractPresenter {

    private final INewsView display;

    public NewsPresenter(RegistryServiceAsync service, HandlerManager eventBus, INewsView news) {
        super(service, eventBus);
        this.display = news;
        bind();

        final SingleSelectionModel<NewsItem> model = this.display.getArchiveSelectionModel();
        model.addSelectionChangeHandler(new Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                NewsItem selected = model.getSelectedObject();
                if (selected == null)
                    return;

                String dateFormat = DateUtilities.formatDate(selected.getCreationDate());
                display.addNewsItem(selected.getId(), dateFormat, selected.getHeader(),
                                    selected.getBody());
            }
        });
    }

    private void bind() {
        display.setAddNewsVisibility(false);

        if (!AppController.accountInfo.isAdmin()) {
            display.setAddNewsButtonVisibilty(false);
        }

        new IceAsyncCallback<ArrayList<NewsItem>>() {
            @Override
            protected void callService(AsyncCallback<ArrayList<NewsItem>> callback) throws AuthenticationException {
                service.retrieveNewsItems(AppController.sessionId, callback);
            }

            @Override
            public void onSuccess(ArrayList<NewsItem> result) {
                display.setArchiveContents(result);

                if (result.isEmpty())
                    return;

                NewsItem item = result.get(0);
                String dateFormat = DateUtilities.formatDate(item.getCreationDate());
                display.addNewsItem(item.getId(), dateFormat, item.getHeader(), item.getBody());
            }
        }.go(eventBus);

        display.getSubmitButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                // TODO : validation
                String title = display.getNewsTitle();
                String body = display.getNewsBody();

                if (title.isEmpty() || body.isEmpty()) {
                    display.setAddNewsVisibility(false);
                    return;
                }

                NewsItem item = new NewsItem();
                item.setHeader(title);
                item.setBody(body);
                save(item);
                display.setAddNewsVisibility(false);
            }
        });
    }

    private void save(final NewsItem item) {

        if (!AppController.accountInfo.isAdmin())
            return;

        new IceAsyncCallback<NewsItem>() {

            @Override
            protected void callService(AsyncCallback<NewsItem> callback) throws AuthenticationException {
                service.createNewsItem(AppController.sessionId, item, callback);
            }

            @Override
            public void onSuccess(NewsItem result) {
                String dateStr = DateUtilities.formatDate(result.getCreationDate());
                display.addNewsItem(result.getId(), dateStr, result.getHeader(), result.getBody());
            }
        }.go(eventBus);
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.display.asWidget());
    }
}
