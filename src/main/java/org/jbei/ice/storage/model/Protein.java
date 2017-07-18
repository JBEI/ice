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

    @Column(name = "dummy", nullable = false)
    @Field
    private String dummy;

    public Protein() {
        super();
        setRecordType(EntryType.PROTEIN.getName());
    }

    public String getDummy() {
        return dummy;
    }

    public void setDummy(String dummy) {
        this.dummy = dummy;
    }

    @Override
    public PartData toDataTransferObject() {
        PartData data = super.toDataTransferObject();
        ProteinData proteinData = new ProteinData();
        proteinData.setDummy(this.dummy);
        data.setProteinData(proteinData);
        return data;
    }
}
