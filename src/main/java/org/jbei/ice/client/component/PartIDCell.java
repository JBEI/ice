package org.jbei.ice.client.component;

import org.jbei.ice.client.util.Utils;
import org.jbei.ice.shared.EntryDataView;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Cell for part Id column values. Renders a url
 * that is has a popup on mouse over
 * 
 * @author Hector Plahar
 */

public class PartIDCell extends AbstractCell<EntryDataView> {

    private PopupPanel popup;
    private static final String MOUSEOVER_EVENT_NAME = "mouseover";
    private static final String MOUSEOUT_EVENT_NAME = "mouseout";
    private static final String MOUSEOVER_STYLE = "mouseover_color";

    public PartIDCell() {

        super(MOUSEOVER_EVENT_NAME, MOUSEOUT_EVENT_NAME);
    }

    @Override
    public void render(Context context, EntryDataView view, SafeHtmlBuilder sb) {

        if (view == null || view.getPartId() == null)
            return;

        // entry/tip/4380
        sb.appendHtmlConstant("<a>" + view.getPartId() + "</a>");
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, EntryDataView value,
            NativeEvent event, ValueUpdater<EntryDataView> valueUpdater) {

        super.onBrowserEvent(context, parent, value, event, valueUpdater);

        final String eventType = event.getType();

        if (MOUSEOVER_EVENT_NAME.equalsIgnoreCase(eventType)) {

            parent.setClassName(MOUSEOVER_STYLE);
            Utils.showPointerCursor(parent);
            final int x = event.getClientX() + 30 + Window.getScrollLeft();
            final int y = event.getClientY() + Window.getScrollTop();

            popup = new PopupPanel(true);
            popup.setStyleName("popup");

            Widget contents = TipViewContentFactory.getContents(value);
            popup.add(contents);

            int bounds = 400 + y; // 400 is expected height of popup
            int yPos = y;
            if (bounds > Window.getClientHeight()) {
                // move it up;
                yPos -= (bounds - Window.getClientHeight());
                if (yPos < 0)
                    yPos = 0;
            }
            popup.setPopupPosition(x, yPos);
            popup.show();
        }

        if (MOUSEOUT_EVENT_NAME.equalsIgnoreCase(eventType)) {

            if (popup != null) {
                popup.hide();
                popup = null;
                Utils.showDefaultCursor(parent);
                parent.removeClassName(MOUSEOVER_STYLE);
            }
        }

    }
}
