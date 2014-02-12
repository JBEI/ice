package org.jbei.ice.services.rest;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.dto.search.SearchQuery;
import org.jbei.ice.lib.dto.search.SearchResults;
import org.jbei.ice.lib.search.SearchController;

/**
 * @author Hector Plahar
 */
@Path("/search")
public class SearchResource extends RestResource {

    private SearchController controller = new SearchController();

    @GET
    @Produces("application/json")
    public SearchResults search(@Context UriInfo info,
            @QueryParam("query") String queryString,
            @QueryParam("searchWeb") boolean searchWeb,
            @HeaderParam(
                    value = "X-ICE-Authentication-SessionId") String userAgentHeader) { //},
                    // @PathParam("start") int start) {
        try {
            String userId = getUserIdFromSessionHeader(userAgentHeader);
            SearchQuery query = new SearchQuery();
            query.setQueryString(queryString);
            HibernateHelper.beginTransaction();
            return controller.runSearch(userId, query, searchWeb);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        } finally {
            HibernateHelper.commitTransaction();
        }
    }

    @GET
    @Produces("application/json")
    public SearchResults search(@Context UriInfo info,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
            @QueryParam("searchWeb") boolean searchWeb,
            SearchQuery query,
            @Context final HttpServletResponse response) { //}, @PathParam("start") int start) {
        try {
            Logger.info(userAgentHeader);
            HibernateHelper.beginTransaction();
            String userId = getUserIdFromSessionHeader(userAgentHeader);
            return controller.runSearch(userId, query, searchWeb);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        } finally {
            HibernateHelper.commitTransaction();
        }
    }
}
