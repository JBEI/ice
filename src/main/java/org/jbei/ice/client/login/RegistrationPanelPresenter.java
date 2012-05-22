package org.jbei.ice.client.login;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;

public class RegistrationPanelPresenter {

    public interface IRegistrationPanelView {

        RegistrationDetails getDetails();

        HandlerRegistration addSubmitHandler(ClickHandler handler);

        HandlerRegistration addCancelHandler(ClickHandler handler);

        boolean validates();
    }

    private HandlerRegistration registration;
    private HandlerRegistration cancelHandler;
    private final IRegistrationPanelView view;

    public RegistrationPanelPresenter(IRegistrationPanelView view) {
        this.view = view;
    }

    public void addSubmitHandler(final ClickHandler handler) {
        if (registration != null) {
            registration.removeHandler();
        }
        registration = view.addSubmitHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (view.validates())
                    handler.onClick(event);
            }
        });
    }

    public void addCancelHandler(ClickHandler handler) {
        if (cancelHandler != null)
            cancelHandler.removeHandler();

        cancelHandler = view.addCancelHandler(handler);
    }
}
