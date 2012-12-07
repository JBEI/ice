package org.jbei.ice.client.event;

import org.jbei.ice.shared.dto.folder.FolderDetails;

import com.google.gwt.event.shared.GwtEvent;

public class CollectionCreatedEvent extends GwtEvent<CollectionCreatedEventHandler> {

    public static Type<CollectionCreatedEventHandler> TYPE = new Type<CollectionCreatedEventHandler>();
    private final FolderDetails folderDetails;

    public CollectionCreatedEvent(FolderDetails details) {
        this.folderDetails = details;
    }

    @Override
    public GwtEvent.Type<CollectionCreatedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CollectionCreatedEventHandler handler) {
        handler.onCollectionCreation(this);
    }

    public FolderDetails getFolderDetails() {
        return this.folderDetails;
    }
}
