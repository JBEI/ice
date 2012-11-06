package org.jbei.ice.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface ShowEntryListEventHandler extends EventHandler {
    void onEntryListContextAvailable(ShowEntryListEvent event);
}
