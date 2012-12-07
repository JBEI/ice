package org.jbei.ice.client.common.header;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.shared.dto.AccountInfo;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasWidgets;

/**
 * @author Hector Plahar
 */
public class HeaderPresenter extends AbstractPresenter {

    private final HeaderView view;

    public HeaderPresenter(RegistryServiceAsync service, HandlerManager eventBus) {
        super(service, eventBus);
        view = HeaderView.getInstance();
    }

    public void setCurrentUser(AccountInfo info) {
        view.setHeaderData(info);
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.view.asWidget());
    }
}
