package org.jbei.ice.lib.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.models.interfaces.IStrainValueObject;

@Entity
@PrimaryKeyJoinColumn(name = "entries_id")
@Table(name = "strains")
public class Strain extends Entry implements IStrainValueObject, IModel {

    private static final long serialVersionUID = 1L;

    @Column(name = "host", length = 255)
    private String host;

    @Column(name = "genotype_phenotype", length = 255)
    private String genotypePhenotype;

    @Column(name = "plasmids", length = 512)
    private String plasmids;

    public Strain() {
    }

    public Strain(String recordId, String versionId, String recordType, String owner,
            String ownerEmail, String creator, String creatorEmail, String status, String alias,
            String keywords, String shortDescription, String longDescription,
            String longDescriptionType, String references, Date creationTime,
            Date modificationTime, String host, String genotypePhenotype, String plasmids) {
        super(recordId, versionId, recordType, owner, ownerEmail, creator, creatorEmail, status,
                alias, keywords, shortDescription, longDescription, longDescriptionType,
                references, creationTime, modificationTime);
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
