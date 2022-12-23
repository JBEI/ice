package org.jbei.ice.services.rest;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.jbei.ice.storage.hibernate.HibernateConfiguration;

/**
 * Rolls back the transaction if http status is 500
 * otherwise commits transaction if started
 *
 * @author Hector Plahar
 */
@Provider
public class IceResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        if (responseContext.getStatus() == 500) {
            HibernateConfiguration.rollbackTransaction();
        } else {
            HibernateConfiguration.commitTransaction();
        }
    }
}
