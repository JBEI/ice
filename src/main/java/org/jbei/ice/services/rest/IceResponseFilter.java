package org.jbei.ice.services.rest;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.hibernate.HibernateConfiguration;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

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
        int status = responseContext.getStatus();
        if (status != 401) {
            if (status == 500)
                HibernateConfiguration.rollbackTransaction();
            else {
                try {
                    HibernateConfiguration.commitTransaction();
                } catch (Throwable e) {
                    Logger.error(e);
                    responseContext.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                    HibernateConfiguration.rollbackTransaction();
                }
            }
        }
    }
}
