package org.jbei.ice.services.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jbei.ice.lib.dto.permission.RemoteAccessPermission;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.dto.web.WebEntries;
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

    /**
     * Retrieves information on other ice instances that is in a web of registries
     * configuration with this instance; also know as registry partners
     *
     * @param approvedOnly    if true, only instances that have been approved are returned; defaults to true
     * @param userAgentHeader session if for user
     * @return wrapper around the list of registry partners
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public WebOfRegistries query(
            @DefaultValue("true") @QueryParam("approved_only") boolean approvedOnly,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        return controller.getRegistryPartners(approvedOnly);
    }

    // get public entries
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries")
    public Response getWebEntries(@Context UriInfo uriInfo,
            @PathParam("id") long partnerId,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("15") @QueryParam("limit") int limit,
            @DefaultValue("created") @QueryParam("sort") String sort,
            @DefaultValue("false") @QueryParam("asc") boolean asc,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        WebEntries result = remoteAccessController.getPublicEntries(partnerId, offset, limit, sort, asc);
        return super.respond(Response.Status.OK, result);
    }

    @POST
    @Path("/partner")
    // admin function
    public Response addWebPartner(@Context UriInfo info,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
            RegistryPartner partner) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        RegistryPartner registryPartner = controller.addWebPartner(userId, partner);
        if (registryPartner != null)
            return respond(Response.Status.OK, registryPartner);
        return respond(Response.Status.INTERNAL_SERVER_ERROR);
    }

    @POST
    @Path("/partner/remote")
    public Response remoteWebPartnerRequest(RegistryPartner partner) {
        controller.addRemoteWebPartner(partner);
        return respond(Response.Status.OK);
    }

    @PUT
    @Path("/partner/{url}")
    public Response updateWebPartner(
            @PathParam("url") String url, RegistryPartner partner,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        controller.updateWebPartner(userId, url, partner);
        return respond(Response.Status.OK);
    }

    @DELETE
    @Path("/partner/{url}")
    public Response removeWebPartner(
            @PathParam("url") String url,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        controller.removeWebPartner(userId, url);
        return respond(Response.Status.OK);
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
