package org.jbei.ice.web;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.Response;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.jbei.ice.lib.logging.Logger;
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
        Logger.debug(request.getURL());
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
