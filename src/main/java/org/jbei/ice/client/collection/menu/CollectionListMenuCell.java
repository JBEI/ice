package org.jbei.ice.client.collection.menu;

import org.jbei.ice.shared.FolderDetails;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Cell for rendering (as menu) an item in a collection cell
 * 
 * @author Hector Plahar
 */

public class CollectionListMenuCell extends AbstractCell<FolderDetails> {

    public CollectionListMenuCell() {
    }

    @Override
    public void render(Context context, FolderDetails value, SafeHtmlBuilder sb) {
        String menuItem;

        if (value == null)
            menuItem = "";
        else
            menuItem = value.getName();
        sb.appendHtmlConstant("<span style=\"padding: 5px\" class=\"collection_user_menu\">"
                + menuItem + "</span><span class=\"menu_count\">" + formatNumber(value.getCount())
                + "</span>");
    }

    private String formatNumber(long l) {
        NumberFormat format = NumberFormat.getFormat("##,###");
        return format.format(l);
    }

}
