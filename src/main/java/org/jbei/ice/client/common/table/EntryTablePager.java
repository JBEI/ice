package org.jbei.ice.client.common.table;

import org.jbei.ice.client.common.widget.FAIconType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.AbstractPager;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasRows;

/**
 * Pager for the Entry Table
 *
 * @author Hector Plahar
 */
public class EntryTablePager extends AbstractPager {

    private static class NavLink extends Button {

        private int page;
        private String defaultHTML;

        public NavLink(int page) {
            super(String.valueOf(page));
            this.page = page;
            this.setStyleName("button_to_link");
        }

        public NavLink(String html, ClickHandler handler) {
            super(html, handler);
            this.setStyleName("button_to_link");
            defaultHTML = html;
        }

        public void setDisabled(boolean isDisabled) {
            if (this.isEnabled() == !isDisabled)
                return;

            this.setEnabled(!isDisabled);
            if (!this.isEnabled()) {
                setStyleName("button_to_link");
            }
        }

        public void setLoading() {
            setHTML("<i class=\"icon-spin " + FAIconType.REFRESH.getStyleName() + "\"></i>");
        }

        public void setDefaultHTML() {
            if (defaultHTML != null)
                setHTML(defaultHTML);
        }

        // value of 0 is invalid
        public int getPage() {
            return this.page;
        }
    }

    private final NavLink first;
    private final NavLink last;
    private final NavLink next;
    private final NavLink prev;

    private FlexTable nav; // left
    private FlexTable jump; // right
    private final Label label; // center

    public static final int JUMP_PAGE_COUNT = 3;
    private int start;

    public EntryTablePager() {
        label = new Label();
        first = new NavLink("<i class=\"" + FAIconType.FAST_BACKWARD.getStyleName() + "\"></i>",
                            new ClickHandler() {

                                @Override
                                public void onClick(ClickEvent event) {
                                    EntryTablePager.super.firstPage();
                                }
                            });

        last = new NavLink("<i class=\"" + FAIconType.FAST_FORWARD.getStyleName() + "\"></i>",
                           new ClickHandler() {

                               @Override
                               public void onClick(ClickEvent event) {
                                   EntryTablePager.super.lastPage();
                               }
                           });
        last.setVisible(false);

        next = new NavLink("<i class=\"" + FAIconType.STEP_FORWARD.getStyleName() + "\"></i>",
                           new ClickHandler() {

                               @Override
                               public void onClick(ClickEvent event) {
                                   int indx = EntryTablePager.super.getPage() + 1;
                                   EntryTablePager.super.setPage(indx);
                               }
                           });

        prev = new NavLink("<i class=\"" + FAIconType.STEP_BACKWARD.getStyleName() + "\"></i>",
                           new ClickHandler() {

                               @Override
                               public void onClick(ClickEvent event) {
                                   int indx = EntryTablePager.super.getPage() - 1;
                                   EntryTablePager.super.setPage(indx);
                               }
                           });

        // result count

        FlexTable layout = new FlexTable();
        this.createNavigation();

        // next, prev paging navigation
        layout.setWidget(0, 0, nav);
        layout.getCellFormatter().setWordWrap(0, 0, false);

        // label navigation
        layout.setWidget(0, 1, label);
        layout.getCellFormatter().setHorizontalAlignment(0, 1, HasAlignment.ALIGN_CENTER);
        layout.getCellFormatter().setWidth(0, 1, "100%");

        // range navigation (page row count)
        Widget rangeOptions = this.createRangeOptions();
        layout.setWidget(0, 2, rangeOptions);
        layout.getCellFormatter().setHorizontalAlignment(0, 2, HasAlignment.ALIGN_RIGHT);

        initWidget(layout);

        // styles
        layout.setStyleName("pager");
        layout.setCellPadding(0);
        layout.setCellSpacing(0);
    }

    public void setLoading() {
        next.setLoading();
        next.setDisabled(true);
    }

    public void determineSetNextEnabled() {
        if (isRangeLimited()) {
            setNextPageButtonsDisabled(!hasNextPage());
        }
    }

    public void setDefaultHTML() {
        next.setDefaultHTML();
    }

    public void goToFirstPage() {
        this.firstPage();
    }

