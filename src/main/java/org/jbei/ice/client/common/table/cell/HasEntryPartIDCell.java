package org.jbei.ice.client.common.table.cell;

import org.jbei.ice.client.Callback;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.common.TipViewContentFactory;
import org.jbei.ice.lib.shared.dto.entry.HasEntryInfo;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Hector Plahar
 */

public class HasEntryPartIDCell<T extends HasEntryInfo> extends AbstractCell<T> {

    protected static final PopupPanel popup = new PopupPanel(true);
    private static final String MOUSEOVER_EVENT_NAME = "mouseover";
    private static final String MOUSEOUT_EVENT_NAME = "mouseout";
    private static final String MOUSE_CLICK = "click";
    protected boolean hidden = false;
    private final ServiceDelegate<T> viewDelegate;

    public HasEntryPartIDCell(ServiceDelegate<T> entryViewDelegate) {
        super(MOUSEOVER_EVENT_NAME, MOUSEOUT_EVENT_NAME, MOUSE_CLICK);
        popup.setStyleName("add_to_popup");
        viewDelegate = entryViewDelegate;
    }

    @Override
    public void render(Context context, T view, SafeHtmlBuilder sb) {
        if (view == null || view.getEntryInfo().getPartId() == null)
            return;

        sb.appendHtmlConstant("<a class=\"cell_mouseover\">" + view.getEntryInfo().getPartId() + "</a>");
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, T value, NativeEvent event, ValueUpdater<T> updater) {
        super.onBrowserEvent(context, parent, value, event, updater);
        final String eventType = event.getType();

        if (MOUSEOVER_EVENT_NAME.equalsIgnoreCase(eventType)) {
            if (withinBounds(event))
                onMouseOver(event, value);
            else
                onMouseOut();

        } else if (MOUSEOUT_EVENT_NAME.equalsIgnoreCase(eventType)) {
            onMouseOut();
        } else if (MOUSE_CLICK.equalsIgnoreCase(eventType)) {
            if (withinBounds(event))
                onMouseClick(value);
        }
    }

    protected void onMouseClick(T info) {
        hidden = true;
        popup.hide();
        viewDelegate.execute(info);
    }

    protected void onMouseOut() {
        hidden = true;
        popup.hide();
    }

    protected boolean withinBounds(NativeEvent event) {
        Element cellElement = event.getEventTarget().cast();
        Element element = cellElement.getFirstChildElement();
        return element == null;
    }

    protected String getURI(T value) {
        return null;
    }

    protected void onMouseOver(NativeEvent event, T value) {
        hidden = false;
        final int x = event.getClientX() + 30 + Window.getScrollLeft();
        final int y = event.getClientY() + Window.getScrollTop();
        // TODO : set popup loading widget

        TipViewContentFactory.getContents(value.getEntryInfo(), getURI(value), new Callback<Widget>() {

            @Override
            public void onSuccess(Widget contents) {
                if (hidden)
                    return;

                popup.setWidget(contents);

                // 450 is expected height of popup. adjust accordingly or the bottom will be hidden
                int bounds = 450 + y;
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

            @Override
            public void onFailure() {
                // doing nothing seems fine. no tooltip will be displayed
            }
        });
    }
}
