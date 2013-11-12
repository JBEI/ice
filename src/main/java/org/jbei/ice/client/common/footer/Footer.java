package org.jbei.ice.client.common.footer;

import com.google.gwt.user.client.ui.*;

public class Footer extends Composite {

    private static Footer INSTANCE;

    public static Footer getInstance() {
        if (INSTANCE == null)
            INSTANCE = new Footer();

        return INSTANCE;
    }

    private Footer() {
        FlexTable layout = new FlexTable();
        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.setWidth("100%");
        initWidget(layout);

        // add line
        layout.setWidget(0, 0, getLine());

        // create images
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(new Image("static/images/doe-bioenergy-research-cent.gif"));
        panel.add(new Image("static/images/lbnl-logo.gif"));
        panel.add(new Image("static/images/sandia-lab-logo.gif"));
        panel.add(new Image("static/images/ucb-logo.gif"));
        panel.add(new Image("static/images/ucdavis-logo.gif"));
        panel.add(new Image("static/images/carnegie-insitution-logo.gif"));
        panel.add(new Image("static/images/llnl-logo.gif"));
        panel.add(new Image("static/images/pnw.png"));

        // add images to a table
        FlexTable contents = new FlexTable();
        contents.setWidth("100%");
        contents.setCellPadding(0);
        contents.setCellSpacing(0);
        contents.setWidget(0, 0, panel);
        contents.setWidget(0, 1, getFooterText());
        contents.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);
        layout.setWidget(1, 0, contents);
    }

    private Widget getLine() {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setStyleName("footer_line");
        panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
        panel.setWidth("100%");
        return panel;
    }

    private Widget getFooterText() {
        VerticalPanel panel = new VerticalPanel();
        panel.setStyleName("font-80em");
        panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

        HorizontalPanel line1 = new HorizontalPanel();
        Anchor registry = new Anchor("JBEI Registry", "https://github.com/JBEI/ice");
        line1.add(new HTML("&copy;&nbsp;"));
        line1.add(registry);

        panel.add(line1);
        panel.add(new Label("All rights reserved."));

        HorizontalPanel line3 = new HorizontalPanel();
        Anchor bugReport = new Anchor("Submit an Issue", "https://github.com/JBEI/ice/issues/new");
        line3.add(bugReport);
        line3.add(new HTML("&nbsp; | &nbsp;"));
        Anchor api = new Anchor("API", "/api");
        line3.add(api);
        line3.add(new HTML("&nbsp; | &nbsp; v3.5.0"));

        panel.add(line3);
        return panel;
    }
}
