package org.jbei.ice.client.collection.event;

import org.jbei.ice.shared.FolderDetails;

import com.google.gwt.event.shared.GwtEvent;

public class FolderEvent extends GwtEvent<FolderEventHandler> {

    public static Type<FolderEventHandler> TYPE = new Type<FolderEventHandler>();
    private final FolderDetails folder;

    public FolderEvent(FolderDetails folder) {
        this.folder = folder;
    }

    @Override
    public GwtEvent.Type<FolderEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(FolderEventHandler handler) {
        handler.onFolderEvent(this);
    }

    public FolderDetails getFolder() {
        return folder;
    }
}
