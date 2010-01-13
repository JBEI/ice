package org.jbei.ice.lib.managers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.EntryFundingSource;
import org.jbei.ice.lib.models.FundingSource;
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

	public static final int VISIBILITY_PUBLIC = 9;
	public static final int VISIBILITY_SHARED = 5;
	public static final int VISIBILITY_PRIVATE = 0;

	public EntryManager() {
	}

	public static Plasmid createPlasmid(Plasmid newPlasmid) throws ManagerException {
		Plasmid savedPlasmid = null;

		String number = getNextPartNumber();
		PartNumber partNumber = new PartNumber();
		partNumber.setPartNumber(number);
		LinkedHashSet<PartNumber> partNumbers = new LinkedHashSet<PartNumber>();
		partNumbers.add(partNumber);
		newPlasmid.setPartNumbers(partNumbers);

		newPlasmid.setRecordId(generateUUID());
		newPlasmid.setVersionId(newPlasmid.getRecordId());
		newPlasmid.setRecordType(PLASMID_ENTRY_TYPE);
		newPlasmid.setCreationTime(Calendar.getInstance().getTime());
		try {
			savedPlasmid = (Plasmid) save(newPlasmid);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ManagerException("Could not save Plasmid to db: " + e.toString());
		}
		return savedPlasmid;

	}

	public static Strain createStrain(Strain newStrain) throws ManagerException {
		Strain savedStrain = null;
		String number = getNextPartNumber();
		PartNumber partNumber = new PartNumber();
		partNumber.setPartNumber(number);
		LinkedHashSet<PartNumber> partNumbers = new LinkedHashSet<PartNumber>();
		partNumbers.add(partNumber);
		newStrain.setPartNumbers(partNumbers);
		newStrain.setRecordId(generateUUID());
		newStrain.setVersionId(newStrain.getRecordId());
		newStrain.setRecordType(STRAIN_ENTRY_TYPE);
		newStrain.setCreationTime(Calendar.getInstance().getTime());
		try {
			savedStrain = (Strain) save(newStrain);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ManagerException("Could not save Plasmid to db: " + e.toString());
		}
		return savedStrain;

	}

	public static Part createPart(Part newPart) throws ManagerException {
		Part savedStrain = null;
		String number = getNextPartNumber();
		PartNumber partNumber = new PartNumber();
		partNumber.setPartNumber(number);
		LinkedHashSet<PartNumber> partNumbers = new LinkedHashSet<PartNumber>();
		partNumbers.add(partNumber);
		newPart.setPartNumbers(partNumbers);
		newPart.setRecordId(generateUUID());
		newPart.setVersionId(newPart.getRecordId());
		newPart.setRecordType(PART_ENTRY_TYPE);
		newPart.setCreationTime(Calendar.getInstance().getTime());
		try {
			savedStrain = (Part) save(newPart);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ManagerException("Could not save Plasmid to db: " + e.toString());
		}
		return savedStrain;
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
			Query query = session.createQuery("from " + Entry.class.getName() + " where id = :id");
			query.setParameter("id", id);
			Entry entry = (Entry) query.uniqueResult();
			return entry;

		} catch (HibernateException e) {
			throw new ManagerException("Couldn't retrieve Entry by id: " + String.valueOf(id), e);
		}
	}

	public static Entry getByRecordId(String recordId) throws ManagerException {
		try {
			Query query = session.createQuery("from " + Entry.class.getName()
					+ " where recordId = :recordId");
			query.setParameter("recordId", recordId);

			Entry entry = (Entry) query.uniqueResult();

			return entry;
		} catch (HibernateException e) {
			throw new ManagerException("Couldn't retrieve Entry by recordId: " + recordId, e);
		}
	}

	public static Entry getByPartNumber(String partNumber) throws ManagerException {
		try {
			Query query = session.createQuery("from " + PartNumber.class.getName()
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
			throw new ManagerException("Couldn't retrieve Entry by partNumber: " + partNumber, e);
		}
	}

	public static Entry getByName(String name) throws ManagerException {
		try {
			Query query = session.createQuery("from " + Name.class.getName()
					+ " where name = :name");
			query.setParameter("name", name);

			Name entryName = (Name) query.uniqueResult();

			if (entryName == null) {
				return null;
			}

			return entryName.getEntry();
		} catch (HibernateException e) {
			throw new ManagerException("Couldn't retrieve Entry by name: " + name, e);
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

		return new LinkedHashSet<Entry>(query.list());
	}

	@SuppressWarnings("unchecked")
	public static Set<Entry> getAll() {
		String queryString = "from Entry";

		Query query = session.createQuery(queryString);

		return new LinkedHashSet<Entry>(query.list());
	}

	public static int getNumberOfPublicEntries() {
		return getNumberOfEntriesByVisibility(VISIBILITY_PUBLIC);
	}

	public static int getNumberOfEntries() {
		String queryString = "select id from Entry";

		Query query = session.createQuery(queryString);

		return query.list().size();
	}

	public static int getNumberOfEntriesByVisibility(int visibility) {
		String queryString = "select id from Entry where visibility = "
				+ String.valueOf(visibility);

		Query query = session.createQuery(queryString);

		return query.list().size();
	}

	public static String generateUUID() {
		return UUID.randomUUID().toString();
	}

	@SuppressWarnings("unchecked")
	public static String generateNextPartNumber(String prefix, String delimiter, String suffix)
			throws ManagerException {
		try {
			String queryString = "from " + PartNumber.class.getName() + " where partNumber LIKE '"
					+ prefix + "%' ORDER BY partNumber DESC";
			Query query = session.createQuery(queryString);

			ArrayList<PartNumber> tempList = new ArrayList<PartNumber>(query.list());
			PartNumber entryPartNumber = null;
			if (tempList.size() > 0) {
				entryPartNumber = (PartNumber) tempList.get(0);
			}

			String nextPartNumber = null;
			if (entryPartNumber == null) {
				nextPartNumber = prefix + delimiter + suffix;
			} else {
				String[] parts = entryPartNumber.getPartNumber().split(prefix + delimiter);

				if (parts != null && parts.length == 2) {
					try {
						int value = Integer.valueOf(parts[1]);

						value++;

						nextPartNumber = prefix + delimiter
								+ String.format("%0" + suffix.length() + "d", value);
					} catch (Exception e) {
						throw new ManagerException("Couldn't parse partNumber", e);
					}
				} else {
					throw new ManagerException("Couldn't parse partNumber");
				}
			}

			return nextPartNumber;
		} catch (HibernateException e) {
			throw new ManagerException("Couldn't retrieve Entry by partNumber", e);
		}
	}

	public static String getNextPartNumber() throws ManagerException {
		return generateNextPartNumber(JbeirSettings.getSetting("PART_NUMBER_PREFIX"), JbeirSettings
				.getSetting("PART_NUMBER_DELIMITER"), JbeirSettings
				.getSetting("PART_NUMBER_DIGITAL_SUFFIX"));
	}

	/**
	 * Updates or Inserts unique funding source and returns the result
	 */
	public static FundingSource saveFundingSource(FundingSource fundingSource)
			throws ManagerException {
		FundingSource result;
		try {
			String queryString = "from " + FundingSource.class.getName()
					+ " where fundingSource=:fundingSource AND"
					+ " principalInvestigator=:principalInvestigator";
			Query query = session.createQuery(queryString);
			query.setParameter("fundingSource", fundingSource.getFundingSource());
			query.setParameter("principalInvestigator", fundingSource.getPrincipalInvestigator());

			FundingSource existingFundingSource = (FundingSource) query.uniqueResult();
			if (existingFundingSource == null) {
				result = (FundingSource) dbSave(fundingSource);
			} else {
				result = existingFundingSource;
			}

		} catch (Exception e) {
			String msg = "Could not save unique funding source";
			throw new ManagerException(msg, e);
		}

		return result;

	}

	public static Entry save(Entry entry) throws ManagerException {
		Entry result = null;
		// deal with associated objects here instead of making individual forms
		// deal with foreign key checks. Deletion of old values happen through
		// Set.clear() and
		// hibernate cascade delete-orphaned in the model.Entry

		for (SelectionMarker selectionMarker : entry.getSelectionMarkers()) {
			selectionMarker.setEntry(entry);
		}
		for (Link link : entry.getLinks()) {
			link.setEntry(entry);
		}
		for (Name name : entry.getNames()) {
			name.setEntry(entry);
		}
		for (PartNumber partNumber : entry.getPartNumbers()) {
			partNumber.setEntry(entry);
		}

		entry.setModificationTime(Calendar.getInstance().getTime());

		// Manual cascade of EntryFundingSource. Guarantees unique FundingSource
		for (EntryFundingSource entryFundingSource : entry.getEntryFundingSources()) {
			FundingSource tempFundingSource = entryFundingSource.getFundingSource();
			entryFundingSource.setFundingSource(saveFundingSource(tempFundingSource));
			dbSave(entryFundingSource.getFundingSource());
		}

		result = (Entry) dbSave(entry);

		return result;
	}

	public static void main(String[] args) {

		int offset = 0;
		int limit = 30;
		ArrayList<String[]> data = new ArrayList<String[]>();
		data.add(new String[] { "owner_email", "tsham@lbl.gov" });
		Set<Entry> temp = EntryManager.getByFilter(data, offset, limit);

		for (Entry entry : temp) {
			System.out.println("" + entry.getId());
		}
	}

}
