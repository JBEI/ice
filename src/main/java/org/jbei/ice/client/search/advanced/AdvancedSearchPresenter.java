package org.jbei.ice.client.search.advanced;

import java.util.ArrayList;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.collection.menu.ExportAsMenu;
import org.jbei.ice.client.collection.menu.UserCollectionMultiSelect;
import org.jbei.ice.client.common.EntryDataViewDataProvider;
import org.jbei.ice.client.common.header.QuickSearchParser;
import org.jbei.ice.client.event.SearchEvent;
import org.jbei.ice.client.event.SearchSelectionHandler;
import org.jbei.ice.client.util.Utils;
import org.jbei.ice.shared.FolderDetails;
import org.jbei.ice.shared.dto.SearchFilterInfo;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.view.client.ListDataProvider;

public class AdvancedSearchPresenter extends AbstractPresenter {

    private final RegistryServiceAsync rpcService;
    private final HandlerManager eventBus;
    private final IAdvancedSearchView display;
    private final EntryDataViewDataProvider dataProvider;
    private final ListDataProvider<FolderDetails> userCollectionDataProvider;

    public AdvancedSearchPresenter(RegistryServiceAsync rpcService, HandlerManager eventBus,
            IAdvancedSearchView view) {

        this.rpcService = rpcService;
        this.eventBus = eventBus;
        this.display = view;
        userCollectionDataProvider = new ListDataProvider<FolderDetails>();

        retrieveUserCollections();

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

    public AdvancedSearchPresenter(RegistryServiceAsync rpcService, HandlerManager eventBus,
            IAdvancedSearchView view, String query) {
        this(rpcService, eventBus, view);
        ArrayList<SearchFilterInfo> filters = QuickSearchParser.parse(query);
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
                Utils.showDefaultCursor(null);
            }
        });

    }

    public void retrieveUserCollections() {
        this.rpcService.retrieveUserCollections(AppController.sessionId,
            AppController.accountInfo.getEmail(), new AsyncCallback<ArrayList<FolderDetails>>() {

                @Override
                public void onFailure(Throwable caught) {
                    Window.alert("Call for user collection failed: " + caught.getMessage());
                }

                @Override
                public void onSuccess(ArrayList<FolderDetails> result) {
                    Button submit = new Button("Submit");
                    userCollectionDataProvider.setList(result);
                    UserCollectionMultiSelect selection = new UserCollectionMultiSelect(submit,
                            userCollectionDataProvider, null);
                    AddToMenu addToMenu = new AddToMenu(selection);
                    HeaderMenu header = new HeaderMenu(new ExportAsMenu(), addToMenu);
                    display.setSelectionMenu(header);
                }
            });
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
            Utils.showWaitCursor(null);

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
                    Utils.showDefaultCursor(null);
                }
            });
        }
    }
}
