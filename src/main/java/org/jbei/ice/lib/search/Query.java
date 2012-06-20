package org.jbei.ice.lib.search;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.EntryUtil;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.EntryFundingSource;
import org.jbei.ice.lib.entry.model.Part;
import org.jbei.ice.lib.entry.model.PartNumber;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.search.blast.Blast;
import org.jbei.ice.lib.search.blast.BlastException;
import org.jbei.ice.lib.search.blast.BlastResult;
import org.jbei.ice.lib.search.blast.ProgramTookTooLongException;
import org.jbei.ice.lib.search.filter.BlastFilter;
import org.jbei.ice.lib.search.filter.Filter;
import org.jbei.ice.lib.search.filter.RadioFilter;
import org.jbei.ice.lib.search.filter.SelectionFilter;
import org.jbei.ice.lib.search.filter.StringFilter;
import org.jbei.ice.lib.utils.UtilsController;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

/*
 * TODO This class was written as a translation from a dynamic language. A cleaner 
 * refactoring would be beneficial.
 */

/**
 * Advanced search queries.
 * <p/>
 * This class allows searches based on database fields, combination of database fields, blast, etc.
 *
 * @author Timothy Ham, Zinovii Dmytriv
 */
@SuppressWarnings("unchecked")
public class Query {

    private final ArrayList<Filter> filters = new ArrayList<Filter>();
    private final SearchController searchController;
    private static Query instance = null;

    /**
     * Obtain singleton instance of {@link Query} object.
     *
     * @return Instance of a Query object.
     */
    public static Query getInstance() {
        if (instance == null) {
            instance = new Query();
        }

        return instance;
    }

    private Query() {
        initializeFilters();
        searchController = new SearchController();
    }

    /**
     * Set up all known filters.
     */
    private void initializeFilters() {
        Map<String, String> selectionMarkersMap = new LinkedHashMap<String, String>();
        TreeSet<String> uniqueSelectionMarkers = null;
        UtilsController controller = new UtilsController();

        try {
            uniqueSelectionMarkers = new TreeSet<String>(controller.getUniqueSelectionMarkers());
        } catch (ControllerException e) {
            String msg = "Could not get unique selection markers in Query";
            Logger.error(msg, e);
        }
        for (String selectionMarker : uniqueSelectionMarkers) {
            selectionMarkersMap.put(selectionMarker, selectionMarker);
        }

        Map<String, String> promotersMap = new LinkedHashMap<String, String>();
        TreeSet<String> uniquePromoters = new TreeSet<String>(controller.getUniquePromoters());
        for (String promoter : uniquePromoters) {
            promotersMap.put(promoter, promoter);
        }

        Map<String, String> originOfReplicationsMap = new LinkedHashMap<String, String>();
        TreeSet<String> uniqueOriginOfReplications = new TreeSet<String>(controller.getUniqueOriginOfReplications());
        for (String originOfReplication : uniqueOriginOfReplications) {
            originOfReplicationsMap.put(originOfReplication, originOfReplication);
        }

        Map<String, String> yesNoMap = new LinkedHashMap<String, String>();
        yesNoMap.put("yes", "Yes");
        yesNoMap.put("no", "No");

        filters.add(new StringFilter("name_or_alias", "Name Or Alias", "filterNameOrAlias"));
        //filters.add(new StringFilter("name", "Name", "filterName"));
        filters.add(new StringFilter("part_number", "Part ID", "filterPartNumber"));
        filters.add(new SelectionFilter("type", "Type", "filterType", EntryUtil
                .getEntryTypeOptionsMap()));
        filters.add(new SelectionFilter("status", "Status", "filterStatus", Entry
                .getStatusOptionsMap()));
        filters.add(new StringFilter("owner", "Owner", "filterOwnerCombined"));
        filters.add(new StringFilter("creator", "Creator", "filterCreatorCombined"));
        //filters.add(new StringFilter("alias", "Alias", "filterAlias"));
        filters.add(new StringFilter("keywords", "Keywords", "filterKeywords"));
        filters.add(new StringFilter("description", "Description (Summary/Notes/References)",
                                     "filterSummaryNotesReferences"));
        filters.add(new RadioFilter("has_attachment", "Has Attachment", "filterHasAttachment",
                                    yesNoMap));
        filters.add(new RadioFilter("has_sequence", "Has Sequence", "filterHasSequence", yesNoMap));
        filters.add(new RadioFilter("has_sample", "Has Sample", "filterHasSample", yesNoMap));
        //filters.add(new StringFilter("short_description", "Summary", "filterShortDescription"));
        //filters.add(new StringFilter("long_description", "Notes", "filterLongDescription"));
        //filters.add(new StringFilter("references", "References", "filterReferences"));
        filters.add(new SelectionFilter("bioSafetyLevel", "Bio Safety Level",
                                        "filterBioSafetyLevel", Entry.getBioSafetyLevelOptionsMap()));
        filters.add(new StringFilter("intelectualProperty", "Intelectual Property",
                                     "filterIntelectualProperty"));
        filters.add(new StringFilter("principal_investigator", "Principal Investigator",
                                     "filterPrincipalInvestigator"));
        filters.add(new StringFilter("fundingSource", "Funding Source", "filterFundingSource"));
        filters.add(new SelectionFilter("selection_marker",
                                        "Selection Marker (Strains and Plasmids)", "filterSelectionMarker",
                                        selectionMarkersMap));
        filters.add(new StringFilter("backbone", "Backbone (Plasmids only)", "filterBackbone"));
        filters.add(new SelectionFilter("promoters", "Promoters (Plasmids only)",
                                        "filterPromoters", promotersMap));
        filters.add(new SelectionFilter("origin_of_replication",
                                        "Origin Of Replication (Plasmids only)", "filterOriginOfReplication",
                                        originOfReplicationsMap));
        filters.add(new StringFilter("host", "Host (Strains only)", "filterHost"));
        filters.add(new StringFilter("strain_plasmids", "Strain Plasmids (Strains only)",
                                     "filterStrainPlasmids"));
        filters.add(new StringFilter("genotype_phenotype", "Genotype/Phenotype (Strains only)",
                                     "filterGenotypePhenotype"));
        filters.add(new SelectionFilter("package_format", "Package Format (Parts only)",
                                        "filterPackageFormat", Part.getPackageFormatOptionsMap()));
        filters.add(new StringFilter("record_id", "Record Id", "filterRecordId"));
        filters.add(new BlastFilter("blastn", "BLAST-Nucleotide", "filterBlast"));
        filters.add(new BlastFilter("tblastx", "BLAST-Translate", "filterBlast"));
    }

