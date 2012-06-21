package org.jbei.ice.lib.entry.model;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.models.interfaces.IPartNumberValueObject;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Stores the part number of an entry.
 * <p/>
 * There can be multiple part numbers for one entry, and part numbers can come from outside gd-ice
 * instances.
 *
 * @author Timothy Ham, Ziovii Dmytriv
 */
@Entity
@Table(name = "part_numbers")
@SequenceGenerator(name = "sequence", sequenceName = "part_numbers_id_seq", allocationSize = 1)
public class PartNumber implements IPartNumberValueObject, IModel {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private long id;

    @Column(name = "part_number", length = 127, nullable = false)
    private String partNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entries_id", nullable = false)
    private Entry entry;

    public PartNumber() {
    }

    public PartNumber(String partNumber, Entry entry) {
        this.partNumber = partNumber;
        this.entry = entry;
    }

    @Override
    @XmlTransient
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String getPartNumber() {
        return partNumber;
    }

    @Override
    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    @Override
    @XmlTransient
    public Entry getEntry() {
        return entry;
    }

    @Override
    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    @Override
    public String toString() {
        return this.partNumber;
    }
}
