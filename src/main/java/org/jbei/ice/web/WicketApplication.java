package org.jbei.ice.web;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.request.WebExternalResourceRequestTarget;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.basic.URIRequestTargetUrlCodingStrategy;
import org.jbei.Start;
import org.jbei.ice.web.pages.HomePage;
import org.jbei.ice.web.pages.LoginPage;
import org.jbei.ice.web.pages.RegisterPage;

/**
 * Application object for your web application. If you want to run this application without deploying, run the Start class.
 * 
 * @see org.jbei.Start#main(String[])
 */
public class WicketApplication extends WebApplication
{    
    /**
     * Constructor
     */
	public WicketApplication()
	{
	}
	
	protected void init() {
		mountBookmarkablePage("/login", LoginPage.class);
		mountBookmarkablePage("/register", RegisterPage.class);
		mount(new URIRequestTargetUrlCodingStrategy("/static")
        {
			@Override
            public IRequestTarget decode(RequestParameters requestParameters)
            {
                String path = "/static/" + getURI(requestParameters);
                return new WebExternalResourceRequestTarget(path);
            }
        });
	}
	
	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	public Class<HomePage> getHomePage()
	{
		return HomePage.class;
	}

}