    /**
     * Retrieve a filter by its key.
     *
     * @param key
     * @return
     */
    private Filter filterByKey(String key) {
        Filter result = null;

        for (int i = 0; i < filters.size(); i++) {
            if (key.equals(filters.get(i).key)) {
                result = filters.get(i);
                break;
            }
        }

        return result;
    }

    /**
     * Query using the filter specified by the key.
     *
     * @param key   name of the filter to query.
     * @param value query value.
     * @return
     * @throws QueryException
     */
    private HashSet<Integer> runFilter(String key, String value) throws QueryException {
        HashSet<Integer> result = null;

        try {
            Filter queryType = filterByKey(key);

            result = (HashSet<Integer>) this.getClass()
                                            .getDeclaredMethod(queryType.method, String.class).invoke(this, value);
        } catch (IllegalArgumentException e) {
            throw new QueryException(e);
        } catch (IllegalAccessException e) {
            throw new QueryException(e);
        } catch (InvocationTargetException e) {
            throw new QueryException(e);
        } catch (SecurityException e) {
            throw new QueryException(e);
        } catch (NoSuchMethodException e) {
            throw new QueryException(e);
        }

        return result;
    }

    /**
     * Search using filter syntax given by data.
     *
     * @param data filter syntax.
     * @return List of {@link Entry} ids
     * @throws QueryException
     */
    public ArrayList<Long> query(ArrayList<String[]> data) throws QueryException {
        ArrayList<Long> resultIds = new ArrayList<Long>();

        boolean firstRun = true;

        try {
            for (String[] item : data) {
                if (firstRun) {
                    @SuppressWarnings("rawtypes")
                    HashSet queryResultSet = runFilter(item[0], item[1]);

                    for (Iterator<Long> iterator = queryResultSet.iterator(); iterator.hasNext(); ) {
                        resultIds.add(iterator.next());
                    }

                    firstRun = false;
                } else {
                    resultIds.retainAll(runFilter(item[0], item[1]));
                }
            }
        } catch (Exception e) {
            throw new QueryException(e);
        }

        return resultIds;
    }

    public final ArrayList<Filter> filters() {
        return filters;
    }

