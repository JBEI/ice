package org.jbei.ice.server.servlet;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.jbei.ice.lib.logging.Logger;

public class IceServletSessionListener implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        Logger.info("New Session created : " + se.getSession().getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        Logger.info("Session destroyed " + se.getSession().getId());
    }
}
