package org.jbei.ice.lib.vo;

import java.io.Serializable;
import java.util.ArrayList;

import org.jbei.ice.lib.shared.dto.entry.PartData;

/**
 * Wrapper for part and all other information about it meant
 * for transfer to other registries
 *
 * @author Hector Plahar
 */
public class PartTransfer implements Serializable {

    public static final long serialVersionUID = 1l;

    private PartData part;
    private ArrayList<PartAttachment> attachments;
    private String sequenceString;

    public PartTransfer() {
        attachments = new ArrayList<>();
    }

    public PartData getPart() {
        return part;
    }

    public void setPart(PartData part) {
        this.part = part;
    }

    public ArrayList<PartAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(ArrayList<PartAttachment> attachments) {
        this.attachments = attachments;
    }

    public String getSequenceString() {
        return sequenceString;
    }

    public void setSequenceString(String sequenceString) {
        this.sequenceString = sequenceString;
    }
}
