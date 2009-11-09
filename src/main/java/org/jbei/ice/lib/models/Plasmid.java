package org.jbei.ice.lib.models;

import java.io.Serializable;
import java.util.Date;

import org.jbei.ice.lib.value_objects.PlasmidValueObject;

public class Plasmid extends Entry implements PlasmidValueObject, Serializable {
	private String backbone;
	private String originOfReplication;
	private String promoters;
	private boolean circular;

	public Plasmid() {
	}

	public Plasmid(String recordId, String versionId, String recordType,
			String owner, String ownerEmail, String creator,
			String creatorEmail, int visibility, String status, String alias,
			String keywords, String shortDescription, String longDescription,
			String references, Date creationTime, Date modificationTime,
			String backbone, String originOfReplication, String promoters,
			boolean circular) {
		super(recordId, versionId, recordType, owner, ownerEmail, creator,
				creatorEmail, visibility, status, alias, keywords,
				shortDescription, longDescription, references, creationTime,
				modificationTime);
		this.backbone = backbone;
		this.originOfReplication = originOfReplication;
		this.promoters = promoters;
		this.circular = circular;
	}

	public String getBackbone() {
		return backbone;
	}

	public void setBackbone(String backbone) {
		this.backbone = backbone;
	}

	public String getOriginOfReplication() {
		return originOfReplication;
	}

	public void setOriginOfReplication(String originOfReplication) {
		this.originOfReplication = originOfReplication;
	}

	public String getPromoters() {
		return promoters;
	}

	public void setPromoters(String promoters) {
		this.promoters = promoters;
	}

	public boolean getCircular() {
		return circular;
	}

	public void setCircular(boolean circular) {
		this.circular = circular;
	}
}
