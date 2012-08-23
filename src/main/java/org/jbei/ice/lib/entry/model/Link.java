package org.jbei.ice.lib.entry.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import org.jbei.ice.lib.dao.IModel;

/**
 * Store url link information.
 *
 * @author Timothy Ham, Zinovii Dmytriv
 */
@Entity
@Indexed
@Table(name = "links")
@SequenceGenerator(name = "sequence", sequenceName = "links_id_seq", allocationSize = 1)
public class Link implements IModel {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private long id;

    @Column(name = "link", length = 1023)
    @Field
    private String link;

    @Column(name = "url", length = 1023)
    private String url;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entries_id", nullable = false)
    private Entry entry;

    public Link() {
    }

    @XmlTransient
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @XmlTransient
    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }
}
