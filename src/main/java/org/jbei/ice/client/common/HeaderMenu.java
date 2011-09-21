package org.jbei.ice.client.common;

import org.jbei.ice.client.Page;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Menu right below main header on top of all protected pages
 * Note that as of now, this composite performs no validation.
 * 
 * @author Hector Plahar
 */

public class HeaderMenu extends Composite {

    private static HeaderMenu INSTANCE;

    public static HeaderMenu getInstance() {
        if (INSTANCE == null)
            INSTANCE = new HeaderMenu();

        return INSTANCE;
    }

    public HeaderMenu() {
        FlexTable table = new FlexTable();
        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setWidth("100%");
        initWidget(table);

        // add content
        table.setWidget(0, 0, createMenus());

        // regular search
        table.setWidget(0, 1, createRegularSearch());
        table.getFlexCellFormatter().setWidth(0, 1, "330px");

        // advanced searches
        table.setWidget(0, 2, createAdvancedSearchLinks());
        table.getFlexCellFormatter().setWidth(0, 2, "120px");

        // below menu line
        table.setWidget(1, 0, getLine());
        table.getFlexCellFormatter().setColSpan(1, 0, 3);
    }

    protected Widget createMenus() {
        FlexTable table = new FlexTable();
        table.setCellPadding(0);
        table.setCellSpacing(0);

        Hyperlink home = new Hyperlink("Home", "page=main;sid=[email]");
        Hyperlink collections = new Hyperlink("Collections", "page=collections;id=[email]");
        Hyperlink add = new Hyperlink("Add new entry", "page=add;id=[email]");
        Hyperlink bulk = new Hyperlink("Bulk Import", "page=bulk;id=[email]");

        table.setWidget(0, 0, home);
        table.setWidget(0, 1, collections);
        table.setWidget(0, 2, add);
        table.setWidget(0, 3, bulk);

        return table;
    }

    protected Widget createRegularSearch() {
        FlexTable table = new FlexTable();
        table.setCellPadding(0);
        table.setCellSpacing(0);

        TextBox input = new TextBox();
        //        input.setStyleName("inputbox");
        input.setWidth("250px");
        table.setWidget(0, 0, input);

        Button search = new Button("Search");
        table.setWidget(0, 1, search);
        return table;
    }

    protected Widget createAdvancedSearchLinks() {
        FlexTable table = new FlexTable();
        table.setCellPadding(0);
        table.setCellSpacing(0);

        Hyperlink search = new Hyperlink("Advanced Search", Page.QUERY.getLink());
        Hyperlink blast = new Hyperlink("Blast", Page.BLAST.getLink());

        table.setWidget(0, 0, search);
        table.setWidget(1, 0, blast);
        return table;
    }

    private Widget getLine() {

        HorizontalPanel panel = new HorizontalPanel();
        panel.setStyleName("footer_line");
        panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
        panel.setWidth("100%");
        return panel;
    }
}
