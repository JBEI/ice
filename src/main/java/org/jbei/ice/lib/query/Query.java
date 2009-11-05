package org.jbei.ice.lib.query;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.TreeSet;

import org.hibernate.Session;
import org.jbei.ice.lib.managers.HibernateHelper;
import org.jbei.ice.lib.models.Entry;

public class Query {
	protected static Session session = HibernateHelper.getSession();
	protected HashMap<String, Method> methodMap = new HashMap<String, Method> ();
	
	public Query() {
		try {
			methodMap.put("name", this.getClass().getDeclaredMethod("filterName", String.class));
			methodMap.put("part_number", this.getClass().getDeclaredMethod("filterPartNumber", String.class));
			methodMap.put("type", this.getClass().getDeclaredMethod("filterType", String.class));
			methodMap.put("has_attachment", this.getClass().getDeclaredMethod("filterHasAttachment", String.class));
			methodMap.put("has_sequence", this.getClass().getDeclaredMethod("filterHasSequence", String.class));
			methodMap.put("has_sample", this.getClass().getDeclaredMethod("filterHasSample", String.class));
			methodMap.put("record_id", this.getClass().getDeclaredMethod("filterRecordId", String.class));	 
			methodMap.put("owner", this.getClass().getDeclaredMethod("filterOwner", String.class));
			methodMap.put("owner_email", this.getClass().getDeclaredMethod("filterOwnerEmail", String.class));
			methodMap.put("creator", this.getClass().getDeclaredMethod("filterCreator", String.class));
			methodMap.put("keywords", this.getClass().getDeclaredMethod("filterKeywords", String.class));
			methodMap.put("short_description", this.getClass().getDeclaredMethod("filterShortDescription", String.class));	
			methodMap.put("long_description", this.getClass().getDeclaredMethod("filterLongDescription", String.class));
			methodMap.put("references", this.getClass().getDeclaredMethod("filterReferences", String.class));
			methodMap.put("backbone", this.getClass().getDeclaredMethod("filterBackbone", String.class));
			methodMap.put("promoters", this.getClass().getDeclaredMethod("filterPromoters", String.class));
			methodMap.put("origin_of_replication", this.getClass().getDeclaredMethod("filterOriginOfReplication", String.class));
			methodMap.put("host", this.getClass().getDeclaredMethod("filterHost", String.class));
			methodMap.put("genotype_phenotype", this.getClass().getDeclaredMethod("filterGenotypePhenotype", String.class));
			methodMap.put("package_format", this.getClass().getDeclaredMethod("filterPackageFormat", String.class));
			methodMap.put("selection_marker", this.getClass().getDeclaredMethod("filterSelectionMarker", String.class));
			methodMap.put("status", this.getClass().getDeclaredMethod("filterStatus", String.class));
			methodMap.put("strain_plasmids", this.getClass().getDeclaredMethod("filterStrainPlasmid", String.class));
			methodMap.put("alias", this.getClass().getDeclaredMethod("filterAlias", String.class));
			methodMap.put("name_or_alias", this.getClass().getDeclaredMethod("filterNameOrAlias", String.class));
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public LinkedHashSet<Entry> query(ArrayList<String[]> data, int offset, int limit) {
		
		TreeSet<Integer> resultIds = new TreeSet<Integer> ();
		
		boolean firstRun = true;
		
		for (String[] item: data) {
			try {
				if (firstRun) {
					resultIds.addAll((HashSet<Integer>) methodMap.get(item[0]).invoke(this, item[1]));
					firstRun = false;
				} else {
					resultIds.retainAll((HashSet<Integer>) methodMap.get(item[0]).invoke(this, item[1]));
				}
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (limit > resultIds.size()) {
			limit = resultIds.size();
		}
		
		Integer[] subset = new Integer[limit];
		System.arraycopy(resultIds.toArray(), offset, subset, offset, limit);
		LinkedHashSet<Entry> result = new LinkedHashSet<Entry>();
		if (resultIds.size() > 0) {
			org.hibernate.Query query = HibernateHelper.getSession().createQuery(
				"select entry from Entry entry where id in (:ids)" 
				);
			query.setParameterList("ids", subset);
			result = new LinkedHashSet<Entry>(query.list());
		}
		return result;
		
	}

	public LinkedHashSet<Entry> query(ArrayList<String[]> data) {
		return query(data, 0, 15);

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
		HashMap<String, String> result = new HashMap<String, String> ();
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
			result.put("operator",operator);
			result.put("value", newValue);
			
			return result;
		
	}
	
	protected HashSet<Integer> filterName(String queryString) {
		HashMap<String, String> parsedQuery = parseQuery(queryString);
		String criteria = makeCriterion("lower(name.name)", parsedQuery.get("operator"), parsedQuery.get("value"));
		org.hibernate.Query query = HibernateHelper.getSession().createQuery(
			"select distinct name.entry.id from Name name where " + criteria
			);
		HashSet<Integer> rawResults = new HashSet<Integer> (query.list());
		
		return rawResults;
	}
	
	protected HashSet<Integer> filterType(String queryString) {
		HashMap<String, String> parsedQuery = parseQuery(queryString);
		String criteria = makeCriterion("lower(entry.recordType)", parsedQuery.get("operator"), parsedQuery.get("value"));
		org.hibernate.Query query = HibernateHelper.getSession().createQuery(
			"select distinct entry.id from Entry entry where " + criteria
			);
		HashSet<Integer> rawResults = new HashSet<Integer> (query.list());
		
		return rawResults;
	}
	
	protected HashSet<Integer> filterAlias(String queryString) {
		HashMap<String, String> parsedQuery = parseQuery(queryString);
		String criteria = makeCriterion("lower(entry.alias)", parsedQuery.get("operator"), parsedQuery.get("value"));
		org.hibernate.Query query = HibernateHelper.getSession().createQuery(
			"select distinct entry.id from Entry entry where " + criteria
			);
		HashSet<Integer> rawResults = new HashSet<Integer> (query.list());
		
		return rawResults;
	}
	
	protected HashSet<Integer> filterPartNumber(String queryString) {
		HashMap<String, String> parsedQuery = parseQuery(queryString);
		String criteria = makeCriterion("lower(partNumber.partNumber)", parsedQuery.get("operator"), parsedQuery.get("value"));
		org.hibernate.Query query = HibernateHelper.getSession().createQuery(
			"select distinct partNumber.entry.id from PartNumber partNumber where " + criteria
			);
		HashSet<Integer> rawResults = new HashSet<Integer> (query.list());
		
		return rawResults;
	}
	
	protected HashSet<Integer> filterHasAttachment(String queryString) {
		HashSet<Integer> result = null;
		HashSet<Integer> allEntriesWithAttachment = null;
		HashSet<Integer> allEntries = null;
		
		org.hibernate.Query query = HibernateHelper.getSession().createQuery(
			"select distinct attachment.entry.id from Attachment attachment"
			);
		allEntriesWithAttachment = new HashSet<Integer> (query.list());
		
		if (queryString.equals("yes")) {
			result = allEntriesWithAttachment;
		} else {
			query = HibernateHelper.getSession().createQuery(
				"select entry.id from Entry entry"
				);
			allEntries = new HashSet<Integer> (query.list());
			
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
			"select distinct sample.entry.id from Sample sample"
			);
		allEntriesWithSample = new HashSet<Integer> (query.list());
		
		if (queryString.equals("yes")) {
			result = allEntriesWithSample;
		} else {
			query = HibernateHelper.getSession().createQuery(
				"select entry.id from Entry entry"
				);
			allEntries = new HashSet<Integer> (query.list());
			
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
			"select distinct sequence.entry.id from Sequence sequence"
			);
		allEntriesWithSequence = new HashSet<Integer> (query.list());
		
		if (queryString.equals("yes")) {
			result = allEntriesWithSequence;
		} else {
			query = HibernateHelper.getSession().createQuery(
				"select entry.id from Entry entry"
				);
			allEntries = new HashSet<Integer> (query.list());
			
			allEntries.removeAll(allEntriesWithSequence);
			result = allEntries;
		}
		
		return result;
	}
	
	protected HashSet<Integer> filterRecordId(String queryString) {
		HashMap<String, String> parsedQuery = parseQuery(queryString);
		String criteria = makeCriterion("lower(entry.recordId) ", parsedQuery.get("operator"), parsedQuery.get("value"));
		org.hibernate.Query query = HibernateHelper.getSession().createQuery(
			"select distinct entry.id from Entry entry where " + criteria
			);
		HashSet<Integer> rawResults = new HashSet<Integer> (query.list());
		
		return rawResults;
	}	
	
	protected HashSet<Integer> filterOwner(String queryString) {
		HashMap<String, String> parsedQuery = parseQuery(queryString);
		String criteria = makeCriterion("lower(entry.owner) ", parsedQuery.get("operator"), parsedQuery.get("value"));
		org.hibernate.Query query = HibernateHelper.getSession().createQuery(
			"select distinct entry.id from Entry entry where " + criteria
			);
		HashSet<Integer> rawResults = new HashSet<Integer> (query.list());
		
		return rawResults;
	}		
	
	protected HashSet<Integer> filterOwnerEmail(String queryString) {
		HashMap<String, String> parsedQuery = parseQuery(queryString);
		String criteria = makeCriterion("lower(entry.ownerEmail) ", parsedQuery.get("operator"), parsedQuery.get("value"));
		org.hibernate.Query query = HibernateHelper.getSession().createQuery(
			"select distinct entry.id from Entry entry where " + criteria
			);
		HashSet<Integer> rawResults = new HashSet<Integer> (query.list());
		
		return rawResults;
	}
	
	protected HashSet<Integer> filterCreator(String queryString) {
		HashMap<String, String> parsedQuery = parseQuery(queryString);
		String criteria = makeCriterion("lower(entry.creator) ", parsedQuery.get("operator"), parsedQuery.get("value"));
		org.hibernate.Query query = HibernateHelper.getSession().createQuery(
			"select distinct entry.id from Entry entry where " + criteria
			);
		HashSet<Integer> rawResults = new HashSet<Integer> (query.list());
		
		return rawResults;
	}		
	
	protected HashSet<Integer> filterKeywords(String queryString) {
		HashMap<String, String> parsedQuery = parseQuery(queryString);
		String criteria = makeCriterion("lower(entry.keywords)", parsedQuery.get("operator"), parsedQuery.get("value"));
		org.hibernate.Query query = HibernateHelper.getSession().createQuery(
			"select distinct entry.id from Entry entry where " + criteria
			);
		HashSet<Integer> rawResults = new HashSet<Integer> (query.list());
		
		return rawResults;
	}		
	
	protected HashSet<Integer> filterShortDescription(String queryString) {
		HashMap<String, String> parsedQuery = parseQuery(queryString);
		String criteria = makeCriterion("lower(entry.shortDescription)", parsedQuery.get("operator"), parsedQuery.get("value"));
		org.hibernate.Query query = HibernateHelper.getSession().createQuery(
			"select distinct entry.id from Entry entry where " + criteria
			);
		HashSet<Integer> rawResults = new HashSet<Integer> (query.list());
		
		return rawResults;
	}		
	
	protected HashSet<Integer> filterLongDescription(String queryString) {
		HashMap<String, String> parsedQuery = parseQuery(queryString);
		String criteria = makeCriterion("lower(entry.longDescription)", parsedQuery.get("operator"), parsedQuery.get("value"));
		org.hibernate.Query query = HibernateHelper.getSession().createQuery(
			"select distinct entry.id from Entry entry where " + criteria
			);
		HashSet<Integer> rawResults = new HashSet<Integer> (query.list());
		
		return rawResults;
	}		
	
	protected HashSet<Integer> filterReferences(String queryString) {
		HashMap<String, String> parsedQuery = parseQuery(queryString);
		String criteria = makeCriterion("lower(entry.references)", parsedQuery.get("operator"), parsedQuery.get("value"));
		org.hibernate.Query query = HibernateHelper.getSession().createQuery(
			"select distinct entry.id from Entry entry where " + criteria
			);
		HashSet<Integer> rawResults = new HashSet<Integer> (query.list());
		
		return rawResults;
	}			
	
	protected HashSet<Integer> filterBackbone(String queryString) {
		HashMap<String, String> parsedQuery = parseQuery(queryString);
		String criteria = makeCriterion("lower(plasmid.backbone)", parsedQuery.get("operator"), parsedQuery.get("value"));
		org.hibernate.Query query = HibernateHelper.getSession().createQuery(
			"select distinct plasmid.id from Plasmid plasmid where " + criteria
			);
		HashSet<Integer> rawResults = new HashSet<Integer> (query.list());
		
		return rawResults;
	}			
	
	protected HashSet<Integer> filterPromoters(String queryString) {
		HashMap<String, String> parsedQuery = parseQuery(queryString);
		String criteria = makeCriterion("lower(plasmid.promoters)", parsedQuery.get("operator"), parsedQuery.get("value"));
		org.hibernate.Query query = HibernateHelper.getSession().createQuery(
			"select distinct plasmid.id from Plasmid plasmid where " + criteria
			);
		HashSet<Integer> rawResults = new HashSet<Integer> (query.list());
		
		return rawResults;
	}	
	
	protected HashSet<Integer> filterOriginOfReplication(String queryString) {
		HashMap<String, String> parsedQuery = parseQuery(queryString);
		String criteria = makeCriterion("lower(plasmid.originOfReplication)", parsedQuery.get("operator"), parsedQuery.get("value"));
		org.hibernate.Query query = HibernateHelper.getSession().createQuery(
			"select distinct plasmid.id from Plasmid plasmid where " + criteria
			);
		HashSet<Integer> rawResults = new HashSet<Integer> (query.list());
		
		return rawResults;
	}		
	
	protected HashSet<Integer> filterHost(String queryString) {
		HashMap<String, String> parsedQuery = parseQuery(queryString);
		String criteria = makeCriterion("lower(strain.host)", parsedQuery.get("operator"), parsedQuery.get("value"));
		org.hibernate.Query query = HibernateHelper.getSession().createQuery(
			"select distinct strain.id from Strain strain where " + criteria
			);
		HashSet<Integer> rawResults = new HashSet<Integer> (query.list());
		
		return rawResults;
	}
	
	protected HashSet<Integer> filterGenotypePhenotype(String queryString) {
		HashMap<String, String> parsedQuery = parseQuery(queryString);
		String criteria = makeCriterion("lower(strain.genotypePhenotype)", parsedQuery.get("operator"), parsedQuery.get("value"));
		org.hibernate.Query query = HibernateHelper.getSession().createQuery(
			"select distinct strain.id from Strain strain where " + criteria
			);
		HashSet<Integer> rawResults = new HashSet<Integer> (query.list());
		
		return rawResults;
	}
	
	protected HashSet<Integer> filterPackageFormat(String queryString) {
		HashMap<String, String> parsedQuery = parseQuery(queryString);
		String criteria = makeCriterion("lower(part.packageFormat)", parsedQuery.get("operator"), parsedQuery.get("value"));
		org.hibernate.Query query = HibernateHelper.getSession().createQuery(
			"select distinct part.id from Part part where " + criteria
			);
		HashSet<Integer> rawResults = new HashSet<Integer> (query.list());
		
		return rawResults;
	}
	
	protected HashSet<Integer> filterSelectionMarker(String queryString) {
		HashMap<String, String> parsedQuery = parseQuery(queryString);
		String criteria = makeCriterion("lower(marker.name)", parsedQuery.get("operator"), parsedQuery.get("value"));
		org.hibernate.Query query = HibernateHelper.getSession().createQuery(
			"select distinct marker.entry.id from SelectionMarker marker where " + criteria
			);
		HashSet<Integer> rawResults = new HashSet<Integer> (query.list());
		
		return rawResults;
	}
	
	protected HashSet<Integer> filterStatus(String queryString) {
		HashMap<String, String> parsedQuery = parseQuery(queryString);
		String criteria = makeCriterion("lower(entry.status)", parsedQuery.get("operator"), parsedQuery.get("value"));
		org.hibernate.Query query = HibernateHelper.getSession().createQuery(
			"select distinct entry.id from Entry entry where " + criteria
			);
		HashSet<Integer> rawResults = new HashSet<Integer> (query.list());
		
		return rawResults;
	}
	
	protected HashSet<Integer> filterStrainPlasmid(String queryString) {
		HashMap<String, String> parsedQuery = parseQuery(queryString);
		String criteria = makeCriterion("lower(strain.plasmids)", parsedQuery.get("operator"), parsedQuery.get("value"));
		org.hibernate.Query query = HibernateHelper.getSession().createQuery(
			"select distinct strain.id from Strain strain where " + criteria
			);
		HashSet<Integer> rawResults = new HashSet<Integer> (query.list());
		
		return rawResults;
	}
	
	protected HashSet<Integer> filterNameOrAlias(String queryString) {
		HashSet<Integer> nameResults = filterName(queryString);
		HashSet<Integer> aliasResults = filterAlias(queryString);
		
		nameResults.addAll(aliasResults);
		
		return nameResults;
	}
	
	public static void main(String[] args) {
		Query q = new Query();
		//HashSet<Integer> results = q.filterName("!~Keasling");
		//HashSet<Integer> results = q.filterSelectionMarker("*");
		
		ArrayList<String[]> data = new ArrayList<String[]>();
		data.add(new String[] {"name_or_alias", "~kan"});
		LinkedHashSet<Entry> results = q.query(data);
		
		
		for (Entry entry: results) {
			System.out.println("" + entry.getId());
		}
		System.out.println("Total: " + results.size());
		
	}
	
}
