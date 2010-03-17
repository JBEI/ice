package org.jbei.ice.lib.vo;

import org.jbei.ice.lib.models.Feature;
import org.jbei.ice.lib.models.Sequence;

public interface ISequenceFeatureValueObject {

    public abstract void setId(int id);

    public abstract int getId();

    public abstract ISequenceValueObject getSequence();

    public abstract void setSequence(Sequence sequence);

    public abstract Feature getFeature();

    public abstract void setFeature(Feature feature);

    public abstract int getStart();

    public abstract void setStart(int start);

    public abstract int getEnd();

    public abstract void setEnd(int end);

    public abstract int getStrand();

    public abstract void setStrand(int strand);

    public abstract String getName();

    public abstract void setName(String name);

}