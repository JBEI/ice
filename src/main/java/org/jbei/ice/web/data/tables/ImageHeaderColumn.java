package org.jbei.ice.web.data.tables;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Fragment;
import org.jbei.ice.web.pages.UnprotectedPage;

public class ImageHeaderColumn<T> extends AbstractSortableColumn<T> {

    private static final long serialVersionUID = 2L;

    private final MarkupContainer container;
    private final String imageId;
    private final String fragmentId;
    private final ResourceReference image;
    private final String altText;
    
    private ResourceReference blankImage = new ResourceReference(UnprotectedPage.class,
            UnprotectedPage.IMAGES_RESOURCE_LOCATION + "blank.png");

    /**
     * Be sure to add the following fragment to the markup container's html
     * <wicket:fragment wicket:id=[fragmentId]">
     * <img wicket:id=[imageId]/>
     * </wicket:fragment>
     * 
     * @param fragmentId
     *            unique wicket id for the fragment (from the container's fragment html)
     * @param imageId
     *            unique wicket id for the image (from container's fragment html)
     * @param resourceName
     *            image name eg. "foo.gif"
     * @param propertyExpression
     *            object property which contains value to be used in the cell
     * @param altText
     *            alt value for image
     * @param container
     *            MarkupContainer that contains the fragment html
     */
    public ImageHeaderColumn(String fragmentId, String imageId, String resourceName,
            String propertyExpression, String altText, MarkupContainer container) {
        super(null, propertyExpression);
        this.imageId = imageId;
        this.fragmentId = fragmentId;
        this.container = container;
        this.altText = altText;

        image = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + resourceName);
    }

    @Override
    public Fragment getHeader(String componentId) {
        Fragment fragment = new Fragment(componentId, fragmentId, container);
        Image image = new Image(this.imageId, this.image);

        if (altText != null && !altText.isEmpty()) {
            image.add(new SimpleAttributeModifier("alt", altText));
            image.add(new SimpleAttributeModifier("title", altText));
        }
        
        fragment.add(image);
        return fragment;
    }
    
    public ResourceReference getBlankImage() {
        return blankImage;
    }

    @Override
    public void detach() {
    }
}