    /**
     * Convert filter selection options into hql query terms.
     *
     * @param field    field to operate on.
     * @param operator ~ is contains, !~ is not contain, = is exact equal, ! is exact not, ^ is matches
     *                 start, $ is matches end.
     * @param term     query term.
     * @return HQL query term for field.
     */
    protected String makeCriterion(String field, String operator, String term) {
        String result = null;
        term = term.toLowerCase();
        if (operator.equals("~")) {
            result = field + " like '%" + term + "%'";
        } else if (operator.equals("!~")) {
            result = field + " not like '%" + term + "%'" + " or " + field + " is null ";
        } else if (operator.equals("=")) {
            if (term.isEmpty()) {
                result = field + " = '' or " + field + " = null";
            } else {
                result = field + " = '" + term + "'";
            }
        } else if (operator.equals("!")) {
            if (term.isEmpty()) {
                result = field + " != '' and " + field + " != null";
            } else {
                result = field + " != '" + term + "'";
            }
        } else if (operator.equals("^")) {
            result = field + " like '" + term + "%'";
        } else if (operator.equals("$")) {
            result = field + " like '%" + term + "'";
        }
        return result;
    }

    /**
     * Parse the syntax in value into operator and the query string.
     *
     * @param value query syntax.
     * @return HashMap containing operator and value.
     */
    protected HashMap<String, String> parseQuery(String value) {
        HashMap<String, String> result = new HashMap<String, String>();
        String operator = "";
        String newValue = "";

        if (value.startsWith("~")) {
            operator = "~";
            newValue = value.substring(1);
        } else if (value.startsWith("!~")) {
            operator = "!~";
            newValue = value.substring(2);
        } else if (value.startsWith("=")) {
            operator = "=";
            newValue = value.substring(1);
        } else if (value.startsWith("!")) {
            operator = "!";
            newValue = value.substring(1);
        } else if (value.startsWith("^")) {
            operator = "^";
            newValue = value.substring(1);
        } else if (value.startsWith("$")) {
            operator = "$";
            newValue = value.substring(1);
        } else {
            operator = "=";
            newValue = value;
        }
        result.put("operator", operator);
        result.put("value", newValue);

        return result;

    }

    /**
     * Filter by {@link org.jbei.ice.lib.entry.model.Name Name}.name field.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterName(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);

        String criteria = makeCriterion("lower(name.name)", parsedQuery.get("operator"),
                                        parsedQuery.get("value"));

        String query = "select distinct name.entry.id from Name name where " + criteria;
        try {
            return searchController.hibernateQuery(query);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }

    /**
     * Filter by {@link Strain}.plasmids field.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterStrainPlasmids(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);

        String criteria = makeCriterion("lower(strain.plasmids)", parsedQuery.get("operator"),
                                        parsedQuery.get("value"));

        String query = "select distinct strain.id from Strain strain where " + criteria;
        try {
            return searchController.hibernateQuery(query);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }

    /**
     * Filter by {@link Entry}.recordType field.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterType(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.recordType)", parsedQuery.get("operator"),
                                        parsedQuery.get("value"));

        String query = "select distinct entry.id from Entry entry where " + criteria;
        try {
            return searchController.hibernateQuery(query);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }

    /**
     * Filter by {@link Entry}.alias field.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterAlias(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.alias)", parsedQuery.get("operator"),
                                        parsedQuery.get("value"));

        String query = "select distinct entry.id from Entry entry where " + criteria;
        try {
            return searchController.hibernateQuery(query);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }

    /**
     * Filter by {@link PartNumber}.partNumber field.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterPartNumber(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(partNumber.partNumber)",
                                        parsedQuery.get("operator"), parsedQuery.get("value"));

        String query = "select distinct partNumber.entry.id from PartNumber partNumber where "
                + criteria;
        try {
            return searchController.hibernateQuery(query);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }

    /**
     * Filter for existence of any {@link org.jbei.ice.lib.entry.attachment.Attachment Attachment}s.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterHasAttachment(String queryString) {
        HashSet<Long> result = null;
        try {

            HashSet<Long> allEntriesWithAttachment = null;
            HashSet<Long> allEntries = null;
            String query = "select distinct attachment.entry.id from Attachment attachment";
            allEntriesWithAttachment = searchController.hibernateQuery(query);

            if (queryString.equals("yes")) {
                result = allEntriesWithAttachment;
            } else {

                query = "select entry.id from Entry entry";
                allEntries = searchController.hibernateQuery(query);

                allEntries.removeAll(allEntriesWithAttachment);
                result = allEntries;
            }
        } catch (ControllerException ce) {
            Logger.error(ce);
        }

        return result;
    }

    /**
     * Filter for existence of any {@link org.jbei.ice.lib.entry.sample.model.Sample Sample}s.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterHasSample(String queryString) {
        HashSet<Long> result = null;
        HashSet<Long> allEntriesWithSample = null;
        HashSet<Long> allEntries = null;

        String query = "select distinct sample.entry.id from Sample sample";
        try {
            allEntriesWithSample = searchController.hibernateQuery(query);

            if (queryString.equals("yes")) {
                result = allEntriesWithSample;
            } else {
                query = "select entry.id from Entry entry";
                allEntries = searchController.hibernateQuery(query);

                allEntries.removeAll(allEntriesWithSample);
                result = allEntries;
            }

        } catch (ControllerException ce) {
            Logger.error(ce);
        }
        return result;
    }

    /**
     * Filter existence of any {@link org.jbei.ice.lib.models.Sequence Sequence}s.
     *
     * @param queryString
     * @return HashSet of {@link Entry} ids.
     */
    protected HashSet<Long> filterHasSequence(String queryString) {
        HashSet<Long> result = null;
        HashSet<Long> allEntriesWithSequence = null;
        HashSet<Long> allEntries = null;

        try {
            String query = "select distinct sequence.entry.id from Sequence sequence";
            allEntriesWithSequence = searchController.hibernateQuery(query);

            if (queryString.equals("yes")) {
                result = allEntriesWithSequence;
            } else {
                query = "select entry.id from Entry entry";
                allEntries = searchController.hibernateQuery(query);

                allEntries.removeAll(allEntriesWithSequence);
                result = allEntries;
            }
        } catch (ControllerException ce) {
            Logger.error(ce);
        }

        return result;
    }

