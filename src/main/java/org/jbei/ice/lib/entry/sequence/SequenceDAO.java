package org.jbei.ice.lib.entry.sequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.hibernate.HibernateRepository;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.AnnotationLocation;
import org.jbei.ice.lib.models.Feature;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.SequenceFeature;
import org.jbei.ice.lib.utils.SequenceUtils;
import org.jbei.ice.lib.utils.UtilityException;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 * Manipulate {@link Sequence} and associated objects in the database.
 *
 * @author Hector Plahar, Timothy Ham, Zinovii Dmytriv
 */
public class SequenceDAO extends HibernateRepository<Sequence> {
    /**
     * Save the given {@link Sequence} object in the database.
     *
     * @param sequence sequence to save
     * @return Saved Sequence object
     * @throws DAOException
     */
    public Sequence saveSequence(Sequence sequence) throws DAOException {
        if (sequence == null) {
            throw new DAOException("Failed to save null sequence!");
        }

        if (sequence.getEntry() == null) {
            throw new DAOException("Failed to save sequence without entry!");
        }

        normalizeAnnotationLocations(sequence);
        Set<SequenceFeature> sequenceFeatureSet = sequence.getSequenceFeatures();

        if (sequenceFeatureSet != null && sequenceFeatureSet.size() > 0) {
            for (SequenceFeature sequenceFeature : sequenceFeatureSet) {
                Feature feature = sequenceFeature.getFeature();

                if (feature == null) {
                    throw new DAOException("SequenceFeature has no feature");
                }

                Feature existingFeature = getFeatureBySequence(feature.getSequence());

                if (existingFeature == null) { // new feature -> save it
                    existingFeature = saveFeature(feature);
                }

                sequenceFeature.setFeature(existingFeature);
            }
        }

        sequence = super.saveOrUpdate(sequence);

        return sequence;
    }

    /**
     * Delete the given {@link Sequence} object in the database.
     *
     * @param sequence sequence to delete
     * @throws DAOException
     */
    public void deleteSequence(Sequence sequence) throws DAOException {
        sequence.setEntry(null);
        super.delete(sequence);
    }

    /**
     * Save the given {@link Feature} object in the database.
     *
     * @param feature feature to save
     * @return Saved Feature object.
     * @throws DAOException
     */
    public Feature saveFeature(Feature feature) throws DAOException {
        if (feature == null) {
            throw new DAOException("Failed to save null model!");
        }

        Session session = newSession();
        try {
            session.getTransaction().begin();
            session.saveOrUpdate(feature);
            session.getTransaction().commit();
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            throw new DAOException("dbSave failed!", e);
        } catch (Exception e1) {
            session.getTransaction().rollback();
            Logger.error(e1);
            throw new DAOException("Unknown database exception ", e1);
        } finally {
            closeSession(session);
        }

        return feature;
    }

    /**
     * Retrieve the {@link Sequence} object associated with the given {@link Entry} object.
     *
     * @param entry entry associated with sequence
     * @return Sequence object.
     * @throws DAOException
     */
    public Sequence getByEntry(Entry entry) throws DAOException {
        Sequence sequence = null;

        Session session = newSession();
        try {
            session.beginTransaction();
            String queryString = "from " + Sequence.class.getName()
                    + " as sequence where sequence.entry = :entry";

            Query query = session.createQuery(queryString);
            query.setEntity("entry", entry);
            Object queryResult = query.uniqueResult();

            if (queryResult != null) {
                sequence = (Sequence) queryResult;
            }
            session.getTransaction().commit();
        } catch (HibernateException e) {
            Logger.error(e);
            session.getTransaction().rollback();
            throw new DAOException("Failed to retrieve sequence by entry: " + entry.getId(), e);
        } finally {
            closeSession(session);
        }
        normalizeAnnotationLocations(sequence);
        return sequence;
    }

