package org.jbei.ice.controllers;

import java.util.ArrayList;

import org.jbei.ice.controllers.common.Controller;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.controllers.permissionVerifiers.EntryPermissionVerifier;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.logging.UsageLogger;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.search.blast.Blast;
import org.jbei.ice.lib.search.blast.BlastException;
import org.jbei.ice.lib.search.blast.BlastResult;
import org.jbei.ice.lib.search.blast.ProgramTookTooLongException;
import org.jbei.ice.lib.search.lucene.AggregateSearch;
import org.jbei.ice.lib.search.lucene.SearchException;
import org.jbei.ice.lib.search.lucene.SearchResult;
import org.jbei.ice.server.EntryToInfoFactory;
import org.jbei.ice.shared.dto.BlastResultInfo;
import org.jbei.ice.shared.dto.EntryInfo;

/**
 * ABI to perform searches in the full text and blast indexes.
 * 
 * @author Timothy Ham, Zinovii Dmytriv
 * 
 */
public class SearchController extends Controller {
    public SearchController(Account account) {
        super(account, new EntryPermissionVerifier());
    }

    /**
     * Perform full text search on the query.
     * 
     * @param query
     * @return ArrayList of {@link SearchResult}s.
     * @throws ControllerException
     */
    public ArrayList<SearchResult> find(String query) throws ControllerException {
        ArrayList<SearchResult> results = new ArrayList<SearchResult>();
        if (query == null) {
            return results;
        }
        String cleanedQuery = query;
        cleanedQuery = cleanedQuery.replace(":", " ");
        cleanedQuery = cleanedQuery.replace(";", " ");
        cleanedQuery = cleanedQuery.replace("\\", " ");
        cleanedQuery = cleanedQuery.replace("[", "\\[");
        cleanedQuery = cleanedQuery.replace("]", "\\]");
        cleanedQuery = cleanedQuery.replace("{", "\\{");
        cleanedQuery = cleanedQuery.replace("}", "\\}");
        cleanedQuery = cleanedQuery.replace("(", "\\(");
        cleanedQuery = cleanedQuery.replace(")", "\\)");
        cleanedQuery = cleanedQuery.replace("+", "\\+");
        cleanedQuery = cleanedQuery.replace("-", "\\-");
        cleanedQuery = cleanedQuery.replace("'", "\\'");
        cleanedQuery = cleanedQuery.replace("\"", "\\\"");
        cleanedQuery = cleanedQuery.replace("^", "\\^");
        cleanedQuery = cleanedQuery.replace("&", "\\&");

        cleanedQuery = (cleanedQuery.endsWith("\\") ? cleanedQuery.substring(0,
            cleanedQuery.length() - 1) : cleanedQuery);
        if (cleanedQuery.startsWith("*")) {
            cleanedQuery = cleanedQuery.substring(1);
        }
        if (cleanedQuery.startsWith("?")) {
            cleanedQuery = cleanedQuery.substring(1);
        }
        try {
            UsageLogger.info(String.format("Searching for: %s", cleanedQuery));

            EntryController entryController = new EntryController(getAccount());

            ArrayList<SearchResult> searchResults = AggregateSearch.query(cleanedQuery,
                getAccount());
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

    /**
     * Perform a blastn search of the query.
     * 
     * @param query
     * @return ArrayList of {@link BlastResult}s.
     * @throws ProgramTookTooLongException
     * @throws ControllerException
     */
    public ArrayList<BlastResult> blastn(String query) throws ProgramTookTooLongException,
            ControllerException {
        return blast(query, "blastn");
    }

    /**
     * Perform a translated blast search of the query (tblastx).
     * 
     * @param query
     * @return ArrayList of {@link BlastResult}s.
     * @throws ProgramTookTooLongException
     * @throws ControllerException
     */
    public ArrayList<BlastResult> tblastx(String query) throws ProgramTookTooLongException,
            ControllerException {
        return blast(query, "tblastx");
    }

    /**
     * Perform a blast search of the query given the program name.
     * 
     * @param query
     * @param program
     *            Blast program name.
     * @return ArrayList of {@link BlastResult}s.
     * @throws ProgramTookTooLongException
     * @throws ControllerException
     */
    protected ArrayList<BlastResult> blast(String query, String program)
            throws ProgramTookTooLongException, ControllerException {

        ArrayList<BlastResult> results = new ArrayList<BlastResult>();

        try {
            Logger.info(String.format("Blast '%s' searching for %s", program, query));

            EntryController entryController = new EntryController(getAccount());

            Blast blast = new Blast();

            ArrayList<BlastResult> blastResults = blast.query(query, program);
            if (blastResults != null) {
                for (BlastResult blastResult : blastResults) {
                    Entry entry = EntryManager.getByRecordId(blastResult.getSubjectId());

                    if (entry != null && entryController.hasReadPermission(entry)) {
                        blastResult.setEntry(entry);

                        results.add(blastResult);
                    }
                }
            }

            Logger.info(String.format("Blast found %d results",
                (results == null) ? 0 : results.size()));
        } catch (BlastException e) {
            throw new ControllerException(e);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        } catch (ProgramTookTooLongException e) {
            throw new ProgramTookTooLongException(e);
        }

        return results;
    }

    // this should eventually replace the above methods for running blast to prevent having to 
    // an additional iteration through the blast results to convert to BlastResultInfo

    public ArrayList<BlastResultInfo> runBlastN(String query) throws ProgramTookTooLongException,
            ControllerException {
        return runBlast(query, "blastn");
    }

    public ArrayList<BlastResultInfo> runTblastx(String query) throws ProgramTookTooLongException,
            ControllerException {
        return runBlast(query, "tblastx");
    }

    protected ArrayList<BlastResultInfo> runBlast(String query, String program)
            throws ProgramTookTooLongException, ControllerException {
        ArrayList<BlastResultInfo> results = new ArrayList<BlastResultInfo>();

        try {
            Logger.info(String.format("Blast '%s' searching for %s", program, query));

            EntryController entryController = new EntryController(getAccount());
            Blast blast = new Blast();

            ArrayList<BlastResult> blastResults = blast.query(query, program);
            if (blastResults != null) {
                for (BlastResult blastResult : blastResults) {
                    Entry entry = EntryManager.getByRecordId(blastResult.getSubjectId());

                    if (entry != null && entryController.hasReadPermission(entry)) {
                        blastResult.setEntry(entry);

                        // slowness here
                        BlastResultInfo info = new BlastResultInfo();
                        info.setBitScore(blastResult.getBitScore());

                        EntryInfo view = EntryToInfoFactory.getSummaryInfo(blastResult.getEntry());
                        info.setEntryInfo(view);

                        info.seteValue(blastResult.geteValue());
                        info.setAlignmentLength(blastResult.getAlignmentLength());
                        info.setPercentId(blastResult.getPercentId());
                        info.setQueryLength(query.length());
                        results.add(info);
                    }
                }
            }

            Logger.info(String.format("Blast found %d results",
                (results == null) ? 0 : results.size()));
        } catch (BlastException e) {
            throw new ControllerException(e);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return results;
    }

}
