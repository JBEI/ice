package org.jbei.ice.client.collection.presenter;

import java.util.ArrayList;

import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.shared.FolderDetails;

public abstract class MoveToFolderHandler extends SubmitHandler {

    public MoveToFolderHandler(RegistryServiceAsync service) {
        super(service);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected ArrayList<FolderDetails> getSource() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected ArrayList<FolderDetails> getDestination() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected ArrayList<Long> getEntryIds() {
        // TODO Auto-generated method stub
        return null;
    }

}
