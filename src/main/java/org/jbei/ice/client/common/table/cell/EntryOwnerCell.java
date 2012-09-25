package org.jbei.ice.client.common.table.cell;

import org.jbei.ice.client.Page;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public abstract class EntryOwnerCell<T> extends AbstractCell<T> {

    @Override
    public void render(Context context, T value, SafeHtmlBuilder sb) {
        String owner = getOwnerName(value);
        String ownerEmail = getOwnerEmail(value);

        if (ownerEmail == null || ownerEmail.isEmpty()) {
            sb.appendHtmlConstant("<i>" + owner + "</i>");
            return;
        }

        sb.appendHtmlConstant("<a href=\"#" + Page.PROFILE.getLink() + ";id=" + ownerEmail + "\">" + owner + "</a>");
    }

    public abstract String getOwnerName(T value);

    public abstract String getOwnerEmail(T value);
}
