package org.jbei.ice.web.panels;

import org.apache.wicket.markup.html.panel.Panel;

public class PermissionDeniedPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public PermissionDeniedPanel(String id, String message) {
        super(id);

        add(new EmptyMessagePanel("msgPanel", message));
    }
}
