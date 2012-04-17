package org.jbei.ice.client.news;

import java.util.ArrayList;
import java.util.Collections;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.dto.NewsItem;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;

/**
 * Presenter for the news page
 * 
 * @author Hector Plahar
 */
public class NewsPresenter extends AbstractPresenter {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final INewsView display;

    public NewsPresenter(RegistryServiceAsync service, HandlerManager eventBus, INewsView news) {
        this.service = service;
        this.eventBus = eventBus;
        this.display = news;
        bind();
    }

    private void bind() {
        display.setAddNewsVisibility(false);

        if (!AppController.accountInfo.isModerator()) {
            display.setAddNewsButtonVisibilty(false);
        }

        service.retrieveNewsItems(AppController.sessionId,
            new AsyncCallback<ArrayList<NewsItem>>() {

                @Override
                public void onSuccess(ArrayList<NewsItem> result) {
                    if (result == null)
                        return;

                    if (result.isEmpty()) {

                    } else {
                        Collections.reverse(result); // TODO :
                        for (NewsItem item : result) {
                            String dateFormat = DateUtilities.formatDate(item.getCreationDate());
                            display.addNewsItem(item.getId(), dateFormat, item.getHeader(),
                                item.getBody());
                        }
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                    Window.alert("failed to retrieve news");
                }
            });

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

    private void save(NewsItem item) {

        if (!AppController.accountInfo.isModerator())
            return;

        service.createNewsItem(AppController.sessionId, item, new AsyncCallback<NewsItem>() {

            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }

            @Override
            public void onSuccess(NewsItem result) {
                String dateStr = DateUtilities.formatDate(result.getCreationDate());
                display.addNewsItem(result.getId(), dateStr, result.getHeader(), result.getBody());
            }
        });
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.display.asWidget());
    }
}
