package org.jbei.ice.storage.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.jbei.ice.dto.entry.EntryType;
import org.jbei.ice.dto.entry.PartData;
import org.jbei.ice.dto.entry.StrainData;

/**
 * Store Strain specific fields.
 * <p/>
 * <ul>
 * <li><b>host: </b>The strain host name, e.g. DH10B.</li>
 * <li><b>genotypePhenotype: </b>Detail genotype or phenotype information.</li>
 * <li><b>plasmids: </b>Plasmids harbored by this strain.</li>
 * </ul>
 *
 * @author Timothy Ham, Ziovii Dmytriv, Hector Plahar
 */
@Entity
@Indexed
@PrimaryKeyJoinColumn(name = "entries_id")
@Table(name = "strains")
public class Strain extends Entry {

    @Column(name = "host")
    @GenericField
    private String host;

    @Column(name = "genotype_phenotype")
    @GenericField
    private String genotypePhenotype;

    public Strain() {
        super();
        setRecordType(EntryType.STRAIN.getName());
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

    @Override
    public PartData toDataTransferObject() {
        PartData data = super.toDataTransferObject();
        StrainData strainData = new StrainData();
        strainData.setGenotypePhenotype(genotypePhenotype);
        strainData.setHost(host);
        data.setStrainData(strainData);
        return data;
    }
}
