package org.jbei.ice.client.bulkimport.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.bulkimport.ImportType;
import org.jbei.ice.client.bulkimport.events.SavedDraftsEvent;
import org.jbei.ice.client.bulkimport.events.SavedDraftsEventHandler;
import org.jbei.ice.client.event.AutoCompleteDataEvent;
import org.jbei.ice.client.event.AutoCompleteDataEventHandler;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.dto.BulkImportDraftInfo;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class BulkImportModel {

    private final RegistryServiceAsync service;

    //    private final HandlerManager eventBus;

    public BulkImportModel(RegistryServiceAsync service, HandlerManager eventBus) {
        this.service = service;
        //        this.eventBus = eventBus;
    }

    public void retrieveAutoCompleteData(final AutoCompleteDataEventHandler handler) {
        service.retrieveAutoCompleteData(AppController.sessionId,
            new AsyncCallback<HashMap<AutoCompleteField, ArrayList<String>>>() {

                @Override
                public void onFailure(Throwable caught) {
                    Window.alert("Failed to retrieve the autocomplete data: " + caught.getMessage());
                }

                @Override
                public void onSuccess(HashMap<AutoCompleteField, ArrayList<String>> result) {
                    AutoCompleteDataEvent event = new AutoCompleteDataEvent(result);
                    handler.onDataRetrieval(event);
                }
            });
    }

    public void retrieveDraftMenuData(final SavedDraftsEventHandler handler) {
        service.retrieveImportDraftData(AppController.sessionId,
            AppController.accountInfo.getEmail(),
            new AsyncCallback<ArrayList<BulkImportDraftInfo>>() {

                @Override
                public void onFailure(Throwable caught) {
                    Window.alert("error");
                    //                    feedback.setFailureMessage("Server Error");
                }

                @Override
                public void onSuccess(ArrayList<BulkImportDraftInfo> result) {
                    handler.onDataRetrieval(new SavedDraftsEvent(result));
                }
            });
    }

    public void saveData(ImportType type, HashMap<Integer, ArrayList<String>> data) {
        SheetModel model = ModelFactory.getModelForType(type);
        ArrayList<EntryInfo> entries = model.createInfo(data);

        // actual save
        //        this.service.
        GWT.log("saving " + entries.size());

    }
}
