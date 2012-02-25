package org.jbei.ice.client.collection.menu;

import org.jbei.ice.client.collection.event.SubmitHandler;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface HasSubmitHandlers extends HasHandlers {
    HandlerRegistration addSubmitHandler(SubmitHandler handler);
}