    public boolean hasSequence(Entry entry) throws DAOException {
        Session session = newSession();
        try {

            Number itemCount = (Number) session.createCriteria(Sequence.class)
                                               .setProjection(Projections.countDistinct("id"))
                                               .add(Restrictions.eq("entry", entry)).uniqueResult();

            return itemCount.intValue() > 0;
        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve sequence by entry: " + entry.getId(), e);
        } finally {
            closeSession(session);
        }
    }

    /**
     * Retrieve all {@link Sequence} objects in the database.
     *
     * @return ArrayList of Sequence objects.
     * @throws DAOException
     */
    @SuppressWarnings("unchecked")
    public ArrayList<Sequence> getAllSequences() throws DAOException {
        ArrayList<Sequence> sequences = null;

        Session session = newSession();
        try {
            Query query = session.createQuery("from " + Sequence.class.getName());
            @SuppressWarnings("rawtypes")
            List list = query.list();
            if (list != null) {
                sequences = (ArrayList<Sequence>) list;
            }
        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve entries!", e);
        } finally {
            closeSession(session);
        }

        return sequences;
    }

    /**
     * Retrieve the {@link Feature} object for the given Feature object. Since only one Feature
     * object should exist for a given unique sequence, this method is used to prevent creation of
     * duplicate features.
     * <p/>
     * Use this to look for a reference feature that should exist in the features table, such as
     * known biobrick prefix/suffix/scar sequence. This method creates the feature if it doesn't
     * exist, using the values passed.
     * <p/>
     *
     * @param feature
     * @return Feature object.
     * @throws DAOException
     */
    public Feature getReferenceFeature(Feature feature) throws DAOException {
        Feature oldFeature = getFeatureBySequence(feature.getSequence());
        if (oldFeature == null) {
            Feature newFeature = saveFeature(feature);
            return newFeature;
        } else {
            return oldFeature;
        }
    }

    /**
     * Retrieve all {@link Feature} objects in the database.
     *
     * @return ArrayList of Feature objects.
     * @throws DAOException
     */
    @SuppressWarnings("unchecked")
    public ArrayList<Feature> getAllFeatures() throws DAOException {
        ArrayList<Feature> features = null;
        Session session = newSession();
        try {
            Query query = session.createQuery("from " + Feature.class.getName());
            @SuppressWarnings("rawtypes")
            List list = query.list();

            if (list != null) {
                features = (ArrayList<Feature>) list;
            }
        } catch (HibernateException e) {
            throw new DAOException(e);
        } finally {
            closeSession(session);
        }

        return features;
    }

    /**
     * Retrieve the {@link Feature} object with the given DNA sequence string.
     *
     * @param featureDnaSequence
     * @return Feature object.
     * @throws DAOException
     */
    private Feature getFeatureBySequence(String featureDnaSequence) throws DAOException {
        featureDnaSequence = featureDnaSequence.toLowerCase();
        Feature result = null;
        Session session = newSession();

        try {
            String queryString = "from " + Feature.class.getName() + " where hash = :hash";
            Query query = session.createQuery(queryString);
            query.setParameter("hash", SequenceUtils.calculateSequenceHash(featureDnaSequence));

            Object queryResult = query.uniqueResult();

            if (queryResult != null) {
                result = (Feature) queryResult;
            } else {
                query.setParameter("hash", SequenceUtils.calculateSequenceHash(SequenceUtils
                                                                                       .reverseComplement(
                                                                                               featureDnaSequence)));

                queryResult = query.uniqueResult();

                if (queryResult != null) {
                    result = (Feature) queryResult;
                } else {
                    result = null;
                }
            }
        } catch (HibernateException e) {
            throw new DAOException("Failed to get Feature by sequence!", e);
        } catch (UtilityException e) {
            throw new DAOException("Failed to get Feature by sequence!", e);
        } finally {
            closeSession(session);
        }

        return result;
    }

    /**
     * Normalize {@link AnnotationLocation}s by fixing strangely defined annotationLocations.
     * <p/>
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
        boolean wholeSequence;
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
