package org.jbei.ice.services.rest;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.net.WebPartners;
import org.jbei.ice.lib.net.WoRController;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

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
     * Add current url obtained from uriInfo as a web partner. Returns list of current
     * partners that are available for web of registries if this is a master.
     *
     * @return information about this instance for use in exchanging data
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addNewPartner(@Context final UriInfo uriInfo,
                                  @Context HttpServletRequest request,
                                  @HeaderParam(AUTHENTICATION_PARAM_NAME) String sessionId,
                                  RegistryPartner partner) {
//        String url = uriInfo.getBaseUri().getAuthority();

        // where the request is coming from
        String url = request.getRemoteHost();

        WebPartners webPartners = new WebPartners();
        String userId = null;
        if (!StringUtils.isEmpty(sessionId))
            userId = getUserId(sessionId);

        RegistryPartner result = webPartners.addNewPartner(url, partner);
        return super.respond(result);
    }
}
