package org.jbei.ice.services.rest;

import org.jbei.ice.lib.collection.Collections;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Collections represent system defined sets of parts. These are <code>Available</code>,
 * <code>Personal</code>, <code>Shared</code>, <code>Drafts</code> and <code>Deleted</code>
 *
 * @author Hector Plahar
 */
@Path("/collections")
public class CollectionResource extends RestResource {

    /**
     * Retrieve the statistics (counts) of all the collections for the specified user
     */
    @GET
    @Path("/stats")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCollectionStats(
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        String userId = getUserId(sessionId);
        Collections collections = new Collections(userId);
        return super.respond(collections.getAllCounts());
    }
}
