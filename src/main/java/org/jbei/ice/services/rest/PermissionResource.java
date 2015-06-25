package org.jbei.ice.services.rest;

import java.util.ArrayList;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
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

    /**
     * @param val
     * @param limit
     * @return matching groups and users for autocomplete widget
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/autocomplete")
    public Response autoComplete(@QueryParam("val") final String val,
            @DefaultValue("8") @QueryParam("limit") final int limit) {
        final String userId = getUserId();
        final ArrayList<AccessPermission> result = controller.getMatchingGroupsOrUsers(userId, val,
                limit);
        if (result == null) {
            return super.respond(Response.Status.INTERNAL_SERVER_ERROR);
        }
        return super.respond(Response.Status.OK, result);
    }
}
