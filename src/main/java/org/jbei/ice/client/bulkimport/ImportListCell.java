package org.jbei.ice.client.bulkimport;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class ImportListCell extends AbstractCell<ImportType> {

    @Override
    public void render(Context context, ImportType value, SafeHtmlBuilder sb) {
        sb.appendHtmlConstant("<span>");
        sb.appendEscaped(value.getDisplay());
        sb.appendHtmlConstant("</span>");
    }
}
