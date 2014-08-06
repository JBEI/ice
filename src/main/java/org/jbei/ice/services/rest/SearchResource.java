package org.jbei.ice.services.rest;

import java.util.Arrays;
import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.search.SearchQuery;
import org.jbei.ice.lib.dto.search.SearchResults;
import org.jbei.ice.lib.search.SearchController;
import org.jbei.ice.lib.shared.ColumnField;

/**
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
    public SearchResults search(
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
            @DefaultValue("false") @QueryParam("w") boolean searchWeb,
            SearchQuery query) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        return controller.runSearch(userId, query, searchWeb);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResults search(
            @QueryParam("q") String queryString,
            @DefaultValue("false") @QueryParam("w") boolean searchWeb,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("15") @QueryParam("limit") int limit,
            @DefaultValue("relevance") @QueryParam("sort") String sort,
            @DefaultValue("false") @QueryParam("asc") boolean asc,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        SearchQuery query = new SearchQuery();
        query.setQueryString(queryString);
        SearchQuery.Parameters parameters = query.getParameters();
        parameters.setRetrieveCount(limit);
        parameters.setStart(offset);
        parameters.setSortAscending(asc);
        parameters.setSortField(ColumnField.valueOf(sort.toUpperCase()));

        List<EntryType> types = Arrays.asList(EntryType.values());
        query.setEntryTypes(types);
        return controller.runSearch(userId, query, searchWeb);
    }
}
