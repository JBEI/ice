package org.jbei.ice.client.profile.message;

import org.jbei.ice.client.Delegate;
import org.jbei.ice.lib.shared.dto.message.MessageInfo;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * @author Hector Plahar
 */
public abstract class MessageTableCell extends AbstractCell<MessageInfo> {

    private final Delegate<MessageInfo> delegate;

    public MessageTableCell(Delegate<MessageInfo> delegate) {
        super("click", "keydown");
        this.delegate = delegate;
    }

    @Override
    protected void onEnterKeyDown(Context context, Element parent, MessageInfo value, NativeEvent event,
            ValueUpdater<MessageInfo> valueUpdater) {
        delegate.execute(value);
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, MessageInfo value, NativeEvent event,
            ValueUpdater<MessageInfo> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
        if ("click".equals(event.getType())) {
            onEnterKeyDown(context, parent, value, event, valueUpdater);
        }
    }

    @Override
    public void render(Context context, MessageInfo value, SafeHtmlBuilder sb) {
        sb.append(render(value));
    }

    public abstract SafeHtml render(MessageInfo value);
}
