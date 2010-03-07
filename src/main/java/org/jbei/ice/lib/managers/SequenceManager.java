package org.jbei.ice.lib.managers;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Feature;
import org.jbei.ice.lib.models.FeatureDNA;
import org.jbei.ice.lib.models.Group;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.SequenceFeature;
import org.jbei.ice.lib.utils.SequenceUtils;

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
            throw new ManagerException("Could not delete sequence in db: " + e.toString());
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
        Session session = getSession();
        Query query = session.createQuery(queryString);
        ArrayList<Sequence> result = null;
        try {
            result = new ArrayList<Sequence>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not get all sequences " + e.toString(), e);
        } finally {

        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static List<Sequence> getAllVisible() {
        Group everybodyGroup = null;
        List<Sequence> result = null;
        Session session = getSession();
        try {
            everybodyGroup = GroupManager.getEverybodyGroup();
            String queryString = "select entry.sequence from Entry entry, ReadGroup readGroup where readGroup.group = :group and readGroup.entry = entry";
            Query query = session.createQuery(queryString);
            query.setParameter("group", everybodyGroup);
            result = new ArrayList<Sequence>(query.list());

        } catch (ManagerException e) {
            Logger.error("getAllVisible: " + e.toString(), e);
        } finally {

        }
        return result;
    }

    public static Sequence get(int id) throws ManagerException {
        Session session = getSession();
        Sequence sequence = null;

        try {
            sequence = (Sequence) session.load(Sequence.class, id);
        } catch (HibernateException e) {
            Logger.error("Could not get sequence ", e);
        }

        return sequence;
    }

    public static Sequence getByUuid(String uuid) throws ManagerException {
        Session session = getSession();

        Query query = session.createQuery("from " + Sequence.class.getName()
                + " where uuid = :uuid");
        query.setString("uuid", uuid);

        Sequence sequence = null;

        try {
            sequence = (Sequence) query.uniqueResult();
        } catch (HibernateException e) {
            Logger.error("Could not retrieve Sequence by uuid", e);
        }

        return sequence;
    }

    public static Sequence getByEntry(Entry entry) throws ManagerException {
        Sequence sequence = null;
        Session session = getSession();

        Query query = session.createQuery("from " + Sequence.class.getName()
                + " where entries_id = :entryId");
        query.setInteger("entryId", entry.getId());

        try {
            sequence = (Sequence) query.uniqueResult();
        } catch (HibernateException e) {
            Logger.error("Could not get sequence ", e);
        }

        return sequence;
    }

    public static boolean hasSequence(Entry entry) {
        boolean result = false;
        Session session = getSession();

        try {
            String queryString = "from " + Sequence.class.getName() + " where entry = :entry";
            Query query = session.createQuery(queryString);
            query.setParameter("entry", entry);
            Sequence sequence = (Sequence) query.uniqueResult();
            if (sequence == null || sequence.getSequence() == null
                    || sequence.getSequence().isEmpty()) {
                result = false;
            } else {
                result = true;
            }
        } catch (Exception e) {
            String msg = "Could net determine if entry has sequence " + entry.getRecordId();
            Logger.warn(msg);
        }

        return result;
    }

    // In python, this is used by soap serve to to serve up sequence and 
    // restruction enzymes. 
    public static Sequence getCompositeByEntry(Sequence sequence) throws ManagerException {
        return sequence;
    }

    public static Sequence save(Sequence sequence) throws ManagerException {
        Sequence result = null;

        result = (Sequence) dbSave(sequence);

        return result;
    }

    public static SequenceFeature save(SequenceFeature sequenceFeature) throws ManagerException {
        SequenceFeature result = null;

        result = (SequenceFeature) dbSave(sequenceFeature);

        return result;
    }

    public static Feature save(Feature feature) throws ManagerException {
        Feature result = null;

        Feature existingFeature = getExistingFeature(feature);

        if (existingFeature == null) {
            result = (Feature) dbSave(feature);
        } else {
            result = existingFeature;
        }

        return result;
    }

    public static Feature getExistingFeature(Feature feature) {
        Feature result = null;
        Session session = getSession();
        try {
            FeatureDNA featureDNA = feature.getFeatureDna();

            String queryString = "from " + FeatureDNA.class.getName() + " where hash = :hash";
            Query query = session.createQuery(queryString);
            query.setParameter("hash", SequenceUtils
                    .calculateSequenceHash(featureDNA.getSequence()));

            FeatureDNA resultFeatureDNA = (FeatureDNA) query.uniqueResult();

            if (resultFeatureDNA == null) {
                query.setParameter("hash", SequenceUtils.calculateSequenceHash(SequenceUtils
                        .reverseComplement(featureDNA.getSequence())));

                resultFeatureDNA = (FeatureDNA) query.uniqueResult();

                result = (resultFeatureDNA != null) ? resultFeatureDNA.getFeature() : null;
            } else {
                result = resultFeatureDNA.getFeature();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.warn(e.toString());
        } finally {

        }

        return result;
    }
}
