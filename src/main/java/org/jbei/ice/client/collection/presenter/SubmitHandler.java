package org.jbei.ice.client.collection.presenter;

import java.util.ArrayList;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.shared.FolderDetails;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

// TODO : currently this just supports "Move To". Sub-class to support both
public abstract class SubmitHandler implements ClickHandler {

    protected final RegistryServiceAsync service;

    public SubmitHandler(RegistryServiceAsync service) {
        this.service = service;
    }

    @Override
    public void onClick(ClickEvent event) {
        ArrayList<Long> sourceFolderIds = new ArrayList<Long>();
        if (getSource() != null) {
            for (FolderDetails folderDetail : getSource()) {
                sourceFolderIds.add(folderDetail.getId());
            }
        }

        ArrayList<Long> destinationFolderIds = new ArrayList<Long>();
        if (getDestination() != null) {
            for (FolderDetails detail : getDestination()) {
                destinationFolderIds.add(detail.getId());
            }
        }

        ArrayList<Long> entryIds = getEntryIds();

        service.moveToUserCollection(AppController.sessionId, sourceFolderIds,
            destinationFolderIds, entryIds, new AsyncCallback<Boolean>() {

                @Override
                public void onSuccess(Boolean result) {
                    if (result)
                        Window.alert("Add to successful");
                    else
                        Window.alert("Add To did not work");
                }

                @Override
                public void onFailure(Throwable caught) {
                    Window.alert("Error making call: " + caught.getMessage());
                }
            });
    }

    /**
     * @return List of folders that the entries are originating from. When performing
     *         a move, the entries are removed from this folder
     */
    protected abstract ArrayList<FolderDetails> getSource();

    /**
     * @return List of folders that the entries are to be added to
     */
    protected abstract ArrayList<FolderDetails> getDestination();

    /**
     * @return List of entry identifiers that are to be moved or added
     */
    protected abstract ArrayList<Long> getEntryIds();
}
