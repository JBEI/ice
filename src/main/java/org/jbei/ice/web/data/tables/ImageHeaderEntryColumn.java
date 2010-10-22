package org.jbei.ice.web.data.tables;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Fragment;
import org.jbei.ice.web.pages.UnprotectedPage;

public class ImageHeaderEntryColumn extends AbstractEntryColumn {

	private static final long serialVersionUID = 1L;
	private final MarkupContainer container;

	/**
	 * Be sure to add the following fragment to the markup container's html
	 * 		<wicket:fragment wicket:id="header_image_fragment">
	 *			<img wicket:id="header_image"/>
	 *		</wicket:fragment>
	 *
	 * @param propertyExpression
	 * @param container
	 */
	public ImageHeaderEntryColumn(String propertyExpression, MarkupContainer container) {
		super(propertyExpression);
		this.container = container;
	}
	
	@Override
	public Fragment getHeader(String componentId) {
		ResourceReference sequenceImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "sequence.gif");
		
		
		Fragment fragment = new Fragment( componentId, "header_image_fragment", container );
		fragment.add( new Image("header_image", sequenceImage) );
		return fragment;
	}
	
}
