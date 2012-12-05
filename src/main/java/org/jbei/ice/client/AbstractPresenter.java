package org.jbei.ice.client;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasWidgets;

public abstract class AbstractPresenter {

    protected final RegistryServiceAsync service;
    protected final HandlerManager eventBus;

    public AbstractPresenter(RegistryServiceAsync service, HandlerManager eventBus) {
        this.eventBus = eventBus;
        this.service = service;
    }

//    // TODO : this can go into the abstract presenter at some point
//    protected void addHeaderSearchHandler(final AbstractLayout view) {
//        if (view == null)
//            return;
//
//        view.getHeader().addSearchClickHandler(new ClickHandler() {
//
//            @Override
//            public void onClick(ClickEvent event) {
//                view.getHeader().setSearchButtonEnable(false);
//                ArrayList<SearchFilterInfo> parse = QuickSearchParser.parse(view.getHeader().getSearchInput());
//                SearchFilterInfo blastInfo = view.getHeader().getBlastInfo();
//                if (blastInfo != null)
//                    parse.add(blastInfo);
//                SearchEvent searchInProgressEvent = new SearchEvent();
//                searchInProgressEvent.setFilters(parse);
//                eventBus.fireEvent(searchInProgressEvent);
//                view.getHeader().setSearchButtonEnable(true);
//            }
//        });
//    }


    /**
     * "Entry Point" for any presenter that implements this
     *
     * @param container Container for displaying views
     */
    public abstract void go(final HasWidgets container);
}
