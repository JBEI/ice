package org.jbei.ice.lib.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.permissions.PermissionsController;
import org.jbei.ice.lib.search.blast.Blast;
import org.jbei.ice.lib.search.blast.BlastException;
import org.jbei.ice.lib.search.blast.BlastResult;
import org.jbei.ice.lib.search.blast.ProgramTookTooLongException;
import org.jbei.ice.server.EntryToInfoFactory;
import org.jbei.ice.server.QueryFilter;
import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.SearchFilterType;
import org.jbei.ice.shared.dto.BlastResultInfo;
import org.jbei.ice.shared.dto.EntryInfo;

/**
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 */
public class SearchController {
    private final SearchDAO dao;
    private final PermissionsController permissionsController;

    public SearchController() {
        dao = new SearchDAO();
        permissionsController = new PermissionsController();
    }

    public Set<Long> runSearch(Account account, ArrayList<QueryFilter> filters) throws ControllerException {
        if (filters == null || filters.isEmpty())
            return new HashSet<Long>();

        Set<Long> results = null;
        Set<Long> stringQueryResult = new HashSet<Long>(); // plain text query typed into the search box. has no type
        EntryController entryController = new EntryController();
        boolean hasStringQuery = false;

        for (QueryFilter filter : filters) {

            SearchFilterType type = filter.getSearchType();
            String operand = filter.getOperand();
            if (operand == null || operand.trim().isEmpty())
                continue;

            if (type == null) {
                hasStringQuery = true;
                ArrayList<SearchResult> searchResults = find(account, operand);
                if (searchResults != null) {

                    // filter results by permission
                    for (SearchResult searchResult : searchResults) {
                        Entry entry = searchResult.getEntry();
                        stringQueryResult.add(entry.getId());
                    }
                }
            } else {

                QueryOperator operator = filter.getOperator();
                try {
                    Set<Long> intermediateResults = dao.runSearchFilter(type, operator,
                                                                        operand);

                    if (results == null) {
                        results = new HashSet<Long>();
                        results.addAll(intermediateResults);
                    } else {
                        results.retainAll(intermediateResults);
                        if (results.isEmpty())
                            break;
                    }
                } catch (DAOException me) {
                    throw new ControllerException(me);
                }
            }
        }

        // post process
        if (hasStringQuery) {
            if (results != null)
                stringQueryResult.retainAll(results);
            return stringQueryResult;
        } else {
            if (results == null)
                return new HashSet<Long>();

            Iterator<Long> resultsIter = results.iterator();

            while (resultsIter.hasNext()) {
                Long next = resultsIter.next();
                try {
                    Entry nextEntry;
                    try {
                        nextEntry = entryController.get(account, next);
                    } catch (PermissionException e) {
                        Logger.error(e);
                        continue;
                    }
                    if (!permissionsController.hasReadPermission(account, nextEntry))
                        resultsIter.remove();
                } catch (ControllerException ce) {
                    Logger.error("Error retrieving permission for entry Id " + next);
                }
            }
            return results;
        }
    }

    /**
     * Perform full text search on the query.
     *
     * @param query
     * @return ArrayList of {@link SearchResult}s.
     * @throws ControllerException
     */
    public ArrayList<SearchResult> find(Account account, String query) throws ControllerException {
        ArrayList<SearchResult> results = new ArrayList<SearchResult>();
        if (query == null) {
            return results;
        }

        String cleanedQuery = cleanQuery(query);

        try {
            Logger.info("Searching for \"" + cleanedQuery + "\"");
            ArrayList<SearchResult> searchResults = AggregateSearch.query(cleanedQuery, account);
            if (searchResults != null) {
                for (SearchResult searchResult : searchResults) {
                    Entry entry = searchResult.getEntry();

                    if (permissionsController.hasReadPermission(account, entry)) {
                        results.add(searchResult);
                    }
                }
            }

            Logger.info(results.size() + " visible results found");
        } catch (SearchException e) {
            throw new ControllerException(e);
        }

        return results;
    }

