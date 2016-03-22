package org.jbei.ice.storage.model;

import org.jbei.ice.lib.dto.History;
import org.jbei.ice.storage.DataModel;

import javax.persistence.*;
import java.util.Date;

/**
 * Audit object for keep track of actions affecting entries.
 * Actions include : edit, view
 *
 * @author Hector Plahar
 */
@Entity
@Table(name = "audit")
@SequenceGenerator(name = "sequence", sequenceName = "audit_id_seq", allocationSize = 1)
public class Audit implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    // what
    @Column(name = "action", length = 12, nullable = false)
    private String action;

    // who
    @Column(name = "userId", length = 127, nullable = false)
    private String userId;

    @OneToOne
    @JoinColumn(name = "remote_client_id", nullable = true)
    private RemoteClientModel remoteClientModel;

    @Column(name = "institution", length = 255)
    private String institution;

    @Column(name = "firstname", length = 50)
    private String firstName;

    @Column(name = "lastName", length = 50)
    private String lastName;

    // when
    @Column(name = "time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date time;

    // entity (one entry can have many audits, one audit cannot have many entries)
    @ManyToOne
    @JoinColumn(name = "entry_id")
    private Entry entry;

    public long getId() {
        return id;
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

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public RemoteClientModel getRemoteClientModel() {
        return remoteClientModel;
    }

    public void setRemoteClientModel(RemoteClientModel remoteClientModel) {
        this.remoteClientModel = remoteClientModel;
    }

    @Override
    public History toDataTransferObject() {
        History history = new History();
        history.setId(id);
        history.setAction(action);
        history.setTime(time.getTime());
        history.setUserId(userId);
        if (remoteClientModel != null) {
            history.setPartner(remoteClientModel.getRemotePartner().toDataTransferObject());
        }
        return history;
    }
}
