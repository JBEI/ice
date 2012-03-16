package org.jbei.ice.client.bulkimport.model;

import java.util.ArrayList;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.bulkimport.events.BulkImportSubmitEvent;
import org.jbei.ice.client.bulkimport.events.BulkImportSubmitEventHandler;
import org.jbei.ice.client.bulkimport.events.SavedDraftsEvent;
import org.jbei.ice.client.bulkimport.events.SavedDraftsEventHandler;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.BulkImportDraftInfo;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class BulkImportModel {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;

    public BulkImportModel(RegistryServiceAsync service, HandlerManager eventBus) {
        this.service = service;
        this.eventBus = eventBus;
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

    public void saveData(EntryAddType type, ArrayList<SheetFieldData[]> data,
            final BulkImportSubmitEventHandler handler) {
        SheetModel model = ModelFactory.getModelForType(type);
        if (model != null) {
            ArrayList<EntryInfo> primary = new ArrayList<EntryInfo>();
            ArrayList<EntryInfo> secondary = new ArrayList<EntryInfo>();

            // arrays get filled out here
            model.createInfo(data, primary, secondary);

            service.submitBulkImport(AppController.sessionId, AppController.accountInfo.getEmail(),
                primary, secondary, new AsyncCallback<Boolean>() {

                    @Override
                    public void onSuccess(Boolean result) {
                        handler.onSubmit(new BulkImportSubmitEvent(result));
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        handler.onSubmit(new BulkImportSubmitEvent(false));
                    }
                });
        }
    }

    public void retrieveUserSavedDrafts(long id, final SavedDraftsEventHandler handler) {
        service.retrieveBulkImport(AppController.sessionId, id,
            new AsyncCallback<BulkImportDraftInfo>() {

                @Override
                public void onFailure(Throwable caught) {
                    handler.onDataRetrieval(null);
                }

                @Override
                public void onSuccess(BulkImportDraftInfo result) {
                    ArrayList<BulkImportDraftInfo> data = new ArrayList<BulkImportDraftInfo>();
                    data.add(result);
                    handler.onDataRetrieval(new SavedDraftsEvent(data));
                }
            });
    }

    /*
     * service.saveBulkImportDraft(AppController.sessionId,
                AppController.accountInfo.getEmail(), name, entries,
                new AsyncCallback<BulkImportDraftInfo>() {

                    @Override
                    public void onSuccess(BulkImportDraftInfo result) {
                        if (result == null)
                            return;

                        view.getDraftMenu().addMenuData(result);
                        panel.setDraftName(result.getName());

                        // change view draft to read only

                        // highlight just added menu; associate just created one with top menu

                        // 
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        feedback.setFailureMessage("Problem saving draft");
                    }
                });
     */
}
