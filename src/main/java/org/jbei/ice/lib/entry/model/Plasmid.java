package org.jbei.ice.lib.entry.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.jbei.ice.shared.dto.EntryType;

/**
 * Store Plasmid specific fields.
 * <p/>
 * <ul>
 * <li><b>backbone: </b>Parent backbone of the plasmid, comma separated.</li>
 * <li><b>originOfReplication: </b>The origin of replication for this plasmid, comma separated.</li>
 * <li><b>promoters: </b>Promoters that are on this plasmid, comma separated.</li>
 * <li><b>circular: </b>True if plasmid is circular.</li>
 * </ul>
 *
 * @author Timothy Ham, Zinovii Dmytriv
 */
@Entity
@PrimaryKeyJoinColumn(name = "entries_id")
@Table(name = "plasmids")
public class Plasmid extends Entry {

    private static final long serialVersionUID = 1L;

    @Column(name = "backbone", length = 127)
    private String backbone;

    @Column(name = "origin_of_replication", length = 127)
    private String originOfReplication;

    @Column(name = "promoters", length = 512)
    private String promoters;

    @Column(name = "circular")
    private Boolean circular;

    public Plasmid() {
        setRecordType(EntryType.PLASMID.getName());
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
