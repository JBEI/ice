package org.jbei.ice.web;

import org.apache.wicket.Page;
import org.apache.wicket.Response;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.web.pages.ErrorPage;

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
		Logger.error(Utils.stackTraceToString(e));

		return new ErrorPage(e);
	}
}
