package org.jbei.ice.client.collection.menu;

import org.jbei.ice.client.event.EntryViewEvent.EntryViewEventHandler;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface IHasEntryHandlers extends HasHandlers {
    HandlerRegistration addEntryHandler(EntryViewEventHandler handler); // TODO : more generic entry handler ?
}
