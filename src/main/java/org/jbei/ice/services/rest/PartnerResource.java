package org.jbei.ice.services.rest;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.entry.EntrySelection;
import org.jbei.ice.lib.net.RemoteEntries;
import org.jbei.ice.lib.net.WebPartners;
import org.jbei.ice.lib.net.WoRController;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
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
    public Response getWebPartners(@HeaderParam(AUTHENTICATION_PARAM_NAME) String sessionId,
                                   @HeaderParam(WOR_PARTNER_TOKEN) String worToken,
                                   @QueryParam("url") String url) {
        WoRController controller = new WoRController();
        if (StringUtils.isEmpty(sessionId))
            return super.respond(controller.getWebPartners(worToken, url));
        final String userId = getUserId(sessionId);
        return super.respond(controller.getWebPartners(userId));
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
    public Response addNewPartner(@Context HttpServletRequest request,
                                  RegistryPartner partner) {

        WebPartners webPartners = new WebPartners();
        RegistryPartner result;
        String userId = getUserId();

        // where the request is coming from
        // assumes that if no session information (or invalid user) then this is
        // a request coming remotely
        // todo : consider making /rest/partners/remote ... the
        if (StringUtils.isEmpty(userId)) {
            String url = request.getRemoteHost();
            Logger.info("Received partner add request from " + url);
            webPartners.processRemoteWebPartnerAdd(url, partner);
        }

        // local request
        result = webPartners.addNewPartner(userId, partner);
        return super.respond(result);
    }

    @POST
    @Path("/{id}/transfer")
    public Response transferEntries(@PathParam("id") final long remoteId,
                                    final EntrySelection entrySelection) {
        final String userId = super.getUserId();
        RemoteEntries remoteEntries = new RemoteEntries();
        remoteEntries.transferEntries(userId, remoteId, entrySelection);
        return super.respond(Response.Status.OK);
    }
}
