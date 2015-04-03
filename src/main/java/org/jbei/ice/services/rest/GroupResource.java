package org.jbei.ice.services.rest;

import java.util.ArrayList;

import javax.ws.rs.GET;
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

    /**
     * @param id
     * @return Response with group info
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response getGroup(@PathParam("id") final long id) {
        final String userId = getUserId();
        final UserGroup group = groupController.getGroupById(userId, id);
        return respond(group);
    }

    /**
     * @param id
     * @return Response with group members
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/members")
    public Response getGroupMembers(@PathParam("id") final long id) {
        final String userId = getUserId();
        final ArrayList<AccountTransfer> members = groupController.getGroupMembers(userId, id);
        return respond(members);
    }

    /**
     * @param id
     * @param group
     * @return response with success or failure
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response updateGroup(@PathParam("id") final long id, final UserGroup group) {
        final String userId = getUserId();
        final boolean success = groupController.updateGroup(userId, group);
        return respond(success);
    }
}
