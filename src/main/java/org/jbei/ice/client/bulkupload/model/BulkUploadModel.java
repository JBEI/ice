package org.jbei.ice.client.bulkupload.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.bulkupload.events.BulkUploadDraftSubmitEvent;
import org.jbei.ice.client.bulkupload.events.BulkUploadSubmitEvent;
import org.jbei.ice.client.bulkupload.events.BulkUploadSubmitEventHandler;
import org.jbei.ice.client.bulkupload.events.SavedDraftsEvent;
import org.jbei.ice.client.bulkupload.events.SavedDraftsEventHandler;
import org.jbei.ice.client.collection.add.form.SampleLocation;
import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.client.event.FeedbackEvent;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.BulkUploadInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.EntryType;
import org.jbei.ice.shared.dto.SampleInfo;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class BulkUploadModel {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final HashMap<EntryType, SampleLocation> locationCache;

    public BulkUploadModel(RegistryServiceAsync service, HandlerManager eventBus) {
        this.service = service;
        this.eventBus = eventBus;
        locationCache = new HashMap<EntryType, SampleLocation>();
    }

    public void retrieveDraftMenuData(final SavedDraftsEventHandler handler) {
        new IceAsyncCallback<ArrayList<BulkUploadInfo>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<BulkUploadInfo>> callback)
                    throws AuthenticationException {
                service.retrieveUserSavedDrafts(AppController.sessionId, callback);
            }

            @Override
            public void onSuccess(ArrayList<BulkUploadInfo> result) {
                handler.onDataRetrieval(new SavedDraftsEvent(result));
            }
        }.go(eventBus);
    }

    public void saveBulkImportDraftData(final EntryAddType type, final String name,
            final ArrayList<EntryInfo> entryList, final String groupUUID,
            final BulkUploadDraftSubmitEvent.BulkUploadDraftSubmitEventHandler handler) {

        new IceAsyncCallback<BulkUploadInfo>() {

            @Override
            protected void callService(AsyncCallback<BulkUploadInfo> callback) throws AuthenticationException {
                service.saveBulkImportDraft(AppController.sessionId, name, type, entryList, groupUUID, callback);
            }

            @Override
            public void onSuccess(BulkUploadInfo result) {
                handler.onSubmit(new BulkUploadDraftSubmitEvent(result));
            }
        }.go(eventBus);
    }

    public void approvePendingBulkImport(final long draftId, final ArrayList<EntryInfo> entryList,
            final String groupUUID, final BulkUploadSubmitEventHandler handler) {

        new IceAsyncCallback<Boolean>() {

            @Override
            protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                service.approvePendingBulkImport(AppController.sessionId, draftId, entryList, groupUUID, callback);
            }

            @Override
            public void onSuccess(Boolean result) {
                handler.onSubmit(new BulkUploadSubmitEvent(result));
            }
        }.go(eventBus);
    }

    public void updateBulkImportDraft(final long id, final ArrayList<EntryInfo> entryList, final String groupUUID,
            final BulkUploadDraftSubmitEvent.BulkUploadDraftSubmitEventHandler handler) {

        new IceAsyncCallback<BulkUploadInfo>() {

            @Override
            protected void callService(AsyncCallback<BulkUploadInfo> callback) throws AuthenticationException {
                service.updateBulkImportDraft(AppController.sessionId, id, entryList, groupUUID, callback);
            }

            @Override
            public void onSuccess(BulkUploadInfo result) {
                handler.onSubmit(new BulkUploadDraftSubmitEvent(result));
            }
        }.go(eventBus);
    }

    public void submitBulkImport(final EntryAddType type, final ArrayList<EntryInfo> data, final String groupUUID,
            final BulkUploadSubmitEventHandler handler) {

        new IceAsyncCallback<Boolean>() {

            @Override
            protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                service.submitBulkImport(AppController.sessionId, type, data, groupUUID, callback);
            }

            @Override
            public void onSuccess(Boolean result) {
                handler.onSubmit(new BulkUploadSubmitEvent(result));
            }
        }.go(eventBus);
    }

    public void submitBulkImportDraft(final long bulkImport, final ArrayList<EntryInfo> data,
            final String groupUUID, final BulkUploadSubmitEventHandler handler) {

        new IceAsyncCallback<Boolean>() {

            @Override
            protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                service.submitBulkImportDraft(AppController.sessionId, bulkImport, data, groupUUID, callback);
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
            protected void callService(AsyncCallback<BulkUploadInfo> callback) throws AuthenticationException {
                service.retrieveBulkImport(AppController.sessionId, id, callback);
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
            protected void callService(AsyncCallback<ArrayList<BulkUploadInfo>> callback)
                    throws AuthenticationException {
                service.retrieveDraftsPendingVerification(AppController.sessionId, callback);
            }

            @Override
            public void onSuccess(ArrayList<BulkUploadInfo> result) {
                handler.onDataRetrieval(new SavedDraftsEvent(result));
            }
        }.go(eventBus);
    }

    public void retrieveStorageSchemes(final EntryAddType type, final NewBulkInput bulkInput,
            final SampleStorage sampleStorage) {

        bulkInput.setSampleLocation(null);  // initialize
        if (type == null) {
            return;
        }

        final EntryType entryType = EntryAddType.addTypeToType(type);
        SampleLocation cacheLocation = locationCache.get(entryType);
        if (cacheLocation != null) {
            bulkInput.setSampleLocation(cacheLocation);
            if (sampleStorage != null && sampleStorage.getSample() != null) {
                String locationId = sampleStorage.getSample().getLocationId();
                bulkInput.getSheet().selectSample(type, locationId);
            }
            return;
        }

        service.retrieveStorageSchemes(
                AppController.sessionId,
                entryType,
                new AsyncCallback<HashMap<SampleInfo, ArrayList<String>>>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        eventBus.fireEvent(new FeedbackEvent(true, "Failed to retrieve the sample location data."));
                    }

                    @Override
                    public void onSuccess(HashMap<SampleInfo, ArrayList<String>> result) {
                        if (result == null)
                            return;

                        SampleLocation sampleLocation = new SampleLocation(result);
                        locationCache.put(entryType, sampleLocation);
                        bulkInput.setSampleLocation(sampleLocation);
                        if (sampleStorage != null && sampleStorage.getSample() != null) {
                            String locationId = sampleStorage.getSample().getLocationId();
                            bulkInput.getSheet().selectSample(type, locationId);
                        }
                    }
                });
    }
}