    /**
     * Filter by {@link Entry}.recordId field.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterRecordId(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.recordId) ", parsedQuery.get("operator"),
                                        parsedQuery.get("value"));

        String query = "select distinct entry.id from Entry entry where " + criteria;
        try {
            return searchController.hibernateQuery(query);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }

    /**
     * Filter by {@link Entry}.owner and .ownerEmail fields.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterOwnerCombined(String queryString) {
        HashSet<Long> ownerResults = filterOwner(queryString);
        HashSet<Long> ownerEmailResults = filterOwnerEmail(queryString);

        ownerResults.addAll(ownerEmailResults);

        return ownerResults;
    }

    /**
     * Filter by {@link Entry}.owner field.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterOwner(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.owner) ", parsedQuery.get("operator"),
                                        parsedQuery.get("value"));

        String query = "select distinct entry.id from Entry entry where " + criteria;
        try {
            return searchController.hibernateQuery(query);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }

    /**
     * Filter by {@link Entry}.ownerEmail field.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterOwnerEmail(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.ownerEmail) ", parsedQuery.get("operator"),
                                        parsedQuery.get("value"));

        String query = "select distinct entry.id from Entry entry where " + criteria;
        try {
            return searchController.hibernateQuery(query);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }

    /**
     * Filter by {@link Entry}.creator and .creatorEmail fields.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterCreatorCombined(String queryString) {
        HashSet<Long> creatorResults = filterCreator(queryString);
        HashSet<Long> creatorEmailResults = filterCreatorEmail(queryString);

        creatorResults.addAll(creatorEmailResults);

        return creatorResults;
    }

    /**
     * Filter by {@link Entry}.creator field.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterCreator(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.creator) ", parsedQuery.get("operator"),
                                        parsedQuery.get("value"));

        String query = "select distinct entry.id from Entry entry where " + criteria;
        try {
            return searchController.hibernateQuery(query);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }

    /**
     * Filter by {@link Entry}.creatorEmail field.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterCreatorEmail(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);

        String criteria = makeCriterion("lower(entry.creatorEmail) ", parsedQuery.get("operator"),
                                        parsedQuery.get("value"));

        String query = "select distinct entry.id from Entry entry where " + criteria;
        try {
            return searchController.hibernateQuery(query);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }

    /**
     * Filter by {@link Entry}.keywords field.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterKeywords(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.keywords)", parsedQuery.get("operator"),
                                        parsedQuery.get("value"));

        String query = "select distinct entry.id from Entry entry where " + criteria;
        try {
            return searchController.hibernateQuery(query);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }

    /**
     * Filter by {@link Entry}.shortDescription, .longDescription, and .references fields.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterSummaryNotesReferences(String queryString) {
        HashSet<Long> summaryResults = filterShortDescription(queryString);
        HashSet<Long> notesResults = filterLongDescription(queryString);
        HashSet<Long> referencesResults = filterReferences(queryString);

        summaryResults.addAll(notesResults);
        summaryResults.addAll(referencesResults);

        return summaryResults;
    }

    /**
     * Filter by {@link Entry}.shortDescription field.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterShortDescription(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.shortDescription)",
                                        parsedQuery.get("operator"), parsedQuery.get("value"));

        String query = "select distinct entry.id from Entry entry where " + criteria;
        try {
            return searchController.hibernateQuery(query);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }

    /**
     * Filter by {@link Entry}.longDescription field.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterLongDescription(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.longDescription)",
                                        parsedQuery.get("operator"), parsedQuery.get("value"));

        String query = "select distinct entry.id from Entry entry where " + criteria;
        try {
            return searchController.hibernateQuery(query);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }

    /**
     * Filter by {@link Entry}.references field.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterReferences(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.references)", parsedQuery.get("operator"),
                                        parsedQuery.get("value"));

        String query = "select distinct entry.id from Entry entry where " + criteria;
        try {
            return searchController.hibernateQuery(query);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }

    /**
     * Filter by {@link Plasmid}.backbone field.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterBackbone(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(plasmid.backbone)", parsedQuery.get("operator"),
                                        parsedQuery.get("value"));

        String query = "select distinct plasmid.id from Plasmid plasmid where " + criteria;
        try {
            return searchController.hibernateQuery(query);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }

    /**
     * Filter by {@link Plasmid}.promoters field.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterPromoters(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(plasmid.promoters)", parsedQuery.get("operator"),
                                        parsedQuery.get("value"));

        String query = "select distinct plasmid.id from Plasmid plasmid where " + criteria;
        try {
            return searchController.hibernateQuery(query);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }

    /**
     * Filter by {@link Plasmid}.originOfReplication field.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterOriginOfReplication(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(plasmid.originOfReplication)",
                                        parsedQuery.get("operator"), parsedQuery.get("value"));

        String query = "select distinct plasmid.id from Plasmid plasmid where " + criteria;
        try {
            return searchController.hibernateQuery(query);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }

    /**
     * Filter by {@link Strain}.host field.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterHost(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(strain.host)", parsedQuery.get("operator"),
                                        parsedQuery.get("value"));

        String query = "select distinct strain.id from Strain strain where " + criteria;
        try {
            return searchController.hibernateQuery(query);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }

    /**
     * Filter by {@link Strain}.genotypePhenotype field.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterGenotypePhenotype(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(strain.genotypePhenotype)",
                                        parsedQuery.get("operator"), parsedQuery.get("value"));

        String query = "select distinct strain.id from Strain strain where " + criteria;
        try {
            return searchController.hibernateQuery(query);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }

    /**
     * Filter by {@link Part}.packageFormat field.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterPackageFormat(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(part.packageFormat)", parsedQuery.get("operator"),
                                        parsedQuery.get("value"));

        String query = "select distinct part.id from Part part where " + criteria;
        try {
            return searchController.hibernateQuery(query);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }

    /**
     * Filter by {@link SelectionMarker}.name field.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterSelectionMarker(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(marker.name)", parsedQuery.get("operator"),
                                        parsedQuery.get("value"));

        String query = "select distinct marker.entry.id from SelectionMarker marker where "
                + criteria;
        try {
            return searchController.hibernateQuery(query);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }

    /**
     * Filter by {@link Entry}.status field.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterStatus(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.status)", parsedQuery.get("operator"),
                                        parsedQuery.get("value"));

        String query = "select distinct entry.id from Entry entry where " + criteria;
        try {
            return searchController.hibernateQuery(query);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }

    /**
     * Filter by {@link Strain}.plasmids field.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterStrainPlasmid(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(strain.plasmids)", parsedQuery.get("operator"),
                                        parsedQuery.get("value"));

        String query = "select distinct strain.id from Strain strain where " + criteria;
        try {
            return searchController.hibernateQuery(query);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }

    /**
     * Filter by {@link org.jbei.ice.lib.entry.model.Name Name}.name or {@link Entry}.alias fields.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterNameOrAlias(String queryString) {
        HashSet<Long> nameResults = filterName(queryString);
        HashSet<Long> aliasResults = filterAlias(queryString);

        nameResults.addAll(aliasResults);

        return nameResults;
    }

    /**
     * Filter by {@link EntryFundingSource}.principalInvestigator field.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterPrincipalInvestigator(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);

        String criteria = makeCriterion(
                "lower(entryFundingSource.fundingSource.principalInvestigator)",
                parsedQuery.get("operator"), parsedQuery.get("value"));

        String query = "select distinct entry.id from " + EntryFundingSource.class.getName()
                + " entryFundingSource where " + criteria;
        try {
            return searchController.hibernateQuery(query);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }

    /**
     * Filter by {@link EntryFundingSource}.fundingSource field.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterFundingSource(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);

        String criteria = makeCriterion("lower(entryFundingSource.fundingSource.fundingSource)",
                                        parsedQuery.get("operator"), parsedQuery.get("value"));

        String query = "select distinct entry.id from " + EntryFundingSource.class.getName()
                + " entryFundingSource where " + criteria;
        try {
            return searchController.hibernateQuery(query);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }

    /**
     * Filter by {@link Entry}.intellectualProperty field.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterIntelectualProperty(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);

        String criteria = makeCriterion("lower(entry.intellectualProperty)",
                                        parsedQuery.get("operator"), parsedQuery.get("value"));

        String query = "select distinct entry.id from " + Entry.class.getName() + " entry where "
                + criteria;

        try {
            return searchController.hibernateQuery(query);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }

    /**
     * Filter by {@link Entry}.bioSafety field, less than equal to inclusive.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterBioSafetyLevel(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);

        String operator = parsedQuery.get("operator");

        String criteria = null;

        if (operator.equals("=")) {
            criteria = "entry.bioSafetyLevel = " + parsedQuery.get("value");
        } else if (operator.equals("!")) {
            criteria = "entry.bioSafetyLevel != " + parsedQuery.get("value");
        }
        String query = "select distinct entry.id from " + Entry.class.getName() + " entry where "
                + criteria;

        try {
            return searchController.hibernateQuery(query);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }

    /**
     * Filter by blast query.
     *
     * @param queryString
     * @return HashSet of {link Entry} ids.
     */
    protected HashSet<Long> filterBlast(String queryString) {
        String[] parameters = queryString.split(","); //query, type, %ident, minLength
        String query = parameters[0];
        String type = parameters[1];
        double minPercentIdentity = 90.0;
        int minLength = 10;
        try {
            minPercentIdentity = Double.parseDouble(parameters[2]);
            minLength = Integer.parseInt(parameters[3]);
        } catch (NumberFormatException e) {
            // could not format numbers. Continue with defaults
        } catch (ArrayIndexOutOfBoundsException e1) {
            // could not find numbers. Continue with defaults
        }
        return blastQuery(query, type, minPercentIdentity, minLength);
    }

