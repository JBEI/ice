package org.jbei.ice.client.collection.presenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jbei.ice.client.*;
import org.jbei.ice.client.collection.ICollectionView;
import org.jbei.ice.client.collection.menu.CollectionMenu;
import org.jbei.ice.client.collection.menu.IDeleteMenuHandler;
import org.jbei.ice.client.collection.menu.MenuHiderTimer;
import org.jbei.ice.client.collection.menu.MenuItem;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.shared.dto.folder.FolderDetails;

import java.util.ArrayList;
import java.util.HashMap;

public class DeleteItemHandler implements IDeleteMenuHandler {

    private final HashMap<Long, ArrayList<Long>> folder;
    private final RegistryServiceAsync service;
    private final ICollectionView view;
    private final HandlerManager eventBus;

    public DeleteItemHandler(RegistryServiceAsync service, HandlerManager eventBus, ICollectionView view) {
        this.eventBus = eventBus;
        folder = new HashMap<Long, ArrayList<Long>>();
        this.service = service;
        this.view = view;
    }

    @Override
    public void delete(long id, final Callback<MenuItem> deleteCallback) {
        service.deleteFolder(ClientController.sessionId, id, new AsyncCallback<FolderDetails>() {

            @Override
            public void onSuccess(FolderDetails result) {
                if (result == null) {
                    view.showFeedbackMessage("Error deleting folder.", true);
                    deleteCallback.onFailure();
                    return;
                }

//                folder.put(result.getId(), result.getContents());
                MenuItem item = new MenuItem(result.getId(), result.getName(), result.getCount());
                deleteCallback.onSuccess(item);
                view.removeSubMenuFolder(item);
            }

            @Override
            public void onFailure(Throwable caught) {
                view.showFeedbackMessage("Error deleting folder.", true);
                deleteCallback.onFailure();
            }
        });
    }

    /**
     * Handler for undoing a delete action
     */
    @Override
    public ClickHandler getUndoHandler(final MenuItem item, final CollectionMenu menu,
                                       final MenuHiderTimer timer) {
        return new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (!folder.containsKey(item.getId()))
                    return;

                new IceAsyncCallback<FolderDetails>() {

                    @Override
                    protected void callService(AsyncCallback<FolderDetails> callback) throws AuthenticationException {
                        try {
                            service.createUserCollection(ClientController.sessionId, item.getName(), "",
                                    folder.get(item.getId()), callback);
                        } catch (AuthenticationException e) {
                            History.newItem(Page.LOGIN.getLink());
                        }
                    }

                    @Override
                    public void onSuccess(FolderDetails result) {
                        timer.cancel();
                        MenuItem newItem = new MenuItem(result.getId(), result.getName(), result.getCount());
                        menu.updateMenuItem(item.getId(), newItem, DeleteItemHandler.this);
                        view.addSubMenuFolder(newItem);
                    }
                }.go(eventBus);
            }
        };
    }
}
