package org.jbei.ice.services.rest;

import org.jbei.ice.storage.hibernate.HibernateUtil;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Rolls back the transaction if http status is 500
 * otherwise commits transaction if started
 *
 * @author Hector Plahar
 */
@Provider
public class IceResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        // 401 = unauthorized (trans not started)
        // 500 = request failed
        // 204 = no content ???
        int status = responseContext.getStatus();
        if (status != 401) {
            if (status == 500)
                HibernateUtil.rollbackTransaction();
            else {
                try {
                    HibernateUtil.commitTransaction();
                } catch (Exception e) {
//                    Logger.error(e);
                    responseContext.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                    HibernateUtil.rollbackTransaction();
                }
            }
        }
    }
}
