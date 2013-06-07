package org.jbei.ice.client.common.footer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.jbei.ice.client.RegistryService;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.common.widget.PopupHandler;

public class Footer extends Composite {

    private final RegistryServiceAsync service = GWT.create(RegistryService.class);

    private static Footer INSTANCE;
    private FeedbackWidget feedbackWidget;

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

        // feedback widget
        feedbackWidget = new FeedbackWidget();

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
        Anchor registry = new Anchor("JBEI Registry", "http://code.google.com/p/gd-ice/");
        line1.add(new HTML("&copy;&nbsp;"));
        line1.add(registry);

        panel.add(line1);
        panel.add(new Label("All rights reserved."));

        HorizontalPanel line3 = new HorizontalPanel();
        Anchor featureReq = new Anchor("Feature Request",
                "http://code.google.com/p/gd-ice/issues/entry?template=Suggest%20Feature");
        line3.add(featureReq);
        line3.add(new HTML("&nbsp; | &nbsp; "));
        Anchor bugReport = new Anchor("Report a Bug",
                "http://code.google.com/p/gd-ice/issues/entry?template=Report%20Bug");
        line3.add(bugReport);
        line3.add(new HTML("&nbsp; | &nbsp; "));
        Label feedback = new Label("Feedback");
        feedback.setStyleName("footer_feedback_widget");

        final PopupHandler handler = new PopupHandler(feedbackWidget, feedback.getElement(), true);
        feedbackWidget.addCloseHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                feedbackWidget.clear();
                handler.hidePopup();
            }
        });
        addSubmitHandler(handler);
        feedback.addClickHandler(handler);
        line3.add(feedback);

        line3.add(new HTML("&nbsp; | &nbsp; "));
        Anchor api = new Anchor("API", GWT.getHostPageBaseURL() + "api");
        line3.add(api);

        panel.add(line3);
        return panel;
    }

    // TODO : presenter logic in view
    private void addSubmitHandler(final PopupHandler handler) {
        feedbackWidget.addSubmitHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                String email = feedbackWidget.getEmail();
                String message = feedbackWidget.getMessage();

                service.sendFeedback(email, message, new AsyncCallback<Boolean>() {

                    @Override
                    public void onSuccess(Boolean result) {
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert(
                                "There was an error submitting your feedback. We apologize for the inconvenience\n\n");
                    }
                });
                handler.hidePopup();
            }
        });
    }
}
