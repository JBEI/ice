package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.jbei.ice.web.panels.PermissionDeniedPanel;

public class PermissionDeniedPage extends UnprotectedPage {
    public PermissionDeniedPage(PageParameters parameters) {
        super(parameters);

        String message = "";

        if (parameters == null || parameters.size() != 1) {
            message = "You are do not have permission to view this page";
        } else {
            message = parameters.getString("0");
        }

        add(new PermissionDeniedPanel("deniedPanel", message));
    }
}
