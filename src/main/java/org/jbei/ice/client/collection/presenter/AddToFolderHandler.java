package org.jbei.ice.client.collection.presenter;

import java.util.ArrayList;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.shared.FolderDetails;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Submit handler that is only concerned with destination folder
 * and the list of entries
 * 
 * @author Hector Plahar
 * 
 */
public abstract class AddToFolderHandler extends SubmitHandler {

    public AddToFolderHandler(RegistryServiceAsync service) {
        super(service);
    }

    @Override
    public void onClick(ClickEvent event) {

        // TODO : might be slower to iterate and get the ids, as opposed to 
        // TODO : sending the list of folder details across the wire
        ArrayList<Long> destinationFolderIds = new ArrayList<Long>();
        if (getDestination() != null) {
            for (FolderDetails detail : getDestination()) {
                destinationFolderIds.add(detail.getId());
            }
        }

        ArrayList<Long> entryIds = getEntryIds();

        service.addEntriesToCollection(AppController.sessionId, destinationFolderIds, entryIds,
            new AsyncCallback<Boolean>() {

                @Override
                public void onSuccess(Boolean result) {
                    if (result)
                        onAddSuccess();
                    else
                        Window.alert("There was an error adding");
                }

                @Override
                public void onFailure(Throwable caught) {
                    Window.alert("Error making call: " + caught.getMessage());
                }
            });
    }

    public abstract void onAddSuccess();

    @Override
    protected ArrayList<FolderDetails> getSource() {
        return null;
    }
}
