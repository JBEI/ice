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

import org.jbei.ice.lib.value_objects.INameValueObject;

@Entity
@Table(name = "names")
@SequenceGenerator(name = "sequence", sequenceName = "names_id_seq", allocationSize = 1)
public class Name implements INameValueObject, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private int id;

    @Column(name = "name", length = 127, nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "entries_id", nullable = false, unique = false)
    private Entry entry;

    public Name() {
    }

    public Name(String name, Entry entry) {
        this.name = name;
        this.entry = entry;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }
}
