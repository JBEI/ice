package org.jbei.ice.web.pages;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.StringHeaderContributor;
import org.apache.wicket.protocol.http.WebRequestCycle;

public class PageExpiredPage extends UnprotectedPage {
    public PageExpiredPage(PageParameters parameters) {
        super(parameters);
        CharSequence relativeUrl = WebRequestCycle.get().urlFor(HomePage.class,
                new PageParameters());

        WebRequestCycle webRequestCycle = (WebRequestCycle) WebRequestCycle.get();

        HttpServletRequest httpServletRequest = webRequestCycle.getWebRequest()
                .getHttpServletRequest();

        String urlHeader = (httpServletRequest.isSecure()) ? "https://" : "http://";
        urlHeader = urlHeader + httpServletRequest.getRemoteHost() + ":"
                + httpServletRequest.getLocalPort() + "/";

        String homePageUrl = urlHeader + relativeUrl;

        String headerString = "<META HTTP-EQUIV=\"Refresh\" CONTENT=\"2; URL=" + homePageUrl
                + "\"> ";
        add(new StringHeaderContributor(headerString));
    }
}
