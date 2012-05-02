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

        @Source("org/jbei/ice/client/resource/image/delete.png")
        ImageResource deleteImage();

        @Source("org/jbei/ice/client/resource/image/busy.gif")
        ImageResource busyIndicatorImage();

        @Source("org/jbei/ice/client/resource/image/minus.png")
        ImageResource minusImage();

        @Source("org/jbei/ice/client/resource/image/edit.png")
        ImageResource editImage();

        @Source("org/jbei/ice/client/resource/image/prev.png")
        ImageResource prevImage();

        @Source("org/jbei/ice/client/resource/image/side_expand.png")
        ImageResource showSideImage();

        @Source("org/jbei/ice/client/resource/image/side_expand2.png")
        ImageResource hideSideImage();

        @Source("org/jbei/ice/client/resource/image/upload.png")
        ImageResource uploadImage();

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

    public static Image getDeleteIcon() {
        return new Image(Resources.INSTANCE.deleteImage());
    }

    public static Image getEditIcon() {
        return new Image(Resources.INSTANCE.editImage());
    }

    public static Image getBusyIcon() {
        return new Image(Resources.INSTANCE.busyIndicatorImage());
    }

    public static Image getMinusIcon() {
        return new Image(Resources.INSTANCE.minusImage());
    }

    public static Image getPrevIcon() {
        return new Image(Resources.INSTANCE.prevImage());
    }

    public static Image getShowSideImage() {
        return new Image(Resources.INSTANCE.showSideImage());
    }

    public static Image getHideSideImage() {
        return new Image(Resources.INSTANCE.hideSideImage());
    }

    public static Image getUploadImage() {
        return new Image(Resources.INSTANCE.uploadImage());
    }
}
