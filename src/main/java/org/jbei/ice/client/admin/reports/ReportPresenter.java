package org.jbei.ice.client.admin.reports;

import java.util.HashMap;

import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.admin.AdminPanel;
import org.jbei.ice.client.admin.AdminPanelPresenter;
import org.jbei.ice.shared.dto.EntryInfo.EntryType;

public class ReportPresenter implements AdminPanelPresenter {

    private final RegistryServiceAsync service;
    private final HashMap<EntryType, Long> counts;

    public ReportPresenter(RegistryServiceAsync service) {
        this.service = service;
        this.counts = new HashMap<EntryType, Long>();
    }

    /**
     * @return Options for creating a pie chart
     */
    //    private Options createOptions() {
    //        Options options = Options.create();
    //        options.setWidth(500);
    //        options.setHeight(350);
    //        options.set3D(true);
    //        options.setTitle("Registry Breakdown for "
    //                + AppController.accountInfo.getVisibleEntryCount() + " entries");
    //        return options;
    //    }

    //    private SelectHandler createSelectHandler(final PieChart chart) {
    //        return new SelectHandler() {
    //            @Override
    //            public void onSelect(SelectEvent event) {
    //                String message = "";
    //
    //                // May be multiple selections.
    //                JsArray<Selection> selections = chart.getSelections();
    //
    //                for (int i = 0; i < selections.length(); i++) {
    //                    // add a new line for each selection
    //                    message += i == 0 ? "" : "\n";
    //
    //                    Selection selection = selections.get(i);
    //
    //                    if (selection.isCell()) {
    //                        // isCell() returns true if a cell has been selected.
    //
    //                        // getRow() returns the row number of the selected cell.
    //                        int row = selection.getRow();
    //                        // getColumn() returns the column number of the selected cell.
    //                        int column = selection.getColumn();
    //                        message += "cell " + row + ":" + column + " selected";
    //                    } else if (selection.isRow()) {
    //                        // isRow() returns true if an entire row has been selected.
    //
    //                        // getRow() returns the row number of the selected row.
    //                        int row = selection.getRow();
    //                        message += "row " + row + " selected";
    //                    } else {
    //                        // unreachable
    //                        message += "Pie chart selections should be either row selections or cell selections.";
    //                        message += "  Other visualizations support column selections as well.";
    //                    }
    //                }
    //
    //                //                Window.alert(message);
    //            }
    //        };
    //    }

    /**
     * Following is from the tutorial
     * For all charts, the data is created using the DataTable class or the DataView class.
     * A DataTable is a two dimensional table with rows and columns and cells. As with the Options
     * classes, the DataTable class is a subclass of JavaScriptObject, so new instances are created
     * by calling the static method DataTable.create(). Each column has a data type defined by the
     * DataTable.ColumnType enum.
     * 
     * @return
     */
    //    private AbstractDataTable createTable() {
    //        DataTable data = DataTable.create();
    //
    //        int rowCount = counts.size();
    //        data.addRows(rowCount);
    //        data.addColumn(ColumnType.STRING, "Record Type");
    //        data.addColumn(ColumnType.NUMBER, "Count");
    //
    //        int row = 0;
    //        for (Entry<EntryType, Long> entry : counts.entrySet()) {
    //            data.setValue(row, 0, entry.getKey().getDisplay());
    //            data.setValue(row, 1, entry.getValue().intValue());
    //            row += 1;
    //        }
    //
    //        //        data.addRows(2);
    //        //        data.setValue(0, 0, "Work");
    //        //        data.setValue(0, 1, 14);
    //        //        data.setValue(1, 0, "Sleep");
    //        //        data.setValue(1, 1, 10);
    //        return data;
    //    }

    @Override
    public void go(final AdminPanel container) {
        // show a loading indicator

        //        if (counts.size() > 0) {
        //            // Create a callback to be called when the visualization API
        //            // has been loaded.
        //            Runnable onLoadCallback = new Runnable() {
        //                public void run() {
        //                    ReportPanel panel = (ReportPanel) container;
        //
        //                    // Create a pie chart visualization.
        //                    PieChart pie = new PieChart(createTable(), createOptions());
        //                    pie.addSelectHandler(createSelectHandler(pie));
        //                    panel.addWidget(pie);
        //                }
        //            };
        //
        //            // Load the visualization api, passing the onLoadCallback to be called
        //            // when loading is done.
        //            VisualizationUtils.loadVisualizationApi(onLoadCallback, PieChart.PACKAGE);
        //            return;
        //        }
        //
        //        // else retrieve data
        //        service.retrieveEntryCounts(AppController.sessionId,
        //            new AsyncCallback<HashMap<EntryType, Long>>() {
        //
        //                @Override
        //                public void onSuccess(HashMap<EntryType, Long> result) {
        //                    counts.clear();
        //                    counts.putAll(result);
        //
        //                    // Create a callback to be called when the visualization API
        //                    // has been loaded.
        //                    Runnable onLoadCallback = new Runnable() {
        //                        public void run() {
        //                            ReportPanel panel = (ReportPanel) container;
        //
        //                            // Create a pie chart visualization.
        //                            PieChart pie = new PieChart(createTable(), createOptions());
        //                            pie.addSelectHandler(createSelectHandler(pie));
        //                            panel.addWidget(pie);
        //                        }
        //                    };
        //
        //                    // Load the visualization api, passing the onLoadCallback to be called
        //                    // when loading is done.
        //                    VisualizationUtils.loadVisualizationApi(onLoadCallback, PieChart.PACKAGE);
        //                }
        //
        //                @Override
        //                public void onFailure(Throwable caught) {
        //                }
        //            });
    }
}
