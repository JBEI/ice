package org.jbei.ice.storage.model;

import org.jbei.ice.lib.manuscript.Manuscript;
import org.jbei.ice.lib.manuscript.ManuscriptStatus;
import org.jbei.ice.storage.DataModel;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Hector Plahar
 */
@Entity
@Table(name = "MANUSCRIPT")
@SequenceGenerator(name = "sequence", sequenceName = "manuscript_id_seq", allocationSize = 1)
public class ManuscriptModel implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @Column(name = "title")
    private String title;

    @OneToOne
    private Folder folder;

    @Column(name = "paragonUrl")
    private String paragonUrl;

    @Column(name = "author_first_name")
    private String authorFirstName;

    @Column(name = "author_last_name")
    private String authorLastName;

    @Enumerated(value = EnumType.STRING)
    private ManuscriptStatus status;

    @Column(name = "creation_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    @Override
    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public String getParagonUrl() {
        return paragonUrl;
    }

    public void setParagonUrl(String paragonUrl) {
        this.paragonUrl = paragonUrl;
    }

    public ManuscriptStatus getStatus() {
        return status;
    }

    public void setStatus(ManuscriptStatus status) {
        this.status = status;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public String getAuthorFirstName() {
        return authorFirstName;
    }

    public void setAuthorFirstName(String authorFirstName) {
        this.authorFirstName = authorFirstName;
    }

    public String getAuthorLastName() {
        return authorLastName;
    }

    public void setAuthorLastName(String authorLastName) {
        this.authorLastName = authorLastName;
    }

    @Override
    public Manuscript toDataTransferObject() {
        Manuscript manuscript = new Manuscript();
        manuscript.setId(this.id);
        manuscript.setCreationTime(this.creationTime.getTime());
        manuscript.setTitle(this.title);
        manuscript.setAuthorFirstName(this.authorFirstName);
        manuscript.setAuthorLastName(this.authorLastName);
        manuscript.setParagonUrl(this.paragonUrl);
        manuscript.setStatus(this.status);
        if (folder != null)
            manuscript.setFolder(this.folder.toDataTransferObject());
        return manuscript;
    }
}
