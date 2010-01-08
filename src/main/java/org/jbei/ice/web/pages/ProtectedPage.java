package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;

/**
 * Any extended class of this class benefits from initialization check on user authentication. See WicketApplication.
 * 
 * @author tham
 */
public class ProtectedPage extends UnprotectedPage {
	public ProtectedPage(PageParameters parameters) {
		super(parameters);
	}
}
