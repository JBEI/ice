package org.jbei.ice.lib.entry.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.jbei.ice.lib.dto.entry.EntryType;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

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

    private static final long serialVersionUID = 1L;

    @Column(name = "host", length = 255)
    @Field(store = Store.YES)
    private String host;

    @Column(name = "genotype_phenotype", length = 255)
    @Field
    private String genotypePhenotype;

    @Column(name = "plasmids", length = 512)
    @Field
    private String plasmids;

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

    public String getPlasmids() {
        return plasmids;
    }

    public void setPlasmids(String plasmids) {
        this.plasmids = plasmids;
    }
}
