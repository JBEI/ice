package org.jbei.ice.client.home;

import java.util.ArrayList;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.shared.dto.NewsItem;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;

public class HomePagePresenter extends AbstractPresenter {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final IHomePageView display;

    public HomePagePresenter(RegistryServiceAsync service, HandlerManager eventBus,
            IHomePageView display) {

        this.service = service;
        this.eventBus = eventBus;
        this.display = display;
        bind();
    }

    protected void bind() {

        service.retrieveNewsItems(AppController.sessionId,
            new AsyncCallback<ArrayList<NewsItem>>() {

                @Override
                public void onSuccess(ArrayList<NewsItem> result) {
                    if (result == null)
                        return;

                    if (result.isEmpty()) {

                    } else {
                        for (NewsItem item : result) {
                            display.addNewsItem(item.getId(), item.getCreationDate().toString(),
                                item.getHeader(), item.getBody());
                        }
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                    // TODO

                }
            });
    }

    @Override
    public void go(HasWidgets container) {

        container.clear();
        container.add(this.display.asWidget());
    }
}
