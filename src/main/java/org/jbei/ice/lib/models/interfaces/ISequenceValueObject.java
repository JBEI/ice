package org.jbei.ice.lib.models.interfaces;

import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.models.SequenceFeature;

import java.util.Set;

public interface ISequenceValueObject {
    long getId();

    void setId(long id);

    String getSequence();

    void setSequence(String sequence);

    String getSequenceUser();

    void setSequenceUser(String sequenceUser);

    String getFwdHash();

    void setFwdHash(String fwdHash);

    String getRevHash();

    void setRevHash(String revHash);

    void setEntry(Entry entry);

    Entry getEntry();

    void setSequenceFeatures(Set<SequenceFeature> sequenceFeatures);

    Set<SequenceFeature> getSequenceFeatures();
}