    /**
     * creates first (<<) prev (<) navigation
     */
    protected void createNavigation() {
        if (this.nav != null)
            return;

        this.nav = new FlexTable();
        this.nav.setWidget(0, 0, first);
        this.nav.setWidget(0, 1, prev);
        this.nav.setCellPadding(0);
        this.nav.setCellSpacing(3);
    }

    // called every time row is updated
    protected void updateNavigation() {

        // jump to urls
        jump = new FlexTable();
        jump.setCellPadding(0);

        // restrict limit to max of JUMP_PAGE_COUNT
        int limit = getPageCount() < JUMP_PAGE_COUNT ? getPageCount() : JUMP_PAGE_COUNT;

        // if total page count is less than total page display
        // or 
        // current page is before midpoint
        if (getPageCount() <= JUMP_PAGE_COUNT || (getPage() <= (limit / 2))) {
            start = 1;
        } else if (getPage() + 1 == getPageCount()) {
            // going to last page
            start = getPageCount() - limit + 1;
        } else if (getPage() + 1 < ((start + (start - 1 + limit)) / 2)) {
            // if less than halfway point of range we are looking at
            // midway = ( start + limit ) / 2
            start -= 1;
        } else if (getPageCount() >= (start + limit)) {
            // if we are not showing the last page yet
            start += 1;
        }

        boolean visible = (limit != 0);
        for (int i = start, j = 0; (i < (start + limit)); i += 1, j += 1) {
            NavLink link = new NavLink(i);
            link.setDisabled(i == (getPage() + 1));
            link.addClickHandler(new JumpToHandler(link));
            jump.setWidget(0, j, link);
        }

        // skip the "jump to" labels if there are no results
        if (visible) {
            this.nav.setWidget(0, 2, jump);
            this.nav.setWidget(0, 3, next);
        } else
            this.nav.setWidget(0, 2, next);

        this.nav.setWidget(0, 4, last);
    }

    protected Widget createRangeOptions() {
        FlexTable panel = new FlexTable();
        panel.setHTML(0, 0, "Show ");
        ListBox box = new ListBox();
        box.addItem("15");
        box.addItem("50");
        box.addItem("100");
        box.setStyleName("pull_down");
        panel.setWidget(0, 1, box);
        panel.setVisible(false);

        // styles       
        panel.setWidth("90px");
        return panel;
    }

    @Override
    protected void onRangeOrRowCountChanged() {
        String txt = this.createText();
        label.setText(txt);
        label.setStyleName("pager_nav");

        // update first and previous buttons
        setPrevPageButtonsDisabled(!hasPreviousPage());

        // Update the next and last buttons.
        if (isRangeLimited()) {
            setNextPageButtonsDisabled(!hasNextPage());
        }

        setRowCountButtons();
        updateNavigation();
    }

    protected void setRowCountButtons() {
        HasRows table = this.getDisplay();
        switch (table.getVisibleRange().getLength()) {
            case 15:
                break;
            case 50:
                break;
            case 100:
                break;
        }
    }

    protected String createText() {
        HasRows display = getDisplay();
        int dataSize = display.getRowCount();
        if (dataSize == 0)
            return "";

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

    /**
     * Enable or disable the previous page buttons.
     *
     * @param disabled true to disable, false to enable
     */
    private void setPrevPageButtonsDisabled(boolean disabled) {
        first.setDisabled(disabled);
        prev.setDisabled(disabled);
    }

    /**
     * Enable or disable the next page buttons.
     *
     * @param disabled true to disable, false to enable
     */
    private void setNextPageButtonsDisabled(boolean disabled) {
        next.setDisabled(disabled);
        last.setDisabled(disabled);
    }

    protected class VisibleRangeChanger implements ClickHandler {

        private final int range;

        public VisibleRangeChanger(int range) {
            super();
            this.range = range;
        }

        @Override
        public void onClick(ClickEvent event) {
            HasRows rows = getDisplay();
            if (rows.getVisibleRange().getLength() == range)
                return;

            int start = rows.getVisibleRange().getStart();
            rows.setVisibleRange(start, range);
            updateNavigation();
        }
    }

    protected class JumpToHandler implements ClickHandler {

        private final NavLink link;

        public JumpToHandler(NavLink link) {
            this.link = link;
        }

        @Override
        public void onClick(ClickEvent event) {

            int pageIndex = this.link.getPage() - 1;
            if (hasPage(pageIndex))
                EntryTablePager.this.setPage(pageIndex);
        }
    }
}
