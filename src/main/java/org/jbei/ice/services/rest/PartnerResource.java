package org.jbei.ice.services.rest;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.entry.EntrySelection;
import org.jbei.ice.lib.net.RemoteEntries;
import org.jbei.ice.lib.net.WebPartners;
import org.jbei.ice.lib.net.WoRController;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Partner resource for web of registries configuration
 *
 * @author Hector Plahar
 */
@Path("/partners")
public class PartnerResource extends RestResource {

    @GET
    public Response getWebPartners(@QueryParam("url") String url) {
        String userId = getUserId();
        WoRController controller = new WoRController();
        if (StringUtils.isEmpty(userId))
            return super.respond(controller.getWebPartners(worPartnerToken, url));
        return super.respond(controller.getWebPartners());
    }

    /**
     * Adds a remote instance as a registry partner. There are two entry points for this call within ICE.
     * One is from the web of registries task. This does not contain any authentication information
     * and therefore requires verification of the token.<p>
     * The second is from the admin ui where a partner is manually added to this ice instance.
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addNewPartner(RegistryPartner partner) {
        WebPartners webPartners = new WebPartners();
        RegistryPartner result;
        String userId = getUserId();

        // where the request is coming from
        // assumes that if no session information (or invalid user or request server is different from local? or contains token?) then this is
        // a request coming remotely
        if (StringUtils.isEmpty(userId) && !StringUtils.isEmpty(partner.getApiKey())) {
            Logger.info("Received remote partner add request from " + partner.getUrl());
            result = webPartners.processRemoteWebPartnerAdd(partner);
        } else {
            // local request
            result = webPartners.addNewPartner(userId, partner);
        }

        return super.respond(result);
    }

    @POST
    @Path("/{id}/entries")
    public Response transferEntries(@PathParam("id") final long remoteId,
                                    final EntrySelection entrySelection) {
        final String userId = super.getUserId();
        RemoteEntries remoteEntries = new RemoteEntries();
        remoteEntries.transferEntries(userId, remoteId, entrySelection);
        return super.respond(Response.Status.OK);
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
        return super.respond(partners.updateAPIKey(userId, partnerId));
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
}
