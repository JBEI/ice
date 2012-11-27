package org.jbei.ice.client.collection.model;

import java.util.ArrayList;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.collection.event.FolderEvent;
import org.jbei.ice.client.collection.event.FolderEventHandler;
import org.jbei.ice.client.collection.event.FolderRetrieveEvent;
import org.jbei.ice.client.collection.event.FolderRetrieveEventHandler;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.FolderDetails;

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

    public void retrieveFolders(final FolderRetrieveEventHandler handler) {
        service.retrieveCollections(AppController.sessionId,
                                    new AsyncCallback<ArrayList<FolderDetails>>() {

                                        @Override
                                        public void onSuccess(ArrayList<FolderDetails> result) {
                                            handler.onFolderRetrieve(new FolderRetrieveEvent(result));
                                        }

                                        @Override
                                        public void onFailure(Throwable caught) {
                                            handler.onFolderRetrieve(null);
                                        }
                                    });
    }

    public void updateFolder(final long id, final FolderDetails newFolder, final FolderEventHandler handler) {
        new IceAsyncCallback<FolderDetails>() {

            @Override
            protected void callService(AsyncCallback<FolderDetails> folderDetailsAsyncCallback)
                    throws AuthenticationException {
                try {
                    service.updateFolder(AppController.sessionId, id, newFolder, folderDetailsAsyncCallback);
                } catch (AuthenticationException e) {
                    History.newItem(Page.LOGIN.getLink());
                }
            }

            @Override
            public void onSuccess(FolderDetails result) {
                handler.onFolderEvent(new FolderEvent(result));
            }
        }.go(eventBus);
    }

    public void createFolder(final String collectionName, final FolderEventHandler handler) {

        new IceAsyncCallback<FolderDetails>() {

            @Override
            protected void callService(AsyncCallback<FolderDetails> folderDetailsAsyncCallback)
                    throws AuthenticationException {
                try {
                    service.createUserCollection(AppController.sessionId, collectionName, "", null,
                                                 folderDetailsAsyncCallback);
                } catch (AuthenticationException e) {
                    History.newItem(Page.LOGIN.getLink());
                }
            }

            @Override
            public void onSuccess(FolderDetails result) {
                handler.onFolderEvent(new FolderEvent(result));
            }
        }.go(eventBus);
    }

    public void retrieveEntriesForFolder(long id, final FolderRetrieveEventHandler handler, int start, int limit) {
        if (id == 0)
            retrieveEntriesForCurrentUser(handler, start, limit);
        else if (id == -1)
            retrieveAllVisibleEntries(handler, start, limit);
        else {
//            service.retrieveEntriesForFolder(AppController.sessionId, id,
//                                             new AsyncCallback<FolderDetails>() {
//
//                                                 @Override
//                                                 public void onSuccess(FolderDetails result) {
//                                                     handler.onFolderRetrieve(new FolderRetrieveEvent(result));
//                                                 }
//
//                                                 @Override
//                                                 public void onFailure(Throwable caught) {
//                                                     handler.onFolderRetrieve(null);
//                                                 }
//                                             });
        }
    }

    public void retrieveAllVisibleEntries(final FolderRetrieveEventHandler handler, int start, int limit) {
        FolderDetails details = new FolderDetails(-1, "Available Entries", true);
        service.retrieveAllVisibleEntryIDs(AppController.sessionId, details, ColumnField.CREATED, false, start, limit,
                                           new AsyncCallback<FolderDetails>() {

                                               @Override
                                               public void onSuccess(FolderDetails result) {
                                                   handler.onFolderRetrieve(new FolderRetrieveEvent(result));
                                               }

                                               @Override
                                               public void onFailure(Throwable caught) {
                                                   handler.onFolderRetrieve(null);
                                               }
                                           });
    }

    public void retrieveEntriesForCurrentUser(final FolderRetrieveEventHandler handler, int start, int limit) {
        service.retrieveUserEntries(AppController.sessionId, AppController.accountInfo.getId() + "",
                                    ColumnField.CREATED, false, start, limit,
                                    new AsyncCallback<FolderDetails>() {

                                        @Override
                                        public void onSuccess(FolderDetails result) {
                                            handler.onFolderRetrieve(new FolderRetrieveEvent(result));
                                        }

                                        @Override
                                        public void onFailure(Throwable caught) {
                                            handler.onFolderRetrieve(null);
                                        }
                                    });
    }

    public void addEntriesToFolder(final ArrayList<Long> destinationFolderIds, final ArrayList<Long> ids,
            final FolderRetrieveEventHandler handler) {

        new IceAsyncCallback<ArrayList<FolderDetails>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<FolderDetails>> folderDetailsAsyncCallback)
                    throws AuthenticationException {
                try {
                    service.addEntriesToCollection(AppController.sessionId, destinationFolderIds, ids,
                                                   folderDetailsAsyncCallback);
                } catch (AuthenticationException e) {
                    History.newItem(Page.LOGIN.getLink());
                }
            }

            @Override
            public void onSuccess(ArrayList<FolderDetails> result) {
                handler.onFolderRetrieve(new FolderRetrieveEvent(result));
            }
        }.go(eventBus);
    }

    public void moveEntriesToFolder(final long source, final ArrayList<Long> destinationFolderIds,
            final ArrayList<Long> ids, final FolderRetrieveEventHandler handler) {
        new IceAsyncCallback<ArrayList<FolderDetails>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<FolderDetails>> callback)
                    throws AuthenticationException {
                try {
                    service.moveToUserCollection(AppController.sessionId, source, destinationFolderIds, ids, callback);
                } catch (AuthenticationException e) {
                    History.newItem(Page.LOGIN.getLink());
                }
            }

            @Override
            public void onSuccess(ArrayList<FolderDetails> result) {
                handler.onFolderRetrieve(new FolderRetrieveEvent(result));
            }
        }.go(eventBus);
    }

    public void removeEntriesFromFolder(final long source, final ArrayList<Long> ids,
            final FolderRetrieveEventHandler handler) {

        new IceAsyncCallback<FolderDetails>() {

            @Override
            protected void callService(AsyncCallback<FolderDetails> callback) throws AuthenticationException {
                try {
                    service.removeFromUserCollection(AppController.sessionId, source, ids, callback);
                } catch (AuthenticationException e) {
                    History.newItem(Page.LOGIN.getLink());
                }
            }

            @Override
            public void onSuccess(FolderDetails result) {
                handler.onFolderRetrieve(new FolderRetrieveEvent(result));
            }
        }.go(eventBus);
    }
}
