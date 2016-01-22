package org.jbei.ice.services.rest;

import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.dto.group.GroupType;
import org.jbei.ice.lib.dto.group.UserGroup;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.group.Groups;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

/**
 * REST resource for groups
 *
 * @author Hector Plahar
 */
@Path("/groups")
public class GroupResource extends RestResource {

    private GroupController groupController = new GroupController();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroups(
            @DefaultValue("PRIVATE") @QueryParam("type") String type,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit) {
        String userId = getUserId();
        GroupType groupType = GroupType.valueOf(type.toUpperCase());
        Groups groups = new Groups(userId);
        return super.respond(groups.get(groupType, offset, limit));
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createGroup(UserGroup userGroup) {
        try {
            String userId = requireUserId();
            Groups groups = new Groups(userId);
            return super.respond(groups.addGroup(userGroup));
        } catch (PermissionException pe) {
            return super.respond(Response.Status.FORBIDDEN);
        }
    }

    /**
     * Retrieve specified group
     *
     * @param id unique identifier for group to be retrieved
     * @return found group
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
