package org.jbei.ice.services.rest;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

/**
 * @author Hector Plahar
 */
@Provider
public class IceAuthenticationFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
//        requestContext.getHeaders();
//        String auth = requestContext.getHeaderString()

//        final SecurityContext securityContext = requestContext.getSecurityContext();
//
//        if (securityContext == null ||
//                !securityContext.isUserInRole("privileged")) {
//
//            requestContext.abortWith(Response
//                                             .status(Response.Status.UNAUTHORIZED)
//                                             .entity("User cannot access the resource.")
//                                             .build());
//        }
        /*
        //GET, POST, PUT, DELETE, ...
        String method = containerRequest.getMethod();
        // myresource/get/56bCA for example
        String path = containerRequest.getPath(true);

        //We do allow wadl to be retrieve
        if(method.equals("GET") && (path.equals("application.wadl") || path.equals("application.wadl/xsd0.xsd")){
            return containerRequest;
        }

        //Get the authentification passed in HTTP headers parameters
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

        return containerRequest;
         */
    }
}
