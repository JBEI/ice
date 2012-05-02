package org.jbei.ice.client.collection.presenter;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.collection.menu.CollectionMenu;
import org.jbei.ice.client.collection.menu.IDeleteMenuHandler;
import org.jbei.ice.client.collection.menu.MenuHiderTimer;
import org.jbei.ice.client.collection.menu.MenuItem;
import org.jbei.ice.shared.FolderDetails;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeleteItemHandler implements IDeleteMenuHandler {

    private final HashMap<Long, ArrayList<Long>> folder;
    private final RegistryServiceAsync service;

    public DeleteItemHandler(RegistryServiceAsync service) {
        folder = new HashMap<Long, ArrayList<Long>>();
        this.service = service;
    }

    @Override
    public boolean delete(long id) {
        service.deleteFolder(AppController.sessionId, id, new AsyncCallback<FolderDetails>() {

            @Override
            public void onSuccess(FolderDetails result) {
                folder.put(result.getId(), result.getContents());
            }

            @Override
            public void onFailure(Throwable caught) {
                // TODO Auto-generated method stub
            }
        });

        return true;
    }

    @Override
    public ClickHandler getUndoHandler(final MenuItem item, final CollectionMenu menu,
            final MenuHiderTimer timer) {
        return new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (!folder.containsKey(item.getId()))
                    return;
                service.createUserCollection(AppController.sessionId, item.getName(), "",
                    folder.get(item.getId()), new AsyncCallback<FolderDetails>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            // TODO Auto-generated method stub
                        }

                        @Override
                        public void onSuccess(FolderDetails result) {
                            timer.cancel();
                            MenuItem newItem = new MenuItem(result.getId(), result.getName(),
                                    result.getCount(), result.isSystemFolder());
                            menu.updateMenuItem(item.getId(), newItem, DeleteItemHandler.this);
                        }
                    });
            }
        };
    }
}
