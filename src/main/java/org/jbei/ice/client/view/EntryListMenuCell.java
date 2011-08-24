package org.jbei.ice.client.view;

import org.jbei.ice.client.EntryMenu;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class EntryListMenuCell extends AbstractCell<EntryMenu> {

    private static final String MOUSEOVER_EVENT_NAME = "mouseover";
    private static final String MOUSEOUT_EVENT_NAME = "mouseout";

    public EntryListMenuCell() {
        super(MOUSEOVER_EVENT_NAME, MOUSEOUT_EVENT_NAME);
    }

    @Override
    public void render(Context context, EntryMenu value, SafeHtmlBuilder sb) {
        sb.appendHtmlConstant("<span style=\"margin: 5px\">" + value.getDisplay() + "</span>");
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, EntryMenu value, NativeEvent event,
            ValueUpdater<EntryMenu> valueUpdater) {

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