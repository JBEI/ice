package org.jbei.ice.services.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.access.PermissionException;
import org.jbei.ice.account.UserApiKeys;
import org.jbei.ice.dto.access.AccessKey;

/**
 * REST Resource for api-keys that enable third-party application access to ice
 *
 * @author Hector Plahar
 */
@Path("/api-keys")
public class ApiKeyResource extends RestResource {

    /**
     * creates an api-key for the specified client id
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createApiKey(@QueryParam("client_id") String clientId) {
        String userId = requireUserId();
        if (StringUtils.isEmpty(clientId))
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        log(userId, "creating api key for client: " + clientId);
        UserApiKeys apiKeys = new UserApiKeys(userId);
        AccessKey key = apiKeys.requestKey(clientId);
        if (key == null)
            throw new WebApplicationException("Could not create api key with client id " + clientId);
        return super.respond(key);
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAPIKey(@PathParam("id") long id, AccessKey key) {
        String userId = requireUserId();
        log(userId, "updating api key " + id);
        UserApiKeys apiKeys = new UserApiKeys(userId);
        return super.respond(apiKeys.update(id, key));
    }

    /**
     * retrieves list of api keys created by user
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApiKeys(@DefaultValue("0") @QueryParam("offset") int offset,
                               @DefaultValue("15") @QueryParam("limit") int limit,
                               @DefaultValue("true") @QueryParam("asc") boolean asc,
                               @DefaultValue("creationTime") @QueryParam("sort") String sort,
                               @DefaultValue("false") @QueryParam("getAll") boolean getAll) {
        String userId = requireUserId();
        UserApiKeys apiKeys = new UserApiKeys(userId);
        try {
            return super.respond(apiKeys.getKeys(limit, offset, sort, asc, getAll));
        } catch (PermissionException pe) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
    }

    /**
     * Delete a specified API key
     *
     * @param secret
     * @param clientId
     * @param id
     * @return
     */
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteApiKey(@QueryParam("secret") String secret,
                                 @QueryParam("clientId") String clientId,
                                 @PathParam("id") long id) {
        String userId = requireUserId();
        UserApiKeys apiKeys = new UserApiKeys(userId);
        return super.respond(apiKeys.deleteKey(id, secret));
    }
}
