package org.jbei.ice.client.common;

import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.Icon;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Panel for displaying feedback messages to the user. The messages have a timeout of
 * 25secs after which they are hidden
 *
 * @author Hector Plahar
 */
public class FeedbackPanel extends Composite {

    interface Style extends CssResource {

        String panel();

        String error();

        String success();
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
        String html = "<span id=\"" + MSG_ID + "\"></span><span id=\"close_panel\"></span>";
        panel = new HTMLPanel(html);
        panel.setWidth(width);
        panel.setStyleName(FeedbackResource.INSTANCE.css().panel());
        initWidget(panel);

        label = new HTML();
        panel.add(label, MSG_ID);
        Icon icon = new Icon(FAIconType.REMOVE);
        panel.add(icon, "close_panel");
        icon.addStyleName("float_right");
        icon.addStyleName("opacity_hover");
        icon.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                panel.setVisible(false);
            }
        });
        panel.setVisible(false);
    }

    public void setSuccessMessage(String msg) {
        label.setHTML(msg);
        label.setStyleName(FeedbackResource.INSTANCE.css().success());
        this.setVisible(true);
        hideInAFew();
    }

    public void setFailureMessage(String msg) {
        label.setHTML(msg);
        label.setStyleName(FeedbackResource.INSTANCE.css().error());
        this.setVisible(true);
        hideInAFew();
    }

    public void setVisible(boolean visible) {
        this.panel.setVisible(visible);
    }

    protected void hideInAFew() {
        new Timer() {

            @Override
            public void run() {
                setVisible(false);
            }
        }.schedule(25000);
    }
}