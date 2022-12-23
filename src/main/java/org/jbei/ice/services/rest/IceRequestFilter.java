package org.jbei.ice.services.rest;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.jbei.ice.storage.hibernate.HibernateConfiguration;

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
    public void filter(ContainerRequestContext requestContext) {
        HibernateConfiguration.beginTransaction();
    }
}
