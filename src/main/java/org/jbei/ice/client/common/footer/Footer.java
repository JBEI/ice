package org.jbei.ice.client.common.footer;

import org.jbei.ice.client.RegistryService;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.common.widget.PopupHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class Footer extends Composite {

    interface Resources extends ClientBundle {
        @Source("org/jbei/ice/client/resource/image/doe-bioenergy-research-cent.gif")
        ImageResource doeLogo();

        @Source("org/jbei/ice/client/resource/image/lbnl-logo.gif")
        ImageResource lblLogo();

        @Source("org/jbei/ice/client/resource/image/sandia-lab-logo.gif")
        ImageResource sandiaLogo();

        @Source("org/jbei/ice/client/resource/image/ucb-logo.gif")
        ImageResource ucLogo();

        @Source("org/jbei/ice/client/resource/image/ucdavis-logo.gif")
        ImageResource davisLogo();

        @Source("org/jbei/ice/client/resource/image/carnegie-insitution-logo.gif")
        ImageResource carnegieLogo();

        @Source("org/jbei/ice/client/resource/image/llnl-logo.gif")
        ImageResource lnlLogo();
    }

    private final Resources resources = GWT.create(Resources.class);
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
        panel.add(new Image(resources.doeLogo()));
        panel.add(new Image(resources.lblLogo()));
        panel.add(new Image(resources.sandiaLogo()));
        panel.add(new Image(resources.ucLogo()));
        panel.add(new Image(resources.davisLogo()));
        panel.add(new Image(resources.carnegieLogo()));
        panel.add(new Image(resources.lnlLogo()));

        // add images to a table
        FlexTable contents = new FlexTable();
        contents.setWidth("100%");
        contents.setCellPadding(0);
        contents.setCellSpacing(0);
        contents.setWidget(0, 0, panel);
        contents.setWidget(0, 1, getFooterText());
        contents.getCellFormatter()
                .setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);

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

        final PopupHandler handler = new PopupHandler(feedbackWidget, feedback.getElement(), -250,
                -200);
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
                        Window.alert("There was an error submitting your feedback. We apologize for the inconvenience\n\n"
                                + caught.getMessage());
                    }
                });
                handler.hidePopup();
            }
        });
    }
}
