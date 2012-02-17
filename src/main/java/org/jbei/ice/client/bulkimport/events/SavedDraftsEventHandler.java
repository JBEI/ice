package org.jbei.ice.client.bulkimport.events;

import com.google.gwt.event.shared.EventHandler;

public interface SavedDraftsEventHandler extends EventHandler {
    void onDataRetrieval(SavedDraftsEvent event);
}