    protected String cleanQuery(String query) {
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

        cleanedQuery = cleanedQuery.endsWith("'") ? cleanedQuery.substring(0, cleanedQuery.length() - 1) : cleanedQuery;
        cleanedQuery = (cleanedQuery.endsWith("\\") ? cleanedQuery.substring(0,
                                                                             cleanedQuery.length() - 1) : cleanedQuery);
        if (cleanedQuery.startsWith("*")) {
            cleanedQuery = cleanedQuery.substring(1);
        }
        return cleanedQuery;
    }

    /**
     * Perform a blastn search of the query.
     *
     * @param query
     * @return ArrayList of {@link BlastResult}s.
     * @throws ProgramTookTooLongException
     * @throws ControllerException
     */
    public ArrayList<BlastResult> blastn(Account account, String query) throws ProgramTookTooLongException,
            ControllerException {
        return blast(account, query, "blastn");
    }

    /**
     * Perform a translated blast search of the query (tblastx).
     *
     * @param query
     * @return ArrayList of {@link BlastResult}s.
     * @throws ProgramTookTooLongException
     * @throws ControllerException
     */
    public ArrayList<BlastResult> tblastx(Account account, String query) throws ProgramTookTooLongException,
            ControllerException {
        return blast(account, query, "tblastx");
    }

    /**
     * Perform a blast search of the query given the program name.
     *
     * @param query
     * @param program Blast program name.
     * @return ArrayList of {@link BlastResult}s.
     * @throws ProgramTookTooLongException
     * @throws ControllerException
     */
    protected ArrayList<BlastResult> blast(Account account, String query, String program)
            throws ProgramTookTooLongException, ControllerException {

        ArrayList<BlastResult> results = new ArrayList<BlastResult>();

        try {
            Logger.info(String.format("Blast '%s' searching for %s", program, query));

            EntryController entryController = new EntryController();

            Blast blast = new Blast();

            ArrayList<BlastResult> blastResults = blast.query(query, program);
            if (blastResults != null) {
                for (BlastResult blastResult : blastResults) {
                    Entry entry;
                    try {
                        entry = entryController.getByRecordId(account, blastResult.getSubjectId());
                    } catch (PermissionException e) {
                        Logger.error(e);
                        continue;
                    }
                    if (entry != null && permissionsController.hasReadPermission(account, entry)) {
                        blastResult.setEntry(entry);
                        results.add(blastResult);
                    }
                }
            }

            Logger.info("Blast found " + results.size() + " results");
        } catch (BlastException e) {
            throw new ControllerException(e);
        } catch (ProgramTookTooLongException e) {
            throw new ProgramTookTooLongException(e);
        }

        return results;
    }

    // this should eventually replace the above methods for running blast to prevent having to 
    // an additional iteration through the blast results to convert to BlastResultInfo

    public ArrayList<BlastResultInfo> runBlastN(Account account, String query) throws ProgramTookTooLongException,
            ControllerException {
        return runBlast(account, query, "blastn");
    }

    public ArrayList<BlastResultInfo> runTblastx(Account account, String query) throws ProgramTookTooLongException,
            ControllerException {
        return runBlast(account, query, "tblastx");
    }

    protected ArrayList<BlastResultInfo> runBlast(Account account, String query, String program)
            throws ProgramTookTooLongException, ControllerException {
        ArrayList<BlastResultInfo> results = new ArrayList<BlastResultInfo>();

        try {
            Logger.info(String.format("Blast '%s' searching for %s", program, query));

            EntryController entryController = new EntryController();
            Blast blast = new Blast();

            ArrayList<BlastResult> blastResults = blast.query(query, program);
            if (blastResults != null) {
                for (BlastResult blastResult : blastResults) {
                    Entry entry;
                    try {
                        entry = entryController.getByRecordId(account, blastResult.getSubjectId());
                    } catch (PermissionException e) {
                        Logger.error(e);
                        continue;
                    }

                    if (entry != null && permissionsController.hasReadPermission(account, entry)) {
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

            Logger.info("Blast found " + results.size() + " results");
        } catch (BlastException e) {
            throw new ControllerException(e);
        }

        return results;
    }

    public HashSet<Long> hibernateQuery(String queryString) throws ControllerException {
        HashSet<Long> rawResults = new HashSet<Long>();
        try {
            rawResults.addAll(dao.runHibernateQuery(queryString));
            return rawResults;
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }
}
