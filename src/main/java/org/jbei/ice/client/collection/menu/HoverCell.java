package org.jbei.ice.client.collection.menu;

import org.jbei.ice.client.collection.menu.CollectionMenu.Resources;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * Menu cell when user hovers over a menu item
 * 
 * @author Hector Plahar
 * 
 */

public class HoverCell extends Composite {
    private final HorizontalPanel panel;
    private final Image edit;
    private final Image delete;

    public HoverCell() {

        panel = new HorizontalPanel();
        panel.setStyleName("user_collection_action");
        initWidget(panel);

        edit = new Image(Resources.INSTANCE.editImage());
        delete = new Image(Resources.INSTANCE.deleteImage());

        panel.add(edit);
        panel.setHeight("16px");
        HTML pipe = new HTML("&nbsp;|&nbsp;");
        pipe.addStyleName("color_eee");
        panel.add(pipe);
        panel.add(delete);
        panel.setStyleName("menu_count");
    }

    public Image getEdit() {
        return this.edit;
    }

    public Image getDelete() {
        return this.delete;
    }
}
