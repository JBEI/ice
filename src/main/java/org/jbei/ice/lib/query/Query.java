package org.jbei.ice.lib.query;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.UtilsManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.EntryFundingSource;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.search.blast.Blast;
import org.jbei.ice.lib.search.blast.BlastException;
import org.jbei.ice.lib.search.blast.BlastResult;
import org.jbei.ice.lib.search.blast.ProgramTookTooLongException;

/*
 * TODO This class was written as a translation from a dynamic language. A cleaner 
 * refactoring would be beneficial.
 */
@SuppressWarnings("unchecked")
public class Query {

    private final ArrayList<Filter> filters = new ArrayList<Filter>();

    private static Query instance = null;

    public static Query getInstance() {
        if (instance == null) {
            instance = new Query();
        }

        return instance;
    }

    private Query() {
        initializeFilters();
    }

    private void initializeFilters() {
        Map<String, String> selectionMarkersMap = new LinkedHashMap<String, String>();
        TreeSet<String> uniqueSelectionMarkers = null;
        try {
            uniqueSelectionMarkers = UtilsManager.getUniqueSelectionMarkers();
        } catch (ManagerException e) {
            String msg = "Could not get unique selection markers in Query";
            Logger.error(msg, e);
        }
        for (String selectionMarker : uniqueSelectionMarkers) {
            selectionMarkersMap.put(selectionMarker, selectionMarker);
        }

        Map<String, String> promotersMap = new LinkedHashMap<String, String>();
        TreeSet<String> uniquePromoters = UtilsManager.getUniquePromoters();
        for (String promoter : uniquePromoters) {
            promotersMap.put(promoter, promoter);
        }

        Map<String, String> originOfReplicationsMap = new LinkedHashMap<String, String>();
        TreeSet<String> uniqueOriginOfReplications = UtilsManager.getUniqueOriginOfReplications();
        for (String originOfReplication : uniqueOriginOfReplications) {
            originOfReplicationsMap.put(originOfReplication, originOfReplication);
        }

        Map<String, String> yesNoMap = new LinkedHashMap<String, String>();
        yesNoMap.put("yes", "Yes");
        yesNoMap.put("no", "No");

        filters.add(new StringFilter("name_or_alias", "Name Or Alias", "filterNameOrAlias"));
        //filters.add(new StringFilter("name", "Name", "filterName"));
        filters.add(new StringFilter("part_number", "Part ID", "filterPartNumber"));
        filters.add(new SelectionFilter("type", "Type", "filterType", Entry
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
        filters.add(new BlastFilter("blastn", "blastn", "filterBlastn"));
    }

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

    public ArrayList<Long> query(ArrayList<String[]> data) throws QueryException {
        ArrayList<Long> resultIds = new ArrayList<Long>();

        boolean firstRun = true;

        try {
            for (String[] item : data) {
                if (firstRun) {
                    @SuppressWarnings("rawtypes")
                    HashSet queryResultSet = runFilter(item[0], item[1]);

                    for (Iterator<Long> iterator = queryResultSet.iterator(); iterator.hasNext();) {
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

    protected HashSet<Long> filterName(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);

        String criteria = makeCriterion("lower(name.name)", parsedQuery.get("operator"),
            parsedQuery.get("value"));

        String query = "select distinct name.entry.id from Name name where " + criteria;
        return hibernateQuery(query);
    }

    protected HashSet<Long> filterStrainPlasmids(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);

        String criteria = makeCriterion("lower(strain.plasmids)", parsedQuery.get("operator"),
            parsedQuery.get("value"));

        String query = "select distinct strain.id from Strain strain where " + criteria;
        return hibernateQuery(query);
    }

    protected HashSet<Long> filterType(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.recordType)", parsedQuery.get("operator"),
            parsedQuery.get("value"));

        String query = "select distinct entry.id from Entry entry where " + criteria;
        return hibernateQuery(query);
    }

    protected HashSet<Long> filterAlias(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.alias)", parsedQuery.get("operator"),
            parsedQuery.get("value"));

        String query = "select distinct entry.id from Entry entry where " + criteria;
        return hibernateQuery(query);
    }

    protected HashSet<Long> filterPartNumber(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(partNumber.partNumber)",
            parsedQuery.get("operator"), parsedQuery.get("value"));

        String query = "select distinct partNumber.entry.id from PartNumber partNumber where "
                + criteria;
        return hibernateQuery(query);
    }

