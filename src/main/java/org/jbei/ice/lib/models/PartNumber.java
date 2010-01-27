package org.jbei.ice.lib.models;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.jbei.ice.lib.value_objects.IPartNumberValueObject;

@Entity
@Table(name = "part_numbers")
@SequenceGenerator(name = "sequence", sequenceName = "part_numbers_id_seq", allocationSize = 1)
public class PartNumber implements IPartNumberValueObject, Serializable {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private int id;

    @Column(name = "part_number", length = 127, nullable = false)
    private String partNumber;

    @ManyToOne
    @JoinColumn(name = "entries_id", nullable = false)
    private Entry entry;

    public PartNumber() {
    }

    public PartNumber(String partNumber, Entry entry) {
        this.partNumber = partNumber;
        this.entry = entry;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }
}
