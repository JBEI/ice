package org.jbei.ice.storage.model;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.ProteinData;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * Store Protein specific fields.
 **/

@Entity
@Indexed
@PrimaryKeyJoinColumn(name = "entries_id")
@Table(name = "proteins")
public class Protein extends Entry {

    @Column(name = "organism")
    @Field
    private String organism;

    @Column(name = "fullName")
    @Field
    private String fullName;

    @Column(name = "geneName")
    @Field
    private String geneName;

    @Column(name = "uploadedFrom")
    @Field
    private String uploadedFrom;

    public Protein() {
        super();
        setRecordType(EntryType.PROTEIN.getName());
    }

    public String getOrganism() {
        return organism;
    }

    public void setOrganism(String organism) {
        this.organism = organism;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getGeneName() {
        return geneName;
    }

    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }

    public String getUploadedFrom() {
        return uploadedFrom;
    }

    public void setUploadedFrom(String uploadedFrom) {
        this.uploadedFrom = uploadedFrom;
    }

    @Override
    public PartData toDataTransferObject() {
        PartData data = super.toDataTransferObject();
        ProteinData proteinData = new ProteinData();
        proteinData.setOrganism(this.organism);
        proteinData.setFullName(this.fullName);
        proteinData.setGeneName(this.geneName);
        proteinData.setUploadedFrom(this.uploadedFrom);
        data.setProteinData(proteinData);
        return data;
    }
}
