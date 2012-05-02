package org.jbei.ice.client.collection;

import com.google.gwt.view.client.SelectionChangeEvent;

class EntryMenuSelectionHandler implements SelectionChangeEvent.Handler {

    @Override
    public void onSelectionChange(SelectionChangeEvent event) {/*
                                                               final EntryMenu selection = menuSelectionModel.getSelectedObject();
                                                               if (selection == null)
                                                               return;

                                                               switch (selection) {
                                                               case MINE:

                                                               service.retrieveUserEntries(sid, null, new AsyncCallback<ArrayList<Long>>() {

                                                               @Override
                                                               public void onFailure(Throwable caught) {
                                                               Window.alert(caught.getMessage());
                                                               }

                                                               @Override
                                                               public void onSuccess(ArrayList<Long> result) {
                                                               if (result == null)
                                                               return;

                                                               // clear folder selection
                                                               if (folderSelection != null)
                                                               folderSelectionModel.setSelected(folderSelection, false);

                                                               menuSelection = selection;
                                                               clearDataDisplayFromProviders();
                                                               entryDataProvider.addDataDisplay(entriesDataTable);
                                                               entriesDataTable.setVisibleRangeAndClearData(
                                                               entriesDataTable.getVisibleRange(), false);
                                                               entryDataProvider.setValues(result);
                                                               display.setDataView(entriesDataTable);
                                                               }
                                                               });
                                                               break;

                                                               case ALL:

                                                               service.retrieveAllEntryIDs(sid, new AsyncCallback<ArrayList<Long>>() {

                                                               @Override
                                                               public void onFailure(Throwable caught) {
                                                               Window.alert("Error: " + caught.getMessage());
                                                               }

                                                               @Override
                                                               public void onSuccess(ArrayList<Long> result) {
                                                               if (result == null)
                                                               return;

                                                               // clear folder selection
                                                               if (folderSelection != null)
                                                               folderSelectionModel.setSelected(folderSelection, false);

                                                               menuSelection = selection;
                                                               clearDataDisplayFromProviders();
                                                               entryDataProvider.addDataDisplay(entriesDataTable);
                                                               entriesDataTable.setVisibleRangeAndClearData(
                                                               entriesDataTable.getVisibleRange(), false);
                                                               entryDataProvider.setValues(result);
                                                               display.setDataView(entriesDataTable);
                                                               }
                                                               });
                                                               break;

                                                               case RECENTLY_VIEWED:
                                                               service.retrieveRecentlyViewed(sid, new AsyncCallback<ArrayList<Long>>() {

                                                               @Override
                                                               public void onFailure(Throwable caught) {
                                                               Window.alert("Error: " + caught.getMessage());
                                                               }

                                                               @Override
                                                               public void onSuccess(ArrayList<Long> result) {
                                                               if (result == null)
                                                               return;

                                                               if (folderSelection != null)
                                                               folderSelectionModel.setSelected(folderSelection, false);

                                                               menuSelection = selection;
                                                               clearDataDisplayFromProviders();
                                                               entryDataProvider.addDataDisplay(recentlyViewedDataView);
                                                               entriesDataTable.setVisibleRangeAndClearData(
                                                               entriesDataTable.getVisibleRange(), false);
                                                               entryDataProvider.setValues(result);
                                                               display.setDataView(recentlyViewedDataView);
                                                               }
                                                               });

                                                               break;

                                                               case SAMPLES:
                                                               //                    service.retrieveSamples(sid, new AsyncCallback<ArrayList<Long>>() {
                                                               //
                                                               //                        @Override
                                                               //                        public void onFailure(Throwable caught) {
                                                               //                            Window.alert("Error: " + caught.getMessage());
                                                               //                        }
                                                               //
                                                               //                        @Override
                                                               //                        public void onSuccess(ArrayList<Long> result) {
                                                               //                            if (result == null)
                                                               //                                return;
                                                               //
                                                               //                            if (folderSelection != null)
                                                               //                                folderModel.setSelected(folderSelection, false);
                                                               //
                                                               //                            menuSelection = selection;
                                                               //                            entryDataProvider.setValues(result);
                                                               //                        }
                                                               //                    });

                                                               break;

                                                               case WORKSPACE:
                                                               service.retrieveWorkspaceEntries(sid, new AsyncCallback<ArrayList<Long>>() {

                                                               @Override
                                                               public void onFailure(Throwable caught) {
                                                               Window.alert("Error: " + caught.getMessage());
                                                               }

                                                               @Override
                                                               public void onSuccess(ArrayList<Long> result) {
                                                               if (result == null)
                                                               return;

                                                               if (folderSelection != null)
                                                               folderSelectionModel.setSelected(folderSelection, false);

                                                               menuSelection = selection;
                                                               entryDataProvider.setValues(result);
                                                               }
                                                               });

                                                               break;

                                                               default:
                                                               Window.alert("Could not handle menu selection of : " + selection.getDisplay());
                                                               }*/
    }
}
