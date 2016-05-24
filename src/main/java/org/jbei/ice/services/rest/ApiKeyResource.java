package org.jbei.ice.services.rest;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.account.UserApiKeys;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
        return super.respond(apiKeys.requestKey(clientId));
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
