package org.jbei.ice.client.event;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.shared.AutoCompleteField;

import com.google.gwt.event.shared.GwtEvent;

public class AutoCompleteDataEvent extends GwtEvent<AutoCompleteDataEventHandler> {

    public static Type<AutoCompleteDataEventHandler> TYPE = new Type<AutoCompleteDataEventHandler>();
    private final HashMap<AutoCompleteField, ArrayList<String>> data;

    public AutoCompleteDataEvent(HashMap<AutoCompleteField, ArrayList<String>> data) {
        this.data = new HashMap<AutoCompleteField, ArrayList<String>>(data);
    }

    @Override
    public GwtEvent.Type<AutoCompleteDataEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(AutoCompleteDataEventHandler handler) {
        handler.onDataRetrieval(this);
    }

    public HashMap<AutoCompleteField, ArrayList<String>> getData() {
        return data;
    }
}
