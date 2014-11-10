package org.jbei.ice.services.rest;

import java.util.ArrayList;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jbei.ice.lib.account.AccountTransfer;
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
    public Response getGroup(@PathParam("id") long id,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        String userId = getUserIdFromSessionHeader(sessionId);
        UserGroup group = groupController.getGroupById(userId, id);
        return respond(group);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/members")
    public Response getGroupMembers(@PathParam("id") long id,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        String userId = getUserIdFromSessionHeader(sessionId);
        ArrayList<AccountTransfer> members = groupController.getGroupMembers(userId, id);
        return respond(members);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response updateGroup(@PathParam("id") long id,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId,
            UserGroup group) {
        String userId = getUserIdFromSessionHeader(sessionId);
        boolean success = groupController.updateGroup(userId, group);
        return respond(success);
    }
}
