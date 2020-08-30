package org.jbei.ice.storage.model;

import org.hibernate.annotations.Type;
import org.jbei.ice.storage.DataModel;
import org.jbei.ice.storage.IDataTransferModel;

import javax.persistence.*;
import java.util.Date;

/**
 * Changes made to sequence information for an entry
 */
@Entity
@Table(name = "sequence_history")
@SequenceGenerator(name = "sequence_history_id", sequenceName = "sequence_history_id_seq", allocationSize = 1)
public class SequenceHistoryModel implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "audit_id")
    private long id;

    @Column(name = "action", length = 12, nullable = false)
    private String action;

    @Column(name = "userId", length = 127, nullable = false)
    private String userId;

    @Column(name = "sequence_string")
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String sequenceString;

    @Column(name = "time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date time;

    @ManyToOne
    @JoinColumn(name = "sequence_id")
    private Sequence sequence;

    // todo : add session to indicate if a group of history actions resulted in multiple changes
    // todo : eg. changing the base pairs where there is a feature will change the sequence information
    @Column(name = "session_id")
    private String sessionId;

    @Override
    public long getId() {
        return id;
    }


    @Override
    public IDataTransferModel toDataTransferObject() {
        return null;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Sequence getSequence() {
        return sequence;
    }

    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSequenceString() {
        return sequenceString;
    }

    public void setSequenceString(String sequenceString) {
        this.sequenceString = sequenceString;
    }
}
