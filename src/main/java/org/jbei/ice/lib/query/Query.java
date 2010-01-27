package org.jbei.ice.lib.query;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeSet;

import org.hibernate.Session;
import org.jbei.ice.lib.managers.HibernateHelper;
import org.jbei.ice.lib.managers.UtilsManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.utils.JbeiConstants;

@SuppressWarnings("unchecked")
public class Query {
    protected static Session session = HibernateHelper.getSession();
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
        Map<String, String> entryTypesFilterMap = new LinkedHashMap<String, String>();
        entryTypesFilterMap.put("plasmid", "Plasmid");
        entryTypesFilterMap.put("strain", "Strain");
        entryTypesFilterMap.put("part", "Part");

        Map<String, String> statusMap = new LinkedHashMap<String, String>();
        statusMap.put("planned", JbeiConstants.getStatus("planned"));
        statusMap.put("complete", JbeiConstants.getStatus("complete"));
        statusMap.put("in progress", JbeiConstants.getStatus("in progress"));

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
        filters.add(new StringFilter("name", "Name", "filterName"));
        filters.add(new StringFilter("part_number", "Part Number", "filterPartNumber"));
        filters.add(new SelectionFilter("type", "Type", "filterType", entryTypesFilterMap));
        filters.add(new SelectionFilter("status", "Status", "filterStatus", statusMap));
        filters.add(new StringFilter("owner", "Owner", "filterOwner"));
        filters.add(new StringFilter("creator", "Creator", "filterCreator"));
        filters.add(new SelectionFilter("selection_marker",
                "Selection Marker (Strains and Plasmids)", "filterSelectionMarker",
                selectionMarkersMap));
        filters.add(new StringFilter("strain_plasmids", "Strain Plasmids", "filterStrainPlasmids"));
        filters.add(new StringFilter("alias", "Alias", "filterAlias"));
        filters.add(new StringFilter("keywords", "Keywords", "filterKeywords"));
        filters.add(new StringFilter("short_description", "Summary", "filterShortDescription"));
        filters.add(new StringFilter("long_description", "Notes", "filterLongDescription"));
        filters.add(new StringFilter("references", "References", "filterReferences"));
        filters.add(new StringFilter("backbone", "Backbone (Plasmids only)", "filterBackbone"));
        filters.add(new SelectionFilter("promoters", "Promoters (Plasmids only)",
                "filterPromoters", promotersMap));
        filters.add(new SelectionFilter("origin_of_replication",
                "Origin Of Replication (Plasmids only)", "filterOriginOfReplication",
                originOfReplicationsMap));
        filters.add(new StringFilter("host", "Host (Strains only)", "filterHost"));
        filters.add(new StringFilter("genotype_phenotype", "Genotype/Phenotype (Strains only)",
                "filterGenotypePhenotype"));
        filters.add(new StringFilter("package_format", "Package Format (Parts only)",
                "filterPackageFormat"));
        filters.add(new RadioFilter("has_attachment", "Has Attachment", "filterHasAttachment",
                yesNoMap));
        filters.add(new RadioFilter("has_sequence", "Has Sequence", "filterHasSequence", yesNoMap));
        filters.add(new RadioFilter("has_sample", "Has Sample", "filterHasSample", yesNoMap));
        filters.add(new StringFilter("record_id", "Record Id", "filterRecordId"));

        // TODO: implement principal investigator filter
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

        Integer[] subset = new Integer[limit];
        System.arraycopy(resultIds.toArray(), offset, subset, offset, limit);
        LinkedHashSet<Entry> result = new LinkedHashSet<Entry>();

