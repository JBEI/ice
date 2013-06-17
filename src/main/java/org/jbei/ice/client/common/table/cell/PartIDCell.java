package org.jbei.ice.client.common.table.cell;

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
import org.jbei.ice.client.Callback;
import org.jbei.ice.client.collection.menu.IHasEntryHandlers;
import org.jbei.ice.client.collection.presenter.EntryContext;
import org.jbei.ice.client.common.TipViewContentFactory;
import org.jbei.ice.client.event.EntryViewEvent;
import org.jbei.ice.client.event.EntryViewEvent.EntryViewEventHandler;
import org.jbei.ice.shared.dto.entry.EntryInfo;

/**
 * Cell for part Id column values. Renders a url
 * that has a popup on mouse over
 *
 * @author Hector Plahar
 */

public class PartIDCell<T extends EntryInfo> extends AbstractCell<T> implements IHasEntryHandlers {

    private static PopupPanel popup = new PopupPanel(true);
    private static final String MOUSEOVER_EVENT_NAME = "mouseover";
    private static final String MOUSEOUT_EVENT_NAME = "mouseout";
    private static final String MOUSE_CLICK = "click";
    private HandlerManager handlerManager;
    private final EntryContext.Type mode;
    private boolean hidden = false;
    private boolean handlingClick;

    public PartIDCell(EntryContext.Type mode) {
        super(MOUSEOVER_EVENT_NAME, MOUSEOUT_EVENT_NAME, MOUSE_CLICK);
        this.mode = mode;
        popup.setStyleName("add_to_popup");
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
            if (withinBounds(event))
                onMouseOver(event, value);
            else
                onMouseOut();

        } else if (MOUSEOUT_EVENT_NAME.equalsIgnoreCase(eventType)) {
            onMouseOut();
        } else if (MOUSE_CLICK.equalsIgnoreCase(eventType)) {
            if (withinBounds(event))
                onMouseClick(value.getId(), value.getRecordId());
        }
    }

    protected void onMouseClick(long id, String recordId) {
        hidden = true;
        popup.hide();
        dispatchEntryViewEvent(id, recordId);
    }

    protected void dispatchEntryViewEvent(final long id, final String recordId) {
        fireEvent(new GwtEvent<EntryViewEventHandler>() {

            @Override
            public Type<EntryViewEventHandler> getAssociatedType() {
                return EntryViewEvent.getType();
            }

            @Override
            protected void dispatch(EntryViewEventHandler handler) {
                handler.onEntryView(new EntryViewEvent(id, recordId, mode));
            }
        });
    }

    protected void onMouseOut() {
        hidden = true;
        popup.hide();
    }

    protected boolean withinBounds(NativeEvent event) {
        Element cellElement = event.getEventTarget().cast();
        Element element = cellElement.getFirstChildElement();
        if (element == null)
            return true;
        return false;
    }

    protected void onMouseOver(final NativeEvent event, EntryInfo value) {
        hidden = false;
        final int x = event.getClientX() + 30 + Window.getScrollLeft();
        final int y = event.getClientY() + Window.getScrollTop();
        // TODO : set popup loading widget

        TipViewContentFactory.getContents(value, null, new Callback<Widget>() {
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
