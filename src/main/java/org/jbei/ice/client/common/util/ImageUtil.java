package org.jbei.ice.client.common.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;

public class ImageUtil {

    interface Resources extends ClientBundle {

        static Resources INSTANCE = GWT.create(Resources.class);

        @Source("org/jbei/ice/client/resource/image/sample.png")
        ImageResource sample();

        @Source("org/jbei/ice/client/resource/image/attachment.gif")
        ImageResource attachment();

        @Source("org/jbei/ice/client/resource/image/sequence.gif")
        ImageResource sequence();

        @Source("org/jbei/ice/client/resource/image/blank.png")
        ImageResource blank();

        @Source("org/jbei/ice/client/resource/image/plus.png")
        ImageResource plus();
    }

    public static Image getSampleIcon() {
        return new Image(Resources.INSTANCE.sample());
    }

    public static Image getAttachment() {
        return new Image(Resources.INSTANCE.attachment());
    }

    public static Image getSequenceIcon() {
        return new Image(Resources.INSTANCE.sequence());
    }

    public static Image getBlankIcon() {
        return new Image(Resources.INSTANCE.blank());
    }

    public static Image getPlusIcon() {
        return new Image(Resources.INSTANCE.plus());
    }
}
