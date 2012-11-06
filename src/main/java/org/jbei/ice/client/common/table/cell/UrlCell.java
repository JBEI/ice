package org.jbei.ice.client.common.table.cell;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Cell that renders a url and allows sub-classes to implement on-click.
 * This comes in handy when history management is desired
 *
 * @param <T> object type cell will render
 * @author Hector Plahar
 */
public abstract class UrlCell<T> extends AbstractCell<T> {

    private static final String MOUSEOVER_EVENT_NAME = "mouseover";
    private static final String MOUSEOUT_EVENT_NAME = "mouseout";
    private static final String MOUSE_CLICK = "click";

    public UrlCell() {
        super(MOUSEOVER_EVENT_NAME, MOUSEOUT_EVENT_NAME, MOUSE_CLICK);
    }

    @Override
    public void render(Context context, T value, SafeHtmlBuilder sb) {

        if (value == null)
            return;

        sb.appendHtmlConstant("<a class=\"cell_mouseover\">" + getCellValue(value) + "</a>");
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, T value, NativeEvent event,
            ValueUpdater<T> valueUpdater) {

        super.onBrowserEvent(context, parent, value, event, valueUpdater);
        final String eventType = event.getType();

        if (MOUSE_CLICK.equalsIgnoreCase(eventType)) {
            if (withinBounds(parent, event))
                onClick(value);
        }
    }

    protected boolean withinBounds(Element parent, NativeEvent event) {
        Element cellElement = event.getEventTarget().cast();
        Element element = cellElement.getFirstChildElement();
        if (element == null)
            return true;
        return false;
    }

    /**
     * @return the value to be displayed as a link in the cell
     */
    protected abstract String getCellValue(T object);

    /**
     * Action to be performed in the event of a click. To actual go to the url,
     * example code is
     * <p/>
     * History.newItem(Page.ENTRY_VIEW.getLink() + ";id=" + recordId);
     */
    protected abstract void onClick(T object);
}
