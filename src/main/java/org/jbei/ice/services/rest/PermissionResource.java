package org.jbei.ice.services.rest;

import org.jbei.ice.lib.access.RemoteAccess;
import org.jbei.ice.lib.dto.access.RemoteAccessPermission;
import org.jbei.ice.lib.dto.web.RegistryPartner;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Resource for interacting with permissions.
 * Currently only exposing a means for adding a remote permission
 *
 * @author Hector Plahar
 */
@Path("permissions")
public class PermissionResource extends RestResource {

    /**
     * Add a remote access from a partner in the web of registries
     */
    @POST
    @Path("/remote")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addRemoteAccess(RemoteAccessPermission accessPermission) {
        RegistryPartner partner = requireWebPartner();
        RemoteAccess remoteAccess = new RemoteAccess();
        return super.respond(remoteAccess.add(partner, accessPermission));
    }
}
