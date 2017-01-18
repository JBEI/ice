package org.jbei.ice.storage.hibernate.dao;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.utils.SequenceUtils;
import org.jbei.ice.lib.utils.UtilityException;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.*;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Manipulate {@link Sequence} and associated objects in the database.
 *
 * @author Hector Plahar
 */
public class SequenceDAO extends HibernateRepository<Sequence> {
    /**
     * Save the given {@link Sequence} object in the database.
     *
     * @param sequence sequence to save
     * @return Saved Sequence object
     * @throws IllegalArgumentException if the sequence object is null or does not have a valid entry associated with it
     * @throws DAOException
     */
    public Sequence saveSequence(Sequence sequence) {
        if (sequence == null || sequence.getEntry() == null)
            throw new IllegalArgumentException("Cannot save null sequence or sequence without entry");

        Set<SequenceFeature> sequenceFeatureSet = null;

        normalizeAnnotationLocations(sequence);
        if (sequence.getSequenceFeatures() != null) {
            sequenceFeatureSet = new HashSet<>(sequence.getSequenceFeatures());
            sequence.setSequenceFeatures(null);
        }

        // create sequence
        sequence = create(sequence);

        // separate out sequence features and uniquely create features
        if (sequenceFeatureSet != null) {
            for (SequenceFeature sequenceFeature : sequenceFeatureSet) {
                Feature feature = sequenceFeature.getFeature();

                if (feature == null) {
                    throw new DAOException("SequenceFeature has no feature");
                }

                Feature existingFeature = getFeatureBySequence(feature.getSequence());

                if (existingFeature == null) {
                    // new feature -> save it
                    FeatureDAO featureDAO = DAOFactory.getFeatureDAO();
                    existingFeature = featureDAO.create(feature);
                } else {
                    if (!sameFeatureUri(existingFeature, feature)) {
                        // same sequence feature but different uri
                        // sequence hash fwa uniqueness causes problems when trying to save a new feature with same seq
                        existingFeature.setUri(feature.getUri());
                    }
                }

                sequenceFeature.setFeature(existingFeature);
                sequenceFeature.setSequence(sequence);
                DAOFactory.getSequenceFeatureDAO().create(sequenceFeature);
            }
        }

        return sequence;
    }

    private boolean sameFeatureUri(Feature f1, Feature f2) {
        if (f1.getUri() == null && f2.getUri() == null)
            return true;

        if (f1.getIdentification() != null && !f1.getIdentification().equalsIgnoreCase(f2.getIdentification()))
            return false;

        if (f1.getUri() != null && !f1.getUri().equalsIgnoreCase(f2.getUri()))
            return false;

        return f2.getUri().equalsIgnoreCase(f1.getUri());
    }

    /**
     * Updates an existing feature with a new set of features
     *
     * @param sequence    existing feature to update
     * @param newFeatures optional new set of features that need updating. If this is null, the features associated with
     *                    the specified sequence is cleared
     * @return updated sequence
     * @throws IllegalArgumentException if sequence is null or does not have an associated entry
     */
    public Sequence updateSequence(Sequence sequence, Set<SequenceFeature> newFeatures) {
        if (sequence == null || sequence.getEntry() == null)
            throw new IllegalArgumentException("Cannot update null sequence or sequence without valid entry");

        // clear existing features
        if (sequence.getSequenceFeatures() != null) {
            for (SequenceFeature feature : sequence.getSequenceFeatures())
                currentSession().delete(feature);
        }

        sequence.setSequenceFeatures(null);
        sequence = update(sequence);

        // add new features
        if (newFeatures != null) {
            for (SequenceFeature sequenceFeature : newFeatures) {
                Feature newFeature = sequenceFeature.getFeature();
                Feature newFeatureExisting = getFeatureBySequence(newFeature.getSequence());
                if (newFeatureExisting == null) {
                    newFeatureExisting = DAOFactory.getFeatureDAO().create(newFeature);
                }
                sequenceFeature.setFeature(newFeatureExisting);
                sequenceFeature.setSequence(sequence);
                DAOFactory.getSequenceFeatureDAO().create(sequenceFeature);
            }
        }

        return sequence;
    }

