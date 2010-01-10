package org.jbei.ice.lib.managers;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Link;
import org.jbei.ice.lib.models.Name;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.PartNumber;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.lib.utils.JbeirSettings;

public class EntryManager extends Manager {
	public static final String STRAIN_ENTRY_TYPE = "strain";
	public static final String PLASMID_ENTRY_TYPE = "plasmid";
	public static final String PART_ENTRY_TYPE = "part";

	// TODO: This should be moved to Settings file
	public static final String DEFAULT_PART_NUMBER_PREFIX = (String) JbeirSettings.getSetting("PART_NAME_PREFIX");
	public static final String DEFAULT_PART_NUMBER_DIGITAL_SUFFIX = "000001";
	public static final String DEFAULT_PART_NUMBER_DELIMITER = "_";

	public EntryManager() {
	}

	public static Plasmid createPlasmid(Plasmid newPlasmid) throws ManagerException {

		Plasmid createPlasmid = createPlasmid(newPlasmid.getNames(), newPlasmid.getOwner(),
				newPlasmid.getOwnerEmail(), newPlasmid.getCreator(),
				newPlasmid.getCreatorEmail(), newPlasmid.getVisibility(),
				newPlasmid.getStatus(), newPlasmid.getLinks(),
				newPlasmid.getSelectionMarkers(), newPlasmid.getAlias(),
				newPlasmid.getKeywords(), newPlasmid.getShortDescription(),
				newPlasmid.getLongDescription(), newPlasmid.getReferences(),
				newPlasmid.getBackbone(), newPlasmid.getOriginOfReplication(),
				newPlasmid.getPromoters(), newPlasmid.getCircular());
		return createPlasmid;
	}
	
	public static Plasmid createPlasmid(Set<Name> names, String owner,
			String ownerEmail, String creator, String creatorEmail,
			int visibility, String status, Set<Link> links,
			Set<SelectionMarker> selectionMarkers, String alias,
			String keywords, String shortDescription, String longDescription,
			String references, String backbone, String originOfReplication,
			String promoters, boolean circular) throws ManagerException {

			String recordId = generateUUID();
			String versionId = recordId;

			Plasmid plasmid = new Plasmid(recordId, versionId,
					PLASMID_ENTRY_TYPE, owner, ownerEmail, creator,
					creatorEmail, visibility, status, alias, keywords,
					shortDescription, longDescription, references, new Date(),
					null, backbone, originOfReplication, promoters, circular);
			
			try {
				dbSave(plasmid);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new ManagerException("Could not save Plasmid to db: " + e.toString());
			}
			
			for (Name name : names) {
				name.setEntry(plasmid);
			}
			plasmid.setNames(names);
			
			for (Link link : links) {
				link.setEntry(plasmid);
			}
			plasmid.setLinks(links);
			
			for (SelectionMarker marker: selectionMarkers) {
				marker.setEntry(plasmid);
			}
			plasmid.setSelectionMarkers(selectionMarkers);

			try {
				dbSave(plasmid);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new ManagerException("Could not save Plasmid to db: " + e.toString());
			}

			return plasmid;
		
	}

	public static Strain createStrain(Set<Name> names, String owner,
			String ownerEmail, String creator, String creatorEmail,
			int visibility, String status, Set<Link> links,
			Set<SelectionMarker> selectionMarkers, String alias,
			String keywords, String shortDescription, String longDescription,
			String references, String host, String genotypePhenotype,
			String plasmids) throws ManagerException {
		try {
			String recordId = generateUUID();
			String versionId = recordId;

			Strain strain = new Strain(recordId, versionId, STRAIN_ENTRY_TYPE,
					owner, ownerEmail, creator, creatorEmail, visibility,
					status, alias, keywords, shortDescription, longDescription,
					references, new Date(), null, host, genotypePhenotype,
					plasmids);

			strain.setNames(names);
			strain.setLinks(links);
			strain.setSelectionMarkers(selectionMarkers);

			try {
				dbSave(strain);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new ManagerException("Could not save Strain to db: " + e.toString());
			}

			return strain;
		} catch (HibernateException e) {
			throw new ManagerException("Couldn't create Plasmid", e);
		}
	}

	public static Part createPart(Set<Name> names, String owner,
			String ownerEmail, String creator, String creatorEmail,
			int visibility, String status, Set<Link> links, String alias,
			String keywords, String shortDescription, String longDescription,
			String references, String packageFormat, String pkgdDnaFwdHash,
			String pkgdDnaRevHash) throws ManagerException {
		try {
			String recordId = generateUUID();
			String versionId = recordId;

			Part part = new Part(recordId, versionId, PART_ENTRY_TYPE, owner,
					ownerEmail, creator, creatorEmail, visibility, status,
					alias, keywords, shortDescription, longDescription,
					references, new Date(), null, packageFormat,
					pkgdDnaFwdHash, pkgdDnaRevHash);

			part.setNames(names);
			part.setLinks(links);

			try {
				dbSave(part);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new ManagerException("Could not save Part to db: " + e.toString());
			}

			return part;
		} catch (HibernateException e) {
			throw new ManagerException("Couldn't create Plasmid", e);
		}
	}

	public static void remove(Entry entry) throws ManagerException {
		try {
			dbDelete(entry);
		} catch (Exception e) {
			throw new ManagerException("Couldn't delete Entry: " + e.toString());
		}
	}

