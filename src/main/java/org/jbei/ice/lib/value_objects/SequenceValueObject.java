package org.jbei.ice.lib.value_objects;

import java.util.Set;

import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.SequenceFeature;

public interface SequenceValueObject {

	public abstract int getId();

	public abstract void setId(int id);

	public abstract String getSequence();

	public abstract void setSequence(String sequence);

	public abstract String getSequenceUser();

	public abstract void setSequenceUser(String sequenceUser);

	public abstract String getFwdHash();

	public abstract void setFwdHash(String fwdHash);

	public abstract String getRevHash();

	public abstract void setRevHash(String revHash);

	public abstract void setEntry(Entry entry);

	public abstract Entry getEntry();

	public abstract void setSequenceFeatures(
			Set<SequenceFeature> sequenceFeatures);

	public abstract Set<SequenceFeature> getSequenceFeatures();

}