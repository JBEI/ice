package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;

/**
 * This page exists to test error logging and reporting
 * 
 * @author tham
 * 
 */
public class AssertErrorPage extends UnprotectedPage {
    public AssertErrorPage(PageParameters parameters) {
        super(parameters);
        String msg = "This is a test exception";
        throw new RuntimeException(msg);
    }
}
