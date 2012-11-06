package org.jbei.ice.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class QueryEvaluateCompleteEvent extends GwtEvent<QueryEvaluateCompleteEventHandler> {

    public static Type<QueryEvaluateCompleteEventHandler> TYPE = new Type<QueryEvaluateCompleteEventHandler>();

    @Override
    public Type<QueryEvaluateCompleteEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(QueryEvaluateCompleteEventHandler handler) {
        handler.onQueryEvaluationComplete(this);
    }
}
