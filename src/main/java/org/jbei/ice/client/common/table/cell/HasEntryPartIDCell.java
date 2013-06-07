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
import org.jbei.ice.shared.dto.entry.EntryInfo;
import org.jbei.ice.shared.dto.entry.HasEntryInfo;

/**
 * @author Hector Plahar
 */

public class HasEntryPartIDCell<T extends HasEntryInfo> extends AbstractCell<T> implements IHasEntryHandlers {

    protected static final PopupPanel popup = new PopupPanel(true);
    private static final String MOUSEOVER_EVENT_NAME = "mouseover";
    private static final String MOUSEOUT_EVENT_NAME = "mouseout";
    private static final String MOUSE_CLICK = "click";
    private HandlerManager handlerManager;
    private final EntryContext.Type mode;
    protected boolean hidden = false;

    public HasEntryPartIDCell(EntryContext.Type mode) {
        super(MOUSEOVER_EVENT_NAME, MOUSEOUT_EVENT_NAME, MOUSE_CLICK);
        this.mode = mode;
        popup.setStyleName("add_to_popup");
    }

    @Override
    public void render(Context context, T view, SafeHtmlBuilder sb) {
        if (view == null || view.getEntryInfo().getPartId() == null)
            return;

        sb.appendHtmlConstant("<a class=\"cell_mouseover\">" + view.getEntryInfo().getPartId() + "</a>");
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
                onMouseClick(value);
        }
    }

    protected void onMouseClick(T info) {
        hidden = true;
        popup.hide();
        dispatchEntryViewEvent(info);
    }

    protected void dispatchEntryViewEvent(final T info) {
        fireEvent(new GwtEvent<EntryViewEvent.EntryViewEventHandler>() {

            @Override
            public Type<EntryViewEvent.EntryViewEventHandler> getAssociatedType() {
                return EntryViewEvent.getType();
            }

            @Override
            protected void dispatch(EntryViewEvent.EntryViewEventHandler handler) {
                EntryInfo entryInfo = info.getEntryInfo();
                EntryViewEvent entryViewEvent = new EntryViewEvent(entryInfo.getId(), entryInfo.getRecordId(), mode);
                entryViewEvent.getContext().setPartnerUrl(getURI(info));
                handler.onEntryView(entryViewEvent);
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

    protected String getURI(HasEntryInfo value) {
        return null;
    }

    protected void onMouseOver(NativeEvent event, HasEntryInfo value) {
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

    @Override
    public HandlerRegistration addEntryHandler(EntryViewEvent.EntryViewEventHandler handler) {
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
