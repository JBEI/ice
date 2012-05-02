package org.jbei.ice.client.bulkimport.model;

import java.util.ArrayList;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.bulkimport.events.BulkImportDraftSubmitEvent;
import org.jbei.ice.client.bulkimport.events.BulkImportDraftSubmitEvent.BulkImportDraftSubmitEventHandler;
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
                    Window.alert("Error retrieving saved drafts");
                }

                @Override
                public void onSuccess(ArrayList<BulkImportDraftInfo> result) {
                    handler.onDataRetrieval(new SavedDraftsEvent(result));
                }
            });
    }

    public void saveBulkImportDraftData(EntryAddType type, String name, ArrayList<SheetFieldData[]> data,
            final BulkImportDraftSubmitEventHandler handler) {
        SheetModel model = ModelFactory.getModelForType(type);
        if (model == null) {
            handler.onSubmit(null);
            return;
        }

        ArrayList<EntryInfo> primary = new ArrayList<EntryInfo>();
        ArrayList<EntryInfo> secondary = new ArrayList<EntryInfo>();

        // arrays get filled out here
        model.createInfo(data, primary, secondary);

        // creator info does not appear to be filled out anywhere
        String creator = AppController.accountInfo.getFullName();
        String creatorEmail = AppController.accountInfo.getEmail();
        for (EntryInfo info : primary) {
            info.setCreator(creator);
            info.setCreatorEmail(creatorEmail);
        }

        for (EntryInfo info : secondary) {
            info.setCreator(creator);
            info.setCreatorEmail(creatorEmail);
        }

        service.saveBulkImportDraft(AppController.sessionId, AppController.accountInfo.getEmail(),
            name, primary, secondary, new AsyncCallback<BulkImportDraftInfo>() {

                @Override
                public void onSuccess(BulkImportDraftInfo result) {
                    handler.onSubmit(new BulkImportDraftSubmitEvent(result));
                }

                @Override
                public void onFailure(Throwable caught) {
                    handler.onSubmit(null);
                }
            });
    }

    public void updateBulkImportDraft(long id, EntryAddType type, String name,
            ArrayList<SheetFieldData[]> data, final BulkImportDraftSubmitEventHandler handler) {
        SheetModel model = ModelFactory.getModelForType(type);
        if (model == null) {
            handler.onSubmit(null);
            return;
        }

        ArrayList<EntryInfo> primary = new ArrayList<EntryInfo>();
        ArrayList<EntryInfo> secondary = new ArrayList<EntryInfo>();

        // arrays get filled out here
        model.createInfo(data, primary, secondary);

        service.updateBulkImportDraft(AppController.sessionId, id,
            AppController.accountInfo.getEmail(), name, primary, secondary,
            new AsyncCallback<BulkImportDraftInfo>() {

                @Override
                public void onSuccess(BulkImportDraftInfo result) {
                    handler.onSubmit(new BulkImportDraftSubmitEvent(result));
                }

                @Override
                public void onFailure(Throwable caught) {
                    handler.onSubmit(null);
                }
            });
    }

    public void submitBulkImport(EntryAddType type, ArrayList<SheetFieldData[]> data,
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

    public void retrieveBulkImport(long id, final SavedDraftsEventHandler handler) {
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
}
