package org.jbei.ice.client.admin.group;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.common.widget.FAIconType;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;
import static com.google.gwt.dom.client.BrowserEvents.KEYDOWN;

/**
 * @author Hector Plahar
 */
public class DeleteActionCell<C> extends AbstractCell<C> {

    private final SafeHtml html;
    private final ServiceDelegate<C> delegate;

    /**
     * Construct a new {@link DeleteActionCell}.
     *
     * @param delegate the delegate that will handle events
     */
    public DeleteActionCell(ServiceDelegate<C> delegate) {
        super(CLICK, KEYDOWN);
        this.delegate = delegate;
        this.html = new SafeHtmlBuilder().appendHtmlConstant(
                "<button type=\"button\" class=\"remove_user_button\">"
                        + "<i class=\"" + FAIconType.REMOVE.getStyleName()
                        + "\">").appendHtmlConstant("</i></button>").toSafeHtml();
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, C value,
            NativeEvent event, ValueUpdater<C> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
        if (CLICK.equals(event.getType())) {
            EventTarget eventTarget = event.getEventTarget();
            if (!Element.is(eventTarget)) {
                return;
            }
            if (parent.getFirstChildElement().isOrHasChild(Element.as(eventTarget))) {
                // Ignore clicks that occur outside of the main element.
                onEnterKeyDown(context, parent, value, event, valueUpdater);
            }
        }
    }

    @Override
    public void render(Context context, C value, SafeHtmlBuilder sb) {
        sb.append(html);
    }

    @Override
    protected void onEnterKeyDown(Context context, Element parent, C value,
            NativeEvent event, ValueUpdater<C> valueUpdater) {
        delegate.execute(value);
    }
}
