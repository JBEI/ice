package org.jbei.ice.services.rest;

import org.jbei.ice.lib.dto.entry.AttachmentInfo;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.PartStatistics;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.dto.web.WebEntries;
import org.jbei.ice.lib.entry.EntrySelection;
import org.jbei.ice.lib.net.RemoteContact;
import org.jbei.ice.lib.net.RemoteEntries;
import org.jbei.ice.lib.net.WoRController;
import org.jbei.ice.lib.vo.FeaturedDNASequence;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;

/**
 * Resource for web of registries requests
 *
 * @author Hector Plahar
 */
@Path("/web")
public class WebResource extends RestResource {

    private final WoRController controller = new WoRController();
    private final RemoteEntries remoteEntries = new RemoteEntries();

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
     * Retrieves entries for specified partner using the specified paging parameters
     * @param partnerId unique identifier for registry partner whose entries are being retrieved
     * @param offset  record retrieve offset paging parameter
     * @param limit maximum number of entries to retrieve
     * @param sort field to sort on
     * @param asc sort order
     * @param sessionId unique identifier for user making request
     * @return <code>OK</code> HTTP status with the list of entries wrapped in a result object
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries")
    public Response getWebEntries(
            @PathParam("id") long partnerId,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("15") @QueryParam("limit") int limit,
            @DefaultValue("created") @QueryParam("sort") String sort,
            @DefaultValue("false") @QueryParam("asc") boolean asc,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        String userId = getUserIdFromSessionHeader(sessionId);
        WebEntries result = remoteEntries.getPublicEntries(userId, partnerId, offset, limit, sort, asc);
        return super.respond(Response.Status.OK, result);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries/{entryId}/attachments")
    public ArrayList<AttachmentInfo> getAttachments(
            @PathParam("id") long partnerId,
            @PathParam("entryId") long partId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        return remoteEntries.getEntryAttachments(userId, partnerId, partId);
    }

    @POST
    @Path("/{id}/transfer")
    public Response transferEntries(
            @PathParam("id") long remoteId,
            EntrySelection entrySelection,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        String userId = super.getUserIdFromSessionHeader(sessionId);
        remoteEntries.transferEntries(userId, remoteId, entrySelection);
        return super.respond(Response.Status.OK);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries/{entryId}")
    public Response getWebEntry(@Context UriInfo uriInfo,
                                @PathParam("id") long partnerId,
                                @PathParam("entryId") long entryId,
                                @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        String userId = super.getUserIdFromSessionHeader(sessionId);
        PartData result = remoteEntries.getPublicEntry(userId, partnerId, entryId);
        return super.respond(Response.Status.OK, result);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries/{entryId}/tooltip")
    public Response getWebEntryTooltip(@Context UriInfo uriInfo,
                                       @PathParam("id") long partnerId,
                                       @PathParam("entryId") long entryId,
                                       @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        String userId = super.getUserIdFromSessionHeader(sessionId);
        PartData result = remoteEntries.getPublicEntryTooltip(userId, partnerId, entryId);
        return super.respond(Response.Status.OK, result);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries/{entryId}/statistics")
    public Response getStatistics(@PathParam("id") long partnerId,
                                  @PathParam("entryId") long entryId,
                                  @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        String userId = super.getUserIdFromSessionHeader(sessionId);
        PartStatistics statistics = remoteEntries.getPublicEntryStatistics(userId, partnerId, entryId);
        return super.respond(statistics);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries/{entryId}/sequence")
    public Response getWebEntrySequence(@Context UriInfo uriInfo,
                                        @PathParam("id") long partnerId,
                                        @PathParam("entryId") long entryId,
                                        @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        String userId = super.getUserIdFromSessionHeader(sessionId);
        FeaturedDNASequence result = remoteEntries.getPublicEntrySequence(userId, partnerId, entryId);
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
