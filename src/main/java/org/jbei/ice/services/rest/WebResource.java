package org.jbei.ice.services.rest;

import java.util.ArrayList;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jbei.ice.lib.dto.entry.AttachmentInfo;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.PartStatistics;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.dto.web.WebEntries;
import org.jbei.ice.lib.dto.web.WebOfRegistries;
import org.jbei.ice.lib.entry.EntrySelection;
import org.jbei.ice.lib.net.RemoteAccessController;
import org.jbei.ice.lib.net.WoRController;
import org.jbei.ice.lib.vo.FeaturedDNASequence;

/**
 * @author Hector Plahar
 */
@Path("/web")
public class WebResource extends RestResource {

    private WoRController controller = new WoRController();
    private RemoteAccessController remoteAccessController = new RemoteAccessController();

    /**
     * Retrieves information on other ice instances that is in a web of registries configuration
     * with this instance; also know as registry partners
     *
     * @param approvedOnly
     *            if true, only instances that have been approved are returned; defaults to true
     * @return wrapper around the list of registry partners
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public WebOfRegistries query(
            @DefaultValue("true") @QueryParam("approved_only") final boolean approvedOnly) {
        getUserId(); // ensure valid session or auth header
        return controller.getRegistryPartners(approvedOnly);
    }

    /**
     * get public entries
     *
     * @param uriInfo
     * @param partnerId
     * @param offset
     * @param limit
     * @param sort
     * @param asc
     * @return Response with public entries from registry partners
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries")
    public Response getWebEntries(@Context final UriInfo uriInfo,
            @PathParam("id") final long partnerId,
            @DefaultValue("0") @QueryParam("offset") final int offset,
            @DefaultValue("15") @QueryParam("limit") final int limit,
            @DefaultValue("created") @QueryParam("sort") final String sort,
            @DefaultValue("false") @QueryParam("asc") final boolean asc) {
        final WebEntries result = remoteAccessController.getPublicEntries(partnerId, offset, limit,
                sort, asc);
        return super.respond(Response.Status.OK, result);
    }

    /**
     * @param partnerId
     * @param partId
     * @return attachment info on a registry partner entry
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries/{entryId}/attachments")
    public ArrayList<AttachmentInfo> getAttachments(@PathParam("id") final long partnerId,
            @PathParam("entryId") final long partId) {
        return remoteAccessController.getPublicEntryAttachments(partnerId, partId);
    }

    /**
     * @param remoteId
     * @param entrySelection
     * @return Response for success
     */
    @POST
    @Path("/{id}/transfer")
    public Response transferEntries(@PathParam("id") final long remoteId,
            final EntrySelection entrySelection) {
        final String userId = super.getUserId();
        remoteAccessController.transferEntries(userId, remoteId, entrySelection);
        return super.respond(Response.Status.OK);
    }

    /**
     * @param uriInfo
     * @param partnerId
     * @param entryId
     * @return Response with a specific entry for a registry partner
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries/{entryId}")
    public Response getWebEntry(@Context final UriInfo uriInfo,
            @PathParam("id") final long partnerId, @PathParam("entryId") final long entryId) {
        final PartData result = remoteAccessController.getPublicEntry(partnerId, entryId);
        return super.respond(Response.Status.OK, result);
    }

    /**
     * @param uriInfo
     * @param partnerId
     * @param entryId
     * @return Response with a specific entry tooltip for a registry partner
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries/{entryId}/tooltip")
    public Response getWebEntryTooltip(@Context final UriInfo uriInfo,
            @PathParam("id") final long partnerId, @PathParam("entryId") final long entryId) {
        final PartData result = remoteAccessController.getPublicEntryTooltip(partnerId, entryId);
        return super.respond(Response.Status.OK, result);
    }

    /**
     * @param partnerId
     * @param entryId
     * @return Response with statistics on a specific entry for a registry partner
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries/{entryId}/statistics")
    public Response getStatistics(@PathParam("id") final long partnerId,
            @PathParam("entryId") final long entryId) {
        final PartStatistics statistics = remoteAccessController.getPublicEntryStatistics(
                partnerId, entryId);
        return super.respond(statistics);
    }

    /**
     * @param uriInfo
     * @param partnerId
     * @param entryId
     * @return Response with a sequence on a registry partner entry
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries/{entryId}/sequence")
    public Response getWebEntrySequence(@Context final UriInfo uriInfo,
            @PathParam("id") final long partnerId, @PathParam("entryId") final long entryId) {
        final FeaturedDNASequence result = remoteAccessController.getPublicEntrySequence(partnerId,
                entryId);
        return super.respond(Response.Status.OK, result);
    }

    /**
     * @param info
     * @param partner
     * @return Response with an added registry partner
     */
    @POST
    @Path("/partner")
    // admin function
    public Response addWebPartner(@Context final UriInfo info, final RegistryPartner partner) {
        final String userId = getUserId();
        final RegistryPartner registryPartner = controller.addWebPartner(userId, partner);
        return respond(Response.Status.OK, registryPartner);
    }

    /**
     * @param info
     * @param partnerId
     * @return Response with registry partner info
     */
    @GET
    @Path("/partner/{id}")
    public Response getWebPartner(@Context final UriInfo info, @PathParam("id") final long partnerId) {
        final String userId = getUserId();
        final RegistryPartner partner = controller.getWebPartner(userId, partnerId);
        return super.respond(Response.Status.OK, partner);
    }

    /**
     * @param partner
     * @return Response for success or failure
     */
    @POST
    @Path("/partner/remote")
    public Response remoteWebPartnerRequest(final RegistryPartner partner) {
        if (controller.addRemoteWebPartner(partner)) {
            return respond(Response.Status.OK);
        }
        return respond(Response.Status.INTERNAL_SERVER_ERROR);
    }

    /**
     * @param url
     * @param partner
     * @return Response for success or failure
     */
    @PUT
    @Path("/partner/{url}")
    public Response updateWebPartner(@PathParam("url") final String url,
            final RegistryPartner partner) {
        final String userId = getUserId();
        if (controller.updateWebPartner(userId, url, partner)) {
            return respond(Response.Status.OK);
        }
        return respond(Response.Status.INTERNAL_SERVER_ERROR);
    }

    /**
     * @param url
     * @return Response for success or failure
     */
    @DELETE
    @Path("/partner/{url}")
    public Response removeWebPartner(@PathParam("url") final String url) {
        final String userId = getUserId();
        if (controller.removeWebPartner(userId, url)) {
            return respond(Response.Status.OK);
        }
        return respond(Response.Status.INTERNAL_SERVER_ERROR);
    }
}
