package org.jbei.ice.services.rest;

import org.jbei.ice.lib.collection.CollectionType;
import org.jbei.ice.lib.collection.Collections;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Collections represent system defined sets of parts.
 * These are:
 * <ul>
 * <li><code>Available</code></li>
 * <li><code>Personal</code></li>
 * <li><code>Shared</code></li>
 * <li><code>Drafts</code></li>
 * <li><code>Deleted</code></li>
 * </ul>
 * <p>
 * These cannot be created or deleted by users
 *
 * @author Hector Plahar
 */
@Path("/collections")
public class CollectionResource extends RestResource {

    /**
     * Retrieve the statistics (counts) of all the collections for the specified user
     */
    @GET
    @Path("/counts")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCollectionStats() {
        String userId = super.requireUserId();
        Collections collections = new Collections(userId);
        return super.respond(collections.getAllCounts());
    }

    /**
     * @return all folders found under a collection of specified type
     */
    @GET
    @Path("/{type}/folders")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCollectionSubFolders(
            @DefaultValue("PERSONAL") @PathParam("type") CollectionType type) {
        final String userId = super.requireUserId();
        Collections collections = new Collections(userId);
        return super.respond(collections.getSubFolders(type));
    }
}
