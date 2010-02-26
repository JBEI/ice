package org.jbei.ice.web;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.Response;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.hibernate.Session;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.logging.UsageLogger;
import org.jbei.ice.lib.managers.HibernateHelper;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.web.pages.ErrorPage;
import org.jbei.ice.web.pages.HomePage;
import org.jbei.ice.web.pages.PermissionDeniedPage;

public class IceRequestCycle extends WebRequestCycle {
    public IceRequestCycle(WebApplication application, WebRequest request, Response response) {
        super(application, request, response);
    }

    @Override
    protected void onBeginRequest() {
        Session session = HibernateHelper.getSession();
        Account account = IceSession.get().getAccount();
        HttpServletRequest httpServletRequest = ((WebRequest) get().getRequest())
                .getHttpServletRequest();

        String user = httpServletRequest.getRemoteAddr();
        String userAgent = httpServletRequest.getHeader("User-Agent");

        if (account != null) {
            user = account.getEmail();
        }

        String urlInfo = request.getURL();
        if (urlInfo.startsWith("resources")) {

        } else {
            String msg = user + "\t" + urlInfo + "\t" + userAgent;
            UsageLogger.info(msg);
        }

    }

    @Override
    protected void onEndRequest() {
        Session session = HibernateHelper.getSession();
        session.close();
    }

    @Override
    public Page onRuntimeException(Page page, RuntimeException e) {
        Page result = null;
        if (e instanceof PermissionException) {
            String msg = "Permission violation: " + e.toString();
            Logger.warn(msg);
            result = new PermissionDeniedPage(new PageParameters());
        } else if (e instanceof PageExpiredException) {
            // If Page is expired due to forms going stale, etc,
            // drop the users to their home page
            result = new HomePage(new PageParameters());
        } else {
            Logger.error(Utils.stackTraceToString(e));
            result = new ErrorPage(e);
        }
        return result;
    }
}
