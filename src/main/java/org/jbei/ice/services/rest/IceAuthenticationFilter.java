package org.jbei.ice.services.rest;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.ContainerRequest;

/**
 * @author Hector Plahar
 */
@Provider
public class IceAuthenticationFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        final SecurityContext securityContext = requestContext.getSecurityContext();
        if (!securityContext.isSecure()) {
            requestContext.abortWith(Response.status(Response.Status.EXPECTATION_FAILED)
                                             .entity("HTTPS only supported")
                                             .build());
        }

        ContainerRequest request = (ContainerRequest) requestContext;
        String path = request.getPath(true);
        String method = request.getMethod();

        if (("PUT".equals(method) || "POST".equals(method)) && (path.equals("/accesstoken") || path.equals
                ("/profile")))
            return;

        String auth = requestContext.getHeaderString("X-ICE-Authentication-SessionId");
        if (auth == null) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                                             .entity("User cannot access the resource.")
                                             .build());
        }

        /*
        String auth = containerRequest.getHeaderValue("authorization");

        //If the user does not have the right (does not provide any HTTP Basic Auth)
        if(auth == null){
            throw new WebApplicationException(Status.UNAUTHORIZED);
        }

        //lap : loginAndPassword
        String[] lap = BasicAuth.decode(auth);

        //If login or password fail
        if(lap == null || lap.length != 2){
            throw new WebApplicationException(Status.UNAUTHORIZED);
        }

        //DO YOUR DATABASE CHECK HERE (replace that line behind)...
        User authentificationResult =  AuthentificationThirdParty.authentification(lap[0], lap[1]);

        //Our system refuse login and password
        if(authentificationResult == null){
            throw new WebApplicationException(Status.UNAUTHORIZED);
        }

        //TODO : HERE YOU SHOULD ADD PARAMETER TO REQUEST, TO REMEMBER USER ON YOUR REST SERVICE...
         */
    }
}
