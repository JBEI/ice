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
     * @return list of retrieved system settings that can be changed (including those with no
     * values)
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<Setting> get() {
        final String userId = getUserId();
        return controller.retrieveSystemSettings(userId);
    }

    /**
     * @param uriInfo
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
    public Setting getConfig(@PathParam("key") final String key) {
        return controller.getPropertyValue(key);
    }

    /**
     * @return Response specifying success or failure of re-index
     */
    @PUT
    @Path("/lucene")
    public Response buildLuceneIndex() {
        final String userId = getUserId();
        final boolean success = searchController.rebuildIndexes(userId, IndexType.LUCENE);
        return super.respond(success);
    }

    /**
     * @return Response specifying success or failure of re-index
     */
    @PUT
    @Path("/blast")
    public Response buildBlastIndex() {
        final String userId = getUserId();
        final boolean success = searchController.rebuildIndexes(userId, IndexType.BLAST);
        return super.respond(success);
    }

    /**
     * @param setting a config value to update
     * @return the updated config key:value
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Setting update(final Setting setting) {
        final String userId = getUserId();
        return controller.updateSetting(userId, setting);
    }
}
