package org.jbei.ice.client.bulkupload.model;

import java.util.ArrayList;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.bulkupload.events.BulkUploadDraftSubmitEvent;
import org.jbei.ice.client.bulkupload.events.BulkUploadSubmitEvent;
import org.jbei.ice.client.bulkupload.events.BulkUploadSubmitEventHandler;
import org.jbei.ice.client.bulkupload.events.SavedDraftsEvent;
import org.jbei.ice.client.bulkupload.events.SavedDraftsEventHandler;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.BulkUploadInfo;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class BulkUploadModel {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;

    public BulkUploadModel(RegistryServiceAsync service, HandlerManager eventBus) {
        this.service = service;
        this.eventBus = eventBus;
    }

    public void retrieveDraftMenuData(final SavedDraftsEventHandler handler) {
        new IceAsyncCallback<ArrayList<BulkUploadInfo>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<BulkUploadInfo>> callback) {
                try {
                    service.retrieveUserSavedDrafts(AppController.sessionId, callback);
                } catch (AuthenticationException e) {
                    History.newItem(Page.LOGIN.getLink());
                }
            }

            @Override
            public void onSuccess(ArrayList<BulkUploadInfo> result) {
                handler.onDataRetrieval(new SavedDraftsEvent(result));
            }
        }.go(eventBus);
    }

    public void saveBulkImportDraftData(final EntryAddType type, final String name,
            final ArrayList<EntryInfo> entryList, final BulkUploadDraftSubmitEvent.BulkUploadDraftSubmitEventHandler
            handler) {

        new IceAsyncCallback<BulkUploadInfo>() {

            @Override
            protected void callService(AsyncCallback<BulkUploadInfo> callback) {
                try {
                    service.saveBulkImportDraft(AppController.sessionId,
                                                name,
                                                type,
                                                entryList, callback);
                } catch (AuthenticationException e) {
                    History.newItem(Page.LOGIN.getLink());
                }
            }

            @Override
            public void onSuccess(BulkUploadInfo result) {
                handler.onSubmit(new BulkUploadDraftSubmitEvent(result));
            }
        }.go(eventBus);
    }

    public void approvePendingBulkImport(final long draftId, final ArrayList<EntryInfo> entryList,
            final BulkUploadSubmitEventHandler handler) {

        new IceAsyncCallback<Boolean>() {

            @Override
            protected void callService(AsyncCallback<Boolean> callback) {
                try {
                    service.approvePendingBulkImport(AppController.sessionId, draftId, entryList, callback);
                } catch (AuthenticationException e) {
                    History.newItem(Page.LOGIN.getLink());
                }
            }

            @Override
            public void onSuccess(Boolean result) {
                handler.onSubmit(new BulkUploadSubmitEvent(result));
            }
        }.go(eventBus);
    }

    public void updateBulkImportDraft(final long id, final EntryAddType type,
            final ArrayList<EntryInfo> entryList, final BulkUploadDraftSubmitEvent.BulkUploadDraftSubmitEventHandler
            handler) {

        new IceAsyncCallback<BulkUploadInfo>() {

            @Override
            protected void callService(AsyncCallback<BulkUploadInfo> callback) {
                try {
                    service.updateBulkImportDraft(AppController.sessionId, id, entryList, callback);
                } catch (AuthenticationException e) {
                    History.newItem(Page.LOGIN.getLink());
                }
            }

            @Override
            public void onSuccess(BulkUploadInfo result) {
                handler.onSubmit(new BulkUploadDraftSubmitEvent(result));
            }
        }.go(eventBus);
    }

    public void submitBulkImport(final EntryAddType type, final ArrayList<EntryInfo> data,
            final BulkUploadSubmitEventHandler handler) {

        new IceAsyncCallback<Boolean>() {

            @Override
            protected void callService(AsyncCallback<Boolean> callback) {
                service.submitBulkImport(AppController.sessionId, type, data, callback);
            }

            @Override
            public void onSuccess(Boolean result) {
                handler.onSubmit(new BulkUploadSubmitEvent(result));
            }
        }.go(eventBus);
    }

    public void retrieveBulkImport(final long id, final SavedDraftsEventHandler handler) {
        new IceAsyncCallback<BulkUploadInfo>() {

            @Override
            protected void callService(AsyncCallback<BulkUploadInfo> callback) {
                try {
                    service.retrieveBulkImport(AppController.sessionId, id, callback);
                } catch (AuthenticationException e) {
                    History.newItem(Page.LOGIN.getLink());
                }
            }

            @Override
            public void onSuccess(BulkUploadInfo result) {
                ArrayList<BulkUploadInfo> data = new ArrayList<BulkUploadInfo>();
                data.add(result);
                handler.onDataRetrieval(new SavedDraftsEvent(data));
            }
        }.go(eventBus);
    }

    public RegistryServiceAsync getService() {
        return this.service;
    }

    public HandlerManager getEventBus() {
        return this.eventBus;
    }

    public void retrieveDraftsPendingVerification(final SavedDraftsEventHandler handler) {
        new IceAsyncCallback<ArrayList<BulkUploadInfo>>() {
            @Override
            protected void callService(AsyncCallback<ArrayList<BulkUploadInfo>> callback) {
                try {
                    service.retrieveDraftsPendingVerification(AppController.sessionId, callback);
                } catch (AuthenticationException e) {
                    History.newItem(Page.LOGIN.getLink());
                }
            }

            @Override
            public void onSuccess(ArrayList<BulkUploadInfo> result) {
                handler.onDataRetrieval(new SavedDraftsEvent(result));
            }
        }.go(eventBus);
    }
}
