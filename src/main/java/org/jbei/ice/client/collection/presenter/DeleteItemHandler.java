package org.jbei.ice.client.collection.presenter;

import org.jbei.ice.client.Callback;
import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.collection.ICollectionView;
import org.jbei.ice.client.collection.menu.IDeleteMenuHandler;
import org.jbei.ice.client.collection.menu.MenuItem;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.client.service.RegistryServiceAsync;
import org.jbei.ice.lib.shared.dto.folder.FolderDetails;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Handler for deleting a folder using the collection menu
 *
 * @author Hector Plahar
 */
public class DeleteItemHandler implements IDeleteMenuHandler {

    private final RegistryServiceAsync service;
    private final ICollectionView view;
    private final HandlerManager eventBus;

    public DeleteItemHandler(RegistryServiceAsync service, HandlerManager eventBus, ICollectionView view) {
        this.eventBus = eventBus;
        this.service = service;
        this.view = view;
    }

    @Override
    public void delete(final long id, final Callback<MenuItem> deleteCallback) {
        new IceAsyncCallback<FolderDetails>() {

            @Override
            protected void callService(AsyncCallback<FolderDetails> callback) throws AuthenticationException {
                service.deleteFolder(ClientController.sessionId, id, callback);
            }

            @Override
            public void onSuccess(FolderDetails result) {
                if (result == null) {
                    view.showFeedbackMessage("Error deleting folder.", true);
                    deleteCallback.onFailure();
                    return;
                }

                MenuItem item = new MenuItem(result.getId(), result.getName(), result.getCount());
                deleteCallback.onSuccess(item);
                view.removeSubMenuFolder(item);
            }

            @Override
            public void serverFailure() {
                view.showFeedbackMessage("Error deleting folder.", true);
                deleteCallback.onFailure();
            }
        }.go(eventBus);
    }
}
