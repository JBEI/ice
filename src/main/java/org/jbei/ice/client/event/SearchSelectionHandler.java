package org.jbei.ice.client.event;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.EventHandler;

public interface SearchSelectionHandler extends EventHandler, ChangeHandler {

    void onChange(SearchEvent event);
}
