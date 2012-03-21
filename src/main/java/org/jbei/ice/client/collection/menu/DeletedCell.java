package org.jbei.ice.client.collection.menu;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;

public class DeletedCell extends Composite {

    private final MenuItem menu;

    public DeletedCell(MenuItem item, ClickHandler undoHandler) {
        this.menu = item;

        String name = item.getName();
        if (name.length() > 27)
            name = (name.substring(0, 24) + "...");

        String html = "<span style=\"padding: 5px\" class=\"undo_delete_name\"><b>"
                + name
                + "</b> deleted.</span><span class=\"undo_delete_name_link\" id=\"undo_link\"></span>";

        HTMLPanel panel = new HTMLPanel(html);
        panel.setTitle(item.getName());

        HTML undo = new HTML("undo");
        undo.addClickHandler(undoHandler);

        panel.add(undo, "undo_link");
        panel.setStyleName("undo_delete_name_row");
        initWidget(panel);
    }

    public MenuItem getMenuItem() {
        return this.menu;
    }
}
