package org.jbei.ice.lib.experiment;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.persistence.*;

import org.jbei.ice.lib.dao.IDataModel;
import org.jbei.ice.lib.entry.model.Entry;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

/**
 * Storage entity for experimental data
 *
 * @author Hector Plahar
 */
@Entity
@Table(name = "experiment")
@SequenceGenerator(name = "sequence", sequenceName = "experiment_id_seq", allocationSize = 1)

public class Experiment implements IDataModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @Column(name = "url", length = 512)
    private String url;

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

    public Study toDataTransferObject() {
        Study study = new Study();
        study.setId(id);
        study.setCreated(creationTime.getTime());
        study.setUrl(url);
        return study;
    }
}
