package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.jbei.ice.web.panels.PermissionDeniedPanel;

public class PermissionDeniedPage extends UnprotectedPage {
    public PermissionDeniedPage(PageParameters parameters) {
        super(parameters);

        add(new PermissionDeniedPanel("deniedPanel"));

    }
}
