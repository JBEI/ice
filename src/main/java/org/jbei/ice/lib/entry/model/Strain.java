package org.jbei.ice.lib.entry.model;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.models.interfaces.IStrainValueObject;
import org.jbei.ice.shared.dto.EntryType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import java.util.Date;

/**
 * Store Strain specific fields.
 * <p/>
 * <ul>
 * <li><b>host: </b>The strain host name, e.g. DH10B.</li>
 * <li><b>genotypePhenotype: </b>Detail genotype or phenotype information.</li>
 * <li><b>plasmids: </b>Plasmids harbored by this strain.</li>
 * </ul>
 *
 * @author Timothy Ham, Ziovii Dmytriv
 */
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
        setRecordType(EntryType.STRAIN.getName());
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

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public String getGenotypePhenotype() {
        return genotypePhenotype;
    }

    @Override
    public void setGenotypePhenotype(String genotypePhenotype) {
        this.genotypePhenotype = genotypePhenotype;
    }

    @Override
    public String getPlasmids() {
        return plasmids;
    }

    @Override
    public void setPlasmids(String plasmids) {
        this.plasmids = plasmids;
    }

}
