package org.jbei.ice.services.rest;

import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.dto.Setting;
import org.jbei.ice.lib.dto.search.IndexType;
import org.jbei.ice.lib.search.SearchController;

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
     * @param sessionId Session Id for user
     * @return list of retrieved system settings that can be changed (including those with no values)
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<Setting> get(@HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        String userId = getUserIdFromSessionHeader(sessionId);
        return controller.retrieveSystemSettings(userId);
    }

    @GET
    @Path("/version")
    @Produces(MediaType.APPLICATION_JSON)
    public Setting getVersion(@Context UriInfo uriInfo) {
        String url = uriInfo.getBaseUri().getAuthority();
        return controller.getSystemVersion(url);
    }

    /**
     * Retrieves the value for the specified config key
     *
     * @return setting containing the passed key and associated value if found
     */
    @GET
    @Path("/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Setting getConfig(@PathParam("key") String key) {
        return controller.getPropertyValue(key);
    }

    @PUT
    @Path("/lucene")
    public Response buildLuceneIndex(@HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        boolean success = searchController.rebuildIndexes(userId, IndexType.LUCENE);
        return super.respond(success);
    }

    @PUT
    @Path("/blast")
    public Response buildBlastIndex(@HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        boolean success = searchController.rebuildIndexes(userId, IndexType.BLAST);
        return super.respond(success);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Setting update(@HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
            Setting setting) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        return controller.updateSetting(userId, setting);
    }
}
