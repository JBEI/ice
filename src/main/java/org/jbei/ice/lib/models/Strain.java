package org.jbei.ice.lib.models;

import java.io.Serializable;
import java.util.Date;

import org.jbei.ice.lib.value_objects.IStrainValueObject;

public class Strain extends Entry implements IStrainValueObject, Serializable {
	private String host;
	private String genotypePhenotype;
	private String plasmids;

	public Strain() {
	}

	public Strain(String recordId, String versionId, String recordType,
			String owner, String ownerEmail, String creator,
			String creatorEmail, int visibility, String status, String alias,
			String keywords, String shortDescription, String longDescription,
			String references, Date creationTime, Date modificationTime,
			String host, String genotypePhenotype, String plasmids) {
		super(recordId, versionId, recordType, owner, ownerEmail, creator,
				creatorEmail, visibility, status, alias, keywords,
				shortDescription, longDescription, references, creationTime,
				modificationTime);
		this.host = host;
		this.genotypePhenotype = genotypePhenotype;
		this.plasmids = plasmids;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getGenotypePhenotype() {
		return genotypePhenotype;
	}

	public void setGenotypePhenotype(String genotypePhenotype) {
		this.genotypePhenotype = genotypePhenotype;
	}

	public String getPlasmids() {
		return plasmids;
	}

	public void setPlasmids(String plasmids) {
		this.plasmids = plasmids;
	}

}