    protected HashSet<Long> filterHasAttachment(String queryString) {
        HashSet<Long> result = null;
        HashSet<Long> allEntriesWithAttachment = null;
        HashSet<Long> allEntries = null;
        String query = "select distinct attachment.entry.id from Attachment attachment";
        allEntriesWithAttachment = hibernateQuery(query);

        if (queryString.equals("yes")) {
            result = allEntriesWithAttachment;
        } else {

            query = "select entry.id from Entry entry";
            allEntries = hibernateQuery(query);

            allEntries.removeAll(allEntriesWithAttachment);
            result = allEntries;
        }

        return result;
    }

    protected HashSet<Long> filterHasSample(String queryString) {
        HashSet<Long> result = null;
        HashSet<Long> allEntriesWithSample = null;
        HashSet<Long> allEntries = null;

        String query = "select distinct sample.entry.id from Sample sample";
        allEntriesWithSample = hibernateQuery(query);

        if (queryString.equals("yes")) {
            result = allEntriesWithSample;
        } else {
            query = "select entry.id from Entry entry";
            allEntries = hibernateQuery(query);

            allEntries.removeAll(allEntriesWithSample);
            result = allEntries;
        }

        return result;
    }

    protected HashSet<Long> filterHasSequence(String queryString) {
        HashSet<Long> result = null;
        HashSet<Long> allEntriesWithSequence = null;
        HashSet<Long> allEntries = null;

        String query = "select distinct sequence.entry.id from Sequence sequence";
        allEntriesWithSequence = hibernateQuery(query);

        if (queryString.equals("yes")) {
            result = allEntriesWithSequence;
        } else {
            query = "select entry.id from Entry entry";
            allEntries = hibernateQuery(query);

            allEntries.removeAll(allEntriesWithSequence);
            result = allEntries;
        }

        return result;
    }

