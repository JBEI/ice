package org.jbei.ice.client.view;

import org.jbei.ice.client.component.EntryTable;
import org.jbei.ice.client.panel.Footer;
import org.jbei.ice.client.panel.Header;
import org.jbei.ice.client.panel.HeaderMenu;
import org.jbei.ice.client.presenter.ProfilePresenter;

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class ProfileView extends Composite implements ProfilePresenter.Display {

    private final FlexTable table;
    private final Label about;
    private final Label entries;
    private final Label samples;
    private final Widget aboutReplacement;
    private final Widget entriesReplacement;
    private final Widget samplesReplacement;

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

        initHeaderStyles();
        aboutReplacement = createAboutWidget();
        entriesReplacement = new EntryTable();
        samplesReplacement = new EntryTable();

        table.setWidget(0, 0, createHeaders());
        table.setWidget(1, 0, aboutReplacement);

        entries.addMouseUpHandler(new MouseUpHandler() {

            @Override
            public void onMouseUp(MouseUpEvent event) {
                table.setWidget(1, 0, entriesReplacement);
            }
        });

        about.addMouseUpHandler(new MouseUpHandler() {

            @Override
            public void onMouseUp(MouseUpEvent event) {
                table.setWidget(1, 0, aboutReplacement);
            }
        });

        samples.addMouseUpHandler(new MouseUpHandler() {

            @Override
            public void onMouseUp(MouseUpEvent event) {
                table.setWidget(1, 0, samplesReplacement);
            }
        });

        // layout
        Label header = new Label("Profile for [Hector Plahar]");
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

    protected Widget createAboutWidget() {

        FlexTable contents = new FlexTable();
        contents.setCellPadding(3);
        contents.setCellSpacing(1);
        contents.setWidth("800px");
        contents.setHTML(0, 0, "<b>Name:</b>");
        contents.getCellFormatter().setWidth(0, 0, "150px");
        contents.setHTML(0, 1, "[Hector Plahar]");

        // password
        contents.setHTML(1, 0, "<b>Email:</b>");
        contents.setHTML(1, 1, "[haplahar@lbl.gov]");

        contents.setHTML(2, 0, "<b>Member since:</b>");
        contents.setHTML(2, 1, "[Mar 8 2011]");

        contents.setHTML(3, 0, "<b>Institution:</b>");
        contents.setHTML(3, 1, "[Lawrence Berkeley Laboratory]");

        contents.setHTML(4, 0, "<b>Description:</b>");
        contents.setHTML(4, 1, "[Physical Biosciences, (510)486-6754, Mail-Stop 978R4121]");

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
