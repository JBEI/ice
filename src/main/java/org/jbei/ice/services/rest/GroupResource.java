package org.jbei.ice.services.rest;

import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.dto.group.GroupType;
import org.jbei.ice.lib.dto.group.UserGroup;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.group.Groups;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/autocomplete")
    public Response matchGroupNames(@QueryParam("token") String token,
                                    @DefaultValue("8") @QueryParam("limit") int limit) {
        String userId = requireUserId();
        Groups groups = new Groups(userId);
        return super.respond(groups.getMatchingGroups(token, limit));
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
    public Response getGroup(@PathParam("id") long id) {
        String userId = requireUserId();
        UserGroup group = groupController.getGroupById(userId, id);
        return respond(group);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response deleteUserGroup(@PathParam("id") long groupId) {
        String userIdStr = requireUserId();
        log(userIdStr, "deleting group " + groupId);
        boolean success = groupController.deleteGroup(userIdStr, groupId);
        return super.respond(success);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/members")
    public Response getGroupMembers(@PathParam("id") long id) {
        String userId = requireUserId();
        Groups groups = new Groups(userId);
        return super.respond(groups.getGroupMembers(id));
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response updateGroup(@PathParam("id") long id, UserGroup group) {
        String userId = requireUserId();
        Groups groups = new Groups(userId);
        return respond(groups.update(id, group));
    }
}
