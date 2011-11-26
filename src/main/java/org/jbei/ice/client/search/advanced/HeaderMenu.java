package org.jbei.ice.client.search.advanced;

import org.jbei.ice.client.collection.menu.ExportAsMenu;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;

public class HeaderMenu extends Composite {

    private final HTMLPanel panel;
    private final ExportAsMenu exportAs;
    private final AddToMenu addTo;

    public HeaderMenu(ExportAsMenu exportAs, AddToMenu addTo) {
        String html = "<span id=\"exportAs\"></span> <span id=\"addTo\"></span>";
        panel = new HTMLPanel(html);
        initWidget(panel);

        this.exportAs = exportAs;
        this.addTo = addTo;

        panel.add(this.exportAs.asWidget(), "exportAs");
        panel.add(this.addTo.asWidget(), "addTo");
    }
}