    /**
     * Perform blast query.
     *
     * @param queryString        sequence to be queried.
     * @param type               blast program name.
     * @param minPercentIdentity lower bound percent identity filter.
     * @param minLength          lower bound length filter.
     * @return HashSet of {link Entry} ids.
     */
    private HashSet<Long> blastQuery(String queryString, String type, double minPercentIdentity,
            int minLength) {
        HashSet<Long> rawResults = new HashSet<Long>();
        Blast b = new Blast();
        EntryController entryController = new EntryController();
        AccountController controller = new AccountController();
        try {
            Account systemAccount = controller.getSystemAccount();

            ArrayList<BlastResult> blastResults = b.query(queryString, type);

            if (blastResults != null) {
                for (BlastResult blastResult : blastResults) {
                    if (blastResult.getPercentId() >= minPercentIdentity
                            && blastResult.getAlignmentLength() >= minLength) {
                        Entry entry;
                        try {
                            entry = entryController.getByRecordId(systemAccount,
                                                                  blastResult.getSubjectId());
                        } catch (PermissionException e) {
                            Logger.warn("Could not add entry to blast result because of insufficient permissions");
                            continue;
                        }
                        rawResults.add(entry.getId());
                    }
                }
            }

        } catch (ProgramTookTooLongException e) {
            // return empty result for this query
            Logger.info("Could not run advanced blastn: " + e.toString());
        } catch (BlastException e) {
            // return empty result for this query
            Logger.info("Could not run advanced blastn: " + e.toString());
        } catch (ControllerException e) {
            // return empty result for this query
            Logger.info("Could not run advanced blastn: " + e.toString());
        }

        return rawResults;
    }
}
