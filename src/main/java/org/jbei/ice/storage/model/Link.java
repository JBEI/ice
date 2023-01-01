package org.jbei.ice.storage.model;

import jakarta.persistence.*;
import org.jbei.ice.storage.DataModel;
import org.jbei.ice.storage.IDataTransferModel;

/**
 * Store url link information.
 *
 * @author Timothy Ham, Zinovii Dmytriv
 */
@Entity
@Table(name = "links")
@SequenceGenerator(name = "links_id", sequenceName = "links_id_seq", allocationSize = 1)
public class Link implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "links_id")
    private long id;

    @Column(name = "link", length = 1023)
    private String link;

    @Column(name = "url", length = 1023)
    private String url;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entries_id", nullable = false)
    private Entry entry;

    public Link() {
    }

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

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    @Override
    public IDataTransferModel toDataTransferObject() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
