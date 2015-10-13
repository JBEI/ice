package org.jbei.ice.servlet.filter;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.hibernate.HibernateUtil;

import javax.servlet.*;
import java.io.IOException;

/**
 * @author Hector Plahar
 */
public class IceFilter implements Filter {
    private FilterConfig config;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        config = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (config == null)
            return;

        try {
            HibernateUtil.beginTransaction();
            chain.doFilter(request, response);
            HibernateUtil.commitTransaction();
        } catch (Throwable t) {
            HibernateUtil.rollbackTransaction();
            try {
                HibernateUtil.beginTransaction();
                Logger.error(t);
                HibernateUtil.commitTransaction();
            } catch (Throwable e) {
                HibernateUtil.rollbackTransaction();
                Logger.warn("Could not log error " + e.getMessage());
            }
        }
    }

    @Override
    public void destroy() {
    }
}
