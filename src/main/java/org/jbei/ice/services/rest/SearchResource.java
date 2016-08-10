package org.jbei.ice.services.rest;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.dto.entry.AutoCompleteFieldValues;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.search.IndexType;
import org.jbei.ice.lib.dto.search.SearchQuery;
import org.jbei.ice.lib.dto.search.SearchResults;
import org.jbei.ice.lib.search.SearchController;
import org.jbei.ice.lib.search.WebSearch;
import org.jbei.ice.lib.shared.ColumnField;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

/**
 * REST resource for searching. Supports keyword search with query params for filtering and advanced
 * search
 *
 * @author Hector Plahar
 */
@Path("/search")
public class SearchResource extends RestResource {

    private SearchController controller = new SearchController();

    /**
     * Search entries by using filters on the values
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/filter")
    public Response searchPartFields(
            @DefaultValue("") @QueryParam("token") String token,
            @DefaultValue("SELECTION_MARKERS") @QueryParam("field") String field,
            @DefaultValue("8") @QueryParam("limit") int limit) {
        requireUserId();
        AutoCompleteFieldValues values = new AutoCompleteFieldValues(field);
        return super.respond(values.getMatchingValues(token, limit));
    }

    /**
     * Advanced Search. The use of post is mostly for the sequence string for blast which can get
     * very long and results in a 413 status code if sent via GET
     *
     * @param searchWeb whether to perform a web of registry search or not
     * @param query     parameters to the search
     * @return results of the search
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response search(@DefaultValue("false") @QueryParam("webSearch") final boolean searchWeb,
                           final SearchQuery query) {
        String userId = getUserId();
        if (searchWeb) {
            if (StringUtils.isEmpty(userId))
                throw new WebApplicationException(Response.Status.FORBIDDEN);

            WebSearch webSearch = new WebSearch();
            return super.respond(webSearch.run(query));
        }

        if (StringUtils.isEmpty(userId)) {
            requireWebPartner();
        }

        final SearchResults results = controller.runSearch(userId, query);
        return super.respond(Response.Status.OK, results);
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
     * @return wrapper around list of search results conforming to query params
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response search(@QueryParam("q") final String queryString,
                           @DefaultValue("false") @QueryParam("webSearch") final boolean searchWeb,
                           @DefaultValue("0") @QueryParam("offset") final int offset,
                           @DefaultValue("15") @QueryParam("limit") final int limit,
                           @DefaultValue("relevance") @QueryParam("sort") final String sort,
                           @DefaultValue("false") @QueryParam("asc") final boolean asc) {
        final String userId = getUserId();
        if (StringUtils.isEmpty(userId) && !searchWeb) {
            return super.respond(Response.Status.FORBIDDEN);
        }

        log(userId, "query \'" + queryString + '\'');
        final SearchQuery query = new SearchQuery();
        query.setQueryString(queryString);
        final SearchQuery.Parameters parameters = query.getParameters();
        parameters.setRetrieveCount(limit);
        parameters.setStart(offset);
        parameters.setSortAscending(asc);
        parameters.setSortField(ColumnField.valueOf(sort.toUpperCase()));

        final List<EntryType> types = Arrays.asList(EntryType.values());
        query.setEntryTypes(types);
        return super.respond(controller.runSearch(userId, query));
    }

    /**
     * Rebuild the lucene indexes used for searching
     */
    @PUT
    @Path("/indexes/lucene")
    public Response updateLuceneIndex() {
        final String userId = requireUserId();
        log(userId, "rebuilding lucene indexes");
        controller.rebuildIndexes(userId, IndexType.LUCENE);
        return super.respond(Response.Status.OK);
    }

    /**
     * Rebuild the blast database
     */
    @PUT
    @Path("/indexes/blast")
    public Response updateBlastIndex() {
        final String userId = requireUserId();
        log(userId, "rebuilding blast database");
        controller.rebuildIndexes(userId, IndexType.BLAST);
        return super.respond(Response.Status.OK);
    }
}
