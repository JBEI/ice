package org.jbei.ice.services.rest;

import java.util.ArrayList;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.jbei.ice.lib.dto.group.UserGroup;
import org.jbei.ice.lib.group.GroupController;

/**
 * @author Hector Plahar
 */
@Path("/groups")
public class GroupResource extends RestResource {

    private GroupController groupController = new GroupController();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public ArrayList<UserGroup> getUserGroups(@Context UriInfo info, @PathParam("id") long userId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userIdString = getUserIdFromSessionHeader(userAgentHeader);
        return groupController.retrieveUserGroups(userIdString, userId, false);
    }
}
