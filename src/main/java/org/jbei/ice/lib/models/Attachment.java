package org.jbei.ice.lib.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.vo.IAttachmentValueObject;

@Entity
@Table(name = "attachments")
@SequenceGenerator(name = "sequence", sequenceName = "attachments_id_seq", allocationSize = 1)
public class Attachment implements IAttachmentValueObject, IModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private int id;

    @Column(name = "description", nullable = false)
    @Lob
    private String description;

    @Column(name = "filename", length = 255, nullable = false)
    private String fileName;

    @Column(name = "file_id", length = 36, nullable = false)
    private String fileId;

    @ManyToOne
    @JoinColumn(name = "entries_id", nullable = false)
    private Entry entry;

    public Attachment() {
    }

    /**
     * Attachment constructor
     * 
     * @param description
     *            description
     * @param fileName
     *            file name
     * @param entry
     *            Entry instance
     * @param data
     *            base64 encoded string
     */
    public Attachment(String description, String fileName, Entry entry) {
        this.description = description;
        this.fileName = fileName;
        this.entry = entry;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }
}
