package org.jbei.ice.server.servlet.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;

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
            HibernateHelper.beginTransaction();
            chain.doFilter(request, response);
            HibernateHelper.commitTransaction();
        } catch (Throwable t) {
            HibernateHelper.rollbackTransaction();
            try {
                HibernateHelper.beginTransaction();
                Logger.error(t);
                HibernateHelper.commitTransaction();
            } catch (Throwable e) {
                HibernateHelper.rollbackTransaction();
                Logger.warn("Could not log error " + e.getMessage());
            }
        }
    }

    @Override
    public void destroy() {
    }
}