	public static Entry get(int id) throws ManagerException {
		try {
			Query query = session.createQuery(
					"from " + Entry.class.getName() + " where id = :id");
			query.setParameter("id", id);
			Entry entry = (Entry) query.uniqueResult();
			return entry;
			
		} catch (HibernateException e) {
			throw new ManagerException("Couldn't retrieve Entry by id: "
					+ String.valueOf(id), e);
		}
	}

	public static Entry getByRecordId(String recordId) throws ManagerException {
		try {
			Query query = session.createQuery(
					"from " + Entry.class.getName()
							+ " where recordId = :recordId");
			query.setParameter("recordId", recordId);

			Entry entry = (Entry) query.uniqueResult();

			return entry;
		} catch (HibernateException e) {
			throw new ManagerException("Couldn't retrieve Entry by recordId: "
					+ recordId, e);
		}
	}

	public static Entry getByPartNumber(String partNumber)
			throws ManagerException {
		try {
			Query query = session.createQuery(
					"from " + PartNumber.class.getName()
							+ " where partNumber = :partNumber");
			query.setParameter("partNumber", partNumber);

			PartNumber entryPartNumber = (PartNumber) query.uniqueResult();

			Entry entry;
			if (entryPartNumber == null) {
				entry = null;
			} else {
				entry = entryPartNumber.getEntry();
			}

			return entry;
		} catch (HibernateException e) {
			throw new ManagerException(
					"Couldn't retrieve Entry by partNumber: " + partNumber, e);
		}
	}

	public static Entry getByName(String name) throws ManagerException {
		try {
			Query query = session.createQuery(
					"from " + Name.class.getName() + " where name = :name");
			query.setParameter("name", name);

			Name entryName = (Name) query.uniqueResult();

			if (entryName == null) {
				return null;
			}

			return entryName.getEntry();
		} catch (HibernateException e) {
			throw new ManagerException("Couldn't retrieve Entry by name: "
					+ name, e);
		}
	}

	public static Set<Entry> getByFilter(ArrayList<String[]> data, int offset, int limit) {
		org.jbei.ice.lib.query.Query q = new org.jbei.ice.lib.query.Query();
		LinkedHashSet<Entry> result = q.query(data, offset, limit);
		return result;

	}
	
	@SuppressWarnings("unchecked")
	public static LinkedHashSet<Entry> getByAccount(Account account, int offset, int limit) {
		String queryString = "from Entry where ownerEmail = :ownerEmail";
		Query query = session.createQuery(queryString);
		query.setParameter("ownerEmail", account.getEmail());
		LinkedHashSet<Entry> result = new LinkedHashSet<Entry>(query.list());
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static Set<Entry> getAll() {
		String queryString = "from Entry";
		Query query = session.createQuery(queryString);
		LinkedHashSet<Entry> result = new LinkedHashSet<Entry>(query.list());
		return result;
	}
	
	public static String generateUUID() {
		return UUID.randomUUID().toString();
	}

	public static String generateNextPartNumber(String prefix, String delimiter,
			String suffix) throws ManagerException {
		try {
			Query query = session
					.createQuery(
							"from "
									+ PartNumber.class.getName()
									+ " where partNumber LIKE ':partNumberPrefix%' ORDER BY partNumber DESC");
			query.setParameter("partNumberPrefix", prefix);

			PartNumber entryPartNumber = (PartNumber) query.uniqueResult();

			String nextPartNumber = null;
			if (entryPartNumber == null) {
				nextPartNumber = prefix + delimiter + suffix;
			} else {
				String[] parts = entryPartNumber.getPartNumber().split(
						prefix + delimiter);

				if (parts != null && parts.length == 1) {
					try {
						int value = Integer.valueOf(parts[0]);

						value++;

						nextPartNumber = prefix
								+ delimiter
								+ String.format("%0" + suffix.length() + "d",
										value);
					} catch (Exception e) {
						throw new ManagerException("Couldn't parse partNumber",
								e);
					}
				} else {
					throw new ManagerException("Couldn't parse partNumber");
				}
			}

			return nextPartNumber;
		} catch (HibernateException e) {
			throw new ManagerException("Couldn't retrieve Entry by partNumber",
					e);
		}
	}

	public static String getNextPartNumber() throws ManagerException {
		return generateNextPartNumber(DEFAULT_PART_NUMBER_PREFIX,
				DEFAULT_PART_NUMBER_DELIMITER,
				DEFAULT_PART_NUMBER_DIGITAL_SUFFIX);
	}

	public static Entry save(Entry entry) throws ManagerException {
		Entry result = null;
		//deal with associated objects here instead of making individual forms
		//deal with foreign key checks. Deletion of old values happen through Set.clear() and 
		//hibernate cascade delete-orphaned in the model.Entry
		
		Set<Name> temp = entry.getNames();
		
		for (SelectionMarker selectionMarker : entry.getSelectionMarkers()) {
			selectionMarker.setEntry(entry);
		}
		for (Link link : entry.getLinks()){
			link.setEntry(entry);
		}
		for (Name name: entry.getNames()) {
			name.setEntry(entry);
		}
		for (PartNumber partNumber : entry.getPartNumbers()) {
			partNumber.setEntry(entry);
		}
		
		result = (Entry) dbSave(entry);
		
		return result;
	}
	
	public static void main(String[] args) {
		int offset = 0;
		int limit = 30;
		ArrayList<String[]> data = new ArrayList<String[]> () ;
		data.add(new String[] {"owner_email", "tsham@lbl.gov"});
		Set<Entry> temp = EntryManager.getByFilter(data, offset, limit);
		
		for (Entry entry : temp) {
			System.out.println("" + entry.getId());
		}
	}
	
	
}
