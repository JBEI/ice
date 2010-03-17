package org.jbei.ice.controllers;

import java.util.ArrayList;

import org.jbei.ice.controllers.BlastController.BlastControllerException;
import org.jbei.ice.controllers.common.Controller;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.controllers.permissionVerifiers.EntryPermissionVerifier;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.logging.UsageLogger;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.search.blast.Blast;
import org.jbei.ice.lib.search.blast.BlastException;
import org.jbei.ice.lib.search.blast.BlastResult;
import org.jbei.ice.lib.search.blast.ProgramTookTooLongException;
import org.jbei.ice.lib.search.lucene.AggregateSearch;
import org.jbei.ice.lib.search.lucene.SearchException;
import org.jbei.ice.lib.search.lucene.SearchResult;
import org.jbei.ice.web.IceSession;

public class SearchController extends Controller {
    public SearchController(Account account) {
        super(account, new EntryPermissionVerifier());
    }

    public ArrayList<SearchResult> find(String query) throws ControllerException {
        ArrayList<SearchResult> results = new ArrayList<SearchResult>();

        try {
            UsageLogger.info(String.format("Searching for: %s", query));

            EntryController entryController = new EntryController(IceSession.get().getAccount());

            ArrayList<SearchResult> searchResults = AggregateSearch.query(query);
            if (searchResults != null) {
                for (SearchResult searchResult : searchResults) {
                    Entry entry = searchResult.getEntry();

                    if (entryController.hasReadPermission(entry)) {
                        results.add(searchResult);
                    }
                }
            }

            UsageLogger.info(String.format("%d visible results found", (results == null) ? 0
                    : results.size()));
        } catch (SearchException e) {
            throw new ControllerException(e);
        }

        return results;
    }

    public static ArrayList<BlastResult> query(String query, String program)
            throws ProgramTookTooLongException {

        ArrayList<BlastResult> results = null;

        try {
            Logger.info(String.format("Blast '%s' searching for %s", program, query));

            Blast blast = new Blast();

            results = blast.query(query, program);

            Logger.info(String.format("Blast found %d results", (results == null) ? 0 : results
                    .size()));
        } catch (BlastException e) {
            Logger.error(BlastControllerException.BLAST_QUERY_FAILED, e);
        } catch (ProgramTookTooLongException e) {
            throw new ProgramTookTooLongException(e);
        } catch (Exception e) {
            Logger.error(BlastControllerException.BLAST_QUERY_FAILED, e);
        }

        return results;
    }
}
