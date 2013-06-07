package org.jbei.ice.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.event.FeedbackEvent.IFeedbackEventHandler;

public class FeedbackEvent extends GwtEvent<IFeedbackEventHandler> {

    public interface IFeedbackEventHandler extends EventHandler {
        void onFeedbackAvailable(FeedbackEvent event);
    }

    private final boolean error;
    private final String msg;
    public static final Type<IFeedbackEventHandler> TYPE = new Type<IFeedbackEventHandler>();

    public FeedbackEvent(boolean error, String msg) {
        this.error = error;
        this.msg = msg;
    }

    public boolean isError() {
        return this.error;
    }

    public String getMessage() {
        if (error)
            return "<i class=\"" + FAIconType.WARNING_SIGN.getStyleName() + "\"></i> " + msg;
        return "<i class=\"" + FAIconType.OK_SIGN.getStyleName() + "\"></i> " + this.msg;
    }

    @Override
    public Type<IFeedbackEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(IFeedbackEventHandler handler) {
        handler.onFeedbackAvailable(this);
    }
}
