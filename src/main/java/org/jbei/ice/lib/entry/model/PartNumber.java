package org.jbei.ice.lib.entry.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;

import org.jbei.ice.lib.dao.IModel;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Boost;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Store;

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
public class PartNumber implements IModel {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @Column(name = "part_number", length = 127, nullable = false)
    @Field(boost = @Boost(2f), store = Store.YES, analyze = Analyze.NO)
    private String partNumber;

    @ContainedIn
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entries_id", nullable = false)
    private Entry entry;

    public PartNumber() {
    }

    public PartNumber(String partNumber, Entry entry) {
        this.partNumber = partNumber;
        this.entry = entry;
    }

    @XmlTransient
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    @XmlTransient
    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    @Override
    public String toString() {
        return this.partNumber;
    }
}
