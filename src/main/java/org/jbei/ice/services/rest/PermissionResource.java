package org.jbei.ice.services.rest;

import java.util.ArrayList;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.dto.permission.AccessPermission;

/**
 * @author Hector Plahar
 */
@Path("/permission")
public class PermissionResource extends RestResource {

    private PermissionsController controller = new PermissionsController();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/autocomplete")
    public Response autoComplete(@QueryParam("val") String val,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
            @DefaultValue("8") @QueryParam("limit") int limit) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        ArrayList<AccessPermission> result = controller.getMatchingGroupsOrUsers(userId, val, limit);
        if (result == null)
            return super.respond(Response.Status.INTERNAL_SERVER_ERROR);
        return super.respond(Response.Status.OK, result);
    }
}