        if (resultIds.size() > 0) {
            org.hibernate.Query query = HibernateHelper.getSession().createQuery(
                    "select entry from Entry entry where id in (:ids)");
            query.setParameterList("ids", subset);

            result = new LinkedHashSet<Entry>(query.list());
        }
        return result;
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
            result = field + " like '" + term + "'";
        } else if (operator.equals("!")) {
            result = field + " not like '" + term + "'" + " or " + field + " is null ";
        } else if (operator.equals("^")) {
            result = field + " like '" + term + "%'";
        } else if (operator.equals("$")) {
            result = field + " like '%" + term + "'";
        } else if (operator.equals("*")) {
            result = field + " is null or " + field + " like ''";
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
        } else if (value.startsWith("*")) {
            operator = "*";
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
        org.hibernate.Query query = HibernateHelper.getSession().createQuery(
                "select distinct name.entry.id from Name name where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>(query.list());

        return rawResults;
    }

    protected HashSet<Integer> filterType(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.recordType)", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        org.hibernate.Query query = HibernateHelper.getSession().createQuery(
                "select distinct entry.id from Entry entry where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>(query.list());

        return rawResults;
    }

    protected HashSet<Integer> filterAlias(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.alias)", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        org.hibernate.Query query = HibernateHelper.getSession().createQuery(
                "select distinct entry.id from Entry entry where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>(query.list());

        return rawResults;
    }

    protected HashSet<Integer> filterPartNumber(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(partNumber.partNumber)",
                parsedQuery.get("operator"), parsedQuery.get("value"));
        org.hibernate.Query query = HibernateHelper.getSession().createQuery(
                "select distinct partNumber.entry.id from PartNumber partNumber where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>(query.list());

        return rawResults;
    }

    protected HashSet<Integer> filterHasAttachment(String queryString) {
        HashSet<Integer> result = null;
        HashSet<Integer> allEntriesWithAttachment = null;
        HashSet<Integer> allEntries = null;

        org.hibernate.Query query = HibernateHelper.getSession().createQuery(
                "select distinct attachment.entry.id from Attachment attachment");
        allEntriesWithAttachment = new HashSet<Integer>(query.list());

        if (queryString.equals("yes")) {
            result = allEntriesWithAttachment;
        } else {
            query = HibernateHelper.getSession().createQuery("select entry.id from Entry entry");
            allEntries = new HashSet<Integer>(query.list());

            allEntries.removeAll(allEntriesWithAttachment);
            result = allEntries;
        }

        return result;
    }

    protected HashSet<Integer> filterHasSample(String queryString) {
        HashSet<Integer> result = null;
        HashSet<Integer> allEntriesWithSample = null;
        HashSet<Integer> allEntries = null;

        org.hibernate.Query query = HibernateHelper.getSession().createQuery(
                "select distinct sample.entry.id from Sample sample");
        allEntriesWithSample = new HashSet<Integer>(query.list());

        if (queryString.equals("yes")) {
            result = allEntriesWithSample;
        } else {
            query = HibernateHelper.getSession().createQuery("select entry.id from Entry entry");
            allEntries = new HashSet<Integer>(query.list());

            allEntries.removeAll(allEntriesWithSample);
            result = allEntries;
        }

        return result;
    }

    protected HashSet<Integer> filterHasSequence(String queryString) {
        HashSet<Integer> result = null;
        HashSet<Integer> allEntriesWithSequence = null;
        HashSet<Integer> allEntries = null;

        org.hibernate.Query query = HibernateHelper.getSession().createQuery(
                "select distinct sequence.entry.id from Sequence sequence");
        allEntriesWithSequence = new HashSet<Integer>(query.list());

        if (queryString.equals("yes")) {
            result = allEntriesWithSequence;
        } else {
            query = HibernateHelper.getSession().createQuery("select entry.id from Entry entry");
            allEntries = new HashSet<Integer>(query.list());

            allEntries.removeAll(allEntriesWithSequence);
            result = allEntries;
        }

        return result;
    }

    protected HashSet<Integer> filterRecordId(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.recordId) ", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        org.hibernate.Query query = HibernateHelper.getSession().createQuery(
                "select distinct entry.id from Entry entry where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>(query.list());

        return rawResults;
    }

    protected HashSet<Integer> filterOwner(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.owner) ", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        org.hibernate.Query query = HibernateHelper.getSession().createQuery(
                "select distinct entry.id from Entry entry where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>(query.list());

        return rawResults;
    }

    protected HashSet<Integer> filterOwnerEmail(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.ownerEmail) ", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        org.hibernate.Query query = HibernateHelper.getSession().createQuery(
                "select distinct entry.id from Entry entry where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>(query.list());

        return rawResults;
    }

    protected HashSet<Integer> filterCreator(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.creator) ", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        org.hibernate.Query query = HibernateHelper.getSession().createQuery(
                "select distinct entry.id from Entry entry where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>(query.list());

        return rawResults;
    }

    protected HashSet<Integer> filterKeywords(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.keywords)", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        org.hibernate.Query query = HibernateHelper.getSession().createQuery(
                "select distinct entry.id from Entry entry where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>(query.list());

        return rawResults;
    }

    protected HashSet<Integer> filterShortDescription(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.shortDescription)", parsedQuery
                .get("operator"), parsedQuery.get("value"));
        org.hibernate.Query query = HibernateHelper.getSession().createQuery(
                "select distinct entry.id from Entry entry where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>(query.list());

        return rawResults;
    }

    protected HashSet<Integer> filterLongDescription(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.longDescription)",
                parsedQuery.get("operator"), parsedQuery.get("value"));
        org.hibernate.Query query = HibernateHelper.getSession().createQuery(
                "select distinct entry.id from Entry entry where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>(query.list());

        return rawResults;
    }

    protected HashSet<Integer> filterReferences(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.references)", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        org.hibernate.Query query = HibernateHelper.getSession().createQuery(
                "select distinct entry.id from Entry entry where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>(query.list());

        return rawResults;
    }

    protected HashSet<Integer> filterBackbone(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(plasmid.backbone)", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        org.hibernate.Query query = HibernateHelper.getSession().createQuery(
                "select distinct plasmid.id from Plasmid plasmid where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>(query.list());

        return rawResults;
    }

    protected HashSet<Integer> filterPromoters(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(plasmid.promoters)", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        org.hibernate.Query query = HibernateHelper.getSession().createQuery(
                "select distinct plasmid.id from Plasmid plasmid where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>(query.list());

        return rawResults;
    }

    protected HashSet<Integer> filterOriginOfReplication(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(plasmid.originOfReplication)", parsedQuery
                .get("operator"), parsedQuery.get("value"));
        org.hibernate.Query query = HibernateHelper.getSession().createQuery(
                "select distinct plasmid.id from Plasmid plasmid where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>(query.list());

        return rawResults;
    }

    protected HashSet<Integer> filterHost(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(strain.host)", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        org.hibernate.Query query = HibernateHelper.getSession().createQuery(
                "select distinct strain.id from Strain strain where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>(query.list());

        return rawResults;
    }

    protected HashSet<Integer> filterGenotypePhenotype(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(strain.genotypePhenotype)", parsedQuery
                .get("operator"), parsedQuery.get("value"));
        org.hibernate.Query query = HibernateHelper.getSession().createQuery(
                "select distinct strain.id from Strain strain where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>(query.list());

        return rawResults;
    }

    protected HashSet<Integer> filterPackageFormat(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(part.packageFormat)", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        org.hibernate.Query query = HibernateHelper.getSession().createQuery(
                "select distinct part.id from Part part where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>(query.list());

        return rawResults;
    }

    protected HashSet<Integer> filterSelectionMarker(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(marker.name)", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        org.hibernate.Query query = HibernateHelper.getSession().createQuery(
                "select distinct marker.entry.id from SelectionMarker marker where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>(query.list());

        return rawResults;
    }

    protected HashSet<Integer> filterStatus(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(entry.status)", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        org.hibernate.Query query = HibernateHelper.getSession().createQuery(
                "select distinct entry.id from Entry entry where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>(query.list());

        return rawResults;
    }

    protected HashSet<Integer> filterStrainPlasmid(String queryString) {
        HashMap<String, String> parsedQuery = parseQuery(queryString);
        String criteria = makeCriterion("lower(strain.plasmids)", parsedQuery.get("operator"),
                parsedQuery.get("value"));
        org.hibernate.Query query = HibernateHelper.getSession().createQuery(
                "select distinct strain.id from Strain strain where " + criteria);
        HashSet<Integer> rawResults = new HashSet<Integer>(query.list());

        return rawResults;
    }

    protected HashSet<Integer> filterNameOrAlias(String queryString) {
        HashSet<Integer> nameResults = filterName(queryString);
        HashSet<Integer> aliasResults = filterAlias(queryString);

        nameResults.addAll(aliasResults);

        return nameResults;
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
