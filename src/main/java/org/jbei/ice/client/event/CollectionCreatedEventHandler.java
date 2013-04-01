package org.jbei.ice.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface CollectionCreatedEventHandler extends EventHandler {
    void onCollectionCreation(CollectionCreatedEvent event);
}
