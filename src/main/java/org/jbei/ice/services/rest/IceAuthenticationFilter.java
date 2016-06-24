package org.jbei.ice.services.rest;

import org.glassfish.jersey.server.ContainerRequest;
import org.jbei.ice.storage.hibernate.HibernateUtil;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * @author Hector Plahar
 */
@Provider
public class IceAuthenticationFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        ContainerRequest request = (ContainerRequest) requestContext;
        String path = request.getPath(true);
        String method = request.getMethod();

//        if (needsSessionId(path, method)) {
        String auth = requestContext.getHeaderString(Headers.AUTHENTICATION_PARAM_NAME);
//            if (auth == null) {
//                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
//                                                 .entity("User cannot access the resource.")
//                                                 .build());
//            }
//        }

        HibernateUtil.beginTransaction();
    }
}
