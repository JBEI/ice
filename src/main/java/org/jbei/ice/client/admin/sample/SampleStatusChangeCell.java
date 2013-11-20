package org.jbei.ice.client.admin.sample;

import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.lib.shared.dto.sample.SampleRequest;
import org.jbei.ice.lib.shared.dto.sample.SampleRequestStatus;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;
import static com.google.gwt.dom.client.BrowserEvents.KEYDOWN;

/**
 * Cell for handling status change requests. Mainly for the SampleRequestTable
 *
 * @author Hector Plahar
 */
public class SampleStatusChangeCell extends AbstractCell<SampleRequest> {

    private Delegate<SampleRequest> delegate;

    public SampleStatusChangeCell(Delegate<SampleRequest> delegate) {
        super(CLICK, KEYDOWN);
        this.delegate = delegate;
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, SampleRequest value,
            NativeEvent event, ValueUpdater<SampleRequest> valueUpdater) {
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
    public void render(Context context, SampleRequest value, SafeHtmlBuilder sb) {
        String style;
        if (value.getStatus() == SampleRequestStatus.FULFILLED)
            style = FAIconType.CHECK_SQUARE_ALT.getStyleName() + " green";
        else if (value.getStatus() == SampleRequestStatus.PENDING)
            style = FAIconType.SQUARE_ALT.getStyleName() + " color_999";
        else
            style = FAIconType.EXCLAMATION_TRIANGLE.getStyleName() + " red";
        sb.appendHtmlConstant("<i class=\"" + style + " pixel_perfect\">").appendHtmlConstant("</i>");
    }

    @Override
    protected void onEnterKeyDown(Context context, Element parent, SampleRequest value,
            NativeEvent event, ValueUpdater<SampleRequest> valueUpdater) {
        if (delegate != null)
            delegate.execute(value);
    }
}
