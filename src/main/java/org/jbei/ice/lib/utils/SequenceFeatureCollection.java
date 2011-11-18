package org.jbei.ice.lib.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jbei.ice.lib.models.AnnotationLocation;
import org.jbei.ice.lib.models.Feature;
import org.jbei.ice.lib.models.SequenceFeature;

/**
 * Container class for {@link SequenceFeature} objects.
 * <p>
 * Implement the Collection class and add some useful SequenceFeature specific convenience methods.
 * 
 * @author Timothy Ham, Zinovii Dmytriv
 * 
 */
public class SequenceFeatureCollection implements Cloneable, Collection<SequenceFeature>,
        Serializable, Set<SequenceFeature> {

    private static final long serialVersionUID = 1L;
    private final ArrayList<SequenceFeature> sequenceFeatures = new ArrayList<SequenceFeature>();
    private final ArrayList<String> hashes = new ArrayList<String>();

    /**
     * Default constructor.
     */
    public SequenceFeatureCollection() {

    }

    /**
     * Constructor that takes a Collection of {@link SequenceFeature}s.
     * 
     * @param c
     *            Collection of SequenceFeatures.
     */
    public SequenceFeatureCollection(Collection<SequenceFeature> c) {
        if (c == null) {
            return;
        }

        addAll(c);
    }

    // unique methods
    /**
     * Determine if the given {@link Feature} exists.
     * 
     * @param feature
     *            Feature to find.
     * @return True if a feature with the same sequence as the given feature exists.
     */
    public boolean exists(Feature feature) {
        boolean result = false;
        List<SequenceFeature> temp = get(feature);
        if (temp.size() > 0) {
            result = true;
        }
        return result;
    }

    /**
     * Determine if the given {@link org.jbei.ice.lib.models.SequenceFeature.AnnotationType
     * SequenceFeature.AnnotationType} exists.
     * 
     * @param flag
     *            AnnotationType to find.
     * @return True if a feature with the given AnnotationType exists.
     */
    public boolean exists(SequenceFeature.AnnotationType flag) {
        boolean result = false;
        List<SequenceFeature> temp = get(flag);
        if (temp.size() > 0) {
            result = true;
        }
        return result;
    }

    /**
     * Retrieve a list of {@link SequenceFeature}s with the identical hash as the given feature.
     * 
     * @param feature
     *            Feature to find.
     * @return List of {@link SequenceFeature}s.
     */
    public List<SequenceFeature> get(Feature feature) {
        ArrayList<SequenceFeature> result = new ArrayList<SequenceFeature>();
        for (int i = 0; i < hashes.size(); i++) {
            if (hashes.get(i).equals(feature.getHash())) {
                result.add(sequenceFeatures.get(i));
            }
        }
        return result;
    }

    /**
     * Retrieve a list of {@link SequenceFeature}s with the given
     * {@link org.jbei.ice.lib.models.SequenceFeature.AnnotationType SequenceFeature.AnnotationType}
     * .
     * 
     * @param flag
     *            AnnotationType to search for.
     * @return List of SequenceFeatures.
     */
    public List<SequenceFeature> get(SequenceFeature.AnnotationType flag) {
        ArrayList<SequenceFeature> result = new ArrayList<SequenceFeature>();
        for (SequenceFeature sequenceFeature : sequenceFeatures) {
            if (sequenceFeature.getAnnotationType() == flag) {
                result.add(sequenceFeature);
            }
        }
        return result;
    }

    /**
     * Retrieve a list of {@link SequenceFeature}s with the given sequence string.
     * 
     * @param sequenceString
     *            sequence to search for.
     * @return List of SequenceFeatures.
     */
    public List<SequenceFeature> getBySequence(String sequenceString) {
        ArrayList<SequenceFeature> result = new ArrayList<SequenceFeature>();
        for (SequenceFeature sequenceFeature : sequenceFeatures) {
            String featureSequence = sequenceFeature.getFeature().getSequence();
            if (featureSequence.equals(sequenceString)) {
                result.add(sequenceFeature);
            }
        }
        return result;
    }

    /**
     * Retrieve the {@link SequenceFeature}s that contain the given sequence position.
     * 
     * @param genbankPosition
     *            1 based position
     * @return List of SequeceFeatures.
     */
    public List<SequenceFeature> getFeaturesAt(int genbankPosition) {
        ArrayList<SequenceFeature> result = new ArrayList<SequenceFeature>();
        for (SequenceFeature sequenceFeature : sequenceFeatures) {
            for (AnnotationLocation location : sequenceFeature.getAnnotationLocations()) {
                if (location.getGenbankStart() >= genbankPosition
                        && genbankPosition <= location.getEnd()) {
                    if (!result.contains(sequenceFeature)) {
                        result.add(sequenceFeature);
                    }
                }
            }

        }
        return result;
    }

    // Overridden methods
    @Override
    public void clear() {
        sequenceFeatures.clear();
        hashes.clear();
    }

    @Override
    public boolean contains(Object o) {
        boolean result = false;
        if (o instanceof SequenceFeature) {
            SequenceFeature temp = (SequenceFeature) o;

            // if object itself is specified, do normal contains
            if (sequenceFeatures.contains(o)) {
                result = true;
            } else {
                result = hashes.contains(temp.getFeature().getHash());
            }
        }
        return result;
    }

    @Override
    public boolean isEmpty() {
        return sequenceFeatures.isEmpty();
    }

    @Override
    public Iterator<SequenceFeature> iterator() {
        return sequenceFeatures.iterator();
    }

    @Override
    public boolean remove(Object o) {
        boolean result = false;
        if (o instanceof SequenceFeature) {

            SequenceFeature temp = (SequenceFeature) o;

            // if object itself is specified, remove
            if (sequenceFeatures.contains(o)) {
                hashes.remove(temp.getFeature().getHash());
                sequenceFeatures.remove(o);
                result = true;
            } else { // otherwise remove first object with same hash
                if (hashes.contains(temp.getFeature().getHash())) {
                    int index = hashes.indexOf(temp.getFeature().getHash());
                    hashes.remove(index);
                    sequenceFeatures.remove(index);
                    result = true;
                }
            }
        }
        return result;
    }

    @Override
    public int size() {
        return sequenceFeatures.size();
    }

    @Override
    public Object[] toArray() {
        return sequenceFeatures.toArray();
    }

    @Override
    public boolean add(SequenceFeature e) {
        hashes.add(e.getFeature().getHash());
        return sequenceFeatures.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends SequenceFeature> c) {
        boolean result = false;
        long counter = 0;
        for (SequenceFeature item : c) {
            hashes.add(item.getFeature().getHash());
            sequenceFeatures.add(item);
            counter = counter + 1;
        }
        if (counter == c.size()) {
            result = true;
        }

        return result;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return sequenceFeatures.containsAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return sequenceFeatures.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return sequenceFeatures.retainAll(c);
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return sequenceFeatures.toArray(a);
    }

}
