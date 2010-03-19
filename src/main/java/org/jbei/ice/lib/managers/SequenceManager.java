package org.jbei.ice.lib.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.biojava.bio.BioException;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Feature;
import org.jbei.ice.lib.models.FeatureDNA;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.SequenceFeature;
import org.jbei.ice.lib.utils.SequenceUtils;

public class SequenceManager {
    public static Sequence saveSequence(Sequence sequence) throws ManagerException {
        if (sequence == null) {
            throw new ManagerException("Failed to save null sequence!");
        }

        if (sequence.getEntry() == null) {
            throw new ManagerException("Failed to save sequence without entry!");
        }

        try {
            Set<SequenceFeature> sequenceFeatureSet = sequence.getSequenceFeatures();

            if (sequenceFeatureSet != null && sequenceFeatureSet.size() > 0) {
                for (SequenceFeature sequenceFeature : sequenceFeatureSet) {
                    Feature feature = sequenceFeature.getFeature();

                    if (feature == null || feature.getFeatureDna() == null) {
                        throw new ManagerException(
                                "SequenceFeature has no feature or featureDNA assigned to it!");
                    }

                    Feature existingFeature = getFeatureBySequence(feature.getFeatureDna()
                            .getSequence());

                    if (existingFeature == null) { // new feature -> save it
                        existingFeature = saveFeature(feature);
                    }

                    sequenceFeature.setFeature(existingFeature);
                }
            }

            sequence = (Sequence) DAO.save(sequence);
        } catch (DAOException e) {
            throw new ManagerException("Failed to save sequence!", e);
        }

        return sequence;
    }

    public static void deleteSequence(Sequence sequence) throws ManagerException {
        if (sequence == null) {
            throw new ManagerException("Failed to delete null sequence!");
        }

        try {
            if (sequence.getEntry() != null) {
                Entry entry = sequence.getEntry();

                entry.setSequence(null);

                DAO.save(entry);

                sequence.setEntry(null);
            }

            DAO.delete(sequence);
        } catch (DAOException e) {
            throw new ManagerException("Failed to delete sequence!", e);
        }
    }

    public static Feature saveFeature(Feature feature) throws ManagerException {
        if (feature == null) {
            throw new ManagerException("Failed to save null feature!");
        }

        try {
            feature = (Feature) DAO.save(feature);
        } catch (DAOException e) {
            throw new ManagerException("Failed to save sequenceFeature!", e);
        }

        return feature;
    }

    public static void deleteFeature(Feature feature) throws ManagerException {
        if (feature == null) {
            throw new ManagerException("Failed to delete null feature!");
        }

        try {
            DAO.delete(feature);
        } catch (DAOException e) {
            throw new ManagerException("Failed to delete feature!", e);
        }
    }

    public static Sequence getByEntry(Entry entry) throws ManagerException {
        Sequence sequence = null;

        Session session = DAO.newSession();
        try {
            String queryString = "from " + Sequence.class.getName()
                    + " as sequence where sequence.entry = :entry";

            Query query = session.createQuery(queryString);

            query.setEntity("entry", entry);

            Object queryResult = query.uniqueResult();

            if (queryResult != null) {
                sequence = (Sequence) queryResult;
            }
        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve sequence by entry: " + entry.getId(), e);
        } finally {
            session.close();
        }

        return sequence;
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<Sequence> getAllSequences() throws ManagerException {
        ArrayList<Sequence> sequences = null;

        Session session = DAO.newSession();
        try {
            Query query = session.createQuery("from " + Sequence.class.getName());

            List list = query.list();

            if (list != null) {
                sequences = (ArrayList<Sequence>) list;
            }
        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve entries!", e);
        } finally {
            session.close();
        }

        return sequences;
    }

    private static Feature getFeatureBySequence(String featureDNASequence) throws ManagerException {
        Feature result = null;
        Session session = DAO.newSession();

        try {
            String queryString = "from " + FeatureDNA.class.getName() + " where hash = :hash";
            Query query = session.createQuery(queryString);
            query.setParameter("hash", SequenceUtils.calculateSequenceHash(featureDNASequence));

            Object queryResult = query.uniqueResult();

            if (queryResult != null) {
                result = ((FeatureDNA) queryResult).getFeature();
            } else {
                query.setParameter("hash", SequenceUtils.calculateSequenceHash(SequenceUtils
                        .reverseComplement(featureDNASequence)));

                queryResult = query.uniqueResult();

                if (queryResult != null) {
                    result = ((FeatureDNA) queryResult).getFeature();
                } else {
                    result = null;
                }
            }
        } catch (HibernateException e) {
            throw new ManagerException("Failed to get Feature by sequence!", e);
        } catch (BioException e) {
            throw new ManagerException("Failed to get Feature by sequence!", e);
        } finally {
            session.close();
        }

        return result;
    }
}
