package org.jbei.ice.lib.vo;

import java.io.Serializable;
import java.util.ArrayList;

import org.jbei.ice.lib.shared.dto.entry.PartData;

/**
 * A bit more heavy weight object than PartTransfer
 * Contains optional information about sequences and trace sequences
 *
 * @author Hector Plahar
 */
public class SequencePartTransfer implements Serializable {

    public static final long serialVersionUID = 1l;

    private PartData part;
    private ArrayList<PartAttachment> attachments;
    private FeaturedDNASequence sequence;

    public SequencePartTransfer() {
        attachments = new ArrayList<>();
    }

    public PartData getPart() {
        return part;
    }

    public void setPart(PartData part) {
        this.part = part;
    }

    public FeaturedDNASequence getSequence() {
        return sequence;
    }

    public void setSequence(FeaturedDNASequence sequence) {
        this.sequence = sequence;
    }

    public ArrayList<PartAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(ArrayList<PartAttachment> attachments) {
        this.attachments = attachments;
    }
}
