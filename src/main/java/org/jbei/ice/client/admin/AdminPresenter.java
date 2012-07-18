package org.jbei.ice.client.admin;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.admin.group.GroupPresenter;
import org.jbei.ice.client.admin.usermanagement.UserPresenter;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasWidgets;

/**
 * Presenter for the admin page
 *
 * @author Hector Plahar
 */
public class AdminPresenter extends AbstractPresenter {

    private final AdminView view;
    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;

    private UserPresenter userPresenter;
    private GroupPresenter groupPresenter;

    public AdminPresenter(RegistryServiceAsync service, HandlerManager eventBus, AdminView view) {
        this.service = service;
        this.view = view;
        this.eventBus = eventBus;

        addSelectionChangeHandler();

        // presenters
        userPresenter = new UserPresenter();
        groupPresenter = new GroupPresenter();

        setTabs();
    }

    protected void setTabs() {
        view.setTabWidget(userPresenter.getView().getTab(), userPresenter.getView());
        view.setTabWidget(groupPresenter.getView().getTab(), groupPresenter.getView());
    }

    /**
     * Adds a tab selection change handler to the view
     */
    private void addSelectionChangeHandler() {

        this.view.addLayoutHandler(new SelectionHandler<Integer>() {

            @Override
            public void onSelection(SelectionEvent<Integer> event) {

                if (event.getSelectedItem() == groupPresenter.getTabIndex()) {
                    groupPresenter.go(service, eventBus);
                    return;
                }

                if (event.getSelectedItem() == userPresenter.getTabIndex()) {
                    userPresenter.go(service, eventBus);
                    return;
                }
            }
        });
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.view.asWidget());
    }
}
