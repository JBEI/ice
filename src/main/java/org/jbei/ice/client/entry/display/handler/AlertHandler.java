package org.jbei.ice.client.entry.display.handler;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.common.entry.IHasPartData;
import org.jbei.ice.client.entry.display.view.IEntryView;
import org.jbei.ice.client.event.FeedbackEvent;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.client.service.RegistryServiceAsync;
import org.jbei.ice.lib.shared.dto.comment.UserComment;
import org.jbei.ice.lib.shared.dto.entry.PartData;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Delegate for handling alerts about entries
 *
 * @author Hector Plahar
 */
public class AlertHandler implements Delegate<String> {

    private final IHasPartData<PartData> hasPartData;
    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final IEntryView display;

    public AlertHandler(RegistryServiceAsync service, HandlerManager eventBus, IEntryView display,
            IHasPartData<PartData> hasPartData) {
        this.hasPartData = hasPartData;
        this.service = service;
        this.eventBus = eventBus;
        this.display = display;
    }

    @Override
    public void execute(final String message) {
        if (message == null || message.trim().isEmpty()) {
            eventBus.fireEvent(new FeedbackEvent(true, "Please enter message"));
            return;
        }

        new IceAsyncCallback<UserComment>() {

            @Override
            protected void callService(AsyncCallback<UserComment> callback) throws AuthenticationException {
                long entryId = hasPartData.getPart().getId();
                service.alertToEntryProblem(ClientController.sessionId, entryId, message, callback);
            }

            @Override
            public void onSuccess(UserComment result) {
                String msg;
                if (result != null) {
                    msg = "Notification sent successfully";
                    display.addComment(result);
                } else
                    msg = "Your notification could not be sent!";
                eventBus.fireEvent(new FeedbackEvent(result == null, msg));
            }
        }.go(eventBus);
    }
}
