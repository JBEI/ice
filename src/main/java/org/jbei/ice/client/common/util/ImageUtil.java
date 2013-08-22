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

        @Source("org/jbei/ice/client/resource/image/busy.gif")
        ImageResource busyIndicatorImage();

        @Source("org/jbei/ice/client/resource/image/show_sidebar.png")
        ImageResource showSideImage();

        @Source("org/jbei/ice/client/resource/image/hide_sidebar.png")
        ImageResource hideSideImage();

        @Source("org/jbei/ice/client/resource/image/upload_file.png")
        ImageResource uploadImage();

        @Source("org/jbei/ice/client/resource/image/file_upload.png")
        ImageResource fileUpload();
    }

    public static Image getSampleIcon() {
        return new Image(Resources.INSTANCE.sample());
    }

    public static Image getBusyIcon() {
        return new Image(Resources.INSTANCE.busyIndicatorImage());
    }

    public static Image getShowSideImage() {
        return new Image(Resources.INSTANCE.showSideImage());
    }

    public static Image getHideSideImage() {
        return new Image(Resources.INSTANCE.hideSideImage());
    }
}