    /**
     * Delete the given {@link Sequence} object in the database.
     *
     * @param sequence          sequence to delete
     * @param pigeonImageFolder path of the image folder where the pigeon images are cached
     */
    public void deleteSequence(Sequence sequence, String pigeonImageFolder) {
        String sequenceHash = sequence.getFwdHash();
        try {
            sequence.setEntry(null);
            sequence.getSequenceFeatures();
            super.delete(sequence);
            currentSession().flush();
            Files.deleteIfExists(Paths.get(pigeonImageFolder, sequenceHash + ".png"));
        } catch (IOException e) {
            Logger.warn(e.getMessage());
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    /**
     * Retrieve the {@link Sequence} object associated with the given {@link Entry} object.
     *
     * @param entry entry associated with sequence
     * @return Sequence object.
     */
    public Sequence getByEntry(Entry entry) {
        try {
            CriteriaQuery<Sequence> query = getBuilder().createQuery(Sequence.class);
            Root<Sequence> from = query.from(Sequence.class);
            query.where(getBuilder().equal(from.get("entry"), entry));
            Optional<Sequence> sequence = currentSession().createQuery(query).uniqueResultOptional();
            if (!sequence.isPresent())
                return null;
            return normalizeAnnotationLocations(sequence.get());
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException("Failed to retrieve sequence by entry: " + entry.getId(), e);
        }
    }

    public Optional<String> getSequenceString(Entry entry) {
        try {
            CriteriaQuery<String> query = getBuilder().createQuery(String.class);
            Root<Sequence> from = query.from(Sequence.class);
            query.select(from.get("sequence")).where(getBuilder().equal(from.get("entry"), entry));
            return currentSession().createQuery(query).uniqueResultOptional();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public boolean hasSequence(long entryId) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Sequence> from = query.from(Sequence.class);
            Join<Sequence, Entry> entry = from.join("entry");
            query.select(getBuilder().countDistinct(from.get("id"))).where(
                    getBuilder().equal(entry.get("id"), entryId)).distinct(true);
            return currentSession().createQuery(query).setMaxResults(1).uniqueResult() > 0;
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public String getSequenceFilename(Entry entry) {
        try {
            CriteriaQuery<String> query = getBuilder().createQuery(String.class);
            Root<Sequence> from = query.from(Sequence.class);
            query.select(from.get("fileName")).where(getBuilder().equal(from.get("entry"), entry));
            return currentSession().createQuery(query).getSingleResult();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    /**
     * Determines if the user uploaded a sequence file and associated it with an entry
     *
     * @param entryId unique identifier for entry
     * @return true if there is a sequence file that was originally uploaded by user, false otherwise
     * @throws DAOException
     */
    public boolean hasOriginalSequence(long entryId) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Sequence> from = query.from(Sequence.class);
            Join<Sequence, Entry> entry = from.join("entry");
            query.select(getBuilder().countDistinct(from.get("id"))).where(
                    getBuilder().equal(entry.get("id"), entryId),
                    getBuilder().notEqual(from.get("sequenceUser"), ""),
                    getBuilder().isNotNull(from.get("sequenceUser")));
            return currentSession().createQuery(query).uniqueResult() > 0;
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    /**
     * Enables retrieving sequences in the database without loading everything in memory
     * <p/>
     * Expected usage is
     * <code>
     * long count = getSequenceCount();
     * int offset = 0;
     * while( offset < count ) {
     * Sequence sequence = dao.getSequence(offset);
     * // do something with sequence
     * }
     * </code>
     *
     * @return Sequence at the specified offset
     * @throws DAOException
     */
    public Sequence getSequence(int offset) {
        try {
            CriteriaQuery<Sequence> query = getBuilder().createQuery(Sequence.class);
            Root<Sequence> from = query.from(Sequence.class);
            Join<Sequence, Entry> entry = from.join("entry");
            query.where(getBuilder().equal(entry.get("visibility"), Visibility.OK.getValue()));
            return currentSession().createQuery(query).setFirstResult(offset).setMaxResults(1).uniqueResult();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    /**
     * @return number of sequences available for all valid (visibility=9) entry object
     */
    public int getSequenceCount() {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Sequence> from = query.from(Sequence.class);
            Join<Sequence, Entry> entry = from.join("entry");
            query.select(getBuilder().countDistinct(from.get("id")));
            query.where(getBuilder().equal(entry.get("visibility"), Visibility.OK.getValue()));
            return currentSession().createQuery(query).uniqueResult().intValue();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    /**
     * Retrieve the {@link Feature} object with the given DNA sequence string.
     *
     * @param featureDnaSequence dna sequence of feature
     * @return Feature object.
     * @throws DAOException
     */
    private Feature getFeatureBySequence(String featureDnaSequence) {
        featureDnaSequence = featureDnaSequence.toLowerCase();

        try {
            String hash = SequenceUtils.calculateSequenceHash(featureDnaSequence);
            CriteriaQuery<Feature> query = getBuilder().createQuery(Feature.class);
            Root<Feature> from = query.from(Feature.class);
            query.where(getBuilder().equal(from.get("hash"), hash));

            Optional<Feature> result = currentSession().createQuery(query).uniqueResultOptional();
            if (result.isPresent())
                return result.get();

            String reverseComplement = SequenceUtils.reverseComplement(featureDnaSequence);
            String sequenceHash = SequenceUtils.calculateSequenceHash(reverseComplement);
            query.getRestriction().getExpressions().clear();
            query.where(getBuilder().equal(from.get("hash"), sequenceHash));
            return currentSession().createQuery(query).uniqueResult();
        } catch (HibernateException | UtilityException e) {
            Logger.error(e);
            throw new DAOException("Failed to get Feature by sequence!", e);
        }
    }

    /**
     * Normalize {@link AnnotationLocation}s by fixing strangely defined annotationLocations.
     * <p/>
     * Fix locations that encompass the entire sequence, but defined strangely. This causes problems
     * elsewhere.
     */
    private static Sequence normalizeAnnotationLocations(Sequence sequence) {
        if (sequence == null) {
            return null;
        }

        if (sequence.getSequenceFeatures() == null) {
            return sequence;
        }

        if (StringUtils.isEmpty(sequence.getSequence()))
            return sequence;

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
                sequenceFeature.getAnnotationLocations().add(new AnnotationLocation(1, length, sequenceFeature));
            }
        }
        return sequence;
    }

    @Override
    public Sequence get(long id) {
        return super.get(Sequence.class, id);
    }
}
