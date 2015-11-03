package org.jbei.ice.services.rest;

import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.Setting;
import org.jbei.ice.lib.dto.search.IndexType;
import org.jbei.ice.lib.search.SearchController;
import org.jbei.ice.lib.utils.Utils;

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

    private ConfigurationController controller = new ConfigurationController();
    private SearchController searchController = new SearchController();

    /**
     * Retrieves list of system settings available
     *
     * @return list of retrieved system settings that can be changed (including those with no
     * values)
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<Setting> get(
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        final String userId = getUserId(sessionId);
        return controller.retrieveSystemSettings(userId);
    }

    @GET
    @Path("/site")
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<Setting> getSiteSettings() {
        ArrayList<Setting> settings = new ArrayList<>();
        settings.add(new Setting("logo", Utils.getConfigValue(ConfigurationKey.LOGO)));
        settings.add(new Setting("loginMessage", Utils.getConfigValue(ConfigurationKey.LOGIN_MESSAGE)));
        settings.add(new Setting("footer", Utils.getConfigValue(ConfigurationKey.FOOTER)));
        settings.add(new Setting("version", "4.6.0"));
        return settings;
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
    public Setting getConfig(@HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId,
                             @PathParam("key") final String key) {
        if (!"NEW_REGISTRATION_ALLOWED".equalsIgnoreCase(key) && !"PASSWORD_CHANGE_ALLOWED".equalsIgnoreCase(key)) {
            getUserId(sessionId);
        }
        return controller.getPropertyValue(key);
    }

    /**
     * @return Response specifying success or failure of re-index
     */
    @PUT
    @Path("/lucene")
    public Response buildLuceneIndex(@HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        final String userId = getUserId(sessionId);
        final boolean success = searchController.rebuildIndexes(userId, IndexType.LUCENE);
        return super.respond(success);
    }

    /**
     * @return Response specifying success or failure of re-index
     */
    @PUT
    @Path("/blast")
    public Response buildBlastIndex(@HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        final String userId = getUserId(sessionId);
        final boolean success = searchController.rebuildIndexes(userId, IndexType.BLAST);
        return super.respond(success);
    }

    /**
     * @param setting a config value to update
     * @return the updated config key:value
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(final Setting setting) {
        final String userId = getUserId();
        final String url = request.getRemoteHost();
        return super.respond(controller.updateSetting(userId, setting, url));
    }
}
