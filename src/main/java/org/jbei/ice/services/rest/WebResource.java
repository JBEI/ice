package org.jbei.ice.services.rest;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jbei.ice.lib.dto.permission.RemoteAccessPermission;
import org.jbei.ice.lib.dto.web.WebOfRegistries;
import org.jbei.ice.lib.net.RemoteAccessController;
import org.jbei.ice.lib.net.WoRController;

/**
 * @author Hector Plahar
 */
@Path("/web")
public class WebResource extends RestResource {

    private WoRController controller = new WoRController();
    private RemoteAccessController remoteAccessController = new RemoteAccessController();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public WebOfRegistries query(
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        return controller.getRegistryPartners();
    }

    @PUT
    public Response addPartToWeb(@HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        return Response.ok().build();
    }

    @PUT
    @Path("/permissions") // from ui
    public Response addRemotePermission(RemoteAccessPermission permission,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
//        remoteAccessController.addPermission(permission);
        return Response.ok().build();
    }

    @PUT
    @Path("/permissions/{userId}/remote") // from other registry instance that is attempting to share with you
    public Response addRemotePermissionFromPartner(
            @PathParam("userId") String userId, // share recipient, must exist on this server
            RemoteAccessPermission permission) {
//        remoteAccessController.addPermission(permission);
        return Response.ok().build();
    }
}
