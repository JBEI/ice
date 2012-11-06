package org.jbei.ice.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class EvaluateQueryEvent extends GwtEvent<EvaluateQueryEventHandler> {

    public static Type<EvaluateQueryEventHandler> TYPE = new Type<EvaluateQueryEventHandler>();

    @Override
    public Type<EvaluateQueryEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(EvaluateQueryEventHandler handler) {
        handler.onEvaluateQuery(this);
    }
}
