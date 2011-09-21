package org.jbei.ice.client.collection;

import org.jbei.ice.shared.FolderDetails;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Cell for rendering (as menu) an item in a collection cell
 * 
 * @author Hector Plahar
 */

public class CollectionListMenuCell extends AbstractCell<FolderDetails> {

    private static final String MOUSEOVER_EVENT_NAME = "mouseover";
    private static final String MOUSEOUT_EVENT_NAME = "mouseout";

    public CollectionListMenuCell() {
        super(MOUSEOVER_EVENT_NAME, MOUSEOUT_EVENT_NAME);
    }

    @Override
    public void render(Context context, FolderDetails value, SafeHtmlBuilder sb) {
        String menuItem;

        if (value == null)
            menuItem = "";
        else
            menuItem = value.getName();
        sb.appendHtmlConstant("<span style=\"margin: 5px\">" + menuItem + "</span>");
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, FolderDetails value,
            NativeEvent event, ValueUpdater<FolderDetails> valueUpdater) {

        super.onBrowserEvent(context, parent, value, event, valueUpdater);

        final String eventType = event.getType();

        if (MOUSEOVER_EVENT_NAME.equalsIgnoreCase(eventType)) {
            parent.addClassName("collections_menu_hover");
        }

        if (MOUSEOUT_EVENT_NAME.equalsIgnoreCase(eventType)) {
            parent.removeClassName("collections_menu_hover");
        }
    }
}
