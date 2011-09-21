package org.jbei.ice.client.common;

import org.jbei.ice.client.ILogoutHandler;
import org.jbei.ice.client.Presenter;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.HasWidgets;

public class HeaderPresenter implements Presenter, ILogoutHandler {

    private final HeaderView view;

    public HeaderPresenter() {
        this.view = new HeaderView();
    }

    @Override
    public HasClickHandlers getClickHandler() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void go(HasWidgets container) {

    }

}
