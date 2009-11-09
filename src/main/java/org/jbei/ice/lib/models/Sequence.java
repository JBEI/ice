package org.jbei.ice.lib.models;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.jbei.ice.lib.value_objects.SequenceValueObject;

public class Sequence implements SequenceValueObject, Serializable {
	private int id;
	private String sequence;
	private String sequenceUser;
	private String fwdHash;
	private String revHash;
	private Entry entry;

	private Set<SequenceFeature> sequenceFeatures = new HashSet<SequenceFeature> ();
	
	public Sequence() {
	}

	public Sequence(String sequence, String sequenceUser, String fwdHash,
			String revHash, Entry entry) {
		super();
		this.sequence = sequence;
		this.sequenceUser = sequenceUser;
		this.fwdHash = fwdHash;
		this.revHash = revHash;
		this.entry = entry;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public String getSequenceUser() {
		return sequenceUser;
	}

	public void setSequenceUser(String sequenceUser) {
		this.sequenceUser = sequenceUser;
	}

	public String getFwdHash() {
		return fwdHash;
	}

	public void setFwdHash(String fwdHash) {
		this.fwdHash = fwdHash;
	}

	public String getRevHash() {
		return revHash;
	}

	public void setRevHash(String revHash) {
		this.revHash = revHash;
	}

	public void setEntry(Entry entry) {
		this.entry = entry;
	}

	public Entry getEntry() {
		return entry;
	}

	public void setSequenceFeatures(Set<SequenceFeature> sequenceFeatures) {
		this.sequenceFeatures = sequenceFeatures;
	}

	public Set<SequenceFeature> getSequenceFeatures() {
		return sequenceFeatures;
	}

}
