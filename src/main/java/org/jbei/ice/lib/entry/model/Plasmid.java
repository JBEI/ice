package org.jbei.ice.lib.entry.model;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.models.interfaces.IPlasmidValueObject;
import org.jbei.ice.shared.dto.EntryType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import java.util.Date;

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
        setRecordType(EntryType.PLASMID.getName());
    }

    public Plasmid(String recordId, String versionId, String recordType, String owner,
            String ownerEmail, String creator, String creatorEmail, String status, String alias,
            String keywords, String shortDescription, String longDescription,
            String longDescriptionType, String references, Date creationTime,
            Date modificationTime, String backbone, String originOfReplication, String promoters,
            boolean circular) {
        super(recordId, versionId, recordType, owner, ownerEmail, creator, creatorEmail, status,
              alias, keywords, shortDescription, longDescription, longDescriptionType,
              references, creationTime, modificationTime);
        this.backbone = backbone;
        this.originOfReplication = originOfReplication;
        this.promoters = promoters;
        this.circular = circular;
    }

    @Override
    public String getBackbone() {
        return backbone;
    }

    @Override
    public void setBackbone(String backbone) {
        this.backbone = backbone;
    }

    @Override
    public String getOriginOfReplication() {
        return originOfReplication;
    }

    @Override
    public void setOriginOfReplication(String originOfReplication) {
        this.originOfReplication = originOfReplication;
    }

    @Override
    public String getPromoters() {
        return promoters;
    }

    @Override
    public void setPromoters(String promoters) {
        this.promoters = promoters;
    }

    @Override
    public boolean getCircular() {
        return circular;
    }

    @Override
    public void setCircular(boolean circular) {
        this.circular = circular;
    }
}
