package org.jbei.ice.services.rest;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.dto.web.WebOfRegistries;
import org.jbei.ice.lib.net.WoRController;

/**
 * @author Hector Plahar
 */
@Path("/web")
public class WebResource extends RestResource {

    private WoRController controller = new WoRController();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public WebOfRegistries query(
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        try {
            return controller.getRegistryPartners();
        } catch (ControllerException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    @PUT
    public Response addPartToWeb(@HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        return Response.ok().build();
    }
}
