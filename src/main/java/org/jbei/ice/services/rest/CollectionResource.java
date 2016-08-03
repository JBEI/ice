package org.jbei.ice.services.rest;

import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.folder.collection.Collection;
import org.jbei.ice.lib.folder.collection.CollectionEntries;
import org.jbei.ice.lib.folder.collection.CollectionType;
import org.jbei.ice.lib.folder.collection.Collections;
import org.jbei.ice.lib.shared.ColumnField;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Collections represent system defined sets of parts.
 * These are:
 * <ul>
 * <li><code>Available</code></li>
 * <li><code>Personal</code></li>
 * <li><code>Shared</code></li>
 * <li><code>Drafts</code></li>
 * <li><code>Deleted</code></li>
 * <li><code>Transferred</code></li>
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
        try {
            Collections collections = new Collections(userId);
            return super.respond(collections.getSubFolders(type));
        } catch (IllegalArgumentException ile) {
            Logger.error(ile);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Retrieves the specified collection type's folder
     * Since the folder is intended to be retrieved from a
     * specified resource's context, this may not always return a result
     * even the folder with the specified id exists
     */
    @GET
    @Path("/{type}/folders/{id}")
    public Response getCollectionFolder(
            @PathParam("type") CollectionType type,
            @PathParam("id") long folderId) {
        String userId = getUserId();
        Collection collection = new Collection(userId, type);
        return super.respond(collection.getFolder(folderId, 0, 30));
    }

    /**
     * Retrieve entries by collection type using paging parameters, including a filter
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{type}/entries")
    public Response read(@PathParam("type") String collectionType,
                         @DefaultValue("0") @QueryParam("offset") final int offset,
                         @DefaultValue("15") @QueryParam("limit") final int limit,
                         @DefaultValue("created") @QueryParam("sort") final String sort,
                         @DefaultValue("false") @QueryParam("asc") final boolean asc,
                         @DefaultValue("") @QueryParam("filter") String filter,
                         @QueryParam("fields") List<String> queryParam) {
        try {
            CollectionType type = CollectionType.valueOf(collectionType.toUpperCase());
            ColumnField sortField = ColumnField.valueOf(sort.toUpperCase());

            String userId = requireUserId();
            log(userId, "retrieving entries for collection " + type);
            CollectionEntries entries = new CollectionEntries(userId, type);

            return super.respond(entries.getEntries(sortField, asc, offset, limit, filter));
        } catch (PermissionException pe) {
            return super.respond(Response.Status.FORBIDDEN);
        } catch (IllegalArgumentException ie) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }
}
