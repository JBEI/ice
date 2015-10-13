package org.jbei.ice.storage.model;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.PlasmidData;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * Store Plasmid specific fields.
 * <p/>
 * <ul>
 * <li><b>backbone: </b>Parent backbone of the plasmid, comma separated.</li>
 * <li><b>originOfReplication: </b>The origin of replication for this plasmid, comma separated.</li>
 * <li><b>promoters: </b>Promoters that are on this plasmid, comma separated.</li>
 * <li><b>circular: </b>True if plasmid is circular.</li>
 * <li><b>replicatesIn: </b></li>
 * </ul>
 *
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 */
@Entity
@Indexed
@PrimaryKeyJoinColumn(name = "entries_id")
@Table(name = "plasmids")
public class Plasmid extends Entry {

    @Column(name = "backbone", length = 127)
    @Field
    private String backbone;

    @Column(name = "origin_of_replication", length = 127)
    @Field
    private String originOfReplication;

    @Column(name = "promoters", length = 512)
    @Field
    private String promoters;

    @Column(name = "circular")
    private Boolean circular;

    @Column(name = "replicates_in")
    private String replicatesIn;

    public Plasmid() {
        super();
        setRecordType(EntryType.PLASMID.getName());
        setCircular(Boolean.TRUE);
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

    public Boolean getCircular() {
        return circular;
    }

    public void setCircular(Boolean circular) {
        if (circular == null)
            circular = Boolean.TRUE;
        this.circular = circular;
    }

    public String getReplicatesIn() {
        return replicatesIn;
    }

    public void setReplicatesIn(String replicatesIn) {
        this.replicatesIn = replicatesIn;
    }

    @Override
    public PartData toDataTransferObject() {
        PartData data = super.toDataTransferObject();
        PlasmidData plasmidData = new PlasmidData();
        plasmidData.setBackbone(this.backbone);
        plasmidData.setCircular(this.circular);
        plasmidData.setOriginOfReplication(this.originOfReplication);
        plasmidData.setPromoters(this.promoters);
        plasmidData.setReplicatesIn(this.replicatesIn);
        data.setPlasmidData(plasmidData);
        return data;
    }
}
