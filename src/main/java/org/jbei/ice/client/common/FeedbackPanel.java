package org.jbei.ice.client.common;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;

public class FeedbackPanel extends Composite {

    interface Style extends CssResource {

        String panel();

        String error();

        String success();

        String img();
    }

    interface FeedbackResource extends ClientBundle {

        static FeedbackResource INSTANCE = GWT.create(FeedbackResource.class);

        @Source("org/jbei/ice/client/resource/image/fail.png")
        @ImageOptions(repeatStyle = RepeatStyle.None)
        ImageResource fail();

        @Source("org/jbei/ice/client/resource/image/success.png")
        @ImageOptions(repeatStyle = RepeatStyle.None)
        ImageResource sucess();

        @Source("org/jbei/ice/client/resource/css/FeedbackPanel.css")
        Style css();
    }

    private final HTMLPanel panel;
    private final String IMG_ID = "feedbackpanel_image";
    private final String MSG_ID = "feedbackpanel_message";
    private final Image img;
    private final HTML label;

    public FeedbackPanel(String width) {
        this(true, width);
    }

    public FeedbackPanel(boolean showIcons, String width) {
        FeedbackResource.INSTANCE.css().ensureInjected();
        String html = "<span id=\"" + IMG_ID + "\"></span> <span id=\"" + MSG_ID + "\"></span>";
        panel = new HTMLPanel(html);
        panel.setWidth(width);
        panel.setStyleName(FeedbackResource.INSTANCE.css().panel());
        initWidget(panel);

        img = new Image();
        panel.add(img, IMG_ID);
        img.setVisible(showIcons);
        img.setStyleName(FeedbackResource.INSTANCE.css().img());
        label = new HTML();
        panel.add(label, MSG_ID);
    }

    public void setSuccessMessage(String msg) {
        img.setUrl(FeedbackResource.INSTANCE.sucess().getSafeUri());
        label.setHTML(msg);
        label.setStyleName(FeedbackResource.INSTANCE.css().success());
        this.setVisible(true);
    }

    public void setFailureMessage(String msg) {
        img.setUrl(FeedbackResource.INSTANCE.fail().getSafeUri());
        label.setHTML(msg);
        label.setStyleName(FeedbackResource.INSTANCE.css().error());
        this.setVisible(true);
    }

    public void setVisible(boolean visible) {
        this.panel.setVisible(visible);
    }
}