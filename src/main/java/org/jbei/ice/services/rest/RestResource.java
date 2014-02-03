package org.jbei.ice.services.rest;

import org.jbei.ice.lib.account.SessionHandler;

/**
 * Parent class for all rest resource object
 *
 * @author Hector Plahar
 */
public class RestResource {

    protected String getUserIdFromSessionHeader(String sessionHeader) {
        return SessionHandler.getUserIdBySession(sessionHeader);
    }

//    protected Response created() {
////        String href = (String)resource.get("href");
//        URI uri = URI.create(href);
//        return Response.created(uri).entity(resource).build();
//    }

}
