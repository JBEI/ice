package org.jbei.ice.lib.query;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeSet;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.HibernateHelper;
import org.jbei.ice.lib.managers.UtilsManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.EntryFundingSource;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.utils.Utils;

@SuppressWarnings("unchecked")
public class Query {

    private ArrayList<Filter> filters = new ArrayList<Filter>();

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
        TreeSet<String> uniqueSelectionMarkers = UtilsManager.getUniqueSelectionMarkers();
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

    private HashSet<Integer> runFilter(String key, String value) {
        HashSet<Integer> result = null;

        try {
            Filter queryType = filterByKey(key);

            result = (HashSet<Integer>) this.getClass().getDeclaredMethod(queryType.method,
                    String.class).invoke(this, value);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return result;
    }

    public LinkedHashSet<Entry> query(ArrayList<String[]> data, int offset, int limit) {
        return query(data, offset, limit, new SortField[] { new SortField("id", true) });
    }

    public LinkedHashSet<Entry> query(ArrayList<String[]> data, int offset, int limit,
            SortField[] sortFields) {
        TreeSet<Integer> resultIds = new TreeSet<Integer>();

        boolean firstRun = true;

        for (String[] item : data) {
            try {
                if (firstRun) {
                    HashSet queryResultSet = runFilter(item[0], item[1]);

                    for (Iterator<Integer> iterator = queryResultSet.iterator(); iterator.hasNext();) {
                        resultIds.add(iterator.next());
                    }

                    firstRun = false;
                } else {
                    resultIds.retainAll(runFilter(item[0], item[1]));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (limit > resultIds.size() || limit == -1) {
            limit = resultIds.size();
        }

        LinkedHashSet<Entry> result = new LinkedHashSet<Entry>();

        String sortQuerySuffix = "";

        if (sortFields != null && sortFields.length > 0) {
            sortQuerySuffix = Utils.join(", ", Arrays.asList(sortFields));
        }

        if (resultIds.size() > 0) {
            Session session = HibernateHelper.getSession();
            org.hibernate.Query query = session
                    .createQuery("SELECT entry FROM Entry entry WHERE id in (:ids) "
                            + (!sortQuerySuffix.isEmpty() ? ("ORDER BY " + sortQuerySuffix) : ""));

            query.setParameterList("ids", resultIds);
            query.setFirstResult(offset);
            query.setMaxResults(limit);
            try {
                result = new LinkedHashSet<Entry>(query.list());
            } catch (HibernateException e) {
                Logger.error("Could not query " + e.toString());
            } finally {

            }
        }

        return result;
    }

    public int queryCount(ArrayList<String[]> data) {
        TreeSet<Integer> resultIds = new TreeSet<Integer>();

        boolean firstRun = true;

        for (String[] item : data) {
            try {
                if (firstRun) {
                    HashSet queryResultSet = runFilter(item[0], item[1]);

                    for (Iterator<Integer> iterator = queryResultSet.iterator(); iterator.hasNext();) {
                        resultIds.add(iterator.next());
                    }

                    firstRun = false;
                } else {
                    resultIds.retainAll(runFilter(item[0], item[1]));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return resultIds.size();
    }

    public LinkedHashSet<Entry> query(ArrayList<String[]> data) {
        return query(data, 0, -1);
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

    protected HashSet<Integer> filterName(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);

        String criteria = makeCriterion("lower(name.name)", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        Session session = HibernateHelper.getSession();
        org.hibernate.Query query = session
                .createQuery("select distinct name.entry.id from Name name where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>();
        try {
            rawResults = new HashSet<Integer>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query " + e.toString());
        } finally {

        }
        return rawResults;
    }

    protected HashSet<Integer> filterStrainPlasmids(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);

        String criteria = makeCriterion("lower(strain.plasmids)", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        Session session = HibernateHelper.getSession();
        org.hibernate.Query query = session
                .createQuery("select distinct strain.id from Strain strain where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>();
        try {
            rawResults = new HashSet<Integer>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query " + e.toString());
        } finally {

        }

        return rawResults;
    }

    protected HashSet<Integer> filterType(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.recordType)", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        Session session = HibernateHelper.getSession();
        org.hibernate.Query query = session
                .createQuery("select distinct entry.id from Entry entry where " + criteria);

        HashSet<Integer> rawResults = new HashSet<Integer>();
        try {
            rawResults = new HashSet<Integer>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query " + e.toString());
        } finally {

        }
        return rawResults;
    }

    protected HashSet<Integer> filterAlias(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.alias)", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        Session session = HibernateHelper.getSession();
        org.hibernate.Query query = session
                .createQuery("select distinct entry.id from Entry entry where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>();
        try {
            rawResults = new HashSet<Integer>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query " + e.toString());
        } finally {

        }
        return rawResults;
    }

    protected HashSet<Integer> filterPartNumber(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(partNumber.partNumber)",
                parsedQuery.get("operator"), parsedQuery.get("value"));
        Session session = HibernateHelper.getSession();
        org.hibernate.Query query = session
                .createQuery("select distinct partNumber.entry.id from PartNumber partNumber where "
                        + criteria);

        HashSet<Integer> rawResults = new HashSet<Integer>();
        try {
            rawResults = new HashSet<Integer>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query " + e.toString());
        } finally {

        }

        return rawResults;
    }

    protected HashSet<Integer> filterHasAttachment(String queryString) {
        HashSet<Integer> result = null;
        HashSet<Integer> allEntriesWithAttachment = null;
        HashSet<Integer> allEntries = null;
        Session session = HibernateHelper.getSession();
        org.hibernate.Query query = session
                .createQuery("select distinct attachment.entry.id from Attachment attachment");
        allEntriesWithAttachment = new HashSet<Integer>();

        try {
            allEntriesWithAttachment = new HashSet<Integer>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query " + e.toString());
        } finally {

        }

        if (queryString.equals("yes")) {
            result = allEntriesWithAttachment;
        } else {
            session = HibernateHelper.getSession();
            query = session.createQuery("select entry.id from Entry entry");
            allEntries = new HashSet<Integer>();
            try {
                allEntries = new HashSet<Integer>(query.list());
            } catch (HibernateException e) {
                Logger.error("Could not query " + e.toString());
            } finally {

            }

            allEntries.removeAll(allEntriesWithAttachment);
            result = allEntries;
        }

        return result;
    }

    protected HashSet<Integer> filterHasSample(String queryString) {
        HashSet<Integer> result = null;
        HashSet<Integer> allEntriesWithSample = null;
        HashSet<Integer> allEntries = null;
        Session session = HibernateHelper.getSession();
        org.hibernate.Query query = session
                .createQuery("select distinct sample.entry.id from Sample sample");
        allEntriesWithSample = new HashSet<Integer>();
        try {
            allEntriesWithSample = new HashSet<Integer>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query " + e.toString());
        } finally {

        }

        if (queryString.equals("yes")) {
            result = allEntriesWithSample;
        } else {
            session = HibernateHelper.getSession();
            query = session.createQuery("select entry.id from Entry entry");
            allEntries = new HashSet<Integer>();
            try {
                allEntries = new HashSet<Integer>(query.list());
            } catch (HibernateException e) {
                Logger.error("Could not query " + e.toString());
            } finally {

            }

            allEntries.removeAll(allEntriesWithSample);
            result = allEntries;
        }

        return result;
    }

    protected HashSet<Integer> filterHasSequence(String queryString) {
        HashSet<Integer> result = null;
        HashSet<Integer> allEntriesWithSequence = null;
        HashSet<Integer> allEntries = null;
        Session session = HibernateHelper.getSession();
        org.hibernate.Query query = session
                .createQuery("select distinct sequence.entry.id from Sequence sequence");
        allEntriesWithSequence = new HashSet<Integer>();
        try {
            allEntriesWithSequence = new HashSet<Integer>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query " + e.toString());
        } finally {

        }

        if (queryString.equals("yes")) {
            result = allEntriesWithSequence;
        } else {
            session = HibernateHelper.getSession();
            query = session.createQuery("select entry.id from Entry entry");
            allEntries = new HashSet<Integer>();
            try {
                allEntries = new HashSet<Integer>(query.list());
            } catch (HibernateException e) {
                Logger.error("Could not query " + e.toString());
            } finally {

            }
            allEntries.removeAll(allEntriesWithSequence);
            result = allEntries;
        }

        return result;
    }

    protected HashSet<Integer> filterRecordId(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.recordId) ", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        Session session = HibernateHelper.getSession();
        org.hibernate.Query query = session
                .createQuery("select distinct entry.id from Entry entry where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>();
        try {
            rawResults = new HashSet<Integer>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query " + e.toString());
        } finally {

        }

        return rawResults;
    }

    protected HashSet<Integer> filterOwnerCombined(String queryString) {
        HashSet<Integer> ownerResults = filterOwner(queryString);
        HashSet<Integer> ownerEmailResults = filterOwnerEmail(queryString);

        ownerResults.addAll(ownerEmailResults);

        return ownerResults;
    }

    protected HashSet<Integer> filterOwner(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.owner) ", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        Session session = HibernateHelper.getSession();
        org.hibernate.Query query = session
                .createQuery("select distinct entry.id from Entry entry where " + criteria);

        HashSet<Integer> rawResults = new HashSet<Integer>();
        try {
            rawResults = new HashSet<Integer>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query " + e.toString());
        } finally {

        }
        return rawResults;
    }

    protected HashSet<Integer> filterOwnerEmail(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.ownerEmail) ", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        Session session = HibernateHelper.getSession();
        org.hibernate.Query query = session
                .createQuery("select distinct entry.id from Entry entry where " + criteria);

        HashSet<Integer> rawResults = new HashSet<Integer>();
        try {
            rawResults = new HashSet<Integer>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query " + e.toString());
        } finally {

        }
        return rawResults;
    }

    protected HashSet<Integer> filterCreatorCombined(String queryString) {
        HashSet<Integer> creatorResults = filterCreator(queryString);
        HashSet<Integer> creatorEmailResults = filterCreatorEmail(queryString);

        creatorResults.addAll(creatorEmailResults);

        return creatorResults;
    }

    protected HashSet<Integer> filterCreator(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.creator) ", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        Session session = HibernateHelper.getSession();
        org.hibernate.Query query = session
                .createQuery("select distinct entry.id from Entry entry where " + criteria);

        HashSet<Integer> rawResults = new HashSet<Integer>();
        try {
            rawResults = new HashSet<Integer>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query " + e.toString());
        } finally {

        }
        return rawResults;
    }

    protected HashSet<Integer> filterCreatorEmail(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);

        String criteria = makeCriterion("lower(entry.creatorEmail) ", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        Session session = HibernateHelper.getSession();
        org.hibernate.Query query = session
                .createQuery("select distinct entry.id from Entry entry where " + criteria);

        HashSet<Integer> rawResults = new HashSet<Integer>();
        try {
            rawResults = new HashSet<Integer>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query " + e.toString());
        } finally {

        }
        return rawResults;
    }

    protected HashSet<Integer> filterKeywords(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.keywords)", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        Session session = HibernateHelper.getSession();
        org.hibernate.Query query = session
                .createQuery("select distinct entry.id from Entry entry where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>();
        try {
            rawResults = new HashSet<Integer>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query " + e.toString());
        } finally {

        }

        return rawResults;
    }

    protected HashSet<Integer> filterSummaryNotesReferences(String queryString) {
        HashSet<Integer> summaryResults = filterShortDescription(queryString);
        HashSet<Integer> notesResults = filterLongDescription(queryString);
        HashSet<Integer> referencesResults = filterReferences(queryString);

        summaryResults.addAll(notesResults);
        summaryResults.addAll(referencesResults);

        return summaryResults;
    }

    protected HashSet<Integer> filterShortDescription(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.shortDescription)", parsedQuery
                .get("operator"), parsedQuery.get("value"));
        Session session = HibernateHelper.getSession();
        org.hibernate.Query query = session
                .createQuery("select distinct entry.id from Entry entry where " + criteria);

        HashSet<Integer> rawResults = new HashSet<Integer>();
        try {
            rawResults = new HashSet<Integer>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query " + e.toString());
        } finally {

        }

        return rawResults;
    }

    protected HashSet<Integer> filterLongDescription(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.longDescription)",
                parsedQuery.get("operator"), parsedQuery.get("value"));
        Session session = HibernateHelper.getSession();
        org.hibernate.Query query = session
                .createQuery("select distinct entry.id from Entry entry where " + criteria);

        HashSet<Integer> rawResults = new HashSet<Integer>();
        try {
            rawResults = new HashSet<Integer>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query " + e.toString());
        } finally {

        }
        return rawResults;
    }

    protected HashSet<Integer> filterReferences(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.references)", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        Session session = HibernateHelper.getSession();
        org.hibernate.Query query = session
                .createQuery("select distinct entry.id from Entry entry where " + criteria);

        HashSet<Integer> rawResults = new HashSet<Integer>();
        try {
            rawResults = new HashSet<Integer>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query " + e.toString());
        } finally {

        }
        return rawResults;
    }

