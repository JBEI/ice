package org.jbei.ice.lib.managers;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Sequence;

public class SequenceManager extends Manager {
	public static Sequence create(Sequence sequence) throws ManagerException {
		Sequence result;
		try {
			result = (Sequence) dbSave(sequence);
		} catch (Exception e) {
			throw new ManagerException("Could not create Sequence in db");
		}
		return result;

	}

	public static void delete(Sequence sequence) throws ManagerException {
		try {
			dbDelete(sequence);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ManagerException("Could not delete attachment in db: " + e.toString());
		}
	}

	public static Sequence update(Sequence sequence) throws ManagerException {
		Sequence result;
		try {
			result = (Sequence) dbSave(sequence);
		} catch (Exception e) {
			throw new ManagerException("Could not update Sequence in db");
		}
		return result;

	}

	@SuppressWarnings("unchecked")
	public static List<Sequence> getAll() {
		String queryString = "from Sequence";

		Query query = session.createQuery(queryString);

		return new ArrayList<Sequence>(query.list());
	}

	public static Sequence get(int id) throws ManagerException {
		Sequence sequence = (Sequence) session.load(Sequence.class, id);
		return sequence;
	}

	public static Sequence getByUuid(String uuid) throws ManagerException {
		Query query = session.createQuery("from " + Sequence.class.getName()
				+ " where uuid = :uuid");
		query.setString("uuid", uuid);
		Sequence sequence;
		try {
			sequence = (Sequence) query.uniqueResult();
		} catch (Exception e) {
			throw new ManagerException("Could not retrieve Sequence by uuid");
		}
		return sequence;
	}

	public static Sequence getByEntry(Entry entry) throws ManagerException {
		Sequence sequence;
		Query query = session.createQuery("from " + Sequence.class.getName()
				+ " where entries_id = :entryId");
		query.setInteger("entryId", entry.getId());
		sequence = (Sequence) query.uniqueResult();

		return sequence;
	}

	public static boolean hasSequence(Entry entry) {
		boolean result = false;
		try {
			String queryString = "from " + Sequence.class.getName() + " where entry = :entry";
			Query query = session.createQuery(queryString);
			query.setParameter("entry", entry);
			Sequence sequence = (Sequence) query.uniqueResult();
			if (sequence == null) {

			} else if (sequence.getSequence() == null) {

			} else if (sequence.getSequence().isEmpty()) {

			} else {
				result = true;
			}

		} catch (Exception e) {

		}
		return result;
	}

	// In python, this is used by soap serve to to serve up sequence and 
	// restruction enzymes. 
	public static Sequence getCompositeByEntry(Sequence sequence) throws ManagerException {
		return sequence;
	}
}
