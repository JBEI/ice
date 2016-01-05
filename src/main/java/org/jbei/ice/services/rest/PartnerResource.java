package org.jbei.ice.services.rest;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.web.RegistryPartner;
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
    @Path("/partners")
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
                                  @HeaderParam(AUTHENTICATION_PARAM_NAME) String sessionId,
                                  RegistryPartner partner) {

        WebPartners webPartners = new WebPartners();
        RegistryPartner result;
        String url = null;
        String userId = null;

        // where the request is coming from
        if (StringUtils.isEmpty(sessionId)) {
            url = request.getRemoteHost();
            Logger.info("Received partner add request from " + url);
        } else {
            userId = getUserId(sessionId);
        }

        result = webPartners.addNewPartner(userId, url, partner);
        return super.respond(result);
    }
}
