package org.jbei.ice.lib.dto.entry;

import org.jbei.ice.lib.dto.DNASequence;
import org.jbei.ice.lib.entry.sequence.SequenceFormat;
import org.jbei.ice.storage.IDataTransferModel;

/**
 * Data transfer object for sequence
 *
 * @author Hector Plahar
 */
public class SequenceInfo implements IDataTransferModel {

    private String filename;
    private String fileId;
    private DNASequence sequence;
    private long entryId;
    private SequenceFormat format;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public DNASequence getSequence() {
        return sequence;
    }

    public void setSequence(DNASequence sequence) {
        this.sequence = sequence;
    }

    public long getEntryId() {
        return entryId;
    }

    public void setEntryId(long entryId) {
        this.entryId = entryId;
    }

    public SequenceFormat getFormat() {
        return format;
    }

    public void setFormat(SequenceFormat format) {
        this.format = format;
    }
}
