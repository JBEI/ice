package org.jbei.ice.client.bulkimport.model;

import org.jbei.ice.shared.dto.BulkImportDraftInfo;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;

public class BulkImportMenu extends CellList<BulkImportDraftInfo> {

    protected interface MenuResources extends Resources {
        /**
         * The styles used in this widget.
         */
        @Override
        @Source("org/jbei/ice/client/resource/css/ListMenu.css")
        // TODO : style in here affect menu selection
        Style cellListStyle();
    }

    private static MenuResources resources = GWT.create(MenuResources.class);

    public BulkImportMenu() {
        super(new Cell(), resources);
    }

    private static class Cell extends AbstractCell<BulkImportDraftInfo> {

        @Override
        public void render(Context context, BulkImportDraftInfo value, SafeHtmlBuilder sb) {
            if (value == null)
                return;

            sb.appendHtmlConstant("<span style=\"padding: 5px\" class=\"collection_user_menu\">"
                    + value.getName() + "</span><span class=\"menu_count\">"
                    + formatNumber(value.getCount()) + "</span>");
        }

        private String formatNumber(long l) {
            NumberFormat format = NumberFormat.getFormat("##,###");
            return format.format(l);
        }
    }
}
