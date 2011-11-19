package org.jbei.ice.client.search.advanced;

import java.util.ArrayList;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.common.EntryDataViewDataProvider;
import org.jbei.ice.client.event.SearchEvent;
import org.jbei.ice.client.event.SearchSelectionHandler;
import org.jbei.ice.client.util.Utils;
import org.jbei.ice.shared.dto.SearchFilterInfo;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public class AdvancedSearchPresenter extends AbstractPresenter {

    public interface Display {

        ArrayList<SearchFilterInfo> getSearchFilters();

        Button getEvaluateButton();

        AdvancedSearchResultsTable getResultsTable();

        void setResultsVisibility(boolean visible);

        void setSelectionMenu(Widget menu);

        void setFilterPanelChangeHandler(SearchSelectionHandler handler);

        Widget asWidget();
    }

    private final RegistryServiceAsync rpcService;
    private final HandlerManager eventBus;
    private final Display display;
    private EntryDataViewDataProvider dataProvider;

    //    private SelectionMenu menu; // TODO : advanced Search needs its own menu

    public AdvancedSearchPresenter(RegistryServiceAsync rpcService, HandlerManager eventBus,
            Display view) {

        this.rpcService = rpcService;
        this.eventBus = eventBus;
        this.display = view;

        bind();

        // hide the results table
        this.display.setResultsVisibility(false);
        dataProvider = new AdvancedSearchDataProvider(display.getResultsTable(), rpcService);

        // change handler
        new SearchSelectionHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onChange(SearchEvent event) {
                // TODO Auto-generated method stub

            }
        };
    }

    public void bind() {
        EvaluateButtonClickHandler handler = new EvaluateButtonClickHandler();
        this.display.getEvaluateButton().addClickHandler(handler);
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(display.asWidget());
    }

    //
    // inner classes
    //

    private class EvaluateButtonClickHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {

            display.getEvaluateButton().setEnabled(false);
            final Element element = display.getEvaluateButton().getElement();
            Utils.showWaitCursor(element);

            // get search results
            ArrayList<SearchFilterInfo> filters = display.getSearchFilters();
            rpcService.retrieveSearchResults(filters, new AsyncCallback<ArrayList<Long>>() {

                @Override
                public void onSuccess(ArrayList<Long> result) {
                    display.setResultsVisibility(true);
                    dataProvider.setValues(result);
                    reset();
                }

                @Override
                public void onFailure(Throwable caught) {
                    Window.alert("Call failed: " + caught.getMessage());

                    // TODO: Hide the table and show a red error msg in the position where it states
                    // "no records found"
                    display.setResultsVisibility(false);
                    reset();
                }

                public void reset() {
                    display.getEvaluateButton().setEnabled(true);
                    Utils.showDefaultCursor(element);
                }
            });
        }
    }
}
