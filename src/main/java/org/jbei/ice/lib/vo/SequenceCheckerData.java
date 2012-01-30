package org.jbei.ice.lib.vo;

import java.io.Serializable;
import java.util.List;

/**
 * Value object to hold SequenceChecker data.
 * 
 * @author Zinovii Dmytriv
 * 
 */
public class SequenceCheckerData implements Serializable {
    private static final long serialVersionUID = 1L;

    private FeaturedDNASequence sequence;
    private List<TraceData> traces;

    public FeaturedDNASequence getSequence() {
        return sequence;
    }

    public void setSequence(FeaturedDNASequence sequence) {
        this.sequence = sequence;
    }

    public List<TraceData> getTraces() {
        return traces;
    }

    public void setTraces(List<TraceData> traces) {
        this.traces = traces;
    }
}
