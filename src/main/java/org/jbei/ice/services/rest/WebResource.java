package org.jbei.ice.services.rest;

import org.jbei.ice.dto.ConfigurationKey;
import org.jbei.ice.dto.FeaturedDNASequence;
import org.jbei.ice.dto.Setting;
import org.jbei.ice.dto.entry.AttachmentInfo;
import org.jbei.ice.dto.entry.PartData;
import org.jbei.ice.dto.entry.PartStatistics;
import org.jbei.ice.dto.search.SearchQuery;
import org.jbei.ice.dto.web.RegistryPartner;
import org.jbei.ice.dto.web.WebEntries;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.net.RemoteContact;
import org.jbei.ice.net.RemoteEntries;
import org.jbei.ice.net.RemoteEntriesAsCSV;
import org.jbei.ice.net.WoRController;
import org.jbei.ice.search.WebSearch;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.model.ConfigurationModel;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.List;

/**
 * Resource for web of registries requests
 * <p>
 * This is particularly useful for third party tools to tap into the web of
 * registries functionality without having specific API keys to each of the instances
 *
 * @author Hector Plahar
 */
@Path("/web")
public class WebResource extends RestResource {

    private final WoRController controller = new WoRController();
    private final RemoteEntries remoteEntries = new RemoteEntries();

    /**
     * Retrieves information on other ice instances that is in a web of registries configuration
     * with this instance; also know as registry partners
     *
     * @param approvedOnly if true, only instances that have been approved are returned; defaults to true
     * @return wrapper around the list of registry partners
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response query(@DefaultValue("true") @QueryParam("approved_only") boolean approvedOnly) {
        getUserId(); // ensure valid session or auth header
        return super.respond(controller.getRegistryPartners(approvedOnly));
    }

//    /**
//     * Retrieves entries for specified partner using the specified paging parameters
//     *
//     * @param partnerId unique identifier for registry partner whose entries are being retrieved
//     * @param offset    record retrieve offset paging parameter
//     * @param limit     maximum number of entries to retrieve
//     * @param sort      field to sort on
//     * @param asc       sort order
//     * @return Response with public entries from registry partners
//     */
//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    @Path("/{id}/entries")
//    public Response getWebEntries(
//            @PathParam("id") final long partnerId,
//            @DefaultValue("0") @QueryParam("offset") final int offset,
//            @DefaultValue("15") @QueryParam("limit") final int limit,
//            @DefaultValue("created") @QueryParam("sort") final String sort,
//            @DefaultValue("false") @QueryParam("asc") final boolean asc) {
//        getUserId();
//        final Results<PartData> result = remoteEntries.getPublicEntries(partnerId, offset, limit, sort, asc);
//        return super.respond(Response.Status.OK, result);
//    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries/{entryId}/attachments")
    public List<AttachmentInfo> getAttachments(@PathParam("id") final long partnerId,
                                               @PathParam("entryId") final long partId) {
        final String userId = getUserId();
        return remoteEntries.getEntryAttachments(userId, partnerId, partId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries/{entryId}")
    public Response getWebEntry(
            @PathParam("id") final long partnerId,
            @PathParam("entryId") final long entryId) {
        final String userId = super.getUserId();
        final PartData result = remoteEntries.getPublicEntry(userId, partnerId, entryId);
        return super.respond(Response.Status.OK, result);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries/{entryId}/tooltip")
    public Response getWebEntryTooltip(
            @PathParam("id") final long partnerId, @PathParam("entryId") final long entryId) {
        final String userId = super.getUserId();
        final PartData result = remoteEntries.getPublicEntryTooltip(userId, partnerId, entryId);
        return super.respond(Response.Status.OK, result);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries/{entryId}/statistics")
    public Response getStatistics(@PathParam("id") final long partnerId,
                                  @PathParam("entryId") final long entryId) {
        final String userId = super.getUserId();
        final PartStatistics statistics = remoteEntries.getPublicEntryStatistics(userId, partnerId, entryId);
        return super.respond(statistics);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries/{entryId}/sequence")
    public Response getWebEntrySequence(
            @PathParam("id") final long partnerId, @PathParam("entryId") final String entryId) {
        requireUserId();
        final FeaturedDNASequence result = remoteEntries.getPublicEntrySequence(partnerId, entryId);
        ConfigurationModel configuration = DAOFactory.getConfigurationDAO().get(ConfigurationKey.URI_PREFIX);
        if (configuration != null && result != null) {
            String uriPrefix = configuration.getValue();
            result.setUri(uriPrefix + "/web/" + partnerId + "/entry/" + entryId);
        }
        return super.respond(Response.Status.OK, result);
    }

    @DELETE
    @Path("/partner/remote")
    public Response remoteWebPartnerRemoveRequest(
            @HeaderParam(WOR_PARTNER_TOKEN) String worToken,
            @QueryParam("url") String url) {
        RemoteContact remoteContact = new RemoteContact();
        return super.respond(remoteContact.handleRemoteRemoveRequest(worToken, url));
    }

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

    @DELETE
    @Path("/partner/{url}")
    public Response removeWebPartner(@PathParam("url") final String url) {
        final String userId = getUserId();
        if (controller.removeWebPartner(userId, url)) {
            return respond(Response.Status.OK);
        }
        return respond(Response.Status.INTERNAL_SERVER_ERROR);
    }

    @GET
    @Path("/entries")
    public Response getWebEntries(@QueryParam("download") boolean download,
                                  @QueryParam("limit") int limit,
                                  @QueryParam("offset") int offset) {
        String userId = requireUserId();
        log(userId, "downloading web entries");
        RemoteEntriesAsCSV remoteEntriesAsCSV = new RemoteEntriesAsCSV(true);
        remoteEntriesAsCSV.getEntries(offset, limit);
        final File file = remoteEntriesAsCSV.getFilePath().toFile();
        if (file.exists()) {
            return Response.ok(new Setting("fileName", file.getName())).build();
        }
        return super.respond(false);
    }

    // equivalent to search web in web of registries
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/entries")
    public Response searchWebEntries(final SearchQuery query,
                                     @DefaultValue("true") @QueryParam("includeLocal") boolean includeLocal) {
        requireUserId();
        WebSearch webSearch = new WebSearch();
        return super.respond(webSearch.run(query, includeLocal));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/entries/{id}")
    public Response getWebEntry(@PathParam("id") String entryId) {
        try {
            requireUserId();
            WebEntries webEntries = new WebEntries();
            return super.respond(webEntries.getPart(entryId));
        } catch (Exception e) {
            Logger.error(e);
            throw new WebApplicationException(e);
        }
    }
}
