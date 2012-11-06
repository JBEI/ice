package org.jbei.ice.lib.vo;

import java.util.ArrayList;

import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.models.Sequence;

/**
 * Value object to hold complete {@link Entry} data, including {@link Sequence} and
 * {@link AttachmentData}, and {@link SequenceTraceFile}.
 *
 * @author Timothy Ham
 */
public class CompleteEntry {
    private Entry entry;
    private ArrayList<AttachmentData> attachments = new ArrayList<AttachmentData>();
    private Sequence sequence;
    private ArrayList<SequenceTraceFile> traceFiles = new ArrayList<SequenceTraceFile>();

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    public Sequence getSequence() {
        return sequence;
    }

    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    public ArrayList<AttachmentData> getAttachments() {
        return attachments;
    }

    public void setAttachments(ArrayList<AttachmentData> attachments) {
        this.attachments = attachments;
    }

    public ArrayList<SequenceTraceFile> getTraceFiles() {
        return traceFiles;
    }

    public void setTraceFiles(ArrayList<SequenceTraceFile> traceFiles) {
        this.traceFiles = traceFiles;
    }

}