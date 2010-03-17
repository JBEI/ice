package org.jbei.ice.lib.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.vo.IPlasmidValueObject;

@Entity
@PrimaryKeyJoinColumn(name = "entries_id")
@Table(name = "plasmids")
public class Plasmid extends Entry implements IPlasmidValueObject, IModel {

    private static final long serialVersionUID = 1L;

    @Column(name = "backbone", length = 127)
    private String backbone;

    @Column(name = "origin_of_replication", length = 127)
    private String originOfReplication;

    @Column(name = "promoters", length = 512)
    private String promoters;

    @Column(name = "circular")
    private boolean circular;

    public Plasmid() {
    }

    public Plasmid(String recordId, String versionId, String recordType, String owner,
            String ownerEmail, String creator, String creatorEmail, String status, String alias,
            String keywords, String shortDescription, String longDescription, String references,
            Date creationTime, Date modificationTime, String backbone, String originOfReplication,
            String promoters, boolean circular) {
        super(recordId, versionId, recordType, owner, ownerEmail, creator, creatorEmail, status,
                alias, keywords, shortDescription, longDescription, references, creationTime,
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
