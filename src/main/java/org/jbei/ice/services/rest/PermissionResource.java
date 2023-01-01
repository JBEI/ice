package org.jbei.ice.services.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jbei.ice.access.RemoteAccess;
import org.jbei.ice.dto.access.AccessPermission;
import org.jbei.ice.dto.web.RegistryPartner;

/**
 * Resource for interacting with permissions.
 * Currently only exposing a means for adding a remote permission
 *
 * @author Hector Plahar
 */
@Path("permissions")
public class PermissionResource extends RestResource {

    /**
     * Add remote access from a partner in the web of registries
     */
    @POST
    @Path("/remote")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addRemoteAccess(AccessPermission accessPermission) {
        RegistryPartner partner = requireWebPartner();
        log(partner.getUrl(), "adding remote permission");
        RemoteAccess remoteAccess = new RemoteAccess();
        return super.respond(remoteAccess.add(partner, accessPermission));
    }
}
