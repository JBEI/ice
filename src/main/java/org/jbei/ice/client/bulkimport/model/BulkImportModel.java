package org.jbei.ice.client.bulkimport.model;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.bulkimport.events.BulkImportDraftSubmitEvent;
import org.jbei.ice.client.bulkimport.events.BulkImportDraftSubmitEvent.BulkImportDraftSubmitEventHandler;
import org.jbei.ice.client.bulkimport.events.BulkImportSubmitEvent;
import org.jbei.ice.client.bulkimport.events.BulkImportSubmitEventHandler;
import org.jbei.ice.client.bulkimport.events.SavedDraftsEvent;
import org.jbei.ice.client.bulkimport.events.SavedDraftsEventHandler;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.BulkImportDraftInfo;
import org.jbei.ice.shared.dto.EntryInfo;

import java.util.ArrayList;

public class BulkImportModel {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;

    public BulkImportModel(RegistryServiceAsync service, HandlerManager eventBus) {
        this.service = service;
        this.eventBus = eventBus;
    }

    public void retrieveDraftMenuData(final SavedDraftsEventHandler handler) {
        new IceAsyncCallback<ArrayList<BulkImportDraftInfo>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<BulkImportDraftInfo>> callback) {
                try {
                    service.retrieveImportDraftData(AppController.sessionId, AppController.accountInfo.getEmail(),
                                                    callback);
                } catch (AuthenticationException e) {
                    History.newItem(Page.LOGIN.getLink());
                }
            }

            @Override
            public void onSuccess(ArrayList<BulkImportDraftInfo> result) {
                handler.onDataRetrieval(new SavedDraftsEvent(result));
            }
        }.go(eventBus);
    }

    public void saveBulkImportDraftData(EntryAddType type, final String name,
            ArrayList<SheetFieldData[]> data, final BulkImportDraftSubmitEventHandler handler) {
        SheetModel model = ModelFactory.getModelForType(type);
        if (model == null) {
            handler.onSubmit(null);
            return;
        }

        final ArrayList<EntryInfo> primary = new ArrayList<EntryInfo>();
        final ArrayList<EntryInfo> secondary = new ArrayList<EntryInfo>();

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

        new IceAsyncCallback<BulkImportDraftInfo>() {

            @Override
            protected void callService(AsyncCallback<BulkImportDraftInfo> callback) {
                try {
                    service.saveBulkImportDraft(AppController.sessionId, AppController.accountInfo.getEmail(), name,
                                                primary, secondary, callback);
                } catch (AuthenticationException e) {
                    History.newItem(Page.LOGIN.getLink());
                }
            }

            @Override
            public void onSuccess(BulkImportDraftInfo result) {
                handler.onSubmit(new BulkImportDraftSubmitEvent(result));
            }
        }.go(eventBus);
    }

    public void updateBulkImportDraft(final long id, EntryAddType type, final String name,
            ArrayList<SheetFieldData[]> data, final BulkImportDraftSubmitEventHandler handler) {
        SheetModel model = ModelFactory.getModelForType(type);
        if (model == null) {
            handler.onSubmit(null);
            return;
        }

        final ArrayList<EntryInfo> primary = new ArrayList<EntryInfo>();
        final ArrayList<EntryInfo> secondary = new ArrayList<EntryInfo>();

        // arrays get filled out here
        model.createInfo(data, primary, secondary);

        new IceAsyncCallback<BulkImportDraftInfo>() {

            @Override
            protected void callService(AsyncCallback<BulkImportDraftInfo> callback) {
                try {
                    service.updateBulkImportDraft(AppController.sessionId, id, AppController.accountInfo.getEmail(),
                                                  name, primary, secondary, callback);
                } catch (AuthenticationException e) {
                    History.newItem(Page.LOGIN.getLink());
                }
            }

            @Override
            public void onSuccess(BulkImportDraftInfo result) {
                if (result.getPrimary() == null)
                    result.setPrimary(primary);

                if (result.getSecondary() == null)
                    result.setSecondary(secondary);
                handler.onSubmit(new BulkImportDraftSubmitEvent(result));
            }
        }.go(eventBus);
    }

    public void submitBulkImport(EntryAddType type, ArrayList<SheetFieldData[]> data,
            final BulkImportSubmitEventHandler handler) {
        SheetModel model = ModelFactory.getModelForType(type);
        if (model != null) {
            final ArrayList<EntryInfo> primary = new ArrayList<EntryInfo>();
            final ArrayList<EntryInfo> secondary = new ArrayList<EntryInfo>();

            // arrays get filled out here
            model.createInfo(data, primary, secondary);

            new IceAsyncCallback<Boolean>() {

                @Override
                protected void callService(AsyncCallback<Boolean> callback) {
                    try {
                        service.submitBulkImport(AppController.sessionId, AppController.accountInfo.getEmail(),
                                                 primary, secondary, callback);
                    } catch (AuthenticationException e) {
                        History.newItem(Page.LOGIN.getLink());
                    }
                }

                @Override
                public void onSuccess(Boolean result) {
                    handler.onSubmit(new BulkImportSubmitEvent(result));
                }
            }.go(eventBus);
        }
    }

    public void retrieveBulkImport(final long id, final SavedDraftsEventHandler handler) {
        new IceAsyncCallback<BulkImportDraftInfo>() {

            @Override
            protected void callService(AsyncCallback<BulkImportDraftInfo> callback) {
                try {
                    service.retrieveBulkImport(AppController.sessionId, id, callback);
                } catch (AuthenticationException e) {
                    History.newItem(Page.LOGIN.getLink());
                }
            }

            @Override
            public void onSuccess(BulkImportDraftInfo result) {
                ArrayList<BulkImportDraftInfo> data = new ArrayList<BulkImportDraftInfo>();
                data.add(result);
                handler.onDataRetrieval(new SavedDraftsEvent(data));
            }
        }.go(eventBus);
    }

    public RegistryServiceAsync getService() {
        return this.service;
    }
}
