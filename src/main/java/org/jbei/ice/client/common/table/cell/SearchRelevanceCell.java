package org.jbei.ice.client.common.table.cell;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * @author Hector Plahar
 */
public abstract class SearchRelevanceCell<T> extends AbstractCell<T> {

    @Override
    public void render(Context context, T value, SafeHtmlBuilder sb) {
        String html = getHTML(value);
        sb.appendHtmlConstant(html);
    }

    public abstract String getHTML(T value);
}
