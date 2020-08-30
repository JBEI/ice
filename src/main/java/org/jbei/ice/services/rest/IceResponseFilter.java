package org.jbei.ice.services.rest;

import org.jbei.ice.storage.hibernate.HibernateUtil;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
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
        if (responseContext.getStatus() == 500) {
            HibernateUtil.rollbackTransaction();
        } else {
            HibernateUtil.commitTransaction();
        }
    }
}
