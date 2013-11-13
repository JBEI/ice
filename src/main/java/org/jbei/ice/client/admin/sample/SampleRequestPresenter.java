package org.jbei.ice.client.admin.sample;

import org.jbei.ice.client.admin.AdminPanelPresenter;
import org.jbei.ice.client.admin.IAdminPanel;
import org.jbei.ice.client.service.RegistryServiceAsync;

import com.google.gwt.event.shared.HandlerManager;

/**
 * Presenter for sample requests
 *
 * @author Hector Plahar
 */
public class SampleRequestPresenter extends AdminPanelPresenter {

    private final SampleRequestPanel panel;

    public SampleRequestPresenter(final RegistryServiceAsync service, HandlerManager eventBus) {
        super(service, eventBus);
        panel = new SampleRequestPanel();
    }

    @Override
    public IAdminPanel getView() {
        return panel;
    }
}
