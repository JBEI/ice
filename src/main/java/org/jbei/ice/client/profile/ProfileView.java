package org.jbei.ice.client.profile;

import java.util.ArrayList;

import org.jbei.ice.client.collection.SamplesDataTable;
import org.jbei.ice.client.common.Footer;
import org.jbei.ice.client.common.Header;
import org.jbei.ice.client.common.HeaderMenu;
import org.jbei.ice.client.component.ExportAsPanel;
import org.jbei.ice.client.component.table.DataTable;
import org.jbei.ice.client.component.table.EntryDataTable;
import org.jbei.ice.client.component.table.EntryTablePager;
import org.jbei.ice.shared.EntryData;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ProfileView extends Composite implements ProfilePresenter.Display {

    private final FlexTable table;
    private final Label about;
    private final Label entries;
    private final Label samples;

    private Widget aboutTabContents;
    private Widget samplesTabContents;
    private Widget entriesTabContents;

    private EntryDataTable<EntryData> entriesTable;
    private SamplesDataTable samplesTable;

    private Label header;
    private FlexTable contents;

    public ProfileView() {

        FlexTable layout = new FlexTable();
        layout.setWidth("100%");
        layout.setHeight("98%");
        layout.setCellSpacing(0);
        layout.setCellPadding(0);
        initWidget(layout);

        about = new Label("About");
        entries = new Label("Entries");
        samples = new Label("Samples");
        table = new FlexTable();
        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setWidth("100%");

        initHeaderStyles();
        aboutTabContents = createAboutWidget();
        entriesTabContents = createEntriesTablePanel();
        samplesTabContents = createSamplesTablePanel();

        table.setWidget(0, 0, createHeaders());
        table.setWidget(1, 0, aboutTabContents);

        // shows the "about" tab contents when it is clicked on
        about.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                table.setWidget(1, 0, aboutTabContents);
            }
        });

        // layout
        header = new Label("User Profile");
        header.addStyleName("panel_header");
        header.addStyleName("pad_top");

        layout.setWidget(0, 0, new Header());
        layout.setWidget(1, 0, new HeaderMenu());
        layout.setWidget(2, 0, header);// header should be moved to the contents
        layout.setWidget(3, 0, table);
        layout.getFlexCellFormatter().setVerticalAlignment(3, 0, HasVerticalAlignment.ALIGN_TOP);
        layout.getCellFormatter().setHeight(3, 0, "100%");
        layout.setWidget(4, 0, Footer.getInstance());
    }

    private VerticalPanel createEntriesTablePanel() {

        entriesTable = new EntryDataTable<EntryData>() {

            @Override
            protected ArrayList<DataTableColumn<?>> createColumns() {

                ArrayList<DataTableColumn<?>> columns = new ArrayList<DataTableColumn<?>>();
                columns.add(super.addTypeColumn(true));
                columns.add(super.addPartIdColumn(true));
                super.addNameColumn();
                super.addSummaryColumn();
                super.addOwnerColumn();
                super.addStatusColumn();
                super.addHasAttachmentColumn();
                super.addHasSampleColumn();
                super.addHasSequenceColumn();
                super.addCreatedColumn();

                return columns;
            }
        };

        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        entriesTable.addStyleName("gray_border");
        panel.add(entriesTable);
        EntryTablePager tablePager = new EntryTablePager();
        tablePager.setDisplay(entriesTable);
        panel.add(tablePager);

        // Export as
        ExportAsPanel export = new ExportAsPanel();
        panel.add(export);
        return panel;
    }

    private VerticalPanel createSamplesTablePanel() {

        samplesTable = new SamplesDataTable();
        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        samplesTable.addStyleName("gray_border");
        panel.add(samplesTable);
        EntryTablePager tablePager = new EntryTablePager();
        tablePager.setDisplay(samplesTable);
        panel.add(tablePager);

        // Export as
        ExportAsPanel export = new ExportAsPanel();
        panel.add(export);
        return panel;
    }

    protected void initHeaderStyles() {

        about.setStyleName("base_tabs");
        about.addStyleName("tabs_active");
        about.addMouseOverHandler(new TabOverMouseHandler(about));
        about.addMouseOutHandler(new TabMouseOutHandler(about));
        about.addMouseDownHandler(new TabMouseClickHandler(about, entries, samples));

        entries.setStyleName("base_tabs");
        entries.addMouseOverHandler(new TabOverMouseHandler(entries));
        entries.addMouseOutHandler(new TabMouseOutHandler(entries));
        entries.addMouseDownHandler(new TabMouseClickHandler(entries, about, samples));

        samples.setStyleName("base_tabs");
        samples.addMouseOverHandler(new TabOverMouseHandler(samples));
        samples.addMouseOutHandler(new TabMouseOutHandler(samples));
        samples.addMouseDownHandler(new TabMouseClickHandler(samples, entries, about));
    }

    protected Widget createHeaders() {

        HorizontalPanel panel = new HorizontalPanel();
        panel.add(about);
        panel.add(entries);
        panel.add(samples);
        return panel;
    }

    /**
     * @return creates and returns the widget to be displayed
     *         for the "about" tab. uses class fields that are exposed
     *         via a setter
     */
    protected Widget createAboutWidget() {

        contents = new FlexTable();
        contents.setCellPadding(3);
        contents.setCellSpacing(1);
        contents.setWidth("800px");
        contents.setHTML(0, 0, "<b>Name:</b>");
        contents.getCellFormatter().setWidth(0, 0, "150px");
        contents.setHTML(0, 1, "");

        // password
        contents.setHTML(1, 0, "<b>Email:</b>");
        contents.setHTML(1, 1, "");

        contents.setHTML(2, 0, "<b>Member since:</b>");
        contents.setHTML(2, 1, "");

        contents.setHTML(3, 0, "<b>Institution:</b>");
        contents.setHTML(3, 1, "");

        contents.setHTML(4, 0, "<b>Description:</b>");
        contents.setHTML(4, 1, "");

        FlexTable layout = new FlexTable();
        layout.addStyleName("data_table");
        layout.setCellPadding(3);
        layout.setCellSpacing(1);
        layout.setHTML(0, 0, "General Information");
        layout.getCellFormatter().addStyleName(0, 0, "title_row_header");
        layout.getCellFormatter().addStyleName(1, 0, "background_white");
        layout.setWidget(1, 0, contents);
        return layout;
    }

    @Override
    public void setData(String name, String email, String since, String institution,
            String description) {

        header.setText("Profile for " + name);
        contents.setHTML(0, 1, name);
        contents.setHTML(1, 1, email);
        contents.setHTML(2, 1, since);
        contents.setHTML(3, 1, institution);
        contents.setHTML(4, 1, description);
    }

    @Override
    public void addEntryClickHandler(final ClickHandler handler) {

        entries.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                handler.onClick(event);
                table.setWidget(1, 0, entriesTabContents);
            }
        });
    }

    @Override
    public void addSamplesClickHandler(final ClickHandler handler) {

        samples.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                handler.onClick(event);
                table.setWidget(1, 0, samplesTabContents);
            }
        });
    }

    @Override
    public DataTable<EntryData> getEntriesDataView() {
        return entriesTable;
    }

    @Override
    public SamplesDataTable getSamplesDataTable() {
        return this.samplesTable;
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    private static class TabOverMouseHandler implements MouseOverHandler {
        private final Widget widget;

        public TabOverMouseHandler(Widget widget) {
            this.widget = widget;
        }

        @Override
        public void onMouseOver(MouseOverEvent event) {
            widget.addStyleName("tabs_hover");
        }
    }

    private static class TabMouseOutHandler implements MouseOutHandler {

        private final Widget widget;

        public TabMouseOutHandler(Widget widget) {
            this.widget = widget;
        }

        @Override
        public void onMouseOut(MouseOutEvent event) {
            widget.removeStyleName("tabs_hover");
        }
    }

    private static class TabMouseClickHandler implements MouseDownHandler {

        private final Widget widget;
        private final Widget[] others;

        public TabMouseClickHandler(Widget widget, Widget... widgets) {
            this.widget = widget;
            this.others = widgets;
        }

        @Override
        public void onMouseDown(MouseDownEvent event) {
            for (Widget other : others)
                other.removeStyleName("tabs_active");

            this.widget.addStyleName("tabs_active");
        }
    }
}
