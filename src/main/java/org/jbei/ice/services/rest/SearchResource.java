package org.jbei.ice.services.rest;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.search.BlastProgram;
import org.jbei.ice.lib.dto.search.BlastQuery;
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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResults search(
            @QueryParam("q") String queryString,
            @DefaultValue("false") @QueryParam("w") boolean searchWeb,
            @DefaultValue("false") @QueryParam("hasSample") boolean hasSample,
            @DefaultValue("false") @QueryParam("hasSequence") boolean hasSequence,
            @DefaultValue("false") @QueryParam("hasAttachment") boolean hasAttachment,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("15") @QueryParam("limit") int limit,
            @DefaultValue("relevance") @QueryParam("sort") String sort,
            @DefaultValue("false") @QueryParam("asc") boolean asc,
            @QueryParam("s") String sequence,
            @QueryParam("b") BlastProgram blastProgram,
            @QueryParam("t") List<String> entryTypes,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        try {
            String userId = getUserIdFromSessionHeader(userAgentHeader);
            SearchQuery query = new SearchQuery();
            query.setQueryString(queryString);
            SearchQuery.Parameters parameters = query.getParameters();
            parameters.setRetrieveCount(limit);
            parameters.setStart(offset);
            parameters.setSortAscending(asc);
            parameters.setSortField(ColumnField.valueOf(sort.toUpperCase()));
            parameters.setHasAttachment(hasAttachment);
            parameters.setHasSample(hasSample);
            parameters.setHasSequence(hasSequence);

            if (sequence != null && !sequence.trim().isEmpty()) {
                if (blastProgram == null)
                    blastProgram = BlastProgram.BLAST_N;
                BlastQuery blastQuery = new BlastQuery();
                blastQuery.setBlastProgram(blastProgram);
                blastQuery.setSequence(sequence);
                query.setBlastQuery(blastQuery);
            }

            // set types
            ArrayList<EntryType> types = new ArrayList<>();
            for (String type : entryTypes) {
                types.add(EntryType.nameToType(type));
            }
            query.setEntryTypes(types);

            return controller.runSearch(userId, query, searchWeb);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }
}
