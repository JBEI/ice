package org.jbei.ice.services.rest;

import org.jbei.ice.lib.dto.entry.AttachmentInfo;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.PartStatistics;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.dto.web.WebEntries;
import org.jbei.ice.lib.entry.EntrySelection;
import org.jbei.ice.lib.net.RemoteAccessController;
import org.jbei.ice.lib.net.RemoteContact;
import org.jbei.ice.lib.net.WoRController;
import org.jbei.ice.lib.vo.FeaturedDNASequence;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;

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
     * @param approvedOnly    if true (default), only instances that have been approved are returned
     * @param userAgentHeader session id for user logged in user
     * @return wrapper around the list of registry partners
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response query(
            @DefaultValue("true") @QueryParam("approved_only") boolean approvedOnly,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        getUserIdFromSessionHeader(userAgentHeader);
        return super.respond(controller.getRegistryPartners(approvedOnly));
    }

    /**
     * @param uriInfo
     * @param partnerId
     * @param offset
     * @param limit
     * @param sort
     * @param asc
     * @param sessionId
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries")
    public Response getWebEntries(@Context UriInfo uriInfo,
            @PathParam("id") long partnerId,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("15") @QueryParam("limit") int limit,
            @DefaultValue("created") @QueryParam("sort") String sort,
            @DefaultValue("false") @QueryParam("asc") boolean asc,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        WebEntries result = remoteAccessController.getPublicEntries(partnerId, offset, limit, sort, asc);
        return super.respond(Response.Status.OK, result);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries/{entryId}/attachments")
    public ArrayList<AttachmentInfo> getAttachments(@PathParam("id") long partnerId,
            @PathParam("entryId") long partId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        getUserIdFromSessionHeader(userAgentHeader);
        return remoteAccessController.getPublicEntryAttachments(partnerId, partId);
    }

    @POST
    @Path("/{id}/transfer")
    public Response transferEntries(
            @PathParam("id") long remoteId,
            EntrySelection entrySelection,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        String userId = super.getUserIdFromSessionHeader(sessionId);
        remoteAccessController.transferEntries(userId, remoteId, entrySelection);
        return super.respond(Response.Status.OK);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries/{entryId}")
    public Response getWebEntry(@Context UriInfo uriInfo,
            @PathParam("id") long partnerId,
            @PathParam("entryId") long entryId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        PartData result = remoteAccessController.getPublicEntry(partnerId, entryId);
        return super.respond(Response.Status.OK, result);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries/{entryId}/tooltip")
    public Response getWebEntryTooltip(@Context UriInfo uriInfo,
            @PathParam("id") long partnerId,
            @PathParam("entryId") long entryId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        PartData result = remoteAccessController.getPublicEntryTooltip(partnerId, entryId);
        return super.respond(Response.Status.OK, result);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries/{entryId}/statistics")
    public Response getStatistics(@PathParam("id") long partnerId,
            @PathParam("entryId") long entryId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        PartStatistics statistics = remoteAccessController.getPublicEntryStatistics(partnerId, entryId);
        return super.respond(statistics);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries/{entryId}/sequence")
    public Response getWebEntrySequence(@Context UriInfo uriInfo,
            @PathParam("id") long partnerId,
            @PathParam("entryId") long entryId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        FeaturedDNASequence result = remoteAccessController.getPublicEntrySequence(partnerId, entryId);
        return super.respond(Response.Status.OK, result);
    }

    @POST
    @Path("/partner")
    // admin function
    public Response addWebPartner(@Context UriInfo info,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId,
            RegistryPartner partner) {
        String userId = getUserIdFromSessionHeader(sessionId);
        RemoteContact contactRemote = new RemoteContact();
        RegistryPartner registryPartner = contactRemote.addWebPartner(userId, partner);
        return respond(Response.Status.OK, registryPartner);
    }

    @GET
    @Path("/partner/{id}")
    public Response getWebPartner(@Context UriInfo info,
            @PathParam("id") long partnerId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        String userId = getUserIdFromSessionHeader(sessionId);
        RegistryPartner partner = controller.getWebPartner(userId, partnerId);
        return super.respond(Response.Status.OK, partner);
    }

    @POST
    @Path("/partner/remote")
    public Response remoteWebPartnerRequest(RegistryPartner partner) {
        RemoteContact remoteContact = new RemoteContact();
        if (remoteContact.handleRemoteAddRequest(partner))
            return respond(Response.Status.OK);
        return respond(Response.Status.INTERNAL_SERVER_ERROR);
    }

    @PUT
    @Path("/partner/{url}")
    public Response updateWebPartner(
            @PathParam("url") String url, RegistryPartner partner,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        String userId = getUserIdFromSessionHeader(sessionId);
        if (controller.updateWebPartner(userId, url, partner))
            return respond(Response.Status.OK);
        return respond(Response.Status.INTERNAL_SERVER_ERROR);
    }

    @DELETE
    @Path("/partner/{url}")
    public Response removeWebPartner(
            @PathParam("url") String url,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        String userId = getUserIdFromSessionHeader(sessionId);
        if (controller.removeWebPartner(userId, url))
            return respond(Response.Status.OK);
        return respond(Response.Status.INTERNAL_SERVER_ERROR);
    }
}
