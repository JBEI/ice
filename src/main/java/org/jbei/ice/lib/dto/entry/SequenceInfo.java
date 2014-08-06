package org.jbei.ice.lib.dto.entry;

import org.jbei.ice.lib.dao.IDataTransferModel;
import org.jbei.ice.lib.vo.DNASequence;

/**
 * @author Hector Plahar
 */
public class SequenceInfo implements IDataTransferModel {

    private String filename;
    private String fileId;
    private DNASequence sequence;
    private long entryId;

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
}
