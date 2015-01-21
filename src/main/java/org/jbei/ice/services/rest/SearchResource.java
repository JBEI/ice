package org.jbei.ice.services.rest;

import org.apache.commons.lang.StringUtils;
import org.jbei.ice.lib.account.SessionHandler;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.search.SearchQuery;
import org.jbei.ice.lib.dto.search.SearchResults;
import org.jbei.ice.lib.search.SearchController;
import org.jbei.ice.lib.shared.ColumnField;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

/**
 * REST resource for searching. Supports keyword search with query params for filtering and
 * advanced search
 *
 * @author Hector Plahar
 */
@Path("/search")
public class SearchResource extends RestResource {

    private SearchController controller = new SearchController();

    /**
     * Advanced Search. The use of post is mostly for the sequence string for
     * blast which can get very long and results in a 413 status code if
     * sent via GET
     *
     * @return results of the search
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response search(
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionHeader,
            @DefaultValue("false") @QueryParam("webSearch") boolean searchWeb,
            SearchQuery query) {
        String userId = SessionHandler.getUserIdBySession(sessionHeader);
        if (StringUtils.isEmpty(userId) && !searchWeb) {
            return super.respond(Response.Status.FORBIDDEN);
        }

        try {
            SearchResults results = controller.runSearch(userId, query, searchWeb);
            return super.respond(Response.Status.OK, results);
        } catch (Exception e) {
            Logger.error(e);
            return super.respond(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Keyword search
     *
     * @param queryString keywords to search on
     * @param searchWeb   whether to perform a web of registry search or not
     * @param offset      result start
     * @param limit       result count upper limit
     * @param sort        result sort
     * @param asc         true if return results in ascending order, false otherwise
     * @param sessionId   user unique session identifier
     * @return wrapper around list of search results conforming to query params
     */

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response search(
            @QueryParam("q") String queryString,
            @DefaultValue("false") @QueryParam("webSearch") boolean searchWeb,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("15") @QueryParam("limit") int limit,
            @DefaultValue("relevance") @QueryParam("sort") String sort,
            @DefaultValue("false") @QueryParam("asc") boolean asc,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        String userId = SessionHandler.getUserIdBySession(sessionId);
        if (StringUtils.isEmpty(userId) && !searchWeb) {
            return super.respond(Response.Status.FORBIDDEN);
        }

        log(userId, "query \'" + queryString + '\'');
        SearchQuery query = new SearchQuery();
        query.setQueryString(queryString);
        SearchQuery.Parameters parameters = query.getParameters();
        parameters.setRetrieveCount(limit);
        parameters.setStart(offset);
        parameters.setSortAscending(asc);
        parameters.setSortField(ColumnField.valueOf(sort.toUpperCase()));

        List<EntryType> types = Arrays.asList(EntryType.values());
        query.setEntryTypes(types);
        return super.respond(controller.runSearch(userId, query, searchWeb));
    }
}
