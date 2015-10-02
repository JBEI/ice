package org.jbei.ice.storage.model;

import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.jbei.ice.lib.dto.entry.AttachmentInfo;
import org.jbei.ice.storage.DataModel;
import org.jbei.ice.storage.hibernate.bridge.EntryBooleanPropertiesBridge;
import org.jbei.ice.storage.hibernate.dao.AttachmentDAO;

import javax.persistence.*;

/**
 * Store information about attachments.
 * <p/>
 * The actual bytes are written to a file on disk. See {@link AttachmentDAO}.
 *
 * @author Timothy Ham, Zinovii Dmytriv
 */
@Entity
@Table(name = "attachments")
@SequenceGenerator(name = "sequence", sequenceName = "attachments_id_seq", allocationSize = 1)
public class Attachment implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @Column(name = "description", nullable = false)
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String description;

    /**
     * The original name of the file.
     */
    @Column(name = "filename", length = 255, nullable = false)
    private String fileName;

    /**
     * Randomly generated UUID assigned to the file on disk.
     */
    @Column(name = "file_id", length = 36, nullable = false)
    private String fileId;

    @ContainedIn
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entries_id", nullable = false)
    @Field(bridge = @FieldBridge(impl = EntryBooleanPropertiesBridge.class, params = {
            @org.hibernate.search.annotations.Parameter(name = "boolean", value = "hasAttachment")
    }))
    private Entry entry;

    public Attachment() {
    }

    /**
     * Attachment constructor.
     *
     * @param description Description.
     * @param fileName    Original file name.
     * @param entry       Entry instance.
     */
    public Attachment(String description, String fileName, Entry entry) {
        this.description = description;
        this.fileName = fileName;
        this.entry = entry;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    @Override
    public AttachmentInfo toDataTransferObject() {
        AttachmentInfo info = new AttachmentInfo();
        info.setFileId(fileId);
        info.setFilename(fileName);
        info.setDescription(description);
        info.setId(id);
        return info;
    }
}
