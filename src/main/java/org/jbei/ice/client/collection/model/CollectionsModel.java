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
import org.jbei.ice.client.event.FeedbackEvent;
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

    public RegistryServiceAsync getService() {
        return this.service;
    }

    public HandlerManager getEventBus() {
        return this.eventBus;
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

    public void retrieveEntriesForFolder(long id, final EntryIdsEventHandler handler) {

        if (id == 0)
            retrieveEntriesForCurrentUser(handler);
        else if (id == -1)
            retrieveAllEntries(handler);
        else {
            service.retrieveEntriesForFolder(AppController.sessionId, id,
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

    public void retrieveAllEntries(final EntryIdsEventHandler handler) {
        service.retrieveAllEntryIDs(AppController.sessionId, new AsyncCallback<ArrayList<Long>>() {

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

    public void retrieveEntriesForCurrentUser(final EntryIdsEventHandler handler) {
        service.retrieveUserEntries(AppController.sessionId, AppController.accountInfo.getEmail(),
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

    public void moveEntriesToFolder(long source, ArrayList<Long> destinationFolderIds,
            ArrayList<Long> ids, final FolderRetrieveEventHandler handler) {
        service.moveToUserCollection(AppController.sessionId, source, destinationFolderIds, ids,
            new AsyncCallback<ArrayList<FolderDetails>>() {

                @Override
                public void onFailure(Throwable caught) {
                    FeedbackEvent event = new FeedbackEvent(true, "Error moving folders");
                    eventBus.fireEvent(event);
                }

                @Override
                public void onSuccess(ArrayList<FolderDetails> results) {
                    handler.onMenuRetrieval(new FolderRetrieveEvent(results));
                }
            });
    }

    public void removeEntriesFromFolder(long source, ArrayList<Long> ids,
            final FolderRetrieveEventHandler handler) {
        service.removeFromUserCollection(AppController.sessionId, source, ids,
            new AsyncCallback<FolderDetails>() {

                @Override
                public void onFailure(Throwable caught) {
                    FeedbackEvent event = new FeedbackEvent(true,
                            "Error removing items from folder");
                    eventBus.fireEvent(event);
                }

                @Override
                public void onSuccess(FolderDetails results) {
                    handler.onMenuRetrieval(new FolderRetrieveEvent(results));
                }
            });
    }
}
