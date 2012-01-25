package org.jbei.ice.client.collection;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.AbstractPager;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.view.client.HasRows;

public class CollectionListPager extends AbstractPager {

    private NavLink prevLink;
    private NavLink nextLink;
    private final FlexTable table;

    public CollectionListPager() {

        nextLink = new NavLink("&gt;", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                int indx = CollectionListPager.super.getPage() + 1;
                CollectionListPager.super.setPage(indx);
            }
        });
        nextLink.addStyleName("nav-left");

        prevLink = new NavLink("&lt;", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                int indx = CollectionListPager.super.getPage() - 1;
                CollectionListPager.super.setPage(indx);
            }
        });
        prevLink.addStyleName("nav-right");

        table = new FlexTable();
        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setWidth("100%");

        table.setText(0, 0, "");
        table.getFlexCellFormatter().setStyleName(0, 0, "font-75em");
        table.getFlexCellFormatter().addStyleName(0, 0, "font-bold");
        table.setWidget(0, 1, prevLink);
        table.setWidget(0, 2, nextLink);

        initWidget(table);
    }

    @Override
    protected void onRangeOrRowCountChanged() {
        String text = this.createText();

        table.setText(0, 0, text);
        prevLink.setDisabled(!hasPreviousPage(), false);
        nextLink.setDisabled(!hasNextPage(), true);
    }

    protected String createText() {

        HasRows display = getDisplay();
        int dataSize = display.getRowCount();
        if (dataSize == 0)
            return "No Records";

        // Default text is 1 based.
        NumberFormat formatter = NumberFormat.getFormat("#,###");

        int pageStart = getPageStart() + 1;
        int pageSize = getPageSize();

        int endIndex = Math.min(dataSize, pageStart + pageSize - 1);
        endIndex = Math.max(pageStart, endIndex);
        boolean exact = display.isRowCountExact();

        return formatter.format(pageStart) + " - " + formatter.format(endIndex)
                + (exact ? " of " : " of over ") + formatter.format(dataSize);
    }

    // 
    // inner classes
    //
    private static class NavLink extends Button {

        public NavLink(String html, ClickHandler handler) {

            super(html, handler);
            setStyleName("nav");
        }

        public void setDisabled(boolean isDisabled, boolean isNext) {

            if (this.isEnabled() == !isDisabled)
                return;

            this.setEnabled(!isDisabled);
            if (!this.isEnabled())
                setStyleName("nav_disabled");
            else
                setStyleName("nav");

            if (isNext)
                addStyleName("nav-right");
            else
                addStyleName("nav-left");
        }
    }
}
