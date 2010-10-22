package org.jbei.ice.web.data.tables;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Fragment;
import org.jbei.ice.web.pages.UnprotectedPage;

public class ImageHeaderEntryColumn extends AbstractEntryColumn {

    private static final long serialVersionUID = 1L;
    private final MarkupContainer container;
    private final String imageId;
    private final String fragmentId;
    private final String resourceName;

    /**
     * Be sure to add the following fragment to the markup container's html
     *  <wicket:fragment wicket:id=[fragmentId]">
     *      <img wicket:id=[imageId]/>
     *  </wicket:fragment>
     * 
     * @param propertyExpression
     * @param container
     */
    public ImageHeaderEntryColumn(String fragmentId, String imageId, String resourceName, String propertyExpression,
            MarkupContainer container) {
        super(propertyExpression);
        this.imageId = imageId;
        this.fragmentId = fragmentId;
        this.resourceName = resourceName;
        this.container = container;
    }

    @Override
    public Fragment getHeader(String componentId) {
        ResourceReference sequenceImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + resourceName);

        Fragment fragment = new Fragment(componentId, fragmentId, container);
        fragment.add(new Image(this.imageId, sequenceImage));
        return fragment;
    }

}
