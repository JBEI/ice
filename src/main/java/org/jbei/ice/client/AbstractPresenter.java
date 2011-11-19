package org.jbei.ice.client;

import com.google.gwt.user.client.ui.HasWidgets;

public abstract class AbstractPresenter {

    public AbstractPresenter() {
    }

    /**
     * "Entry Point" for any presenter that implements this
     * 
     * @param container
     *            Container for displaying views
     */
    public abstract void go(final HasWidgets container);
}
