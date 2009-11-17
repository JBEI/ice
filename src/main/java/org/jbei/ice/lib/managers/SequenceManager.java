package org.jbei.ice.lib.managers;

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
	
	public static Sequence get(int id) throws ManagerException {
		Sequence sequence = (Sequence) HibernateHelper.getSession().load(
				Sequence.class, id);
		return sequence;
	}
	
	
	public static Sequence getByUuid(String uuid) throws ManagerException {
		Query query = HibernateHelper.getSession().createQuery(
				"from " + Sequence.class.getName()
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
		Query query = HibernateHelper.getSession().createQuery(
				"from " + Sequence.class.getName() + " where entries_id = :entryId");
		query.setInteger("entryId", entry.getId());
		sequence = (Sequence) query.uniqueResult();
		
		return sequence;
	}
	/* These methods doesn't seem to be used by any python code, so it's not 
	 * implemented in java.
	public static Set<Feature> getFeaturesByHash(String hash) throws ManagerException {
		
		Set<Feature> results = new HashSet<Feature>();
		
		Query query = HibernateHelper.getSession().createQuery(
				"from " + FeatureDNA.class.getName() + " where hash = :hash");
		query.setString("hash", hash);
		FeatureDNA featureDna = (FeatureDNA) query.uniqueResult();
		Feature feature = featureDna.getFeature();
		
		
		
		
		return results;
	}
	*/
	
	// In python, this is used by soap serve to to serve up sequence and 
	// restruction enzymes. 
	public static Sequence getCompositeByEntry(Sequence sequence) throws ManagerException {
		return sequence;
	}
	
	
}
