package org.jbei.ice.lib.dto.entry;

import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.storage.IDataTransferModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hector Plahar
 * <p>
 * Wrapper (POJO) for part data and sequence
 */
public class PartWithSequence implements IDataTransferModel {

    private PartData part;
    private FeaturedDNASequence sequence;
    private List<PartWithSequence> children;
    private PartSource partSource;

    public PartWithSequence() {
        this.children = new ArrayList<>();
    }

    public PartWithSequence(PartData partData, FeaturedDNASequence sequence) {
        this.part = partData;
        this.sequence = sequence;
        this.part.setHasSequence(this.sequence != null);
        this.children = new ArrayList<>();
    }

    public void setPart(PartData part) {
        this.part = part;
    }

    public void setSequence(FeaturedDNASequence sequence) {
        this.sequence = sequence;
    }

    public PartData getPart() {
        return this.part;
    }

    public FeaturedDNASequence getSequence() {
        return this.sequence;
    }

    public List<PartWithSequence> getChildren() {
        return this.children;
    }

    public PartSource getPartSource() {
        return partSource;
    }

    public void setPartSource(PartSource partSource) {
        this.partSource = partSource;
    }
}
