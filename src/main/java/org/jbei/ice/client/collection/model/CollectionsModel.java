package org.jbei.ice.client.collection.model;

import java.util.ArrayList;

import org.jbei.ice.client.Callback;
import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.collection.view.OptionSelect;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.lib.shared.dto.folder.FolderDetails;
import org.jbei.ice.lib.shared.dto.web.WebOfRegistries;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class CollectionsModel {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;

    public CollectionsModel(RegistryServiceAsync service, HandlerManager eventBus) {
        this.service = service;
        this.eventBus = eventBus;
    }

    public RegistryServiceAsync getService() {
        return this.service;
    }

    public HandlerManager getEventBus() {
        return this.eventBus;
    }

    public void retrieveFolders(final Callback<ArrayList<FolderDetails>> handler) {
        new IceAsyncCallback<ArrayList<FolderDetails>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<FolderDetails>> callback)
                    throws AuthenticationException {
                service.retrieveCollections(ClientController.sessionId, callback);
            }

            @Override
            public void onSuccess(ArrayList<FolderDetails> result) {
                handler.onSuccess(result);
            }
        }.go(eventBus);
    }

    public void updateFolder(final long id, final FolderDetails newFolder, final Callback<FolderDetails> callback) {
        new IceAsyncCallback<FolderDetails>() {

            @Override
            protected void callService(AsyncCallback<FolderDetails> folderDetailsAsyncCallback)
                    throws AuthenticationException {
                service.updateFolder(ClientController.sessionId, id, newFolder, folderDetailsAsyncCallback);
            }

            @Override
            public void onSuccess(FolderDetails result) {
                callback.onSuccess(result);
            }
        }.go(eventBus);
    }

    public void createFolder(final String collectionName, final Callback<FolderDetails> callback) {

        new IceAsyncCallback<FolderDetails>() {

            @Override
            protected void callService(AsyncCallback<FolderDetails> folderDetailsAsyncCallback)
                    throws AuthenticationException {
                service.createUserCollection(ClientController.sessionId, collectionName, "", null,
                                             folderDetailsAsyncCallback);
            }

            @Override
            public void onSuccess(FolderDetails result) {
                callback.onSuccess(result);
            }

            @Override
            public void serverFailure() {
                callback.onFailure();
            }
        }.go(eventBus);
    }

    public void retrieveEntriesForFolder(long id, final Callback<FolderDetails> callback, int start, int limit) {
        if (id == 0)
            retrieveEntriesForCurrentUser(callback, start, limit);
        else if (id == -1)
            retrieveAllVisibleEntries(callback, start, limit);
        else {
            service.retrieveEntriesForFolder(
                    ClientController.sessionId, id, ColumnField.CREATED, false, start, limit,
                    new AsyncCallback<FolderDetails>() {

                        @Override
                        public void onSuccess(FolderDetails result) {
                            callback.onSuccess(result);
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            callback.onFailure();
                        }
                    });
        }
    }

    public void retrieveAllVisibleEntries(final Callback<FolderDetails> callback, final int start, final int limit) {
        new IceAsyncCallback<FolderDetails>() {

            @Override
            protected void callService(AsyncCallback<FolderDetails> callback) throws AuthenticationException {
                FolderDetails details = new FolderDetails(-1, "Available Entries");
                service.retrieveAllVisibleEntrys(ClientController.sessionId, details, ColumnField.CREATED, false, start,
                                                 limit, callback);
            }

            @Override
            public void onSuccess(FolderDetails result) {
                callback.onSuccess(result);
            }

            @Override
            public void serverFailure() {
                callback.onFailure();
            }
        }.go(eventBus);
    }

    public void retrieveEntriesForCurrentUser(final Callback<FolderDetails> callback, final int start,
            final int limit) {
        new IceAsyncCallback<FolderDetails>() {

            @Override
            protected void callService(AsyncCallback<FolderDetails> callback) throws AuthenticationException {
                String id = Long.toString(ClientController.account.getId());
                service.retrieveUserEntries(ClientController.sessionId, id, ColumnField.CREATED, false, start, limit,
                                            callback);
            }

            @Override
            public void onSuccess(FolderDetails result) {
                callback.onSuccess(result);
            }

            @Override
            public void serverFailure() {
                callback.onFailure();
            }
        }.go(eventBus);
    }

    public void addEntriesToFolder(final ArrayList<Long> destinationFolderIds, final ArrayList<Long> ids,
            final Callback<ArrayList<FolderDetails>> resultCallback) {
        new IceAsyncCallback<ArrayList<FolderDetails>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<FolderDetails>> callback)
                    throws AuthenticationException {
                try {
                    service.addEntriesToCollection(ClientController.sessionId, destinationFolderIds, ids, callback);
                } catch (AuthenticationException e) {
                    ClientController.sessionId = null;
                    History.newItem(Page.LOGIN.getLink());
                }
            }

            @Override
            public void onSuccess(ArrayList<FolderDetails> result) {
                resultCallback.onSuccess(result);
            }

            @Override
            public void serverFailure() {
                resultCallback.onFailure();
            }
        }.go(eventBus);
    }

    public void moveEntriesToFolder(final long source, final ArrayList<Long> destinationFolderIds,
            final ArrayList<Long> ids, final Callback<ArrayList<FolderDetails>> callback) {
        new IceAsyncCallback<ArrayList<FolderDetails>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<FolderDetails>> callback)
                    throws AuthenticationException {
                service.moveToUserCollection(ClientController.sessionId, source, destinationFolderIds, ids, callback);
            }

            @Override
            public void onSuccess(ArrayList<FolderDetails> result) {
                callback.onSuccess(result);
            }
        }.go(eventBus);
    }

    public void removeEntriesFromFolder(final long source, final ArrayList<Long> ids,
            final Callback<FolderDetails> callback) {
        new IceAsyncCallback<FolderDetails>() {

            @Override
            protected void callService(AsyncCallback<FolderDetails> callback) throws AuthenticationException {
                service.removeFromUserCollection(ClientController.sessionId, source, ids, callback);
            }

            @Override
            public void onSuccess(FolderDetails result) {
                callback.onSuccess(result);
            }
        }.go(eventBus);
    }

    public void retrieveWebOfRegistryPartners(final Callback<WebOfRegistries> callback) {
        new IceAsyncCallback<WebOfRegistries>() {

            @Override
            protected void callService(AsyncCallback<WebOfRegistries> callback) throws AuthenticationException {
                service.retrieveWebOfRegistryPartners(ClientController.sessionId, callback);
            }

            @Override
            public void onSuccess(WebOfRegistries result) {
                callback.onSuccess(result);
            }
        }.go(eventBus);
    }

    public void requestTransfer(final ArrayList<Long> ids, ArrayList<OptionSelect> selectedTransfer) {
        if (selectedTransfer == null || ids == null || ids.isEmpty() || selectedTransfer.isEmpty())
            return;

        final ArrayList<String> sites = new ArrayList<String>();
        for (OptionSelect select : selectedTransfer) {
            sites.add(select.getName());
        }

        new IceAsyncCallback<Void>() {

            @Override
            protected void callService(AsyncCallback<Void> callback) throws AuthenticationException {
                service.requestEntryTransfer(ClientController.sessionId, ids, sites, callback);
            }

            @Override
            public void onSuccess(Void result) {
            }

            @Override
            public void onNullResult() {
            }
        }.go(eventBus);
    }
}
