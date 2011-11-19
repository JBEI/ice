package org.jbei.ice.client.profile;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class ProfileMenuCell extends AbstractCell<CellEntry> {

    @Override
    public void render(Context context, CellEntry value, SafeHtmlBuilder sb) {
        sb.appendHtmlConstant("<span>");
        sb.appendEscaped(value.getType().getDisplay());

        if (value.getCount() >= 0) {
            sb.appendHtmlConstant("<span style=\"float: right\">");
            sb.append(value.getCount());
            sb.appendHtmlConstant("</span>");
        }
        sb.appendHtmlConstant("</span>");
    }

}
