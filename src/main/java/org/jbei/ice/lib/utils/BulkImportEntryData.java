package org.jbei.ice.lib.utils;

import org.jbei.ice.lib.entry.model.Entry;

import java.io.Serializable;

/**
 * A wrapper representing an entry, attachment and sequence file tuple
 *
 * @author Hector Plahar
 */

public class BulkImportEntryData implements Serializable {

    private static final long serialVersionUID = 1L;

    private Entry entry;
    private String attachmentFilename;
    private String sequenceFilename;

    public BulkImportEntryData() {
    }

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    public String getAttachmentFilename() {
        return attachmentFilename;
    }

    public void setAttachmentFilename(String attachmentFilename) {
        this.attachmentFilename = attachmentFilename;
    }

    public String getSequenceFilename() {
        return sequenceFilename;
    }

    public void setSequenceFilename(String sequenceFilename) {
        this.sequenceFilename = sequenceFilename;
    }
}
