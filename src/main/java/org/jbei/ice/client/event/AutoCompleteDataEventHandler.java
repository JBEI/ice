package org.jbei.ice.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface AutoCompleteDataEventHandler extends EventHandler {

    void onDataRetrieval(AutoCompleteDataEvent event);
}
