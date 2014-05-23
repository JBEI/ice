package org.jbei.ice.lib.vo;

import java.util.Date;

/**
 * Value object for Sequence Checker project.
 *
 * @author Zinovii Dmytriv
 */
public class SequenceCheckerProject extends Project {
    private static final long serialVersionUID = 1L;

    private SequenceCheckerData sequenceCheckerData;

    public SequenceCheckerProject() {
        super();
    }

    public SequenceCheckerProject(String name, String description, String uuid, String ownerEmail,
            String ownerName, Date creationTime, Date modificationTime,
            SequenceCheckerData sequenceCheckerData) {
        super(name, description, uuid, ownerEmail, ownerName, creationTime, modificationTime);

        this.sequenceCheckerData = sequenceCheckerData;
    }

    public SequenceCheckerData getSequenceCheckerData() {
        return sequenceCheckerData;
    }

    public void setSequenceCheckerData(SequenceCheckerData sequenceCheckerData) {
        this.sequenceCheckerData = sequenceCheckerData;
    }

    @Override
    public String typeName() {
        return "sequence-checker";
    }
}