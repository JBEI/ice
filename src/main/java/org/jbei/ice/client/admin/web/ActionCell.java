package org.jbei.ice.client.admin.web;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.lib.shared.dto.web.RegistryPartner;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;

/**
 * Custom cell that displays the remote partner's {@link org.jbei.ice.lib.shared.dto.web.RemoteActionStatus} in a
 * Partner table. Also adds the ability to change the status of the partner.
 *
 * @author Hector Plahar
 */
public class ActionCell extends AbstractCell<RegistryPartner> {

    private final ServiceDelegate<RegistryPartner> delegate;
    private final SafeHtml html;

    public ActionCell(ServiceDelegate<RegistryPartner> delegate) {
        super(BrowserEvents.CLICK, BrowserEvents.KEYDOWN);
        this.delegate = delegate;
        this.html = new SafeHtmlBuilder().appendHtmlConstant(
                "<button type=\"button\" class=\"delete_icon\">"
                        + "<i class=\"" + FAIconType.BAN_CIRCLE.getStyleName()
                        + "\">").appendHtmlConstant("</i></button>").toSafeHtml();
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, RegistryPartner value,
            NativeEvent event, ValueUpdater<RegistryPartner> valueUpdater) {
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
    public void render(Context context, RegistryPartner value, SafeHtmlBuilder sb) {
        sb.append(html);
    }

    @Override
    protected void onEnterKeyDown(Context context, Element parent, RegistryPartner value,
            NativeEvent event, ValueUpdater<RegistryPartner> valueUpdater) {
        delegate.execute(value);
    }

}
