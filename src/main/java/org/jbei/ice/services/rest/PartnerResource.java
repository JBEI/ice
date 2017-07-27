package org.jbei.ice.services.rest;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.access.RemoteAccess;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.dto.entry.AttachmentInfo;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.TraceSequenceAnalysis;
import org.jbei.ice.lib.dto.web.PartnerEntries;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.entry.EntrySelection;
import org.jbei.ice.lib.net.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Resource for interacting with partners in the web of registries configuration with this instance
 *
 * @author Hector Plahar
 */
@Path("/partners")
public class PartnerResource extends RestResource {

    private final WebPartners webPartners = new WebPartners();

    /**
     * Retrieve details about a specific partner
     *
     * @param partnerId unique partner identifier (either the database id or the url)
     * @return details about the partner if found
     */
    @GET
    @Path("{id}")
    public Response getWebPartner(@PathParam("id") final String partnerId) {
        requireUserIdOrWebPartner("retrieving details for partner " + partnerId);
        final RegistryPartner partner = webPartners.get(partnerId);
        return super.respond(Response.Status.OK, partner);
    }

    /**
     * Retrieves list of available web partners requested by user or an ICE instance
     *
     * @return list of partners
     */
    @GET
    public Response getWebPartners() {
        requireUserIdOrWebPartner("retrieving web partners");
        WebPartners webPartners = new WebPartners();
        return super.respond(webPartners.getPartners());
    }

