package org.jbei.ice.storage.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.jbei.ice.dto.entry.EntryType;
import org.jbei.ice.dto.entry.PartData;
import org.jbei.ice.dto.entry.ProteinData;

/**
 * Store Protein specific fields.
 **/

@Entity
@Indexed
@PrimaryKeyJoinColumn(name = "entries_id")
@Table(name = "proteins")
public class Protein extends Entry {

    @Column(name = "organism")
    @GenericField
    private String organism;

    @Column(name = "fullName")
    @GenericField
    private String fullName;

    @Column(name = "geneName")
    @GenericField
    private String geneName;

    @Column(name = "uploadedFrom")
    @GenericField
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
