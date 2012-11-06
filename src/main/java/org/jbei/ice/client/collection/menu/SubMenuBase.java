package org.jbei.ice.client.collection.menu;

import org.jbei.ice.client.collection.event.SubmitEvent;
import org.jbei.ice.client.collection.event.SubmitHandler;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;

/**
 * Base class for sub menus (i.e. menus at the top of views like "Add To")
 * which perform some action on submit (usually a click)
 *
 * @author Hector Plahar
 */
public abstract class SubMenuBase extends Composite implements HasSubmitHandlers {

    /**
     * Adds a {@link SubmitEvent} handler.
     *
     * @param handler the handler
     * @return the handler registration used to remove the handler
     */
    @Override
    public HandlerRegistration addSubmitHandler(SubmitHandler handler) {
        return addHandler(handler, SubmitEvent.getType());
    }

    protected void dispatchSubmitEvent() {
        fireEvent(new GwtEvent<SubmitHandler>() {

            @Override
            public Type<SubmitHandler> getAssociatedType() {
                return SubmitEvent.getType();
            }

            @Override
            protected void dispatch(SubmitHandler handler) {
                handler.onSubmit(new SubmitEvent());
            }
        });
    }
}
