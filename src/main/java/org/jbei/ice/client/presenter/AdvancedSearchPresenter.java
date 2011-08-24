package org.jbei.ice.client.presenter;

import java.util.ArrayList;

import org.jbei.ice.client.Presenter;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.event.EvaluateQueryEvent;
import org.jbei.ice.client.util.Utils;
import org.jbei.ice.shared.EntryDataView;
import org.jbei.ice.shared.FilterTrans;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

public class AdvancedSearchPresenter implements Presenter {

    private final RegistryServiceAsync rpcService;
    private final HandlerManager eventBus;
    private final Display display;

    public AdvancedSearchPresenter(RegistryServiceAsync rpcService, HandlerManager eventBus,
            Display view) {

        this.rpcService = rpcService;
        this.eventBus = eventBus;
        this.display = view;

        // hide the results table
        this.display.getResultsTable().setPageSize(5);
        this.display.getResultsTable().setVisible(false);

        bind();
    }

    public interface Display {

        ArrayList<FilterTrans> getSearchFilters();

        CellTable<EntryDataView> getResultsTable();

        Button getEvaluateButton();

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
            //            display.getResultsTable().setVisible(true);
            final com.google.gwt.user.client.Element element = display.getEvaluateButton()
                    .getElement();
            Utils.showWaitCursor(element);

            // here to illustrate app wide event broadcast but not really needed
            // we do no need to let the system know that a query is being run
            eventBus.fireEvent(new EvaluateQueryEvent());

            // get search results
            ArrayList<FilterTrans> filters = display.getSearchFilters();

            rpcService.getSearchResults(filters, new AsyncCallback<ArrayList<EntryDataView>>() {

                @Override
                public void onSuccess(ArrayList<EntryDataView> result) {
                    display.getResultsTable().setVisible(true);
                    //                    EntryDataProvider dataProvider = new EntryDataProvider();
                    ListDataProvider<EntryDataView> dataProvider = new ListDataProvider<EntryDataView>();
                    dataProvider.getList().addAll(result);

                    //                    display.getResultsTable().setRowData(0, result);
                    dataProvider.addDataDisplay(display.getResultsTable());

                    reset();
                }

                @Override
                public void onFailure(Throwable caught) {

                    Window.alert("Call failed: " + caught.getMessage());

                    // TODO: Hide the table and show a red error msg in the position where it states
                    // "no records found"

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
