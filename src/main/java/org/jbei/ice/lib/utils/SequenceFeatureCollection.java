package org.jbei.ice.lib.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jbei.ice.lib.models.Feature;
import org.jbei.ice.lib.models.SequenceFeature;

public class SequenceFeatureCollection implements Cloneable, Collection<SequenceFeature>,
        Serializable, Set<SequenceFeature> {

    private static final long serialVersionUID = 1L;
    private ArrayList<SequenceFeature> sequenceFeatures = new ArrayList<SequenceFeature>();
    private ArrayList<String> hashes = new ArrayList<String>();

    public SequenceFeatureCollection() {

    }

    public SequenceFeatureCollection(Collection<SequenceFeature> c) {
        addAll(c);
    }

    // unique methods
    public boolean exists(Feature feature) {
        boolean result = false;
        List<SequenceFeature> temp = get(feature);
        if (temp.size() > 0) {
            result = true;
        }
        return result;
    }

    public boolean exists(SequenceFeature.Flag flag) {
        boolean result = false;
        List<SequenceFeature> temp = get(flag);
        if (temp.size() > 0) {
            result = true;
        }
        return result;
    }

    public List<SequenceFeature> get(Feature feature) {
        ArrayList<SequenceFeature> result = new ArrayList<SequenceFeature>();
        for (int i = 0; i < hashes.size(); i++) {
            if (hashes.get(i).equals(feature.getHash())) {
                result.add(sequenceFeatures.get(i));
            }
        }
        return result;
    }

    public List<SequenceFeature> get(SequenceFeature.Flag flag) {
        ArrayList<SequenceFeature> result = new ArrayList<SequenceFeature>();
        for (SequenceFeature sequenceFeature : sequenceFeatures) {
            if (sequenceFeature.getFlag() == flag) {
                result.add(sequenceFeature);
            }
        }
        return result;
    }

    /**
     * @param 1 based position
     * @return
     */
    public List<SequenceFeature> getFeatursAt(int position) {
        ArrayList<SequenceFeature> result = new ArrayList<SequenceFeature>();
        for (SequenceFeature sequenceFeature : sequenceFeatures) {
            if (sequenceFeature.getStart() >= position && position <= sequenceFeature.getEnd()) {
                result.add(sequenceFeature);
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