    protected HashSet<Integer> filterBackbone(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(plasmid.backbone)", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        Session session = HibernateHelper.getSession();
        org.hibernate.Query query = session
                .createQuery("select distinct plasmid.id from Plasmid plasmid where " + criteria);

        HashSet<Integer> rawResults = new HashSet<Integer>();
        try {
            rawResults = new HashSet<Integer>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query " + e.toString());
        } finally {

        }

        return rawResults;
    }

    protected HashSet<Integer> filterPromoters(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(plasmid.promoters)", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        Session session = HibernateHelper.getSession();
        org.hibernate.Query query = session
                .createQuery("select distinct plasmid.id from Plasmid plasmid where " + criteria);

        HashSet<Integer> rawResults = new HashSet<Integer>();
        try {
            rawResults = new HashSet<Integer>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query " + e.toString());
        } finally {

        }
        return rawResults;
    }

    protected HashSet<Integer> filterOriginOfReplication(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(plasmid.originOfReplication)", parsedQuery
                .get("operator"), parsedQuery.get("value"));
        Session session = HibernateHelper.getSession();
        org.hibernate.Query query = session
                .createQuery("select distinct plasmid.id from Plasmid plasmid where " + criteria);

        HashSet<Integer> rawResults = new HashSet<Integer>();
        try {
            rawResults = new HashSet<Integer>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query " + e.toString());
        } finally {

        }
        return rawResults;
    }

    protected HashSet<Integer> filterHost(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(strain.host)", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        Session session = HibernateHelper.getSession();
        org.hibernate.Query query = session
                .createQuery("select distinct strain.id from Strain strain where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>();
        try {
            rawResults = new HashSet<Integer>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query " + e.toString());
        } finally {

        }
        return rawResults;
    }

    protected HashSet<Integer> filterGenotypePhenotype(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(strain.genotypePhenotype)", parsedQuery
                .get("operator"), parsedQuery.get("value"));
        Session session = HibernateHelper.getSession();
        org.hibernate.Query query = session
                .createQuery("select distinct strain.id from Strain strain where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>();
        try {
            rawResults = new HashSet<Integer>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query " + e.toString());
        } finally {

        }

        return rawResults;
    }

    protected HashSet<Integer> filterPackageFormat(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(part.packageFormat)", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        Session session = HibernateHelper.getSession();
        org.hibernate.Query query = session
                .createQuery("select distinct part.id from Part part where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>();
        try {
            rawResults = new HashSet<Integer>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query " + e.toString());
        } finally {

        }

        return rawResults;
    }

    protected HashSet<Integer> filterSelectionMarker(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(marker.name)", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        Session session = HibernateHelper.getSession();
        org.hibernate.Query query = session
                .createQuery("select distinct marker.entry.id from SelectionMarker marker where "
                        + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>();
        try {
            rawResults = new HashSet<Integer>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query " + e.toString());
        } finally {

        }

        return rawResults;
    }

    protected HashSet<Integer> filterStatus(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.status)", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        Session session = HibernateHelper.getSession();
        org.hibernate.Query query = session
                .createQuery("select distinct entry.id from Entry entry where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>();
        try {
            rawResults = new HashSet<Integer>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query " + e.toString());
        } finally {

        }

        return rawResults;
    }

    protected HashSet<Integer> filterStrainPlasmid(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(strain.plasmids)", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        Session session = HibernateHelper.getSession();
        org.hibernate.Query query = session
                .createQuery("select distinct strain.id from Strain strain where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>();
        try {
            rawResults = new HashSet<Integer>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query " + e.toString());
        } finally {

        }

        return rawResults;
    }

    protected HashSet<Integer> filterNameOrAlias(String queryString) {
        HashSet<Integer> nameResults = filterName(queryString);
        HashSet<Integer> aliasResults = filterAlias(queryString);

        nameResults.addAll(aliasResults);

        return nameResults;
    }

    protected HashSet<Integer> filterPrincipalInvestigator(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);

        String criteria = makeCriterion(
                "lower(entryFundingSource.fundingSource.principalInvestigator)", parsedQuery
                        .get("operator"), parsedQuery.get("value"));
        Session session = HibernateHelper.getSession();
        org.hibernate.Query query = session.createQuery("select distinct entry.id from "
                + EntryFundingSource.class.getName() + " entryFundingSource where " + criteria);

        HashSet<Integer> rawResults = new HashSet<Integer>();
        try {
            rawResults = new HashSet<Integer>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query " + e.toString());
        } finally {

        }
        return rawResults;
    }

    protected HashSet<Integer> filterFundingSource(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);

        String criteria = makeCriterion("lower(entryFundingSource.fundingSource.fundingSource)",
                parsedQuery.get("operator"), parsedQuery.get("value"));
        Session session = HibernateHelper.getSession();
        org.hibernate.Query query = session.createQuery("select distinct entry.id from "
                + EntryFundingSource.class.getName() + " entryFundingSource where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>();
        try {
            rawResults = new HashSet<Integer>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query " + e.toString());
        } finally {

        }
        return rawResults;
    }

    protected HashSet<Integer> filterIntelectualProperty(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);

        String criteria = makeCriterion("lower(entry.intellectualProperty)", parsedQuery
                .get("operator"), parsedQuery.get("value"));
        Session session = HibernateHelper.getSession();
        org.hibernate.Query query = session.createQuery("select distinct entry.id from "
                + Entry.class.getName() + " entry where " + criteria);

        HashSet<Integer> rawResults = new HashSet<Integer>();
        try {
            rawResults = new HashSet<Integer>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query " + e.toString());
        } finally {

        }
        return rawResults;

    }

    protected HashSet<Integer> filterBioSafetyLevel(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);

        String operator = parsedQuery.get("operator");

        String criteria = null;

        if (operator.equals("=")) {
            criteria = "entry.bioSafetyLevel = " + parsedQuery.get("value");
        } else if (operator.equals("!")) {
            criteria = "entry.bioSafetyLevel != " + parsedQuery.get("value");
        }
        Session session = HibernateHelper.getSession();
        org.hibernate.Query query = session.createQuery("select distinct entry.id from "
                + Entry.class.getName() + " entry where " + criteria);

        HashSet<Integer> rawResults = new HashSet<Integer>();
        try {
            rawResults = new HashSet<Integer>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not query " + e.toString());
        } finally {

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
