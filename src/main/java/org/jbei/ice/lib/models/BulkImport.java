package org.jbei.ice.lib.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.utils.BulkImportEntryData;

@Entity
@Table(name = "bulk_import")
@SequenceGenerator(name = "sequence", sequenceName = "bulk_import_id_seq", allocationSize = 1)
public class BulkImport implements IModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "data")
    private ArrayList<BulkImportEntryData> data; // each corresponds to a row in the bulk import sheet

    @Column(name = "data2")
    private ArrayList<BulkImportEntryData> data2; // to get around the issue of strain with plasmid 

    @Column(name = "sequence_filename")
    private String sequenceFilename;

    @Column(name = "sequence_file")
    @Lob
    private Byte[] sequenceFile;

    @Column(name = "attachment_file")
    @Lob
    private Byte[] attachmentFile;

    @Column(name = "attachment_filename")
    private String attachmentFilename;

    @Column(name = "type")
    private String type;

    @Column(name = "creation_time")
    private Date creationTime;

    public BulkImport() {
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Account getAccount() {
        return this.account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public List<BulkImportEntryData> getPrimaryData() {
        return data;
    }

    public void setPrimaryData(ArrayList<BulkImportEntryData> wrapper) {
        this.data = wrapper;
    }

    public List<BulkImportEntryData> getSecondaryData() {
        return this.data2;
    }

    public void setSecondaryData(ArrayList<BulkImportEntryData> data) {
        this.data2 = data;
    }

    public String getSequenceFilename() {
        return sequenceFilename;
    }

    public void setSequenceFilename(String sequenceFilename) {
        this.sequenceFilename = sequenceFilename;
    }

    public Byte[] getSequenceFile() {
        return sequenceFile;
    }

    public void setSequenceFile(Byte[] sequenceFile) {
        this.sequenceFile = sequenceFile;
    }

    public Byte[] getAttachmentFile() {
        return attachmentFile;
    }

    public void setAttachmentFile(Byte[] attachmentFile) {
        this.attachmentFile = attachmentFile;
    }

    public String getAttachmentFilename() {
        return attachmentFilename;
    }

    public void setAttachmentFilename(String attachmentFilename) {
        this.attachmentFilename = attachmentFilename;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getCreationTime() {
        return this.creationTime;
    }

    public void setCreationTime(Date date) {
        this.creationTime = date;
    }
}
