package org.jbei.ice.client.common.table.cell;

import org.jbei.ice.client.Page;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public abstract class EntryOwnerCell<T> extends AbstractCell<T> {

    @Override
    public void render(Context context, T value, SafeHtmlBuilder sb) {
        String owner = getOwnerName(value);
        String ownerId = getOwnerId(value);
        sb.appendHtmlConstant("<a href=\"#" + Page.PROFILE.getLink() + ";id=" + ownerId + "\">" + owner + "</a>");
    }

    public abstract String getOwnerName(T value);

    public abstract String getOwnerId(T value);
}
