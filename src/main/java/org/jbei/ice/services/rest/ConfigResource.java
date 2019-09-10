package org.jbei.ice.services.rest;

import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.config.ConfigurationSettings;
import org.jbei.ice.lib.dto.Setting;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;

/**
 * @author Hector Plahar
 */
@Path("/config")
public class ConfigResource extends RestResource {

    private ConfigurationSettings controller = new ConfigurationSettings();

    /**
     * Retrieves list of system settings available
     *
     * @return list of retrieved system settings that can be changed (including those with no
     * values)
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<Setting> get() {
        final String userId = requireUserId();
        return controller.retrieveSystemSettings(userId);
    }

    @GET
    @Path("/site")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSiteSettings() {
        return super.respond(controller.getSiteSettings());
    }

    /**
     * @return the version setting of this ICE instance
     */
    @GET
    @Path("/version")
    @Produces(MediaType.APPLICATION_JSON)
    public Setting getVersion(@Context final UriInfo uriInfo) {
        final String url = uriInfo.getBaseUri().getAuthority();
        return controller.getSystemVersion(url);
    }

    /**
     * Retrieves the value for the specified config key
     *
     * @param key config key
     * @return setting containing the passed key and associated value if found
     */
    @GET
    @Path("/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConfig(@PathParam("key") final String key) {
        if (!"NEW_REGISTRATION_ALLOWED".equalsIgnoreCase(key) && !"PASSWORD_CHANGE_ALLOWED".equalsIgnoreCase(key)) {
            requireUserId();
        }
        return super.respond(controller.getPropertyValue(key));
    }

    /**
     * @param setting a config value to update
     * @return the updated config key:value
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(final Setting setting) {
        final String userId = requireUserId();
        log(userId, "updating system setting " + setting.getKey() + " to \'" + setting.getValue() + "\'");
        final String url = getThisServer(false);
        return super.respond(controller.updateSetting(userId, setting, url));
    }

    @PUT
    @Path("/value")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateConfigValue(Setting setting) {
        try {
            String userId = requireUserId();
            log(userId, "auto updating setting " + setting.getKey());
            setting = controller.autoUpdateSetting(userId, setting);
            if (setting == null)
                throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
            return super.respond(setting);
        } catch (PermissionException e) {
            Logger.error(e);
            throw new WebApplicationException(e.getMessage(), Response.Status.FORBIDDEN);
        }
    }
}