    protected HashSet<Long> filterRecordId(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.recordId) ", parsedQuery.get("operator"),
            parsedQuery.get("value"));

        String query = "select distinct entry.id from Entry entry where " + criteria;
        return hibernateQuery(query);
    }

    protected HashSet<Long> filterOwnerCombined(String queryString) {
        HashSet<Long> ownerResults = filterOwner(queryString);
        HashSet<Long> ownerEmailResults = filterOwnerEmail(queryString);

        ownerResults.addAll(ownerEmailResults);

        return ownerResults;
    }

    protected HashSet<Long> filterOwner(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.owner) ", parsedQuery.get("operator"),
            parsedQuery.get("value"));

        String query = "select distinct entry.id from Entry entry where " + criteria;
        return hibernateQuery(query);
    }

    protected HashSet<Long> filterOwnerEmail(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.ownerEmail) ", parsedQuery.get("operator"),
            parsedQuery.get("value"));

        String query = "select distinct entry.id from Entry entry where " + criteria;
        return hibernateQuery(query);
    }

    protected HashSet<Long> filterCreatorCombined(String queryString) {
        HashSet<Long> creatorResults = filterCreator(queryString);
        HashSet<Long> creatorEmailResults = filterCreatorEmail(queryString);

        creatorResults.addAll(creatorEmailResults);

        return creatorResults;
    }

    protected HashSet<Long> filterCreator(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.creator) ", parsedQuery.get("operator"),
            parsedQuery.get("value"));

        String query = "select distinct entry.id from Entry entry where " + criteria;
        return hibernateQuery(query);
    }

    protected HashSet<Long> filterCreatorEmail(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);

        String criteria = makeCriterion("lower(entry.creatorEmail) ", parsedQuery.get("operator"),
            parsedQuery.get("value"));

        String query = "select distinct entry.id from Entry entry where " + criteria;
        return hibernateQuery(query);
    }

    protected HashSet<Long> filterKeywords(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.keywords)", parsedQuery.get("operator"),
            parsedQuery.get("value"));

        String query = "select distinct entry.id from Entry entry where " + criteria;
        return hibernateQuery(query);
    }

    protected HashSet<Long> filterSummaryNotesReferences(String queryString) {
        HashSet<Long> summaryResults = filterShortDescription(queryString);
        HashSet<Long> notesResults = filterLongDescription(queryString);
        HashSet<Long> referencesResults = filterReferences(queryString);

        summaryResults.addAll(notesResults);
        summaryResults.addAll(referencesResults);

        return summaryResults;
    }

    protected HashSet<Long> filterShortDescription(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.shortDescription)",
            parsedQuery.get("operator"), parsedQuery.get("value"));

        String query = "select distinct entry.id from Entry entry where " + criteria;
        return hibernateQuery(query);
    }

    protected HashSet<Long> filterLongDescription(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.longDescription)",
            parsedQuery.get("operator"), parsedQuery.get("value"));

        String query = "select distinct entry.id from Entry entry where " + criteria;
        return hibernateQuery(query);
    }

    protected HashSet<Long> filterReferences(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.references)", parsedQuery.get("operator"),
            parsedQuery.get("value"));

        String query = "select distinct entry.id from Entry entry where " + criteria;
        return hibernateQuery(query);
    }

    protected HashSet<Long> filterBackbone(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(plasmid.backbone)", parsedQuery.get("operator"),
            parsedQuery.get("value"));

        String query = "select distinct plasmid.id from Plasmid plasmid where " + criteria;
        return hibernateQuery(query);
    }

    protected HashSet<Long> filterPromoters(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(plasmid.promoters)", parsedQuery.get("operator"),
            parsedQuery.get("value"));

        String query = "select distinct plasmid.id from Plasmid plasmid where " + criteria;
        return hibernateQuery(query);
    }

    protected HashSet<Long> filterOriginOfReplication(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(plasmid.originOfReplication)",
            parsedQuery.get("operator"), parsedQuery.get("value"));

        String query = "select distinct plasmid.id from Plasmid plasmid where " + criteria;
        return hibernateQuery(query);
    }

    protected HashSet<Long> filterHost(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(strain.host)", parsedQuery.get("operator"),
            parsedQuery.get("value"));

        String query = "select distinct strain.id from Strain strain where " + criteria;
        return hibernateQuery(query);
    }

    protected HashSet<Long> filterGenotypePhenotype(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(strain.genotypePhenotype)",
            parsedQuery.get("operator"), parsedQuery.get("value"));

        String query = "select distinct strain.id from Strain strain where " + criteria;
        return hibernateQuery(query);
    }

    protected HashSet<Long> filterPackageFormat(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(part.packageFormat)", parsedQuery.get("operator"),
            parsedQuery.get("value"));

        String query = "select distinct part.id from Part part where " + criteria;
        return hibernateQuery(query);
    }

    protected HashSet<Long> filterSelectionMarker(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(marker.name)", parsedQuery.get("operator"),
            parsedQuery.get("value"));

        String query = "select distinct marker.entry.id from SelectionMarker marker where "
                + criteria;
        return hibernateQuery(query);
    }

    protected HashSet<Long> filterStatus(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.status)", parsedQuery.get("operator"),
            parsedQuery.get("value"));

        String query = "select distinct entry.id from Entry entry where " + criteria;
        return hibernateQuery(query);
    }

    protected HashSet<Long> filterStrainPlasmid(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(strain.plasmids)", parsedQuery.get("operator"),
            parsedQuery.get("value"));

        String query = "select distinct strain.id from Strain strain where " + criteria;
        return hibernateQuery(query);
    }

    protected HashSet<Long> filterNameOrAlias(String queryString) {
        HashSet<Long> nameResults = filterName(queryString);
        HashSet<Long> aliasResults = filterAlias(queryString);

        nameResults.addAll(aliasResults);

        return nameResults;
    }

    protected HashSet<Long> filterPrincipalInvestigator(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);

        String criteria = makeCriterion(
            "lower(entryFundingSource.fundingSource.principalInvestigator)",
            parsedQuery.get("operator"), parsedQuery.get("value"));

        String query = "select distinct entry.id from " + EntryFundingSource.class.getName()
                + " entryFundingSource where " + criteria;
        return hibernateQuery(query);
    }

    protected HashSet<Long> filterFundingSource(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);

        String criteria = makeCriterion("lower(entryFundingSource.fundingSource.fundingSource)",
            parsedQuery.get("operator"), parsedQuery.get("value"));

        String query = "select distinct entry.id from " + EntryFundingSource.class.getName()
                + " entryFundingSource where " + criteria;
        return hibernateQuery(query);
    }

    protected HashSet<Long> filterIntelectualProperty(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);

        String criteria = makeCriterion("lower(entry.intellectualProperty)",
            parsedQuery.get("operator"), parsedQuery.get("value"));

        String query = "select distinct entry.id from " + Entry.class.getName() + " entry where "
                + criteria;

        return hibernateQuery(query);
    }

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

        return hibernateQuery(query);
    }

    protected HashSet<Long> filterBlastn(String queryString) {
        String[] parameters = queryString.split(","); //query, %ident, minLength
        String query = parameters[0];
        double minPercentIdentity = 90.0;
        int minLength = 10;
        try {
            minPercentIdentity = Double.parseDouble(parameters[1]);
            minLength = Integer.parseInt(parameters[2]);
        } catch (NumberFormatException e) {
            // could not format numbers. Continue with defaults
        }
        return blastnQuery(query, minPercentIdentity, minLength);
    }

    private HashSet<Long> hibernateQuery(String queryString) {
        HashSet<Long> rawResults = new HashSet<Long>();
        Session session = DAO.newSession();
        session.beginTransaction();
        org.hibernate.Query query = session.createQuery(queryString);

        try {
            rawResults = new HashSet<Long>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query ", e);
        } finally {
            session.getTransaction().rollback();
            if (session.isOpen()) {
                session.close();
            }
        }
        return rawResults;
    }

    private HashSet<Long> blastnQuery(String queryString, double minPercentIdentity, int minLength) {
        HashSet<Long> rawResults = new HashSet<Long>();
        Blast b = new Blast();

        try {
            ArrayList<BlastResult> blastResults = b.query(queryString, "blastn");

            if (blastResults != null) {
                for (BlastResult blastResult : blastResults) {
                    if (blastResult.getPercentId() >= minPercentIdentity
                            && blastResult.getAlignmentLength() >= minLength) {
                        Entry entry = EntryManager.getByRecordId(blastResult.getSubjectId());
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
        } catch (ManagerException e) {
            // return empty result for this query
            Logger.info("Could not run advanced blastn: " + e.toString());
        }

        return rawResults;
    }

    public static void main(String[] args) {
        /*Query q = new Query();
        // HashSet<Integer> results = q.filterName("!~Keasling");
        // HashSet<Integer> results = q.filterSelectionMarker("*");

        ArrayList<String[]> data = new ArrayList<String[]>();
        data.add(new String[] { "name_or_alias", "~kan" });
        LinkedHashSet<Entry> results = q.query(data);

        for (Entry entry : results) {
            System.out.println("" + entry.getId());
        }
        System.out.println("Total: " + results.size());*/
    }
}
