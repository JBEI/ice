package org.jbei.ice.client.common.table.column;

import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;

/**
 * Column which has a static image as a header and the same image as content. A boolean condition determines where to
 * display the image or not
 *
 * @author Hector Plahar
 */

public abstract class ImageColumn<T> extends Column<T, ImageResource> {

    private final static Resources resources = GWT.create(Resources.class);

    /**
     * Supported image types for the column
     */
    public enum Type {
        SAMPLE(resources.sample()),
        ATTACHMENT(resources.attachment()),
        SEQUENCE(resources.sequence());

        private transient ImageResource image;

        Type(ImageResource resource) {
            this.image = resource;
        }

        public ImageResource getResource() {
            return image;
        }
    }

    private final Type type;
    private final ImageHeader header;

    public ImageColumn(Type type) {
        super(new ImageResourceCell());
        this.type = type;
        header = new ImageHeader(this.type);
    }

    public abstract boolean showImage(T object);

    @Override
    public ImageResource getValue(T object) {

        boolean showImage = showImage(object);
        if (showImage)
            return this.type.getResource();

        return resources.blank();
    }

    public ImageHeader getHeader() {
        return this.header;
    }

    interface Resources extends ClientBundle {

        @Source("org/jbei/ice/client/resource/image/sample.png")
        ImageResource sample();

        @Source("org/jbei/ice/client/resource/image/attachment.gif")
        ImageResource attachment();

        @Source("org/jbei/ice/client/resource/image/sequence.gif")
        ImageResource sequence();

        @Source("org/jbei/ice/client/resource/image/blank.png")
        ImageResource blank();
    }

    protected static class ImageHeader extends Header<ImageResource> {

        private final static ImageResourceCell cell = new ImageResourceCell();
        private final ImageColumn.Type type;

        public ImageHeader(ImageColumn.Type type) {
            super(cell);
            this.type = type;
        }

        @Override
        public ImageResource getValue() {
            return this.type.getResource();
        }
    }
}
