package org.jbei.ice.client;

import com.google.gwt.user.client.ui.HasWidgets;

public abstract interface Presenter {

    /**
     * "Entry Point" for any presenter that implements this
     * 
     * @param container
     *            Container for displaying views
     */
    public abstract void go(final HasWidgets container);
}
