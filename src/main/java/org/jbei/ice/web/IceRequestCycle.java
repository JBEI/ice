package org.jbei.ice.web;

import java.lang.reflect.InvocationTargetException;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.Response;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.logging.UsageLogger;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.common.ViewPermissionException;
import org.jbei.ice.web.pages.ErrorPage;
import org.jbei.ice.web.pages.PageExpiredPage;
import org.jbei.ice.web.pages.PermissionDeniedPage;

/**
 * Custom wicket {@link WebRequestCycle} for gd-ice.
 * 
 * @author Zinovii Dmytriv, Timothy Ham
 * 
 */
public class IceRequestCycle extends WebRequestCycle {
    /**
     * Constructor.
     * 
     * @param application
     * @param request
     * @param response
     */
    public IceRequestCycle(WebApplication application, WebRequest request, Response response) {
        super(application, request, response);
    }

    /**
     * Called when the request cycle object is beginning its response.
     * <p>
     * Also logs the request to the usage log.
     */
    @Override
    protected void onBeginRequest() {
        Account account = IceSession.get().getAccount();
        HttpServletRequest httpServletRequest = ((WebRequest) get().getRequest())
                .getHttpServletRequest();

        String user = httpServletRequest.getRemoteAddr();
        //String userAgent = httpServletRequest.getHeader("User-Agent");

        if (account != null) {
            user = account.getEmail();
        }

        String urlInfo = request.getURL();
        if (!urlInfo.startsWith("resources")) {
            //String msg = user + "\t" + urlInfo + "\t" + userAgent;
            String msg = user + "\t" + urlInfo;
            UsageLogger.info(msg);
        }
    }

    /**
     * Handle runtime exception. Generate exception pages (Permission denied, Page expired, or an
     * Error page).
     * <p>
     * Also logs to the logfile.
     */
    @Override
    public Page onRuntimeException(Page page, RuntimeException e) {
        Page result = null;

        if (e instanceof ViewPermissionException) {
            String msg = "Permission violation: " + e.getMessage();

            Logger.warn(msg);

            result = new PermissionDeniedPage(new PageParameters("0=" + msg));
        } else if (e.getCause() instanceof InvocationTargetException
                && ((InvocationTargetException) e.getCause()).getTargetException() != null
                && ((InvocationTargetException) e.getCause()).getTargetException() instanceof ViewPermissionException) {
            String msg = "Permission violation: "
                    + ((InvocationTargetException) e.getCause()).getTargetException().getMessage();

            Logger.warn(msg);

            result = new PermissionDeniedPage(new PageParameters("0=" + msg));
        } else if (e instanceof ViewException) {
            Logger.error("Unexpected Error", e);

            result = new ErrorPage(e);
        } else if (e instanceof PageExpiredException) {
            result = new PageExpiredPage(new PageParameters());
        } else {
            Logger.error("Unknown Error", e);

            result = new ErrorPage(e);
        }

        return result;
    }
}
