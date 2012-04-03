package org.jbei.ice.client.common.table.cell;

import org.jbei.ice.client.collection.menu.IHasEntryHandlers;
import org.jbei.ice.client.collection.presenter.EntryContext;
import org.jbei.ice.client.common.TipViewContentFactory;
import org.jbei.ice.client.event.EntryViewEvent;
import org.jbei.ice.client.event.EntryViewEvent.EntryViewEventHandler;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Cell for part Id column values. Renders a url
 * that has a popup on mouse over
 * 
 * @author Hector Plahar
 */

public class PartIDCell<T extends EntryInfo> extends AbstractCell<T> implements IHasEntryHandlers {

    private PopupPanel popup;
    private static final String MOUSEOVER_EVENT_NAME = "mouseover";
    private static final String MOUSEOUT_EVENT_NAME = "mouseout";
    private static final String MOUSE_CLICK = "click";
    private HandlerManager handlerManager;
    private final EntryContext.Type mode;

    public PartIDCell(EntryContext.Type mode) {
        super(MOUSEOVER_EVENT_NAME, MOUSEOUT_EVENT_NAME, MOUSE_CLICK);
        this.mode = mode;
    }

    @Override
    public void render(Context context, T view, SafeHtmlBuilder sb) {

        if (view == null || view.getPartId() == null)
            return;

        sb.appendHtmlConstant("<a class=\"cell_mouseover\">" + view.getPartId() + "</a>");
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, T value, NativeEvent event,
            ValueUpdater<T> valueUpdater) {

        super.onBrowserEvent(context, parent, value, event, valueUpdater);

        final String eventType = event.getType();

        if (MOUSEOVER_EVENT_NAME.equalsIgnoreCase(eventType)) {
            onMouseOver(parent, event, value);
        } else if (MOUSEOUT_EVENT_NAME.equalsIgnoreCase(eventType)) {
            onMouseOut(parent);
        } else if (MOUSE_CLICK.equalsIgnoreCase(eventType)) {
            onMouseClick(value.getId());
        }
    }

    protected void onMouseClick(long recordId) {
        dispatchEntryViewEvent(recordId);
    }

    protected void dispatchEntryViewEvent(final long recordId) {
        fireEvent(new GwtEvent<EntryViewEventHandler>() {

            @Override
            public Type<EntryViewEventHandler> getAssociatedType() {
                return EntryViewEvent.getType();
            }

            @Override
            protected void dispatch(EntryViewEventHandler handler) {
                handler.onEntryView(new EntryViewEvent(recordId, mode));
            }
        });
    }

    protected void onMouseOut(Element parent) {
        if (popup != null) {
            popup.hide();
            popup = null;
        }
    }

    protected void onMouseOver(Element parent, NativeEvent event, EntryInfo value) {

        final int x = event.getClientX() + 30 + Window.getScrollLeft();
        final int y = event.getClientY() + Window.getScrollTop();

        popup = new PopupPanel(true);
        popup.setStyleName("add_to_popup");

        Widget contents = getTipViewContents(value);
        popup.add(contents);

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

    protected Widget getTipViewContents(EntryInfo value) {
        return TipViewContentFactory.getContents(value);
    }

    @Override
    public HandlerRegistration addEntryHandler(EntryViewEventHandler handler) {
        if (handlerManager == null)
            handlerManager = new HandlerManager(this);
        return handlerManager.addHandler(EntryViewEvent.getType(), handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        if (handlerManager != null)
            handlerManager.fireEvent(event);
    }
}
