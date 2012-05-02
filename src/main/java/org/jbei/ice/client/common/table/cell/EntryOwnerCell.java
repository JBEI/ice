package org.jbei.ice.client.common.table.cell;

import org.jbei.ice.client.Page;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class EntryOwnerCell<T extends EntryInfo> extends AbstractCell<T> {

    @Override
    public void render(Context context, T value, SafeHtmlBuilder sb) {
        String owner = value.getOwner();
        String ownerEmail = value.getOwnerEmail();

        if (ownerEmail == null || ownerEmail.isEmpty()) {
            sb.appendHtmlConstant("<i>" + owner + "</i>");
            return;
        }

        sb.appendHtmlConstant("<a href=\"#" + Page.PROFILE.getLink() + ";id="
                + value.getOwnerEmail() + "\">" + owner + "</a>");
    }
}