    /**
     * Adds a remote instance as a registry partner. There are two entry points for this call within ICE.
     * One is from the web of registries task. This does not contain any authentication information
     * and therefore requires verification of the token.<p>
     * The second is from the admin ui where a partner is manually added to this ice instance.
     *
     * @param partner details about the partner to add
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addNewPartner(RegistryPartner partner) {
        RegistryPartner result;
        String userId = getUserId();

        // where the request is coming from
        // assumes that if no session information (or invalid user or request server is different from local?
        // or contains token?) then this is a request coming remotely
        if (StringUtils.isEmpty(userId) && !StringUtils.isEmpty(partner.getApiKey())) {
            Logger.info("Received remote partner add request from " + partner.getUrl());
            result = webPartners.processRemoteWebPartnerAdd(partner);
        } else {
            // local request
            result = webPartners.addNewPartner(userId, partner, super.getThisServer(false));
        }

        return super.respond(result);
    }

    /**
     * Schedules a task to transfer a list of entries (explicit or context for generating them) to a specified partner
     *
     * @param remoteId       unique partner identifier
     * @param entrySelection set of entries to transfer or context used to generate entries
     */
    @POST
    @Path("/{id}/entries")
    public Response transferEntries(@PathParam("id") final long remoteId,
                                    final EntrySelection entrySelection) {
        final String userId = requireUserId();
        RemoteEntries remoteEntries = new RemoteEntries();
        try {
            remoteEntries.transferEntries(userId, remoteId, entrySelection);
            return super.respond(Response.Status.OK);
        } catch (PermissionException pe) {
            return super.respond(Response.Status.FORBIDDEN);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries/{entryId}/tooltip")
    public Response getWebEntryTooltip(
            @PathParam("id") final long partnerId, @PathParam("entryId") final long entryId) {
        final String userId = super.getUserId();
        RemoteEntries remoteEntries = new RemoteEntries();
        final PartData result = remoteEntries.getPublicEntryTooltip(userId, partnerId, entryId);
        return super.respond(Response.Status.OK, result);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries/{entryId}/attachments")
    public List<AttachmentInfo> getAttachments(@PathParam("id") final long partnerId,
                                               @PathParam("entryId") final long partId) {
        final String userId = requireUserId();
        RemoteEntries remoteEntries = new RemoteEntries();
        return remoteEntries.getEntryAttachments(userId, partnerId, partId);
    }

    @PUT
    @Path("/{id}")
    public Response updateWebPartner(@PathParam("id") long partnerId,
                                     final RegistryPartner partner) {
        String userId = requireUserId();
        WebPartners partners = new WebPartners();
        return super.respond(partners.update(userId, partnerId, partner));
    }

    @PUT
    @Path("/{id}/apiKey")
    public Response updateWebPartnerAPIKey(@PathParam("id") long partnerId) {
        String userId = requireUserId();
        WebPartners partners = new WebPartners();
        try {
            return super.respond(partners.updateAPIKey(userId, partnerId));
        } catch (PermissionException pe) {
            Logger.error(pe);
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        } catch (IllegalArgumentException pe) {
            Logger.error(pe);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Request from a remote ICE instance to update it's api key
     * Verification uses the existing API key. Similar to updating password
     * by sending the old password
     *
     * @param partner remote partner information (including new api key)
     * @return information about this instance with the new token remote partner
     */
    @PUT
    public Response updateRemotePartnerAPIKey(RegistryPartner partner) {
        RegistryPartner registryPartner = requireWebPartner();
        WebPartners partners = new WebPartners();
        return super.respond(partners.updateRemoteAPIKey(registryPartner.getUrl(), partner));
    }

    @DELETE
    @Path("/{id}")
    public Response removeWebPartner(@PathParam("id") String partnerId) {
        try {
            long id = Long.decode(partnerId);
            String userId = requireUserId();
            WebPartners partners = new WebPartners();
            return super.respond(partners.remove(userId, id));
        } catch (NumberFormatException nfe) {
            RegistryPartner registryPartner = requireWebPartner();
            WebPartners partners = new WebPartners();
            return super.respond(partners.removeRemotePartner(registryPartner.getId(), partnerId));
        }
    }

    /**
     * Retrieves available folders from the specified remote ice partner
     *
     * @param remoteId unique identifier for remote partner being accessed
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}/available")
    public Response readRemoteUser(@PathParam("id") long remoteId) {
        requireUserId();
        try {
            RemoteFolders remoteFolders = new RemoteFolders(remoteId);
            return super.respond(remoteFolders.getAvailableFolders());
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}/users/{email}")
    public Response getRemoteUser(@PathParam("id") final long remoteId,
                                  @PathParam("email") final String email) {
        requireUserId();
        RemoteAccess remoteAccess = new RemoteAccess();
        AccountTransfer accountTransfer = remoteAccess.getRemoteUser(remoteId, email);
        return super.respond(accountTransfer);
    }

    /**
     * @return sequence from remote ICE
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}/{entryId}/sequence")
    public Response getSequence(@PathParam("id") final long remoteId,
                                @PathParam("entryId") final long partId) {
        requireUserId();
        try {
            RemoteSequence remoteSequence = new RemoteSequence(remoteId, partId);
            final FeaturedDNASequence sequence = remoteSequence.getRemoteSequence();
            if (sequence == null) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            return Response.status(Response.Status.OK).entity(sequence).build();
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

    /**
     * @return traces from remote ICE
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}/parts/{entryId}/traces")
    public Response getSequenceTraces(@PathParam("id") long remoteId,
                                      @PathParam("entryId") long partId) {
        try {
            requireUserId();
            RemoteEntry remoteEntry = new RemoteEntry(remoteId, partId);
            List<TraceSequenceAnalysis> traces = remoteEntry.getTraces();
            if (traces == null) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            return Response.status(Response.Status.OK).entity(traces).build();
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

    /**
     * @return public folders from remote ICE
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}/folders/{folderId}")
    public Response getPublicFolderEntries(@PathParam("id") final long remoteId,
                                           @PathParam("folderId") final long folderId,
                                           @DefaultValue("0") @QueryParam("offset") final int offset,
                                           @DefaultValue("15") @QueryParam("limit") final int limit,
                                           @DefaultValue("created") @QueryParam("sort") final String sort,
                                           @DefaultValue("false") @QueryParam("asc") final boolean asc) {
        requireUserId();
        try {
            RemoteFolder remoteFolder = new RemoteFolder(remoteId, folderId);
            return super.respond(remoteFolder.getEntries(sort, asc, offset, limit));
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

    /**
     * @return part samples from remote ICE
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/parts/{partId}/samples")
    public Response getRemotePartSamples(@PathParam("id") long remoteId,
                                         @PathParam("partId") long partId) {
        requireUserId();
        try {
            RemoteEntry remoteEntry = new RemoteEntry(remoteId, partId);
            return super.respond(remoteEntry.getSamples());
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

    /**
     * @return comments from remote ICE
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}/parts/{partId}/comments")
    public Response getRemotePartComments(@PathParam("id") long remoteId,
                                          @PathParam("partId") long partId) {
        requireUserId();
        try {
            RemoteEntry remoteEntry = new RemoteEntry(remoteId, partId);
            return super.respond(remoteEntry.getComments());
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Retrieves entries for specified partner using the specified paging parameters
     *
     * @param partnerId unique identifier for registry partner whose entries are being retrieved
     * @param offset    record retrieve offset paging parameter
     * @param limit     maximum number of entries to retrieve
     * @param sort      field to sort on
     * @param asc       sort order
     * @return Response with public entries from registry partners
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries")
    public Response getWebEntries(
            @PathParam("id") final long partnerId,
            @DefaultValue("0") @QueryParam("offset") final int offset,
            @DefaultValue("15") @QueryParam("limit") final int limit,
            @DefaultValue("created") @QueryParam("sort") final String sort,
            @DefaultValue("false") @QueryParam("asc") final boolean asc) {
        requireUserId();
        RemoteEntries remoteEntries = new RemoteEntries();
        PartnerEntries entries = remoteEntries.getPublicEntries(partnerId, offset, limit, sort, asc);
        return super.respond(Response.Status.OK, entries);
    }
}
