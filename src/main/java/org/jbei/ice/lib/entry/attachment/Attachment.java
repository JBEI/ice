package org.jbei.ice.lib.entry.attachment;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.models.interfaces.IAttachmentValueObject;

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
public class Attachment implements IAttachmentValueObject, IModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private long id;

    @Column(name = "description", nullable = false)
    @Lob
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entries_id", nullable = false)
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

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getFileId() {
        return fileId;
    }

    @Override
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    @Override
    public Entry getEntry() {
        return entry;
    }

    @Override
    public void setEntry(Entry entry) {
        this.entry = entry;
    }
}
