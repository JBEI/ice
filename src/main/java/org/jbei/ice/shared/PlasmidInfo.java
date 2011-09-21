package org.jbei.ice.shared;

import org.jbei.ice.shared.dto.EntryInfo;


public class PlasmidInfo extends EntryInfo {

    private static final long serialVersionUID = 1L;

    private String backbone;
    private String originOfReplication;
    private String promoters;
    private boolean circular;

    public PlasmidInfo() {
    }

    //    public Plasmid(String recordId, String versionId, String recordType, String owner,
    //            String ownerEmail, String creator, String creatorEmail, String status, String alias,
    //            String keywords, String shortDescription, String longDescription,
    //            String longDescriptionType, String references, Date creationTime,
    //            Date modificationTime, String backbone, String originOfReplication, String promoters,
    //            boolean circular) {
    //        super(recordId, versionId, recordType, owner, ownerEmail, creator, creatorEmail, status,
    //                alias, keywords, shortDescription, longDescription, longDescriptionType,
    //                references, creationTime, modificationTime);
    //        this.backbone = backbone;
    //        this.originOfReplication = originOfReplication;
    //        this.promoters = promoters;
    //        this.circular = circular;
    //    }

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
