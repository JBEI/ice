package org.jbei.ice.client.presenter;

import java.util.ArrayList;

import org.jbei.ice.client.Presenter;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.component.EntryTableDataProvider;
import org.jbei.ice.client.component.EntryTablePager;
import org.jbei.ice.client.component.ExportAsPanel;
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
    private EntryTableDataProvider dataProvider;

    public AdvancedSearchPresenter(RegistryServiceAsync rpcService, HandlerManager eventBus,
            Display view) {

        this.rpcService = rpcService;
        this.eventBus = eventBus;
        this.display = view;

        // hide the results table
        this.display.getResultsTable().setPageSize(5);
        this.display.getResultsTable().setVisible(false);

        bind();

        this.display.getResultsTable().setVisible(false);
        this.display.getPager().setVisible(false);
        this.display.getExportOptions().setVisible(false);
    }

    public interface Display {

        ArrayList<FilterTrans> getSearchFilters();

        Button getEvaluateButton();

        CellTable<EntryDataView> getResultsTable();

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
            //            display.getResultsTable().setVisible(true);
            final com.google.gwt.user.client.Element element = display.getEvaluateButton()
                    .getElement();
            Utils.showWaitCursor(element);

            // here to illustrate app wide event broadcast but not really needed
            // we do no need to let the system know that a query is being run
            //            eventBus.fireEvent(new EvaluateQueryEvent());

            // get search results
            ArrayList<FilterTrans> filters = display.getSearchFilters();

            rpcService.retrieveSearchResults(filters, new AsyncCallback<ArrayList<Long>>() {

                @Override
                public void onSuccess(ArrayList<Long> result) {
                    CellTable<EntryDataView> table = display.getResultsTable();

                    // TODO : instead of this, just update the result in the data provider
                    // which should then take care of updating the display settings (row count etc)
                    // e.g. dataProvider.getList().addAll(result);

                    if (dataProvider != null)
                        dataProvider.removeDataDisplay(table);

                    dataProvider = new EntryTableDataProvider(result, rpcService);
                    dataProvider.addDataDisplay(table);
                    table.setVisible(true);
                    ListDataProvider<EntryDataView> dataProvider = new ListDataProvider<EntryDataView>();

                    //                    display.getResultsTable().setRowData(0, result);
                    dataProvider.addDataDisplay(display.getResultsTable());

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
