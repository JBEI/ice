package org.jbei.ice.client.home;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.StackLayoutPanel;

public class ArchiveMenu extends Composite {

    private final StackLayoutPanel panel;

    public ArchiveMenu() {
        panel = new StackLayoutPanel(Unit.EM);
        initWidget(panel);
    }
}
