package org.jbei.ice.client.entry.add.menu;

import org.jbei.ice.shared.EntryAddType;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;

public class NewEntryMenu extends CellList<EntryAddType> {

    public NewEntryMenu() {
        super(new Cell());
    }

    //
    // inner classes
    //
    static class Cell extends AbstractCell<EntryAddType> {

        private static final String MOUSEOVER_EVENT_NAME = "mouseover";
        private static final String MOUSEOUT_EVENT_NAME = "mouseout";

        public Cell() {
            super(MOUSEOVER_EVENT_NAME, MOUSEOUT_EVENT_NAME);
        }

        @Override
        public void render(com.google.gwt.cell.client.Cell.Context context, EntryAddType value,
                SafeHtmlBuilder sb) {

            if (value == null)
                return;

            sb.appendHtmlConstant("<span style=\"margin: 5px\">" + value.getDisplay() + "</span>");
        }

        @Override
        public void onBrowserEvent(Context context, Element parent, EntryAddType value,
                NativeEvent event, ValueUpdater<EntryAddType> valueUpdater) {

            super.onBrowserEvent(context, parent, value, event, valueUpdater);

            final String eventType = event.getType();

            if (MOUSEOVER_EVENT_NAME.equalsIgnoreCase(eventType)) {
                //                parent.addClassName("collections_menu_hover"); // TODO 
            }

            if (MOUSEOUT_EVENT_NAME.equalsIgnoreCase(eventType)) {
                //                parent.removeClassName("collections_menu_hover"); // TODO
            }
        }
    }
}
