package org.jbei.ice.lib.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.models.AnnotationLocation;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Feature;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.SequenceFeature;
import org.jbei.ice.lib.utils.SequenceUtils;
import org.jbei.ice.lib.utils.UtilityException;

/**
 * Manipulate {@link Sequence} and associated objects in the database.
 * 
 * @author Timothy Ham, Zinovii Dmytriv
 * 
 */
public class SequenceManager {
    /**
     * Save the given {@link Sequence} object in the database.
     * 
     * @param sequence
     * @return Saved Sequence object
     * @throws ManagerException
     */
    public static Sequence saveSequence(Sequence sequence) throws ManagerException {
        if (sequence == null) {
            throw new ManagerException("Failed to save null sequence!");
        }

        if (sequence.getEntry() == null) {
            throw new ManagerException("Failed to save sequence without entry!");
        }

        try {
            normalizeAnnotationLocations(sequence);
            Set<SequenceFeature> sequenceFeatureSet = sequence.getSequenceFeatures();

            if (sequenceFeatureSet != null && sequenceFeatureSet.size() > 0) {
                for (SequenceFeature sequenceFeature : sequenceFeatureSet) {
                    Feature feature = sequenceFeature.getFeature();

                    if (feature == null) {
                        throw new ManagerException("SequenceFeature has no feature");
                    }

                    Feature existingFeature = getFeatureBySequence(feature.getSequence());

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

    /**
     * Delete the given {@link Sequence} object in the database.
     * 
     * @param sequence
     * @throws ManagerException
     */
    public static void deleteSequence(Sequence sequence) throws ManagerException {
        if (sequence == null) {
            throw new ManagerException("Failed to delete null sequence!");
        }

        try {
            sequence.setEntry(null);

            DAO.delete(sequence);
        } catch (DAOException e) {
            throw new ManagerException("Failed to delete sequence!", e);
        }
    }

    /**
     * Save the given {@link Feature} object in the database.
     * 
     * @param feature
     * @return Saved Feature object.
     * @throws ManagerException
     */
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

    /**
     * Delete the give {@link Feature} object in the database.
     * 
     * @param feature
     * @throws ManagerException
     */
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

    /**
     * Retrieve the {@link Sequence} object associated with the given {@link Entry} object.
     * 
     * @param entry
     * @return Sequence object.
     * @throws ManagerException
     */
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
            if (session.isOpen()) {
                session.close();
            }
        }
        normalizeAnnotationLocations(sequence);
        return sequence;
    }

    /**
     * Retrieve all {@link Sequence} objects in the database.
     * 
     * @return ArrayList of Sequence objects.
     * @throws ManagerException
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<Sequence> getAllSequences() throws ManagerException {
        ArrayList<Sequence> sequences = null;

        Session session = DAO.newSession();
        try {
            Query query = session.createQuery("from " + Sequence.class.getName());

            @SuppressWarnings("rawtypes")
            List list = query.list();

            if (list != null) {
                sequences = (ArrayList<Sequence>) list;
            }
        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve entries!", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return sequences;
    }

    /**
     * Retrieve the {@link Feature} object for the given Feature object. Since only one Feature
     * object should exist for a given unique sequence, this method is used to prevent creation of
     * duplicate features.
     * <p>
     * Use this to look for a reference feature that should exist in the features table, such as
     * known biobrick prefix/suffix/scar sequence. This method creates the feature if it doesn't
     * exist, using the values passed.
     * <p>
     * 
     * 
     * @param feature
     * @return Feature object.
     * @throws ControllerException
     * @throws ManagerException
     */
    public static Feature getReferenceFeature(Feature feature) throws ControllerException {
        try {
            Feature oldFeature = getFeatureBySequence(feature.getSequence());
            if (oldFeature == null) {
                Feature newFeature = SequenceManager.saveFeature(feature);
                return newFeature;
            } else {
                return oldFeature;
            }
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

    }

    /**
     * Retrieve a {@link Feature} object by its id.
     * 
     * @param id
     * @return Feature object.
     */
    public static Feature getFeature(long id) {
        Feature result = null;
        Session session = DAO.newSession();
        try {
            String queryString = "from " + Feature.class.getName() + " where id = :id";
            Query query = session.createQuery(queryString);
            query.setParameter("id", id);
            Object queryResult = query.uniqueResult();
            if (true) {
                result = (Feature) queryResult;
            }
        } catch (HibernateException e) {
            e.printStackTrace();
        }
        return result;

    }

    /**
     * Retrieve all {@link Feature} objects in the database.
     * 
     * @return ArrayList of Feature objects.
     * @throws ManagerException
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<Feature> getAllFeatures() throws ManagerException {
        ArrayList<Feature> features = null;
        Session session = DAO.newSession();
        try {
            Query query = session.createQuery("from " + Feature.class.getName());
            @SuppressWarnings("rawtypes")
            List list = query.list();

            if (list != null) {
                features = (ArrayList<Feature>) list;
            }
        } catch (HibernateException e) {
            throw new ManagerException(e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return features;
    }

    /**
     * Retrieve the {@link Feature} object with the given DNA sequence string.
     * 
     * @param featureDnaSequence
     * @return Feature object.
     * @throws ManagerException
     */
    private static Feature getFeatureBySequence(String featureDnaSequence) throws ManagerException {
        featureDnaSequence = featureDnaSequence.toLowerCase();
        Feature result = null;
        Session session = DAO.newSession();

        try {
            String queryString = "from " + Feature.class.getName() + " where hash = :hash";
            Query query = session.createQuery(queryString);
            query.setParameter("hash", SequenceUtils.calculateSequenceHash(featureDnaSequence));

            Object queryResult = query.uniqueResult();

            if (queryResult != null) {
                result = (Feature) queryResult;
            } else {
                query.setParameter("hash", SequenceUtils.calculateSequenceHash(SequenceUtils
                        .reverseComplement(featureDnaSequence)));

                queryResult = query.uniqueResult();

                if (queryResult != null) {
                    result = (Feature) queryResult;
                } else {
                    result = null;
                }
            }
        } catch (HibernateException e) {
            throw new ManagerException("Failed to get Feature by sequence!", e);
        } catch (UtilityException e) {
            throw new ManagerException("Failed to get Feature by sequence!", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return result;
    }

    /**
     * Normalize {@link AnnotationLocation}s by fixing strangely defined annotationLocations.
     * <p>
     * Fix locations that encompass the entire sequence, but defined strangely. This causes problems
     * elsewhere.
     * 
     * @param sequence
     * @return
     */
    private static Sequence normalizeAnnotationLocations(Sequence sequence) {
        if (sequence == null) {
            return null;
        }
        int length = sequence.getSequence().length();
        boolean wholeSequence = false;
        for (SequenceFeature sequenceFeature : sequence.getSequenceFeatures()) {
            wholeSequence = false;
            Set<AnnotationLocation> locations = sequenceFeature.getAnnotationLocations();
            for (AnnotationLocation location : locations) {
                if (location.getGenbankStart() == location.getEnd() + 1) {
                    wholeSequence = true;
                }
            }
            if (wholeSequence) {
                sequenceFeature.setStrand(1);
                sequenceFeature.getAnnotationLocations().clear();
                sequenceFeature.getAnnotationLocations().add(
                    new AnnotationLocation(1, length, sequenceFeature));
            }
        }
        return sequence;
    }
}
