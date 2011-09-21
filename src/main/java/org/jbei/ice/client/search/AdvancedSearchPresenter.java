package org.jbei.ice.client.search;

import java.util.ArrayList;

import org.jbei.ice.client.Presenter;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.component.EntryDataViewDataProvider;
import org.jbei.ice.client.component.ExportAsPanel;
import org.jbei.ice.client.component.table.DataTable;
import org.jbei.ice.client.component.table.EntryTablePager;
import org.jbei.ice.client.util.Utils;
import org.jbei.ice.shared.EntryData;
import org.jbei.ice.shared.FilterTrans;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public class AdvancedSearchPresenter implements Presenter {

    private final RegistryServiceAsync rpcService;
    private final HandlerManager eventBus;
    private final Display display;
    private EntryDataViewDataProvider dataProvider;

    public AdvancedSearchPresenter(RegistryServiceAsync rpcService, HandlerManager eventBus,
            Display view) {

        this.rpcService = rpcService;
        this.eventBus = eventBus;
        this.display = view;

        bind();

        // hide the results table
        this.display.getResultsTable().setVisible(false);
        this.display.getPager().setVisible(false);
        this.display.getExportOptions().setVisible(false);

        dataProvider = new EntryDataViewDataProvider(display.getResultsTable(), rpcService);
    }

    public interface Display {

        ArrayList<FilterTrans> getSearchFilters();

        Button getEvaluateButton();

        DataTable<EntryData> getResultsTable();

        EntryTablePager getPager();

        ExportAsPanel getExportOptions();

        Widget asWidget();
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

    private class EvaluateButtonClickHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {

            display.getEvaluateButton().setEnabled(false);
            final com.google.gwt.user.client.Element element = display.asWidget().getElement();
            Utils.showWaitCursor(element);

            // get search results
            ArrayList<FilterTrans> filters = display.getSearchFilters();

            rpcService.retrieveSearchResults(filters, new AsyncCallback<ArrayList<Long>>() {

                @Override
                public void onSuccess(ArrayList<Long> result) {

                    display.getResultsTable().setVisible(true);
                    display.getPager().setVisible(true);
                    display.getExportOptions().setVisible(true);

                    dataProvider.setValues(result);

                    reset();
                }

                @Override
                public void onFailure(Throwable caught) {

                    Window.alert("Call failed: " + caught.getMessage());

                    // TODO: Hide the table and show a red error msg in the position where it states
                    // "no records found"

                    display.getResultsTable().setVisible(false);
                    display.getPager().setVisible(false);
                    display.getExportOptions().setVisible(false);

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
