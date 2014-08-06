package org.jbei.ice.lib.news;

import java.util.Date;
import javax.persistence.*;

import org.jbei.ice.lib.dao.IDataModel;
import org.jbei.ice.lib.dao.IDataTransferModel;

import org.hibernate.annotations.Type;

/**
 * Store information about a news item.
 *
 * @author Timothy Ham, Zinovii Dmytriv
 */
@Entity
@Table(name = "news")
@SequenceGenerator(name = "sequence", sequenceName = "news_id_seq", allocationSize = 1)
public class News implements IDataModel {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @Column(name = "is_published", nullable = false)
    private int isPublished;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "body", nullable = false)
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String body;

    @Column(name = "creation_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    @Column(name = "modification_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificationTime;

    @Column(name = "publication_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date publicationTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getIsPublished() {
        return isPublished;
    }

    public void setIsPublished(int isPublished) {
        this.isPublished = isPublished;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(Date modificationTime) {
        this.modificationTime = modificationTime;
    }

    public Date getPublicationTime() {
        return publicationTime;
    }

    public void setPublicationTime(Date publicationTime) {
        this.publicationTime = publicationTime;
    }

    @Override
    public IDataTransferModel toDataTransferObject() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
