package org.jbei.ice.client.common;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;

public class FeedbackPanel extends Composite {

    interface Style extends CssResource {

        String panel();

        String error();

        String success();

        String img();
    }

    interface FeedbackResource extends ClientBundle {

        static FeedbackResource INSTANCE = GWT.create(FeedbackResource.class);

        @Source("org/jbei/ice/client/resource/css/FeedbackPanel.css")
        Style css();
    }

    private final HTMLPanel panel;
    private final HTML label;

    public FeedbackPanel(String width) {
        FeedbackResource.INSTANCE.css().ensureInjected();
        final String MSG_ID = "feedbackpanel_message";
        String html = "<span id=\"" + MSG_ID + "\"></span>";
        panel = new HTMLPanel(html);
        panel.setWidth(width);
        panel.setStyleName(FeedbackResource.INSTANCE.css().panel());
        initWidget(panel);

        label = new HTML();
        panel.add(label, MSG_ID);
        panel.setVisible(false);
    }

    public void setSuccessMessage(String msg) {
        label.setHTML(msg);
        label.setStyleName(FeedbackResource.INSTANCE.css().success());
        this.setVisible(true);
    }

    public void setFailureMessage(String msg) {
        label.setHTML(msg);
        label.setStyleName(FeedbackResource.INSTANCE.css().error());
        this.setVisible(true);
    }

    public void setVisible(boolean visible) {
        this.panel.setVisible(visible);
    }
}