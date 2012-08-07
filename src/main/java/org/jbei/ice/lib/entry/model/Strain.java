package org.jbei.ice.lib.entry.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.jbei.ice.shared.dto.EntryType;

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
public class Strain extends Entry {

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
