package org.jbei.ice.services.rest;

import org.jbei.ice.storage.hibernate.HibernateUtil;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Request filter which begins transaction for request. This is the main filter
 * for all requests from the servlet container. {@see IceResponseFilter} for response filter which also
 * contains transaction close/commit
 *
 * @author Hector Plahar
 */
@Provider
public class IceRequestFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        HibernateUtil.beginTransaction();
    }
}
