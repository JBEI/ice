package org.jbei.ice.storage.model;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.jbei.ice.lib.experiment.Study;
import org.jbei.ice.storage.DataModel;

import javax.persistence.*;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Storage entity for experimental data
 *
 * @author Hector Plahar
 */
@Entity
@Table(name = "experiment")
@SequenceGenerator(name = "sequence", sequenceName = "experiment_id_seq", allocationSize = 1)

public class Experiment implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @Column(name = "label", length = 128)
    private String label;

    @Column(name = "url", length = 512)
    private String url;

    @Column(name = "owner_email", length = 512)
    private String ownerEmail;

    @Column(name = "creation_time")
    private Date creationTime;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "experiment_entry", joinColumns = {@JoinColumn(name = "experiment_id", nullable = false)},
               inverseJoinColumns = {@JoinColumn(name = "entry_id", nullable = false)})
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Entry> subjects = new LinkedHashSet<>();

    public long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Set<Entry> getSubjects() {
        return subjects;
    }

    public void setSubjects(Set<Entry> subjects) {
        this.subjects = subjects;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public Study toDataTransferObject() {
        Study study = new Study();
        study.setId(id);
        study.setCreated(creationTime.getTime());
        study.setLabel(label);
        study.setOwnerEmail(this.ownerEmail);
        study.setUrl(url);
        return study;
    }
}
