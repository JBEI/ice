package org.jbei.ice.client.collection.event;

import com.google.gwt.event.shared.GwtEvent;

public class SubmitEvent extends GwtEvent<SubmitHandler> {

    /**
     * The event type.
     */
    private static Type<SubmitHandler> TYPE = new Type<SubmitHandler>();

    /**
     * Handler hook.
     * 
     * @return the handler hook
     */
    public static Type<SubmitHandler> getType() {
        return TYPE;
    }

    @Override
    public final Type<SubmitHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SubmitHandler handler) {
        handler.onSubmit(this);
    }
}
