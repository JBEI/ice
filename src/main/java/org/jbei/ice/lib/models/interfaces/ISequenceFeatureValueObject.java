package org.jbei.ice.lib.models.interfaces;

import org.jbei.ice.lib.models.Feature;
import org.jbei.ice.lib.models.Sequence;

public interface ISequenceFeatureValueObject {
    void setId(long id);

    long getId();

    ISequenceValueObject getSequence();

    void setSequence(Sequence sequence);

    Feature getFeature();

    void setFeature(Feature feature);

    int getStart();

    void setStart(int start);

    int getEnd();

    void setEnd(int end);

    int getStrand();

    void setStrand(int strand);

    String getName();

    void setName(String name);
}