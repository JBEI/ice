package org.jbei.ice.client.collection.model;

import java.util.ArrayList;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.collection.event.EntryIdsEvent;
import org.jbei.ice.client.collection.event.EntryIdsEventHandler;
import org.jbei.ice.client.collection.event.FolderEvent;
import org.jbei.ice.client.collection.event.FolderEventHandler;
import org.jbei.ice.client.collection.event.FolderRetrieveEvent;
import org.jbei.ice.client.collection.event.FolderRetrieveEventHandler;
import org.jbei.ice.shared.FolderDetails;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class CollectionsModel {
    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;

    public CollectionsModel(RegistryServiceAsync service, HandlerManager eventBus) {
        this.service = service;
        this.eventBus = eventBus;
    }

    public void retrieveFolders(final FolderRetrieveEventHandler handler) {
        service.retrieveCollections(AppController.sessionId,
            new AsyncCallback<ArrayList<FolderDetails>>() {

                @Override
                public void onSuccess(ArrayList<FolderDetails> result) {
                    handler.onMenuRetrieval(new FolderRetrieveEvent(result));
                }

                @Override
                public void onFailure(Throwable caught) {
                    handler.onMenuRetrieval(null);
                }
            });
    }

    public void updateFolder(long id, FolderDetails newFolder, final FolderEventHandler handler) {
        service.updateFolder(AppController.sessionId, id, newFolder,
            new AsyncCallback<FolderDetails>() {

                @Override
                public void onSuccess(FolderDetails result) {
                    handler.onFolderEvent(new FolderEvent(result));
                }

                @Override
                public void onFailure(Throwable caught) {
                    handler.onFolderEvent(null);
                }
            });
    }

    public void createFolder(String collectionName, final FolderEventHandler handler) {
        service.createUserCollection(AppController.sessionId, collectionName, "",
            new AsyncCallback<FolderDetails>() {

                @Override
                public void onFailure(Throwable caught) {
                    handler.onFolderEvent(null);
                }

                @Override
                public void onSuccess(FolderDetails result) {
                    handler.onFolderEvent(new FolderEvent(result));
                }
            });
    }

    public void retrieveEntriesForFolder(String id, final EntryIdsEventHandler handler) {

        // TODO : find a different way to distinguish between folder id and user id (isNumeric?)
        try {
            service.retrieveEntriesForFolder(AppController.sessionId, Long.decode(id),
                new AsyncCallback<ArrayList<Long>>() {

                    @Override
                    public void onSuccess(ArrayList<Long> result) {
                        handler.onEntryIdsEvent(new EntryIdsEvent(result));
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        handler.onEntryIdsEvent(null);
                    }
                });

        } catch (NumberFormatException nfe) {
            service.retrieveUserEntries(AppController.sessionId, id,
                new AsyncCallback<ArrayList<Long>>() {

                    @Override
                    public void onSuccess(ArrayList<Long> result) {
                        handler.onEntryIdsEvent(new EntryIdsEvent(result));
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        handler.onEntryIdsEvent(null);
                    }
                });
        }
    }

    public void addEntriesToFolder(ArrayList<Long> destinationFolderIds, ArrayList<Long> ids,
            final FolderRetrieveEventHandler handler) {
        service.addEntriesToCollection(AppController.sessionId, destinationFolderIds, ids,
            new AsyncCallback<ArrayList<FolderDetails>>() {

                @Override
                public void onSuccess(ArrayList<FolderDetails> results) {
                    handler.onMenuRetrieval(new FolderRetrieveEvent(results));
                }

                @Override
                public void onFailure(Throwable caught) {
                    handler.onMenuRetrieval(null);
                }
            });
    }
}
